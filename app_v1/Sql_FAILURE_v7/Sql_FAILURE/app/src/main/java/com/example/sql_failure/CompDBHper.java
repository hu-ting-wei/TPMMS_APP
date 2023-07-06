package com.example.sql_failure;

import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.sql_failure.check_fragment.HomeFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class CompDBHper extends SQLiteOpenHelper {

    private static String DB_PATH = "/data/data/" + "com.example.sql_failure" + "/databases/";
    private static String DB_NAME = "myDB1.db";
    String dbpath = DB_PATH + DB_NAME;

    InputStream input;

    public CompDBHper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version,InputStream input) {
        super(context, name, factory, version);

        this.input=input;
    }
    /***/
    public boolean createDatabase() {
        boolean dbExist1 = checkDatabase();
        this.getReadableDatabase();
        if (dbExist1 == false) {
            if (copyDatabase() == false) {
                return false;
            }
        }
        return true;
    }

    private boolean checkDatabase() {
        SQLiteDatabase checkDB = null;
        try {
            checkDB = SQLiteDatabase.openDatabase(dbpath,
                    null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            return false;
        }
        if (checkDB != null) {
            checkDB.close();
            return true;
        }
        return false;
    }

    private boolean copyDatabase() {
        try {
            this.getReadableDatabase();
            String outFileName = dbpath;
            OutputStream output =
                    new FileOutputStream(outFileName);
            byte [] buffer = new byte[1024];
            int length;
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            output.flush();
            output.close();
            input.close();
        } catch (Exception e) {
            Log.d("error", e.toString());
            return false;
        }
        return true;
    }
    /***/
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d("msg","oon");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }
    //查詢
    public ArrayList<String> get(String sql){
        SQLiteDatabase db;
        db=SQLiteDatabase.openOrCreateDatabase(dbpath,null,null);

        Cursor recSet=db.rawQuery(sql,null);
        ArrayList<String> recAry=new ArrayList<String>();
        int columnCount=recSet.getColumnCount();
        while(recSet.moveToNext()){
            String fIdSet="";
            //每一個欄位以"#"做區隔，一列(第一組)做完才換下一列(第二組)
            for(int i=0;i<columnCount;i++){
                fIdSet+=recSet.getString(i)+"#";
            }
            //一列做完就放入array
            recAry.add(fIdSet);
        }
        Log.d("content", String.valueOf(recAry));
        Log.d("count", String.valueOf(recAry.size()));
        recSet.close();
        db.close();
        return recAry;
    }
    //寫入
    public void set(String TBname,String PMID,ArrayList<String> result,ArrayList<String> attachment){
        SQLiteDatabase db=getWritableDatabase();
        String sql_command = "";
        switch (TBname){
            case "checked_result":
                String tmp1="INSERT INTO checked_result VALUES ";
                for(int i=0;i< result.size();i++){
                    if(i== result.size()-1){
                        tmp1+="(" + PMID + ",'" + null + "','" + attachment.get(i) + "','" + null + "','" + result.get(i) + "')";
                        break;
                    }
                    tmp1+="(" + PMID + ",'" + null + "','" + attachment.get(i) + "','" + null + "','" + result.get(i) + "'),";
                }
                sql_command=tmp1;
                break;
            case "check_condition":
                String tmp2="INSERT INTO check_condition VALUES ";
                tmp2+="(" + result.get(0) +
                        ",'" + result.get(1) +
                        "','" + result.get(2) +
                        "','" + result.get(3) +
                        "','" + result.get(4) +
                        "','" + result.get(5) +
                        "','" + result.get(6) +
                        "','" + result.get(7) +
                        "','" + result.get(8) +
                        "','" + result.get(9) +
                        "','" + result.get(10) +
                        "','" + result.get(11) +
                        "','" + result.get(12) +
                        "')";
                sql_command=tmp2;
                break;
        }
        db.execSQL(sql_command);
        db.close();
    }
    //刪除、更新
    public void delete_update(String sql){
        SQLiteDatabase db=getWritableDatabase();
        db.execSQL(sql);
        db.close();
    }
}
