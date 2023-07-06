package com.example.sql_failure.check_fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Space;
import android.widget.TextView;

import com.example.sql_failure.CheckActivity;
import com.example.sql_failure.CompDBHper;
import com.example.sql_failure.R;
import com.example.sql_failure.post_activities.EditActivity;
import com.facebook.stetho.Stetho;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
    private static String PMID;
    private String postMod;


    /**SQL**/
    private static final String DBname="myDB1.db";
    private static final int DBversion=1;
    private static final String TBname="check_item";
    private static final int itemID=52;
    //CompDBHper compDBHper=new CompDBHper(getActivity(),DBname,null,DBversion);
    static CompDBHper compDBHper;
    private String SQL_command;
    private ArrayList<String> recSet;//包含#的內容

    /*****/
    private int id=1;//給各元件的id，從1開始
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
        //監看DB內容
        Stetho.initializeWithDefaults(getContext());
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

        replaceFragment(new CreateFragment());



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


}
