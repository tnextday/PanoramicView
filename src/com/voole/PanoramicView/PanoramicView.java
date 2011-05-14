package com.voole.PanoramicView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by IntelliJ IDEA.
 * User:  霍峰
 * Date: 11-5-3
 * Time: 上午9:50
 * Description:
 */
public class PanoramicView extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Intent intent = getIntent();
        String texPath = intent.getStringExtra("texturePath");

        // Create our Preview view and set it as the content of our
        // Activity
        mGLSurfaceView = new TouchGLSurfaceView(this);
        mPanoramic = new PanoramicRenderer(texPath);
        mGLSurfaceView.setRenderer(mPanoramic);
//        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        setContentView(mGLSurfaceView);
        mGLSurfaceView.requestFocus();
        mGLSurfaceView.setFocusableInTouchMode(true);
    }

    
    @Override
    protected void onResume() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onPause();
        mGLSurfaceView.onPause();
    }

    private TouchGLSurfaceView mGLSurfaceView;
    private PanoramicRenderer mPanoramic;

    static private class TouchGLSurfaceView extends GLSurfaceView{
        private PanoramicRenderer mRender;

        public TouchGLSurfaceView(Context context) {
            super(context);
        }

        public void setRenderer(PanoramicRenderer renderer) {
            super.setRenderer(renderer);
            mRender = renderer;
            
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            float x = e.getX();
            float y = e.getY();
            switch (e.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    return mRender.onTouchMove(x, y);
                case MotionEvent.ACTION_DOWN:
                    return mRender.onTouchDown(x, y);
                case MotionEvent.ACTION_UP:
                    return mRender.onTouchUp(x, y);
            }
            return true;
        }
    }
}
