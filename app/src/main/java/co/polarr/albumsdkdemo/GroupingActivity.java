package co.polarr.albumsdkdemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import co.polarr.albumsdkdemo.entities.GroupingResult;
import co.polarr.albumsdkdemo.utils.ExportUtil;
import co.polarr.albumsdkdemo.utils.MemoryCache;
import co.polarr.processing.Processing;
import co.polarr.processing.entities.ResultItem;

public class GroupingActivity extends AppCompatActivity {

    private RecyclerView groupCon;
    private GroupingResult photoFiles;
    private TextView tv_sortby;
    private boolean isBurst;
    private boolean isFaces;
    private ResultItem bestItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grouping);

        groupCon = (RecyclerView) findViewById(R.id.rv_photos);
        tv_sortby = (TextView) findViewById(R.id.tv_sortby);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(GroupingActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        groupCon.setLayoutManager(linearLayoutManager);

        photoFiles = (GroupingResult) MemoryCache.get("group_files");

        tv_sortby.setVisibility(View.VISIBLE);

        isBurst = getIntent().getBooleanExtra("BURST", false);
        isFaces = getIntent().getBooleanExtra("FACE", false);

        if (isBurst) {
            tv_sortby.setText("Burst mode.");

        } else if (isFaces) {
            tv_sortby.setText("Faces mode.");
        } else {
            tv_sortby.setText("Sorted by rating.");
        }
        if (isFaces) {
            bestItem = photoFiles.optFiles.get(0).get(0);
        } else if (isBurst) {
            bestItem = null;
        } else {
            bestItem = Processing.getBest(photoFiles.optFiles);
            Processing.sortGroupsByScore(photoFiles.optFiles);
        }
        groupCon.setAdapter(new GroupPhotoAdapter(GroupingActivity.this, photoFiles, bestItem));
    }

    public void buttonHandle(View view) {
        switch (view.getId()) {
            case R.id.btn_sort_score:
                Processing.sortGroupsByScore(photoFiles.optFiles);
                groupCon.setAdapter(new GroupPhotoAdapter(GroupingActivity.this, photoFiles, bestItem));
                tv_sortby.setVisibility(View.VISIBLE);
                if (isBurst) {
                    tv_sortby.setText("BrustMode by: rating");
                } else {
                    tv_sortby.setText("Sorted by: rating");
                }
//                showNotice();
                break;
            case R.id.btn_sort_time:
                Processing.sortGroupsByTime(photoFiles.optFiles);
                groupCon.setAdapter(new GroupPhotoAdapter(GroupingActivity.this, photoFiles, bestItem));
                tv_sortby.setVisibility(View.VISIBLE);
                if (isBurst) {
                    tv_sortby.setText("BrustMode by: time");
                } else {
                    tv_sortby.setText("Sorted by: time");
                }
//                showNotice();
                break;
            case R.id.btn_export:
                List<ResultItem> allItems = new ArrayList<>();
                for (List<ResultItem> optItem : photoFiles.optFiles) {
                    allItems.addAll(optItem);
                }
                allItems.addAll(photoFiles.badFiles);
                allItems.addAll(photoFiles.droppedFiles);

                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                File path = new File(getFilesDir(), "files");
                if(!path.exists()) {
                    path.mkdir();
                }
                File targetFile = new File(path, "A+SDK_Export.csv");
                ExportUtil.ExportToCsv(allItems, targetFile);
                Uri uri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".GroupingActivity", targetFile);

                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.setType("*/*");
                startActivity(Intent.createChooser(shareIntent, "Send to..."));
                break;
        }
    }
}
