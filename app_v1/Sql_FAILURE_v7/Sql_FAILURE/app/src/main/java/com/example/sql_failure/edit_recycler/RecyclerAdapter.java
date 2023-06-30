package com.example.sql_failure.edit_recycler;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sql_failure.CompDBHper;
import com.example.sql_failure.R;
import com.example.sql_failure.post_activities.TaskcardStatusActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

//參考:https://www.kaelli.com/22.html Git:https://github.com/KaelLi1989/AndroidTreeRecyclerView
//參考:https://blog.csdn.net/chengxu_hou/article/details/70344759 Git:https://github.com/monkeyLittleMonkey/ExpandRecyclerViewDemo/tree/3181c89fe760b082668ddf0632288f39f31f8af0
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements TreeStateChangeListener{

    private Context mContext;
    private List<TreeItem> mList;//存取顯示列表
    private OnScrollListener mOnScrollListener;//滾動監聽

    public RecyclerAdapter(Context context,List<TreeItem> list){
        //RecyclerAdapter帶有參數的建購子
        this.mList=new LinkedList<>();
        this.mContext=context;
        this.mList.addAll(list);
    }

    class myParentViewHolder extends RecyclerView.ViewHolder{
        //連結內部所需要的控件(TextView 之類的)
        //ViewHolder就是我們儲存 View reference 的地方，可以把它當成一個儲存 View 的 class。
        private LinearLayout vlParent;      //當點擊時觸發onclick使icon轉動
        private View view;                  //findById使用
        private TextView tvTaskcardType,tvParentDate,tvParentLocation,tvParentTaskcard;
        private ImageView ivParentIcon;     //箭頭icon
        private View vParentDivider;        //白色分隔線
        //自動生成的建構子
        public myParentViewHolder(@NonNull View itemView) {
            super(itemView);
            this.view=itemView;
        }
        public void bindView(TreeItem treeItem){
            //準備的材料就是在主要頁面的onCreate內已經載入好的arrayList
            vlParent=((LinearLayout) view.findViewById(R.id.vlIdParent));
            tvTaskcardType=((TextView) view.findViewById(R.id.tvIdTaskcardType));
            tvParentDate=((TextView) view.findViewById(R.id.tvIdParentDate));
            tvParentLocation=((TextView) view.findViewById(R.id.tvIdParentLocation));
            tvParentTaskcard=((TextView) view.findViewById(R.id.tvIdParentTaskcard));
            ivParentIcon=((ImageView) view.findViewById(R.id.ivIdParentIcon));

            tvTaskcardType.setText(treeItem.getParentTaskcardType());
            tvParentDate.setText(treeItem.getParentDate());
            tvParentLocation.setText(treeItem.getParentLocation());
            tvParentTaskcard.setText(treeItem.getParentTaskcard());
        }
    }
    class myChildViewHolder extends RecyclerView.ViewHolder{
        //ViewHolder就是我們儲存 View reference 的地方，大家也可以把它當成一個儲存 View 的 class。
        //簡單來說就是內部連結所需要的控件(TextView 之類的)
        private View view;
        private TextView tvChildAttach,tvChildPerson;
        private Button btChildDelete,btChildEdit;
        //資料庫
        private static final String DBname="myDB1.db";
        private static final int DBversion=1;
        CompDBHper compDBHper;
        //自動生成的建構子
        public myChildViewHolder(@NonNull View itemView) {
            super(itemView);
            this.view=itemView;
        }
        public void bindView(TreeItem treeItem,myChildViewHolder holder){
            //準備的材料就是在主要頁面的onCreate內已經載入好的arrayList
            tvChildAttach=((TextView) view.findViewById(R.id.tvIdChildAttach));
            tvChildPerson=((TextView) view.findViewById(R.id.tvIdChildPerson));
            btChildDelete=((Button) view.findViewById(R.id.btIdChildDelete));
            btChildEdit=((Button) view.findViewById(R.id.btIdChildEdit));

            tvChildAttach.setText(treeItem.getChildAttach());
            tvChildPerson.setText(treeItem.getChildPerson());

            String PMID=treeItem.getPMID();
            //資料庫
            try {
                InputStream am=mContext.getAssets().open(DBname);
                compDBHper=new CompDBHper(mContext,DBname,null,DBversion,am);
            } catch (IOException e) {
                throw new Error(e.toString());
            }
            try {
                compDBHper.createDatabase();
            } catch (Exception e) {
                throw new Error("Database not created....");
            }

            //刪除按鈕監聽
            btChildDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position=holder.getAdapterPosition();//點下刪除按鈕後才取得位置
                    mList.remove(position);     //刪除本身
                    notifyItemRemoved(position);
                    mList.remove(position-1);//刪除parent
                    notifyItemRemoved(position-1);

                    String SQL_command_1="DELETE FROM check_condition WHERE PMID='" + PMID + "'" ;
                    compDBHper.delete_update(SQL_command_1);
                    String SQL_command_2="DELETE FROM checked_result WHERE PMID='" + PMID + "'" ;
                    compDBHper.delete_update(SQL_command_2);
                }
            });
            //編輯按鈕監聽
            btChildEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(mContext, TaskcardStatusActivity.class);
                    Bundle bundle=new Bundle();
                    bundle.putString("postMod","edit");
                    bundle.putString("PMID",PMID);
                    bundle.putString("taskcard_name",treeItem.getParentTaskcard());
                    intent.putExtras(bundle);
                    mContext.startActivity(intent);
                }
            });
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //連接layout檔案，return一個View
        //onCreateViewHolder只有在首次建置時會跑一次
        View view;
        switch(viewType){
            case TreeItem.PARENT_ITEM:
            default:
                Log.d("TAG", "PARENT_ITEM");
                view= LayoutInflater.from(mContext).inflate(R.layout.edit_list_parent,parent,false);
                return new myParentViewHolder(view);
            case TreeItem.CHILD_ITEM:
                Log.d("TAG", "CHILD_ITEM");
                view= LayoutInflater.from(mContext).inflate(R.layout.edit_list_child,parent,false);
                return new myChildViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //在這裡取得元件的控制(每個item內的控制)，依據 position 把正確的資料跟 ViewHolder 綁定在一起。
        //holder的用意就是連結 myChildViewHolder/myParentViewHolder 宣告過的元件，但我們有兩種ViewHolder(myParentViewHolder、myChildViewHolder)，所以要分開寫
        TreeItem treeItem=mList.get(position);//每個位置對應的項目
        switch (getItemViewType(position)){
            case TreeItem.PARENT_ITEM:
                myParentViewHolder mParentViewHolder=(myParentViewHolder) holder;//將此方法內的holder強制轉型為我們自己的viewHolder
                mParentViewHolder.bindView(mList.get(position));                 //開始設定控件，需要mList當前位置的值
                //onClick監聽(點擊以展開子元素)
                mParentViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (treeItem.getType()==0 && !treeItem.getExpand()){
                            Log.d("TAG", "I should open");
                            ((myParentViewHolder) holder).ivParentIcon.setRotation(90); //icon旋轉
                            onOpen(treeItem,mParentViewHolder.getAdapterPosition());    //type為0代表要打開
                        }
                        else {
                            Log.d("TAG", "I should close");
                            ((myParentViewHolder) holder).ivParentIcon.setRotation(0);  //icon旋轉
                            onClose(treeItem,mParentViewHolder.getAdapterPosition());   //type為1代表要關閉
                        }
                    }
                });
                Log.d("TAG", "bindParent");
                break;
            case TreeItem.CHILD_ITEM:
                myChildViewHolder mChildViewHolder=(myChildViewHolder) holder;//將此方法內的holder強制轉型為我們自己的viewHolder
                mChildViewHolder.bindView(mList.get(position),mChildViewHolder);//開始設定控件，需要mList當前位置的值及viewHolder(child中有按鈕需要取得position)
                Log.d("TAG", "bindChild");
                break;
        }
    }

    @Override
    public int getItemCount() {
        // 回傳整個 Adapter 包含幾筆資料。
        return mList.size();
    }
    @Override
    public int getItemViewType(int position){
        // 回傳顯示類型(PARENT_ITEM:0 CHILD_ITEM:1)
        return mList.get(position).getType();
    }

    //實作介面(點擊parent項目，完成展開及收合的方法)
    @Override
    public void onOpen(TreeItem treeItem, int position) {
        TreeItem children =getChildrenData(treeItem);//拿取要展示的子layout資料
        if (children==null){
            return;
        }
        treeItem.setExpand(true);//讓程式知道現在已經是展開狀態了
        mList.add(position+1,children);//在當前item下方插入
        notifyItemInserted(position+1);

        if (position<=mList.size() && mOnScrollListener!=null){
            mOnScrollListener.scrollTo(position+1);//向下滾動讓child能被看見
        }
    }

    @Override
    public void onClose(TreeItem treeItem, int position) {
        if (treeItem.getChildData()==null){
            return;
        }
        treeItem.setExpand(false);//讓程式知道現在已經是收合狀態了
        mList.remove(position+1);
        notifyItemRemoved(position+1);

        if (mOnScrollListener!=null){
            mOnScrollListener.scrollTo(position);//回原位
        }
    }

    //當展開時，拿取子layout資料(複製一份並插入到父元素的位置後一格)
    public TreeItem getChildrenData(TreeItem treeItem){
        TreeItem child=new TreeItem();
        child.setType(1);
        //notifyItemInserted後，程式會針對新增的位置再跑一遍預設的那些函數(@Override那些，**onCreateViewHolder:只有在首次建置時會跑一次)，此時child.setType(1);就會發生作用，轉往CHILD_ITEM的layout建置
        child.setParentTaskcardType(treeItem.getParentTaskcardType());
        child.setParentDate(treeItem.getParentDate());
        child.setParentLocation(treeItem.getParentLocation());
        child.setParentTaskcard(treeItem.getParentTaskcard());

        child.setChildAttach(treeItem.getChildAttach());
        child.setChildPerson(treeItem.getChildPerson());
        child.setPMID(treeItem.getPMID());//編輯、刪除鍵在子項目中，所以PMID也要複製一份
        return child;
    }

    //自製滾動監聽
    public void mSetOnScrollListener(OnScrollListener onScrollListener){
        this.mOnScrollListener=onScrollListener;
    }
    public interface OnScrollListener{
        void scrollTo(int pos);
    }
}
