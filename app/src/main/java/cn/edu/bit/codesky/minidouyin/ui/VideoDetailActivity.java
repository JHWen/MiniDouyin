package cn.edu.bit.codesky.minidouyin.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.shuyu.gsyvideoplayer.GSYBaseActivityDetail;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import cn.edu.bit.codesky.minidouyin.MyClickListener;
import cn.edu.bit.codesky.minidouyin.R;
import cn.edu.bit.codesky.minidouyin.db.VideoContract;
import cn.edu.bit.codesky.minidouyin.db.VideoDbHelper;


/**
 * @author codesky
 * @date 2019/1/27 10:43
 * @description default
 */
public class VideoDetailActivity extends GSYBaseActivityDetail<StandardGSYVideoPlayer> {

    private static final String TAG = VideoDetailActivity.class.getName();
    private View player;
    private View player1;
    private View player2;
    private TextView tv_name;
    private ImageView iv_like;
    private ImageView iv_heart;
    private boolean b_islike = false;

    private StandardGSYVideoPlayer videoDetailPlayer;

    private static final String URL_KEY = "video_url";
    private static final String TITLE_KEY = "video_title";
    private static final String URL_BUNDLE = "url_bundle";

    private AnimatorSet animatorSet;
    public SQLiteDatabase db;

