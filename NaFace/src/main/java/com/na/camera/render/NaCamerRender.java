package com.na.camera.render;

import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.view.SurfaceHolder;

import com.na.camera.INaCameraEvent;
import com.na.camera.INaSurfaceHelper;
import com.na.camera.NaCameraEngineManager;
import com.na.camera.filter.NaCameraFilter;
import com.na.camera.filter.gpuimage.GPUImageFilter;
import com.na.camera.glutils.OpenGLUtils;
import com.na.camera.glutils.TextureRotationUtil;
import com.na.uitls.NaLog;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @actor:taotao
 * @DATE: 17/2/28
 */

public class NaCamerRender implements Renderer, SurfaceHolder.Callback, INaSurfaceHelper, INaCameraEvent{

    public final static int CAMERA_BACK = 0;
    public final static int CAMERA_FRONT = 1;
    private static final String TAG = "NaCamerRender";

    private GLSurfaceView mGLSurfaceView;

    private NaCameraFilter mCameraFilter;
    private GPUImageFilter mFilter;

    /**
     * 顶点坐标
     */
    protected FloatBuffer gLCubeBuffer;

    /**
     * 纹理坐标
     */
    protected FloatBuffer gLTextureBuffer;

    private int mImageWidth;
    private int mImageHeight;

    private int mSurfaceViewWidth;
    private int mSurfaceViewHeight;

    private ByteBuffer mRGBABuffer;

    private int mCameraId = CAMERA_FRONT;

    protected ScaleType mScaleType = ScaleType.FIT_XY;

    private SurfaceTexture mSurfaceTexture;
    private int mTextureId = OpenGLUtils.NO_TEXTURE;

    private long mStartTime = 0;

    private OnFrameAvailableListener mOnFrameAvailableListener = new OnFrameAvailableListener() {
        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            if (mGLSurfaceView != null){
                mGLSurfaceView.requestRender();
            }
        }
    };

    public NaCamerRender(GLSurfaceView gLSurfaceView) {
        this.mGLSurfaceView = gLSurfaceView;
        initGLSurfaceView();
    }

    private void initCameraFilter() {
        mCameraFilter = new NaCameraFilter();
        mCameraFilter.init();
        mFilter = new GPUImageFilter();
        mFilter.init();
    }

    private void initGLSurfaceView(){
        this.mGLSurfaceView.setEGLContextClientVersion(2);
        this.mGLSurfaceView.setRenderer(this);
        this.mGLSurfaceView.getHolder().addCallback(this);
        this.mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        gLCubeBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        gLCubeBuffer.put(TextureRotationUtil.CUBE).position(0);

        gLTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        gLTextureBuffer.put(TextureRotationUtil.TEXTURE_NO_ROTATION).position(0);
    }

    private void openCamera() {
        NaCameraEngineManager.getInstance().setSurfaceHelper(this);
        NaCameraEngineManager.getInstance().setCameraEvent(this);
        NaCameraEngineManager.getInstance().openCamera(mCameraId);
    }

    private void closeCamera(){
        NaCameraEngineManager.getInstance().closeCamera();
    }

    private void initSurfaceTexture(){
        if (mTextureId == OpenGLUtils.NO_TEXTURE) {
            mTextureId = OpenGLUtils.getExternalOESTextureID();
            mSurfaceTexture = new SurfaceTexture(mTextureId);
            mSurfaceTexture.setOnFrameAvailableListener(mOnFrameAvailableListener);
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glDisable(GL10.GL_DITHER);
        GLES20.glClearColor(0,0, 0, 0);
        GLES20.glEnable(GL10.GL_CULL_FACE);
        GLES20.glEnable(GL10.GL_DEPTH_TEST);

        initSurfaceTexture();
        initCameraFilter();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0,0,width, height);
        this.mSurfaceViewWidth = width;
        this.mSurfaceViewHeight = height;
        openCamera();

        int iwidth = NaCameraEngineManager.getInstance().getPreviewWidth();
        int iheight = NaCameraEngineManager.getInstance().getPreviewHeight();
        int rotation = NaCameraEngineManager.getInstance().getOrientation();
        if (rotation == 90 || rotation == 270){
            mImageHeight = iwidth;
            mImageWidth = iheight;
        } else {
            mImageWidth = iwidth;
            mImageHeight = iheight;
        }
        if (mCameraFilter != null){
            boolean flipVertical = true;
            boolean flibHorizontal= NaCameraEngineManager.getInstance().isFrontCamera();
            mCameraFilter.onDisplaySizeChanged(width, height);
            mCameraFilter.onInputSizeChanged(mImageWidth, mImageHeight);
            NaLog.e(TAG, "onSurfaceChanged IW=" + mImageWidth + ",ih=" + mImageHeight + ",flibh=" + flibHorizontal + ",rotation=" + rotation);
            mCameraFilter.adjustSize(rotation, flibHorizontal, flipVertical, mScaleType);
        }

        if (mFilter != null){
            mFilter.onDisplaySizeChanged(width, height);
            mFilter.onInputSizeChanged(mImageWidth, mImageHeight);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (mSurfaceTexture != null){
            mSurfaceTexture.updateTexImage();

            float[] mtx = new float[16];
            mSurfaceTexture.getTransformMatrix(mtx);



            long dt = System.currentTimeMillis() - mStartTime;
            mStartTime = System.currentTimeMillis();
//            if(mFpsListener != null) {
//                mFpsListener.onFpsChanged((int) dt);
//            }

            if (mRGBABuffer == null) {
                mRGBABuffer = ByteBuffer.allocate(mImageHeight * mImageWidth * 4);
            }
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            mRGBABuffer.rewind();

            int textureId = mTextureId;
            if (mCameraFilter != null){

                mCameraFilter.setTextureTransformMatrix(mtx);
                mCameraFilter.onDrawFrame(textureId, gLCubeBuffer, gLTextureBuffer);
                textureId = mCameraFilter.onDrawBuffer(textureId, mRGBABuffer);
//                GLES20.glViewport(0, 0, mSurfaceViewWidth, mSurfaceViewHeight);
                if (mFilter != null){
                    mFilter.onDrawFrame(textureId);
                } else {
                    mCameraFilter.onDrawFrame(textureId);
                }
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        closeCamera();
    }

    public void onResume() {

    }

    public void onPause() {

    }

    @Override
    public void onCameraError(String errMsg) {

    }

    @Override
    public void onCameraOpening(int id) {

    }

    @Override
    public void onSwicthCamera(boolean isSuccess, int cameraId) {

    }

    @Override
    public void onCameraClosed() {

    }

    @Override
    public void onSwithcLight(boolean b) {

    }

    @Override
    public void onZoom(int mCurrentZoom, boolean isSuccess) {

    }

    @Override
    public void onFirstFrameAvailable() {

    }

    @Override
    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

    @Override
    public SurfaceHolder getSurfaceHolder() {
        return null;
    }

    @Override
    public void onPreviewFrame(byte[] data, int width, int height, int orientation, long captureTimeNs) {

    }

    @Override
    public void onRotation(int orientation, boolean isBack) {

    }

    public enum  ScaleType{
        CENTER_INSIDE,
        CENTER_CROP,
        FIT_XY;
    }
}
