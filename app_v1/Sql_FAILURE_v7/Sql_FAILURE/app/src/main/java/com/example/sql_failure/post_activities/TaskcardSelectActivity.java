package com.example.sql_failure.post_activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.example.sql_failure.CompDBHper;
import com.example.sql_failure.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

public class TaskcardSelectActivity extends AppCompatActivity {
    private Toolbar taskcardToolbar;
    private Spinner spnSelectBase;
    private int basePos;
    private Button btTaskcardNext,btBackHome;
    private TextView tvLocation;//在類型c中，"地點"需變更為"里程"
    private TextView tvAttach;//在類型c中，"附件"需隱藏
    private Button btDate,btTaskcard,btLocation,btAttachment;
    private Calendar calendar;
    private DatePickerDialog datePicker;

    private ArrayList<String> recSet;
    private String SQL_command;
    private static final String DBname="myDB1.db";
    private static final int DBversion=1;

    private boolean dateEnable,taskcardEnable,locationEnable,attachEnable=false;

    private String post_type;//使用者選擇的登載類型(SSS、WAY"SCADA)
    private ArrayList<String> taskcard_result;//taskcard的spinner顯示的是組合過的字串，不利於資料庫搜尋，所以用原始條件做篩選條件

    private ArrayList<String> taskcard_list;//顯示的taskcard list(組合過的字串)
    private List<String> location_list;//顯示的location list
    private ArrayList<String> attachment_be_set;//顯示的attachment list

    private ArrayList<String> location_preResult;//同一個工作說明書、類型會同時有多組地點可選擇且對應不同附件，所以在spnLocationListener中要把一組一組地點拿出比對，找出相對於附件list的位置(無法直接透過SQL搜尋，因為SP1,SSP1會判斷成一樣)
    private ArrayAdapter<String> adTaskcardList;
    private ArrayAdapter<String> adLocationList;
    private ArrayAdapter<String> adAttachmentList;
    private String checked_date;//使用者選擇的日期
    private String selected_taskcard;//使用者選擇的taskcard
    private String taskcard_name;
    private String taskcard_type="a";//工作說明書有 a、b、c 3種類型(location_equipment)
    private String selected_location;//使用者選擇的location
    private String selected_attachment;//使用者選擇的attachment

