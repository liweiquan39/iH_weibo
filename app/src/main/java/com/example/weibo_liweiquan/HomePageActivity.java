package com.example.weibo_liweiquan;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

public class HomePageActivity extends AppCompatActivity {
    private BottomNavigationView navigationView;
    private ViewPager viewPager;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        navigationView = findViewById(R.id.nav_bottom);
        viewPager = findViewById(R.id.vp);

        boolean isNetworkConnected1 = NetworkUtils.isNetworkConnected(this);
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new homeFragment(true, isNetworkConnected1));
        fragments.add(new mineFragment());


        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(),fragments);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(1);//保证在相邻页切换不会重新实例化（重走onCreateView）
        //底部导航栏
        navigationView.setItemIconTintList(null);//必须加这条！否则图标颜色错误！
        navigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener(){
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if (item.getItemId()==R.id.item_home){
                    viewPager.setCurrentItem(0);
                    return true;
                }
                else if(item.getItemId()==R.id.item_mine){
                    viewPager.setCurrentItem(1);
                    return true;
                }
                return false;
            }

        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position){  // 根据页面位置更新导航栏的选中状态
                    case 0:
                        navigationView.setSelectedItemId(R.id.item_home);
                        break;
                    case 1:
                        navigationView.setSelectedItemId(R.id.item_mine);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });


        Intent intent = getIntent();
        if(intent.getBooleanExtra("tick", false)){
            boolean isNetworkConnected2 = NetworkUtils.isNetworkConnected(this);
            fragments = new ArrayList<>();
            fragments.add(new homeFragment(false, isNetworkConnected2));
            fragments.add(new mineFragment());
            viewPagerAdapter.updateView(fragments);
            viewPager.setAdapter(viewPagerAdapter);
            viewPager.setCurrentItem(1);
        }
        else {
            viewPager.setCurrentItem(0);
        }

    }
}
