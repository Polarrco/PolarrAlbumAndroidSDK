package co.polarr.albumsdkdemo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import co.polarr.albumsdkdemo.R;

/**
 * Created by Colin on 2017/3/21.
 * render bitmap for imageviews
 */

public class ImageRenderUtil {
    private static Handler mainHandler;

    private synchronized static Handler getMainHandler() {
        if (mainHandler == null) {
            synchronized (ImageRenderUtil.class) {
                if (mainHandler == null) {
                    mainHandler = new Handler(Looper.getMainLooper());
                }
            }
        }

        return mainHandler;
    }

    /**
     * render thread pool 4 core threads
     */
    private static final ThreadPoolExecutor renderPool = new ThreadPoolExecutor(4, 4, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    static class ImageRender {
        /**
         * local file path
         */
        private String filePath;
        private Context context;
        /**
         * target max width
         */
        public int width;
        /**
         * target max height
         */
        public int height;
        /**
         * image view to be set
         */
        private ImageView imageView;
        /**
         * temp bitmap object
         */
        private Bitmap bitmap;

        /**
         * decode task
         */
        private Runnable decodeRunnable;
        /**
         * render task in main thread
         */
        private Runnable setRunnable;
        /**
         * stop render mark
         */
        private boolean isCancel = false;
        public boolean needRecyle;

        /**
         * start working
         */
        private void load() {
            decodeRunnable = new Runnable() {
                @Override
                public void run() {
                    // do decode
                    if (filePath != null) {
                        bitmap = ImageUtil.decodeThumbBitmapForFile(filePath, width, height);
                    }

                    // check if cancel
                    if (isCancel && bitmap != null && !bitmap.isRecycled()) {
                        bitmap.recycle();
                    }

                    setRunnable = new Runnable() {
                        @Override
                        public void run() {
                            // check if cancel
                            if (isCancel && bitmap != null && !bitmap.isRecycled()) {
                                bitmap.recycle();
                            }

                            /** do set image bitmap */
                            if (imageView != null && bitmap != null && !bitmap.isRecycled()) {
                                imageView.setImageBitmap(bitmap);

                                //appear animation
                                AlphaAnimation showAnimation = new AlphaAnimation(0.0f, 1.0f);
                                showAnimation.setDuration(500);
                                showAnimation.setFillAfter(true);
                                imageView.startAnimation(showAnimation);

                                imageView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                                    @Override
                                    public void onViewAttachedToWindow(View v) {

                                    }

                                    @Override
                                    public void onViewDetachedFromWindow(View v) {
                                        if (needRecyle) {
                                            /** recyle bitmap if it detached from screen */
                                            if (bitmap != null && !bitmap.isRecycled()) {
                                                bitmap.recycle();
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    };
                    getMainHandler().post(setRunnable);
                }
            };
            /** real run the task*/
            renderPool.execute(decodeRunnable);
        }

        /**
         * cancel task, will remove from queue and recyle
         */
        public void cancel() {
            isCancel = true;
            if (decodeRunnable != null) {
                renderPool.remove(decodeRunnable);
                decodeRunnable = null;
            }
            if (setRunnable != null) {
                getMainHandler().removeCallbacks(setRunnable);
                setRunnable = null;
            }
        }
    }

    /**
     * Builder structure
     */
    public static class Builder {
        private String filePath;
        private int width;
        private int height;
        private Context context;
        private boolean needRecyle = true;

        /**
         * image file path
         */
        public Builder setPath(String filePath) {
            this.filePath = filePath;

            return this;
        }

        public Builder setNeedRecyle(boolean needRecyle) {
            this.needRecyle = needRecyle;

            return this;
        }

        /**
         * target image size
         */
        public Builder setSize(int width, int height) {
            this.width = width;
            this.height = height;

            return this;
        }

        /**
         * instance call
         */
        private ImageRender build() {
            ImageRender imageRender = new ImageRender();
            imageRender.filePath = filePath;
            imageRender.context = context;
            imageRender.width = width;
            imageRender.height = height;
            imageRender.needRecyle = needRecyle;

            return imageRender;
        }

        /**
         * do image load
         */
        public ImageRender into(ImageView imageView) {
            ImageRender render = (ImageRender) imageView.getTag(R.id.image_render_tag);
            if (render != null) {
                render.cancel();
            }
            imageView.setImageBitmap(null);
            render = build();
            render.imageView = imageView;
            imageView.setTag(R.id.image_render_tag, render);
            render.load();

            return render;
        }
    }

    /**
     * create an instance easily
     */
    public static Builder load() {
        return new Builder();
    }
}
