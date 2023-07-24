package com.example.sql_failure;

import static java.security.AccessController.getContext;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import com.example.sql_failure.check_fragment.CreateAFragment;
import com.example.sql_failure.check_fragment.CreateBFragment;
import com.example.sql_failure.check_fragment.CreateCFragment;
import com.example.sql_failure.check_fragment.HomeFragment;
import com.example.sql_failure.check_fragment.MMEFragment;
import com.example.sql_failure.check_fragment.StatusFragment;
import com.example.sql_failure.databinding.ActivityCheckBinding;
import com.example.sql_failure.post_activities.MainActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.sql_failure.CompDBHper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class CheckActivity extends AppCompatActivity {
    private ArrayList<String> recSet;
    static CompDBHper dbHper;

    private static final String DBname="myDB1.db";
    private static final int DBversion=1;
    private String mod="book";

    ActivityCheckBinding binding;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            InputStream am=this.getAssets().open(DBname);
            dbHper=new CompDBHper(this,DBname,null,DBversion,am);
        } catch (IOException e) {
            throw new Error(e.toString());
        }
        try {
            dbHper.createDatabase();
        } catch (Exception e) {
            throw new Error("Database not created....");
        }

        binding= ActivityCheckBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        /**
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        **/
        BottomNavigationView bottomNavigationView=((BottomNavigationView) findViewById(R.id.bottomNavigationView));
        bottomNavigationView.setItemIconTintList(null);

        replaceFragment(new HomeFragment());
        mod="book";
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {

            switch (item.getItemId()){
                case R.id.bnavHome:
                    ConfirmExit();
                    break;
                case R.id.bnavBook:
                    replaceFragment(new HomeFragment());
                    mod="book";
                    break;
                case R.id.bnavMME:
                    replaceFragment(new MMEFragment());
                    mod="mme";
                    break;
                case R.id.bnavStatus:
                    replaceFragment(new StatusFragment());
                    break;
                case R.id.bnavSave:
                    SaveData();
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
    private void SaveData(){
        String SQL_command="SELECT type,taskcard_attach_pkey,location_equipment_tk FROM " + "check_condition"  + " WHERE PMID=" + getIntent().getExtras().getString("PMID") + "";   //拿取過去紀錄
        recSet=dbHper.get(SQL_command);
        ArrayList<String> history=pre_work();
        switch (mod){
            case "book":
                switch (history.get(0)){
                    case "a":
                        CreateAFragment.save();
                        break;
                    case "b":
                        CreateBFragment.save();
                        break;
                    case "c":
                        CreateCFragment.save();
                        break;
                }
                break;
            case "mme":
                MMEFragment.save();
                break;
        }


    }
    private ArrayList<String> pre_work(){
        int recSet_size=recSet.size();
        ArrayList<String> AryResult = new ArrayList<String>();
        for(int i=0;i<recSet_size;i++){
            String[] buffer=recSet.get(i).split("#");
            for(int y=0;y< buffer.length;y++){
                //當搜尋結果分離#號後超過1組，依照順序將buffer中的內容塞到AryResult裡
                AryResult.add(buffer[y]);
            }
        }
        return AryResult;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//捕捉返回鍵
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            ConfirmExit();//按返回鍵，則執行退出確認
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    private void ConfirmExit(){//退出確認
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
                        SaveData();
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