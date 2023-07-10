package com.example.sql_failure;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class SpinnerColorAdapter  extends BaseAdapter {
    private LayoutInflater inflator;
    private ArrayList<String> mList;
    public SpinnerColorAdapter(Context context, ArrayList<String> _InputList){
        inflator = LayoutInflater.from(context);
        mList = _InputList;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    //getView()在創建時有被重複呼叫的問題，導致運行不正常，使用getTag & viewHolder解決此問題
        //setTag、getTag:在第一次創建itemView的时候，完成對控件的绑定，同时把控件作為一個object--holder，通過setTag()存到itemView中，再第二次使用的时候就可以通過getTag()把holder取出来直接使用，也就是说，在list中itemView相同的情况下，只進行了一次的控件資源绑定
        //連結：https://www.jianshu.com/p/61b732f59e3d
        //使用 ViewHolder 模式，可以在 getView 方法中重用畫面，而不是每次都創建新的畫面
        //連結:https://juejin.cn/s/android%20listview%20adapter%20getview%20called%20multiple%20times
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view==null){
            holder=new ViewHolder();
            view=inflator.inflate(R.layout.customized_spinner_item,null,false);
            holder.tvItem=((TextView) view.findViewById(R.id.tvItemColor));
            view.setTag(holder);
        }
        else{
            holder = (ViewHolder) view.getTag();
        }

        holder.tvItem.setText(mList.get(i));
        //顏色相間效果
        if ((i+2)%2==0){
            holder.tvItem.setBackgroundResource(R.color.white);
        }
        else {
            holder.tvItem.setBackgroundResource(R.color.spinner_dark);
        }
        return view;
    }

    static class ViewHolder {
        TextView tvItem;
    }
}
