# Badge

![JitPack](https://img.shields.io/jitpack/v/github/yhyzgn/Badge?color=brightgreen&label=badge) ![JitPack](https://img.shields.io/jitpack/v/github/yhyzgn/Badge?color=brightgreen&label=badge-annotation) ![JitPack](https://img.shields.io/jitpack/v/github/yhyzgn/Badge?color=brightgreen&label=badge-compiler) [![996.icu](https://img.shields.io/badge/link-996.icu-red.svg)](https://996.icu) [![LICENSE](https://img.shields.io/badge/license-Anti%20996-blue.svg)](https://github.com/996icu/996.ICU/blob/master/LICENSE)

> 一个注解让`view`生成徽章

**☆ 注意 ☆**

>   仅支持`AndroidX`

### 用法

* 引入`jitpack`

  > 仓库已迁移到`jitpack`

  ```groovy
  allprojects {
      repositories {
          // ...
          maven { url "https://jitpack.io" }
      }
  }
  ```

* `build.gradle`中添加以下依赖

  ```groovy
  dependencies {
      implementation 'com.github.yhyzgn.Badge:badge:latestVersion'
      annotationProcessor 'com.github.yhyzgn.Badge:badge-compiler:latestVersion'
  }
  ```

