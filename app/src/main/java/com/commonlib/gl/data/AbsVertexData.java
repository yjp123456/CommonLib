package com.commonlib.gl.data;


import com.commonlib.gl.basis.AbsMTGLFilter;

public abstract class AbsVertexData {

    VertexArray vertexArray;
    IndexBuffer indexBuffer;


    public void initData(float[] data) {

        vertexArray = new VertexArray(data);

    }

    public VertexArray getVertexArray() {
        return vertexArray;
    }

    /**
     * 绑定顶点数据
     */
    public abstract void bindData(AbsMTGLFilter filter);

    /**
     * 绘制Texture
     */
    public abstract void draw();

    public abstract void drawElements();

    /**
     * 更新顶点数据
     *
     * @param ratioWidth  宽度比例
     * @param ratioHeight 高度比例
     */
    public abstract void updateVertexData(float ratioWidth, float ratioHeight);

    public void setVertexData(float[] vertexData) {
        vertexArray.updateBuffer(vertexData);
    }

    public void setIndexData(short[] indexData) {
        indexBuffer = new IndexBuffer(indexData);
    }
}
