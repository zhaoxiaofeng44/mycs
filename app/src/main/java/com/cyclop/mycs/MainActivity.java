package com.cyclop.mycs;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.cyclop.angle.AngleActivity;
import com.cyclop.angle.AngleRotatingSprite;
import com.cyclop.angle.AngleSprite;
import com.cyclop.angle.AngleSpriteLayout;
import com.cyclop.angle.AngleUI;
import com.cyclop.angle.FPSCounter;

public class MainActivity extends AngleActivity
{

    private class MyDemo extends AngleUI
    {
        //Now our rolling sprite(s) will be in our new UI
        //>Ahora nuestro(s) sprite(s) giratorio(s) estar�(n) en nuestra nueva UI
        AngleSpriteLayout mLogoLayout;

        public MyDemo(AngleActivity activity)
        {
            super(activity);
            mLogoLayout = new AngleSpriteLayout(mGLSurfaceView, 128, 128, R.drawable.anglelogo);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event)
        {
            if (event.getAction()==MotionEvent.ACTION_DOWN)
            {
                //Add a new MyAnimatedSprite on touch position
                //To access mGLSurfaceView we will use mActivity
                //>A�adimos un nuevo MyAnimatedSprite en la posici�n donde se ha pulsado
                //>Para acceder a mGLSurfaceView usaremos mActivity
                //mActivity.mGLSurfaceView.addObject(new MyAnimatedSprite ((int)event.getX(), (int)event.getY(), mLogoLayout));

                mActivity.mGLSurfaceView.addObject(new AngleSprite((int)event.getX(), (int)event.getY(), mLogoLayout));

                return true;
            }
            return super.onTouchEvent(event);
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        mGLSurfaceView.addObject(new FPSCounter());

        FrameLayout mMainLayout=new FrameLayout(this);
        mMainLayout.addView(mGLSurfaceView);
        setContentView(mMainLayout);

        //Set current UI (create inline)
        //>Fijamos la UI activa. (creaci�n inline)
        setUI(new MyDemo(this));
    }
}
