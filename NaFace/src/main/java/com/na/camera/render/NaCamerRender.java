package com.na.camera.render;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Environment;
import android.view.SurfaceHolder;

import com.na.camera.INaCameraEvent;
import com.na.camera.INaSurfaceHelper;
import com.na.camera.NaCameraEngineManager;
import com.na.camera.filter.NaBeautyFilter;
import com.na.camera.filter.NaCameraFilter;
import com.na.camera.filter.NaFilter;
import com.na.camera.glutils.OpenGLUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @actor:taotao
 * @DATE: 17/2/28
 */

public class NaCamerRender implements Renderer, SurfaceHolder.Callback, INaSurfaceHelper, INaCameraEvent{

    private static final String TAG = "NaCamerRender";

    public final static int CAMERA_BACK = 0;
    public final static int CAMERA_FRONT = 1;

    private GLSurfaceView mGLSurfaceView;

    private NaCameraFilter mCameraFilter;

    private NaFilter mFilter;

    private int mImageWidth;
    private int mImageHeight;

    private int mSurfaceViewWidth;
    private int mSurfaceViewHeight;

    private ByteBuffer mRGBABuffer;

    private int mCameraId = CAMERA_FRONT;

    private SurfaceTexture mSurfaceTexture;
    private int mTextureId = OpenGLUtils.NO_TEXTURE;

    private long mStartTime = 0;

    private boolean isSavePicture = false;
    private SavePictureListener mSavePictureListener;

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
    }

    private void initFilter() {
        mFilter = new NaBeautyFilter();
        mFilter.init();
    }

    private void initGLSurfaceView(){
        this.mGLSurfaceView.setEGLContextClientVersion(2);
        this.mGLSurfaceView.setRenderer(this);
        this.mGLSurfaceView.getHolder().addCallback(this);
        this.mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private void openCamera() {
        NaCameraEngineManager.getInstance().setSurfaceHelper(this);
        NaCameraEngineManager.getInstance().setCameraEvent(this);
        NaCameraEngineManager.getInstance().openCamera(mCameraId);
        updateFilter();
    }

    private void updateFilter(){
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
            boolean flibHorizontal = true;
            boolean flipVertical= NaCameraEngineManager.getInstance().isFrontCamera();
            mCameraFilter.adjustTextureBuffer(rotation, flibHorizontal, flipVertical);
            mCameraFilter.onChangeFrameSized(mSurfaceViewWidth, mSurfaceViewHeight, mImageWidth, mImageHeight);
        }

        if (mFilter != null){
            mFilter.onChangeFrameSized(mSurfaceViewWidth, mSurfaceViewHeight, mImageWidth, mImageHeight);
        }
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
        initFilter();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        this.mSurfaceViewWidth = width;
        this.mSurfaceViewHeight = height;
        openCamera();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (mSurfaceTexture != null){
            mSurfaceTexture.updateTexImage();

            long dt = System.currentTimeMillis() - mStartTime;
            mStartTime = System.currentTimeMillis();

            if (mRGBABuffer == null) {
                mRGBABuffer = ByteBuffer.allocate(mImageHeight * mImageWidth * 4);
            }
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            mRGBABuffer.rewind();

            int textureId = mTextureId;
            if (mCameraFilter != null){
                textureId = mCameraFilter.onDrawTextureId(textureId, mRGBABuffer);
//                int textureId = mCameraFilter.preProcess(mTextureId, mRGBABuffer);
//                textureId = mFilter.onDrawFrame()
                GLES20.glViewport(0, 0, mSurfaceViewWidth, mSurfaceViewHeight);
                if (mFilter != null) {
                    mFilter.onDrawFrame(textureId);
                } else {
                    mCameraFilter.onDrawFrame(textureId);
                }
            }

            if (isSavePicture){
                isSavePicture = false;
                String ret = null;
                try {
                    mCameraFilter.onSavePictureTextureId(textureId, mRGBABuffer);
                    Bitmap result = Bitmap.createBitmap(mImageWidth, mImageHeight, Bitmap.Config.ARGB_8888);
                    result.copyPixelsFromBuffer(mRGBABuffer);
                    ret = savePicture(result);
                } catch (Exception e){
                    e.printStackTrace();
                }
                if (mSavePictureListener != null){
                    mSavePictureListener.onSavePictureResult(ret);
                }
            }
        }
    }

    public File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "NaCamera");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINESE).format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    private String savePicture(Bitmap bitmap){
        String fileName = null;
        File file = getOutputMediaFile();
        if (file.exists()) {
            file.delete();
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            //png 比 jpeg 生成文件大一倍 故用jpeg格式压缩保存
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            bitmap.recycle();
            fileName = out.toString();
        }  catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
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
        mCameraId = cameraId;
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

    public void switchCamera() {
        if (mGLSurfaceView != null){
            mGLSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    mCameraId++;
                    NaCameraEngineManager.getInstance().switchCamera();
                    updateFilter();
                }
            });
        }
    }

    public void savePicture(SavePictureListener listener) {
        if (isSavePicture){
            return;
        }
        mSavePictureListener = listener;
        isSavePicture = true;
    }
}
