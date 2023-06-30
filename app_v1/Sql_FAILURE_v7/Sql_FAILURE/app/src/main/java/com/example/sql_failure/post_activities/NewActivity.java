package com.example.sql_failure.post_activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.sql_failure.R;

public class NewActivity extends AppCompatActivity {
    private Toolbar newToolbar;
    private RadioGroup rgNewType;
    private Button btNewNext,btBackHome;
    private Spinner spnNewBase;
    private TextView tvNewTxt;
    private int basePos;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        basePos= Integer.parseInt(getIntent().getExtras().getString("basePos"));

        build_views();
    }
    private void build_views(){
        newToolbar=((Toolbar) findViewById(R.id.newToolbar));
        setSupportActionBar(newToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);

        spnNewBase=((Spinner) findViewById(R.id.spnIdNewBase));
        ArrayAdapter adBase=new ArrayAdapter(NewActivity.this, R.layout.spinner_style_white,new String[]{"六家基地","台北基地","烏日基地","雲林基地","左營基地"});
        adBase.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spnNewBase.setAdapter(adBase);
        spnNewBase.setSelection(basePos);
        spnNewBase.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                basePos=i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        tvNewTxt=((TextView) findViewById(R.id.tvIdNewTxt));

        rgNewType=((RadioGroup) findViewById(R.id.rgIdNewType));
        //完成點擊
        rgNewType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                btNewNext.setBackgroundResource(R.color.theme_teal);
                btNewNext.setTextColor(Color.parseColor("#ffffff"));
                btNewNext.setEnabled(true);
            }
        });

        btBackHome=((Button) findViewById(R.id.btIdNewBackHome));
        btBackHome.setOnClickListener(BackHomeListener);

        btNewNext=((Button) findViewById(R.id.btIdNewNext));
        btNewNext.setBackgroundResource(R.color.gray);
        btNewNext.setTextColor(Color.parseColor("#7f7f7f"));
        btNewNext.setEnabled(false);
        btNewNext.setOnClickListener(NewNextListener);
    }
    private View.OnClickListener BackHomeListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent=new Intent(NewActivity.this, MainActivity.class);
            startActivity(intent);
        }
    };
    private View.OnClickListener NewNextListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int rbTypeID=rgNewType.getCheckedRadioButtonId();
            RadioButton rb=((RadioButton) findViewById(rbTypeID));
            String post_type= String.valueOf(rb.getText());
            if(post_type.equals("WAY")){
                post_type="WAYSIDE";
            }
            Intent intent=new Intent(NewActivity.this,TaskcardSelectActivity.class);
            Bundle bundle=new Bundle();
            bundle.putString("basePos", String.valueOf(basePos));
            bundle.putString("post_type",post_type);
            bundle.putString("postMod",getIntent().getExtras().getString("postMod"));
            intent.putExtras(bundle);
            startActivity(intent);
        }
    };
}