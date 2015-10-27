package me.handso.ngoc.rt3;

public abstract class Generic3x3<E> {

	public static final int P1 = 0;
	public static final int P2 = 1;
	public static final int NULL = -1;
	public static final int FULL = 2;

	public E[][] board;
	public int win;
	public boolean neutral = false;

	/** Check if the game has finished. */
	public boolean isFinished(){
		return (win != NULL);
	}

	public abstract void checkRow(int x);
	public abstract void checkColumn(int y);
	public abstract void checkDiagonal1();
	public abstract void checkDiagonal2();
	public abstract void checkNeutral();
	public abstract void checkFull();
	
	/**
	 * Checks if the game is won when a change is committed.
	 * @param x The x-coord of the box changed
	 * @param y The y-coord of the box changed
	 */
	public void update(int x, int y){
		if (isFinished()) return;
		checkNeutral();
		if (!neutral){
			checkRow(x);
			checkColumn(y);
			if (x == y) checkDiagonal1();
			if (x+y == 2) checkDiagonal2();
		}
		if (!isFinished()) checkFull();
	}
	
	boolean checkNeutralHelper(int c1, int c2, int c3) {
		int temp;
		if (c2 == 1){
			temp = c1;
			c1 = c2;
			c2 = temp;
		}
		if (c3 == 1){
			temp = c1;
			c1 = c3;
			c3 = temp;
		}
		if (c1!=1) return false;
		return (c2 == 0 || c3 == 0);
	}
}
