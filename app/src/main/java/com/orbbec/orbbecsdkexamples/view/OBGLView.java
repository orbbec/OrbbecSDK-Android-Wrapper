package com.orbbec.orbbecsdkexamples.view;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;

import com.orbbec.obsensor.types.Format;
import com.orbbec.obsensor.types.StreamType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;

public class OBGLView extends GLSurfaceView {
    private static final String TAG = "OBGLView";

    private OBRender mOBRender;
    private Handler mHandler;

    public OBGLView(Context context) {
        super(context);
        init();
    }

    public OBGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setEGLContextClientVersion(2);
        mOBRender = new OBRender(this);
        setRenderer(mOBRender);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        mHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Render frame
     *
     * @param w      width
     * @param h      height
     * @param type   Stream type {@link StreamType#COLOR}/{@link StreamType#DEPTH}/{@link StreamType#IR}
     * @param format Frame Format {@link Format}
     * @param data   Frame data
     */
    public void update(int w, int h, StreamType type, Format format, byte[] data, float scale) {
        if (checkViewSize(w, h) && null != mOBRender) {
            mOBRender.update(w, h, type, format, data, scale);
        }
    }

    /**
     * Render frame
     *
     * @param w      width
     * @param h      height
     * @param type   Stream type {@link StreamType#COLOR}/{@link StreamType#DEPTH}/{@link StreamType#IR}
     * @param format Frame Format{@link Format}
     * @param buffer Frame data
     */
    public void update(int w, int h, StreamType type, Format format, ByteBuffer buffer, float scale) {
        if (checkViewSize(w, h) && null != mOBRender) {
            mOBRender.update(w, h, type, format, buffer, scale);
        }
    }

    /**
     * Clear window
     */
    public void clearWindow() {
        if (null != mOBRender) {
            mOBRender.clearWindow();
        }
    }

    /**
     * Check whether the aspect ratio of the view is consistent with the rendered frame
     *
     * @param frameW frame width
     * @param frameH frame height
     * @return true: same size, false: frame size not equal.
     */
    private boolean checkViewSize(int frameW, int frameH) {
        int viewW = this.getWidth();
        int viewH = this.getHeight();
        float viewScale = 0;
        if (0 != viewW && 0 != viewH) {
            viewScale = new BigDecimal(viewW * 1.0f / viewH).setScale(2, RoundingMode.HALF_UP).floatValue();
        }
        float frameScale = new BigDecimal(frameW * 1.0f / frameH).setScale(2, RoundingMode.HALF_UP).floatValue();
        if (Math.abs(viewScale - frameScale) >= 0.05f) {
            Log.i(TAG, "checkViewSize: view size:" + viewW + "x" + viewH + " view scale:" + viewScale
                    + " frameSize:" + frameW + "x" + frameH + " frameScale:" + frameScale);
            if (null != mHandler) {
                mHandler.post(() -> updateGLSurfaceViewSize(frameW, frameH));
            }
            return false;
        }
        return true;
    }

    /**
     * Update the size of the gl view window in real time according to the frame resolution to ensure
     * proportional rendering
     *
     * @param frameW frame width
     * @param frameH frame height
     */
    private void updateGLSurfaceViewSize(int frameW, int frameH) {
        ViewGroup parent = (ViewGroup) this.getParent();
        if (null == parent || parent.getWidth() <= 0 || parent.getHeight() <= 0
                || frameH <= 0 || frameW <= 0) {
            Log.w(TAG, "Invalid layout parameters - parent:" + parent
                    + " parentSize:[" + (parent != null ? parent.getWidth() : 0) + "x" + (parent != null ? parent.getHeight() : 0) + "]"
                    + " frameSize:[" + frameW + "x" + frameH + "]");
            return;
        }

        // Consistent width, proportional stretch height
        int w1 = parent.getWidth();
        int h1 = parent.getWidth() * frameH / frameW;

        // Consistent height, proportional width stretch
        int h2 = parent.getHeight();
        int w2 = parent.getHeight() * frameW / frameH;

        int targetWidth, targetHeight;
        if (h1 <= parent.getHeight()) {
            targetWidth = w1;
            targetHeight = h1;
        } else {
            targetWidth = w2;
            targetHeight = h2;
        }

        ViewGroup.LayoutParams params = this.getLayoutParams();
        params.width = targetWidth;
        params.height = targetHeight;
        this.setLayoutParams(params);
    }
}
