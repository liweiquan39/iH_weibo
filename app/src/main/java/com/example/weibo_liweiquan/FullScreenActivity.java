package com.example.weibo_liweiquan;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import android.view.MotionEvent;


public class FullScreenActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private TextView pageIndicator, downloadButton, userName;
    private ArrayList<String> imageUrls;
    private int currentIndex;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private ImageView userAvatar;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        viewPager = findViewById(R.id.viewPager);
        pageIndicator = findViewById(R.id.pageIndicator);
        downloadButton = findViewById(R.id.downloadButton);

        userAvatar = findViewById(R.id.avatar_image_detail);
        userName = findViewById(R.id.username_image_detail);
        userName.setText(getIntent().getStringExtra("username"));

        Glide.with(userAvatar)
                .load(getIntent().getStringExtra("useravatar"))
                .transform(new FitCenter(), new RoundedCorners(80))
                .into(userAvatar);


        imageUrls = getIntent().getStringArrayListExtra("imageUrls");
        currentIndex = getIntent().getIntExtra("imageIndex", 0);

        ImagePagerAdapter adapter = new ImagePagerAdapter(this, imageUrls);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentIndex);
        updatePageIndicator(currentIndex);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                currentIndex = position;
                updatePageIndicator(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        downloadButton.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            } else {
                if (NetworkUtils.isNetworkConnected(this)){
                    downloadImage(imageUrls.get(currentIndex));
                }
                else{
                    Toast.makeText(this, "当前无网络！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewPager.setOnTouchListener(new View.OnTouchListener() {
            private float startX;
            private float startY; // 通过移动距离判断是否单击还是滑动
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        float endX = event.getX();
                        float endY = event.getY();
                        if (isClick(startX, startY, endX, endY)) {
                            finish();
                            return true;
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    private boolean isClick(float startX, float startY, float endX, float endY) {
        float deltaX = Math.abs(startX - endX);
        float deltaY = Math.abs(startY - endY);
        return deltaX < 10 && deltaY < 10;
    }
    private void updatePageIndicator(int position) {
        pageIndicator.setText((position + 1) + "/" + imageUrls.size());
    }

    private void downloadImage(String imageUrl) {
        new Thread(() -> {
            try {
                runOnUiThread(() -> Toast.makeText(this, "正在下载图片！", Toast.LENGTH_SHORT).show());
                String name = imageUrl.substring(imageUrl.length()-20, imageUrl.length()-4);
                Log.i("downimagemine",name);
                new DownloadImageTask(new ImageView(getBaseContext()), name).execute(imageUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (NetworkUtils.isNetworkConnected(this)){
                    downloadImage(imageUrls.get(currentIndex));
                }
                else{
                    Toast.makeText(this, "当前无网络！", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void finish() {
        super.finish();
        // 设置返回动画
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}

