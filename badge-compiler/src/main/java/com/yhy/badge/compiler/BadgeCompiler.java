package com.yhy.badge.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.yhy.badge.annotation.BadgeViews;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2018-03-12 10:42
 * version: 1.0.0
 * desc   : 生成BadgeView的代码生成器
 */
@AutoService(Processor.class)
public class BadgeCompiler extends AbstractProcessor {
    private static final String CLASS_JAVA_DOC = "BadgeView compiler\r\n\r\n@author : 颜洪毅\r\n@e-mail : yhyzgn@gmail.com\r\n@github : https://github.com/yhyzgn\r\n";
    private static final String PACKAGE_NAME = "com.yhy.badge";
    private static final String CLASS_PREFIX = "Badge";

    private Filer mFileUtils;
    private Messager mMessager;

    /**
     * 初始化
     *
     * @param processingEnvironment 编译环境
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFileUtils = processingEnv.getFiler();
        mMessager = processingEnv.getMessager();
    }

    /**
     * 获取该编译器所支持的Java版本
     *
     * @return 该编译器所支持的Java版本
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * 告知 Processor 哪些注解需要处理
     *
     * @return 返回一个 Set 集合，集合内容为自定义注解的包名+类名
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        final Set<String> annotationTypes = new LinkedHashSet<>();
        annotationTypes.add(BadgeViews.class.getCanonicalName());
        return annotationTypes;
    }

    /**
     * 所有的注解处理都是从这个方法开始的，当 APT 找到所有需要处理的注解后，会回调这个方法。当没有属于该 Processor 处理的注解被使用时，不会回调该方法
     *
     * @param set              所有的由该 Processor 处理，并待处理的 Annotations「属于该 Processor 处理的注解，但并未被使用，不存在与这个集合里」
     * @param roundEnvironment 表示当前或是之前的运行环境，可以通过该对象查找到注解
     * @return 表示这组 Annotation 是否被这个 Processor 消费，如果消费「返回 true」后续子的 Processor 不会再对这组 Annotation 进行处理
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BadgeViews.class);
        if (elements == null || elements.isEmpty()) {
            return true;
        }
        mMessager.printMessage(Diagnostic.Kind.NOTE, "====================================== BadgeCompiler process START ======================================");
        Set<String> viewClassSet = new HashSet<>();
        parseParams(elements, viewClassSet);
        try {
            generate(viewClassSet);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IOException e) {
            mMessager.printMessage(Diagnostic.Kind.ERROR, "Exception occurred when generating class file.");
            e.printStackTrace();
        }
        mMessager.printMessage(Diagnostic.Kind.NOTE, "====================================== BadgeCompiler process END ======================================");
        return true;
    }

    /**
     * 解析注解参数
     *
     * @param elements     注解节点
     * @param viewClassSet 注解中所注册的类名集合
     */
    private void parseParams(Set<? extends Element> elements, Set<String> viewClassSet) {
        for (Element element : elements) {
            checkAnnotationValid(element, BadgeViews.class);
            TypeElement classElement = (TypeElement) element;
            // 获取该注解的值
            BadgeViews annotation = classElement.getAnnotation(BadgeViews.class);
            try {
                annotation.value();
            } catch (MirroredTypesException e) {
                List<? extends TypeMirror> typeMirrors = e.getTypeMirrors();
                for (TypeMirror typeMirror : typeMirrors) {
                    DeclaredType classTypeMirror = (DeclaredType) typeMirror;
                    TypeElement classTypeElement = (TypeElement) classTypeMirror.asElement();
                    String qualifiedName = classTypeElement.getQualifiedName().toString();
                    viewClassSet.add(qualifiedName);
                }
            }
        }
    }

