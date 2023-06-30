package com.example.sql_failure.post_activities;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.example.sql_failure.R;
import com.example.sql_failure.ViewPagerAdapter;
import com.facebook.stetho.Stetho;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private Button btLogin_out,btPost,btRead,btUpdate;
    private Spinner spnBase;
    private int basePos;
    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private ViewPagerAdapter myViewPagerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Stetho.initializeWithDefaults(this);

        build_views();
    }
    private void build_views(){
        drawerLayout=((DrawerLayout) findViewById(R.id.drawerLayout));
        navigationView=((NavigationView) findViewById(R.id.navigation_view));
        toolbar=((Toolbar) findViewById(R.id.mainToolbar));

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.drawer_open,R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_dehaze);

        btLogin_out=((Button) findViewById(R.id.btIdMainLogin));
        spnBase=((Spinner) findViewById(R.id.spnIdMainBase));
        ArrayAdapter adBase=new ArrayAdapter(MainActivity.this, R.layout.spinner_style_white,new String[]{"六家基地","台北基地","烏日基地","雲林基地","左營基地"});
        adBase.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spnBase.setAdapter(adBase);
        spnBase.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                basePos=i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btPost=((Button) findViewById(R.id.btIdPost));
        SpannableString styled=new SpannableString("登載\n工作檢查表");
        styled.setSpan(new TextAppearanceSpan(this,R.style.text_big),0,2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(new TextAppearanceSpan(this,R.style.text_small),2,8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        btPost.setText(styled);
        btPost.setOnClickListener(postListener);

        btRead=((Button) findViewById(R.id.btIdRead));
        styled=new SpannableString("查閱\n說明書本文");
        styled.setSpan(new TextAppearanceSpan(this,R.style.text_big),0,2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(new TextAppearanceSpan(this,R.style.text_small),2,8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        btRead.setText(styled);

        btUpdate=((Button) findViewById(R.id.btIdUpdate));
        styled=new SpannableString("更新\n本機資料");
        styled.setSpan(new TextAppearanceSpan(this,R.style.text_big),0,2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(new TextAppearanceSpan(this,R.style.text_small),2,7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        btUpdate.setText(styled);


        tabLayout= (TabLayout) findViewById(R.id.tlTabLayout);
        viewPager2=((ViewPager2) findViewById(R.id.vpViewPage));
        myViewPagerAdapter=new ViewPagerAdapter(this);
        viewPager2.setAdapter(myViewPagerAdapter);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.getTabAt(position).select();
            }
        });
    }
    private View.OnClickListener postListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent=new Intent(MainActivity.this, PostActivity.class);
            Bundle bundle=new Bundle();
            bundle.putString("basePos", String.valueOf(basePos));
            intent.putExtras(bundle);
            startActivity(intent);
        }
    };
}