    CompDBHper dbHper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taskcard_select);

        basePos= Integer.parseInt(getIntent().getExtras().getString("basePos"));

        build_view();
    }
    private void build_view(){
        try {
            InputStream am=this.getAssets().open(DBname);
            dbHper=new CompDBHper(this,DBname,null,DBversion,am);
        } catch (IOException e) {
            throw new Error(e.toString());
        }
        taskcardToolbar=((Toolbar) findViewById(R.id.taskcardToolbar));
        setSupportActionBar(taskcardToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);

        spnSelectBase=((Spinner) findViewById(R.id.spnIdStatusBase));
        ArrayAdapter adBase=new ArrayAdapter(TaskcardSelectActivity.this, R.layout.spinner_style_white,new String[]{"六家基地","台北基地","烏日基地","雲林基地","左營基地"});
        adBase.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spnSelectBase.setAdapter(adBase);
        spnSelectBase.setSelection(basePos);
        spnSelectBase.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                basePos=i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        tvLocation=((TextView) findViewById(R.id.tvIdLocation));
        tvAttach=((TextView) findViewById(R.id.tvIdAttach));
        btDate=((Button) findViewById(R.id.btIdDate));
        btDate.setOnClickListener(dateListener);
        btTaskcard=((Button) findViewById(R.id.btIdTaskcard));
        btLocation=((Button) findViewById(R.id.btIdLocation));
        btAttachment=((Button) findViewById(R.id.btIdAttach));

        btBackHome=((Button) findViewById(R.id.btIdTaskcardBackHome));
        btBackHome.setOnClickListener(BackHomeListener);

        btTaskcardNext=((Button) findViewById(R.id.btIdTaskcardNext));
        btTaskcardNext.setBackgroundResource(R.color.gray);
        btTaskcardNext.setText("請填寫相關資料");
        btTaskcardNext.setTextColor(Color.parseColor("#7f7f7f"));
        btTaskcardNext.setEnabled(false);
        btTaskcardNext.setOnClickListener(TaskcardNextListener);

        try {
            dbHper.createDatabase();
        } catch (Exception e) {
            throw new Error("Database not created....");
        }

        post_type=getIntent().getExtras().getString("post_type");

        SQL_command="SELECT DISTINCT taskcard_pkey,taskcard_name FROM taskcard_new WHERE taskcard_sys='" + post_type + "'";
        recSet=dbHper.get(SQL_command);
        taskcard_result=pre_work();
        //組合工作說明書的字串(taskcard_code + taskcard_name)
        taskcard_list=new ArrayList<>();
        for(int i=0;i<taskcard_result.size();i+=2){
            String combine=taskcard_result.get(i).substring(0,(taskcard_result.get(i).length()-8))+ " " + taskcard_result.get(i+1);
            taskcard_list.add(combine);
        }
        adTaskcardList=new ArrayAdapter<String>(this, R.layout.spinner_style,taskcard_list);
        adTaskcardList.setDropDownViewResource(R.layout.spinner_item);
        btTaskcard.setBackgroundResource(R.drawable.spinner_background);
        btTaskcard.setOnClickListener(taskcardListener);
        btLocation.setOnClickListener(locationListener);
        btAttachment.setOnClickListener(attachmentListener);
    }

    /**檢查日期**/
    private View.OnClickListener dateListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            calendar=Calendar.getInstance();
            int day=calendar.get(Calendar.DAY_OF_MONTH);
            int month=calendar.get(Calendar.MONTH);
            int year=calendar.get(Calendar.YEAR);

            datePicker=new DatePickerDialog(TaskcardSelectActivity.this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int mYear, int mMonth, int mDay) {
                    checked_date=mYear + "/" + (mMonth+1) + "/" + mDay;
                    btDate.setText(mYear + "/" + (mMonth+1) + "/" + mDay);

                    if(String.valueOf(mYear)!=null){
                        dateEnable=true;
                        if(dateEnable&taskcardEnable&locationEnable&attachEnable){
                            btTaskcardNext.setBackgroundResource(R.color.theme_teal);
                            btTaskcardNext.setText("下一步");
                            btTaskcardNext.setTextColor(Color.parseColor("#ffffff"));
                            btTaskcardNext.setEnabled(true);
                        }
                        else{
                            btTaskcardNext.setBackgroundResource(R.color.gray);
                            btTaskcardNext.setText("請填寫相關資料");
                            btTaskcardNext.setTextColor(Color.parseColor("#7f7f7f"));
                            btTaskcardNext.setEnabled(false);
                        }
                    }
                    else {
                        dateEnable=false;
                    }
                }
            },year,month,day);
            datePicker.show();
        }
    };
    /**依工作說明書更改地點**/
    private View.OnClickListener taskcardListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            new AlertDialog.Builder(TaskcardSelectActivity.this)
                    .setTitle("請先選擇日期")
                    .setAdapter(adTaskcardList, new DialogInterface.OnClickListener() {

                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            btTaskcard.setText(taskcard_list.get(which));
                            selected_taskcard= taskcard_result.get(which*2);//要寫入資料庫的格式(taskcard_pkey)
                            taskcard_name=taskcard_result.get((which*2)+1);

                            //依工作說明書會有 a、b、c 3種類型(location_equipment)會影響到本頁面的配置，在此做判斷並處理
                            SQL_command="SELECT DISTINCT type FROM taskcard_attach_new WHERE taskcard_attach_pkey LIKE '" + selected_taskcard + "%'";
                            recSet=dbHper.get(SQL_command);
                            ArrayList<String> location_equipment=new ArrayList<>();
                            location_equipment=pre_work();//取得類型

                            //依選擇的工作說明書選擇相對應的地點
                            SQL_command="SELECT location FROM taskcard_new WHERE taskcard_pkey='" + selected_taskcard+"'";
                            recSet=dbHper.get(SQL_command);

                            location_preResult=pre_work();
                            location_list=comma_work(location_preResult);

                            taskcard_type=location_equipment.get(0);
                            switch (taskcard_type){
                                case "a":
                                    //a類型為預設畫面
                                    //資料處理並加入下拉選單
                                    adLocationList=new ArrayAdapter<>(TaskcardSelectActivity.this, R.layout.spinner_style,location_list);//android.R.layout.simple_spinner_item
                                    adLocationList.setDropDownViewResource(R.layout.spinner_item);//android.R.layout.select_dialog_singlechoice
                                    break;
                                case "b":
                                    //b類型為多選畫面
                                    ArrayList<String> location_buffer=new ArrayList<>();//暫存處理完
                                    for (int i = 0; i<location_list.size(); i++){
                                        String typeB_location=location_list.get(i).replace("_","    ");
                                        location_buffer.add(typeB_location);
                                        location_list=location_buffer;
                                    }
                                    break;
                                case "c":
                                    //c類型為里程選擇畫面，且無附件
                                    tvLocation.setText("*里程");
                                    btLocation.setHint("選擇里程");
                                    tvAttach.setVisibility(View.INVISIBLE);
                                    btAttachment.setVisibility(View.INVISIBLE);
                                    break;
                            }

                            btLocation.setText(null);
                            locationEnable=false;
                            btAttachment.setText(null);
                            attachEnable=false;
                            dialog.dismiss();

                            if(btTaskcard.getText()!=null){
                                taskcardEnable=true;
                                if(dateEnable&taskcardEnable&locationEnable&attachEnable){
                                    btTaskcardNext.setBackgroundResource(R.color.theme_teal);
                                    btTaskcardNext.setText("下一步");
                                    btTaskcardNext.setTextColor(Color.parseColor("#ffffff"));
                                    btTaskcardNext.setEnabled(true);
                                }
                                else{
                                    btTaskcardNext.setBackgroundResource(R.color.gray);
                                    btTaskcardNext.setText("請填寫相關資料");
                                    btTaskcardNext.setTextColor(Color.parseColor("#7f7f7f"));
                                    btTaskcardNext.setEnabled(false);
                                }
                            }
                            else{
                                taskcardEnable=false;
                            }
                        }
                    }).create().show();
        }
    };
    /**依地點更改附件**/
    private View.OnClickListener locationListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //依類型更改配置
            switch (taskcard_type){
                case "a":
                    new AlertDialog.Builder(TaskcardSelectActivity.this)
                    .setTitle("請先選擇工作說明書")
                    .setAdapter(adLocationList, new DialogInterface.OnClickListener() {

                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            btLocation.setText(location_list.get(which));
                            selected_location= location_list.get(which);//要寫入資料庫的格式

                            SQL_command="SELECT attach FROM taskcard_attach_new WHERE taskcard_attach_pkey LIKE'" + selected_taskcard + "%'";
                            recSet=dbHper.get(SQL_command);
                            ArrayList<String> attachment_list=pre_work();
                            attachment_be_set=new ArrayList<>();
                            for(int x=0;x< location_preResult.size();x++){//x:組
                                String[] compare_buffer=location_preResult.get(x).split(",");
                                for (int z=0;z<compare_buffer.length;z++){//找到一樣的地點就把當前組別記錄下來
                                    if(compare_buffer[z].equals(selected_location)){
                                        attachment_be_set.add(attachment_list.get(x));
                                    }
                                }
                            }
                            adAttachmentList=new ArrayAdapter<>(TaskcardSelectActivity.this, R.layout.spinner_style,attachment_be_set);
                            adAttachmentList.setDropDownViewResource(R.layout.spinner_item);

                            btAttachment.setText(null);
                            attachEnable=false;
                            dialog.dismiss();

                            if(btLocation.getText()!=null){
                                locationEnable=true;
                                if(dateEnable&taskcardEnable&locationEnable&attachEnable){
                                    btTaskcardNext.setBackgroundResource(R.color.theme_teal);
                                    btTaskcardNext.setText("下一步");
                                    btTaskcardNext.setTextColor(Color.parseColor("#ffffff"));
                                    btTaskcardNext.setEnabled(true);
                                }
                                else{
                                    btTaskcardNext.setBackgroundResource(R.color.gray);
                                    btTaskcardNext.setText("請填寫相關資料");
                                    btTaskcardNext.setTextColor(Color.parseColor("#7f7f7f"));
                                    btTaskcardNext.setEnabled(false);
                                }
                            }
                            else{
                                locationEnable=false;
                            }
                        }
                    }).create().show();
                    break;
                case "b":
                    String selected_location[]=new String[location_list.size()];//紀錄多選下拉式選單結果的陣列
                    new AlertDialog.Builder(TaskcardSelectActivity.this)
                            .setTitle("請先選擇工作說明書")
                            .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //多選單確定鍵listener(查找對應attachment)
                                    dialogInterface.dismiss();
                                    if(btLocation.getText()!=null){
                                        locationEnable=true;
                                        if(dateEnable&taskcardEnable&locationEnable&attachEnable){
                                            btTaskcardNext.setBackgroundResource(R.color.theme_teal);
                                            btTaskcardNext.setText("下一步");
                                            btTaskcardNext.setTextColor(Color.parseColor("#ffffff"));
                                            btTaskcardNext.setEnabled(true);
                                        }
                                        else{
                                            btTaskcardNext.setBackgroundResource(R.color.gray);
                                            btTaskcardNext.setText("請填寫相關資料");
                                            btTaskcardNext.setTextColor(Color.parseColor("#7f7f7f"));
                                            btTaskcardNext.setEnabled(false);
                                        }
                                    }
                                    else{
                                        locationEnable=false;
                                    }
                                    String display_location = "";
                                    for (int x=0;x<selected_location.length;x++){
                                        if(selected_location[x]!=null){
                                            display_location += selected_location[x]+"\n";
                                        }
                                    }
                                    if (display_location.length()!=0){
                                        btLocation.setText(display_location.substring(0,display_location.length()-1));
                                    }
                                    else {
                                        btLocation.setText("");
                                    }
                                }
                            })
                            .setMultiChoiceItems(location_list.toArray(new CharSequence[0]),null,new DialogInterface.OnMultiChoiceClickListener(){
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                                    if(b){
                                        selected_location[i]=location_list.get(i);
                                    }
                                    else {
                                        selected_location[i]=null;
                                    }
                                    Log.d("TAG", Arrays.toString(selected_location));
                                }
                            }).create().show();

                    break;
                case "c":
                    View mileage_layout = getLayoutInflater().inflate(R.layout.taskcard_select_mileage,null);
                    new AlertDialog.Builder(TaskcardSelectActivity.this)
                            .setTitle("請先選擇工作說明書")
                            .setView(mileage_layout)
                            .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Spinner spnStart=(mileage_layout.findViewById(R.id.spnIdMainStartMile));
                                    spnStart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                        @Override
                                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                                        }

                                        @Override
                                        public void onNothingSelected(AdapterView<?> adapterView) {

                                        }
                                    });
                                    Spinner spnEnd=(mileage_layout.findViewById(R.id.spnIdMainEndMile));
                                    spnEnd.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                        @Override
                                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                                        }

                                        @Override
                                        public void onNothingSelected(AdapterView<?> adapterView) {

                                        }
                                    });
                                }
                            }).create().show();
                    break;
            }
        }
    };
    /**附件listener**/
    private View.OnClickListener attachmentListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            new AlertDialog.Builder(TaskcardSelectActivity.this)
                    .setTitle("請先選擇地點")
                    .setAdapter(adAttachmentList, new DialogInterface.OnClickListener() {

                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            btAttachment.setText(attachment_be_set.get(which));
                            selected_attachment= attachment_be_set.get(which);//要寫入資料庫的格式
                            dialog.dismiss();

                            if(btAttachment.getText()!=null){
                                attachEnable=true;
                                if(dateEnable&taskcardEnable&locationEnable&attachEnable){
                                    btTaskcardNext.setBackgroundResource(R.color.theme_teal);
                                    btTaskcardNext.setText("下一步");
                                    btTaskcardNext.setTextColor(Color.parseColor("#ffffff"));
                                    btTaskcardNext.setEnabled(true);
                                }
                                else{
                                    btTaskcardNext.setBackgroundResource(R.color.gray);
                                    btTaskcardNext.setText("請填寫相關資料");
                                    btTaskcardNext.setTextColor(Color.parseColor("#7f7f7f"));
                                    btTaskcardNext.setEnabled(false);
                                }
                            }
                            else{
                                attachEnable=false;
                            }
                        }
                    }).create().show();
        }
    };
    private View.OnClickListener BackHomeListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent=new Intent(TaskcardSelectActivity.this, MainActivity.class);
            startActivity(intent);
        }
    };

    private View.OnClickListener TaskcardNextListener =new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent=new Intent(TaskcardSelectActivity.this,TaskcardStatusActivity.class);
            Bundle bundle=new Bundle();
            bundle.putString("basePos", String.valueOf(basePos));
            bundle.putString("postMod",getIntent().getExtras().getString("postMod"));
            bundle.putString("checked_date",checked_date);
            bundle.putString("selected_taskcard",selected_taskcard);
            bundle.putString("taskcard_name",taskcard_name);
            bundle.putString("selected_location",selected_location);
            bundle.putString("selected_attachment",selected_attachment);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    };
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
    private ArrayList<String> comma_work(ArrayList<String> location_set){
        int location_set_size=location_set.size();
        ArrayList<String> AryResult = new ArrayList<String>();
        for(int i=0;i<location_set_size;i++){
            String[] buffer=location_set.get(i).split(",");
            for(int y=0;y< buffer.length;y++){
                //當搜尋結果分離逗號後超過1組，依照順序將buffer中的內容塞到AryResult裡
                AryResult.add(buffer[y]);
            }
        }
        return AryResult;
    }
}


