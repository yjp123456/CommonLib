package com.commonlib.filter;


import com.commonlib.gl.basis.AbsMTGLFilter;
import com.commonlib.gl.data.AbsVertexData;
import com.commonlib.gl.data.MTGLTextureVertexData;
import com.commonlib.gl.util.MTGLUtil;

import static android.opengl.GLES20.GL_TEXTURE1;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform1i;

/**
 * 滤镜效果处理类
 */

public class EffectFilter extends AbsMTGLFilter {
    private static final String TAG = "Material";

    private int alphaLocation;
    private int materialMaskLocation;


    private int mMaterialMaskTextureId = MTGLUtil.NO_TEXTURE;

    private static final String MATERIAL_ALPHA = "alpha";
    private static final String MATERIAL_MASK_TEXTURE = "mt_tempData1";


    private static final String VERTEX_SHADER_ASSETS_PATH = "filter_script/filter.vs";
    private static final String FRAGMENT_SHADER_ASSETS_PATH = "filter_script/filter.fs";
    private float mAlpha = 1.0f;


    @Override
    protected String getVertexShaderResource() {
        return MTGLUtil.readAssetsText(VERTEX_SHADER_ASSETS_PATH);
    }

    @Override
    protected String getFragmentShaderResource() {
        return MTGLUtil.readAssetsText(FRAGMENT_SHADER_ASSETS_PATH);
    }

    @Override
    protected void initLocation(int program) {
        alphaLocation = glGetUniformLocation(program, MATERIAL_ALPHA);
        materialMaskLocation = glGetUniformLocation(program, MATERIAL_MASK_TEXTURE);
    }

    @Override
    protected AbsVertexData getVertexData() {
        return new MTGLTextureVertexData();
    }

    @Override
    protected void updateData() {
        if (mMaterialMaskTextureId != MTGLUtil.NO_TEXTURE) {
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, mMaterialMaskTextureId);
            glUniform1i(materialMaskLocation, 1);
        }

        glUniform1f(alphaLocation, mAlpha);

    }


    @Override
    protected void resetParams() {

    }


    public void setAlpha(float alpha) {
        mAlpha = alpha;
    }

    public void setMaterialMaskTextureId(int textureId) {
        mMaterialMaskTextureId = textureId;
    }

    public int getMaterialMaskTextureId() {
        return mMaterialMaskTextureId;
    }


    public void handleActionDown(float startX, float startY) {

    }

    public void handleActionUp(float x, float y, boolean isMove) {

    }

    @Override
    protected void handleActionMove(float x, float y) {

    }
}
