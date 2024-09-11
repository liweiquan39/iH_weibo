package com.example.weibo_liweiquan;

public class Item extends FlowItem{
    public WeiboInfo weiboInfo;
    public Item(WeiboInfo weibo){
        super(TYPE_IMAGE);
        weiboInfo = weibo;
    }

    public WeiboInfo getInfo() {
        return weiboInfo;
    }
}
