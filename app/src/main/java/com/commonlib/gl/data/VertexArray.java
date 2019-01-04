package com.commonlib.gl.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glVertexAttribPointer;


/**
 * 顶点数据封装类
 */
public class VertexArray {
    /**
     * float类型所占字节数
     */
    static final int BYTE_PER_FLOAT = 4;
    private final FloatBuffer floatBuffer;


    public VertexArray(float[] vertexData) {
        floatBuffer = ByteBuffer.allocateDirect(vertexData.length * BYTE_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);//最后记得一定要put进去，否则数据是没有放到native memory中的
    }

    /**
     * 设置顶点数据
     *
     * @param dataOffset        首位偏移量
     * @param attributeLocation 属性location
     * @param componentCount    组成每个点所需要的属性个数
     * @param stride            连续两个点之间的间隔，比如两个color之间，两个position之间
     */
    public void setVertexAttribPointer(int dataOffset, int attributeLocation, int componentCount, int stride) {
        floatBuffer.position(dataOffset);
        glVertexAttribPointer(attributeLocation, componentCount, GL_FLOAT, false, stride, floatBuffer);
        glEnableVertexAttribArray(attributeLocation);
        floatBuffer.position(0);
    }

    /**
     * 更新点数据
     *
     * @param vertexData 新数据
     */
    public void updateBuffer(float[] vertexData) {
        floatBuffer.position(0);
        floatBuffer.put(vertexData, 0, vertexData.length);
        floatBuffer.position(0);
    }


}
