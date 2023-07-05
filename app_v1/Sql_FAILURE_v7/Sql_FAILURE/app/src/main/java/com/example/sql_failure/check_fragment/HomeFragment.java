package com.example.sql_failure.check_fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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

    private LinearLayout vlFather;

    private RadioGroup content_rg[][];
    private EditText content_et[][];
    /**SQL**/
    private static final String DBname="myDB1.db";
    private static final int DBversion=1;
    private static final String TBname="check_item";
    private static final int itemID=52;
    //CompDBHper compDBHper=new CompDBHper(getActivity(),DBname,null,DBversion);
    static CompDBHper compDBHper;
    private String SQL_command;
    private ArrayList<String> recSet;//包含#的內容
    private ArrayList<String> item_ID_set;//itemID及其標題，顯示於textview上
    private static ArrayList<String> taskcard_attach_item_pkey;//各項目代碼(去除#的內容)
    private ArrayList<String> item_description_set;//去除#的內容(標頭)
    private int ALL_item_description_count;
    /*****/
    private ArrayList<String> check_set=new ArrayList<>();//去除#的內容(內文)
    private int check_set_count;//內文項目數量(/3為組數)
    private int radio_group_set=0;//作為radio_group組數標記
    private int editText_set=0;//作為EditText_set組數標記
    private int editText_id;//紀錄editText的id，當editText內容改變時無法像radioGroup一樣取得當前物件並取得id，所以只能靠onTouch來取得id。
                                //但編輯模式會直接改變editText的值，因此無法靠onTouch來取得id，就必須用此參數作為id紀錄
    /*****/
    private static ArrayList<ArrayList<Integer>> element_IDs=new ArrayList<>();//收藏所有id們，element_IDs[組別][小項目]
    private static ArrayList<ArrayList<String>> db_result=new ArrayList<>();//收藏使用者的輸入結果(即將寫入db)，db_result[組別][小項目]

    private ArrayList<String> all_standard_type;//去除#的內容(計數在同一itemID下整個表格的item數量，作為二維陣列初始化值，以防不夠用)
    private ArrayList<String> type_record=new ArrayList<>();//標頭固定為radioGroup但standard_type不一定是0，所以另用一個array來記錄各項的type(在編輯模式中會需要知道type才能把紀錄塞回去)
    private int content_item_count;
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

        //找出所有項目(不分itemID)
        SQL_command="SELECT DISTINCT item_description FROM " + TBname;
        recSet=compDBHper.get(SQL_command);
        ArrayList<String> ALL_item_description_set=pre_work();
        ALL_item_description_count=ALL_item_description_set.size();

        //找出所有細項目(不分itemID、取全部以防不夠)
        SQL_command="SELECT standard_type FROM " + TBname;
        recSet=compDBHper.get(SQL_command);
        all_standard_type=pre_work();
        content_item_count=all_standard_type.size();

        content_rg=new RadioGroup[ALL_item_description_count][content_item_count];//內文radio group的監聽(二維:組、每組細項(取全部以防不夠))
        content_et=new EditText[ALL_item_description_count][content_item_count];//內文EditText的監聽(二維:組、每組細項(取全部以防不夠))
        //找出最外層item_ID組數
        SQL_command="SELECT DISTINCT itemID,item FROM " + TBname;
        recSet=compDBHper.get(SQL_command);
        item_ID_set=pre_work();
        int item_ID_count=item_ID_set.size();
        db_result=new ArrayList<>();
        element_IDs=new ArrayList<>();
        //建置主要分成三層:最外層item_ID、中層base_creator檢查項目item_description、裏層content_creator檢查細項check_list(可能有可能沒有)
        for(int x=0;x<item_ID_count/2;x++){
            /**************************************************************************************************/
            SQL_command="SELECT DISTINCT item_description FROM " + TBname + " WHERE itemID='" + item_ID_set.get(x*2) + "'";
            recSet=compDBHper.get(SQL_command);
            item_description_set=pre_work();
            int item_description_count=item_description_set.size();//單個itemID所對應到的項目數量
            //開始建置
            for (int i=0;i<item_description_count;i++){
                base_creator(item_description_set.get(i),i,x);
            }
        }
        SQL_command="SELECT taskcard_attach_item_pkey FROM " + TBname;
        recSet=compDBHper.get(SQL_command);
        taskcard_attach_item_pkey=pre_work();

        //以上初始完成
        //初始完成時就先寫入db，以防第一次登載時沒儲存就離開(導致checked_result中無資料提供編輯時查找)
        if (postMod.equals("new")){
            save();
        }
        //如果是編輯模式 需要將歷史紀錄填回表格

        else if (postMod.equals("edit")){
            SQL_command="SELECT data FROM checked_result WHERE PMID='" + PMID + "'";
            recSet=compDBHper.get(SQL_command);
            ArrayList<String> history_arr=pre_work();//去除#的內容(內文)

            //二維id array轉一維結果
            ArrayList<Integer> id_arr=new ArrayList<>();
            for(int i=0;i<element_IDs.size();i++){
                for (int j=0;j<element_IDs.get(i).size();j++){
                    id_arr.add(element_IDs.get(i).get(j));
                }
            }
            //將歷史結果依照類型(radio,edittext,checkbox)塞回
            //使用三個arraylist:type_record(所有細項的類型),id_arr(所有細項對應的id),history_arr(資料庫提取出所有細項的結果) 以上大小應相同
            for (int x=0;x<history_arr.size();x++){
                switch (type_record.get(x)){
                    case "r":
                        //radio
                        RadioGroup radioGroup=((RadioGroup) myView.findViewById(id_arr.get(x)));
                        if (history_arr.get(x).equals("合格")){
                            RadioButton radioButton= (RadioButton) radioGroup.getChildAt(0);
                            radioGroup.check(radioButton.getId());
                        }
                        else if (history_arr.get(x).equals("異常")){
                            RadioButton radioButton= (RadioButton) radioGroup.getChildAt(1);
                            radioGroup.check(radioButton.getId());
                        }
                        break;
                    case "c":
                        //checkbox
                        LinearLayout checkBox_group=((LinearLayout) myView.findViewById(id_arr.get(x)));
                        int checkbox_count=checkBox_group.getChildCount();//checkbox的數量
                        //先取得checkbox的名字
                        ArrayList<String> checkbox_name=new ArrayList<>();
                        for (int i=0;i<checkbox_count;i++){
                            CheckBox checkBox=(CheckBox) checkBox_group.getChildAt(i);
                            checkbox_name.add((String) checkBox.getText());
                        }
                        //再與資料庫結果比對，並依照位置填上
                        String[] checkbox_history= history_arr.get(x).split("、");
                        for (int i=0;i<checkbox_history.length;i++){
                            for (int j=0;j<checkbox_name.size();j++){
                                if (checkbox_history[i].equals(checkbox_name.get(j))){
                                    CheckBox checkBox=(CheckBox) checkBox_group.getChildAt(j);
                                    checkBox.setChecked(true);
                                }
                            }
                        }
                        break;
                    case "e":
                        //edittext
                        EditText editText=((EditText) myView.findViewById(id_arr.get(x)));
                        editText_id=id_arr.get(x);
                        if (history_arr.get(x).equals("null")){
                            editText.setText("");
                        }
                        else{
                            editText.setText(history_arr.get(x));
                        }
                        break;
                }
            }
        }
        return myView;
    }
    //建構ScrollView中的內容
    public void base_creator(String title, int check_list,int current_itemID){
        ArrayList<Integer> innerIDlist=new ArrayList<>();
        ArrayList<String> innerResultList=new ArrayList<>();
        //內文的項目(放這的目的是先分出有無內文)
        SQL_command="SELECT check_list,standard_type,standard FROM " + TBname + " WHERE item_description='" + item_description_set.get(check_list) + "' AND check_list IS NOT NULL";
        recSet=compDBHper.get(SQL_command);
        check_set=pre_work();//去除#的內容(內文)
        check_set_count=check_set.size();//計算所有內容物(除以3等於有幾組內容物)，於內文中使用

        LinearLayout.LayoutParams weight_set=new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT,1);
        LinearLayout.LayoutParams weight_set_2=new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT,2);
        LinearLayout.LayoutParams weight_set_8=new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT,8);
        LinearLayout.LayoutParams margin_set=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        margin_set.setMargins(0,0,0,20);
        /**vlFather*/
        vlFather=((LinearLayout) myView.findViewById(R.id.vlIdFather));
        /**vl_1**/
        LinearLayout vl_1=new LinearLayout(getActivity());
        vl_1.setOrientation(LinearLayout.VERTICAL);
        vl_1.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.border_shade));
        vl_1.setLayoutParams(margin_set);

        TextView tvItemID=new TextView(getActivity());
        tvItemID.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        String itemID_title=item_ID_set.get(current_itemID*2) + " " + item_ID_set.get(current_itemID*2+1);
        tvItemID.setText(itemID_title);
        tvItemID.setTextSize(34);
        tvItemID.setGravity(Gravity.CENTER);
        tvItemID.setBackgroundResource(R.drawable.area_shade);
        vl_1.addView(tvItemID);
        /*************************************************標頭linearLayout(horizontal)**/
        LinearLayout hl_1_title=new LinearLayout(getActivity());
        hl_1_title.setOrientation(LinearLayout.HORIZONTAL);
        /**tvTitle**/
        TextView tvTitle=new TextView(getActivity());
        tvTitle.setTextSize(30);
        tvTitle.setText(title);
        tvTitle.setGravity(Gravity.CENTER_VERTICAL);
        tvTitle.setLayoutParams(weight_set);
        /**radioGroup**/
        RadioGroup radioGroup=new RadioGroup(getActivity());
        radioGroup.setOrientation(LinearLayout.HORIZONTAL);
        radioGroup.setLayoutParams(weight_set);
        radioGroup.setId(id);//設定id,類型以獲取結果
        type_record.add("r");
        innerIDlist.add(id);
        innerResultList.add(null);
        id+=1;
        if(check_set_count==0){
            //存取無內容的項目的radio group的結果
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                    RadioButton radioButton=((RadioButton) myView.findViewById(checkedId));
                    String text= (String) radioButton.getText();

                    //找出被點擊按鈕當前的位置
                    int current_set=0,current_item=0;
                    for (int j=0;j<ALL_item_description_count;j++){
                        for (int k=0;k<element_IDs.get(j).size();k++){
                            if(radioGroup.getId()==element_IDs.get(j).get(k)){
                                //找出被點擊按鈕當前的位置
                                current_set=j;
                                current_item=k;
                            }
                        }
                    }
                    db_result.get(current_set).set(current_item,text);
                }
            });
        }
        RadioButton btRadio_yes=new RadioButton(getActivity());
        btRadio_yes.setTextSize(28);
        btRadio_yes.setText("合格");
        btRadio_yes.setLayoutParams(weight_set);
        btRadio_yes.setBackgroundResource(R.drawable.radiobtn_pass_selector);
        btRadio_yes.setButtonDrawable(R.drawable.radio_pass_icon_selector);
        btRadio_yes.setPadding(20,0,0,0);
        btRadio_yes.setId(id);//設定id,類型以獲取結果
        id+=1;
        if (check_set_count==0){
            btRadio_yes.setClickable(true);
        }
        else {
            btRadio_yes.setClickable(false);
        }

        RadioButton btRadio_no=new RadioButton(getActivity());
        btRadio_no.setTextSize(28);
        btRadio_no.setText("異常");
        btRadio_no.setLayoutParams(weight_set);
        btRadio_no.setBackgroundResource(R.drawable.radiobtn_fail_selector);
        btRadio_no.setButtonDrawable(R.drawable.radio_fail_icon_selector);
        btRadio_no.setPadding(20,0,0,0);
        btRadio_no.setId(id);//設定id,類型以獲取結果
        id+=1;
            if (check_set_count==0){
            btRadio_no.setClickable(true);
        }
        else {
            btRadio_no.setClickable(false);
        }

        radioGroup.addView(btRadio_yes);
        radioGroup.addView(btRadio_no);
        /**標頭建置**/
        hl_1_title.addView(tvTitle);
        hl_1_title.addView(radioGroup);
        /********************************內容linearLayout(vertical)**/
        LinearLayout vlContent=new LinearLayout(getActivity());
        vlContent.setOrientation(LinearLayout.VERTICAL);
        vlContent.setDividerDrawable(ContextCompat.getDrawable(getContext(),R.drawable.divider));
        vlContent.setShowDividers(LinearLayout.SHOW_DIVIDER_BEGINNING);
        if(check_set_count!=0){
            //有內文才做
            content_creator(vlContent, innerIDlist,innerResultList);
        }

        /********************************標尾linearLayout(vertical)**/
        LinearLayout vlEnd=new LinearLayout(getActivity());
        vlEnd.setOrientation(LinearLayout.VERTICAL);
        vlEnd.setDividerDrawable(ContextCompat.getDrawable(getContext(),R.drawable.divider));
        vlEnd.setShowDividers(LinearLayout.SHOW_DIVIDER_BEGINNING);
        /**linearLayout(horizontal)**/
        LinearLayout hl_1_end=new LinearLayout(getActivity());
        hl_1_end.setOrientation(LinearLayout.HORIZONTAL);
        /**tvEnd**/
        TextView tvEnd=new TextView(getActivity());
        tvEnd.setTextSize(28);
        tvEnd.setText("備註:");
        tvEnd.setGravity(Gravity.CENTER_VERTICAL);
        tvEnd.setPadding(100,0,0,0);
        tvEnd.setLayoutParams(weight_set_2);
        /**etOther**/
        EditText etOther=new EditText(getActivity());
        etOther.setTextSize(28);
        etOther.setLayoutParams(weight_set_8);
        SpannableString st=new SpannableString("other..");
        etOther.setHint(st);
        etOther.setId(id);
        type_record.add("e");

        innerIDlist.add(id);
        innerResultList.add(null);
        id+=1;

        final int[] fdf = {0};
        etOther.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                fdf[0] =view.getId();
                editText_id=fdf[0];
                return false;
            }
        });
        etOther.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int ii, int i1, int i2) {
                int current_set=0;
                int current_item=0;
                for (int j=0;j<ALL_item_description_count;j++){
                    for (int k=0;k<element_IDs.get(j).size();k++){
                        if(fdf[0]==element_IDs.get(j).get(k) || editText_id==element_IDs.get(j).get(k)){
                            //找出被點擊按鈕(fdf[0])id在陣列中(element_IDs)的位置
                            //在編輯模式的初始狀態下則是要比對editText_id在陣列中(element_IDs)的位置
                            current_set=j;
                            current_item=k;
                        }
                    }
                }
                EditText et=((EditText) myView.findViewById(editText_id));
                Editable text= et.getText();
                db_result.get(current_set).set(current_item, String.valueOf(text));
                if(text.toString().isEmpty()){
                    db_result.get(current_set).set(current_item, null);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        /**標尾建置**/
        hl_1_end.addView(tvEnd);
        hl_1_end.addView(etOther);
        vlEnd.addView(hl_1_end);
        /**建置**/
        vl_1.addView(hl_1_title);
        vl_1.addView(vlContent);
        vl_1.addView(vlEnd);
        vlFather.addView(vl_1);
        //ID收集、結果預建置
        element_IDs.add(innerIDlist);
        db_result.add(innerResultList);
    }
    public void content_creator(LinearLayout vlContent, ArrayList innerIDlist, ArrayList innerResultList){
        LinearLayout.LayoutParams weight_set=new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT,1);
        LinearLayout.LayoutParams margin_set=new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT,1);
        margin_set.setMargins(100,0,0,0);
        switch (check_set.get(1)){
            case "0":
                for (int i=0;i<(check_set_count/3);i++){
                    TextView tvCheckItem=new TextView(getActivity());
                    tvCheckItem.setTextSize(28);
                    tvCheckItem.setText(check_set.get(i*3));
                    tvCheckItem.setPadding(100,0,0,0);
                    tvCheckItem.setHeight(100);
                    tvCheckItem.setGravity(Gravity.CENTER_VERTICAL);

                    content_rg[radio_group_set][i]=new RadioGroup(getActivity());
                    content_rg[radio_group_set][i].setOrientation(LinearLayout.HORIZONTAL);
                    content_rg[radio_group_set][i].setPadding(100,0,0,0);
                    content_rg[radio_group_set][i].setId(id);
                    type_record.add("r");

                    innerIDlist.add(id);//設定id,類型以獲取結果
                    innerResultList.add(null);
                    id+=1;
                    /****/
                    content_rg[radio_group_set][i].setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                            int int_result=0;//對應radio button位置(合格0、異常1)
                            String string_result = null;//對應到要寫入結果的值
                            int current_set=0,current_item=0;
                            for (int j=0;j<ALL_item_description_count;j++){
                                for (int k=0;k<element_IDs.get(j).size();k++){
                                    if(radioGroup.getId()==element_IDs.get(j).get(k)){
                                        //找出被點擊按鈕當前的位置
                                        current_set=j;
                                        current_item=k;
                                    }
                                }
                            }
                            RadioButton radioButton=((RadioButton) myView.findViewById(checkedId));
                            String text= (String) radioButton.getText();
                            db_result.get(current_set).set(current_item,text);

                            for (int c=1;c<element_IDs.get(current_set).size()-1;c++){
                                if(db_result.get(current_set).get(c)==null){
                                    return;
                                }
                                else if(db_result.get(current_set).get(c)=="合格"){
                                    int_result=0;
                                    string_result="合格";
                                }
                                else if(db_result.get(current_set).get(c)=="異常"){
                                    int_result=1;
                                    string_result="異常";
                                    break;
                                }
                            }
                            RadioGroup title_rq=((RadioGroup) myView.findViewById(element_IDs.get(current_set).get(0)));
                            RadioButton rb=(RadioButton) title_rq.getChildAt(int_result);
                            //rb.setChecked(true);
                            title_rq.check(rb.getId());
                            db_result.get(current_set).set(0,string_result);
                        }
                    });

                    RadioButton btRadio_yes=new RadioButton(getActivity());
                    btRadio_yes.setTextSize(28);
                    btRadio_yes.setText("合格");
                    btRadio_yes.setHeight(100);
                    btRadio_yes.setLayoutParams(weight_set);
                    btRadio_yes.setBackgroundResource(R.drawable.radiobtn_pass_selector);
                    btRadio_yes.setButtonDrawable(R.drawable.radio_pass_icon_selector);
                    btRadio_yes.setPadding(20,0,0,0);
                    btRadio_yes.setId(id);// 設定id,類型以獲取結果
                    id+=1;

                    RadioButton btRadio_no=new RadioButton(getActivity());
                    btRadio_no.setTextSize(28);
                    btRadio_no.setText("異常");
                    btRadio_no.setHeight(100);
                    btRadio_no.setLayoutParams(weight_set);
                    btRadio_no.setBackgroundResource(R.drawable.radiobtn_fail_selector);
                    btRadio_no.setButtonDrawable(R.drawable.radio_fail_icon_selector);
                    btRadio_no.setPadding(20,0,0,0);
                    btRadio_no.setId(id);//設定id,類型以獲取結果//設定id以獲取結果
                    id+=1;

                    content_rg[radio_group_set][i].addView(btRadio_yes);
                    content_rg[radio_group_set][i].addView(btRadio_no);
                    Space space=new Space(getActivity());
                    space.setLayoutParams(weight_set);
                    content_rg[radio_group_set][i].addView(space);

                    vlContent.addView(tvCheckItem);
                    vlContent.addView(content_rg[radio_group_set][i]);
                }
                radio_group_set+=1;//作為radio_group組數"連續"標記
                break;
            case "1":
                for (int i=0;i<(check_set_count/3);i++){
                    TextView tvMultiCheckItem=new TextView(getActivity());
                    tvMultiCheckItem.setTextSize(28);
                    tvMultiCheckItem.setText(check_set.get(i*3));
                    tvMultiCheckItem.setPadding(100,0,0,0);
                    tvMultiCheckItem.setHeight(100);
                    tvMultiCheckItem.setGravity(Gravity.CENTER_VERTICAL);

                    LinearLayout vlCheckboxGroup=new LinearLayout(getActivity());//需要將每項的多選框分組，達到像radioGroup的效果
                    vlCheckboxGroup.setOrientation(LinearLayout.HORIZONTAL);
                    vlCheckboxGroup.setPadding(100,0,0,0);
                    vlCheckboxGroup.setId(id);
                    type_record.add("c");
                    innerIDlist.add(id);//設定id,類型以獲取結果
                    innerResultList.add(null);
                    id+=1;

                    String[] checkbox_item=check_set.get(i*3).split(",");
                    for (int x=0;x<checkbox_item.length;x++){
                        CheckBox checkBox=new CheckBox(getActivity());
                        checkBox.setTextSize(28);
                        checkBox.setLayoutParams(weight_set);
                        checkBox.setText(checkbox_item[x]);
                        checkBox.setId(id);
                        id+=1;
                        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                int parentLayout=((LinearLayout) compoundButton.getParent()).getId();
                                int current_set=0,current_item=0;
                                for (int j=0;j<ALL_item_description_count;j++){
                                    for (int k=0;k<element_IDs.get(j).size();k++){
                                        if(parentLayout==element_IDs.get(j).get(k)){
                                            //找出被點擊按鈕當前的位置
                                            current_set=j;
                                            current_item=k;
                                        }
                                    }
                                }
                                //看checkbox group有多少個checkbox，並進行結果判斷
                                    //1.多少個checkbox(counter)
                                int counter=0;
                                while (true){
                                    if (((LinearLayout) compoundButton.getParent()).getChildAt(counter)!=null){
                                        counter++;
                                    }
                                    else{
                                        break;
                                    }
                                }
                                    //2.將cb結果寫入暫存buffer
                                ArrayList<Integer> cb_num_buffer=new ArrayList<>(counter);//顯示在項目的 合格 異常
                                ArrayList<String> cb_result_buffer=new ArrayList<>(counter);//細項的結果
                                int re=0;
                                String cb_result = "";
                                for (int w=0;w<counter;w++){
                                    CheckBox cb= (CheckBox) ((LinearLayout) compoundButton.getParent()).getChildAt(w);
                                    if (cb.isChecked()){
                                        cb_num_buffer.add(w,1);
                                        cb_result_buffer.add(w, String.valueOf(cb.getText()));//被選取的checkbox名稱
                                    }
                                    else {
                                        cb_num_buffer.add(w,0);
                                        cb_result_buffer.add(w,null);
                                    }
                                    re+=cb_num_buffer.get(w);//re=0不顯示,re=1合格,其他異常
                                    if(cb_result_buffer.get(w)!=null){
                                        //將不是空值的checkbox結果加上頓號串在一起
                                        cb_result+=cb_result_buffer.get(w) + "、";
                                    }
                                }
                                if (cb_result.length()!=0){
                                    cb_result=cb_result.substring(0,cb_result.length()-1);//去除字尾頓號
                                }
                                else{
                                    cb_result=null;
                                }
                                db_result.get(current_set).set(current_item,cb_result);//勾選項目 寫入
                                    //3.項目結果判斷
                                String parent_result;
                                RadioGroup title_rq=((RadioGroup) myView.findViewById(element_IDs.get(current_set).get(0)));
                                if(re==0){
                                    title_rq.clearCheck();
                                    parent_result=null;
                                }
                                else if(re==1){
                                    RadioButton rb=(RadioButton) title_rq.getChildAt(0);
                                    title_rq.check(rb.getId());
                                    parent_result="合格";
                                }
                                else{
                                    RadioButton rb=(RadioButton) title_rq.getChildAt(1);
                                    title_rq.check(rb.getId());
                                    parent_result="異常";
                                }
                                db_result.get(current_set).set(0,parent_result);//合格 異常 寫入
                            }
                        });
                        vlCheckboxGroup.addView(checkBox);
                    }
                    vlContent.addView(tvMultiCheckItem);
                    vlContent.addView(vlCheckboxGroup);
                }
                break;
            case "2":
                for (int i=0;i<(check_set_count/3);i++){
                    LinearLayout item_box=new LinearLayout(getActivity());
                    item_box.setOrientation(LinearLayout.HORIZONTAL);
                    TextView tvInputItem=new TextView(getActivity());
                    tvInputItem.setTextSize(28);
                    tvInputItem.setText(check_set.get(i*3));
                    tvInputItem.setPadding(100,0,0,0);
                    tvInputItem.setLayoutParams(weight_set);
                    tvInputItem.setHeight(100);
                    tvInputItem.setGravity(Gravity.CENTER_VERTICAL);
                    item_box.addView(tvInputItem);
                    if(i==0){
                        TextView tvStandard=new TextView(getActivity());
                        tvStandard.setTextSize(28);
                        tvStandard.setText(check_set.get(i+2));
                        tvStandard.setLayoutParams(weight_set);
                        tvStandard.setGravity(Gravity.CENTER);
                        item_box.addView(tvStandard);
                    }
                    LinearLayout input_box=new LinearLayout(getActivity());
                    input_box.setOrientation(LinearLayout.HORIZONTAL);

                    content_et[editText_set][i]=new EditText(getActivity());
                    content_et[editText_set][i].setSingleLine();
                    content_et[editText_set][i].setTextSize(28);
                    content_et[editText_set][i].setHint("輸入數值");
                    content_et[editText_set][i].setPadding(100,0,0,0);
                    content_et[editText_set][i].setLayoutParams(margin_set);
                    content_et[editText_set][i].setHeight(100);
                    content_et[editText_set][i].setGravity(Gravity.CENTER_VERTICAL);
                    content_et[editText_set][i].setBackgroundResource(R.drawable.radiobtn_unchecked);
                    content_et[editText_set][i].setInputType(InputType.TYPE_CLASS_NUMBER);
                    content_et[editText_set][i].setId(id);//設定id,類型以獲取結果
                    type_record.add("e");

                    innerIDlist.add(id);
                    innerResultList.add(null);
                    id+=1;
                    /****/
                    final int[] fdf = {0};
                    content_et[editText_set][i].setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            fdf[0] =view.getId();
                            editText_id=fdf[0];
                            return false;
                        }
                    });
                    String et_range=check_set.get(i*3+2);

                    String[] et_buffer={"",""};
                    int et_count=0;
                    int et_count1=0;
                    for (int o=0;o<et_range.length();o++){
                        if (et_range.charAt(o)>=48 && et_range.charAt(o)<=57){
                            et_buffer[et_count]+=et_range.charAt(o);
                            et_count1=1;
                        }
                        else {
                            if (et_count1==1) {
                                et_count+=1;
                                et_count1=0;
                            }
                        }
                    }
                    int et_min=0;
                    int et_max=0;
                    if(et_range.charAt(0)=='<'){
                        et_max= Integer.parseInt(String.valueOf(et_buffer[0]));
                    }
                    else {
                        et_min= Integer.parseInt(String.valueOf(et_buffer[0]));
                        et_max= Integer.parseInt(String.valueOf(et_buffer[1]));
                    }


                    int finalEt_min = et_min;
                    int finalEt_max = et_max;
                    content_et[editText_set][i].addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int ii, int i1, int i2) {


                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            int current_set=0;
                            int current_item=0;
                            for (int j=0;j<ALL_item_description_count;j++){
                                for (int k=0;k<element_IDs.get(j).size();k++){
                                    if(fdf[0]==element_IDs.get(j).get(k) || editText_id==element_IDs.get(j).get(k)){
                                        //找出被點擊按鈕(fdf[0])id在陣列中(element_IDs)的位置
                                        //在編輯模式的初始狀態下則是要比對editText_id在陣列中(element_IDs)的位置
                                        current_set=j;
                                        current_item=k;
                                    }
                                }
                            }
                            EditText et=((EditText) myView.findViewById(editText_id));
                            Editable text= et.getText();
                            db_result.get(current_set).set(current_item, String.valueOf(text));

                            if(text.toString().isEmpty()){
                                et.setBackgroundResource(R.drawable.radiobtn_unchecked);
                                db_result.get(current_set).set(current_item, null);
                            }
                            else {
                                int et_value=Integer.parseInt(String.valueOf(text));

                                if(et_value>= finalEt_min && et_value<= finalEt_max){
                                    et.setBackgroundResource(R.drawable.radiobtn_pass);
                                }
                                else {
                                    et.setBackgroundResource(R.drawable.radiobtn_fail);
                                }
                            }
                            int int_result=0;//對應radio button位置(合格0、異常1)
                            String string_result = null;//對應到要寫入結果的值
                            for (int c=1;c<element_IDs.get(current_set).size()-1;c++){
                                if(db_result.get(current_set).get(c)==null){
                                    return;
                                }
                                else if(Integer.parseInt(String.valueOf(db_result.get(current_set).get(c)))>= finalEt_min &&Integer.parseInt(String.valueOf(db_result.get(current_set).get(c)))<= finalEt_max){
                                    int_result=0;
                                    string_result="合格";
                                }
                                else {
                                    int_result=1;
                                    string_result="異常";
                                    break;
                                }
                            }
                            RadioGroup title_rq=((RadioGroup) myView.findViewById(element_IDs.get(current_set).get(0)));
                            RadioButton rb=(RadioButton) title_rq.getChildAt(int_result);

                            title_rq.check(rb.getId());
                            db_result.get(current_set).set(0,string_result);

                        }
                    });
                    TextView tvValue=new TextView(getActivity());
                    tvValue.setTextSize(28);
                    tvValue.setText("0");
                    tvValue.setLayoutParams(weight_set);
                    tvValue.setGravity(Gravity.CENTER);
                    input_box.addView(content_et[editText_set][i]);
                    input_box.addView(tvValue);

                    vlContent.addView(item_box);
                    vlContent.addView(input_box);
                }
                editText_set+=1;
                break;
        }
    }

    public static void save(){
        Log.d("ids", String.valueOf(element_IDs));
        Log.d("db_result", String.valueOf(db_result));
        //二維結果轉一維結果
        ArrayList<String> result_dim1=new ArrayList<>();
        ArrayList<String> taskcard_attach_item_pkey_1=new ArrayList<>();
        int count1=0;
        for(int i=0;i<db_result.size();i++){
            int count2=0;
            for (int j=0;j<db_result.get(i).size();j++){
                result_dim1.add(db_result.get(i).get(j));
                if (j==db_result.get(i).size()-1){
                    taskcard_attach_item_pkey_1.add(taskcard_attach_item_pkey.get(count1)+"_ex");
                }
                else{
                    taskcard_attach_item_pkey_1.add(taskcard_attach_item_pkey.get(count1+count2));
                    count2+=1;
                }
            }
            count1+=count2;
        }
        //儲存時將原有資料刪除，重寫入一次
        String sql="DELETE FROM checked_result WHERE PMID='" + PMID + "'";
        compDBHper.delete_update(sql);
        compDBHper.set("checked_result",PMID,result_dim1,taskcard_attach_item_pkey_1);
    }
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
