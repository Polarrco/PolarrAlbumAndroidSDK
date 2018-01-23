package co.polarr.albumsdkdemo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import co.polarr.albumsdkdemo.entities.GroupingResult;
import co.polarr.albumsdkdemo.utils.BenchmarkUtil;
import co.polarr.albumsdkdemo.utils.FileUtils;
import co.polarr.albumsdkdemo.utils.ImageUtil;
import co.polarr.albumsdkdemo.utils.MemoryCache;
import co.polarr.processing.POGenerateHClusterCallbackFunction;
import co.polarr.processing.Processing;
import co.polarr.processing.entities.GroupingResultItem;
import co.polarr.processing.entities.ResultItem;
import co.polarr.tagging.probdet.TaggingUtil;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PHOTO = 1;
    private static final int REQUEST_PHOTOS_GROUP = 2;
    private static final int REQUEST_PHOTOS_FACE = 3;
    private static final int REQUEST_PHOTOS_BURST = 4;
    private static final int REQUEST_IMPORT_PHOTO = 5;
    private static final int LOG_MAX_CHARS = 10000;

    private static final int TEST_FACE_DET_WIDTH = 720;
    private static final int TEST_FACE_DET_HEIGHT = 960;
    private static final PointF[] TEST_SENSTIME_FACE_DET_POINTS = {
            new PointF(286.54385f, 361.40244f), new PointF(286.98926f, 370.95502f), new PointF(287.66284f, 380.5722f), new PointF(288.8452f, 390.11874f), new PointF(290.59595f, 399.60583f), new PointF(292.58813f, 409.04327f), new PointF(294.79718f, 418.42172f), new PointF(297.31805f, 427.75858f), new PointF(300.52127f, 436.86423f), new PointF(304.60583f, 445.5826f), new PointF(309.56546f, 453.7632f), new PointF(315.1762f, 461.54095f), new PointF(321.2893f, 468.77216f), new PointF(327.90924f, 475.54947f), new PointF(335.61063f, 480.95938f), new PointF(344.5748f, 484.00867f), new PointF(354.12518f, 485.0118f), new PointF(364.4589f, 484.03537f), new PointF(374.39117f, 480.9098f), new PointF(383.2429f, 475.47137f), new PointF(391.07263f, 468.60773f), new PointF(398.3251f, 461.15106f), new PointF(404.96265f, 453.04764f), new PointF(410.79282f, 444.40442f), new PointF(415.72287f, 435.14694f), new PointF(419.44952f, 425.44678f), new PointF(422.16202f, 415.46448f), new PointF(424.4317f, 405.33017f), new PointF(426.39014f, 395.1298f), new PointF(427.87985f, 384.8649f), new PointF(428.8516f, 374.49973f), new PointF(429.06696f, 364.12546f), new PointF(428.86285f, 353.8208f), new PointF(295.56638f, 354.43555f), new PointF(304.7123f, 346.115f), new PointF(316.51245f, 344.22446f), new PointF(328.42963f, 345.39853f), new PointF(339.70798f, 348.29095f), new PointF(366.42245f, 344.99475f), new PointF(378.44077f, 339.75504f), new PointF(391.2325f, 336.49878f), new PointF(404.2696f, 337.11374f), new PointF(415.27835f, 345.15005f), new PointF(352.52637f, 362.28363f), new PointF(352.42432f, 376.34515f), new PointF(352.28232f, 390.61414f), new PointF(352.0493f, 404.80865f), new PointF(337.2761f, 414.10254f), new PointF(344.88043f, 415.9044f), new PointF(352.78952f, 417.38397f), new PointF(361.22955f, 415.31042f), new PointF(369.71594f, 413.17322f), new PointF(308.23813f, 365.988f), new PointF(314.43814f, 360.0169f), new PointF(330.3685f, 360.5202f), new PointF(335.66235f, 367.1159f), new PointF(328.80783f, 369.59146f), new PointF(314.4073f, 369.74176f), new PointF(372.96698f, 363.60492f), new PointF(377.86545f, 355.58917f), new PointF(394.72934f, 352.37402f), new PointF(402.05685f, 357.57825f), new PointF(396.04874f, 362.6315f), new PointF(380.6767f, 364.8764f), new PointF(305.19937f, 353.27466f), new PointF(316.7307f, 351.9191f), new PointF(328.34464f, 352.38483f), new PointF(339.4216f, 353.99265f), new PointF(367.07736f, 350.9349f), new PointF(378.95068f, 347.15588f), new PointF(391.58194f, 344.47906f), new PointF(404.33224f, 344.4525f), new PointF(322.53348f, 358.07278f), new PointF(321.62204f, 370.6366f), new PointF(322.0506f, 365.20505f), new PointF(385.98596f, 351.6984f), new PointF(388.59607f, 364.87848f), new PointF(387.37802f, 359.03247f), new PointF(344.13632f, 364.67505f), new PointF(362.7452f, 362.9245f), new PointF(337.4274f, 397.70618f), new PointF(369.35043f, 396.54208f), new PointF(332.39542f, 407.87662f), new PointF(374.76157f, 406.22858f), new PointF(325.89536f, 438.50137f), new PointF(335.72464f, 435.0731f), new PointF(345.9736f, 433.19495f), new PointF(352.50787f, 434.28616f), new PointF(359.1929f, 432.96387f), new PointF(370.30127f, 434.28937f), new PointF(381.57416f, 437.40097f), new PointF(373.37506f, 445.39786f), new PointF(363.63495f, 451.29007f), new PointF(352.41733f, 453.4249f), new PointF(341.73892f, 452.04962f), new PointF(332.70715f, 446.4889f), new PointF(329.62732f, 439.2097f), new PointF(341.15405f, 439.66818f), new PointF(352.5827f, 440.88235f), new PointF(364.85266f, 439.2419f), new PointF(377.56366f, 438.37354f), new PointF(364.9507f, 440.5428f), new PointF(352.26904f, 442.23212f), new PointF(340.71368f, 441.1324f), new PointF(322.04477f, 365.2079f), new PointF(387.36346f, 359.03104f)
    };
    private static final Rect TEST_SENSTIME_FACE_RECT = new Rect(280, 325, 427, 472);

    /**
     * debug log view
     */
    private TextView outputView;
    private ScrollView outputCon;

    /**
     * show processing photo
     */
    private ImageView thumbnailView;
    /**
     * show auto enhanced photo
     */
    private ImageView autoEnhanceView;
    /**
     * open grouped photos view
     */
    private Button btnGroupResult;
    private EditText grouping_max;
    private EditText grouping_result_min;
    private EditText grouping_result_max;
    private EditText sensitivity;
    // processing ui
    private View processing_con;
    private ProgressBar processing_pb;
    private TextView processing_tv;
    private boolean isProcessing;
    private final Object processingLock = new Object();
    private Button btn_cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        outputView = (TextView) findViewById(R.id.tf_output);
        outputCon = (ScrollView) findViewById(R.id.sv_output);
        thumbnailView = (ImageView) findViewById(R.id.iv_thumbnail);
        autoEnhanceView = (ImageView) findViewById(R.id.iv_autoenhance);
        btnGroupResult = (Button) findViewById(R.id.btn_group_result);
        btn_cancel = (Button) findViewById(R.id.btn_cancel);
        processing_con = findViewById(R.id.processing_con);
        processing_tv = (TextView) findViewById(R.id.processing_tv);
        processing_pb = (ProgressBar) findViewById(R.id.processing_pb);
        grouping_max = (EditText) findViewById(R.id.grouping_max);
        grouping_result_min = (EditText) findViewById(R.id.grouping_result_min);
        grouping_result_max = (EditText) findViewById(R.id.grouping_result_max);
        sensitivity = (EditText) findViewById(R.id.sensitivity);

        autoEnhanceView.setVisibility(View.GONE);
        btnGroupResult.setVisibility(View.GONE);
        processing_con.setVisibility(View.GONE);


        findViewById(R.id.btn_import).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPhotos(false, false);
            }
        });
        findViewById(R.id.btn_import_faces).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPhotos(true, false);
            }
        });
        findViewById(R.id.btn_import_burst).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPhotos(false, true);
            }
        });
        findViewById(R.id.btn_group_demo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                grouping(new File(getFilesDir() + "/demo"), false, false);
            }
        });
        findViewById(R.id.btn_tag).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPhoto();
            }
        });
        findViewById(R.id.btn_tag_demo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ratingPhoto(new File(getFilesDir() + "/demo/d_sample.jpg"));
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isProcessing) {
                    isProcessing = false;
                }
            }
        });

        copyDemoAssets();
    }

    private void copyDemoAssets() {
        AssetManager assetManager = getAssets();

        InputStream in;
        OutputStream out;

        String[] files;
        try {
            File demoPath = new File(getFilesDir() + "/demo");
            files = getAssets().list("demo");
            demoPath.mkdirs();

            for (File f : demoPath.listFiles()) {
                f.delete();
            }
            for (String fileName : files) {
                File file = new File(getFilesDir(), "/demo/" + fileName);
                try {
                    in = assetManager.open("demo/" + fileName);
                    out = new FileOutputStream(file);

                    copyFile(in, out);
                    in.close();
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    /**
     * photo folder chooser
     */
    private void selectPhotos(final boolean isFaces, final boolean isBurst) {
        if (!checkAndRequirePermission(isFaces ? REQUEST_PHOTOS_FACE : isBurst ? REQUEST_PHOTOS_BURST : REQUEST_PHOTOS_GROUP)) {
            return;
        }
        new ChooserDialog().with(this)
                .withFilter(true, false)
                .withStartFile(Environment.getExternalStorageDirectory().getPath() + "/DCIM")
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String path, File pathFile) {
                        grouping(pathFile, isFaces, isBurst);
                    }
                })
                .build()
                .show();
    }

    /**
     * photo chooser
     */
    private void selectPhoto() {
        if (!checkAndRequirePermission(REQUEST_PHOTO)) {
            return;
        }

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Photo"), REQUEST_IMPORT_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMPORT_PHOTO) {

                Uri imageUri = data.getData();
                if (imageUri != null) {
                    String filePath = FileUtils.getPath(this, imageUri);

                    ratingPhoto(new File(filePath));
                }
            }
        }

    }

    private boolean checkAndRequirePermission(int permissionRequestId) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    permissionRequestId);

            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PHOTO && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectPhoto();
            }
        } else if ((requestCode == REQUEST_PHOTOS_FACE || requestCode == REQUEST_PHOTOS_GROUP || requestCode == REQUEST_PHOTOS_BURST) && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectPhotos(requestCode == REQUEST_PHOTOS_FACE, requestCode == REQUEST_PHOTOS_BURST);
            }
        }
    }

    /**
     * clear all logs
     */
    private void clearOutput() {
        outputView.setText("");
    }

    /**
     * show output logs
     */
    private void output(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String currentText = outputView.getText().toString();
                if (currentText.length() - LOG_MAX_CHARS > 1000) {
                    currentText = currentText.substring(currentText.length() - LOG_MAX_CHARS, currentText.length());
                }
                currentText += "\n" + text;
                outputView.setText(currentText);

                outputView.post(new Runnable() {
                    @Override
                    public void run() {
                        outputCon.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
    }

    /**
     * show current photo
     */
    private void updateThumbnail(File file) {
        final Bitmap bitmap = ImageUtil.decodeThumbBitmapForFile(file.getPath(), 500, 500);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                thumbnailView.setImageBitmap(bitmap);
            }
        });
    }


    private void updateThumbnail(final Bitmap bitmap) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                thumbnailView.setImageBitmap(bitmap);
            }
        });
    }

    /**
     * processing photo, get photo rating and tagging
     */
    private void ratingPhoto(final File file) {
        clearOutput();
        btnGroupResult.setVisibility(View.GONE);
        autoEnhanceView.setVisibility(View.GONE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                output("start processing...");
                updateThumbnail(file);
                BenchmarkUtil.TimeStart("processingFile");
                BenchmarkUtil.MemStart("processingFile");
                final Map<String, Object> result = Processing.processingFile(MainActivity.this, file.getPath(), false);
                BenchmarkUtil.MemEnd("processingFile");

//                // TODO add senstime interface here
//                List<List<PointF>> facePoints = new ArrayList<>();
//                facePoints.add(Arrays.asList(TEST_SENSTIME_FACE_DET_POINTS));
//                List<Rect> faceRects = new ArrayList<>();
//                faceRects.add(TEST_SENSTIME_FACE_RECT);
//                Processing.computeEmotion(result, facePoints, faceRects, TEST_FACE_DET_WIDTH, TEST_FACE_DET_HEIGHT);

                for (String label : result.keySet()) {
                    if (label.startsWith("metric_")) {
                        String showLable = label.substring("metric_".length());
                        output("\t" + showLable + ": " + result.get(label));

                    }
                }

                thumbnailView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showResult(result, file.getName());
                    }
                });

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showResult(result, file.getName());
                    }
                });

                output("end processing.");
            }
        }).start();
    }

    private void showResult(Map<String, Object> result, String fileName) {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Rating Result");
        alertDialog.setMessage(MainActivity.getRatingDisplayResult(result, false, fileName));
        alertDialog.show();
    }

    public static String getRatingDisplayResult(Map<String, Object> result, boolean isBad, String fileName) {
        StringBuilder sb = new StringBuilder();

        if (isBad) {
            sb.append("Bad Reason:");
            sb.append(result.get("grouping_bad_reason"));
            sb.append("\n");
        }

        sb.append("\n");

        if (result.containsKey("metric_clarity")) {
            sb.append("clarity:");
            sb.append(result.get("metric_clarity"));
            sb.append("\n");
        }
        if (result.containsKey("metric_exposure")) {
            sb.append("exposure:");
            sb.append(result.get("metric_exposure"));
            sb.append("\n");
        }
        if (result.containsKey("metric_colorfulness")) {
            sb.append("colorfulness:");
            sb.append(result.get("metric_colorfulness"));
            sb.append("\n");
        }
        if (result.containsKey("metric_emotion")) {
            float emotion = (float) result.get("metric_emotion");
            if (emotion > 0) {
                sb.append("emotion:");
                sb.append(emotion);
                sb.append("\n");
            }
        }

        if (result.containsKey("rating_all")) {
            sb.append("\n");
            sb.append("Overall:");
            sb.append(result.get("rating_all"));
        }

        if (fileName != null) {
            sb.append("\n");
            sb.append("File:");
            sb.append(new File(fileName).getName());
        }

        return sb.toString();
    }

    /**
     * processing and groupping photos
     *
     * @param folderPath folder of photos
     */
    private void grouping(final File folderPath, final boolean isFaces, final boolean isBurst) {
        if (isProcessing) {
            return;
        }

        clearOutput();
        btnGroupResult.setVisibility(View.GONE);
        autoEnhanceView.setVisibility(View.GONE);

        processing_con.setVisibility(View.VISIBLE);
        btn_cancel.setVisibility(View.VISIBLE);
        isProcessing = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (processingLock) {
                    output("Prepare photos...");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            processing_tv.setText("Prepare photos...");
                        }
                    });
                    File[] photoFiles = folderPath.listFiles();
                    List<File> realPhotos = new ArrayList<>();
                    List<File> nonPhotos = new ArrayList<>();
                    Editable maxStr = grouping_max.getText();

                    int maxProcessing = 30;
                    try {
                        maxProcessing = Integer.parseInt(maxStr.toString());
                    } catch (Exception e) {
                        e.printStackTrace();

                        maxProcessing = 30;
                        final int finalMaxProcessing = maxProcessing;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                grouping_max.setText(String.valueOf(finalMaxProcessing));
                            }
                        });
                    }


                    Editable editStr;

                    editStr = grouping_result_min.getText();

                    int minResult = 5;
                    try {
                        minResult = Integer.parseInt(editStr.toString());
                    } catch (Exception e) {
                        e.printStackTrace();

                        minResult = 5;
                        final int finalMinResult = minResult;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                grouping_result_min.setText(String.valueOf(finalMinResult));
                            }
                        });
                    }

                    editStr = grouping_result_max.getText();

                    int maxResult = 50;
                    try {
                        maxResult = Integer.parseInt(editStr.toString());
                    } catch (Exception e) {
                        e.printStackTrace();

                        maxResult = 50;
                    }

                    maxResult = Math.max(maxResult, minResult);
                    final int finalMaxResult = maxResult;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            grouping_result_max.setText(String.valueOf(finalMaxResult));
                        }
                    });

                    int directFiles = 0;
                    for (File photo : photoFiles) {
                        if (photo.isDirectory()) {
                            directFiles++;
                            continue;
                        }
                        BitmapFactory.Options size = ImageUtil.decodeImageSize(photo.getPath());

                        if (size.outWidth > 0 && size.outHeight > 0) {
                            realPhotos.add(photo);
                        } else {
                            nonPhotos.add(photo);
                        }
                        if (--maxProcessing == 0) {
                            break;
                        }
                    }

                    if (realPhotos.size() < (photoFiles.length - directFiles)) {
                        for (File nonPhoto : nonPhotos) {
                            output("cannot decode:" + nonPhoto.getName());
                        }
                        final int totalCount = photoFiles.length - directFiles;
                        final int photoCount = realPhotos.size();
                        try {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                                    alertDialog.setTitle("Notice");
                                    alertDialog.setMessage(String.format(Locale.ENGLISH, "There are %d files in the folder, only %d/%d can be decoded!\nCheck the not decoded files in the bottom. Need continue?", totalCount, photoCount, totalCount));
                                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Continue", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            synchronized (processingLock) {
                                                processingLock.notify();
                                            }
                                        }
                                    });
                                    alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                                    alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            isProcessing = false;
                                            synchronized (processingLock) {
                                                processingLock.notify();
                                            }
                                        }
                                    });
                                    alertDialog.show();
                                }
                            });
                            processingLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if (realPhotos.isEmpty()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                                alertDialog.setTitle("Notice");
                                alertDialog.setMessage("There is no photo can be proceed!");
                                alertDialog.show();
                            }
                        });
                        isProcessing = false;
                        checkCancel();
                        return;
                    }

                    if (!checkCancel()) {
                        return;
                    }

                    List<Map<String, Object>> features = new ArrayList<>();

                    final int total = realPhotos.size();
                    output("Processing photo features...");
                    int currentIndex = 0;
                    for (File photo : realPhotos) {
                        Bitmap bitmap = ImageUtil.getScaledFitBitmap(photo.getPath(), 300, 300);
                        updateThumbnail(bitmap);
                        output("\tProcessing " + photo.getPath() + "...");

                        long fileCreateTime = ImageUtil.getPhotoCreationTime(photo);


                        final Map<String, Object> featureResult = Processing.processingFile(MainActivity.this, bitmap, fileCreateTime, !isFaces && isBurst);

                        // TODO add senstime interface here
//                        List<List<PointF>> facePoints = new ArrayList<>();
//                        List<Rect> faceRects = new ArrayList<>();
//                        Processing.computeEmotion(featureResult, facePoints, faceRects);

                        if (!isFaces && !isBurst) {
                            bitmap = ImageUtil.getScaledBitmap(photo.getPath(), 224, 224);
                            Map<String, Object> taggingResult = TaggingUtil.tagPhoto(getAssets(), bitmap);
                            bitmap.recycle();
                            featureResult.putAll(taggingResult);
                        }

                        features.add(featureResult);
                        currentIndex++;
                        final int finalIndex = currentIndex;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int progress = finalIndex * 100 / total;
                                processing_tv.setText("Processing photo features, " + finalIndex + "/" + total + " photos...");

                                processing_pb.setProgress(progress);
                            }
                        });

                        if (!checkCancel()) {
                            return;
                        }
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            processing_tv.setText("Grouping photos...");
                        }
                    });

                    output("Grouping photos...");

                    String identifier = "identifier";
                    GroupingResultItem result = null;
                    try {
                        if (isFaces) {
                            result = Processing.processingFaces(features);
                        } else {

                            BenchmarkUtil.TimeStart("processingGrouping");
                            BenchmarkUtil.MemStart("processingGrouping");
                            result = Processing.processingGrouping(identifier, features, isBurst, Float.parseFloat(sensitivity.getText().toString()), new POGenerateHClusterCallbackFunction() {
                                @Override
                                public void progress(final double progress) {
                                    output(String.format(Locale.ENGLISH, "Processing %.2f%%", progress * 100));

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            processing_pb.setProgress((int) (progress * 100));
                                        }
                                    });

                                    if (!checkCancel()) {
                                        throw new IllegalStateException("cancel");
                                    }
                                }
                            });
                            BenchmarkUtil.TimeEnd("processingGrouping");
                            BenchmarkUtil.MemEnd("processingGrouping");
                        }
                    } catch (IllegalStateException e) {
                        if (e.getMessage().equals("cancel")) {
                            return;
                        } else {
                            throw e;
                        }
                    }

                    if (!checkCancel()) {
                        return;
                    }
                    output("Grouping result:" + result.toString());

                    Map<Integer, List<List<Integer>>> groups = result.groups;
                    List<List<Integer>> finalResult;
                    int opt = result.optimalGroupIndex;
                    if (!isFaces && !isBurst) {
                        if (groups.size() < minResult) {
                            finalResult = groups.get(groups.size());
                        } else {
                            if (opt > maxResult) {
                                finalResult = groups.get(opt);
                            } else if (opt < minResult) {
                                finalResult = groups.get(minResult);
                            } else {
                                finalResult = groups.get(opt);
                            }
                        }
                    } else {
                        finalResult = groups.get(opt);
                    }


                    GroupingResult groupingResult = new GroupingResult();
                    List<Integer> grouped = new ArrayList<>();
                    List<List<ResultItem>> groupdFiles = new ArrayList<>();
                    for (List<Integer> subGroup : finalResult) {
                        List<ResultItem> sub = new ArrayList<>();
                        for (Integer index : subGroup) {
                            ResultItem resultItem = new ResultItem();
                            resultItem.filePath = realPhotos.get(index).getPath();
                            resultItem.features = features.get(index);
                            grouped.add(index);

                            sub.add(resultItem);
                        }
                        groupdFiles.add(sub);
                    }

                    if (!isFaces && !isBurst) {
                        List<ResultItem> droppedGroup = new ArrayList<>();
                        if (opt > maxResult) {
                            Processing.sortGroupsByScore(groupdFiles);
                            while (groupdFiles.size() > maxResult) {
                                List<ResultItem> lastGroup = groupdFiles.get(groupdFiles.size() - 1);
                                droppedGroup.addAll(lastGroup);
                                groupdFiles.remove(lastGroup);
                            }
                        }
                        groupingResult.droppedFiles.addAll(droppedGroup);
                    }

                    groupingResult.optFiles.addAll(groupdFiles);

                    for (int i = 0; i < realPhotos.size(); i++) {
                        if (!grouped.contains(i)) {
                            ResultItem resultItem = new ResultItem();
                            resultItem.filePath = realPhotos.get(i).getPath();
                            resultItem.features = features.get(i);

                            groupingResult.badFiles.add(resultItem);
                        }
                    }


                    MemoryCache.put("group_files", groupingResult);

                    output("Optimal group:" + groupingResult.optFiles.toString());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnGroupResult.setVisibility(View.VISIBLE);
                            btnGroupResult.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
//                                    testGrouping();
                                    Intent intent = new Intent(MainActivity.this, GroupingActivity.class);
                                    intent.putExtra("BURST", isBurst);
                                    intent.putExtra("FACE", isFaces);

                                    startActivity(intent);
                                }
                            });
                        }
                    });

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            processing_tv.setText("Processing finished!");
                            btn_cancel.setVisibility(View.GONE);
                        }
                    });

                    output("end processing.\nClick 'Grouping result' button to see the result.");
                    isProcessing = false;
                    processing_con.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            processing_con.setVisibility(View.GONE);
                        }
                    }, 3000);
                }
            }
        }).start();
    }

    private boolean checkCancel() {
        if (!isProcessing) {
            output("Processing canneled!");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    processing_tv.setText("Processing canneled!");
                    btn_cancel.setVisibility(View.GONE);
                }
            });

            processing_con.postDelayed(new Runnable() {
                @Override
                public void run() {
                    processing_con.setVisibility(View.GONE);
                }
            }, 3000);
        }

        return isProcessing;
    }
}
