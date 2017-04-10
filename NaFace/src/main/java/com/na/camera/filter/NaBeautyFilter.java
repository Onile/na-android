package com.na.camera.filter;

import android.opengl.GLES20;

import com.na.api.NaApi;
import com.na.camera.glutils.OpenGLUtils;
import com.na.face.R;

/**
 * @actor:taotao
 * @DATE: 2017/3/7
 */

public class NaBeautyFilter extends NaFilter {
    private int mSingleStepOffsetLocation;
    private int mParamsLocation;
    private int beautyLevel = 0;

    public NaBeautyFilter() {
        super(NO_FILTER_VERTEX_SHADER, OpenGLUtils.readShaderFromRawResource(NaApi.getApi().getApplicationContext(),R.raw.beauty));
    }

    protected void onInit() {
        super.onInit();
        mSingleStepOffsetLocation = GLES20.glGetUniformLocation(getGLProgId(), "singleStepOffset");
        mParamsLocation = GLES20.glGetUniformLocation(getGLProgId(), "params");
        setBeautyLevel(beautyLevel);
    }

    private void setTexelSize(final float w, final float h) {
        setFloatVec2(mSingleStepOffsetLocation, new float[]{2.0f / w, 2.0f / h});
    }

    @Override
    public void onChangeFrameSized(int displayW, int displayH, int frameW, int frameH) {
        super.onChangeFrameSized(displayW, displayH, frameW, frameH);
        setTexelSize(frameW, frameH);
    }

    public void setBeautyLevel(int level) {
        switch (level) {
            case 1:
                setFloat(mParamsLocation, 1.0f);
                break;
            case 2:
                setFloat(mParamsLocation, 0.8f);
                break;
            case 3:
                setFloat(mParamsLocation, 0.6f);
                break;
            case 4:
                setFloat(mParamsLocation, 0.4f);
                break;
            case 5:
                setFloat(mParamsLocation, 0.33f);
                break;
            default:
                break;
        }
    }
}
