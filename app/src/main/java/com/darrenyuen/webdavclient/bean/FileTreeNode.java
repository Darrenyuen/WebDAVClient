package com.darrenyuen.webdavclient.bean;

import java.util.List;

/**
 * Create by yuan on 2021/3/2
 */
public class FileTreeNode {

    public FileTreeNode mParent;
    public FileBean mValue;
    public List<FileTreeNode> mChildren;

    public FileTreeNode() {
        super();
    }

    public FileTreeNode(FileBean value) {
        super();
        mValue = value;
    }

    public FileTreeNode(FileBean value, List<FileTreeNode> children) {
        super();
        mValue = value;
        mChildren = children;
    }


}
