package co.polarr.demo.rtfeature;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by Colin on 2018/4/26.
 */
public class CameraView extends SurfaceView implements Camera.AutoFocusCallback {
    /**
     * Camera and SurfaceTexture
     */
    private Camera mCamera;
    private int camera_width = 0;
    private int camera_height = 0;
    private boolean isRear = true;
    private Camera.PreviewCallback mPreviewCallback;
    private int previewWidth;
    private int previewHeight;
    private boolean isRearRotate180 = false;
    private int rotation;

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setClickable(true);
        getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, final int width, int height) {
                previewWidth = width;
                previewHeight = height;
                updateCamera(isRear);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mCamera.stopPreview();
            }
        });
    }

    private int findCamera(boolean isRear) {
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras(); // get cameras number

        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
            if (cameraInfo.facing == (isRear ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT)) {
                return camIdx;
            }
        }
        return -1;
    }

    private void updateCamera(boolean isRear) {
        if (previewWidth <= 0 || previewHeight <= 0) {
            return;
        }
        if (mCamera != null) {
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        mCamera = Camera.open(findCamera(isRear));


        // small preview
        int pSizeW = previewWidth;
        int pSizeH = previewHeight;

        Camera.Parameters param = mCamera.getParameters();
        List<Camera.Size> psize = param.getSupportedPreviewSizes();
        if (psize.size() > 0) {
            int i;
            for (i = 0; i < psize.size(); i++) {
                if (psize.get(i).width < pSizeW || psize.get(i).height < pSizeH)
                    break;
            }
            if (i > 0)
                i--;
            param.setPreviewSize(psize.get(i).width, psize.get(i).height);
            rotation = 0;

            if (isRear) {
                rotation = isRearRotate180 ? 270 : 90;

            } else {
                rotation = 90;
            }

            setDisplayOrientation(mCamera, rotation);
            camera_width = psize.get(i).width;
            camera_height = psize.get(i).height;
        }
        mCamera.setParameters(param);

        post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams params = getLayoutParams();
                params.width = previewWidth;
                params.height = previewWidth * camera_width / camera_height; // portrait mode only
                setLayoutParams(params);
            }
        });

//        getHolder().setFixedSize(camera_height, camera_width);
        getHolder().setFixedSize(previewWidth, previewHeight);
        startCamera();
    }

    public int getCameraRotation() {
        return rotation;
    }

    private void startCamera() {
        try {
            mCamera.setPreviewDisplay(getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                if (mPreviewCallback != null) {
                    mPreviewCallback.onPreviewFrame(data, camera);
                }

                camera.addCallbackBuffer(data);
            }
        });
        mCamera.addCallbackBuffer(new byte[((camera_width * camera_height) * ImageFormat.getBitsPerPixel(ImageFormat.NV21)) / 8]);
        mCamera.startPreview();
    }

    public void setPreviewCallback(Camera.PreviewCallback previewCallback) {
        this.mPreviewCallback = previewCallback;
    }

    public void switchCamera(boolean isRear) {
        if (this.isRear != isRear) {
            this.isRear = isRear;
            updateCamera(isRear);
        }
    }

    public void setRearRotate(boolean isRearRotate180) {
        if (this.isRearRotate180 != isRearRotate180) {
            this.isRearRotate180 = isRearRotate180;
            updateCamera(isRear);
        }
    }

    private void setDisplayOrientation(Camera camera, int i) {
        Method downPolymorphic;
        try {
            downPolymorphic = camera.getClass().getMethod(
                    "setDisplayOrientation", new Class[]{int.class});
            if (downPolymorphic != null) {
                downPolymorphic.invoke(camera, new Object[]{i});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mCamera != null) {
            mCamera.autoFocus(this);
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {

    }
}
