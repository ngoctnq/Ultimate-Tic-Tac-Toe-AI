package me.handso.ngoc.rt3;

public class EasyBot implements Bot {
	Greater3x3 x;

	public EasyBot(Greater3x3 x) {
		this.x = x;
	}

	@Override
	public int[] playMove(int x1, int y1, int x2, int y2) {
		int x11, y11, x21, y21;
		x11 = x2; y11 = y2;
		do {
			if ((x2 == -1 && y2 == -1) || x.board[x11][y11].isFinished()) {
				x11 = (int) (Math.random()*3);
				y11 = (int) (Math.random()*3);
			}
			x21 = (int) (Math.random()*3);
			y21 = (int) (Math.random()*3);
		} while (x.board[x11][y11].board[x21][y21] != Generic3x3.NULL || x.board[x11][y11].isFinished());
		return new int[]{x11, y11, x21, y21};
	}
}
