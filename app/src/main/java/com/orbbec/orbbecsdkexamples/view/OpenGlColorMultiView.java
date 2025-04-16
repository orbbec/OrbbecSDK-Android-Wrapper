package com.orbbec.orbbecsdkexamples.view;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES10;
import android.opengl.GLES11;
import android.opengl.GLES11Ext;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenGlColorMultiView extends GLSurfaceView {
    protected int mSurfaceWidth = 0;
    protected int mSurfaceHeight = 0;

    protected ByteBuffer mTexture=null;
    protected int mTextureId = 0;

    private int mDiffDrawTime = 0;

    private int mCurrFrameWidth = 0;
    private int mCurrFrameHeight = 0;

    private int mBaseColor = Color.WHITE;
    private static final String TAG = "OpenNIView";

    private int mVersionInt;
    public OpenGlColorMultiView(Context context) {
        super(context);
        init();
    }

    public OpenGlColorMultiView(Context context, AttributeSet attrs){
        super( context,  attrs);
        init();
    }

    private void init() {

        mVersionInt = Build.VERSION.SDK_INT;
        setRenderer(new Renderer() {

            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig c) {
                /* Disable these capabilities. */
                final int[] gCapbilitiesToDisable = {
                        GLES10.GL_FOG,
                        GLES10.GL_LIGHTING,
                        GLES10.GL_CULL_FACE,
                        GLES10.GL_ALPHA_TEST,
                        GLES10.GL_BLEND,
                        GLES10.GL_COLOR_LOGIC_OP,
                        GLES10.GL_DITHER,
                        GLES10.GL_STENCIL_TEST,
                        GLES10.GL_DEPTH_TEST,
                        GLES10.GL_COLOR_MATERIAL,
                };

                for (int capability : gCapbilitiesToDisable) {
                    GLES10.glDisable(capability);
                }

                GLES10.glEnable(GLES10.GL_TEXTURE_2D);

                int[] ids = new int[1];
                GLES10.glGenTextures(1, ids, 0);
                mTextureId = ids[0];
                GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, mTextureId);

                GLES10.glTexParameterf(GLES10.GL_TEXTURE_2D, GLES10.GL_TEXTURE_MIN_FILTER, GLES10.GL_LINEAR);
                GLES10.glTexParameterf(GLES10.GL_TEXTURE_2D, GLES10.GL_TEXTURE_MAG_FILTER, GLES10.GL_LINEAR);
                GLES10.glShadeModel(GLES10.GL_FLAT);
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int w, int h) {
                synchronized (OpenGlColorMultiView.this) {
                    mSurfaceWidth = w;
                    mSurfaceHeight = h;
                }


            }

            @Override
            public void onDrawFrame(GL10 gl) {
                synchronized (OpenGlColorMultiView.this) {
                    long beforeDrawTime = System.currentTimeMillis();
                    onDrawGL();
                    long afterDrawTime = System.currentTimeMillis();

                    mDiffDrawTime = (int)(afterDrawTime - beforeDrawTime);

                }
            }
        });

        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

//    public void update(int width, int height, ShortBuffer depthData){
//        if (mTexture == null) {
//            mCurrFrameWidth = width;
//            mCurrFrameHeight = height;
//            // need to reallocate texture
//            mTexture = ByteBuffer.allocateDirect(mCurrFrameWidth * mCurrFrameHeight * 3);
//            mTexture.order(ByteOrder.nativeOrder());
//            Log.v(TAG, "mTexture: " + mTexture.limit());
//        }
//        NativeUtils.ConvertTORGB(depthData, mTexture, width, height, 0);
//        requestRender();
//    }

    /**
     * Requests updateDepth of the view with an OpenNI frame.
     */
    public void update(int w, int h, byte[] texture) {

//        Log.i(TAG, "updateDepth() thread = " + Thread.currentThread().getName());
        //if (mTexture == null) {
            mCurrFrameWidth = w;
            mCurrFrameHeight = h;
            // need to reallocate texture
//            mTexture = ByteBuffer.allocateDirect(mCurrFrameWidth * mCurrFrameHeight * 3);
//            mTexture.order(ByteOrder.nativeOrder());
         //   Log.v(TAG, "mTexture: " + mTexture.limit());
       // }
//        NativeUtils.ByteBufferCopy(texture, mTexture, w * h * 3);
        mTexture = ByteBuffer.wrap(texture);
        requestRender();
    }

    protected void onDrawGL() {
        if (mTexture == null || mSurfaceWidth == 0 || mSurfaceHeight == 0) {
            return;
        }

        GLES10.glEnable(GLES10.GL_BLEND);
        GLES10.glBlendFunc(GLES10.GL_SRC_ALPHA, GLES10.GL_ONE_MINUS_SRC_ALPHA);
        int red = Color.red(mBaseColor);
        int green = Color.green(mBaseColor);
        int blue = Color.blue(mBaseColor);
        int alpha = Color.alpha(mBaseColor);
        GLES10.glColor4f(red/255.f, green/255.f, blue/255.f, alpha/255.f);

        GLES10.glEnable(GLES10.GL_TEXTURE_2D);

        GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, mTextureId);
        int[] rect = {0, mCurrFrameHeight, mCurrFrameWidth, -mCurrFrameHeight};
        GLES11.glTexParameteriv(GLES10.GL_TEXTURE_2D, GLES11Ext.GL_TEXTURE_CROP_RECT_OES, rect, 0);
        GLES10.glClear(GLES10.GL_COLOR_BUFFER_BIT);

        GLES10.glTexImage2D(GLES10.GL_TEXTURE_2D, 0, GLES10.GL_RGB, mCurrFrameWidth, mCurrFrameHeight, 0, GLES10.GL_RGB,
                GLES10.GL_UNSIGNED_BYTE, mTexture);
        float ratioW = 1f * mCurrFrameWidth / mSurfaceWidth;
        float ratioH = 1f * mCurrFrameHeight / mSurfaceHeight;
        int viewWidth = mSurfaceWidth;
        int viewHeight = mSurfaceHeight;
        if (ratioW > ratioH) {
            viewHeight = (int) (mSurfaceHeight / ratioW);
        } else {
            viewWidth = (int) (mSurfaceWidth / ratioH) * 2;
        }
        Log.d(TAG, "onDrawGL: mSurfaceWidth = " + mSurfaceWidth + ", mSurfaceHeight = " + mSurfaceHeight);
        Log.d(TAG, "onDrawGL: viewWidth = " + viewWidth + ", viewHeight = " + viewHeight);
        GLES11Ext.glDrawTexiOES(0, 0, 0, viewWidth, viewHeight);

        GLES10.glDisable(GLES10.GL_TEXTURE_2D);
    }

    public int getDiffDrawTime(){
        return mDiffDrawTime;
    }

}
