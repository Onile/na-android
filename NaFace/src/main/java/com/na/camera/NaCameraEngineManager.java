package com.na.camera;

/**
 * @actor:taotao
 * @DATE: 16/10/14
 */
public class NaCameraEngineManager implements INaCameraEngine, INaCameraEvent{
    private final static String Tag = "NaCameraEngineManager";
    private static NaCameraEngineManager sInstance;

    private INaCameraEvent mCameraEvent;
    private INaCameraEngine mCameraEngine;

    public static NaCameraEngineManager getInstance() {
        if (sInstance == null) {
            synchronized (NaCameraEngineManager.class) {
                if (sInstance == null) {
                    sInstance = new NaCameraEngineManager();
                    sInstance.init();
                }
            }
        }
        return sInstance;
    }

    private void init() {
        if (mCameraEngine == null) {
            mCameraEngine = new NaCameraEngine();
        }
    }

    private void release() {
        sInstance = null;
    }

    @Override
    public void setCameraEvent(INaCameraEvent event) {
        this.mCameraEvent = event;
        if (mCameraEngine != null) {
            mCameraEngine.setCameraEvent(this);
        }
    }

    @Override
    public void openCamera(int cameraId) {
        if (mCameraEngine != null) {
            mCameraEngine.openCamera(cameraId);
        }
    }

    @Override
    public void switchCamera() {
        if (mCameraEngine != null) {
            mCameraEngine.switchCamera();
        }
    }

    @Override
    public void closeCamera() {
        if (mCameraEngine != null) {
            mCameraEngine.closeCamera();
        }
    }

    @Override
    public void switchLight(boolean isOpen) {
        if (mCameraEngine != null) {
            mCameraEngine.switchLight(isOpen);
        }
    }

    @Override
    public void destroy() {
        if (mCameraEngine != null) {
            mCameraEngine.destroy();
        }
        mCameraEngine = null;
        mCameraEvent = null;
        release();
    }

    @Override
    public void setSurfaceHelper(INaSurfaceHelper helper) {
        if (mCameraEngine != null) {
            mCameraEngine.setSurfaceHelper(helper);
        }
    }

    @Override
    public void setZoom(float scale) {
        if (mCameraEngine != null) {
            mCameraEngine.setZoom(scale);
        }
    }

    @Override
    public void setFocus() {
        if (mCameraEngine != null) {
            mCameraEngine.setFocus();
        }
    }

    @Override
    public int getCurrentCameraId() {
        if (mCameraEngine != null){
            return mCameraEngine.getCurrentCameraId();
        }
        return -1;
    }

    @Override
    public int getOrientation() {
        if (mCameraEngine != null){
            return mCameraEngine.getOrientation();
        }
        return 0;
    }

    @Override
    public boolean isFrontCamera() {
        if(mCameraEngine != null){
            return mCameraEngine.isFrontCamera();
        }
        return false;
    }

    @Override
    public int getPreviewWidth() {
        if (mCameraEngine != null){
            return mCameraEngine.getPreviewWidth();
        }
        return 0;
    }

    @Override
    public int getPreviewHeight() {
        if (mCameraEngine != null){
            return mCameraEngine.getPreviewHeight();
        }
        return 0;
    }

    @Override
    public int getPictureWidth() {
        if (mCameraEngine != null){
            return mCameraEngine.getPictureWidth();
        }
        return 0;
    }

    @Override
    public int getPictureHeight() {
        if (mCameraEngine != null){
            return mCameraEngine.getPictureHeight();
        }
        return 0;
    }

    @Override
    public void onCameraError(String errorDescription) {
        if (mCameraEvent != null) {
            mCameraEvent.onCameraError(errorDescription);
        }
    }

    @Override
    public void onCameraOpening(int cameraId) {
        if (mCameraEvent != null) {
            mCameraEvent.onCameraOpening(cameraId);
        }
    }

    @Override
    public void onFirstFrameAvailable() {
        if (mCameraEvent != null) {
            mCameraEvent.onFirstFrameAvailable();
        }
    }

    @Override
    public void onCameraClosed() {
        if (mCameraEvent != null) {
            mCameraEvent.onCameraClosed();
        }
    }

    @Override
    public void onSwicthCamera(boolean isSuccess, int cameraId) {
        if (mCameraEvent != null) {
            mCameraEvent.onSwicthCamera(isSuccess, cameraId);
        }
    }

    @Override
    public void onSwithcLight(boolean isSuccess) {
        if (mCameraEvent != null) {
            mCameraEvent.onSwithcLight(isSuccess);
        }
    }

    @Override
    public void onZoom(int curZoom, boolean isSuccess) {
        if (mCameraEvent != null) {
            mCameraEvent.onZoom(curZoom, isSuccess);
        }
    }
}
