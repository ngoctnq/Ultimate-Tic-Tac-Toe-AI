package me.handso.ngoc.rt3;

public class Smaller3x3 extends Generic3x3<Integer> {

	public Smaller3x3() {
		board = new Integer[3][3];
		for (int i=0; i<3; i++) for (int j=0; j<3; j++) board[i][j] = NULL;
		win = NULL;
	}

	public void checkRow(int x) {
		if (board[x][0] == board[x][1] && board[x][1] == board[x][2]){
			if (board[x][0] == P1) win = P1;
			else if (board[x][0] == P2) win = P2;
		}
	}

	public void checkColumn(int y) {
		if (board[0][y] == board[1][y] && board[1][y] == board[2][y]){
			if (board[0][y] == P1) win = P1;
			else if (board[0][y] == P2) win = P2;
		}
	}

	public void checkDiagonal1(){
		if (board[0][0] == board[1][1] && board[1][1] == board[2][2]){
			if (board[0][0] == P1) win = P1;
			else if (board[0][0] == P2) win = P2;
		}
	}

	public void checkDiagonal2(){
		if (board[0][2] == board[1][1] && board[1][1] == board[2][0]){
			if (board[0][2] == P1) win = P1;
			else if (board[0][2] == P2) win = P2;
		}
	}

	public Smaller3x3 clone(){
		Smaller3x3 c = new Smaller3x3();
		for (int i=0; i<3; i++) for (int j=0; j<3; j++) c.board[i][j] = this.board[i][j];
		c.win = this.win;
		return c;
	}

	@Override
	public void checkNeutral() {
		if (!neutral){
			for (int i = 0; i<3; i++) {
				if (!checkNeutralHelper(board[i][0], board[i][1], board[i][2])) return;
				if (!checkNeutralHelper(board[0][i], board[1][i], board[2][i])) return;
			}
			if (!checkNeutralHelper(board[0][0], board[1][1], board[2][2])) return;
			if (!checkNeutralHelper(board[2][0], board[1][1], board[0][2])) return;
			neutral = true;
		}
	}

	@Override
	public void checkFull() {
		for (int i=0; i<3; i++) for (int j=0; j<3; j++) if (board[i][j] == NULL) return;
		win = FULL;
	}
}
