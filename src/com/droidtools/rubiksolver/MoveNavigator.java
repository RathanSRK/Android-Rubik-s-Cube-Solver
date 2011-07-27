package com.droidtools.rubiksolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MoveNavigator extends SurfaceView implements
		SurfaceHolder.Callback {

	SurfaceHolder mSurfaceHolder;
	int mCanvasWidth;
	int mCanvasHeight;
	int mPosition;
	boolean mRunning;
	List<RubikMove> mSol;
	Map<String, Bitmap> icons;
	Thread drawThread;

	public MoveNavigator(Context context, AttributeSet attrs) {
		super(context, attrs);
		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);
		setFocusable(true); // make sure we get key events
		//setWillNotDraw(false);
	}

	public void init(List<RubikMove> sol, int pos) {
		mSol = sol;
		mPosition = pos;
		icons = new HashMap<String, Bitmap>();
		for (Map.Entry<String, Integer> entry : RubikMove.icons.entrySet()) {
			Bitmap icon = BitmapFactory.decodeResource(getResources(),
                    entry.getValue());
			icons.put(entry.getKey(), icon);
		}
		
		drawThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (mRunning) {
					Canvas c = null;
					try {
						c = mSurfaceHolder.lockCanvas(null);
						synchronized (mSurfaceHolder) {
							doDraw(c);
						}
					} finally {
						// do this in a finally so that if an exception is
						// thrown
						// during the above, we don't leave the Surface in an
						// inconsistent state
						if (c != null) {
							mSurfaceHolder.unlockCanvasAndPost(c);
						}
					}
				}
			}

		}, "Cube Drawer");
	}
	
	public void setRunning(boolean running) {
		mRunning = running;
	}
	
	public void setPosition(int pos) {
		mPosition = pos;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		synchronized (mSurfaceHolder) {
			Log.d("SURFACE_CHANGE",
					String.format("Width - %d Height - %d", width, height));
			mCanvasWidth = width;
			mCanvasHeight = height;
		}

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!isInEditMode()) {
			setRunning(true);
			drawThread.start();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		
		boolean retry = true;
		setRunning(false);
		retry = true;
		while (retry) {
			try {
				drawThread.join();
				retry = false;
			} catch (InterruptedException e) {
			}
		}
		Log.d("SURFACE", "draw thread dead");
	}
	

	public void doDraw(Canvas canvas) {
		final int sectionSize = 50;
		int num = mCanvasWidth / sectionSize;
		if  (num % 2 == 0) 
			num--;
		int s = (mPosition - num/2);
		int e = (mPosition + num/2)+1;
		int x;
		for (int i=s; i<e; i++) {
			if (i < 0 || i >= mSol.size()) {
				continue;
			}
			x = 10 + sectionSize*(i-s);
			android.util.Log.d("MOVENAV X:", ""+x+","+s+","+e+","+num+","+mCanvasWidth);
			if (i == mPosition) {
				Paint paint = new Paint(); 
		        paint.setStyle(Style.FILL); 
		        paint.setARGB(255, 80, 80, 80); 
		        canvas.drawRect(new RectF(x,0,x+sectionSize,sectionSize), paint);
			}
			canvas.drawBitmap(icons.get(mSol.get(i).getMoveRep()), x+7, 7, null);
		}
	}

}
