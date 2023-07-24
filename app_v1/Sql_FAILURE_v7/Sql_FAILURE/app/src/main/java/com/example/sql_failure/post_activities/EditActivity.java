package com.example.sql_failure.post_activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.widget.Toolbar;

import com.example.sql_failure.CompDBHper;
import com.example.sql_failure.R;
import com.example.sql_failure.edit_recycler.RecyclerAdapter;
import com.example.sql_failure.edit_recycler.TreeItem;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;

public class EditActivity extends AppCompatActivity {

    private Toolbar editToolBar;
    private Button btEditLogin;

    private RecyclerView rvEditItemView;
    private TreeItem treeItem;

    private ArrayList<String> recSet;
    private String SQL_command;
    private static final String DBname="myDB1.db";
    private static final int DBversion=1;
    CompDBHper dbHper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        build_views();
    }
    private void build_views(){
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

        editToolBar=((Toolbar) findViewById(R.id.editToolbar));
        setSupportActionBar(editToolBar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);

        btEditLogin=((Button) findViewById(R.id.btIdEditLogin));
        //list中一個元素須的格式(6項):[sss,變電站半年檢查,F1(台北車站...),2022/1/19,BSS1,張先生]，前三項(top_three_arr)於taskcard_new、taskcard_attach_new查找，後三項(last_three_arr)於check_condition查找
        //另一個額外元素是要拿取PMID，當使用者要編輯或刪除時使用PMID
        //查找流程:
        //      *需先找出工作說明書的type(c類型在DB中無附件資料，需與a、b類型分開處理)
        //      1.先從check_condition中找出taskcard_attach_pkey，並且拆開成taskcard_pkey與附件名稱(ex:F10)兩項
        //      2.透過拆分的兩筆資料回taskcard_new、taskcard_attach_new查找剩餘資料
        //      3.組裝attach與remark，ex:F1 and 苗栗車站,雲林車站,彰化車站,into F1(苗栗車站,雲林車站,彰化車站)
        //      4.從check_condition中找出location_equipment_tk,WPS,create_date
        //      5.額外拿取PMID

        //1.
        SQL_command="SELECT taskcard_attach_pkey,type FROM check_condition";
        recSet=dbHper.get(SQL_command);
        ArrayList<String> taskcard_attach_pkey_type_Array=pre_work();//找出taskcard_attach_pkey及type

        ArrayList<String> attach_and_pkey=new ArrayList<>();//存放拆成兩項的ArrayList{附件名稱,taskcard_pkey,...}
        for (int i=0;i<taskcard_attach_pkey_type_Array.size();i+=2){
            if (taskcard_attach_pkey_type_Array.get(i+1).equals("c")){
                //c類型在DB中無附件資料
                attach_and_pkey.add("no_attach");
                attach_and_pkey.add(taskcard_attach_pkey_type_Array.get(i));
            }
            else {
                //a、b類型
                String mString=taskcard_attach_pkey_type_Array.get(i);//taskcard_attach_pkey
                String attach_name=mString.substring(mString.length()-5);//取後5位，確保'-'有被加入，以利split("-")
                String[] attach_name_without_minus=attach_name.split("-");//把'-'拆開，並擷取附件名稱
                attach_and_pkey.add(attach_name_without_minus[1]);

                String[] pkey_name=mString.split("-" + attach_name_without_minus[1]);//把剛剛擷取的附件名稱透過split過濾出taskcard_pkey
                attach_and_pkey.add(pkey_name[0]);
            }
        }
        //2.3.
        ArrayList<String> top_three_arr=new ArrayList<>();//前三項
        for (int j=0;j<attach_and_pkey.size();j+=2){
            //從taskcard_new取得說明書登載類型(taskcard_sys)、工作說明書名稱(taskcard_name)
            SQL_command="SELECT taskcard_sys,taskcard_name FROM taskcard_new WHERE taskcard_pkey='" + attach_and_pkey.get(j+1) + "'";
            recSet=dbHper.get(SQL_command);
            ArrayList<String> taskcard_sys_name=pre_work();
            if (taskcard_sys_name.get(0).equals("WAYSIDE")){
                taskcard_sys_name.set(0,"WAY");
            }
            top_three_arr.add(taskcard_sys_name.get(0));//top_three_arr第一項
            top_three_arr.add(taskcard_sys_name.get(1));//top_three_arr第二項
            //從taskcard_attach_new取得附件
            if ( attach_and_pkey.get(j).equals("no_attach")){
                //c類型無附件資料
                top_three_arr.add("");//top_three_arr第三項(c)
            }
            else {
                SQL_command="SELECT attach,remark FROM taskcard_attach_new WHERE taskcard_attach_pkey LIKE '" + attach_and_pkey.get(j+1) + "%' AND attach='" + attach_and_pkey.get(j) +"'";
                recSet=dbHper.get(SQL_command);
                ArrayList<String> attach_remark=pre_work();
                String attachment=attach_remark.get(0) + "(" + attach_remark.get(1) + ")";
                top_three_arr.add(attachment);//top_three_arr第三項(a、b)
            }
        }
        /************/
        //1.
        /*SQL_command="SELECT taskcard_attach_pkey FROM check_condition";
        recSet=dbHper.get(SQL_command);
        ArrayList<String> taskcard_attach_pkey_Array=pre_work();//找出taskcard_attach_pkey

        ArrayList<String> pkey_and_attach=new ArrayList<>();//存放拆成兩項的ArrayList{附件名稱,taskcard_pkey,...}
        for (String mString:taskcard_attach_pkey_Array){    //foreach
            String attach_name=mString.substring(mString.length()-5);//取後5位，確保'-'有被加入，以利split("-")
            String[] attach_name_without_minus=attach_name.split("-");//把'-'拆開，並擷取附件名稱
            pkey_and_attach.add(attach_name_without_minus[1]);

            String[] pkey_name=mString.split("-" + attach_name_without_minus[1]);//把剛剛擷取的附件名稱透過split過濾出taskcard_pkey
            pkey_and_attach.add(pkey_name[0]);
        }
       //2.3.
        ArrayList<String> top_three_arr=new ArrayList<>();//前三項
        for(int j=0;j<pkey_and_attach.size();j+=2){
            SQL_command="SELECT taskcard_sys,taskcard_name,field17 FROM spinner_system WHERE type='" + pkey_and_attach.get(j) + "' AND taskcard_pkey='" + pkey_and_attach.get(j+1) + "'";
            recSet=dbHper.get(SQL_command);
            ArrayList<String> result=pre_work();//找出taskcard_sys,taskcard_name,field17
            String attach_field=pkey_and_attach.get(j) + "(" + result.get(2) + ")";//組裝附件名稱與field17
            result.set(2,attach_field); //取代原本的值
              for (String mString:result){    //foreach:遍歷 result陣列 所有元素
                if (mString.equals("WAYSIDE")){
                    mString="WAY";
                }
                top_three_arr.add(mString);
            }
        }*/
        //4.
        SQL_command="SELECT create_date,location_equipment_tk,WPS FROM check_condition";
        recSet=dbHper.get(SQL_command);
        ArrayList<String> last_three_arr=pre_work();//找出create_date,location_equipment_tk,WPS，後三項
            //c類型地點為哩程需顯示為tk格式(ex:25.04,66.55--->TK25.04~TK66.55)
        SQL_command="SELECT type FROM check_condition";
        recSet=dbHper.get(SQL_command);
        ArrayList<String> taskcard_type=pre_work();

        for (int y=0;y<taskcard_type.size();y++){
            if (taskcard_type.get(y).equals("c")){
                String[] tk_range=last_three_arr.get(y*3+1).split(",");
                String tk;
                if (tk_range.length>1){
                    //有範圍
                    tk="TK" + tk_range[0] + "\n~\nTK" + tk_range[1];
                }
                else {
                    //同範圍
                    tk="TK" + tk_range[0];
                }
                last_three_arr.set(y*3+1,tk);
            }
            else if (taskcard_type.get(y).equals("b")){
                String[] multiLocation=last_three_arr.get(y*3+1).split(",");
                String[] equipment=multiLocation[0].split("_");
                String eq=equipment[1] + " 設備";
                last_three_arr.set(y*3+1,eq);
            }
        }
        //5.
        SQL_command="SELECT PMID FROM check_condition";
        recSet=dbHper.get(SQL_command);
        ArrayList<String> PMID_arr=pre_work();//找出PMID
        //宣告RecyclerView
        rvEditItemView=((RecyclerView) findViewById(R.id.rvIdEditItemView));
        rvEditItemView.setLayoutManager(new LinearLayoutManager(this));//指定 RecyclerView 排列方式
        RecyclerAdapter mRecyclerAdapter=new RecyclerAdapter(this,initList(top_three_arr,last_three_arr,PMID_arr));//宣告adapter物件，同時給予建構子所需的參數(Context,List<TreeItem>)
        rvEditItemView.setAdapter(mRecyclerAdapter);
        //滾動監聽，我的想法是透過mSetOnScrollListener實作介面OnScrollListener
        mRecyclerAdapter.mSetOnScrollListener(new RecyclerAdapter.OnScrollListener() {
            @Override
            public void scrollTo(int pos) {
                rvEditItemView.scrollToPosition(pos);
            }
        });
    }
    private List<TreeItem> initList(ArrayList<String> top_three_arr,ArrayList<String> last_three_arr,ArrayList<String> PMID_arr){
        //要送入adapter的list
        List<TreeItem> list=new ArrayList<>();

        for (int i=0;i<top_three_arr.size()/3;i++){
            treeItem=new TreeItem();
            //top_three
            treeItem.setParentTaskcardType(top_three_arr.get(i*3));   //ex:SSS
            treeItem.setParentTaskcard(top_three_arr.get(i*3+1));       //ex:SCADA系統每月檢查
            treeItem.setChildAttach(top_three_arr.get(i*3+2));          //ex:F4(南港車站檢查表)
            //last_three
            treeItem.setParentDate(last_three_arr.get(i*3));           //ex:2022/12/12
            treeItem.setParentLocation(last_three_arr.get(i*3+1));       //ex:BSS1
            treeItem.setChildPerson(last_three_arr.get(i*3+2));          //ex:盧柏翰
            //PMID
            treeItem.setPMID(PMID_arr.get(i));  //set PMID

            treeItem.setType(0);
            treeItem.setChildDta(treeItem);
            list.add(treeItem);
        }
        return list;
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
}