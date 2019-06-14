package com.cyclop.angle;

import javax.microedition.khronos.opengles.GL10;

/**
 * Fastest sprite with no rotation support
 * 
 * @author Ivan Pajuelo
 * 
 */
public class AngleSprite extends AngleRendering
{
	public AngleSpriteLayout roLayout; //Sprite Layout with information about how to draw the sprite
	public boolean mFlipHorizontal;
	public boolean mFlipVertical;
	public int roFrame; //Frame number. (ReadOnly)
	private boolean isFrameInvalid;

	/**
	 * 
	 * @param layout AngleSpriteLayout
	 */
	public AngleSprite(AngleSpriteLayout layout)
	{
		super(8,6);
		doInit(0, 0, 1,layout);
	}

	/**
	 * 
	 * @param x Position
	 * @param y Position
	 * @param layout AngleSpriteLayout
	 */
	public AngleSprite(int x, int y, AngleSpriteLayout layout)
	{
		super(8,6);
		doInit(x, y, 1, layout);
	}
	
	/**
	 * 
	 * @param x Position
	 * @param y Position
	 * @param alpha Normalized alpha channel value
	 * @param layout AngleSpriteLayout
	 */
	public AngleSprite(int x, int y, float alpha, AngleSpriteLayout layout)
	{
		super(8,6);
		doInit(x, y, alpha, layout);
	}

	private void doInit(int x, int y, float alpha,AngleSpriteLayout layout)
	{
		mPosition.set(x,y);
		mAlpha = alpha;
		for (int i = 0; i < AngleSurfaceView.sIndexValues.length; ++i)
			mIndexBuffer.put(i, AngleSurfaceView.sIndexValues[i]);

		setLayout(layout);
	}

	public void setLayout(AngleSpriteLayout layout)
	{
		roLayout = layout;
		setFrame(0);
	}

	public void setFrame(int frame)
	{
		if (roLayout != null)
		{
			if (frame < roLayout.roFrameCount)
			{
				roFrame = frame;
				float W = roLayout.roTexture.mWidth;
				float H = roLayout.roTexture.mHeight;
				if ((W>0)&&(H>0))
				{
					float frameLeft = (roFrame % roLayout.mFrameColumns) * roLayout.roCropWidth;
					float frameTop = (roFrame / roLayout.mFrameColumns) * roLayout.roCropHeight;

					float left = (roLayout.roCropLeft + frameLeft) / W;
					float bottom = (roLayout.roCropTop + roLayout.roCropHeight + frameTop) / H;
					float right = (roLayout.roCropLeft + roLayout.roCropWidth + frameLeft) / W;
					float top = (roLayout.roCropTop + frameTop) / H;

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
			}
		}
	}


	@Override
	public void draw(GL10 gl)
	{
		if (roLayout != null && roLayout.roTexture != null){
			if (roLayout.roTexture.mHWTextureID < 0){
				roLayout.roTexture.linkToGL(gl);
			}
			mHardwareTextureID = roLayout.roTexture.mHWTextureID;
		}
		else{
			mHardwareTextureID = -1;
		}
		super.draw(gl);
	}
}
