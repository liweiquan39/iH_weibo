package com.example.weibo_liweiquan;

import android.annotation.SuppressLint;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnLoadMoreListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;


public class homeFragment extends Fragment implements NetworkChangeReceiver.NetworkChangeListener {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private NetworkChangeReceiver networkChangeReceiver;
    private String mParam1;
    private String mParam2;
    private static final String TAG = "homeFragmentMine";
    private SharedPreferences sharedPreferences;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FlowAdapter mAdapter;
    private final ScheduledExecutorService mExecutor = Executors.newSingleThreadScheduledExecutor();
    private SwipeRefreshLayout mRefresh;
    private Handler handler = new Handler();
    private ImageView imageLoad, imageFail;
    public Boolean firstOpen;
    public static Boolean networkStatus=true;
    private RecyclerView recyclerView;
    private ConstraintLayout request_layout;
    private Button request_button;
    private TextView nomessage;

    public homeFragment(Boolean first_open, Boolean network) {
        // Required empty public constructor
        firstOpen = first_open;
        networkStatus = network;
    }
    public homeFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment homeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static homeFragment newInstance(String param1, String param2, Boolean networkStatus) {
        homeFragment fragment = new homeFragment(false, networkStatus);
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
    @SuppressLint("WrongViewCast")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nomessage = view.findViewById(R.id.noNetworkMessage);
        imageLoad = view.findViewById(R.id.loading_image);
        request_layout = view.findViewById(R.id.request_layout);
        request_button = view.findViewById(R.id.request_button);
        imageFail = view.findViewById(R.id.fail_image);
        networkChangeReceiver = new NetworkChangeReceiver(this);
        swipeRefreshLayout = view.findViewById(R.id.refresh_layout);


        recyclerView = view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, RecyclerView.VERTICAL));
        recyclerView.setItemAnimator(null);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration(){
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state){
                outRect.set(15,30,15,0);
            }
        });

        sharedPreferences = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token","");// 若有token则传入请求头

        List<FlowItem> lists = FlowDataSource.loadItems(getContext(), token);
        requireActivity().runOnUiThread(()->{
            FlowAdapter adapter=new FlowAdapter(lists, getContext());
            recyclerView.setAdapter(adapter);
            mAdapter=adapter;
            mAdapter.setAnimationWithDefault(BaseQuickAdapter.AnimationType.SlideInRight);
        });
        if(firstOpen) {
            imageLoad.setVisibility(View.VISIBLE);
            request_layout.setVisibility(View.GONE);
            imageFail.setVisibility(View.GONE);
            request_button.setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.GONE);
            nomessage.setVisibility(View.GONE);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    imageLoad.setVisibility(View.GONE);
                    swipeRefreshLayout.setVisibility(View.VISIBLE);
                }
            }, 800);
        }
        if (!networkStatus){
            SharedPreferences sharedP = requireActivity().getSharedPreferences("dataCache", Context.MODE_PRIVATE);
            String json = sharedP.getString("dataList", null);
            if (json != null) {
                setCacheData(view, json);
            } else { // 缓存为空
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(!networkStatus) {
                            imageFail.setVisibility(View.VISIBLE);
                            request_button.setVisibility(View.VISIBLE);
                            request_layout.setVisibility(View.VISIBLE);
                            swipeRefreshLayout.setVisibility(View.GONE);
                            nomessage.setVisibility(View.GONE);
                            imageLoad.setVisibility(View.GONE);
                        }
                        else{
                            netWorkSuccess(view);
                        }
                    }
                }, 500);
                request_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getContext(),"正在重新连接", Toast.LENGTH_SHORT).show();
                        if(!networkStatus){
                            request_layout.setVisibility(View.GONE);
                            imageFail.setVisibility(View.GONE);
                            request_button.setVisibility(View.GONE);
                            imageLoad.setVisibility(View.VISIBLE);
                            swipeRefreshLayout.setVisibility(View.GONE);
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (!networkStatus){
                                        netWorkFail();
                                    }
                                    else {
                                        netWorkSuccess(view);
                                    }
                                }
                            }, 400);
                        }
                        else {
                            netWorkButtonSuccess(view);
                        }
                    }
                });
            }
        }

        initLoadMore();
        initRefresh(view);
    }
    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        requireActivity().registerReceiver(networkChangeReceiver, filter);
    }
    @Override
    public void onPause() {
        super.onPause();
        requireActivity().unregisterReceiver(networkChangeReceiver);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        requireActivity().unregisterReceiver(networkChangeReceiver);
    }


    @Override
    public void onNetworkConnected() {
        if(!networkStatus){
            networkStatus = true;
            request_layout.setVisibility(View.GONE);
            imageFail.setVisibility(View.GONE);
            request_button.setVisibility(View.GONE);
            Toast.makeText(getContext(), "网络已连接", Toast.LENGTH_SHORT).show();

            sharedPreferences = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
            String token = sharedPreferences.getString("token","");// 若有token则传入请求头
            mRefresh.setRefreshing(true);
            mExecutor.schedule(()->{
                List<FlowItem> list=FlowDataSource.refreshget(getContext(), token);
                requireActivity().runOnUiThread(()->{
                    mAdapter.setList(list);
                    mRefresh.setRefreshing(false);
                });
            }, 200, TimeUnit.MILLISECONDS);
            initLoadMore();

            swipeRefreshLayout.setVisibility(View.VISIBLE);
            request_layout.setVisibility(View.GONE);
            nomessage.setVisibility(View.GONE);
            imageFail.setVisibility(View.GONE);
            request_button.setVisibility(View.GONE);
        }

        networkStatus = true;
    }

    @Override
    public void onNetworkDisconnected() {
        networkStatus = false;
//        Toast.makeText(getContext(), "网络已断开", Toast.LENGTH_SHORT).show();
        nomessage.setVisibility(View.VISIBLE);
    }

    private void initRefresh(View view){
        sharedPreferences = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token","");// 若有token则传入请求头
        mRefresh = view.findViewById(R.id.refresh_layout);
        mRefresh.setColorSchemeColors(requireContext().getColor(R.color.my_color1),requireContext().getColor(R.color.my_color2),requireContext().getColor(R.color.my_color3));
        mRefresh.setOnRefreshListener(()->{
            mRefresh.setRefreshing(true);
            mExecutor.schedule(()->{
                List<FlowItem> list=FlowDataSource.refreshget(getContext(), token);
                requireActivity().runOnUiThread(()->{
                    mAdapter.setList(list);
                    mRefresh.setRefreshing(false);
                });
            }, 500, TimeUnit.MILLISECONDS);
        });

    }
    private void initLoadMore(){
        mAdapter.getLoadMoreModule().setPreLoadNumber(0);
        mAdapter.getLoadMoreModule().setOnLoadMoreListener(new OnLoadMoreListener(){
            public void onLoadMore(){
                mExecutor.schedule(()->{
                    List<FlowItem> list=FlowDataSource.loadMore();
                    if(list.isEmpty()){
                        requireActivity().runOnUiThread(()->{
                            mAdapter.getLoadMoreModule().loadMoreEnd(false);
                            Toast.makeText(getActivity(), "无更多内容！", Toast.LENGTH_SHORT).show();
                            Log.i(TAG,"onLoadMore end");
                        });
                        return;
                    }
                    requireActivity().runOnUiThread(()->{
                        mAdapter.addData(list);
                        mAdapter.getLoadMoreModule().loadMoreComplete();
                        Log.i(TAG,"onLoadMore loadMoreComplete");
                    });
                },0,TimeUnit.SECONDS);
            }
        });
    }
    private void setCacheData(View view, String json){
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<WeiboInfo>>() {}.getType();
        List<WeiboInfo> list_fail = gson.fromJson(json, type);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                imageLoad.setVisibility(View.GONE);
                swipeRefreshLayout.setVisibility(View.VISIBLE);
                mRefresh = view.findViewById(R.id.refresh_layout);
                mRefresh.setRefreshing(true);
                mExecutor.schedule(()->{
                    List<FlowItem> list=FlowDataSource.setFailData(list_fail);
                    requireActivity().runOnUiThread(()->{
                        mAdapter.setList(list);
                        mRefresh.setRefreshing(false);
                    });
                }, 0, TimeUnit.MILLISECONDS);
            }
        }, 500);
    }
    private void netWorkSuccess(View view){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(),"网络连接成功", Toast.LENGTH_SHORT).show();
                imageLoad.setVisibility(View.GONE);
                request_layout.setVisibility(View.GONE);
                imageFail.setVisibility(View.GONE);
                request_button.setVisibility(View.GONE);
                swipeRefreshLayout.setVisibility(View.VISIBLE);
                mRefresh = view.findViewById(R.id.refresh_layout);
                mRefresh.setRefreshing(true);
                mExecutor.schedule(()->{
                    List<FlowItem> list=FlowDataSource.refresh(getContext());
                    requireActivity().runOnUiThread(()->{
                        mAdapter.setList(list);
                        mRefresh.setRefreshing(false);
                    });
                }, 0, TimeUnit.MILLISECONDS);
            }
        }, 100);
    }
    private void netWorkFail(){
        Toast.makeText(getContext(),"网络连接失败", Toast.LENGTH_SHORT).show();
        imageLoad.setVisibility(View.GONE);
        request_layout.setVisibility(View.VISIBLE);
        imageFail.setVisibility(View.VISIBLE);
        request_button.setVisibility(View.VISIBLE);
    }
    private void netWorkButtonSuccess(View view){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(),"网络连接成功", Toast.LENGTH_SHORT).show();
                imageLoad.setVisibility(View.GONE);
                request_layout.setVisibility(View.GONE);
                imageFail.setVisibility(View.GONE);
                request_button.setVisibility(View.GONE);
                swipeRefreshLayout.setVisibility(View.VISIBLE);
                mRefresh = view.findViewById(R.id.refresh_layout);
                mRefresh.setRefreshing(true);
                mExecutor.schedule(()->{
                    List<FlowItem> list=FlowDataSource.refresh(getContext());
                    requireActivity().runOnUiThread(()->{
                        mAdapter.setList(list);
                        mRefresh.setRefreshing(false);
                    });
                }, 0, TimeUnit.MILLISECONDS);
            }
        }, 100);
    }

}