    private String title = "demo";
    private String url = "https://res.exexm.com/cw_145225549855002";
    private String img_url = "null";
    private String stu_id = "0";


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
                stu_id = bundle.getString("stuid");
                img_url = bundle.getString("cover");
            }
        }

        VideoDbHelper mDbHelper = new VideoDbHelper(this);
        db = mDbHelper.getWritableDatabase();

        player=findViewById(R.id.video_detail_player);
        player1=findViewById(R.id.iv_touch1);
        player2=findViewById(R.id.iv_touch2);

        tv_name=findViewById(R.id.tv_name);
        tv_name.setText("上传者:" + title);
        iv_like=findViewById(R.id.iv_like);
        iv_heart=findViewById(R.id.iv_heart);

        if(isindb(url)) {
            if(islikeurl(url))
            {
                iv_like.setImageDrawable(getResources().getDrawable(R.drawable.heart_r));
                b_islike=true;
            }
        }
        else {
            saveNote2Database(url, stu_id, title, img_url);
        }

        initamina();


        player1.setOnTouchListener(new MyClickListener
                (new MyClickListener.MyClickCallBack() {

                    @Override
                    public void oneClick() {
                        //videoDetailPlayer.startPlayLogic();
                    }

                    @Override
                    public void doubleClick() {
                        if(b_islike==false) {
                            likeeffect(url);
                        }
                    }
                }));

        player2.setOnTouchListener(new MyClickListener
                (new MyClickListener.MyClickCallBack() {

                    @Override
                    public void oneClick() {
                        //videoDetailPlayer.startPlayLogic();
                    }

                    @Override
                    public void doubleClick() {
                        if(b_islike==false) {
                            likeeffect(url);
                        }
                    }
                }));

        iv_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(b_islike==false) {
                    likeeffect(url);
                }
                else{
                    dislikevid(url);
                    iv_like.setImageDrawable(getResources().getDrawable(R.drawable.heart));
                    b_islike=false;
                }
            }
        });

        videoDetailPlayer = findViewById(R.id.video_detail_player);
        //增加title
        videoDetailPlayer.getTitleTextView().setVisibility(View.VISIBLE);
        videoDetailPlayer.getBackButton().setVisibility(View.VISIBLE);

        initVideoBuilderMode();

        videoDetailPlayer.startPlayLogic();
    }

    public void initamina()
    {
        if (animatorSet != null) {
            animatorSet.cancel();
        }

        ObjectAnimator alphaAnimation = ObjectAnimator.ofFloat(iv_heart, "alpha", 0.0f, 1.0f, 0.0f);
        alphaAnimation.setDuration(1000);
        alphaAnimation.setRepeatMode(ValueAnimator.REVERSE);

        animatorSet = new AnimatorSet();
        animatorSet.play(alphaAnimation);
        //animatorSet.start();
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

    private boolean isindb(String vidurl)
    {
        //ContentValues values=new ContentValues();
        //values.put(VideoContract.VideoEntry.COLUMN_STATE,note.getState()==State.DONE?1:0);
        Cursor cursor=null;
        String selection = VideoContract.VideoEntry.COLUMN_VIDURL + " LIKE ?";
        String[] selectionArgs = {vidurl};
        cursor = db.query(VideoContract.VideoEntry.TABLE_NAME,new String[]{VideoContract.VideoEntry.COLUMN_VIDURL,VideoContract.VideoEntry.COLUMN_ISLIKE},
                selection,selectionArgs,null,null,null);
        if(cursor.getCount()==0)
            return false;
        else
            return true;
    }

    private boolean saveNote2Database(String vidurl, String stuid, String usrname, String imgurl) {
        if(isindb(vidurl))
            return false;

        ContentValues values = new ContentValues();
        values.put(VideoContract.VideoEntry.COLUMN_VIDURL, vidurl);
        values.put(VideoContract.VideoEntry.COLUMN_STUID, stuid);
        values.put(VideoContract.VideoEntry.COLUMN_USRNAME, usrname);
        values.put(VideoContract.VideoEntry.COLUMN_IMGURL, imgurl);
        values.put(VideoContract.VideoEntry.COLUMN_ISLIKE, 0);
        values.put(VideoContract.VideoEntry.COLUMN_LIKENUM, 0);

        long newRowId = db.insert(VideoContract.VideoEntry.TABLE_NAME,null, values);
        if(newRowId>=0)
            return true;
        else
            return false;
    }

    private boolean likevid(String vidurl)
    {
        ContentValues values=new ContentValues();
        values.put(VideoContract.VideoEntry.COLUMN_ISLIKE,1);

        String selection = VideoContract.VideoEntry.COLUMN_VIDURL + " LIKE ?";
        String[] selectionArgs = {vidurl};
        int count = db.update(VideoContract.VideoEntry.TABLE_NAME,values,selection,selectionArgs);
        if(count>0)
            return true;
        else
            return false;
    }

    private void likeeffect(String vidurl)
    {
        likevid(vidurl);
        iv_like.setImageDrawable(getResources().getDrawable(R.drawable.heart_r));
        animatorSet.start();
        b_islike=true;
    }

    private boolean dislikevid(String vidurl)
    {
        ContentValues values=new ContentValues();
        values.put(VideoContract.VideoEntry.COLUMN_ISLIKE,0);

        String selection = VideoContract.VideoEntry.COLUMN_VIDURL + " LIKE ?";
        String[] selectionArgs = {vidurl};
        int count = db.update(VideoContract.VideoEntry.TABLE_NAME,values,selection,selectionArgs);
        if(count>0)
            return true;
        else
            return false;
    }

    private boolean islikeurl (String url)
    {
        Cursor cursor=null;
        String selection = VideoContract.VideoEntry.COLUMN_VIDURL + " LIKE ?";
        String[] selectionArgs = {url};
        try{
            cursor = db.query(VideoContract.VideoEntry.TABLE_NAME,new String[]{VideoContract.VideoEntry.COLUMN_VIDURL,VideoContract.VideoEntry.COLUMN_ISLIKE},
                    selection,selectionArgs,null,null,null);

            while(cursor.moveToNext())
            {
                int l = cursor.getInt(cursor.getColumnIndex(VideoContract.VideoEntry.COLUMN_ISLIKE));
                if(l==1)
                    return true;
                else
                    return false;

            }
        } finally {
            if(cursor != null)
            {
                cursor.close();
            }
        }
        return false;
    }
}
