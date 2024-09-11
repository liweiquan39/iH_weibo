package com.example.weibo_liweiquan;

import com.chad.library.adapter.base.entity.MultiItemEntity;

public class FlowItem implements MultiItemEntity {
    public static final int TYPE_IMAGE = 1;
    private final int type;
    FlowItem(int type){this.type=type;}
    public int getItemType(){return type;}
}
