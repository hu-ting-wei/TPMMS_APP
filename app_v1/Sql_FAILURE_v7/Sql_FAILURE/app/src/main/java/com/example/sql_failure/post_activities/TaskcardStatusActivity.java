package com.example.sql_failure.post_activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.sql_failure.CheckActivity;
import com.example.sql_failure.CompDBHper;
import com.example.sql_failure.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class TaskcardStatusActivity extends AppCompatActivity {
    private String taskcard_name,postMod,checked_date,selected_taskcard,selected_location,selected_attachment;
    private Toolbar statusToolbar;
    private TextView tvStatusTaskcard,tvCheckedDate,tvDays;
    private EditText etWeather,etTemperature,etHumidity,etWo,etResponsible,etWorker,etCompany;
    private Button btStatusNext,btBackHome;
    private boolean woEnable,responsibleEnable=false;//必填項目檢查
    private int basePos;
    private ArrayList<String> recSet=new ArrayList<>();
    private String edit_PMID;

    private static final String DBname="myDB1.db";
    private static final int DBversion=1;
    private static final String TBname="check_condition";
    CompDBHper compDBHper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taskcard_status);

        build_view();
    }
    private void build_view(){
        try {
            InputStream am=this.getAssets().open(DBname);
            compDBHper=new CompDBHper(this,DBname,null,DBversion,am);
        } catch (IOException e) {
            throw new Error(e.toString());
        }
        try {
            compDBHper.createDatabase();
        } catch (Exception e) {
            throw new Error("Database not created....");
        }
        //ID設定
        statusToolbar=((Toolbar) findViewById(R.id.statusToolbar));
        setSupportActionBar(statusToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);

        tvStatusTaskcard=((TextView) findViewById(R.id.tvIdStatusTaskcard));
        tvCheckedDate=((TextView) findViewById(R.id.tvIdDate));
        tvDays=((TextView) findViewById(R.id.tvIdDays));
        etWeather= ((EditText) findViewById(R.id.etIdWeather));
        etTemperature=((EditText) findViewById(R.id.etIdTemperature));
        etHumidity=((EditText) findViewById(R.id.etIdHumidity));
        etWo=((EditText) findViewById(R.id.etIdWo));
        etResponsible=((EditText) findViewById(R.id.etIdResponsible));
        etWorker=((EditText) findViewById(R.id.etIdWorker));
        etCompany=((EditText) findViewById(R.id.etIdCompany));
        btStatusNext=((Button) findViewById(R.id.btIdStatusNext));
        //提取資料
        postMod=getIntent().getExtras().getString("postMod");
        if (postMod.equals("new")){
            basePos= Integer.parseInt(getIntent().getExtras().getString("basePos"));
            taskcard_name=getIntent().getExtras().getString("taskcard_name");//顯示於toolbar
            checked_date=getIntent().getExtras().getString("checked_date");
            selected_taskcard=getIntent().getExtras().getString("selected_taskcard");
            selected_location=getIntent().getExtras().getString("selected_location");
            selected_attachment=getIntent().getExtras().getString("selected_attachment");
            //下一步按鈕判斷
            btStatusNext.setBackgroundResource(R.color.gray);
            btStatusNext.setText("請填寫相關資料");
            btStatusNext.setEnabled(false);
        }
        else {
            edit_PMID= getIntent().getExtras().getString("PMID");
            taskcard_name=getIntent().getExtras().getString("taskcard_name");//顯示於toolbar
            String SQL_command="SELECT * FROM " + TBname  + " WHERE PMID='" + edit_PMID + "'";   //拿取過去紀錄
            recSet=compDBHper.get("myDB1.db",SQL_command);
            ArrayList<String> history=pre_work();//去除#的內容(內文)
            checked_date=history.get(10);
            tvDays.setText("test");
            etWeather.setText(history.get(3));
            etTemperature.setText(history.get(4));
            etHumidity.setText(history.get(5));
            etWo.setText(history.get(6));
            etResponsible.setText(history.get(7));
            etWorker.setText(history.get(8));
            etCompany.setText("test");
            //下一步按鈕判斷
            woEnable=true;
            responsibleEnable=true;
            btStatusNext.setBackgroundResource(R.color.theme_teal);
            btStatusNext.setText("下一步");
            btStatusNext.setEnabled(true);
        }
        tvStatusTaskcard.setText(taskcard_name);
        tvStatusTaskcard.setSelected(true);

        tvCheckedDate.setText(checked_date);
        if(postMod.equals("new")){
            tvDays.setText("第一天");
        }
        etWo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(etWo.getText().toString().isEmpty()){
                    woEnable=false;
                }
                else{
                    woEnable=true;
                }
                if(woEnable&responsibleEnable){
                    btStatusNext.setBackgroundResource(R.color.theme_teal);
                    btStatusNext.setText("下一步");
                    btStatusNext.setEnabled(true);
                }
                else {
                    btStatusNext.setBackgroundResource(R.color.gray);
                    btStatusNext.setText("請填寫相關資料");
                    btStatusNext.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        etResponsible.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(etResponsible.getText().toString().isEmpty()){
                    responsibleEnable=false;
                }
                else {
                    responsibleEnable=true;
                }
                if(woEnable&responsibleEnable){
                    btStatusNext.setBackgroundResource(R.color.theme_teal);
                    btStatusNext.setText("下一步");
                    btStatusNext.setEnabled(true);
                }
                else {
                    btStatusNext.setBackgroundResource(R.color.gray);
                    btStatusNext.setText("請填寫相關資料");
                    btStatusNext.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        btBackHome=((Button) findViewById(R.id.btIdStatusBackHome));
        btBackHome.setOnClickListener(BackHomeListener);

        btStatusNext.setOnClickListener(statusNextListener);
    }
    private View.OnClickListener BackHomeListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent=new Intent(TaskcardStatusActivity.this, MainActivity.class);
            startActivity(intent);
        }
    };

    private View.OnClickListener statusNextListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String weather=etWeather.getText().toString();
            String temperature=etTemperature.getText().toString();
            String humidity=etHumidity.getText().toString();
            String wo=etWo.getText().toString();
            String responsible=etResponsible.getText().toString();
            String worker=etWorker.getText().toString();

            if (postMod.equals("new")){ //寫入資料庫
                String PMID;
                String SQL_command="SELECT PMID FROM " + TBname ;
                recSet=compDBHper.get("myDB1.db",SQL_command);
                ArrayList<String> PMID_arr=pre_work();//去除#的內容(內文)
                //流水號，初始為1，再來就是最後一筆的流水號往上+1
                if(PMID_arr.size()==0){
                    PMID="1";
                }
                else {
                    int pmid_value=Integer.parseInt(PMID_arr.get(PMID_arr.size()-1))+1;
                    PMID=String.valueOf(pmid_value);
                }


                String taskcard_attach_pkey=selected_taskcard + "-" + selected_attachment;
                String type="a";
                String[] baseList={"LUDP","TPEP","WUDP","YULP","ZUDP"};

                ArrayList<String> status_result=new ArrayList<>();
                status_result.add(PMID);
                status_result.add(taskcard_attach_pkey);
                status_result.add(type);
                status_result.add(selected_location);
                status_result.add(weather);
                status_result.add(temperature);
                status_result.add(humidity);
                status_result.add(wo);
                status_result.add(responsible);
                status_result.add(worker);
                status_result.add("1");//非正規
                status_result.add(checked_date);
                status_result.add(baseList[basePos]);//非正規

                compDBHper.set(TBname,null,status_result,null);
                edit_PMID=PMID;
            }
            else{   //更新資料庫
                String SQL_command="UPDATE " + TBname +
                                    " SET " +
                                            "weather='" + weather + "',temperature='" + temperature +
                                            "',humidity='" + humidity + "',WO='" + wo +
                                            "',WPS='" + responsible + "',workers='" + worker + "'" +
                                    " WHERE PMID='" + edit_PMID + "'";
                compDBHper.delete_update(SQL_command);
            }
            Intent intent=new Intent(TaskcardStatusActivity.this, CheckActivity.class);
            Bundle bundle=new Bundle();
            bundle.putString("taskcard_name",taskcard_name);
            bundle.putString("PMID", edit_PMID);
            bundle.putString("postMod",postMod);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    };
    /********工具*********/
    //資料庫資料處理
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
}