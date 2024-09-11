package com.example.weibo_liweiquan;

import static android.graphics.Color.pack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private AlertDialog alertDialog;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);

        if (isFirstLaunch()) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showPrivacyContent();
                }
            }, 1000);
            setContentView(R.layout.activity_main);
        } else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    navigateToHomePage();
                }
            }, 1000);
            setContentView(R.layout.activity_main);

        }
    }

    private boolean isFirstLaunch() {
        return sharedPreferences.getBoolean("first_launch", true);
    }

    private void showPrivacyContent() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialogview, null);

        TextView textView = dialogView.findViewById(R.id.dialog_detail);
        String text = "欢迎使用 iH微博，我们将严格遵守相关法律法规和隐私政策保护您的个人隐私。请您阅读并同意《用户协议》与《隐私政策》";
        SpannableString spannableString = new SpannableString(text);
        // 设置点击的部分
        ClickableSpan clickableSpan1 = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Toast.makeText(MainActivity.this, "查看用户协议", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ds.setColor(pack(Color.parseColor("#0D84FF")));       // 设置点击部分的文本颜色
                }
                ds.setUnderlineText(false);    // 设置点击部分的文本是否有下划线
            }
        };
        int start1 = text.indexOf("《用户协议》");
        int end1 = start1 + "《用户协议》".length();
        spannableString.setSpan(clickableSpan1, start1, end1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ClickableSpan clickableSpan2 = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Toast.makeText(MainActivity.this, "查看隐私政策", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ds.setColor(pack(Color.parseColor("#0D84FF")));       // 设置点击部分的文本颜色
                }
                ds.setUnderlineText(false);    // 设置点击部分的文本是否有下划线
            }
        };
        int start2 = text.indexOf("《隐私政策》");
        int end2 = start2 + "《隐私政策》".length();
        spannableString.setSpan(clickableSpan2, start2, end2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        alertDialog = builder.create();

        TextView noTextView = dialogView.findViewById(R.id.no_text);
        TextView yesTextView = dialogView.findViewById(R.id.yes_text);

        noTextView.setOnClickListener(v -> {
            finish();
        });
        yesTextView.setOnClickListener(v -> {
            sharedPreferences.edit().putBoolean("first_launch", false).apply();
            navigateToHomePage();
        });

        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawableResource(R.drawable.dialog_background);
        alertDialog.show();

        int targetWidthInDp = 260;
        int targetHeightInDp = 252;
        // 获取屏幕密度
        float density = Resources.getSystem().getDisplayMetrics().density;
        // 将dp转换为px
        int widthInPx = (int) (targetWidthInDp * density);
        int heightInPx = (int) (targetHeightInDp * density);
        WindowManager.LayoutParams params = alertDialog.getWindow().getAttributes();
        params.width = widthInPx;
        params.height = heightInPx;
        alertDialog.getWindow().setAttributes(params);

    }

    private void navigateToHomePage() {
        Intent intent = new Intent(this, HomePageActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }
}
