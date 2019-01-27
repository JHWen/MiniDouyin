package cn.edu.bit.codesky.minidouyin.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.shuyu.gsyvideoplayer.GSYBaseActivityDetail;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import cn.edu.bit.codesky.minidouyin.R;


/**
 * @author codesky
 * @date 2019/1/27 10:43
 * @description default
 */
public class VideoDetailActivity extends GSYBaseActivityDetail<StandardGSYVideoPlayer> {

    private StandardGSYVideoPlayer videoDetailPlayer;

    private static final String URL_KEY = "video_url";
    private static final String TITLE_KEY = "video_title";
    private static final String URL_BUNDLE = "url_bundle";
    private String title = "demo";

    private String url = "https://res.exexm.com/cw_145225549855002";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_detail);

        //获取视频列表跳转到视频详情页的video url
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Bundle bundle = extras.getBundle(URL_BUNDLE);
            if (bundle != null) {
                url = bundle.getString(URL_KEY);
                title = bundle.getString(TITLE_KEY);
            }
        }

        videoDetailPlayer = findViewById(R.id.video_detail_player);
        //增加title
        videoDetailPlayer.getTitleTextView().setVisibility(View.VISIBLE);
        videoDetailPlayer.getBackButton().setVisibility(View.VISIBLE);

        initVideoBuilderMode();
    }


    @Override
    public StandardGSYVideoPlayer getGSYVideoPlayer() {
        return videoDetailPlayer;
    }

    @Override
    public GSYVideoOptionBuilder getGSYVideoOptionBuilder() {
        //内置封面可参考SampleCoverVideo
        ImageView imageView = new ImageView(this);
        loadCover(imageView, url);
        return new GSYVideoOptionBuilder()
                .setThumbImageView(imageView)
                .setUrl(url)
                .setCacheWithPlay(true)
                .setVideoTitle(title)
                .setIsTouchWiget(true)
                .setRotateViewAuto(false)
                .setLockLand(false)
                .setShowFullAnimation(false)//打开动画
                .setNeedLockFull(true)
                .setSeekRatio(1);
    }

    @Override
    public void clickForFullScreen() {

    }

    /**
     * 是否启动旋转横屏，true表示启动
     */
    @Override
    public boolean getDetailOrientationRotateAuto() {
        return true;
    }

    private void loadCover(ImageView imageView, String url) {
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.mipmap.error_disp);
        Glide.with(this.getApplicationContext())
                .setDefaultRequestOptions(
                        new RequestOptions()
                                .frame(3000000)
                                .centerCrop()
                                .error(R.mipmap.default_disp)
                                .placeholder(R.mipmap.error_disp))
                .load(url)
                .into(imageView);
    }

}
