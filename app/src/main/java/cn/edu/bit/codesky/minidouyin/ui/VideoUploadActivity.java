package cn.edu.bit.codesky.minidouyin.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.VideoView;

import java.io.File;

import cn.edu.bit.codesky.minidouyin.R;
import cn.edu.bit.codesky.minidouyin.util.UriUtils;
import cn.edu.bit.codesky.minidouyin.util.Utils;

/**
 * @author codesky
 * @date 2019/1/27 13:53
 * @description 视频上传activity
 */
public class VideoUploadActivity extends AppCompatActivity {

    private static final String TAG = VideoUploadActivity.class.getName();

    private VideoView videoView;
    private Button btnUpload;

    private File videoFile;

    private static final String BUNDLE_KEY = "bundle_key";
    private static final String VIDEO_FILE_PATH_KEY = "video_file_path";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_upload);

        videoView = findViewById(R.id.video_view);
        btnUpload = findViewById(R.id.btn_upload);

        btnUpload.setOnClickListener(v -> {

        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //预览拍摄视频
            Bundle bundle = extras.getBundle(BUNDLE_KEY);
            if (bundle != null) {
                String videoPath = bundle.getString(VIDEO_FILE_PATH_KEY);
                videoFile = new File(videoPath);
                videoView.setVideoURI(Uri.fromFile(videoFile));
                videoView.start();
            }
        }

        videoView.setOnClickListener(v -> {
            if (videoView.isPlaying()) {
                videoView.pause();
            } else {
                videoView.start();
            }
        });

    }
}
