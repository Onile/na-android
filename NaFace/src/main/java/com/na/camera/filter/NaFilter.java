package com.na.camera.filter;

import android.graphics.PointF;
import android.opengl.GLES20;

import com.na.camera.glutils.OpenGLUtils;
import com.na.camera.glutils.TextureRotationUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.LinkedList;

/**
 * @actor:taotao
 * @DATE: 17/3/1
 */

public class NaFilter implements INaFilter {

    public static final String NO_FILTER_VERTEX_SHADER = "" +
            "attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoordinate;\n" +
            " \n" +
            "varying vec2 textureCoordinate;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "    gl_Position = position;\n" +
            "    textureCoordinate = inputTextureCoordinate.xy;\n" +
            "}";
    public static final String NO_FILTER_FRAGMENT_SHADER = "" +
            "varying highp vec2 textureCoordinate;\n" +
            " \n" +
            "uniform sampler2D inputImageTexture;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "}";

    protected final static String POSITION_COORDINATE = "position";
    protected final static String TEXTURE_UNIFORM = "inputImageTexture";
    protected final static String TEXTURE_COORDINATE = "inputTextureCoordinate";

    private LinkedList<Runnable> mRunOnDraw;
    private String mVertexShader;
    private String mFragmentShader;

    private int mGLProgId;
    private int mGLAttribPosition;
    private int mGLUniformTexture;
    private int mGLAttribTextureCoordinate;

    private boolean mIsInitialized;
    private FloatBuffer mGLCubeBuffer;
    private FloatBuffer mGLTextureBuffer;

    private int mFrameWidth;
    private int mFrameHeight;

    private int mDisplayWidth;
    private int mDisplayHeight;


    protected void setFragmentShader(String mFragmentShader) {
        this.mFragmentShader = mFragmentShader;
    }

    protected void setVertexShader(String mVertexShader) {
        this.mVertexShader = mVertexShader;
    }

    protected int getFrameHeight() {
        return mFrameHeight;
    }

    protected int getFrameWidth() {
        return mFrameWidth;
    }

    protected int getGLAttribPosition() {
        return mGLAttribPosition;
    }

    protected int getGLAttribTextureCoordinate() {
        return mGLAttribTextureCoordinate;
    }

    protected FloatBuffer getGLCubeBuffer() {
        return mGLCubeBuffer;
    }

    public int getGLProgId() {
        return mGLProgId;
    }

    public FloatBuffer getGLTextureBuffer() {
        return mGLTextureBuffer;
    }

    public int getGLUniformTexture() {
        return mGLUniformTexture;
    }

    public boolean isInitialized() {
        return mIsInitialized;
    }

    protected void runPendingOnDrawTasks() {
        while (!mRunOnDraw.isEmpty()) {
            mRunOnDraw.removeFirst().run();
        }
    }

    protected void runOnDraw(final Runnable runnable) {
        synchronized (mRunOnDraw) {
            mRunOnDraw.addLast(runnable);
        }
    }

    protected void setInteger(final int location, final int intValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform1i(location, intValue);
            }
        });
    }

    protected void setFloat(final int location, final float floatValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform1f(location, floatValue);
            }
        });
    }

    protected void setFloatVec2(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform2fv(location, 1, FloatBuffer.wrap(arrayValue));
            }
        });
    }

    protected void setFloatVec3(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform3fv(location, 1, FloatBuffer.wrap(arrayValue));
            }
        });
    }

    protected void setFloatVec4(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform4fv(location, 1, FloatBuffer.wrap(arrayValue));
            }
        });
    }

    protected void setFloatArray(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform1fv(location, arrayValue.length, FloatBuffer.wrap(arrayValue));
            }
        });
    }

    protected void setPoint(final int location, final PointF point) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                float[] vec2 = new float[2];
                vec2[0] = point.x;
                vec2[1] = point.y;
                GLES20.glUniform2fv(location, 1, vec2, 0);
            }
        });
    }

    protected void setUniformMatrix3f(final int location, final float[] matrix) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniformMatrix3fv(location, 1, false, matrix, 0);
            }
        });
    }

    protected void setUniformMatrix4f(final int location, final float[] matrix) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniformMatrix4fv(location, 1, false, matrix, 0);
            }
        });
    }

    public NaFilter() {
        this(NO_FILTER_VERTEX_SHADER, NO_FILTER_FRAGMENT_SHADER);
    }

    public NaFilter(String vertexShader, String fragmentShader) {
        mRunOnDraw = new LinkedList<>();
        mVertexShader = vertexShader;
        mFragmentShader = fragmentShader;

        mGLCubeBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLCubeBuffer.put(TextureRotationUtil.CUBE).position(0);

        mGLTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
//        mGLTextureBuffer.put(TextureRotationUtil.getRotation(Rotation.NORMAL.asInt(), false, true)).position(0);
        mGLTextureBuffer.put(TextureRotationUtil.TEXTURE_NO_ROTATION).position(0);
    }

    public void init() {
        onInit();
        mIsInitialized = true;
        onInitialized();
    }

    protected void onInit() {
        initShader();
    }

    protected void initShader(){
        mGLProgId = OpenGLUtils.loadProgram(mVertexShader, mFragmentShader);
        if (mGLProgId != 0) {
            mGLAttribPosition = GLES20.glGetAttribLocation(mGLProgId, POSITION_COORDINATE);
            mGLUniformTexture = GLES20.glGetUniformLocation(mGLProgId, TEXTURE_UNIFORM);
            mGLAttribTextureCoordinate = GLES20.glGetAttribLocation(mGLProgId, TEXTURE_COORDINATE);
        }
    }

    protected void onInitialized() {

    }

    public final void destroy() {
        mIsInitialized = false;
        GLES20.glDeleteProgram(mGLProgId);
        onDestroy();
    }

    protected void onDestroy() {

    }

    public int onDrawFrame(final int textureId) {
        GLES20.glUseProgram(mGLProgId);
        runPendingOnDrawTasks();
        if (!mIsInitialized)
            return OpenGLUtils.NOT_INIT;

        mGLCubeBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribPosition, 2, GLES20.GL_FLOAT, false, 0, mGLCubeBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribPosition);

        mGLTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0,
                mGLTextureBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate);

        if (textureId != OpenGLUtils.NO_TEXTURE) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glUniform1i(mGLUniformTexture, 0);
        }
        onDrawArraysPre();
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(mGLAttribPosition);
        GLES20.glDisableVertexAttribArray(mGLAttribTextureCoordinate);
        onDrawArraysAfter();
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return OpenGLUtils.ON_DRAWN;
    }

    protected void onDrawArraysPre() {

    }

    protected void onDrawArraysAfter() {

    }

    public void onChangeFrameSized(int displayW, int displayH, int frameW, int frameH){
        this.mDisplayWidth = displayW;
        this.mDisplayHeight = displayH;
        this.mFrameWidth = frameW;
        this.mFrameHeight = frameH;
    }
}
