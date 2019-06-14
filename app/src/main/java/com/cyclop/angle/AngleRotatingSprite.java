package com.cyclop.angle;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

/**
 * Sprite with rotating capabilities. Uses hardware buffers if available
 * 
 * @author Ivan Pajuelo
 * 
 */
public class AngleRotatingSprite extends AngleAbstractSprite
{
    public float mRotation;
    protected int mTextureCoordBufferIndex;
    public int mVertBufferIndex;
    private boolean isFrameInvalid;

    //size
    protected CharBuffer mIndexBuffer;
    protected FloatBuffer mVertexBuffer;
    protected FloatBuffer mTexCoordBuffer;


    /**
     *
     * @param layout AngleSpriteLayout
     */
    public AngleRotatingSprite(AngleSpriteLayout layout)
    {
        super(layout);
        doInit(0, 0, 1);
    }

    /**
     *
     * @param x Position
     * @param y Position
     * @param layout AngleSpriteLayout
     */
    public AngleRotatingSprite(int x, int y, AngleSpriteLayout layout)
    {
        super(layout);
        doInit(x, y, 1);
    }

    /**
     *
     * @param x Position
     * @param y Position
     * @param alpha Normalized alpha channel value
     * @param layout AngleSpriteLayout
     */
    public AngleRotatingSprite(int x, int y, float alpha, AngleSpriteLayout layout)
    {
        super(layout);
        doInit(x, y, alpha);
    }

