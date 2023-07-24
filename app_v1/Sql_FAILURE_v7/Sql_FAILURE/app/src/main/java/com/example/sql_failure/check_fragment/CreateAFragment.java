package com.example.sql_failure.check_fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Space;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.sql_failure.CheckActivity;
import com.example.sql_failure.CompDBHper;
import com.example.sql_failure.R;
import com.example.sql_failure.post_activities.TaskcardSelectActivity;
import com.facebook.stetho.Stetho;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;

public class CreateAFragment extends Fragment {
    private TextView tvFragCheck;
    private String taskcard_attach_pkey;
    private static String PMID;
    private String postMod;
    /**第三類型會使用到日期選擇**/
    private DatePickerDialog datePicker;//第三類型會使用到日期選擇
    private Calendar calendar;
    private String checked_date;//使用者選擇的日期
    /****/
    private LinearLayout vlFather;

    private RadioGroup content_rg[][];
    private EditText content_et[][];
    /**SQL**/
    private static final String DBname="myDB1.db";
    private static final int DBversion=1;
    private static final String TBname="taskcard_attach_template_new";
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

    private ArrayList<String> expand_item_set=new ArrayList<>();//去除#的內容(第4類型的素材)
    private int expand_item_set_count;//第4類型的素材數量(/2為組數)
    private int radio_group_set=0;//作為radio_group組數標記
    private int editText_set=0;//作為EditText_set組數標記
    private int editText_id;//紀錄editText的id，當editText內容改變時無法像radioGroup一樣取得當前物件並取得id，所以只能靠onTouch來取得id。
    //但編輯模式會直接改變editText的值，因此無法靠onTouch來取得id，就必須用此參數作為id紀錄
    /*****/
    private static ArrayList<ArrayList<Integer>> element_IDs=new ArrayList<>();//收藏所有id們，element_IDs[組別][小項目]
    private ArrayList<Integer> id_arr=new ArrayList<>();//一維 element_IDs
    private static ArrayList<ArrayList<String>> db_result=new ArrayList<>();//收藏使用者的輸入結果(即將寫入db)，db_result[組別][小項目]

    private ArrayList<String> all_standard_type;//去除#的內容(計數在同一itemID下整個表格的item數量，作為二維陣列初始化值，以防不夠用)
    private ArrayList<String> type_record=new ArrayList<>();//標頭固定為radioGroup但standard_type不一定是0，所以另用一個array來記錄各項的type(在編輯模式中會需要知道type才能把紀錄塞回去)
    private int content_item_count;
    /*****/
    ArrayList<String> history_arr=new ArrayList<>();
    private int id=1;//給各元件的id，從1開始
    private boolean edit_sign=false;//回填資料標示(用於第四類型)
    private ArrayList<Integer> popup_btn_clicked=new ArrayList<>();//紀錄按過的按鈕，防止popup資料被多次回填