    /**
     * 生成代码
     *
     * @param viewClassSet 需要生成BadgeView的类
     * @throws IllegalAccessException 访问异常
     * @throws IOException            IO异常
     */
    private void generate(Set<String> viewClassSet) throws IllegalAccessException, IOException {
        mMessager.printMessage(Diagnostic.Kind.NOTE, "生成 " + viewClassSet.size() + " 个");

        for (String clazz : viewClassSet) {
            int lastDotIndex = clazz.lastIndexOf(".");
            String superPackageName = clazz.substring(0, lastDotIndex);
            String superClassName = clazz.substring(lastDotIndex + 1);
            String className = CLASS_PREFIX + superClassName;

            mMessager.printMessage(Diagnostic.Kind.NOTE, clazz + " ====> " + className);

            TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(className)
                    .addJavadoc(CLASS_JAVA_DOC)
                    .addModifiers(Modifier.PUBLIC)
                    .superclass(ClassName.get(superPackageName, superClassName))
                    .addSuperinterface(ClassName.get(PACKAGE_NAME, "Badge"))
                    .addField(ClassName.get(PACKAGE_NAME, "BadgeViewHelper"), "mBadgeViewHelper", Modifier.PRIVATE);

            generateMethod(typeBuilder, clazz);

            JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, typeBuilder.build()).build();
            javaFile.writeTo(mFileUtils);
        }
    }

    /**
     * 生成相关方法
     *
     * @param typeBuilder 类构造器
     * @param clazz       当前正在生成的类名
     */
    private void generateMethod(TypeSpec.Builder typeBuilder, String clazz) {
        constructor(typeBuilder, clazz);
        onTouchEvent(typeBuilder);
        callSuperOnTouchEvent(typeBuilder);
        if (isAssignable(clazz, "android.view.ViewGroup")) {
            dispatchDraw(typeBuilder);
        } else {
            onDraw(typeBuilder);
        }
        showCirclePointBadge(typeBuilder);
        showTextBadge(typeBuilder);
        hiddenBadge(typeBuilder);
        showDrawableBadge(typeBuilder);
        setOnDismissListener(typeBuilder);
        isShowBadge(typeBuilder);
        getBadgeViewHelper(typeBuilder);
    }

    /**
     * 生成构造方法
     *
     * @param typeBuilder 类构造器
     * @param clazz       当前正在生成的类名
     */
    private void constructor(TypeSpec.Builder typeBuilder, String clazz) {
        TypeName contextType = ClassName.get("android.content", "Context");
        TypeName attributeSetType = ClassName.get("android.util", "AttributeSet");

        MethodSpec constructorOne = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(contextType, "context")
                .addStatement("this(context, null)")
                .build();

        // 该构造方法中，如果是RadioButton的子类，需要将defStyleAttr参数设置为“android.R.attr.radioButtonStyle”，否则无法点击选中；其他控件默认为0即可
        MethodSpec constructorTwo = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(contextType, "context")
                .addParameter(attributeSetType, "attrs")
                .addStatement("this(context, attrs, " + (isAssignable(clazz, "android.widget.RadioButton") ? "android.R.attr.radioButtonStyle" : "0") + ")")
                .build();

        MethodSpec.Builder constructorThreeBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(contextType, "context")
                .addParameter(attributeSetType, "attrs")
                .addParameter(int.class, "defStyleAttr")
                .addStatement("super(context, attrs, defStyleAttr)");

        if (isAssignable(clazz, "android.widget.ImageView") || isAssignable(clazz, "android.widget.RadioButton")) {
            constructorThreeBuilder.addStatement("mBadgeViewHelper = new BadgeViewHelper(this, context, attrs, BadgeViewHelper.BadgeGravity.RightTop)");
        } else {
            constructorThreeBuilder.addStatement(
                    "mBadgeViewHelper = new BadgeViewHelper(this, context, attrs, BadgeViewHelper.BadgeGravity.RightCenter)");
        }

        typeBuilder.addMethod(constructorOne)
                .addMethod(constructorTwo)
                .addMethod(constructorThreeBuilder.build());
    }

    /**
     * 生成onTouchEvent方法
     *
     * @param typeBuilder 类构造器
     */
    private void onTouchEvent(TypeSpec.Builder typeBuilder) {
        MethodSpec methodSpec = MethodSpec.methodBuilder("onTouchEvent")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get("android.view", "MotionEvent"), "event")
                .addStatement("return mBadgeViewHelper.onTouchEvent(event)")
                .returns(boolean.class)
                .build();
        typeBuilder.addMethod(methodSpec);
    }

    /**
     * 生成callSuperOnTouchEvent方法
     *
     * @param typeBuilder 类构造器
     */
    private void callSuperOnTouchEvent(TypeSpec.Builder typeBuilder) {
        MethodSpec methodSpec = MethodSpec.methodBuilder("callSuperOnTouchEvent")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get("android.view", "MotionEvent"), "event")
                .addStatement("return super.onTouchEvent(event)")
                .returns(boolean.class)
                .build();
        typeBuilder.addMethod(methodSpec);
    }

    /**
     * 生成onDraw方法
     *
     * @param typeBuilder 类构造器
     */
    private void onDraw(TypeSpec.Builder typeBuilder) {
        MethodSpec methodSpec = MethodSpec.methodBuilder("onDraw")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get("android.graphics", "Canvas"), "canvas")
                .addStatement("super.onDraw(canvas)")
                .addStatement("mBadgeViewHelper.drawBadge(canvas)")
                .build();
        typeBuilder.addMethod(methodSpec);
    }

    /**
     * 生成dispatchDraw方法
     *
     * @param typeBuilder 类构造器
     */
    private void dispatchDraw(TypeSpec.Builder typeBuilder) {
        MethodSpec methodSpec = MethodSpec.methodBuilder("dispatchDraw")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get("android.graphics", "Canvas"), "canvas")
                .addStatement("super.dispatchDraw(canvas)")
                .addStatement("mBadgeViewHelper.drawBadge(canvas)")
                .build();
        typeBuilder.addMethod(methodSpec);
    }

    /**
     * 生成showCirclePointBadge方法
     *
     * @param typeBuilder 类构造器
     */
    private void showCirclePointBadge(TypeSpec.Builder typeBuilder) {
        MethodSpec methodSpec = MethodSpec.methodBuilder("showCirclePointBadge")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("mBadgeViewHelper.showCirclePointBadge()")
                .build();
        typeBuilder.addMethod(methodSpec);
    }

    /**
     * 生成showTextBadge方法
     *
     * @param typeBuilder 类构造器
     */
    private void showTextBadge(TypeSpec.Builder typeBuilder) {
        MethodSpec methodSpec = MethodSpec.methodBuilder("showTextBadge")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "badgeText")
                .addStatement("mBadgeViewHelper.showTextBadge(badgeText)")
                .build();
        typeBuilder.addMethod(methodSpec);
    }

    /**
     * 生成hiddenBadge方法
     *
     * @param typeBuilder 类构造器
     */
    private void hiddenBadge(TypeSpec.Builder typeBuilder) {
        MethodSpec methodSpec = MethodSpec.methodBuilder("hiddenBadge")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("mBadgeViewHelper.hiddenBadge()")
                .build();
        typeBuilder.addMethod(methodSpec);
    }

    /**
     * 生成showDrawableBadge方法
     *
     * @param typeBuilder 类构造器
     */
    private void showDrawableBadge(TypeSpec.Builder typeBuilder) {
        MethodSpec methodSpec = MethodSpec.methodBuilder("showDrawableBadge")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get("android.graphics", "Bitmap"), "bitmap")
                .addStatement("mBadgeViewHelper.showDrawable(bitmap)")
                .build();
        typeBuilder.addMethod(methodSpec);
    }

    /**
     * 生成setOnDismissListener方法
     *
     * @param typeBuilder 类构造器
     */
    private void setOnDismissListener(TypeSpec.Builder typeBuilder) {
        MethodSpec methodSpec = MethodSpec.methodBuilder("setOnDismissListener")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get(PACKAGE_NAME, "OnDismissListener"), "listener")
                .addStatement("mBadgeViewHelper.setOnDismissListener(listener)")
                .build();
        typeBuilder.addMethod(methodSpec);
    }

    /**
     * 生成isShowBadge方法
     *
     * @param typeBuilder 类构造器
     */
    private void isShowBadge(TypeSpec.Builder typeBuilder) {
        MethodSpec methodSpec = MethodSpec.methodBuilder("isShowBadge")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return mBadgeViewHelper.isShowBadge()")
                .returns(boolean.class)
                .build();
        typeBuilder.addMethod(methodSpec);
    }

    /**
     * 生成getBadgeViewHelper方法
     *
     * @param typeBuilder 类构造器
     */
    private void getBadgeViewHelper(TypeSpec.Builder typeBuilder) {
        MethodSpec methodSpec = MethodSpec.methodBuilder("getBadgeViewHelper")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return mBadgeViewHelper")
                .returns(ClassName.get(PACKAGE_NAME, "BadgeViewHelper"))
                .build();
        typeBuilder.addMethod(methodSpec);
    }

    /**
     * 检查注解节点是否有效
     *
     * @param annotatedElement 解节点
     * @param clazz            当前注解的类
     * @return 是否有效
     */
    private boolean checkAnnotationValid(Element annotatedElement, Class clazz) {
        if (annotatedElement.getKind() != ElementKind.CLASS) {
            error(annotatedElement, "%s must be declared on class.", clazz.getSimpleName());
            return false;
        }

        if (annotatedElement.getModifiers().contains(Modifier.PRIVATE)) {
            error(annotatedElement, "%s must can not be private.", ((TypeElement) annotatedElement).getQualifiedName());
            return false;
        }
        return true;
    }

    /**
     * 错误信息打印
     *
     * @param element 当前节点
     * @param message 错误信息
     * @param args    错误信息格式化参数
     */
    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        mMessager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    /**
     * 某个类是否是另一个类的子类
     *
     * @param childClazz 子类名
     * @param superClazz 父类名
     * @return 是否是子类
     */
    private boolean isAssignable(String childClazz, String superClazz) {
        return processingEnv.getTypeUtils().isAssignable(
                processingEnv.getElementUtils().getTypeElement(childClazz).asType(),
                processingEnv.getElementUtils().getTypeElement(superClazz).asType());
    }
}
