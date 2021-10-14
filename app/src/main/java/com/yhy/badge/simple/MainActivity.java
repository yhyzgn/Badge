package com.yhy.badge.simple;

import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yhy.badge.Badge;
import com.yhy.badge.BadgeRadioButton;
import com.yhy.badge.BadgeTextView;
import com.yhy.badge.OnDismissListener;
import com.yhy.badge.annotation.BadgeViews;

@BadgeViews({TextView.class, RadioButton.class})
public class MainActivity extends AppCompatActivity {

    private BadgeTextView btvTest;
    private RadioGroup rgTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btvTest = findViewById(R.id.btv_test);
        rgTest = findViewById(R.id.rg_test);


        btvTest.showTextBadge("23");
        btvTest.getBadgeViewHelper().setDragEnable(true);
        btvTest.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(Badge badge) {
                Toast.makeText(MainActivity.this, "消失了", Toast.LENGTH_SHORT).show();
            }
        });

        ((BadgeRadioButton) rgTest.getChildAt(0)).showTextBadge("12");
        ((BadgeRadioButton) rgTest.getChildAt(1)).showTextBadge("345");
        ((BadgeRadioButton) rgTest.getChildAt(2)).showTextBadge("哈哈");
        rgTest.check(rgTest.getChildAt(0).getId());

        rgTest.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Toast.makeText(MainActivity.this, getRbById(group, checkedId).getText(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private BadgeRadioButton getRbById(RadioGroup group, int checkedId) {
        return group.findViewById(checkedId);
    }
}
