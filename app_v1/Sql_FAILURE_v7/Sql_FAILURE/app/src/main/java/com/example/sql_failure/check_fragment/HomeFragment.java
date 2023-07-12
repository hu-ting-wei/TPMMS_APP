package com.example.sql_failure.check_fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.sql_failure.CheckActivity;
import com.example.sql_failure.CompDBHper;
import com.example.sql_failure.R;
import com.example.sql_failure.post_activities.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    private TextView tvFragCheck;
    private String taskcard_name;
    private String taskcard_attach_pkey;
    private static String PMID;
    public static String Room;
    private String[] room;
    private String postMod;


    /**SQL**/
    private static final String DBname="myDB1.db";
    private static final int DBversion=1;
    private static final String TBname="check_item";
    private static final int itemID=52;
    private int TypeB_checkID=0;
    //CompDBHper compDBHper=new CompDBHper(getActivity(),DBname,null,DBversion);
    static CompDBHper compDBHper;
    private String SQL_command;
    private ArrayList<String> recSet;//包含#的內容

    /*****/
    private boolean click=true;//給各元件的id，從1開始
    private View myView;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    //透過fragment的onAttach()提取Intent資料
    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        ArrayList<String> fromStatus=((CheckActivity)context).toValue();
        taskcard_name=fromStatus.get(0);
        PMID=fromStatus.get(1);
        postMod=fromStatus.get(2);
    }

    //@RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //取得檔案位置
        try {
            InputStream am=getContext().getAssets().open(DBname);
            compDBHper=new CompDBHper(getContext(),DBname,null,DBversion,am);
        } catch (IOException e) {
            throw new Error(e.toString());
        }
        // Inflate the layout for this fragment
        myView=inflater.inflate(R.layout.fragment_home, container, false);


        try {
            compDBHper.createDatabase();
        } catch (Exception e) {
            throw new Error(e.toString());
        }
        /****/
        tvFragCheck=((TextView) myView.findViewById(R.id.tvIdFragCheck));
        tvFragCheck.setText(taskcard_name);
        tvFragCheck.setSelected(true);

        String SQL_command="SELECT type,taskcard_attach_pkey,location_equipment_tk FROM " + "check_condition"  + " WHERE PMID=" + PMID + "";   //拿取過去紀錄
        recSet=compDBHper.get(SQL_command);
        ArrayList<String> history=pre_work();
        RadioGroup rgRoom=((RadioGroup) myView.findViewById(R.id.rgRoom));

        switch (history.get(0)){
            case "a":
                room= new String[]{"全部","1","2","3"};
                for (int i = 0; i < room.length; i++) {
                    RadioButton btRadio_room=new RadioButton(getActivity());
                    LinearLayout.LayoutParams layoutset=new RadioGroup.LayoutParams(450, 150);
                    layoutset.setMargins(5, 0, 5, 0);
                    btRadio_room.setLayoutParams(layoutset);
                    btRadio_room.setBackgroundResource(R.color.station_blue);
                    btRadio_room.setGravity(android.view.Gravity.CENTER);
                    btRadio_room.setTextSize(22);
                    btRadio_room.setText(room[i]);
                    btRadio_room.setTextColor(getResources().getColor(R.color.black));
                    btRadio_room.setTypeface(null, Typeface.BOLD);
                    btRadio_room.setId(i);

                    rgRoom.addView(btRadio_room);
                }
                rgRoom.check(TypeB_checkID);

                rgRoom.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
                        if (click) ConfirmPageChange(new CreateAFragment());
                    }
                });
                Room=null;

                replaceFragment(new CreateAFragment());

                break;
            case "b":
                room=history.get(2).split(",");
                for (int i = 0; i < room.length; i++) {
                    RadioButton btRadio_room=new RadioButton(getActivity());
                    LinearLayout.LayoutParams layoutset=new RadioGroup.LayoutParams(450, 150);
                    layoutset.setMargins(5, 0, 5, 0);
                    btRadio_room.setLayoutParams(layoutset);
                    btRadio_room.setBackgroundResource(R.color.station_blue);
                    btRadio_room.setGravity(android.view.Gravity.CENTER);
                    btRadio_room.setTextSize(22);
                    btRadio_room.setText(room[i]);
                    btRadio_room.setTextColor(getResources().getColor(R.color.black));
                    btRadio_room.setTypeface(null, Typeface.BOLD);
                    btRadio_room.setId(i);

                    rgRoom.addView(btRadio_room);
                }

                rgRoom.check(TypeB_checkID);
                Room=room[0];


                rgRoom.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {

                        if (click) ConfirmPageChange(new CreateBFragment());

                    }
                });


                replaceFragment(new CreateBFragment());


                break;
            case "c":
                String[] location=history.get(2).split(",");


                break;

        }


        //replaceFragment(new CreateAFragment());



        return myView;
    }





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
    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager=getChildFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout5,fragment);
        fragmentTransaction.commit();
    }

    private void ConfirmPageChange(Fragment fragment){//頁面切換確認
        RadioGroup rgRoom=((RadioGroup) myView.findViewById(R.id.rgRoom));
        AlertDialog.Builder ad_leave=new AlertDialog.Builder(getActivity());
        ad_leave.setTitle("離開");
        ad_leave.setMessage("是否要切換頁面?");
        ad_leave.setPositiveButton("是", new DialogInterface.OnClickListener() {//退出按鈕
            public void onClick(DialogInterface dialog, int i) {
                // TODO Auto-generated method stub
                AlertDialog.Builder ad_save=new AlertDialog.Builder(getActivity());
                ad_save.setTitle("存檔");
                ad_save.setMessage("是否要儲存尚未存檔之更動?");
                ad_save.setPositiveButton("是", new DialogInterface.OnClickListener() {//退出按鈕
                    public void onClick(DialogInterface dialog, int i) {
                        // TODO Auto-generated method stub
                        TypeB_checkID=rgRoom.getCheckedRadioButtonId();
                        Room=room[TypeB_checkID];
                        SaveData();
                        replaceFragment(fragment);

                    }
                });
                ad_save.setNegativeButton("否",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        TypeB_checkID=rgRoom.getCheckedRadioButtonId();
                        Room=room[TypeB_checkID];
                        replaceFragment(fragment);
                    }
                });
                ad_save.show();//顯示對話框
            }
        });
        ad_leave.setNegativeButton("否",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                click=false;
                rgRoom.check(TypeB_checkID);
                click=true;
            }
        });
        ad_leave.show();//顯示對話框
    }
    private void SaveData(){
        String SQL_command="SELECT type,taskcard_attach_pkey,location_equipment_tk FROM " + "check_condition"  + " WHERE PMID=" + PMID + "";   //拿取過去紀錄
        recSet=compDBHper.get(SQL_command);
        ArrayList<String> history=pre_work();
        switch (history.get(0)){
            case "a":
                CreateAFragment.save();
                break;
            case "b":
                CreateBFragment.save();
                break;
            case "c":
                break;
        }
    }




}