    private ArrayList<Integer>  all_popup_IDs=new ArrayList<>();//收集所有popup元件的id
    private int pos=0;//all_popup_IDs中的位置，編輯模式使用到
    private View myView;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CreateAFragment() {
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
    public static CreateAFragment newInstance(String param1, String param2) {
        CreateAFragment fragment = new CreateAFragment();
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
        //taskcard_name=fromStatus.get(0);
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
        myView=inflater.inflate(R.layout.fragment_create, container, false);


        try {
            compDBHper.createDatabase();
        } catch (Exception e) {
            throw new Error(e.toString());
        }
        /****/
        String SQL_command="SELECT type,taskcard_attach_pkey FROM " + "check_condition"  + " WHERE PMID='" + PMID + "'";   //拿取過去紀錄
        recSet=compDBHper.get(SQL_command);
        ArrayList<String> history=pre_work();
        taskcard_attach_pkey=history.get(1);


        //找出所有項目(不分itemID)
        SQL_command="SELECT item_description FROM " + TBname +" WHERE taskcard_attach_pkey='" + taskcard_attach_pkey + "' AND parent_child='p' AND standard_type < 5";
        recSet=compDBHper.get(SQL_command);
        ArrayList<String> ALL_item_description_set=pre_work();
        ALL_item_description_count=ALL_item_description_set.size();

        //找出所有細項目(不分itemID、取全部以防不夠)
        SQL_command="SELECT standard_type FROM " + TBname+" WHERE taskcard_attach_pkey='" + taskcard_attach_pkey + "' AND standard_type < 5";
        recSet=compDBHper.get(SQL_command);
        all_standard_type=pre_work();
        content_item_count=all_standard_type.size();

        content_rg=new RadioGroup[ALL_item_description_count][content_item_count];//內文radio group的監聽(二維:組、每組細項(取全部以防不夠))
        content_et=new EditText[ALL_item_description_count][content_item_count];//內文EditText的監聽(二維:組、每組細項(取全部以防不夠))
        //找出最外層item_ID組數
        SQL_command="SELECT DISTINCT itemID,item FROM " + TBname+" WHERE taskcard_attach_pkey='" + taskcard_attach_pkey + "' AND standard_type < 5";
        recSet=compDBHper.get(SQL_command);
        item_ID_set=pre_work();
        int item_ID_count=item_ID_set.size();
        db_result=new ArrayList<>();
        element_IDs=new ArrayList<>();
        //建置主要分成三層:最外層item_ID、中層base_creator檢查項目item_description、裏層content_creator檢查細項check_list(可能有可能沒有)
        for(int x=0;x<item_ID_count/2;x++){
            /**************************************************************************************************/
            SQL_command="SELECT item_description FROM " + TBname + " WHERE itemID='" + item_ID_set.get(x*2) + "' AND item='" + item_ID_set.get(x*2+1)+"' AND taskcard_attach_pkey='" + taskcard_attach_pkey + "' AND parent_child='p' AND standard_type < 5";
            recSet=compDBHper.get(SQL_command);
            item_description_set=pre_work();
            int item_description_count=item_description_set.size();//單個itemID所對應到的項目數量
            //開始建置
            for (int i=0;i<item_description_count;i++){
                base_creator(item_description_set.get(i),i,x);
            }
        }
        SQL_command="SELECT taskcard_attach_item_pkey FROM " + TBname+" WHERE taskcard_attach_pkey='" + taskcard_attach_pkey + "' AND standard_type < 5";
        recSet=compDBHper.get(SQL_command);
        taskcard_attach_item_pkey=pre_work();

        //以上初始完成
        //初始完成時就先寫入db，以防第一次登載時沒儲存就離開(導致checked_result中無資料提供編輯時查找)

        SQL_command="SELECT data FROM checked_result WHERE PMID='" + PMID + "'";
        recSet=compDBHper.get(SQL_command);

        if (recSet.size()==0){
            edit_sign=false;
            save();
        }
        //如果是編輯模式 需要將歷史紀錄填回表格

        else {
            edit_sign=true;//須回填資料
            history_arr=pre_work();//去除#的內容(內文)

            //二維id array轉一維結果
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
                        int s = 0,n=0;
                        for (int i=0;i<element_IDs.size();i++){
                            for (int j=0;j<element_IDs.get(i).size();j++){
                                if (id_arr.get(x).equals(element_IDs.get(i).get(j))){
                                    s= i;
                                    n=j;
                                }
                            }
                        }
                        RadioGroup radioGroup=((RadioGroup) myView.findViewById(id_arr.get(x)));
                        if (history_arr.get(x).equals("合格")){
                            RadioButton radioButton= (RadioButton) radioGroup.getChildAt(0);
                            radioGroup.check(radioButton.getId());
                            db_result.get(s).set(n,history_arr.get(x));
                        }
                        else if (history_arr.get(x).equals("異常")){
                            RadioButton radioButton= (RadioButton) radioGroup.getChildAt(1);
                            radioGroup.check(radioButton.getId());
                            db_result.get(s).set(n,history_arr.get(x));
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
                    case "d":
                        //date select
                        //把歷史紀錄填回去(bt.setText)，但是如果在儲存前都沒有觸發onClick的話就不會被寫入db_result，因此須預先寫入db_result
                        int set = 0,num=0;
                        for (int i=0;i<element_IDs.size();i++){
                            for (int j=0;j<element_IDs.get(i).size();j++){
                                if (id_arr.get(x).equals(element_IDs.get(i).get(j))){
                                    set= i;
                                    num=j;
                                }
                            }
                        }
                        Button button=((Button) myView.findViewById(id_arr.get(x)));
                        if (history_arr.get(x).equals("null")){
                            button.setText("");
                        }
                        else{
                            button.setText(history_arr.get(x));
                            db_result.get(set).set(num,history_arr.get(x));
                        }
                        break;
                    case "mt":
                        //彈出視窗，填寫多表格
                        //不在此處回填，每個按鈕的popup都會動態生成元件，layout會被刷新，導致無法查找元件，因此在每個觸發popup的按鈕onClick中才回填數值
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
        SQL_command="SELECT check_list,standard_type,standard,check_list_equation FROM " + TBname + " WHERE item_description='" + title + "' AND taskcard_attach_pkey='" + taskcard_attach_pkey + "' AND itemID='" + item_ID_set.get(current_itemID*2) + "' AND item='" + item_ID_set.get(current_itemID*2+1) + "' AND parent_child='c' AND standard_type < 5";
        recSet=compDBHper.get(SQL_command);
        check_set=pre_work();//去除#的內容(內文)
        check_set_count=check_set.size();//計算所有內容物(除以3等於有幾組內容物)，於內文中使用

        //此SQL是為了收集第4類型的素材
        SQL_command="SELECT standard,check_list_variable,item FROM " + TBname + " WHERE item_description='" + title + "' AND taskcard_attach_pkey='" + taskcard_attach_pkey + "' AND itemID='" + item_ID_set.get(current_itemID*2) + "' AND item='" + item_ID_set.get(current_itemID*2+1) + "' AND parent_child='c' AND standard_type='4'";
        recSet=compDBHper.get(SQL_command);
        expand_item_set=pre_work();//去除#的內容(內文)
        expand_item_set_count=expand_item_set.size();//計算所有內容物(除以3等於有幾組內容物)，於內文中使用

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
                    for (int j=0;j<element_IDs.size();j++){
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
                for (int j=0;j<element_IDs.size();j++){
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
        //a類型的工作說明書共4種屬性
        switch (check_set.get(1)){
            case "0":
                for (int i=0;i<(check_set_count/4);i++){
                    TextView tvCheckItem=new TextView(getActivity());
                    tvCheckItem.setTextSize(28);
                    tvCheckItem.setText(check_set.get(i*4));
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
                            for (int j=0;j<element_IDs.size();j++){
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

                            for (int c=1;c<db_result.get(current_set).size()-1;c++){
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
                for (int i=0;i<(check_set_count/4);i++){
                    TextView tvMultiCheckItem=new TextView(getActivity());
                    tvMultiCheckItem.setTextSize(28);
                    tvMultiCheckItem.setText(check_set.get(i*4));
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

                    String[] checkbox_item=check_set.get(i*4).split(",");
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
                                for (int j=0;j<element_IDs.size();j++){
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
                for (int i=0;i<(check_set_count/4);i++){
                    LinearLayout item_box=new LinearLayout(getActivity());
                    item_box.setOrientation(LinearLayout.HORIZONTAL);
                    TextView tvInputItem=new TextView(getActivity());
                    tvInputItem.setTextSize(28);
                    tvInputItem.setText(check_set.get(i*4));
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
                    content_et[editText_set][i].setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
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

                    String[] et_range=check_set.get(i*4+3).split(",");

                    String et_min=null;
                    String et_max=null;
                    String et_min_eq=null;
                    String et_max_eq=null;


                    for (int o=0;o<et_range.length;o++){
                        if (et_range[o].charAt(1)=='<' && et_range[o].charAt(2)=='='){
                            et_max_eq=et_range[o].substring(3);
                        } else if (et_range[o].charAt(1)=='>' && et_range[o].charAt(2)=='=') {
                            et_min_eq=et_range[o].substring(3);
                        } else if (et_range[o].charAt(1)=='<' && et_range[o].charAt(2)!='=') {
                            et_max=et_range[o].substring(2);
                        } else if (et_range[o].charAt(1)=='>' && et_range[o].charAt(2)!='=') {
                            et_min=et_range[o].substring(2);
                        }
                    }
                    String[] rangeCheck={et_min_eq, et_min, et_max_eq, et_max, null};



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
                            for (int j=0;j<element_IDs.size();j++){
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

                            if(text.toString().isEmpty() || String.valueOf(text).equals("-") || String.valueOf(text).equals("+")){
                                et.setBackgroundResource(R.drawable.radiobtn_unchecked);
                                db_result.get(current_set).set(current_item, null);
                            }
                            else {


                                String et_value=String.valueOf(text);
                                rangeCheck[4]=et_value;

                                if(RangeCheck(rangeCheck)){
                                    et.setBackgroundResource(R.drawable.radiobtn_pass);
                                }
                                else {
                                    et.setBackgroundResource(R.drawable.radiobtn_fail);
                                }
                            }
                            int int_result=0;//對應radio button位置(合格0、異常1)
                            String string_result = null;//對應到要寫入結果的值
                            for (int c=1;c<element_IDs.get(current_set).size()-1;c++){
                                String et_value=String.valueOf(db_result.get(current_set).get(c));

                                rangeCheck[4]=et_value;


                                if(db_result.get(current_set).get(c)==null){
                                    return;
                                }
                                else if(RangeCheck(rangeCheck)){
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
            case "3":
                //填寫日期
                for (int i=0;i<(check_set_count/4);i++) {
                    //細項
                    TextView tvCheckItem = new TextView(getActivity());
                    tvCheckItem.setTextSize(28);
                    tvCheckItem.setText(check_set.get(i * 4));
                    tvCheckItem.setPadding(100, 0, 0, 0);
                    tvCheckItem.setHeight(100);
                    tvCheckItem.setGravity(Gravity.CENTER_VERTICAL);

                    //日期選擇鈕
                    LinearLayout input_box=new LinearLayout(getActivity());//放入btCheckDate和一個space(不要讓btCheckDate太長)
                    input_box.setOrientation(LinearLayout.HORIZONTAL);
                    input_box.setPadding(100, 0, 0, 0);

                    Space space=new Space(getActivity());
                    space.setLayoutParams(weight_set);

                    Button btCheckDate=new Button(getActivity());
                    btCheckDate.setTextSize(28);
                    btCheckDate.setHeight(100);
                    btCheckDate.setGravity(Gravity.CENTER);
                    btCheckDate.setLayoutParams(weight_set);
                    btCheckDate.setBackgroundResource(R.drawable.datepicker_background);

                    btCheckDate.setId(id);//設定id,類型以獲取結果
                    type_record.add("d");

                    innerIDlist.add(id);
                    innerResultList.add(null);
                    id+=1;
                    btCheckDate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            calendar= Calendar.getInstance();
                            int day=calendar.get(Calendar.DAY_OF_MONTH);
                            int month=calendar.get(Calendar.MONTH);
                            int year=calendar.get(Calendar.YEAR);

                            datePicker=new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker datePicker, int mYear, int mMonth, int mDay) {
                                    checked_date=mYear + "/" + (mMonth+1) + "/" + mDay; //使用者輸入的日期
                                    btCheckDate.setText(checked_date);

                                    //日期填寫結果儲存
                                    int int_result=0;//對應radio button位置(合格0、異常1)
                                    String string_result = null;//對應到要寫入結果的值

                                    int current_set=0,current_item=0;
                                    for (int j=0;j<element_IDs.size();j++){
                                        for (int k=0;k<element_IDs.get(j).size();k++){
                                            if(btCheckDate.getId()==element_IDs.get(j).get(k)){
                                                //找出被點擊按鈕當前的位置
                                                current_set=j;
                                                current_item=k;
                                            }
                                        }
                                    }
                                    String text= (String) btCheckDate.getText();
                                    db_result.get(current_set).set(current_item,text);

                                    //標頭合格異常回填(全部有填即為合格)
                                    for (int c=1;c<db_result.get(current_set).size()-1;c++){
                                        if(db_result.get(current_set).get(c)==null){
                                            return;
                                        }
                                        else if(db_result.get(current_set).get(c)!=null){
                                            int_result=0;
                                            string_result="合格";
                                        }
                                    }
                                    RadioGroup title_rq=((RadioGroup) myView.findViewById(element_IDs.get(current_set).get(0)));
                                    RadioButton rb=(RadioButton) title_rq.getChildAt(int_result);
                                    title_rq.check(rb.getId());
                                    db_result.get(current_set).set(0,string_result);
                                }
                            },year,month,day);
                            datePicker.show();
                        }
                    });
                    input_box.addView(btCheckDate);
                    input_box.addView(space);

                    vlContent.addView(tvCheckItem);
                    vlContent.addView(input_box);
                }
                break;
            case "4":
                LinearLayout.LayoutParams btExpand_par=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                btExpand_par.setMargins(20,20,20,20);

                Button btExpandItem=new Button(getActivity());
                btExpandItem.setTextSize(26);
                btExpandItem.setHeight(100);
                btExpandItem.setText("點擊展開檢查表");
                btExpandItem.setGravity(Gravity.CENTER);
                btExpandItem.setLayoutParams(btExpand_par);
                btExpandItem.setBackgroundColor(getResources().getColor(R.color.teal_200));
                btExpandItem.setId(id);//將彈出視窗所有元件的結果串成字串，作為一組儲存
                type_record.add("mt");

                innerIDlist.add(id);
                innerResultList.add(null);
                id+=1;

                //type_4th_checked_list 動態新增
                final View[] type_4th_layout = {getLayoutInflater().inflate(R.layout.type_4th_checked_list, null)};//找尋layout文件
                LinearLayout vlFourthFather= type_4th_layout[0].findViewById(R.id.vlIdFourthFather);//裝所有項目的parent

                LinearLayout.LayoutParams expand_item_par=new LinearLayout.LayoutParams(0, 100,1);//均分等分
                expand_item_par.setMargins(10,10,10,10);
                LinearLayout.LayoutParams expand_item_et_par=new LinearLayout.LayoutParams(0, 100,2);//editText
                expand_item_et_par.setMargins(10,10,10,10);

                ArrayList<Integer> popup_IDs=new ArrayList<>();//在按下確認後透過這個ID array將各EDITTEXT的結果拉出來儲存(單個popup)
                //一個horizontal layout照順序裝:TextView(1-1)、EditText(填入電壓)、TextView(v)、EditText(填入電壓)、TextView(mΩ)
                for (int i=0;i<Integer.parseInt(expand_item_set.get(1));i++){
                    LinearLayout horizontal_box=new LinearLayout(getActivity());
                    if ((i+2)%2==0){
                        horizontal_box.setBackgroundColor(getResources().getColor(R.color.spinner_dark));
                    }

                    String[] sub_num_list=expand_item_set.get(2).split("NO.");
                    String sub_num=sub_num_list[1].substring(0,1);
                    String number= i + 1 + "-" + sub_num;
                    TextView tvNum=new TextView(getActivity());
                    tvNum.setText(number);
                    tvNum.setGravity(Gravity.CENTER);
                    tvNum.setTextSize(28);
                    tvNum.setLayoutParams(expand_item_par);

                    EditText etVoltage=new EditText(getActivity());
                    etVoltage.setGravity(Gravity.CENTER);
                    etVoltage.setTextSize(28);
                    etVoltage.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
                    etVoltage.setBackgroundResource(R.drawable.type__4th_edittexd_background);
                    etVoltage.setLayoutParams(expand_item_et_par);
                    int uniqueId_1 = View.generateViewId();
                    etVoltage.setId(uniqueId_1);//紀錄id才能把結果拉出來
                    popup_IDs.add(uniqueId_1);
                    all_popup_IDs.add(uniqueId_1);


                    TextView tvV=new TextView(getActivity());
                    tvV.setText("V");
                    tvV.setGravity(Gravity.CENTER);
                    tvV.setTextSize(28);
                    tvV.setLayoutParams(expand_item_par);

                    EditText etOhm=new EditText(getActivity());
                    etOhm.setGravity(Gravity.CENTER);
                    etOhm.setTextSize(28);
                    etOhm.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
                    etOhm.setBackgroundResource(R.drawable.type__4th_edittexd_background);
                    etOhm.setLayoutParams(expand_item_et_par);
                    int uniqueId_2 = View.generateViewId();
                    etOhm.setId(uniqueId_2);//紀錄id才能把結果拉出來
                    popup_IDs.add(uniqueId_2);
                    all_popup_IDs.add(uniqueId_2);


                    TextView tvOhm=new TextView(getActivity());
                    tvOhm.setText("mΩ");
                    tvOhm.setGravity(Gravity.CENTER);
                    tvOhm.setTextSize(28);
                    tvOhm.setLayoutParams(expand_item_par);

                    horizontal_box.addView(tvNum);
                    horizontal_box.addView(etVoltage);
                    horizontal_box.addView(tvV);
                    horizontal_box.addView(etOhm);
                    horizontal_box.addView(tvOhm);
                    vlFourthFather.addView(horizontal_box);
                }

                String Expand_title=expand_item_set.get(0);//"電壓≧2.00Vdc、300型內阻＜0.630mΩ"
                btExpandItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder type_4th_builder=new AlertDialog.Builder(getActivity());
                        type_4th_builder.setTitle(Expand_title);
                        type_4th_builder.setView(type_4th_layout[0]);
                        type_4th_builder.setCancelable(false);//無法透過手機返回鍵取消

                        /**歷史記錄回填**/
                        //依照當前按鈕取出相並回填對應的popup歷史資料
                            //需讓歷史資料只回填第一次，不然每次點擊按鈕都會重複覆蓋新的資料(確認當前按鈕id是否出現在儲存被按過按鈕id的array中)
                        int has_been_clicked=0;//維持0表示這是第一次被按下
                        for (int btn_has_clicked:popup_btn_clicked){
                            if (btn_has_clicked==btExpandItem.getId()){
                                has_been_clicked+=1;
                            }
                        }
                        if (edit_sign && has_been_clicked==0){
                            //找出被點擊按鈕當前的位置
                            int current_item=0;
                            for (int j=0;j<id_arr.size();j++){
                                if(btExpandItem.getId()==id_arr.get(j)){
                                    current_item=j;
                                }
                            }
                            //回填資料
                            if (history_arr.get(current_item).length()==4){
                                //完全沒有編輯過(db欄位只有null)(按下確認即算編輯)
                                for (int x=0;x<popup_IDs.size();x++){
                                    EditText et=type_4th_layout[0].findViewById(popup_IDs.get(x));
                                    et.setText("");
                                }
                            }
                            else {
                                //編輯過(db欄位為字串)(按下確認即算編輯)
                                String[] popup_result=history_arr.get(current_item).split("_");//popup_result為單個popup的結果
                                for (int x=0;x<popup_IDs.size();x++){
                                    EditText et=type_4th_layout[0].findViewById(popup_IDs.get(x));
                                    if (popup_result[x].equals("null")){
                                        et.setText("");
                                    }
                                    else{
                                        et.setText(popup_result[x]);
                                    }
                                }
                            }
                        }
                        popup_btn_clicked.add(btExpandItem.getId());//把被按過的按鈕記錄下來
                        type_4th_builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                //一個容器同時只能裝一個view,
                                //dialogBuilder這個容器.setView(dialogItem)裝了這個view之後,沒有remove的動作,
                                //所以當再次顯示,又要再setView時,裡面已經有先前的那個view,就會出錯,
                                //因此setOnDismissListener裡面呼叫removeView()就是用來當dialog結束後,將內容的view給remove掉
                                ViewGroup vs=(ViewGroup) type_4th_layout[0].getParent();
                                vs.removeView(type_4th_layout[0]);
                                //再次點擊,再次裝的view,是原先的view,因此若原先的view有編輯,
                                //相關的資訊會被記錄下來,再次點擊,再次裝的view,就會是前次有輸入過的內容
                            }
                        });
                        type_4th_builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ArrayList<String> popup_result_arr=new ArrayList<>();//儲存結果的array
                                StringBuilder popup_result = new StringBuilder();//儲存結果的字串
                                for (int z=0;z<popup_IDs.size();z++){
                                    EditText et=((EditText) type_4th_layout[0].findViewById(popup_IDs.get(z)));
                                    if (String.valueOf(et.getText()).equals("")){
                                        popup_result_arr.add(null);
                                        popup_result.append((String) null);
                                        popup_result.append("_");
                                    }
                                    else {
                                        popup_result_arr.add(String.valueOf(et.getText()));
                                        popup_result.append((String) String.valueOf(et.getText()));
                                        popup_result.append("_");
                                    }
                                }
                                popup_result= new StringBuilder(popup_result.substring(0, popup_result.length() - 1));//字尾底線去除

                                //寫入db
                                int current_set=0,current_item=0;
                                for (int j=0;j<element_IDs.size();j++){
                                    for (int k=0;k<element_IDs.get(j).size();k++){
                                        if(btExpandItem.getId()==element_IDs.get(j).get(k)){
                                            //找出被點擊按鈕當前的位置
                                            current_set=j;
                                            current_item=k;
                                        }
                                    }
                                }
                                db_result.get(current_set).set(current_item, String.valueOf(popup_result));
                                //標頭合格異常判斷及db寫入
                                int count=0;//count=popup_result_arr.size():合格、count=0:異常、count<popup_result_arr.size():null
                                for(int p=0;p<popup_result_arr.size();p++){
                                    if (String.valueOf(popup_result_arr.get(p)).equals("null")){
                                        count+=0;
                                    }
                                    else if(String.valueOf(popup_result_arr.get(p)).equals("0")){
                                        count=-1;
                                        break;
                                    }
                                    else {
                                        count+=1;
                                    }
                                }
                                RadioGroup title_rq=((RadioGroup) myView.findViewById(element_IDs.get(current_set).get(0)));
                                if (count==popup_result_arr.size()){
                                    RadioButton rb=(RadioButton) title_rq.getChildAt(0);
                                    title_rq.check(rb.getId());
                                    db_result.get(current_set).set(0, "合格");
                                }
                                else if (count==-1){
                                    RadioButton rb=(RadioButton) title_rq.getChildAt(1);
                                    title_rq.check(rb.getId());
                                    db_result.get(current_set).set(0, "異常");
                                }
                                else {
                                    title_rq.clearCheck();
                                    db_result.get(current_set).set(0, null);
                                }
                                dialogInterface.dismiss();
                            }
                        });
                        AlertDialog alert =type_4th_builder.create();
                        alert.setCanceledOnTouchOutside(false);//無法點擊外面取消
                        alert.show();
                    }
                });
                vlContent.addView(btExpandItem);
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
        compDBHper.set("checked_result",PMID,null,result_dim1,taskcard_attach_item_pkey_1);
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
    private Boolean RangeCheck(String[] ff){
        if (ff[4]==null ) return false;
        if (ff[0]!=null && Float.parseFloat(ff[4])<Float.parseFloat(ff[0])) return false;
        if (ff[1]!=null && Float.parseFloat(ff[4])<=Float.parseFloat(ff[1])) return false;
        if (ff[2]!=null && Float.parseFloat(ff[4])>Float.parseFloat(ff[2])) return false;
        if (ff[3]!=null && Float.parseFloat(ff[4])>=Float.parseFloat(ff[3])) return false;
        if (ff[0]==null && ff[1]==null && ff[2]==null && ff[3]==null) return false;
        return true;
    }

}
