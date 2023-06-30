package com.example.sql_failure.edit_recycler;

//展開及收合需實做的項目
public interface TreeStateChangeListener {
    void onOpen(TreeItem treeItem,int position);
    void onClose(TreeItem treeItem,int position);
}
