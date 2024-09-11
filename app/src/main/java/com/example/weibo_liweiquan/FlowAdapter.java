package com.example.weibo_liweiquan;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FlowAdapter extends BaseMultiItemQuickAdapter<FlowItem, BaseViewHolder> implements LoadMoreModule {

    private final Context context;
    private static final String TAG = "FlowAdapter";

    public FlowAdapter(@NonNull List<FlowItem> data, Context context) {
        super(data);
        this.context = context;
        addItemType(FlowItem.TYPE_IMAGE,R.layout.item);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, FlowItem flowItem) {
        Item item = (Item) flowItem;
        WeiboInfo weiboInfo = item.getInfo();
        VideoViewHolder videoViewHolder = new VideoViewHolder(baseViewHolder.itemView, context, weiboInfo);


        ImageButton btn_delete = baseViewHolder.itemView.findViewById(R.id.btn_delete);
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("deleteitemadapter", String.valueOf(baseViewHolder.getAdapterPosition()));
                int position = baseViewHolder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    onItemDelete(position);
                }
            }
        });


        ImageButton btn_comment = baseViewHolder.itemView.findViewById(R.id.btn_comment);
        TextView comment_text = baseViewHolder.itemView.findViewById(R.id.comment_text);


        // 评论
        btn_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"点击第" + String.valueOf(baseViewHolder.getAdapterPosition()+1) + "条数据评论按钮", Toast.LENGTH_SHORT).show();
            }
        });
        comment_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"点击第" + String.valueOf(baseViewHolder.getAdapterPosition()+1) + "条数据评论按钮", Toast.LENGTH_SHORT).show();
            }
        });


    }

    public void onItemDelete(int position) {
        getData().remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }


}
