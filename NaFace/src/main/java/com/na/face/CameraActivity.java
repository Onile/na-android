package com.na.face;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.na.camera.render.NaCamerRender;
import com.na.camera.render.SavePictureListener;
import com.na.uitls.NaLog;

public class CameraActivity extends NaBaseActivity implements View.OnClickListener{

    private static final String TAG = "CameraActivity";
    private GLSurfaceView glSurfaceView;
    private ImageView ivSwitchCamera;
    private ImageView ivSavePicture;

    private NaCamerRender naCamerRender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        glSurfaceView = (GLSurfaceView) findViewById(R.id.glSurfaceView);
        ivSwitchCamera = (ImageView) findViewById(R.id.ivSwitchCamera);
        ivSavePicture = (ImageView) findViewById(R.id.ivSavePicture);

        ivSwitchCamera.setOnClickListener(this);
        ivSavePicture.setOnClickListener(this);

        naCamerRender = new NaCamerRender(glSurfaceView);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.ivSwitchCamera:{
                switchCamera();
                break;
            }
            case R.id.ivSavePicture:{
                SavePicture();
                break;
            }
            default:{
                break;
            }
        }
    }

    private void SavePicture() {
        if (naCamerRender != null){
            naCamerRender.savePicture(new SavePictureListener() {
                @Override
                public void onSavePictureResult(String pictureFileName) {
                    NaLog.d(TAG, "onSavePictureResult pictureFileName=" + pictureFileName);
                }
            });
        }
    }

    private void switchCamera() {
        if (naCamerRender != null){
            naCamerRender.switchCamera();
        }
    }
}
