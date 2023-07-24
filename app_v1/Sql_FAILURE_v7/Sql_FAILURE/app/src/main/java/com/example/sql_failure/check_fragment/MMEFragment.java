package com.example.sql_failure.check_fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.sql_failure.CheckActivity;
import com.example.sql_failure.CompDBHper;
import com.example.sql_failure.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MMEFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MMEFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private Context mContext;

    private String taskcard_name;
    private TextView tvFragCheck;

    private LinearLayout vlFather;
    private View mmeitemView;

    private static final String DBname="myDB1.db";

    private static final int DBversion=1;

    static CompDBHper compDBHper;

    private static String PMID;

    private ArrayList<String> recSet;//包含#的內容

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private View myView;

    private static ArrayList<String> mme_result=new ArrayList<>();
    private static ArrayList<String> mme_item_name=new ArrayList<>();

    public MMEFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MMEFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MMEFragment newInstance(String param1, String param2) {
        MMEFragment fragment = new MMEFragment();
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

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        ArrayList<String> fromStatus=((CheckActivity)context).toValue();
        taskcard_name=fromStatus.get(0);
        PMID=fromStatus.get(1);
        //postMod=fromStatus.get(2);
        mContext=getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        try {
            InputStream am=getContext().getAssets().open(DBname);
            compDBHper=new CompDBHper(getContext(),DBname,null,DBversion,am);
        } catch (IOException e) {
            throw new Error(e.toString());
        }
        // Inflate the layout for this fragment
        myView=inflater.inflate(R.layout.fragment_mme, container, false);
        tvFragCheck=((TextView) myView.findViewById(R.id.tvIdFragCheck));
        tvFragCheck.setText(taskcard_name);
        tvFragCheck.setSelected(true);


        try {
            compDBHper.createDatabase();
        } catch (Exception e) {
            throw new Error(e.toString());
        }

        mme_result=new ArrayList<>();
        mme_item_name=new ArrayList<>();

        String SQL_command="SELECT type,taskcard_attach_pkey,location_equipment_tk FROM " + "check_condition"  + " WHERE PMID=" + PMID + "";
        recSet=compDBHper.get(SQL_command);
        ArrayList<String> history=pre_work();

        String taskcard_attach_pkey=history.get(1);

        SQL_command="SELECT DISTINCT mme_tool,tool_label,tool_code FROM taskcard_attach_mme_new WHERE taskcard_attach_pkey='" + taskcard_attach_pkey + "'";
        recSet=compDBHper.get(SQL_command);
        ArrayList<String> mme_item=pre_work();







        int mme_item_count= mme_item.size();
        vlFather = ((LinearLayout) myView.findViewById(R.id.vlIdFather));

        int id=0;

        for (int i=0;i<(mme_item_count/3);i++){
            mmeitemView =  LayoutInflater.from(mContext).inflate(R.layout.mme_item,null);
            TextView tvTool=((TextView) mmeitemView.findViewById(R.id.tvIdTool));
            tvTool.setText(mme_item.get(i*3));

            ArrayList<String> tool_lable_list = new ArrayList<>();//加入"請選擇"選項
            tool_lable_list.add(mme_item.get(i*3+1));
            tool_lable_list.add(0,"請選擇");//初始顯示的值，所以之後處理資料需要忽略第0項
            ArrayAdapter label_adapter = new ArrayAdapter(mContext,R.layout.my_selected_item, tool_lable_list);
            Spinner spnLabel =((Spinner) mmeitemView.findViewById(R.id.spnIdLabel));
            label_adapter.setDropDownViewResource(R.layout.my_spinner_dropdown_item);
            spnLabel.setAdapter(label_adapter);
            spnLabel.setId(id);
            mme_result.add(null);
            mme_item_name.add(mme_item.get(i*3)+"-label");
            id+=1;

            spnLabel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (spnLabel.getSelectedItem()=="請選擇")
                        mme_result.set(spnLabel.getId(),null);
                    else
                        mme_result.set(spnLabel.getId(),(String) spnLabel.getSelectedItem());
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }

            });


            ArrayList<String> tool_code_list = new ArrayList<>();//加入"請選擇"選項
            tool_code_list.add(mme_item.get(i*3+2));
            tool_code_list.add(0,"請選擇");//初始顯示的值，所以之後處理資料需要忽略第0項
            ArrayAdapter code_adapter = new ArrayAdapter(mContext,R.layout.my_selected_item, tool_code_list);
            Spinner spnCode =((Spinner) mmeitemView.findViewById(R.id.spnIdCode));
            code_adapter.setDropDownViewResource(R.layout.my_spinner_dropdown_item);
            spnCode.setAdapter(code_adapter);
            spnCode.setId(id);
            mme_result.add(null);
            mme_item_name.add(mme_item.get(i*3)+"-code");
            id+=1;

            spnCode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (spnCode.getSelectedItem()=="請選擇")
                        mme_result.set(spnCode.getId(),null);
                    else
                        mme_result.set(spnCode.getId(),(String) spnCode.getSelectedItem());
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });





            vlFather.addView(mmeitemView);
        }
        SQL_command="SELECT data FROM checked_result WHERE PMID='" + PMID + "' AND room='mme'";
        recSet=compDBHper.get(SQL_command);

        if (recSet.size()==0){
            save();
        }
        else {
            ArrayList<String> history_arr=pre_work();
            mme_result=history_arr;
            for (int i=0;i<history_arr.size();i++){
                if (history_arr.get(i).equals("null")==false){
                    Spinner spn =(Spinner) myView.findViewById(i);
                    spn.setSelection(1);
                }

            }
        }


        return myView;
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

    public static void save(){

        String sql="DELETE FROM checked_result WHERE PMID='" + PMID + "' AND room='mme'";
        compDBHper.delete_update(sql);
        compDBHper.set("checked_result",PMID,"mme",mme_result,mme_item_name);
    }
}



