package co.polarr.albumsdkdemo;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import co.polarr.albumsdkdemo.utils.ImageRenderUtil;
import co.polarr.albumsdkdemo.utils.ImageUtil;
import co.polarr.albumsdkdemo.utils.ScaledImageView;
import co.polarr.processing.entities.ResultItem;

/**
 * Created by Colin on 2017/3/9.
 * picky layout title adapter
 */

public class SingleGroupAdapter extends RecyclerView.Adapter<SingleGroupAdapter.LayoutViewHolder> {
    private static final int MAX_PREVIEW_SIZE = 2048;
    private final List<ResultItem> mPhotos;
    private boolean mIsBad;
    private boolean mIsDropped;
    private Context mContext;
    private LayoutInflater mInflater;

    public SingleGroupAdapter(Context context, List<ResultItem> photoFiles, boolean isBadGroup, boolean isDrppedGroup) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mPhotos = photoFiles;
        mIsBad = isBadGroup;
        mIsDropped = isDrppedGroup;
    }

    @Override
    public LayoutViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.single_group_item, parent, false);

        return new LayoutViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mPhotos.size();
    }

    @Override
    public void onBindViewHolder(LayoutViewHolder holder, int position) {
        holder.updateView(position);
    }

    class LayoutViewHolder extends RecyclerView.ViewHolder {
        private TextView score_tv;
        private View star_iv;
        private ImageView photoCon;

        public LayoutViewHolder(View itemView) {
            super(itemView);
            photoCon = (ImageView) itemView.findViewById(R.id.iv_photo);
            score_tv = (TextView) itemView.findViewById(R.id.score_tv);
            star_iv = itemView.findViewById(R.id.star_iv);
        }

        void updateView(int index) {
            if (index == 0 && !mIsBad && !mIsDropped) {
                star_iv.setVisibility(View.VISIBLE);
            } else {
                star_iv.setVisibility(View.INVISIBLE);
            }
            final String photoPath = mPhotos.get(index).filePath;
            final Map<String, Object> features = mPhotos.get(index).features;
            if (features.containsKey("aesthetics_score")) {
                score_tv.setText(String.format(Locale.ENGLISH, "Score: %.2f", (float) features.get("aesthetics_score") * 100));
            } else {
                String scoreStr = String.format(Locale.ENGLISH, "%.1f|%.1f|%.1f",
                        (float) features.get("metric_clarity"),
                        (float) features.get("metric_exposure"),
                        (float) features.get("metric_colorfulness"));
                score_tv.setText(scoreStr);
            }
            score_tv.setVisibility(View.GONE);
            photoCon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                            .setPositiveButton("Show Original", null)
                            .create();
                    alertDialog.setTitle("Rating Result");
                    alertDialog.setMessage(MainActivity.getRatingDisplayResult(features, mIsBad, photoPath));
                    alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
                            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    showImage(photoPath);
                                }
                            });
                        }
                    });
                    alertDialog.show();
                }
            });
            ImageRenderUtil.load().setPath(photoPath).setNeedRecyle(false).setSize(200, 200).into(photoCon);
        }
    }


    public void showImage(String photoPath) {
//        final AlertDialog dialog = new AlertDialog.Builder(mContext).create();
//        ImageView imgView = getView();
//        dialog.setView(imgView);
//        dialog.show();
        final Dialog dialog = new Dialog(mContext, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        ImageView imgView = getView(photoPath);
        dialog.setContentView(imgView);
        dialog.show();

//        imgView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
    }


    private ScaledImageView getView(String photoPath) {
        ScaledImageView imgView = new ScaledImageView(mContext);
        imgView.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
        imgView.setImageBitmap(ImageUtil.decodeThumbBitmapForFile(photoPath, MAX_PREVIEW_SIZE, MAX_PREVIEW_SIZE));

        return imgView;
    }
}
