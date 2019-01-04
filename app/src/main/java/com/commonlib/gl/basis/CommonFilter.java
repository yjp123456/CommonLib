package com.commonlib.gl.basis;


import com.commonlib.gl.data.AbsVertexData;
import com.commonlib.gl.data.MTGLTextureVertexData;
import com.commonlib.gl.util.MTGLUtil;

/**
 * Created by jieping on 2018/5/10.
 */

public class CommonFilter extends AbsMTGLFilter {
    private static final String TAG = "Src Image Filter";


    private static final String VERTEX_SHADER_ASSETS_PATH = "basic/basic.vs";
    private static final String FRAGMENT_SHADER_ASSETS_PATH = "basic/basic.fs";


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
