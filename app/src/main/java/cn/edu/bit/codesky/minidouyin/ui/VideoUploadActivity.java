package cn.edu.bit.codesky.minidouyin.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;

import cn.edu.bit.codesky.minidouyin.MainActivity;
import cn.edu.bit.codesky.minidouyin.R;
import cn.edu.bit.codesky.minidouyin.beans.PostVideoResponse;
import cn.edu.bit.codesky.minidouyin.util.IMiniDouyinService;
import cn.edu.bit.codesky.minidouyin.util.ResourceUtils;
import cn.edu.bit.codesky.minidouyin.util.RetrofitManager;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * @author codesky
 * @date 2019/1/27 13:53
 * @description 视频上传activity
 */
public class VideoUploadActivity extends AppCompatActivity {

    private static final String TAG = VideoUploadActivity.class.getName();

    private VideoView videoView;
    private Button btnUpload;
    private ContentLoadingProgressBar progressBar;

    private File videoFile;

    private static final String BUNDLE_KEY = "bundle_key";
    private static final String VIDEO_FILE_PATH_KEY = "video_file_path";

    // 上传视频的一些参数
    private static final String HOST = "http://10.108.10.39:8080/";
    private static final String IMAGE_NAME = "cover_image";
    private static final String VIDEO_NAME = "video";
    private static final String STUDENT_ID = "3220180750";
    private static final String USER_NAME = "wenjiahao";

    // 上传的视频、封面的uri
    public Uri mSelectedImage;
    private Uri mSelectedVideo;

    private static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_upload);

        videoView = findViewById(R.id.video_view);
        btnUpload = findViewById(R.id.btn_upload);
        progressBar = findViewById(R.id.pb_loading);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //预览拍摄视频
            Bundle bundle = extras.getBundle(BUNDLE_KEY);
            if (bundle != null) {
                String videoPath = bundle.getString(VIDEO_FILE_PATH_KEY);
                videoFile = new File(videoPath);
                mSelectedVideo = Uri.fromFile(videoFile);
                videoView.setVideoURI(mSelectedVideo);
                videoView.start();
            }
        }

        // 点击视频暂停/播放效果
        videoView.setOnClickListener(v -> {
            if (videoView.isPlaying()) {
                videoView.pause();
            } else {
                videoView.start();
            }
        });

        progressBar.hide();

        // 上次视频操作
        btnUpload.setOnClickListener(v -> {
            if (mSelectedImage != null && mSelectedVideo != null) {
                progressBar.show();
                postVideo();
            } else {
                Toast.makeText(getApplicationContext(), "请选择封面图", Toast.LENGTH_LONG).show();
                chooseImage();
            }
        });

    }

    // Start Activity to select an image
    private void chooseImage() {
        //隐式的Intent，定义规则，交给系统去选择activity执行操作
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && null != data) {
            switch (requestCode) {
                case PICK_IMAGE:
                    mSelectedImage = data.getData();
                    break;
            }
        }
    }

    private MultipartBody.Part getMultipartFromUri(String name, Uri uri) {
        // if NullPointerException thrown, try to allow storage permission in system settings
        File f = new File(ResourceUtils.getRealPath(VideoUploadActivity.this, uri));
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
        return MultipartBody.Part.createFormData(name, f.getName(), requestFile);
    }

    // 上传视频操作
    private void postVideo() {
        // Send Request to post a video with its cover image
        // if success, make a text Toast and show
        Retrofit retrofit = RetrofitManager.get(HOST);

        MultipartBody.Part imagePart = getMultipartFromUri(IMAGE_NAME, mSelectedImage);

        MultipartBody.Part videoPart = getMultipartFromUri(VIDEO_NAME, mSelectedVideo);

        Call<PostVideoResponse> call = retrofit.create(IMiniDouyinService.class)
                .postVideo(STUDENT_ID, USER_NAME, imagePart, videoPart);

        call.enqueue(new Callback<PostVideoResponse>() {
            @Override
            public void onResponse(Call<PostVideoResponse> call, Response<PostVideoResponse> response) {
                if (response.isSuccessful()) {
                    PostVideoResponse postVideoResponse = response.body();
                    if (postVideoResponse != null && postVideoResponse.isSuccess()) {
                        // return to video list activity
                        Log.d(TAG, "onResponse(): upload successfully");
                        Toast.makeText(getApplicationContext(), "视频上传成功", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(VideoUploadActivity.this, MainActivity.class));
                    } else {
                        Log.d(TAG, "onResponse(): fail in uploading");
                    }
                } else {
                    Log.d(TAG, "onResponse: fail in uploading");
                }

            }

            @Override
            public void onFailure(Call<PostVideoResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: network request failure");
            }
        });

    }
}
