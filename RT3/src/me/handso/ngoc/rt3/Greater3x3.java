package me.handso.ngoc.rt3;

public class Greater3x3 extends Generic3x3<Smaller3x3> {

	public Greater3x3() {
		board = new Smaller3x3[3][3];
		for (int i=0; i<3; i++) for (int j=0; j<3; j++) board[i][j] = new Smaller3x3();
		win = NULL;
	}

	public void checkRow(int x) {
		if (win != NULL) return;
		if (board[x][0].win == board[x][1].win && board[x][1].win == board[x][2].win){
			if (board[x][0].win == P1) win = P1;
			else if (board[x][0].win == P2) win = P2;
		}
	}

	public void checkColumn(int y) {
		if (win != NULL) return;
		if (board[0][y].win == board[1][y].win && board[1][y].win == board[2][y].win){
			if (board[0][y].win == P1) win = P1;
			else if (board[0][y].win == P2) win = P2;
		}
	}

	public void checkDiagonal1(){
		if (win != NULL) return;
		if (board[0][0].win == board[1][1].win && board[1][1].win == board[2][2].win){
			if (board[0][0].win == P1) win = P1;
			else if (board[0][0].win == P2) win = P2;
		}
	}

	public void checkDiagonal2(){
		if (win != NULL) return;
		if (board[0][2].win == board[1][1].win && board[1][1].win == board[2][0].win){
			if (board[0][2].win == P1) win = P1;
			else if (board[0][2].win == P2) win = P2;
		}
	}

	public Greater3x3 clone(){
		Greater3x3 c = new Greater3x3();
		for (int i=0; i<3; i++) for (int j=0; j<3; j++) c.board[i][j] = this.board[i][j].clone();
		c.win = this.win;
		return c;
	}
	
	@Override
	boolean checkNeutralHelper(int c1, int c2, int c3) {
		if (c1==FULL) return true;
		if (c2==FULL) return true;
		if (c3==FULL) return true;
		return super.checkNeutralHelper(c1, c2, c3);
	}

	@Override
	public void checkNeutral() {
		if (!neutral){
			for (int i = 0; i<3; i++) {
				if (!checkNeutralHelper(board[i][0].win, board[i][1].win, board[i][2].win)) return;
				if (!(board[i][0].neutral || board[i][1].neutral || board[i][2].neutral)) return;
				
				if (!checkNeutralHelper(board[0][i].win, board[1][i].win, board[2][i].win)) return;
				if (!(board[0][i].neutral || board[1][i].neutral || board[2][i].neutral)) return;
			}
			if (!checkNeutralHelper(board[0][0].win, board[1][1].win, board[2][2].win)) return;
			if (!(board[0][0].neutral || board[1][1].neutral || board[2][2].neutral)) return;

			if (!checkNeutralHelper(board[2][0].win, board[1][1].win, board[0][2].win)) return;
			if (!(board[2][0].neutral || board[1][1].neutral || board[2][2].neutral)) return;

			neutral = true;
		}
	}

	@Override
	public void checkFull() {
		for (int i=0; i<3; i++) for (int j=0; j<3; j++) if (board[i][j].win == NULL) return;
		win = FULL;
	}
	
	@Override
	public void update(int x, int y) {
		super.update(x, y);
		if (neutral) {
			for (int i=0; i<3; i++) for (int j=0; j<3; j++)
				if (board[i][j].neutral) board[i][j].win = FULL;
			win = FULL;
		}
	}
	
	int checkCapacity() {
		int x = 0;
		for (Smaller3x3[] sm:board) for (Smaller3x3 sm2:sm) for (Integer[] b:sm2.board) for (Integer i:b) if (i!=NULL) x++;
		return x;
	}
}
