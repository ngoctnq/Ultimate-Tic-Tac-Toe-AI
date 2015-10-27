package me.handso.ngoc.rt3;

import static me.handso.ngoc.rt3.Generic3x3.NULL;
import static me.handso.ngoc.rt3.Generic3x3.P1;
import me.handso.ngoc.rt3.RT3Fragment.PIECES;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.annotation.SuppressLint;
import android.app.Activity;

public class GameView2 extends View {

static final String TAG = "GAME_VIEW2";
	
	RT3Fragment game_fragment;
	float width, height, side, sthick, lthick, xbegin, ybegin, bigSqr, smallSqr;
	
	Paint x, o, bigWood, smallWood, neutral, white, yellow, xbg, obg;
	
	boolean listening;

	public GameView2(Context context, AttributeSet attrs) {
		super(context, attrs);
//		game_fragment = (RT3Fragment) getParent();
		game_fragment = (RT3Fragment) ((Activity) context).getFragmentManager().findFragmentById(R.id.game_fragment);
		Log.d(TAG, "in Constructor");
		
		x = new Paint();
		o = new Paint();
		bigWood = new Paint();
		smallWood = new Paint();
		neutral = new Paint();
		white = new Paint();
		yellow = new Paint();
		xbg = new Paint();
		obg = new Paint();
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
		x.setStrokeWidth(2*sthick);
		x.setColor(getResources().getColor(R.color.greenX));
		
		o = new Paint();
//		o.setStrokeWidth(lthick);
		o.setColor(getResources().getColor(R.color.redO));
		
		bigWood.setColor(getResources().getColor(R.color.lwood));
//		bigWood.setStrokeWidth(lthick);
		
		smallWood.setColor(getResources().getColor(R.color.swood));
//		smallWood.setStrokeWidth(sthick);
		
		white.setColor(Color.WHITE);
		
		// not implemented yet
		yellow.setColor(getResources().getColor(R.color.yellowBG));
		neutral.setColor(getResources().getColor(R.color.greyBG));
		xbg.setColor(getResources().getColor(R.color.greenBG));
		obg.setColor(getResources().getColor(R.color.redBG));
	}
	
	void boardDraw(Greater3x3 x, Canvas canvas){
		highlightDraw(x, canvas);
		boardDrawHelper(canvas);
		
		for (int i = 0; i<3; i++) for (int j = 0; j<3; j++)	for (int i1 = 0; i1<3; i1++) for (int j1 = 0; j1<3; j1++){
			int playa = x.board[i][j].board[i1][j1];
			if (playa!=NULL) drawMove(i, j, i1, j1, (playa==P1) ? PIECES.X : PIECES.O, canvas);
		}
	}
	
	private void highlightDraw(Greater3x3 x2, Canvas canvas) {
		float x, y;
		x = xbegin;
		y = ybegin;
		
		// fucking offset
		x += sthick/4;
		y += sthick/4;
		
		for (int i=0; i<3; i++) for (int j = 0; j<3; j++){
			if (x2.board[i][j].isFinished()){
				Paint paint;
				switch (x2.board[i][j].win){
				case Generic3x3.P1:
					paint = xbg; break;
				case Generic3x3.P2:
					paint = obg; break;
				default:
					paint = neutral; break;
				}
				canvas.drawRect(x+(lthick+bigSqr)*i+lthick-sthick/4, y+(lthick+bigSqr)*j+lthick-sthick/4,
						x+bigSqr+lthick+(lthick+bigSqr)*i-sthick/4, y+bigSqr+lthick+(lthick+bigSqr)*j-sthick/4, paint);
			}
			else if (game_fragment.xF == i && game_fragment.yF == j && !game_fragment.x.isFinished()){
//				canvas.drawRect(x+(lthick+bigSqr)*i, y+(lthick+bigSqr)*j,
//						x+bigSqr+lthick+(lthick+bigSqr)*i, y+bigSqr+lthick+(lthick+bigSqr)*j, yellow);
//				canvas.drawRect(x+sthick+(lthick+bigSqr)*i, y+sthick+(lthick+bigSqr)*j,
//						x+bigSqr+lthick-sthick+(lthick+bigSqr)*i, y+bigSqr+lthick-sthick+(lthick+bigSqr)*j, white);
				canvas.drawRect(x+(lthick+bigSqr)*i+lthick-sthick/4, y+(lthick+bigSqr)*j+lthick-sthick/4,
						x+bigSqr+lthick+(lthick+bigSqr)*i-sthick/4, y+bigSqr+lthick+(lthick+bigSqr)*j-sthick/4, yellow);
			}
		}
	}

