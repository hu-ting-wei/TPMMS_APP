package com.example.sql_failure;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.example.sql_failure.check_fragment.BookFragment;
import com.example.sql_failure.check_fragment.HomeFragment;
import com.example.sql_failure.check_fragment.MMEFragment;
import com.example.sql_failure.check_fragment.StatusFragment;
import com.example.sql_failure.databinding.ActivityCheckBinding;
import com.example.sql_failure.post_activities.MainActivity;
import com.example.sql_failure.post_activities.TaskcardStatusActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class CheckActivity extends AppCompatActivity {

    ActivityCheckBinding binding;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding= ActivityCheckBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        /**
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        **/
        BottomNavigationView bottomNavigationView=((BottomNavigationView) findViewById(R.id.bottomNavigationView));
        bottomNavigationView.setItemIconTintList(null);

        replaceFragment(new HomeFragment());
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {

            switch (item.getItemId()){
                case R.id.bnavHome:
                    ConfirmExit();
                    break;
                case R.id.bnavBook:
                    replaceFragment(new HomeFragment());
                    break;
                case R.id.bnavMME:
                    replaceFragment(new MMEFragment());
                    break;
                case R.id.bnavStatus:
                    replaceFragment(new StatusFragment());
                    break;
            }
            return true;
        });
    }

    public ArrayList<String> toValue(){
        String taskcard_name=getIntent().getExtras().getString("taskcard_name");
        String PMID=getIntent().getExtras().getString("PMID");
        String postMod=getIntent().getExtras().getString("postMod");
        ArrayList<String> toFrag=new ArrayList<>();
        toFrag.add(taskcard_name);
        toFrag.add(String.valueOf(PMID));
        toFrag.add(postMod);
        return toFrag;
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout2,fragment);
        fragmentTransaction.commit();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//捕捉返回鍵
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            ConfirmExit();//按返回鍵，則執行退出確認
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    public void ConfirmExit(){//退出確認
        AlertDialog.Builder ad_leave=new AlertDialog.Builder(CheckActivity.this);
        ad_leave.setTitle("離開");
        ad_leave.setMessage("是否要離開?");
        ad_leave.setPositiveButton("是", new DialogInterface.OnClickListener() {//退出按鈕
            public void onClick(DialogInterface dialog, int i) {
                // TODO Auto-generated method stub
                AlertDialog.Builder ad_save=new AlertDialog.Builder(CheckActivity.this);
                ad_save.setTitle("存檔");
                ad_save.setMessage("是否要儲存尚未存檔之更動?");
                ad_save.setPositiveButton("是", new DialogInterface.OnClickListener() {//退出按鈕
                    public void onClick(DialogInterface dialog, int i) {
                        // TODO Auto-generated method stub
                        HomeFragment.save();

                        Intent intent=new Intent(CheckActivity.this, MainActivity.class);
                        startActivity(intent);

                    }
                });
                ad_save.setNegativeButton("否",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {

                        Intent intent=new Intent(CheckActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                });
                ad_save.show();//顯示對話框
            }
        });
        ad_leave.setNegativeButton("否",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {

            }
        });
        ad_leave.show();//顯示對話框
    }
}