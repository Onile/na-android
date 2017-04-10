package com.na.camera.filter;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.na.camera.glutils.GlUtil;
import com.na.camera.glutils.OpenGLUtils;
import com.na.camera.glutils.TextureRotationUtil;
import com.na.uitls.NaLog;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

/**
 * @actor:taotao
 * @DATE: 2017/3/6
 */

public class NaCameraFilter extends NaFilter{

    private static final String TAG = "NaCameraFilter";

    private static final String CAMERA_INPUT_VERTEX_SHADER = "" +
            "attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoordinate;\n" +
            "\n" +
            "varying vec2 textureCoordinate;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "	textureCoordinate = inputTextureCoordinate.xy;\n" +
            "	gl_Position = position;\n" +
            "}";

    private static final String CAMERA_INPUT_FRAGMENT_SHADER_OES = "" +
            "#extension GL_OES_EGL_image_external : require\n" +
            "\n" +
            "precision mediump float;\n" +
            "varying vec2 textureCoordinate;\n" +
            "uniform samplerExternalOES inputImageTexture;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "	gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "}";

    public static final String CAMERA_INPUT_FRAGMENT_SHADER = "" +
            "precision mediump float;\n" +
            "varying highp vec2 textureCoordinate;\n" +
            " \n" +
            "uniform sampler2D inputImageTexture;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "}";


    private int mCameraGLProgId;
    private int mCameraGLAttribPosition;
    private int mCameraGLUniformTexture;
    private int mCameraGLAttribTextureCoordinate;

    private FloatBuffer mCameraTextureBuffer;
    private FloatBuffer mCameraVertexBuffer;

    private FloatBuffer mGLSaveTextureBuffer;

    private int[] mFrameBuffers;
    private int[] mFrameBufferTextures;

    private void initFrameBuffers(int width, int height) {
        destroyFrameBuffers();
        if (mFrameBuffers == null) {
            mFrameBuffers = new int[2];
            mFrameBufferTextures = new int[2];

            GLES20.glGenFramebuffers(2, mFrameBuffers, 0);
            GLES20.glGenTextures(2, mFrameBufferTextures, 0);

            bindFrameBuffer(mFrameBufferTextures[0], mFrameBuffers[0], width, height);
            bindFrameBuffer(mFrameBufferTextures[1], mFrameBuffers[1], width, height);
        }
    }

    private void bindFrameBuffer(int textureId, int frameBuffer, int width, int height) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D,textureId, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    public void destroyFrameBuffers() {
        if (mFrameBufferTextures != null) {
            GLES20.glDeleteTextures(2, mFrameBufferTextures, 0);
            mFrameBufferTextures = null;
        }
        if (mFrameBuffers != null) {
            GLES20.glDeleteFramebuffers(2, mFrameBuffers, 0);
            mFrameBuffers = null;
        }
    }

    public NaCameraFilter() {
        super(CAMERA_INPUT_VERTEX_SHADER, CAMERA_INPUT_FRAGMENT_SHADER);
        mGLSaveTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLSaveTextureBuffer.put(TextureRotationUtil.getRotation(0, false, true)).position(0);
    }

    @Override
    protected void onInit() {
        super.onInit();
        mCameraGLProgId = OpenGLUtils.loadProgram(CAMERA_INPUT_VERTEX_SHADER, CAMERA_INPUT_FRAGMENT_SHADER_OES);
        if (mCameraGLProgId != 0) {
            mCameraGLAttribPosition = GLES20.glGetAttribLocation(mCameraGLProgId, POSITION_COORDINATE);
            mCameraGLUniformTexture = GLES20.glGetUniformLocation(mCameraGLProgId, TEXTURE_UNIFORM);
            mCameraGLAttribTextureCoordinate = GLES20.glGetAttribLocation(mCameraGLProgId, TEXTURE_COORDINATE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GLES20.glDeleteProgram(mCameraGLProgId);
        destroyFrameBuffers();
    }

    public int onSavePictureTextureId(int textureId, ByteBuffer buffer){
        GLES20.glUseProgram(getGLProgId());
        GlUtil.checkGlError("glUseProgram");
        runPendingOnDrawTasks();
        if(mFrameBuffers == null) {
            return OpenGLUtils.NO_TEXTURE;
        }

        if (!isInitialized()) {
            return OpenGLUtils.NOT_INIT;
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[1]);
        GLES20.glViewport(0, 0, getFrameWidth(), getFrameHeight());

        getGLCubeBuffer().position(0);
        int glAttribPosition = getGLAttribPosition();
        GLES20.glVertexAttribPointer(glAttribPosition, 2, GLES20.GL_FLOAT, false, 0, getGLCubeBuffer());
        GLES20.glEnableVertexAttribArray(glAttribPosition);

        mGLSaveTextureBuffer.position(0);
        int glAttribTextureCoordinate = getGLAttribTextureCoordinate();
        GLES20.glVertexAttribPointer(glAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, mGLSaveTextureBuffer);
        GLES20.glEnableVertexAttribArray(glAttribTextureCoordinate);

        if(textureId != OpenGLUtils.NO_TEXTURE) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glUniform1i(getGLUniformTexture(), 0);
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        if(buffer != null) {
            GLES20.glReadPixels(0, 0, getFrameWidth(), getFrameHeight(), GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
        }

        GLES20.glDisableVertexAttribArray(glAttribPosition);
        GLES20.glDisableVertexAttribArray(glAttribTextureCoordinate);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        return mFrameBufferTextures[1];
    }

    /**
     * 此函数有三个功能
     * 1. 将OES的纹理转换为标准的GL_TEXTURE_2D格式
     * 2. 将纹理宽高对换，即将wxh的纹理转换为了hxw的纹理，并且如果是前置摄像头，则需要有水平的翻转
     * 3. 读取上面两个步骤后纹理的内容到cpu内存，存储为RGBA格式的buffer
     * @param textureId 输入的OES的纹理id
     * @param buffer 输出的RGBA的buffer
     * @return 转换后的GL_TEXTURE_2D的纹理id
     */
    public int onDrawTextureId(int textureId, ByteBuffer buffer) {

        GLES20.glUseProgram(mCameraGLProgId);
        GlUtil.checkGlError("glUseProgram");
        runPendingOnDrawTasks();

        if (mFrameBuffers == null || !isInitialized()) {
            return OpenGLUtils.NOT_INIT;
        }

        mCameraVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mCameraGLAttribPosition, 2, GLES20.GL_FLOAT, false, 0, mCameraVertexBuffer);
        GLES20.glEnableVertexAttribArray(mCameraGLAttribPosition);

        mCameraTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(mCameraGLAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, mCameraTextureBuffer);
        GLES20.glEnableVertexAttribArray(mCameraGLAttribTextureCoordinate);

        if (textureId != -1) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
            GLES20.glUniform1i(mCameraGLUniformTexture, 0);
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
        GlUtil.checkGlError("glBindFramebuffer");
        GLES20.glViewport(0, 0, getFrameWidth(), getFrameHeight());

        onDrawArraysPre();
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        if (buffer != null) {
            GLES20.glReadPixels(0, 0, getFrameWidth(), getFrameHeight(), GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
        }

        GLES20.glDisableVertexAttribArray(mCameraGLAttribPosition);
        GLES20.glDisableVertexAttribArray(mCameraGLAttribTextureCoordinate);
        onDrawArraysAfter();
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glUseProgram(0);
        return mFrameBufferTextures[0];
    }

//    public int onDrawFrame(final int textureId) {
//
//        GLES20.glUseProgram(getGLProgId());
//        GlUtil.checkGlError("glUseProgram");
//        runPendingOnDrawTasks();
//
//        if (!isInitialized()) {
//            return OpenGLUtils.NOT_INIT;
//        }
//
//        mCameraVertexBuffer.position(0);
//        GLES20.glVertexAttribPointer(getGLAttribPosition(), 2, GLES20.GL_FLOAT, false, 0, mCameraVertexBuffer);
//        GLES20.glEnableVertexAttribArray(getGLAttribPosition());
//
//        getGLTextureBuffer().position(0);
//        GLES20.glVertexAttribPointer(getGLAttribTextureCoordinate(), 2, GLES20.GL_FLOAT, false, 0, getGLTextureBuffer());
//        GLES20.glEnableVertexAttribArray(getGLAttribTextureCoordinate());
//
//        if (textureId != OpenGLUtils.NO_TEXTURE) {
//            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
//            GLES20.glUniform1i(getGLUniformTexture(), 0);
//        }
//
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
//        GLES20.glDisableVertexAttribArray(getGLAttribPosition());
//        GLES20.glDisableVertexAttribArray(getGLAttribTextureCoordinate());
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
//        return OpenGLUtils.ON_DRAWN;
//    }

    @Override
    public void onChangeFrameSized(int displayW, int displayH, int frameW, int frameH) {
        super.onChangeFrameSized(displayW, displayH, frameW, frameH);
        calculateVertexBuffer(displayW, displayH);
        initFrameBuffer();
    }

    private void calculateVertexBuffer(int displayW, int displayH){
        calculateVertexBuffer(displayW, displayH, getFrameWidth(), getFrameHeight());
    }

    private void initFrameBuffer(){
        initFrameBuffers(getFrameWidth(), getFrameHeight());
    }

    public void adjustTextureBuffer(int orientation, boolean flipHorizontal, boolean flipVertical) {
        float[] textureCords = TextureRotationUtil.getRotation(orientation, flipHorizontal, flipVertical);
        NaLog.d(TAG, "==========rotation: " + orientation + " flipVertical: " + flipVertical
                + " texturePos: " + Arrays.toString(textureCords));
        if (mCameraTextureBuffer == null) {
            mCameraTextureBuffer = ByteBuffer.allocateDirect(textureCords.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
        }
        mCameraTextureBuffer.clear();
        mCameraTextureBuffer.put(textureCords).position(0);
    }

    /**
     * 用来计算贴纸渲染的纹理最终需要的顶点坐标
     */
    public void calculateVertexBuffer(int displayW, int displayH, int imageW, int imageH) {
        int outputHeight = displayH;
        int outputWidth = displayW;

        float ratio1 = (float) outputWidth / imageW;
        float ratio2 = (float) outputHeight / imageH;
        float ratioMax = Math.max(ratio1, ratio2);
        int imageWidthNew = Math.round(imageW * ratioMax);
        int imageHeightNew = Math.round(imageH * ratioMax);

        float ratioWidth = imageWidthNew / (float) outputWidth;
        float ratioHeight = imageHeightNew / (float) outputHeight;

        float[] cube = new float[]{
                TextureRotationUtil.CUBE[0] / ratioHeight, TextureRotationUtil.CUBE[1] / ratioWidth,
                TextureRotationUtil.CUBE[2] / ratioHeight, TextureRotationUtil.CUBE[3] / ratioWidth,
                TextureRotationUtil.CUBE[4] / ratioHeight, TextureRotationUtil.CUBE[5] / ratioWidth,
                TextureRotationUtil.CUBE[6] / ratioHeight, TextureRotationUtil.CUBE[7] / ratioWidth,
        };

        if (mCameraVertexBuffer == null) {
            mCameraVertexBuffer = ByteBuffer.allocateDirect(cube.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
        }
        mCameraVertexBuffer.clear();
        mCameraVertexBuffer.put(cube).position(0);
    }
}
