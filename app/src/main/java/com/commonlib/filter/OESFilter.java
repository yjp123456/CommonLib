package com.commonlib.filter;


import com.commonlib.gl.basis.AbsMTGLFilter;
import com.commonlib.gl.data.AbsVertexData;
import com.commonlib.gl.data.MTGLTextureVertexData;
import com.commonlib.gl.util.MTGLUtil;

/**
 * 处理视频流渲染，需要使用特定的脚本，并且脚本里面不能同时存在视频流纹理和普通2d纹理，
 * 需要先将视频流纹理绘制到fbo中，再拿fbo做其他渲染
 */

public class OESFilter extends AbsMTGLFilter {
    private static final String TAG = "Src Image Filter";


    private static final String VERTEX_SHADER_ASSETS_PATH = "oes_script/oes.vs";
    private static final String FRAGMENT_SHADER_ASSETS_PATH = "oes_script/oes.fs";


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

    }

    @Override
    protected AbsVertexData getVertexData() {
        return new MTGLTextureVertexData();
    }

    @Override
    protected void updateData() {


    }


    @Override
    protected void resetParams() {

    }


    public void handleActionDown(float startX, float startY) {

    }

    public void handleActionUp(float x, float y, boolean isMove) {

    }

    @Override
    protected void handleActionMove(float x, float y) {

    }
}