    private void doInit(int x, int y, float alpha)
    {
        mRotation = 0;

        mTextureCoordBufferIndex = -1;

        mVertBufferIndex = -1;

        mIndexBuffer = ByteBuffer.allocateDirect(6  * 2).order(ByteOrder.nativeOrder()).asCharBuffer();
        mVertexBuffer = ByteBuffer.allocateDirect(8 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTexCoordBuffer = ByteBuffer.allocateDirect(8 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();

        for (int i = 0; i < AngleSurfaceView.sIndexValues.length; ++i)
            mIndexBuffer.put(i, AngleSurfaceView.sIndexValues[i]);

        setLayout(roLayout);
        mPosition.set(x,y);
        mAlpha=alpha;
        isFrameInvalid=true;
    }

    @Override
    public void setLayout(AngleSpriteLayout layout)
    {
        super.setLayout(layout);
        setFrame(0);
    }

    @Override
    public void invalidateTexture(GL10 gl)
    {
        setFrame(roFrame);
        super.invalidateTexture(gl);
    }

    @Override
    public void setFrame(int frame)
    {
        if (roLayout != null)
        {
            if (frame < roLayout.roFrameCount)
            {
                roFrame = frame;
                float W = roLayout.roTexture.mWidth;
                float H = roLayout.roTexture.mHeight;
                if ((W>0)&(H>0))
                {
                    float frameLeft = (roFrame % roLayout.mFrameColumns) * roLayout.roCropWidth;
                    float frameTop = (roFrame / roLayout.mFrameColumns) * roLayout.roCropHeight;

                    float left = (roLayout.roCropLeft + frameLeft) / W;
                    float bottom = (roLayout.roCropTop + roLayout.roCropHeight + frameTop) / H;
                    float right = (roLayout.roCropLeft + roLayout.roCropWidth + frameLeft) / W;
                    float top = (roLayout.roCropTop + frameTop) / H;

                    isFrameInvalid=false;

                    if (mFlipHorizontal)
                    {
                        mTexCoordBuffer.put(0, right);
                        mTexCoordBuffer.put(2, left);
                        mTexCoordBuffer.put(4, right);
                        mTexCoordBuffer.put(6, left);
                    }
                    else
                    {
                        mTexCoordBuffer.put(0, left);
                        mTexCoordBuffer.put(2, right);
                        mTexCoordBuffer.put(4, left);
                        mTexCoordBuffer.put(6, right);
                    }
                    if (mFlipVertical)
                    {
                        mTexCoordBuffer.put(1, top);
                        mTexCoordBuffer.put(3, top);
                        mTexCoordBuffer.put(5, bottom);
                        mTexCoordBuffer.put(7, bottom);
                    }
                    else
                    {
                        mTexCoordBuffer.put(1, bottom);
                        mTexCoordBuffer.put(3, bottom);
                        mTexCoordBuffer.put(5, top);
                        mTexCoordBuffer.put(7, top);
                    }
                    roLayout.fillVertexValues(roFrame, mVertexBuffer);

                }
                mTextureCoordBufferIndex=-1;
                mVertBufferIndex=-1;
            }
        }
    }

    @Override
    public void invalidateHardwareBuffers(GL10 gl)
    {
        int[] hwBuffers = new int[2];
        ((GL11) gl).glGenBuffers(2, hwBuffers, 0);

        // Allocate and fill the texture buffer.
        mTextureCoordBufferIndex = hwBuffers[0];
        ((GL11) gl).glBindBuffer(GL11.GL_ARRAY_BUFFER, mTextureCoordBufferIndex);
        ((GL11) gl).glBufferData(GL11.GL_ARRAY_BUFFER, 8 * 4, mTexCoordBuffer, GL11.GL_STATIC_DRAW);
        mVertBufferIndex = hwBuffers[1];
        ((GL11) gl).glBindBuffer(GL11.GL_ARRAY_BUFFER, mVertBufferIndex);
        ((GL11) gl).glBufferData(GL11.GL_ARRAY_BUFFER, 8 * 4, mVertexBuffer, GL11.GL_STATIC_DRAW);
        ((GL11) gl).glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);

        super.invalidateHardwareBuffers(gl);

    }

    @Override
    public void releaseHardwareBuffers(GL10 gl)
    {
        int[] hwBuffers = new int[2];
        hwBuffers[0] = mTextureCoordBufferIndex;
        hwBuffers[1] = mVertBufferIndex;
        if (gl!=null)
            ((GL11) gl).glDeleteBuffers(2, hwBuffers, 0);
        mTextureCoordBufferIndex = -1;
        mVertBufferIndex = -1;
    }

    @Override
    public void draw(GL10 gl)
    {
        if (roLayout != null)
        {
            if (roLayout.roTexture != null)
            {
                if (roLayout.roTexture.mHWTextureID > -1)
                {
                    //if (isFrameInvalid)
                        //setFrame(roFrame);

                    gl.glPushMatrix();
                    gl.glLoadIdentity();

                    //gl.glTranslatef(mPosition.mX, mPosition.mY, 0);
                    if (mRotation != 0)
                        gl.glRotatef(-mRotation, 0, 0, 1);
                    if ((mScale.mX != 1) || (mScale.mY != 1))
                        gl.glScalef(mScale.mX, mScale.mY, 1);

                    gl.glBindTexture(GL10.GL_TEXTURE_2D, roLayout.roTexture.mHWTextureID);
                    gl.glColor4f(mRed, mGreen, mBlue, mAlpha);

                    if (AngleSurfaceView.sUseHWBuffers)
                    {
                        if ((mTextureCoordBufferIndex < 0)||(mVertBufferIndex < 0))
                            invalidateHardwareBuffers(gl);

                        ((GL11) gl).glBindBuffer(GL11.GL_ARRAY_BUFFER, mVertBufferIndex);
                        ((GL11) gl).glVertexPointer(2, GL10.GL_FLOAT, 0, 0);

                        ((GL11) gl).glBindBuffer(GL11.GL_ARRAY_BUFFER, mTextureCoordBufferIndex);
                        ((GL11) gl).glTexCoordPointer(2, GL10.GL_FLOAT, 0, 0);

                        ((GL11) gl).glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, AngleSurfaceView.roIndexBufferIndex);
                        ((GL11) gl).glDrawElements(GL10.GL_TRIANGLES, 6, GL10.GL_UNSIGNED_SHORT, 0);

                        ((GL11) gl).glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
                        ((GL11) gl).glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
                    }
                    else
                    {
                        gl.glVertexPointer(2, GL10.GL_FLOAT, 0, mVertexBuffer);
                        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexCoordBuffer);
                        gl.glDrawElements(GL10.GL_TRIANGLES, AngleSurfaceView.sIndexValues.length, GL10.GL_UNSIGNED_SHORT, mIndexBuffer);
                    }

                    gl.glPopMatrix();
                }
                else
                    roLayout.roTexture.linkToGL(gl);
            }
        }
        super.draw(gl);
    }

}
