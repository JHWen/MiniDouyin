package cn.edu.bit.codesky.minidouyin.ui;

import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import cn.edu.bit.codesky.minidouyin.R;
import cn.edu.bit.codesky.minidouyin.widget.CircleProgressBarView;

import static cn.edu.bit.codesky.minidouyin.util.Utils.MEDIA_TYPE_IMAGE;
import static cn.edu.bit.codesky.minidouyin.util.Utils.MEDIA_TYPE_VIDEO;
import static cn.edu.bit.codesky.minidouyin.util.Utils.getOutputMediaFile;

/**
 * @author codesky
 * @date 2019/1/27 13:59
 * @description 视频录制activity
 */
public class VideoRecordActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final String TAG = VideoRecordActivity.class.getName();

    private SurfaceView mSurfaceView;
    private Camera mCamera;
    private Button btnNextStep;
    private Chronometer chronometer;
    private CircleProgressBarView circleProgressBarView;
    private Button btnRecord;

    private int CAMERA_TYPE = Camera.CameraInfo.CAMERA_FACING_BACK;

    private boolean isRecording = false;

    private int rotationDegree = 0;

    private static final String BUNDLE_KEY = "bundle_key";
    private static final String VIDEO_FILE_PATH_KEY = "video_file_path";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_record);

        circleProgressBarView = findViewById(R.id.circle_progress_bar);
        chronometer = findViewById(R.id.chronometer);
        mSurfaceView = findViewById(R.id.img);
        //获取摄像头/后置摄像头
        rotationDegree = getCameraDisplayOrientation(CAMERA_TYPE);
        mCamera = getCamera(CAMERA_TYPE);
        //todo 给SurfaceHolder添加Callback
        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(this);

        btnNextStep = findViewById(R.id.btn_next_step);
        btnNextStep.setOnClickListener(v -> {
            // 下一步 -> 预览视频，上传视频
            if (outputVideoFile != null) {
                Intent intent = new Intent(this, VideoUploadActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(VIDEO_FILE_PATH_KEY, outputVideoFile.getAbsolutePath());
                intent.putExtra(BUNDLE_KEY, bundle);
                startActivity(intent);
            }
        });

        btnRecord = findViewById(R.id.btn_record);
        btnRecord.setOnClickListener(v -> {
            // 录制，第一次点击是start，第二次点击是stop
            if (isRecording) {
                //停止录制
                stopRecordProcess();
            } else {
                if (prepareVideoRecorder()) {
                    // 录制
                    isRecording = true;
                    Log.d(TAG, "开始录制");
                    // 启动计时器
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();
                    circleProgressBarView.startProgressAnimation();
                    // 事件分发机制，延迟10秒执行操作
                    new Handler().postDelayed(() -> {
                        if (isRecording) {
                            //十秒自动结束录制
                            stopRecordProcess();
                        }
                    }, 11 * 1000);

                    // 视频至少录制3秒
                    btnRecord.setEnabled(false);
                    new Handler().postDelayed(() -> btnRecord.setEnabled(true), 3 * 1000);

                } else {
                    isRecording = false;
                }
            }
        });

        findViewById(R.id.btn_facing).setOnClickListener(v -> {
            // 切换前后摄像头
            if (CAMERA_TYPE == Camera.CameraInfo.CAMERA_FACING_BACK) {
                rotationDegree = getCameraDisplayOrientation(Camera.CameraInfo.CAMERA_FACING_FRONT);
                openCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
            } else {
                rotationDegree = getCameraDisplayOrientation(Camera.CameraInfo.CAMERA_FACING_BACK);
                openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
            }
        });

        findViewById(R.id.btn_zoom).setOnClickListener(v -> {
            // 调焦，需要判断手机是否支持
            if (mCamera != null) {
                Camera.Parameters parameters = mCamera.getParameters();
                if (parameters.isZoomSupported()) {
                    int maxZoom = parameters.getMaxZoom();
                    int currentZoom = parameters.getZoom();
                    Log.d(TAG, "maxZoom:" + maxZoom);
                    Log.d(TAG, "maxZoom:" + currentZoom);
                    int changeZoom = currentZoom + 10;
                    if (changeZoom > maxZoom) {
                        changeZoom = 0;
                    } else {
                        changeZoom = Math.min(changeZoom, maxZoom);
                    }
                    parameters.setZoom(changeZoom);
                    mCamera.setParameters(parameters);
                    Log.d(TAG, "zoom: " + changeZoom);
                }
            }
        });
    }

    // 停止录制的一系列操作
    private void stopRecordProcess() {
        // 停止录制
        releaseMediaRecorder();

        btnNextStep.setEnabled(true);
        btnNextStep.setVisibility(View.VISIBLE);
        isRecording = false;

        chronometer.stop();
        circleProgressBarView.stopProgressAnimation();
        // 发送广播通知相册更新数据,显示所拍摄的视频
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(outputVideoFile)));

    }

    //重新获取摄像头，开始预览
    private void openCamera(int type) {
        try {
            mCamera = getCamera(type);
            startPreview(mSurfaceView.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera == null) {
            openCamera(CAMERA_TYPE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();
        releaseCameraAndPreview();
    }


    public Camera getCamera(int position) {
        CAMERA_TYPE = position;
        releaseCameraAndPreview();
        Camera cam = Camera.open(position);
        //todo 摄像头添加属性，例是否自动对焦，设置旋转方向等
        cam.setDisplayOrientation(rotationDegree);

        Camera.Parameters params = cam.getParameters();
        List<String> focusModes = params.getSupportedFocusModes();
        Log.d(TAG, focusModes.toString());
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            Log.d(TAG, "auto focus");
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        cam.setParameters(params);

        return cam;
    }


    private static final int DEGREE_90 = 90;
    private static final int DEGREE_180 = 180;
    private static final int DEGREE_270 = 270;
    private static final int DEGREE_360 = 360;

    private int getCameraDisplayOrientation(int cameraId) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = DEGREE_90;
                break;
            case Surface.ROTATION_180:
                degrees = DEGREE_180;
                break;
            case Surface.ROTATION_270:
                degrees = DEGREE_270;
                break;
            default:
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % DEGREE_360;
            result = (DEGREE_360 - result) % DEGREE_360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + DEGREE_360) % DEGREE_360;
        }
        return result;
    }


    private void releaseCameraAndPreview() {
        //todo 释放camera资源
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    Camera.Size size;

    private void startPreview(SurfaceHolder holder) throws IOException {
        //todo 开始预览
        // 调整预览size
        Camera.Parameters params = mCamera.getParameters();
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();
        Camera.Size optimalPreviewSize = getOptimalPreviewSize(sizes, mSurfaceView.getWidth(),
                mSurfaceView.getHeight());
        if (optimalPreviewSize != null) {
            params.setPreviewSize(optimalPreviewSize.width, optimalPreviewSize.height);
        }

        mCamera.setPreviewDisplay(holder);
        mCamera.startPreview();
        mCamera.cancelAutoFocus();

    }


    private MediaRecorder mMediaRecorder;
    private File outputVideoFile;

    private boolean prepareVideoRecorder() {
        //todo 准备MediaRecorder
        if (mMediaRecorder != null) {
            releaseMediaRecorder();
        }
        mMediaRecorder = new MediaRecorder();

        //step1:Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        //step2:set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        //step3:set a CamcorderProfile(require API Level 8 or higher)
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        //step4:set output file
        outputVideoFile = getOutputMediaFile(MEDIA_TYPE_VIDEO);
        mMediaRecorder.setOutputFile(outputVideoFile.toString());
        //step5:set the preview output
        mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
        mMediaRecorder.setOrientationHint(rotationDegree);
        //step6:Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        }
        return true;
    }


    private void releaseMediaRecorder() {
        //todo 释放MediaRecorder
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mCamera.lock();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            startPreview(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //todo 释放Camera和MediaRecorder资源
        releaseMediaRecorder();
        releaseCameraAndPreview();
    }


    private Camera.PictureCallback mPicture = (data, camera) -> {
        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (pictureFile == null) {
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
        } catch (IOException e) {
            Log.d("mPicture", "Error accessing file: " + e.getMessage());
        }
        // 发送广播通知相册更新数据,显示所拍摄的照片
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(pictureFile)));
        mCamera.startPreview();
    };


    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = Math.min(w, h);

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

}
