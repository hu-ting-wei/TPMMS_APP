package com.example.sql_failure.post_activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.appcompat.widget.Toolbar;

import com.example.sql_failure.R;

public class PostActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private Spinner spnBase;
    private int basePos;
    private RadioGroup rgPostType;
    private RadioButton rbNew,rbEdit;
    private Button btNext,btBackHome;
    private String postMod;//紀錄使用者選擇是新增還是編輯
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        basePos= Integer.parseInt(getIntent().getExtras().getString("basePos"));

        build_views();
    }
    private void build_views(){
        toolbar=((Toolbar) findViewById(R.id.postToolbar));
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);

        spnBase=((Spinner) findViewById(R.id.spnIdPostBase));
        ArrayAdapter adBase=new ArrayAdapter(PostActivity.this, R.layout.spinner_style_white,new String[]{"六家基地","台北基地","烏日基地","雲林基地","左營基地"});
        adBase.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spnBase.setAdapter(adBase);
        spnBase.setSelection(basePos);
        spnBase.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                basePos=i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        rgPostType=((RadioGroup) findViewById(R.id.rgIdPostType));
        //完成點擊
        rgPostType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i==rbNew.getId()){
                    postMod="new";
                }
                else{
                    postMod="edit";
                }
                btNext.setBackgroundResource(R.color.theme_teal);
                btNext.setTextColor(Color.parseColor("#ffffff"));
                btNext.setEnabled(true);
            }
        });
        rbNew=((RadioButton) findViewById(R.id.rbIdNew));
        SpannableString styled=new SpannableString("新增\n檢查表");
        styled.setSpan(new TextAppearanceSpan(this,R.style.text_big),0,2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(new TextAppearanceSpan(this,R.style.text_small),2,6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        rbNew.setText(styled);
        rbEdit=((RadioButton) findViewById(R.id.rbIdEdit));
        styled=new SpannableString("編輯\n檢查表");
        styled.setSpan(new TextAppearanceSpan(this,R.style.text_big),0,2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(new TextAppearanceSpan(this,R.style.text_small),2,6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        rbEdit.setText(styled);

        btBackHome=((Button) findViewById(R.id.btIdPostBackHome));
        btBackHome.setOnClickListener(BackHomeListener);

        btNext=((Button) findViewById(R.id.btIdPostNext));
        btNext.setBackgroundResource(R.color.gray);
        btNext.setTextColor(Color.parseColor("#7f7f7f"));
        btNext.setEnabled(false);
        btNext.setOnClickListener(NextListener);
    }
    private View.OnClickListener BackHomeListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent=new Intent(PostActivity.this, MainActivity.class);
            startActivity(intent);
        }
    };
    private View.OnClickListener NextListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent;
            if(postMod.equals("new")){
                intent = new Intent(PostActivity.this, NewActivity.class);
            }
            else {
                intent = new Intent(PostActivity.this, EditActivity.class);
            }
            Bundle bundle=new Bundle();
            bundle.putString("basePos", String.valueOf(basePos));
            bundle.putString("postMod",postMod);
            intent.putExtras(bundle);
            startActivity(intent);

        }
    };
}