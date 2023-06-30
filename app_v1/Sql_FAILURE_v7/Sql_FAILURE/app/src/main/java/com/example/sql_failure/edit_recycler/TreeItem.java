package com.example.sql_failure.edit_recycler;

//具有展開效果的項目所擁有的類別
public class TreeItem {
    public static final int PARENT_ITEM=0;
    public static final int CHILD_ITEM=1;
    public int type;        //顯示類型(parent,child)程式會依照type來建置相對應的layout
    public TreeItem childData;//用來確保關閉子布局時是有資料的，若沒有則不用關閉
    public boolean isExpand;   //元素狀態(打開時設為true，收合時設為false)
    //parent區字串
    public String parentTaskcardType;
    public String parentDate;
    public String parentLocation;
    public String parentTaskcard;
    //child區字串
    public String childAttach;
    public String childPerson;
    //不顯示區
    public String PMID;

    /**實作 parent元素 方法**/
    //設置
    public void setParentTaskcardType(String parentTaskcardType){this.parentTaskcardType=parentTaskcardType;}
    public void setParentDate(String parentDate){this.parentDate=parentDate;}
    public void setParentLocation(String parentLocation){this.parentLocation=parentLocation;}
    public void setParentTaskcard(String parentTaskcard){this.parentTaskcard=parentTaskcard;}
    //拿取
    public String getParentTaskcardType(){return parentTaskcardType;}
    public String getParentDate(){return parentDate;}
    public String getParentLocation(){return parentLocation;}
    public String getParentTaskcard(){return parentTaskcard;}
    /**實作 child元素 方法**/
    //設置
    public void setChildAttach(String childAttach){this.childAttach=childAttach;}
    public void setChildPerson(String childPerson){this.childPerson=childPerson;}
    //拿取
    public String getChildAttach(){return childAttach;}
    public String getChildPerson(){return childPerson;}
    /**實作 PMID 方法**/
    public void setPMID(String PMID){this.PMID=PMID;}
    public String getPMID(){return PMID;}
    /**實作 childData 方法**/
    //設置
    public void setChildDta(TreeItem childData){this.childData=childData;}
    //拿取
    public TreeItem getChildData(){return childData;}
    /**實作 type 方法**/
    //設置
    public void setType(int type){this.type=type;}
    //拿取
    public int getType(){return type;}
    /**實作 isExpand 方法**/
    //設置
    public void setExpand(boolean expand){this.isExpand=expand;}
    //拿取
    public boolean getExpand(){return isExpand;}
}