/*  以button代替spinner(spinner廢棄集中區)
        //宣告
        private Spinner spnTaskcard,spnLocation,spnAttach;
        //build_view
        spnTaskcard=((Spinner) findViewById(R.id.spIdTaskcard));
        spnLocation=((Spinner) findViewById(R.id.spIdLocation));
        spnAttach=((Spinner) findViewById(R.id.spIdAttach));
        spnTaskcard.setOnItemSelectedListener(spnTaskcardListener);
        spnLocation.setOnItemSelectedListener(spnLocationListener);
        spnAttach.setOnItemSelectedListener(spnAttachListener);
        //adapter
        spnTaskcard.setAdapter(adTaskcardList);
        spnLocation.setAdapter(adLocationList);
        spnAttach.setAdapter(adAttachmentList);
        //listener
            //spnTaskcardListener
            private AdapterView.OnItemSelectedListener spnTaskcardListener=new AdapterView.OnItemSelectedListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    selected_taskcard= taskcard_result.get(i*2);
                    SQL_command="SELECT field17 FROM " + TBname + " WHERE taskcard_sys='" + post_type + "' AND taskcard_pkey='" + taskcard_result.get(i*2) + "' AND field17 IS NOT NULL";
                    recSet=dbHper.get(SQL_command);

                    location_preResult=pre_work();
                    ArrayList<String> location_result=comma_work(location_preResult);
                    List<String> ii=location_result.stream().distinct().collect(Collectors.toList());//重複的要拿掉

                    adLocationList=new ArrayAdapter<>(TaskcardSelectActivity.this, R.layout.spinner_style,ii);//android.R.layout.simple_spinner_item
                    adLocationList.setDropDownViewResource(R.layout.spinner_item);//android.R.layout.select_dialog_singlechoice

                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            };
            //spnLocationListener
            private AdapterView.OnItemSelectedListener spnLocationListener=new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    selected_location=adapterView.getSelectedItem().toString();

                    //把一組一組的地點拿出比對，找出相對於附件list的位置(
                    SQL_command="SELECT type FROM " + TBname + " WHERE taskcard_sys='" + post_type + "' AND taskcard_pkey='" + selected_taskcard + "' AND field17 IS NOT NULL";
                    recSet=dbHper.get(SQL_command);
                    ArrayList<String> attachment_list=pre_work();
                    ArrayList<String> attachment_be_set=new ArrayList<>();
                    for(int x=0;x< location_preResult.size();x++){//x:組
                        String[] compare_buffer=location_preResult.get(x).split(",");
                        for (int z=0;z<compare_buffer.length;z++){//找到一樣的地點就把當前組別記錄下來
                            if(compare_buffer[z].equals(selected_location)){
                                attachment_be_set.add(attachment_list.get(x));
                            }
                        }
                    }
                    adAttachmentList=new ArrayAdapter<>(TaskcardSelectActivity.this, R.layout.spinner_style,attachment_be_set);
                    adAttachmentList.setDropDownViewResource(R.layout.spinner_item);

                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            };
*/