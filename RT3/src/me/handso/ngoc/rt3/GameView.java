package me.handso.ngoc.rt3;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import me.handso.ngoc.rt3.RT3Fragment.PIECES;
import static me.handso.ngoc.rt3.Generic3x3.*;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
	
	static final String TAG = "GAME_VIEW";
	
	RT3Fragment game_fragment;
	float width, height, side, sthick, lthick, xbegin, ybegin, bigSqr, smallSqr;
	
	Paint x, o, bigWood, smallWood, neutral, white, xbg, obg;
	
	boolean listening = false;
	boolean drawable = false;

	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		game_fragment = (RT3Fragment) getParent();
		getHolder().addCallback(this);
		Log.d(TAG, "in Constructor");
		
		x = new Paint();
		o = new Paint();
		bigWood = new Paint();
		smallWood = new Paint();
		neutral = new Paint();
		white = new Paint();
		xbg = new Paint();
		obg = new Paint();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "in surfaceCreated()");
		drawable = true;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "in surfaceDestroyed()");
		drawable = false;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		listening = true;

		width = w;
		height = h;
		side = Math.min(w, h);
		xbegin = (w-side)/2;
		ybegin = (h-side)/2;
		lthick = side/40;
		bigSqr = (side-4*lthick)/3;
		sthick = bigSqr/40;
		smallSqr = (bigSqr-4*sthick)/3;
		
		x = new Paint();
		x.setStrokeWidth(lthick);
		x.setColor(getResources().getColor(R.color.greenX));
		
		o = new Paint();
		o.setStrokeWidth(lthick);
		o.setColor(getResources().getColor(R.color.redO));
		
		bigWood.setColor(getResources().getColor(R.color.lwood));
		bigWood.setStrokeWidth(lthick);
		
		smallWood.setColor(getResources().getColor(R.color.swood));
		smallWood.setStrokeWidth(sthick);
		
		// not implemented yet
		neutral.setColor(getResources().getColor(R.color.greyBG));
		white.setColor(Color.WHITE);
		xbg.setColor(getResources().getColor(R.color.greenBG));
		obg.setColor(getResources().getColor(R.color.redBG));
	}
	
	void initDraw(Greater3x3 x){
		Log.d(TAG, "drawable: "+drawable);
		SurfaceHolder holder = getHolder();
		Canvas canvas = holder.lockCanvas();
		Log.d(TAG, "canvas: "+((canvas == null) ? "" : "not")+" null");
		boardDrawHelper(canvas);
		holder.unlockCanvasAndPost(canvas);
		
		for (int i = 0; i<3; i++) for (int j = 0; j<3; j++)	for (int i1 = 0; i1<3; i1++) for (int j1 = 0; j1<3; j1++){
			int playa = x.board[i][j].board[i1][j1];
			if (playa!=NULL) drawMove(i, j, i1, j1, (playa==P1) ? PIECES.X : PIECES.O);
		}
	}
	
	void boardDrawHelper(Canvas canvas){
		canvas.drawRect(xbegin+bigSqr+lthick, ybegin, xbegin+bigSqr+lthick*2, ybegin+side, bigWood);
		canvas.drawRect(xbegin+bigSqr*2+lthick*2, ybegin, xbegin+bigSqr*2+lthick*3, ybegin+side, bigWood);
		canvas.drawRect(xbegin, ybegin+bigSqr+lthick, xbegin+side, ybegin+bigSqr+lthick*2, bigWood);
		canvas.drawRect(xbegin, ybegin+bigSqr*2+lthick*2, xbegin+side, ybegin+bigSqr*2+lthick*3, bigWood);

		for (int i = 0; i<3; i++) for (int j = 0; j<3; j++) {
			canvas.drawRect(xbegin+smallSqr+lthick+(lthick+bigSqr)*i, ybegin+(lthick+bigSqr)*j,
					xbegin+smallSqr+lthick+sthick+(lthick+bigSqr)*i, ybegin+bigSqr+(lthick+bigSqr)*j, smallWood);
			canvas.drawRect(xbegin+smallSqr*2+lthick+sthick+(lthick+bigSqr)*i, ybegin+(lthick+bigSqr)*j,
					xbegin+bigSqr*2+lthick+sthick*2+(lthick+bigSqr)*i, ybegin+bigSqr+(lthick+bigSqr)*j, smallWood);
			canvas.drawRect(xbegin+(lthick+bigSqr)*i, ybegin+smallSqr+sthick+(lthick+bigSqr)*j,
					xbegin+bigSqr+(lthick+bigSqr)*i, ybegin+smallSqr+lthick+sthick+(lthick+bigSqr)*j, smallWood);
			canvas.drawRect(xbegin+(lthick+bigSqr)*i, ybegin+smallSqr*2+lthick+sthick+(lthick+bigSqr)*j,
					xbegin+bigSqr+(lthick+bigSqr)*i, ybegin+bigSqr*2+lthick+sthick*2+(lthick+bigSqr)*j, smallWood);
		}
	}
	
	void drawMove(int x1, int y1, int x2, int y2, PIECES pieces) {
		int[] c = get2Coord(x1, y1, x2, y2);
		SurfaceHolder holder = getHolder();
		Canvas canvas = holder.lockCanvas();
		drawMoveHelper(c[0], c[1], pieces, canvas);
		holder.unlockCanvasAndPost(canvas);
	}
	
	void drawMoveHelper(float x1, float y1, PIECES pieces, Canvas canvas){
		if (pieces == PIECES.X){
			canvas.drawLine(x1+smallSqr/8, y1+smallSqr/8, x1+smallSqr*7/8, y1+smallSqr*7/8, x);
			canvas.drawLine(x1+smallSqr/8, y1+smallSqr*7/8, x1+smallSqr*7/8, y1+smallSqr/8, x);
		}
		else {
			canvas.drawCircle(x1+smallSqr/2, y1+smallSqr/2, smallSqr*3/8, o);
			canvas.drawCircle(x1+smallSqr/2, y1+smallSqr/2, smallSqr/4, white);
		}
	}

	int[] get4Coord(float x, float y) {
		int[] r = new int[4];
		x -= xbegin;
		y-= ybegin;
		if (x < lthick || x > (side-lthick) || y < lthick || y > (side-lthick) ||
				(x > lthick+bigSqr && x < lthick*2+bigSqr) || (x > lthick*2+bigSqr*2 && x < lthick*3+bigSqr*2) ||
				(y > lthick+bigSqr && y < lthick*2+bigSqr) || (y > lthick*2+bigSqr*2 && y < lthick*3+bigSqr*2)) return null;
		r[0] = (int) (x/(lthick+bigSqr));
		r[1] = (int) (y/(lthick+bigSqr));
		x -= (lthick+bigSqr)*r[0];
		y -= (lthick+bigSqr)*r[1];
		if (x < sthick || x > (bigSqr-sthick) || y < sthick || y > (bigSqr-sthick) ||
				(x > sthick+smallSqr && x < sthick*2+smallSqr) || (x > sthick*2+smallSqr*2 && x < sthick*3+smallSqr*2) ||
				(y > sthick+smallSqr && y < sthick*2+smallSqr) || (y > sthick*2+smallSqr*2 && y < sthick*3+smallSqr*2)) return null;
		r[2] = (int) (x/(sthick+smallSqr));
		r[3] = (int) (y/(sthick+smallSqr));
		return r;
	}
	
	int[] get2Coord(int x1, int y1, int x2, int y2) {
		int[] r = new int[2];
		r[0] = r[1] = 0;
		r[0] += xbegin;
		r[1] += ybegin;
		r[0] += (lthick+bigSqr)*x1;
		r[1] += (lthick+bigSqr)*y1;
		r[0] += (sthick+smallSqr)*x2;
		r[1] += (sthick+smallSqr)*y2;
		return r;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN && listening) {
			int[] ret = get4Coord(event.getX(), event.getY());
			if (ret!= null) {
				game_fragment.humanMove(ret);
				listening = false;
			}
		}
		return true;
	}
}
