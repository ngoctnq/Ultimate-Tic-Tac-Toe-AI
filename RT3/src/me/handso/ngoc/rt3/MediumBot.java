package me.handso.ngoc.rt3;

import android.util.Log;

public class MediumBot implements Bot {
	
	public static final String TAG = "MEDIUM_BOT";
	
	Greater3x3 x;
	
	public MediumBot(Greater3x3 x) {
		this.x = x;
	}
	
	@Override
	public int[] playMove(int x1, int y1, int x2, int y2) {
		Log.i(TAG, "playing from "+x1+" "+y1+" "+x2+" "+y2);
		
		if (x2 == -1 && y2 == -1) 
//			return new EasyBot(x).playMove(x1, y1, x2, y2);
			return maxChance();
		
		Smaller3x3 sm = x.board[x2][y2];

		if (sm.isFinished() || sm.neutral) {
			Log.i(TAG, "small board finished?: "+sm.isFinished());
			Log.i(TAG, "small board neutral?: "+sm.neutral);
			int[] r = 
//					new EasyBot(x).playMove(x1, y1, x2, y2);
					maxChance();
			Log.i(TAG, "Creating new EasyBot, played move: "+r[0]+" "+r[1]+" "+r[2]+" "+r[3]);
			return r;
		}
		// the 4-coords to return
		int[] r = new int[4];
		// big coords must match last move
		r[0] = x2;
		r[1] = y2;
		// calculating win chance of each move
		int[][] chance = new int[3][3];
		for (int i = 0; i<3; i++) {
			for (int j = 0; j<3; j++){
				Smaller3x3 s = sm.clone();
				// if that square is already filled
				if (s.board[i][j] == Generic3x3.NULL) {
					// prioritize winning first
					s.board[i][j] = Generic3x3.P2;
					if (s.win == Generic3x3.P2) { chance[i][j] = Integer.MAX_VALUE; break; }

					// prioritize not losing second
					s.board[i][j] = Generic3x3.P1;
					if (s.win == Generic3x3.P1) { chance[i][j] = Integer.MAX_VALUE; break; }

					// then calculate chances
					s.board[i][j] = Generic3x3.P2;
					chance[i][j] = calcChance(s);
				}
				else chance[i][j] = -100;
			}
		}
		int xmax, ymax, chancemax;
		xmax = ymax = chancemax = -100;
		
//		Log.i(TAG, "chances:");
//		for (int i = 0; i<3; i++) for (int j = 0; j<3; j++)
//			Log.i(TAG, "sqr "+i+" "+j+": "+chance[i][j]);
		
		// find the max chance of winning that board
		for (int i = 0; i<3; i++) for (int j = 0; j<3; j++){
			if (chancemax<chance[i][j]) {
				chancemax = chance[i][j];
				xmax = i; ymax = j;
			}
		}
		r[2] = xmax;
		r[3] = ymax;
		return r;
	}

	// experimental algorithm, not proven / proven by common sense
	private int[] maxChance() {
		int[] r = new int[4];
		double maxChance = -100;
		for (int m = 0; m<3; m++) for (int n = 0; n<3; n++) for (int p=0; p<3; p++) for (int q=0; q<3; q++){
			Greater3x3 clone = x.clone();
			if (clone.board[m][n].board[p][q] != Generic3x3.NULL || clone.board[m][n].isFinished()) continue;
			else clone.board[m][n].board[p][q] = Generic3x3.P2;
			// here is where the experimental part is
			// formula is not proven
			// come up in a whim
			double chance = Math.exp(calcChance(clone))*calcChance(clone.board[m][n]);
			if (chance>maxChance) {
				r[0] = m;
				r[1] = n;
				r[2] = p;
				r[3] = q;
				maxChance = chance;
			}
		}
		return r;
	}

	static int calcChance(Smaller3x3 s) {
		int chance = 0;
		int[] c = new int[3];
		
		// check rows & columns
		for (int i = 0; i<3; i++){
			for (int j = 0; j<3; j++) c[j] = s.board[i][j];
			if (!s.checkNeutralHelper(c[1], c[2], c[0])) for (int j = 0; j<3; j++){
				if (c[j] == Generic3x3.P1) {
					chance -= 1;
				}
				if (c[j] == Generic3x3.P2) {
					chance += 1;
				}
			}
			for (int j = 0; j<3; j++) c[j] = s.board[j][i];
			if (!s.checkNeutralHelper(c[1], c[2], c[0])) for (int j = 0; j<3; j++){
				if (c[j] == Generic3x3.P1) {
					chance -= 1;
				}
				if (c[j] == Generic3x3.P2) {
					chance += 1;
				}
			}
		}
		for (int j = 0; j<3; j++) c[j] = s.board[j][j];
		if (!s.checkNeutralHelper(c[1], c[2], c[0])) for (int j = 0; j<3; j++){
			if (c[j] == Generic3x3.P1) {
				chance -= 1;
			}
			if (c[j] == Generic3x3.P2) {
				chance += 1;
			}
		}
		for (int j = 0; j<3; j++) c[j] = s.board[j][2-j];
		if (!s.checkNeutralHelper(c[1], c[2], c[0])) for (int j = 0; j<3; j++){
			if (c[j] == Generic3x3.P1) {
				chance -= 1;
			}
			if (c[j] == Generic3x3.P2) {
				chance += 1;
			}
		}
		return chance;
	}
	
	static int calcChance(Greater3x3 s) {
		int chance = 0;
		int[] c = new int[3];
		
		// check rows & columns
		for (int i = 0; i<3; i++){
			for (int j = 0; j<3; j++) c[j] = s.board[i][j].win;
			if (!s.checkNeutralHelper(c[1], c[2], c[0])) for (int j = 0; j<3; j++){
				if (c[j] == Generic3x3.P1) {
					chance -= 1;
				}
				if (c[j] == Generic3x3.P2) {
					chance += 1;
				}
			}
			for (int j = 0; j<3; j++) c[j] = s.board[j][i].win;
			if (!s.checkNeutralHelper(c[1], c[2], c[0])) for (int j = 0; j<3; j++){
				if (c[j] == Generic3x3.P1) {
					chance -= 1;
				}
				if (c[j] == Generic3x3.P2) {
					chance += 1;
				}
			}
		}
		for (int j = 0; j<3; j++) c[j] = s.board[j][j].win;
		if (!s.checkNeutralHelper(c[1], c[2], c[0])) for (int j = 0; j<3; j++){
			if (c[j] == Generic3x3.P1) {
				chance -= 1;
			}
			if (c[j] == Generic3x3.P2) {
				chance += 1;
			}
		}
		for (int j = 0; j<3; j++) c[j] = s.board[j][2-j].win;
		if (!s.checkNeutralHelper(c[1], c[2], c[0])) for (int j = 0; j<3; j++){
			if (c[j] == Generic3x3.P1) {
				chance -= 1;
			}
			if (c[j] == Generic3x3.P2) {
				chance += 1;
			}
		}
		return chance;
	}
}
