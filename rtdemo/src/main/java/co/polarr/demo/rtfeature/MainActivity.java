package co.polarr.demo.rtfeature;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

import co.polarr.processing.PolarrFeatureProcessor;
import co.polarr.processing.entities.FeatureItem;

public class MainActivity extends Activity {
    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private CameraView camera_iv;

    private PolarrFeatureProcessor processor;
    private boolean isFeatureWorking;
    private Handler featureHandler;

    // YUV to RGB
    private Bitmap stitchBmp;
    private RenderScript rs;
    private ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;
    private Allocation in;
    private Allocation out;
    private int lastWidth;
    private int lastHeight;
    private TextView feature_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        feature_tv = findViewById(R.id.feature_tv);

        camera_iv = findViewById(R.id.camera_iv);
        camera_iv.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(final byte[] bytes, Camera camera) {
                Camera.Size size = camera.getParameters().getPreviewSize();
                final int width = size.width;
                final int height = size.height;


                if (!isFeatureWorking) {
                    featureHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            isFeatureWorking = true;
                            renderScriptNV21ToRGBA888(width, height, bytes);

                            synchronized (stitchBmp) {
                                out.copyTo(stitchBmp);
                            }

                            if (processor == null) {
                                return;
                            }

                            long start = System.currentTimeMillis();
                            Bitmap scaledBM = getScaledFitBitmap(stitchBmp, 300, 300);
                            final FeatureItem featureResult = processor.featurePhotoSync(scaledBM, width, height);
//                            scaledBM.recycle();
                            featureResult.benchmark = (System.currentTimeMillis() - start);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    feature_tv.setText(getRatingDisplayResult(featureResult));
                                }
                            });

                            isFeatureWorking = false;
                        }
                    });
                }
            }
        });

        initNV21Processing();

        HandlerThread featureThread = new HandlerThread("PolarrFeatureThread");
        featureThread.start();
        featureHandler = new Handler(featureThread.getLooper());

        featureHandler.post(new Runnable() {
            @Override
            public void run() {
                processor = new PolarrFeatureProcessor();
                processor.initSync(MainActivity.this);
            }
        });

        if (!checkAndRequirePermission(REQUEST_CAMERA_PERMISSION)) {
            camera_iv.setVisibility(View.INVISIBLE);
        }
    }

    private boolean checkAndRequirePermission(int permissionRequestId) {
        String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        switch (permissionRequestId) {
            case REQUEST_CAMERA_PERMISSION:
                permission = Manifest.permission.CAMERA;
                break;
        }
        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    permissionRequestId);

            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                camera_iv.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        processor.release();
        processor = null;
        super.onDestroy();
    }

    private void initNV21Processing() {
        rs = RenderScript.create(this);
        yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
    }

    private void renderScriptNV21ToRGBA888(int width, int height, byte[] nv21) {
        if (stitchBmp == null) {
            stitchBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }

        if (lastWidth == width && lastHeight == height) {
            in.copyFrom(nv21);

            yuvToRgbIntrinsic.setInput(in);
            yuvToRgbIntrinsic.forEach(out);
        } else {
            Type.Builder yuvType = new Type.Builder(rs, Element.U8(rs)).setX(nv21.length);
            in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);

            Type.Builder rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height);
            out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);

            lastWidth = width;
            lastHeight = height;

            synchronized (stitchBmp) {
//                stitchBmp.recycle();
            }

            stitchBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }
    }

    private static Bitmap getScaledFitBitmap(Bitmap bitmap, int destWidth, int destHeight) {
        float minScale = Math.min((float) destWidth / bitmap.getWidth(), (float) destHeight / bitmap.getHeight());
        minScale = Math.min(1, minScale);
        int targetWidth = (int) (bitmap.getWidth() * minScale);
        int targetHeight = (int) (bitmap.getHeight() * minScale);
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
    }

    private static String getRatingDisplayResult(FeatureItem result) {
        StringBuilder sb = new StringBuilder();

        sb.append("Scoring result:");
        sb.append("\n");
        sb.append("\n");

        if (result.grouping_bad_reason != null) {
            sb.append("Bad Reason:");
            sb.append(result.grouping_bad_reason);
            sb.append("\n");
        }

        sb.append("Overall:");
        sb.append(String.format(Locale.ENGLISH, "%.2f", result.rating_all * 100));
        sb.append("\n");

        sb.append("\n");
        sb.append("clarity:");
        sb.append(result.metric_clarity);
        sb.append("\n");
        sb.append("exposure:");
        sb.append(result.metric_exposure);
        sb.append("\n");
        sb.append("colorfulness:");
        sb.append(result.metric_colorfulness);
        sb.append("\n");
        float emotion = result.metric_emotion.a();
        if (emotion > 0) {
            sb.append("emotion:");
            sb.append(emotion);
            sb.append("\n");
        }

        sb.append("\n");
        sb.append("Benchmark:");
        sb.append(result.benchmark);
        sb.append("ms");

        return sb.toString();
    }
}
