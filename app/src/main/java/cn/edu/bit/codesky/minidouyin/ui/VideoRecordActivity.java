package cn.edu.bit.codesky.minidouyin.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import cn.edu.bit.codesky.minidouyin.R;
import cn.edu.bit.codesky.minidouyin.widget.CircleProgressBarView;

import static cn.edu.bit.codesky.minidouyin.util.Utils.MEDIA_TYPE_IMAGE;
import static cn.edu.bit.codesky.minidouyin.util.Utils.MEDIA_TYPE_VIDEO;
import static cn.edu.bit.codesky.minidouyin.util.Utils.getOutputMediaFile;
import static java.lang.Math.sqrt;

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
    private Button btnDelete;

    private Handler handler;
    private Runnable autoStopRecordRunnable;

    private int CAMERA_TYPE = Camera.CameraInfo.CAMERA_FACING_BACK;

    private boolean isRecording = false;

    private int rotationDegree = 0;

    private static final String BUNDLE_KEY = "bundle_key";
    private static final String VIDEO_FILE_PATH_KEY = "video_file_path";
    private static final int MAX_RECORD_TIME = 11 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_record);

        handler = new Handler();
        autoStopRecordRunnable = () -> {
            if (isRecording) {
                //十秒自动结束录制
                stopRecordProcess();
            }
        };

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
                    handler.postDelayed(autoStopRecordRunnable, MAX_RECORD_TIME);

                    //隐藏左右的按钮
                    btnDelete.setVisibility(View.GONE);
                    btnNextStep.setVisibility(View.GONE);

                    // 视频至少录制3秒
                    btnRecord.setEnabled(false);
                    handler.postDelayed(() -> btnRecord.setEnabled(true), 3 * 1000);

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


        btnDelete = findViewById(R.id.btn_cancel_delete_video);
        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("确认删除上一段视频？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(TAG, "执行删除操作");
                            if (null != outputVideoFile && outputVideoFile.exists()) {
                                boolean flag = outputVideoFile.delete();
                                if (flag) {
                                    Toast.makeText(getApplicationContext(), "删除成功，请重新录制！", Toast.LENGTH_LONG)
                                            .show();

                                    // 执行初始化逻辑，清空状态
                                    btnNextStep.setVisibility(View.GONE);
                                    btnDelete.setVisibility(View.GONE);
                                    chronometer.setBase(SystemClock.elapsedRealtime());
                                    circleProgressBarView.reset();

                                    //移除10秒自动停止任务
                                    handler.removeCallbacks(autoStopRecordRunnable);
                                    // 发送广播通知相册更新数据,显示所拍摄的视频
                                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(outputVideoFile)));
                                    outputVideoFile = null;
                                } else {
                                    Toast.makeText(getApplicationContext(), "删除失败", Toast.LENGTH_LONG)
                                            .show();
                                }
                            }
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(TAG, "取消删除操作");
                        }
                    }).create().show();
        });
    }


    /**
     * @param zoomNum  放大或缩小的数量
     * @param isZoomUp true为放大，false为缩小
     */
    private void adjustZoom(int zoomNum, boolean isZoomUp) {
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            if (parameters.isZoomSupported()) {
                int maxZoom = parameters.getMaxZoom();
                int currentZoom = parameters.getZoom();

                Log.d(TAG, "maxZoom:" + maxZoom);
                Log.d(TAG, "maxZoom:" + currentZoom);
                int changeZoom;
                if (isZoomUp) {
                    // 放大
                    changeZoom = currentZoom + zoomNum;
                    changeZoom = Math.min(changeZoom, maxZoom);
                } else {
                    // 缩小
                    changeZoom = currentZoom - zoomNum;
                    changeZoom = Math.max(changeZoom, 0);
                }
                parameters.setZoom(changeZoom);
                mCamera.setParameters(parameters);
                Log.d(TAG, "zoom: " + changeZoom);
            }
        }
    }

    private int mode = 0;
    private boolean fingerHasMove = false;
    private float preDistance = 0;

    // 监听触摸事件，实现双指手势缩放调焦
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                //单指触摸
                mode = 1;
                Log.d(TAG, "ACTION_DOWN");
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mode = 2;
                Log.d(TAG, "ACTION_POINTER_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "ACTION_MOVE");
                if (mode == 2) {
                    if (!fingerHasMove) {
                        preDistance = getDistance(event);
                        fingerHasMove = true;
                    } else {
                        float distance = getDistance(event);
                        if (distance - preDistance > 10) {
                            // 放大
                            adjustZoom(20, true);
                            mode = 0;
                        } else if (preDistance - distance > 10) {
                            // 缩小
                            adjustZoom(20, false);
                            mode = 0;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "ACTION_UP");
                mode = 0;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                Log.d(TAG, "ACTION_POINTER_UP");
                // 一个手指离开屏幕
                preDistance = 0;
                fingerHasMove = false;
                mode = 0;
                break;
        }
        return super.onTouchEvent(event);

    }

    // 计算两个屏幕触点之间的距离
    private float getDistance(MotionEvent event) {
        // 勾股定理
        float dx = event.getX(1) - event.getY(0);
        float dy = event.getY(1) - event.getY(0);
        return (float) sqrt(dx * dx + dy * dy);
    }

    // 停止录制的一系列操作
    private void stopRecordProcess() {
        // 停止录制
        releaseMediaRecorder();

        this.runOnUiThread(() -> {
            // 显示取消按钮
            btnNextStep.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
            chronometer.stop();
            circleProgressBarView.stopProgressAnimation();
        });

        isRecording = false;

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
