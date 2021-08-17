package co.polarr.albumsdkdemo;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import co.polarr.albumsdkdemo.entities.GroupingResult;
import co.polarr.processing.entities.ResultItem;

/**
 * Created by Colin on 2017/3/9.
 * picky layout title adapter
 */

public class GroupPhotoAdapter extends RecyclerView.Adapter<GroupPhotoAdapter.LayoutViewHolder> {
    private final GroupingResult mPhotoFiles;
    private ResultItem mBestItem;
    private Context mContext;
    private LayoutInflater mInflater;

    public GroupPhotoAdapter(Context context, GroupingResult photoFiles, ResultItem bestItem) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mPhotoFiles = photoFiles;
        mBestItem = bestItem;
    }

    @Override
    public LayoutViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.group_photo_item, parent, false);

        return new LayoutViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mPhotoFiles.optFiles.size() + (mPhotoFiles.badFiles.isEmpty() ? 0 : 1)
                + (mPhotoFiles.droppedFiles.isEmpty() ? 0 : 1) + (mBestItem != null ? 1 : 0);
    }

    @Override
    public void onBindViewHolder(LayoutViewHolder holder, int position) {
        holder.updateView(position);
    }

    class LayoutViewHolder extends RecyclerView.ViewHolder {
        private RecyclerView photosCon;
        private TextView tvGroupName;

        public LayoutViewHolder(View itemView) {
            super(itemView);
            photosCon = (RecyclerView) itemView.findViewById(R.id.rv_photos);
            tvGroupName = (TextView) itemView.findViewById(R.id.tv_groupName);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            photosCon.setLayoutManager(linearLayoutManager);
        }

        void updateView(int index) {
            boolean isBadGroup = false;
            boolean isDrppedGroup = false;
            List<ResultItem> files;

            if (index == 0 && mBestItem != null) {
                files = new ArrayList<>();
                files.add(mBestItem);
                tvGroupName.setText("The best photo");
            } else {
                if (mBestItem != null) {
                    index--;
                }
                if (index < mPhotoFiles.optFiles.size()) {
                    files = mPhotoFiles.optFiles.get(index);
                    tvGroupName.setText(String.format(Locale.ENGLISH, "Group %d, total: %d photos:", index + 1, files.size()));
                } else if (index == mPhotoFiles.optFiles.size() && !mPhotoFiles.badFiles.isEmpty()) {
                    files = mPhotoFiles.badFiles;
                    tvGroupName.setText(String.format(Locale.ENGLISH, "Bad group %d photos:", files.size()));
                    isBadGroup = true;
                } else {
                    files = mPhotoFiles.droppedFiles;
                    tvGroupName.setText(String.format(Locale.ENGLISH, "Dropped group %d photos:", files.size()));
                    isDrppedGroup = true;
                }
            }

            SingleGroupAdapter singleGroupAdapter = new SingleGroupAdapter(mContext, files, isBadGroup, isDrppedGroup);
            photosCon.setAdapter(singleGroupAdapter);
        }
    }
}