	void boardDrawHelper(Canvas canvas){
		canvas.drawRect(xbegin+bigSqr+lthick, ybegin+lthick, xbegin+bigSqr+lthick*2, ybegin+side-lthick, bigWood);
		canvas.drawRect(xbegin+bigSqr*2+lthick*2, ybegin+lthick, xbegin+bigSqr*2+lthick*3, ybegin+side-lthick, bigWood);
		canvas.drawRect(xbegin+lthick, ybegin+bigSqr+lthick, xbegin+side-lthick, ybegin+bigSqr+lthick*2, bigWood);
		canvas.drawRect(xbegin+lthick, ybegin+bigSqr*2+lthick*2, xbegin+side-lthick, ybegin+bigSqr*2+lthick*3, bigWood);

		for (int i = 0; i<3; i++) for (int j = 0; j<3; j++) {
			canvas.drawRect(xbegin+smallSqr+lthick+(lthick+bigSqr)*i+sthick, ybegin+(lthick+bigSqr)*j+sthick+lthick,
					xbegin+smallSqr+lthick+sthick+(lthick+bigSqr)*i+sthick, ybegin+bigSqr+(lthick+bigSqr)*j+lthick-sthick, smallWood);
			canvas.drawRect(xbegin+smallSqr*2+lthick+sthick+(lthick+bigSqr)*i+sthick, ybegin+(lthick+bigSqr)*j+sthick+lthick,
					xbegin+smallSqr*2+lthick+sthick*2+(lthick+bigSqr)*i+sthick, ybegin+bigSqr+(lthick+bigSqr)*j+lthick-sthick, smallWood);
			canvas.drawRect(xbegin+(lthick+bigSqr)*i+sthick+lthick, ybegin+smallSqr+sthick+(lthick+bigSqr)*j+lthick,
					xbegin+bigSqr+(lthick+bigSqr)*i-sthick+lthick, ybegin+smallSqr+sthick+sthick+(lthick+bigSqr)*j+lthick, smallWood);
			canvas.drawRect(xbegin+(lthick+bigSqr)*i+sthick+lthick, ybegin+smallSqr*2+sthick+sthick+(lthick+bigSqr)*j+lthick,
					xbegin+bigSqr+(lthick+bigSqr)*i-sthick+lthick, ybegin+smallSqr*2+sthick+sthick*2+(lthick+bigSqr)*j+lthick, smallWood);
		}
	}
	
	void drawMove(int x1, int y1, int x2, int y2, PIECES pieces, Canvas canvas) {
		int[] c = get2Coord(x1, y1, x2, y2);
		drawMoveHelper(c[0], c[1], pieces, canvas, x1, y1);
	}
	
	void drawMoveHelper(float x1, float y1, PIECES pieces, Canvas canvas, int x2, int y2){
		if (pieces == PIECES.X){
			canvas.drawLine(x1+smallSqr/8, y1+smallSqr/8, x1+smallSqr*7/8, y1+smallSqr*7/8, x);
			canvas.drawLine(x1+smallSqr/8, y1+smallSqr*7/8, x1+smallSqr*7/8, y1+smallSqr/8, x);
//			canvas.drawRect(x1, y1, x1+smallSqr, y1+smallSqr, x);
		}
		else {
			Paint paint;
			int won = game_fragment.x.board[x2][y2].win;

			switch (won){
			case (Generic3x3.FULL):
				paint = neutral; break;
			case (Generic3x3.P1):
				paint = xbg; break;
			case (Generic3x3.P2):
				paint = obg; break;
			default:
				if (x2 == game_fragment.xF && y2 == game_fragment.yF && !game_fragment.x.isFinished()) paint = yellow;
				else paint = white;
			}
			canvas.drawCircle(x1+smallSqr/2, y1+smallSqr/2, smallSqr*3/8, o);
			canvas.drawCircle(x1+smallSqr/2, y1+smallSqr/2, smallSqr/4, white);
			canvas.drawCircle(x1+smallSqr/2, y1+smallSqr/2, smallSqr/4, paint);
		}
	}

	int[] get4Coord(float x, float y) {
		int[] r = new int[4];
		
		// some stupid offset
		x -= (sthick*5/4+lthick);
		y -= (sthick*5/4+lthick);
		
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
		
		// some stupid offset
		r[0] += (sthick*5/4+lthick);
		r[1] += (sthick*5/4+lthick);
		return r;
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN && listening) {
			int[] ret = get4Coord(event.getX(), event.getY());
			if (ret!= null) {
				game_fragment.humanMove(ret);
			}
			else Log.i(TAG, "null detected.");
		}
		return true;
	}
	
	@Override
		protected void onDraw(Canvas canvas) {
		Log.i(TAG, "onDraw() triggered");
			super.onDraw(canvas);
			boardDraw(game_fragment.x, canvas);
		}
}
