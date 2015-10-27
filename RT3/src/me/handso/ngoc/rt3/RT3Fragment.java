package me.handso.ngoc.rt3;

import java.util.Map;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class RT3Fragment extends Fragment {

	private Activity activity;
//	private GameView gameView;
	private GameView2 gameView;
	private TextView status;

	private final String TAG = "RT3_FRAGMENT";
	private final String SAVEGAME = "SAVE";
	private SharedPreferences saveGame;
	private SharedPreferences.Editor prefsEditor;

	boolean gameEnded = false;

	static enum PIECES {
		X, O
	};

	static enum DIFFICULTY {
		EASY, MEDIUM, HARD
	};

	PIECES turn;
	PIECES bot;
	DIFFICULTY difficulty;
	Bot AI;
	boolean AIGame = false;
	int xF, yF;

	Greater3x3 x;

	void prefsCorrupted() {
		Log.e(TAG, "prefs corrupted.");
		prefsEditor.clear();
		if (prefsEditor.commit())
			Toast.makeText(activity, R.string.corrupted, Toast.LENGTH_SHORT)
					.show();
		else
			Toast.makeText(activity, R.string.corrupted2, Toast.LENGTH_SHORT)
					.show();
		Log.e(TAG, "starting new game because of corrupted prefs.");
		newGame();
	}

	void parseSave() {
		// detect first run
		gameEnded = saveGame.getInt("ENDED", 1) == 1;
		@SuppressWarnings("unchecked")
		Map<String, Integer> savedBoard = (Map<String, Integer>) saveGame.getAll();
		for (String key : savedBoard.keySet()) {
			// should never happens, but just for fun :P
			if (key.length() != 5)
				prefsCorrupted();
			if (key == "IF_AI")
				AIGame = savedBoard.get(key) == 1;
			else if (key == "AIDIF") {
				int d = savedBoard.get(key);
				if (AIGame)
					switch (d) {
					case 1:
						AI = new EasyBot(x);
						break;
					case 2:
						AI = new MediumBot(x);
						break;
					case 3:
						AI = new MonteCarlo(x);
						break;
					}
			} else if (key == "NEUTR")
				x.neutral = savedBoard.get(key) == 1;
			else if (key == "WIN?_")
				x.win = savedBoard.get(key);
			else if (key == "XF_CD") xF = savedBoard.get(key);
			else if (key == "YF_CD") yF = savedBoard.get(key);
			else if (key == "TURN_") {
				int val = savedBoard.get(key);
				if (val == Generic3x3.P1) turn = PIECES.X;
				if (val == Generic3x3.P2) turn = PIECES.O;
			}
			else {
				int c1 = key.charAt(0);
				int c2 = key.charAt(1);
				int c3 = key.charAt(2);
				int c4 = key.charAt(3);
				char c5 = key.charAt(4);

				if (c1 == '0')
					c1 = 0;
				else if (c1 == '1')
					c1 = 1;
				else if (c1 == '2')
					c1 = 2;

				if (c2 == '0')
					c2 = 0;
				else if (c2 == '1')
					c2 = 1;
				else if (c2 == '2')
					c2 = 2;

				if (c3 == '0')
					c3 = 0;
				else if (c3 == '1')
					c3 = 1;
				else if (c3 == '2')
					c3 = 2;

				if (c4 == '0')
					c4 = 0;
				else if (c4 == '1')
					c4 = 1;
				else if (c4 == '2')
					c4 = 2;

				// win field
				if (c5 == 'w')
					x.board[c1][c2].win = savedBoard.get(key);
				// neutral field
				else if (c5 == 'n')
					x.board[c1][c2].neutral = savedBoard.get(key) == 1;
				// data field
				else if (c5 == 'd')
					x.board[c1][c2].board[c3][c4] = savedBoard.get(key);
				// else, you're fucked.
				else
					prefsCorrupted();
			}
		}
	}

	void saveGame() {
		prefsEditor.putInt("IF_AI", (AIGame) ? 1 : 0).putInt("XF_CD", xF).putInt("YF_CD",yF)
				.putInt("NEUTR", (x.neutral) ? 1 : 0).putInt("WIN?_", x.win)
				.putInt("ENDED", (gameEnded) ? 1 : 0);
		if (turn == PIECES.X) prefsEditor.putInt("TURN_", Generic3x3.P1);
		if (turn == PIECES.O) prefsEditor.putInt("TURN_", Generic3x3.P2);
		for (int c1 = 0; c1 < 3; c1++)
			for (int c2 = 0; c2 < 3; c2++) {
				prefsEditor.putInt("" + c1 + "" + c2 + "__w",
						x.board[c1][c2].win).putInt("" + c1 + "" + c2 + "__n",
						(x.board[c1][c2].neutral) ? 1 : 0);
				for (int c3 = 0; c3 < 3; c3++)
					for (int c4 = 0; c4 < 3; c4++)
						prefsEditor.putInt("" + c1 + "" + c2 + "" + c3 + ""
								+ c4 + "d", x.board[c1][c2].board[c3][c4]);
			}
		prefsEditor.apply();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = getActivity();
		saveGame = activity.getSharedPreferences(SAVEGAME,
				Activity.MODE_PRIVATE);
		prefsEditor = saveGame.edit();
		setHasOptionsMenu(true);

		x = new Greater3x3();
		turn = PIECES.X;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.game_fragment, container, false);
		gameView = (GameView2) view.findViewById(R.id.gameView);
		status = (TextView) view.findViewById(R.id.status);
		return view;
	}

	@Override
	public void onResume() {
		parseSave();
		if (x.win != Generic3x3.NULL || gameEnded) {
			Toast.makeText(activity, R.string.gameEnded, Toast.LENGTH_SHORT)
					.show();
			newGame();
		}
//		else gameView.initDraw(x);
		else gameView.invalidate();
		if (AIGame && turn == bot) {
			AI.playMove(-1, -1, xF, yF);
		}
		super.onResume();
	}

	@Override
	public void onPause() {
		saveGame();
		super.onPause();
	}

	public void switchTurn() {
		if (turn == PIECES.X)
			turn = PIECES.O;
		else
			turn = PIECES.X;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.game_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_newGame:
			newGame();
			return true;
		case R.id.menu_settings:
			endGame();
			new SettingsFragment().show(getFragmentManager(),
					getString(R.string.settings));
		}
		return super.onOptionsItemSelected(item);
	}

	void newGame() {
		// TODO add moar
		x = new Greater3x3();
		prefsEditor.clear().apply();
		gameEnded = false;
		status.setText("");
		turn = PIECES.X;
		xF = yF = -1;
//		gameView.initDraw(x);
		gameView.invalidate();
		if (AIGame) switch (difficulty){
				case EASY:
					AI = new EasyBot(x); break;
				case MEDIUM:
					AI = new MediumBot(x); break;
				default:
					AI = new MonteCarlo(x); break;
		}
		if (AIGame && bot == PIECES.X)
			AImove(AI.playMove(-1, -1, -1, -1));
		gameView.listening = true;
	}

	private void AImove(int[] fourCoords) {
//		gameView.drawMove(fourCoords[0], fourCoords[1], fourCoords[2], fourCoords[3], bot);
		x.board[fourCoords[0]][fourCoords[1]].board[fourCoords[2]][fourCoords[3]] = Generic3x3.P2;
		x.board[fourCoords[0]][fourCoords[1]].update(fourCoords[2], fourCoords[3]);
		x.update(fourCoords[0], fourCoords[1]);
		gameView.invalidate();
		
		xF = fourCoords[2]; yF = fourCoords[3];
		if (x.board[xF][yF].isFinished()) { xF = yF = -1; }
		
		Log.i(TAG, "AI moved "+fourCoords[0]+" "+fourCoords[1]+" "+fourCoords[2]+" "+fourCoords[3]);
		
		if (x.isFinished()) {
			gameView.listening = false;
			switch (x.win) {
			case (Generic3x3.P1):
				status.setText(getString(R.string.human) + " "
						+ getString(R.string.win));
				break;
			case (Generic3x3.P2):
				status.setText(getString(R.string.AI) + " "
						+ getString(R.string.win));
				break;
			default:
				status.setText(R.string.tie);
				break;
			}
		} else {
			switchTurn();
			status.setText(getString(R.string.human)+" "+getString(R.string.turn));
			gameView.listening = true;
		}
	}

	public void humanMove(int[] fourCoords) {
		Log.i(TAG,"player move "+fourCoords[0]+" "+fourCoords[1]+" "+fourCoords[2]+" "+fourCoords[3]);
		Log.i(TAG,"preexisted value "+x.board[fourCoords[0]][fourCoords[1]].board[fourCoords[2]][fourCoords[3]]);
		if ((!x.board[fourCoords[0]][fourCoords[1]].isFinished()) &&
				x.board[fourCoords[0]][fourCoords[1]].board[fourCoords[2]][fourCoords[3]] == Generic3x3.NULL && 
				((fourCoords[0] == xF && fourCoords[1] == yF) || (xF == -1 && yF == -1))) {
						
			int playa;
			if (AIGame)
				playa = Generic3x3.P1;
			else
				playa = (turn == PIECES.X) ? Generic3x3.P1 : Generic3x3.P2;
//			gameView.drawMove(fourCoords[0], fourCoords[1], fourCoords[2], fourCoords[3], turn);
			x.board[fourCoords[0]][fourCoords[1]].board[fourCoords[2]][fourCoords[3]] = playa;
			x.board[fourCoords[0]][fourCoords[1]].update(fourCoords[2], fourCoords[3]);
			x.update(fourCoords[0], fourCoords[1]);
			
			gameView.invalidate();
			
			xF = fourCoords[2]; yF = fourCoords[3];
			if (x.board[xF][yF].isFinished()) { xF = yF = -1; }

			if (x.isFinished()) {
				gameView.listening = false;
				switch (x.win) {
				case (Generic3x3.P1):
					if (AIGame)
						status.setText(getString(R.string.human) + " "
								+ getString(R.string.win));
					else
						status.setText(getString(R.string.P1) + " "
								+ getString(R.string.win));
					break;
				case (Generic3x3.P2):
					if (AIGame)
						status.setText(getString(R.string.AI) + " "
								+ getString(R.string.win));
					else
						status.setText(getString(R.string.P2) + " "
								+ getString(R.string.win));
					break;
				default:
					status.setText(R.string.tie);
					break;
				}
			} else {
				switchTurn();
				if(AIGame) {
					gameView.listening = false;
					status.setText(R.string.procrastinating);
					Log.i(TAG, "just before AI plays");
					AImove(AI.playMove(fourCoords[0],fourCoords[1],fourCoords[2],fourCoords[3]));
				}
				else {
					status.setText((turn == PIECES.X ? getString(R.string.P1) : getString(R.string.P2))+" "+getString(R.string.turn));
					gameView.listening = true;
				}
			}
		}
	}

	private void endGame() {
		gameView.listening = false;
	}
}
