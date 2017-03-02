package com.na.face;

import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.na.camera.render.NaCamerRender;

public class CameraActivity extends AppCompatActivity {

    private GLSurfaceView mGLSurfaceView;
    private NaCamerRender naCamerRender;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mGLSurfaceView = (GLSurfaceView) findViewById(R.id.glSurfaceView);
        naCamerRender = new NaCamerRender(mGLSurfaceView);
    }
}
