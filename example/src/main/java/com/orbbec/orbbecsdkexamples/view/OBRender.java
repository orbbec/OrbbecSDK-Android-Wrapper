package com.orbbec.orbbecsdkexamples.view;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.libyuv.util.YuvUtil;
import com.orbbec.obsensor.Format;
import com.orbbec.obsensor.StreamType;
import com.orbbec.orbbecsdkexamples.utils.GlUtil;
import com.orbbec.orbbecsdkexamples.utils.ImageUtils;

import java.lang.ref.WeakReference;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OBRender implements GLSurfaceView.Renderer {
    private static final String TAG = "OBPoseRender";

    private WeakReference<GLSurfaceView> mSurfaceView;

    private int mProgramHandle;
    private int maPositionLoc;
    private int maTextureCoordLoc;
    private int muTexture0Loc;

    private int mTextureColor;

    private float mRenderFps = 0;
    private long mFrameCount = 0;
    private long mLastTime = System.nanoTime();

    private ByteBuffer mDecodeBuffer;
    private ByteBuffer mRenderBuffer;
    private ByteBuffer mSrcDataBuffer;

    private int mWidth;
    private int mHeight;

    private Object mRenderLock = new Object();

    private static final String VERTEX_SHADER =
            "attribute vec4 aPosition;\n" +
                    "attribute vec2 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "    gl_Position = aPosition;\n" +
                    "    vTextureCoord = aTextureCoord.xy;\n" +
                    "}\n";

    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform sampler2D uTexture0;\n" +
                    "void main(){\n" +
                    "    vec4 color = texture2D(uTexture0, vTextureCoord);\n" +
                    "    gl_FragColor = color;\n" +
                    "}\n";

    private static final float[] VERTEX = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
    };

    private static final float[] TEXTURE = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
    };

    private static final FloatBuffer mVertexArray = GlUtil.createFloatBuffer(VERTEX);
    private static final FloatBuffer mTexCoordArray = GlUtil.createFloatBuffer(TEXTURE);

    public OBRender(GLSurfaceView surfaceView) {
        mSurfaceView = new WeakReference<>(surfaceView);
    }

    /**
     * Clear window
     */
    public void clearWindow() {
        synchronized (mRenderLock) {
            mDecodeBuffer = ByteBuffer.allocateDirect(mWidth * mHeight * 3);
            mRenderBuffer = ByteBuffer.allocateDirect(mWidth * mHeight * 3);
        }
        requestRender(mWidth, mHeight);
        mRenderFps = 0;
    }

    public void update(int w, int h, StreamType type, Format format, byte[] data, float scale) {
        decodeToRgb(w, h, type, format, data, scale);
        requestRender(w, h);
    }

    public void update(int w, int h, StreamType type, Format format, ByteBuffer buffer, float scale) {
        decodeToRgb(w, h, type, format, buffer, scale);
        requestRender(w, h);
    }

    private void decodeProcess(int w, int h, StreamType type, Format format) {
        switch (type) {
            case COLOR:
                decodeColorFrame(w, h, format);
                break;
            case DEPTH:
                decodeDepthFrame(w, h, format);
                break;
            case IR:
                decodeIrFrame(w, h, format);
                break;
            default:
                Log.w(TAG, "decodeToRgb: unsupported stream type!");
                break;
        }
    }

    private void decodeToRgb(int w, int h, StreamType type, Format format, ByteBuffer data, float scale) {
        checkBuffers(w, h, data.capacity());
        mSrcDataBuffer = data;
        if (type == StreamType.DEPTH) {
            ImageUtils.nScalePrecisionToDepthPixel(mSrcDataBuffer, w, h, data.capacity(), scale);
        }
        decodeProcess(w, h, type, format);
    }

    private void decodeToRgb(int w, int h, StreamType type, Format format, byte[] data, float scale) {
        checkBuffers(w, h, data.length);
        mSrcDataBuffer.put(data);
        mSrcDataBuffer.flip();
        if (type == StreamType.DEPTH) {
            ImageUtils.nScalePrecisionToDepthPixel(mSrcDataBuffer, w, h, data.length, scale);
        }
        decodeProcess(w, h, type, format);
    }

    private void requestRender(int w, int h) {
        synchronized (mRenderLock) {
            GLSurfaceView glSurfaceView = mSurfaceView.get();
            if (null == glSurfaceView) {
                Log.e(TAG, "drawFrame: the glSurfaceView is null!");
                return;
            }

            glSurfaceView.queueEvent(() -> {
                calculateFps();
                loadTexture(mRenderBuffer, w, h, GLES20.GL_RGB, GLES20.GL_RGB, mTextureColor);
            });
            glSurfaceView.requestRender();
        }
    }

    /**
     * Convert IR image to rgb format
     *
     * @param w      width
     * @param h      height
     * @param format Format
     */
    private void decodeIrFrame(int w, int h, Format format) {
        if (format == Format.Y8) {
            ImageUtils.y8ToRgb(mSrcDataBuffer, mDecodeBuffer, w, h);
        } else {
            YuvUtil.ir2RGB888(mSrcDataBuffer, mDecodeBuffer, w, h, mSrcDataBuffer.capacity());
        }
        synchronized (mRenderLock) {
            mRenderBuffer.put(mDecodeBuffer);
            mRenderBuffer.flip();
        }
    }

    private void checkBuffers(int w, int h, int srcDataSize) {
        synchronized (mRenderLock) {
            int rgbSize = w * h * 3;
            if (null == mDecodeBuffer || mDecodeBuffer.capacity() != rgbSize) {
                mDecodeBuffer = ByteBuffer.allocateDirect(rgbSize);
                mRenderBuffer = ByteBuffer.allocateDirect(rgbSize);

                mWidth = w;
                mHeight = h;
            }

            if (null == mSrcDataBuffer || mSrcDataBuffer.capacity() != srcDataSize) {
                mSrcDataBuffer = ByteBuffer.allocateDirect(srcDataSize);
            }
            mDecodeBuffer.clear();
            mRenderBuffer.clear();
            mSrcDataBuffer.clear();
        }
    }

    /**
     * Convert depth map to rgb format
     *
     * @param w      width
     * @param h      height
     * @param format Format
     */
    private void decodeDepthFrame(int w, int h, Format format) {
        ImageUtils.depthToRgb(mSrcDataBuffer, mDecodeBuffer);
        synchronized (mRenderLock) {
            mRenderBuffer.put(mDecodeBuffer);
            mRenderBuffer.flip();
        }
    }

    /**
     * Convert color grid to rgb format
     *
     * @param w      width
     * @param h      height
     * @param format Format
     */
    private void decodeColorFrame(int w, int h, Format format) {
        switch (format) {
            case RGB888:
                synchronized (mRenderLock) {
                    mRenderBuffer.put(mSrcDataBuffer);
                    mRenderBuffer.flip();
                }
                break;
            case YUYV:
                YuvUtil.yuyv2Rgb888(mSrcDataBuffer, mDecodeBuffer, w * h * 2);
                synchronized (mRenderLock) {
                    mRenderBuffer.put(mDecodeBuffer);
                    mRenderBuffer.flip();
                }
                break;
            case UYVY:
                ImageUtils.uyvyToRgb(mSrcDataBuffer, mDecodeBuffer, w, h);
                synchronized (mRenderLock) {
                    mRenderBuffer.put(mDecodeBuffer);
                    mRenderBuffer.flip();
                }
                break;
            default:
                Log.w(TAG, "decodeColorFrame: unsupported format:" + format);
                break;
        }
    }

    /**
     * Get the rendering frame rate
     *
     * @return render rate
     */
    public float getRenderRate() {
        return mRenderFps;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        if (mProgramHandle == 0) {
            Log.d(TAG, "createProgram failed");
            return;
        }

        maPositionLoc = GLES20.glGetAttribLocation(mProgramHandle, "aPosition");
        GlUtil.checkLocation(maPositionLoc, "aPosition");

        maTextureCoordLoc = GLES20.glGetAttribLocation(mProgramHandle, "aTextureCoord");
        GlUtil.checkLocation(maTextureCoordLoc, "aTextureCoord");
        muTexture0Loc = GLES20.glGetUniformLocation(mProgramHandle, "uTexture0");
        GlUtil.checkLocation(muTexture0Loc, "uTexture0");

        mTextureColor = createTexture();
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        GLES20.glUseProgram(mProgramHandle);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureColor);
        GLES20.glUniform1i(muTexture0Loc, 0);

        GLES20.glEnableVertexAttribArray(maPositionLoc);
        GLES20.glVertexAttribPointer(maPositionLoc, 2, GLES20.GL_FLOAT, false, 0, mVertexArray);

        GLES20.glEnableVertexAttribArray(maTextureCoordLoc);
        GLES20.glVertexAttribPointer(maTextureCoordLoc, 2, GLES20.GL_FLOAT, false, 0, mTexCoordArray);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(maPositionLoc);
        GLES20.glDisableVertexAttribArray(maTextureCoordLoc);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);
    }

    private int createTexture() {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        return textures[0];
    }

    private void loadTexture(Buffer data, int width, int height, int internalFormat, int format, int usedTexId) {
        try {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, usedTexId);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, GLES20.GL_UNSIGNED_BYTE, data);
            GlUtil.checkGlError("glTexImage2D");
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateTexture(Buffer data, int width, int height, int format, int usedTexId) {
        try {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, usedTexId);
            GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, width, height, format, GLES20.GL_UNSIGNED_BYTE, data);
            GlUtil.checkGlError("glTexSubImage2D");
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Statistics Rendering Frame Rate
     */
    private void calculateFps() {
        // calculate fps
        mFrameCount++;
        if (mFrameCount == 30) {
            long now = System.nanoTime();
            long diff = now - mLastTime;
            mRenderFps = (float) (1e9 * mFrameCount / diff);
            mFrameCount = 0;
            mLastTime = now;
        }
    }
}
