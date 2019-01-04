package com.commonlib.gl.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glGenBuffers;
import static com.commonlib.gl.util.MTGLConstants.BYTE_PER_SHORT;


/**
 * Created by jieping on 2018/5/9.
 */


public class IndexBuffer {
    private final int bufferId;
    private final int numElements;

    public IndexBuffer(short[] vertexData) {
        numElements = vertexData.length;
        final int buffers[] = new int[1];
        glGenBuffers(buffers.length, buffers, 0);
        if (buffers[0] == 0) {
            throw new RuntimeException("Could not create a new buffer");
        }
        bufferId = buffers[0];

        //bind to the buffer
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, buffers[0]);

        //transfer data to native memory
        ShortBuffer vertexArray = ByteBuffer.allocateDirect(vertexData.length * BYTE_PER_SHORT)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(vertexData);
        vertexArray.position(0);

        //transfer data from native memory to the GPU buffer
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, vertexArray.capacity() * BYTE_PER_SHORT, vertexArray, GL_STATIC_DRAW);

        //Unbind from the buffer when we're done with it
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public int getBufferId() {
        return bufferId;
    }

    public int getNumElements() {
        return numElements;
    }

}
