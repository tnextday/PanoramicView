/**
 * 
 */
package com.voole.PanoramicView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

class Grid {
    // Size of vertex data elements in bytes:
    final static int FLOAT_SIZE = 4;
    final static int CHAR_SIZE = 2;

    // Vertex structure:
    // float x, y, z;
    // float u, v;
    // float weight0, weight1;
    // byte palette0, palette1, pad0, pad1;

    final static int VERTEX_SIZE = 8 * FLOAT_SIZE;
    final static int VERTEX_TEXTURE_BUFFER_INDEX_OFFSET = 3;
    final static int VERTEX_WEIGHT_BUFFER_INDEX_OFFSET = 5;
    final static int VERTEX_PALETTE_INDEX_OFFSET = 7 * FLOAT_SIZE;

    private int mVertexBufferObjectId;
    private int mElementBufferObjectId;

    // These buffers are used to hold the vertex and index data while
    // constructing the grid. Once createBufferObjects() is called
    // the buffers are nulled out to save memory.

    private ByteBuffer mVertexByteBuffer;
    private FloatBuffer mVertexBuffer;
    private CharBuffer mIndexBuffer;

    private int mW;
    private int mH;
    private int mIndexCount;

    public Grid(int w, int h) {
        if (w < 0 || w >= 65536) {
            throw new IllegalArgumentException("w");
        }
        if (h < 0 || h >= 65536) {
            throw new IllegalArgumentException("h");
        }
        if (w * h >= 65536) {
            throw new IllegalArgumentException("w * h >= 65536");
        }

        mW = w;
        mH = h;
        int size = w * h;

        mVertexByteBuffer = ByteBuffer.allocateDirect(VERTEX_SIZE * size)
            .order(ByteOrder.nativeOrder());
        mVertexBuffer = mVertexByteBuffer.asFloatBuffer();

        int quadW = mW - 1;
        int quadH = mH - 1;
        int quadCount = quadW * quadH;
        int indexCount = quadCount * 6;
        mIndexCount = indexCount;
        mIndexBuffer = ByteBuffer.allocateDirect(CHAR_SIZE * indexCount)
            .order(ByteOrder.nativeOrder()).asCharBuffer();

        /*
         * Initialize triangle list mesh.
         *
         *     [0]-----[  1] ...
         *      |    /   |
         *      |   /    |
         *      |  /     |
         *     [w]-----[w+1] ...
         *      |       |
         *
         */

        {
            int i = 0;
            for (int y = 0; y < quadH; y++) {
                for (int x = 0; x < quadW; x++) {
                    char a = (char) (y * mW + x);
                    char b = (char) (y * mW + x + 1);
                    char c = (char) ((y + 1) * mW + x);
                    char d = (char) ((y + 1) * mW + x + 1);

                    mIndexBuffer.put(i++, a);
                    mIndexBuffer.put(i++, c);
                    mIndexBuffer.put(i++, b);

                    mIndexBuffer.put(i++, b);
                    mIndexBuffer.put(i++, c);
                    mIndexBuffer.put(i++, d);
                }
            }
        }

    }

    public void set(int i, int j, float x, float y, float z,
            float u, float v,
            float w0, float w1,
            int p0, int p1) {
        if (i < 0 || i >= mW) {
            throw new IllegalArgumentException("i");
        }
        if (j < 0 || j >= mH) {
            throw new IllegalArgumentException("j");
        }

        if (w0 + w1 != 1.0f) {
            throw new IllegalArgumentException("Weights must add up to 1.0f");
        }

        int index = mW * j + i;

        mVertexBuffer.position(index * VERTEX_SIZE / FLOAT_SIZE);
        mVertexBuffer.put(x);
        mVertexBuffer.put(y);
        mVertexBuffer.put(z);
        mVertexBuffer.put(u);
        mVertexBuffer.put(v);
        mVertexBuffer.put(w0);
        mVertexBuffer.put(w1);

        mVertexByteBuffer.position(index * VERTEX_SIZE + VERTEX_PALETTE_INDEX_OFFSET);
        mVertexByteBuffer.put((byte) p0);
        mVertexByteBuffer.put((byte) p1);
    }

    public void createBufferObjects(GL gl) {
        // Generate a the vertex and element buffer IDs
        int[] vboIds = new int[2];
        GL11 gl11 = (GL11) gl;
        gl11.glGenBuffers(2, vboIds, 0);
        mVertexBufferObjectId = vboIds[0];
        mElementBufferObjectId = vboIds[1];

        // Upload the vertex data
        gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mVertexBufferObjectId);
        mVertexByteBuffer.position(0);
        gl11.glBufferData(GL11.GL_ARRAY_BUFFER, mVertexByteBuffer.capacity(), mVertexByteBuffer, GL11.GL_STATIC_DRAW);

        gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, mElementBufferObjectId);
        mIndexBuffer.position(0);
        gl11.glBufferData(GL11.GL_ELEMENT_ARRAY_BUFFER, mIndexBuffer.capacity() * CHAR_SIZE, mIndexBuffer, GL11.GL_STATIC_DRAW);

        // We don't need the in-memory data any more
        mVertexBuffer = null;
        mVertexByteBuffer = null;
        mIndexBuffer = null;
    }

    public void draw(GL10 gl) {
        GL11 gl11 = (GL11) gl;
        GL11Ext gl11Ext = (GL11Ext) gl;

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mVertexBufferObjectId);
        gl11.glVertexPointer(3, GL10.GL_FLOAT, VERTEX_SIZE, 0);
        gl11.glTexCoordPointer(2, GL10.GL_FLOAT, VERTEX_SIZE, VERTEX_TEXTURE_BUFFER_INDEX_OFFSET * FLOAT_SIZE);

        gl.glEnableClientState(GL11Ext.GL_MATRIX_INDEX_ARRAY_OES);
        gl.glEnableClientState(GL11Ext.GL_WEIGHT_ARRAY_OES);

        gl11Ext.glWeightPointerOES(2, GL10.GL_FLOAT, VERTEX_SIZE, VERTEX_WEIGHT_BUFFER_INDEX_OFFSET  * FLOAT_SIZE);
        gl11Ext.glMatrixIndexPointerOES(2, GL10.GL_UNSIGNED_BYTE, VERTEX_SIZE, VERTEX_PALETTE_INDEX_OFFSET );

        gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, mElementBufferObjectId);
        gl11.glDrawElements(GL10.GL_TRIANGLES, mIndexCount, GL10.GL_UNSIGNED_SHORT, 0);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL11Ext.GL_MATRIX_INDEX_ARRAY_OES);
        gl.glDisableClientState(GL11Ext.GL_WEIGHT_ARRAY_OES);
        gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
        gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
    }
}