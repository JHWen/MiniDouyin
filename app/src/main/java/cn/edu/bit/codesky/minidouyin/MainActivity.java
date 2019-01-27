package cn.edu.bit.codesky.minidouyin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import cn.edu.bit.codesky.minidouyin.adapter.VideoListAdapter;
import cn.edu.bit.codesky.minidouyin.beans.Feed;
import cn.edu.bit.codesky.minidouyin.beans.FeedResponse;
import cn.edu.bit.codesky.minidouyin.ui.VideoDetailActivity;
import cn.edu.bit.codesky.minidouyin.util.IMiniDouyinService;
import cn.edu.bit.codesky.minidouyin.util.RetrofitManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * @author codesky
 * @date 2019/1/27  10:55
 * @description 首页activity，显示视频列表信息流
 */
public class MainActivity extends AppCompatActivity implements VideoListAdapter.ListItemOnClickListener {

    private static final String TAG = MainActivity.class.getName();
    private static final String URL_KEY = "video_url";
    private static final String TITLE_KEY = "video_title";
    private static final String URL_BUNDLE = "url_bundle";

    private RecyclerView recyclerView;
    private VideoListAdapter videoListAdapter;

    //Restful API host
    private static final String HOST = "http://10.108.10.39:8080/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn1 = findViewById(R.id.btn_detail);
        btn1.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, VideoDetailActivity.class)));
        Button btn2 = findViewById(R.id.btn_refresh);
        btn2.setOnClickListener(v -> refresh());

        recyclerView = findViewById(R.id.rv_video_list);

        //set LayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        //set Adapter
        videoListAdapter = new VideoListAdapter(this);
        recyclerView.setAdapter(videoListAdapter);

        //refresh feed
        refresh();
    }

    /**
     * 刷新视频Feed流
     */
    private void refresh() {
        Retrofit retrofit = RetrofitManager.get(HOST);

        Call<FeedResponse> call = retrofit.create(IMiniDouyinService.class).feed();
        //异步网络请求
        call.enqueue(new Callback<FeedResponse>() {
            @Override
            public void onResponse(Call<FeedResponse> call, Response<FeedResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "视频流刷新成功", Toast.LENGTH_LONG).show();
                    FeedResponse feedResponse = response.body();
                    if (feedResponse != null && feedResponse.isSuccess()) {
                        Log.d(TAG, feedResponse.getFeeds().toString());
                        videoListAdapter.refresh(feedResponse.getFeeds());
                    }
                }
            }

            @Override
            public void onFailure(Call<FeedResponse> call, Throwable t) {
                Log.d(TAG, "retrofit request fail");
            }
        });


    }

    @Override
    public void onListItemClick(Feed feed) {
        Log.d(TAG, "onListItemClick");
        Intent intent = new Intent(MainActivity.this, VideoDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(URL_KEY, feed.getVideoUrl());
        bundle.putString(TITLE_KEY, feed.getUsername());
        intent.putExtra(URL_BUNDLE, bundle);
        startActivity(intent);
    }
}
