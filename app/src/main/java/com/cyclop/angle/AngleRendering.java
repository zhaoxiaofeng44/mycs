package com.cyclop.angle;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

/**
 * Sprite base class
 * @author Ivan Pajuelo
 *
 */
public abstract class AngleRendering extends AngleObject
{
	public AngleVector mPosition; //Set to change the position of the sprite
	public AngleVector mScale; //Set to change the scale of the sprite
	public float mRotation;
	public float mRed;   //Red tint (0 - 1)
	public float mGreen;	//Green tint (0 - 1)
	public float mBlue;	//Blue tint (0 - 1)
	public float mAlpha;	//Alpha channel (0 - 1)

	//index
	protected int mIndexSize;
	protected int mVertexSize;
	//size
	protected CharBuffer mIndexBuffer;
	protected FloatBuffer mVertexBuffer;
	protected FloatBuffer mTexCoordBuffer;

	protected int mHardwareTextureID;
	protected int mHardwareVertBufferIndex;
	protected int mHardwareTexCoordBufferIndex;

	public AngleRendering(int verSize,int indexSize)
	{
		doInit(verSize,indexSize);
	}

	private void doInit(int verSize,int indexSize)
	{
		mScale = new AngleVector(1, 1);
		mPosition = new AngleVector(0, 0);
		mRotation = 0;
		mRed= mGreen = mBlue = mAlpha = 1;

		mHardwareTextureID = -1;
		mHardwareVertBufferIndex = -1;
		mHardwareTexCoordBufferIndex = -1;

		mVertexSize = verSize;
		mIndexSize = indexSize;

		mIndexBuffer = ByteBuffer.allocateDirect(mIndexSize  * 2).order(ByteOrder.nativeOrder()).asCharBuffer();
		mVertexBuffer = ByteBuffer.allocateDirect(mVertexSize * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mTexCoordBuffer = ByteBuffer.allocateDirect(mVertexSize * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
	}

	@Override
	public void invalidateTexture(GL10 gl)
	{
		super.invalidateTexture(gl);
	}

	@Override
	public void invalidateHardwareBuffers(GL10 gl)
	{
		int[] hwBuffers = new int[2];
		((GL11) gl).glGenBuffers(2, hwBuffers, 0);

		// Allocate and fill the texture buffer.
		mHardwareTexCoordBufferIndex = hwBuffers[0];
		((GL11) gl).glBindBuffer(GL11.GL_ARRAY_BUFFER, mHardwareTexCoordBufferIndex);
		((GL11) gl).glBufferData(GL11.GL_ARRAY_BUFFER, mVertexSize * 4, mTexCoordBuffer, GL11.GL_STATIC_DRAW);
		mHardwareVertBufferIndex = hwBuffers[1];
		((GL11) gl).glBindBuffer(GL11.GL_ARRAY_BUFFER, mHardwareVertBufferIndex);
		((GL11) gl).glBufferData(GL11.GL_ARRAY_BUFFER, mVertexSize * 4, mVertexBuffer, GL11.GL_STATIC_DRAW);
		((GL11) gl).glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);

		super.invalidateHardwareBuffers(gl);
	}

	@Override
	public void releaseHardwareBuffers(GL10 gl)
	{
		int[] hwBuffers = new int[2];
		hwBuffers[0] = mHardwareTexCoordBufferIndex;
		hwBuffers[1] = mHardwareVertBufferIndex;
		if (gl!=null)
			((GL11) gl).glDeleteBuffers(2, hwBuffers, 0);
		mHardwareTexCoordBufferIndex = -1;
		mHardwareVertBufferIndex = -1;
	}

	@Override
	public void draw(GL10 gl)
	{
		if (mHardwareTextureID > -1)
		{
			Log.v("draw","mHardwareTextureID  " +mHardwareTextureID+"  mIndexSize  " + mIndexSize);
			gl.glPushMatrix();
			gl.glLoadIdentity();

			if ((mPosition.mX != 1) || (mPosition.mY != 1))
				gl.glTranslatef(mPosition.mX, mPosition.mY, 0);

			if (mRotation != 0)
				gl.glRotatef(-mRotation, 0, 0, 1);

			if ((mScale.mX != 1) || (mScale.mY != 1))
				gl.glScalef(mScale.mX, mScale.mY, 1);

			gl.glColor4f(mRed, mGreen, mBlue, mAlpha);
			gl.glBindTexture(GL10.GL_TEXTURE_2D, mHardwareTextureID);

			if (AngleSurfaceView.sUseHWBuffers)
			{
				if ((mHardwareTexCoordBufferIndex < 0) || (mHardwareVertBufferIndex < 0))
					invalidateHardwareBuffers(gl);

				((GL11) gl).glBindBuffer(GL11.GL_ARRAY_BUFFER, mHardwareVertBufferIndex);
				((GL11) gl).glVertexPointer(2, GL10.GL_FLOAT, 0, 0);

				((GL11) gl).glBindBuffer(GL11.GL_ARRAY_BUFFER, mHardwareTexCoordBufferIndex);
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
				gl.glDrawElements(GL10.GL_TRIANGLES,mIndexBuffer.length(), GL10.GL_UNSIGNED_SHORT, mIndexBuffer);
			}
			gl.glPopMatrix();
		}
		super.draw(gl);
	}

}


