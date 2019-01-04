package com.commonlib.gl.data;


import com.commonlib.gl.basis.AbsMTGLFilter;

import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glDrawElements;
import static com.commonlib.gl.data.VertexArray.BYTE_PER_FLOAT;


public class MTGLTextureVertexData extends AbsVertexData {


    public static final int POSITION_COMPONENT_COUNT = 2;
    public static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
    public static final int STRIDE = (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTE_PER_FLOAT;

    public float[] VERTEX_DATA = new float[]{
            //opengl在手机上x,y,z坐标范围都是[-1,1],左下角是[-1,-1],右上角是[1,1]
            //triangle FAN  x,y,S,T S和T代表纹理坐标，范围都是[0,1],纹理左上角是（0,0），右下角是（1,1)
            0.0f, 0.0f, 0.5f, 0.5f,
            -1.0f, -1.0f, 0f, 1.0f,
            1.0f, -1.0f, 1f, 1f,
            1.0f, 1.0f, 1f, 0.0f,
            -1.0f, 1.0f, 0f, 0.0f,
            -1.0f, -1.0f, 0f, 1f
    };

    public MTGLTextureVertexData() {
        initData(VERTEX_DATA);
    }

    /**
     * 顶点坐标
     */
    private float[] posPosition = new float[]{
            0, 0,
            -1, -1,
            1, -1,
            1, 1,
            -1, 1,
            -1, -1
    };


    @Override
    public void updateVertexData(float ratioWidth, float ratioHeight) {

        VERTEX_DATA = new float[]{
                posPosition[0] * ratioWidth, posPosition[1] * ratioHeight, 0.5f, 0.5f,
                posPosition[2] * ratioWidth, posPosition[3] * ratioHeight, 0f, 1.0f,
                posPosition[4] * ratioWidth, posPosition[5] * ratioHeight, 1f, 1f,
                posPosition[6] * ratioWidth, posPosition[7] * ratioHeight, 1f, 0.0f,
                posPosition[8] * ratioWidth, posPosition[9] * ratioHeight, 0f, 0.0f,
                posPosition[10] * ratioWidth, posPosition[11] * ratioHeight, 0f, 1f
        };
       /* for(int i=0;i<VERTEX_DATA.fileLength;i+=2){
            float[] center = new float[]{VERTEX_DATA[0],VERTEX_DATA[1]};
            float[] origin = new float[]{VERTEX_DATA[i],VERTEX_DATA[i+1]};
            rotate(center,origin,45);
            VERTEX_DATA[i] = origin[0];
            VERTEX_DATA[i+1] = origin[1];
        }*/
        vertexArray.updateBuffer(VERTEX_DATA);
    }


    @Override
    public void bindData(AbsMTGLFilter filter) {
        vertexArray.setVertexAttribPointer(0, filter.getPositionAttributeLocation(), POSITION_COMPONENT_COUNT, STRIDE);
        vertexArray.setVertexAttribPointer(POSITION_COMPONENT_COUNT, filter.getTextureCoordinatesAttributeLocation(), TEXTURE_COORDINATES_COMPONENT_COUNT, STRIDE);
    }

    @Override
    public void draw() {
        glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
    }

    @Override
    public void drawElements() {
        if (indexBuffer != null) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer.getBufferId());
            glDrawElements(GL_TRIANGLES, indexBuffer.getNumElements(), GL_UNSIGNED_SHORT, 0);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        }
    }

}
