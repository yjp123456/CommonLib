package com.commonlib.gl.basis;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.commonlib.gl.data.AbsVertexData;
import com.commonlib.gl.util.MTGLUtil;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;

/**
 * GL特效类
 */
public abstract class AbsMTGLFilter {
    /**
     * 默认可撤销步数
     */
    private static final int DEFAULT_ACTION_SIZE = 5;

    private int positionCoordinates;
    private int textureCoordinates;
    private int inputImageTexture;
    private int uMatrixLocation;


    private static final String U_MATRIX = "u_Matrix";
    private static final String POSITION_COORDINATES = "posCoord";
    private static final String TEXTURE_COORDINATES = "texCoord";
    private static final String INPUT_IMAGE_TEXTURE = "srctexture";


    private int mProgram;
    private int mVertexShader;
    private int mFragmentShader;
    protected AbsVertexData mData;


    /**
     * 设置定点着色器源码
     */
    protected abstract String getVertexShaderResource();

    /**
     * 设置片段着色器源码
     */
    protected abstract String getFragmentShaderResource();

    /**
     * 初始化属性位置
     */
    protected abstract void initLocation(int program);

    /**
     * 设置顶点数据
     */
    protected abstract AbsVertexData getVertexData();

    /**
     * 更新数据
     */
    protected abstract void updateData();

    /**
     * 处理点击事件
     */
    protected abstract void handleActionDown(float x, float y);

    /**
     * 处理点击事件
     */
    protected abstract void handleActionUp(float x, float y, boolean isMove);

    /**
     * 处理点击事件
     */
    protected abstract void handleActionMove(float endX, float endY);

    /**
     * 重置参数
     */
    protected abstract void resetParams();


    public void init() {
        mVertexShader = MTGLUtil.compileShader(GLES20.GL_VERTEX_SHADER, getVertexShaderResource());
        mFragmentShader = MTGLUtil.compileShader(GLES20.GL_FRAGMENT_SHADER, getFragmentShaderResource());
        mProgram = MTGLUtil.buildProgram(mVertexShader, mFragmentShader);
        GLES20.glDeleteShader(mVertexShader);
        GLES20.glDeleteShader(mFragmentShader);
        mData = getVertexData();

        positionCoordinates = glGetAttribLocation(mProgram, POSITION_COORDINATES);
        textureCoordinates = glGetAttribLocation(mProgram, TEXTURE_COORDINATES);
        uMatrixLocation = glGetUniformLocation(mProgram, U_MATRIX);
        inputImageTexture = glGetUniformLocation(mProgram, INPUT_IMAGE_TEXTURE);


        initLocation(mProgram);
    }

    /**
     * 设置最大可撤销步骤数
     */
    int getActionStep() {
        return DEFAULT_ACTION_SIZE;
    }

    public final void deleteProgram() {
        if (mProgram != MTGLUtil.NO_PROGRAM) {
            GLES20.glDeleteProgram(mProgram);
            mProgram = 0;
            mVertexShader = 0;
            mFragmentShader = 0;
        }
    }


    /**
     * 更新顶点数据
     *
     * @param ratioWidth  宽度比例
     * @param ratioHeight 高度比例
     */
    public void updateVertexData(float ratioWidth, float ratioHeight) {
        mData.updateVertexData(ratioWidth, ratioHeight);
    }

    public void setVertexData(float[] vertexData) {
        mData.setVertexData(vertexData);
    }

    public void initVertexData(float[] vertexData) {
        mData.initData(vertexData);
    }

    /**
     * 绘制
     */
    public void draw(float[] matrix, int textureId) {
        glUseProgram(mProgram);
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);

        if (textureId != MTGLUtil.NO_TEXTURE) {
            //set active texture unit to unit 0
            glActiveTexture(GL_TEXTURE0);

            glBindTexture(GL_TEXTURE_2D, textureId);

            //tell the texture uniform sampler to use this texture in the shader
            // by telling it to read from texture unit 0
            glUniform1i(inputImageTexture, 0);
        }
        updateData();
        mData.bindData(this);
        mData.draw();

    }

    /**
     * 绘制帧数据
     */
    public void drawOES(float[] matrix, int textureId) {
        glUseProgram(mProgram);
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);

        if (textureId != MTGLUtil.NO_TEXTURE) {
            //set active texture unit to unit 0
            glActiveTexture(GLES20.GL_TEXTURE0);

            glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

            //tell the texture uniform sampler to use this texture in the shader
            // by telling it to read from texture unit 0
            glUniform1i(inputImageTexture, 0);
        }
        updateData();
        mData.bindData(this);
        mData.draw();

    }

    public void drawElements(int textureId) {
        glUseProgram(mProgram);
        // glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);
        //set active texture unit to unit 0
        glActiveTexture(GL_TEXTURE0);

        glBindTexture(GL_TEXTURE_2D, textureId);

        //tell the texture uniform sampler to use this texture in the shader
        // by telling it to read from texture unit 0
        glUniform1i(inputImageTexture, 0);
        updateData();
        mData.bindData(this);
        mData.drawElements();
    }

    public final int getPositionAttributeLocation() {
        return positionCoordinates;
    }

    public final int getTextureCoordinatesAttributeLocation() {
        return textureCoordinates;
    }


}
