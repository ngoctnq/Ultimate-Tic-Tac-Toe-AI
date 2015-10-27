package me.handso.ngoc.rt3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import android.os.AsyncTask;
import android.util.Log;

class SortableChoice implements Comparable<SortableChoice>{
	int x1, x2, y1, y2;
	double chance;
	
	public SortableChoice(int m, int n, int p, int q, double c) {
		x1 = m;
		y1 = n;
		x2 = p;
		y2 = q;
		chance = c;
	}
	
	@Override
	public int compareTo(SortableChoice sc) {
		if (chance>sc.chance) return 1;
		else if (chance<sc.chance) return -1;
		else return 0;
	}
	
}

class TreeNode {

	enum STAT {WIN, LOST, TIE, IN_PROG};

	static final String TAG = "MONTE_BOT";
	static Random r = new Random();
	static double biasValue = Math.sqrt(2);
	static double epsilon = 1e-6;

	STAT stat = STAT.IN_PROG;

	int x1, y1, x2, y2, p;

	TreeNode[] children;
	double nVisits, totValue;

	TreeNode(int x1, int y1, int x2, int y2, int p){
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.p = p;
	}
	
	TreeNode[] max9Chance(Greater3x3 x) {
		List<SortableChoice> set = new ArrayList<SortableChoice>();
		for (int m = 0; m<3; m++) for (int n = 0; n<3; n++) for (int p=0; p<3; p++) for (int q=0; q<3; q++){
			Greater3x3 clone = x.clone();
			if (clone.board[m][n].board[p][q] != Generic3x3.NULL || clone.board[m][n].isFinished()) continue;
			else clone.board[m][n].board[p][q] = Generic3x3.P2;
			// here is where the experimental part is
			// formula is not proven
			// come up in a whim
			double chance = Math.exp(MediumBot.calcChance(clone))*MediumBot.calcChance(clone.board[m][n]);
			set.add(new SortableChoice(m, n, p, q, chance));
		}
		Collections.sort(set);
		int p;
		if (this.p == Generic3x3.P1) p = Generic3x3.P2;
		else p = Generic3x3.P1;
		TreeNode r[] = new TreeNode[9];
		for (int i = 0; i<9; i++){
			if (set.size()>0){
				SortableChoice sc = set.get(set.size()-1);
				set.remove(set.size()-1);
				r[i] = new TreeNode(sc.x1, sc.y1, sc.x2, sc.y2, p);
			}
			else {
				TreeNode tn = new TreeNode(-1, -1, -1, -1, p);
				tn.stat = STAT.LOST;
				r[i] = tn;
			}
		}
		return r;
	}

	void selectAction(Greater3x3 x) {
		Greater3x3 clone = x.clone();

		List<TreeNode> visited = new LinkedList<TreeNode>();
		TreeNode cur = this;

		// I'll reorganize this later - ever
		if (clone.board[cur.x1][cur.y1].board[cur.x2][cur.y2] != Generic3x3.NULL ||
				clone.board[cur.x1][cur.y1].isFinished()) {
			cur.stat = STAT.TIE;
			return;
		}
		clone.board[cur.x1][cur.y1].board[cur.x2][cur.y2] = cur.p;
		clone.board[cur.x1][cur.y1].update(cur.x2, cur.y2);
		clone.update(cur.x1, cur.y1);

		visited.add(this);
		while (!cur.isLeaf()) {
			if (cur.isDeadEnd()) return;
			cur = cur.select();

			if (clone.board[cur.x1][cur.y1].board[cur.x2][cur.y2] != Generic3x3.NULL) {
				cur.stat = STAT.TIE;
				return;
			}
			clone.board[cur.x1][cur.y1].board[cur.x2][cur.y2] = cur.p;
			clone.board[cur.x1][cur.y1].update(cur.x2, cur.y2);
			clone.update(cur.x1, cur.y1);

			visited.add(cur);
		}
		double value = 0;
		if (!clone.isFinished()) {
			if (clone.neutral) {

				// log
				Log.i(TAG, "Neutral board yielded.");

				cur.stat = STAT.TIE;
				return;
			}
			if (clone.board[cur.x2][cur.y2].isFinished()) cur.expand(true, x);
			else cur.expand(false, x);
			TreeNode newNode = cur.select();
			if (clone.board[newNode.x1][newNode.y1].board[newNode.x2][newNode.y2] != Generic3x3.NULL){
				newNode.stat = STAT.TIE;
				return;
			}
			visited.add(newNode);
		}
		else {

			// log
			Log.i(TAG, "Something desirable happened.");

			if (clone.win == Generic3x3.P2) {
				value = 1;
				cur.stat = STAT.WIN;
			}
			else if (clone.win == Generic3x3.P1) cur.stat = STAT.LOST;
			else cur.stat = STAT.TIE;
		}
		for (TreeNode node : visited) node.updateStats(value);
	}

	void expand(boolean ended, Greater3x3 x) {
		children = new TreeNode[9];
		int x1, y1, x2, y2, p;

		if (ended){			
			// TODO IMPROVE ALGORITHM
//			int temp = (int) (r.nextDouble()*9.0) % 9;
//			x1 = temp%3;
//			y1 = temp/3;
			children = max9Chance(x);
			return;
		}
		else {
			x1 = this.x2;
			y1 = this.y2;
		}

		if (this.p == Generic3x3.P1) p = Generic3x3.P2;
		else p = Generic3x3.P1;

		for (int i=0; i<9; i++) {
			x2 = i%3;
			y2 = i/3;

			children[i] = new TreeNode(x1, y1, x2, y2, p);
		}
	}

	TreeNode select() {
		TreeNode selected = null;
		double bestValue = Double.MIN_VALUE;
		for (TreeNode c : children) {
			// small random number to break ties randomly in unexpanded nodes
			double uctValue = c.totValue / (c.nVisits + epsilon) +
					biasValue * Math.sqrt(Math.log(nVisits+1) / (c.nVisits + epsilon)) +
					r.nextDouble() * epsilon;
			// finished nodes will not be chosen
			if (c.stat == STAT.WIN || c.stat == STAT.LOST || c.stat == STAT.TIE) continue;
			if (uctValue > bestValue) {
				selected = c;
				bestValue = uctValue;
			}
		}
		return selected;
	}

	boolean isDeadEnd() {
		for (int i=0; i<9; i++) if (children[i].stat == STAT.IN_PROG) return false;

		// log
		Log.i(TAG, "Something came to a deadend.");

		int winCount = 0;
		for (int i=0; i<9; i++) if (children[i].stat == STAT.WIN) winCount++;
		if (winCount>4) this.stat = STAT.WIN;
		else this.stat = STAT.LOST;
		return true;
	}

	boolean isLeaf() {
		return children == null;
	}

	void updateStats(double value) {
		nVisits++;
		totValue += value;
	}

	public String toString(){
		String s = "";
		s+=x1; s+=" ";
		s+=y1; s+=" ";
		s+=x2; s+=" ";
		s+=y2; s+=" ";
		if (p == Generic3x3.P1) s+="P1 ";
		else s+="P2 ";
		s+=totValue; s+=" ";
		s+=nVisits; s+=" ";
		if (stat == STAT.WIN) s+="WIN";
		if (stat == STAT.TIE) s+="TIE";
		if (stat == STAT.LOST) s+="LOST";
		if (stat == STAT.IN_PROG) s+="IN_PROG";
		return s;
	}
}

public class MonteCarlo implements Bot{
	Greater3x3 x;
	TreeNode root;

	MonteCarlo(Greater3x3 x){
		this.x = x;
	}

	public static void main(String[] args) {

		// log
		//		System.out.println("Begin.");

		MonteCarlo mc = new MonteCarlo(new Greater3x3());

		mc.root = new TreeNode(1, 1, 1, 1, Generic3x3.P1);
		mc.x.board[1][1].board[1][1] = Generic3x3.P1;
		mc.root.expand(false, mc.x);
		long t1 = System.currentTimeMillis();
		while ((System.currentTimeMillis() - t1) < 60000)
			for (int i=0; i<9; i++) {
				mc.root.children[i].selectAction(mc.x);
			}
		for (int i = 0; i<9; i++) System.out.printf("Node %d: %.0f/%.0f\n",
				i, mc.root.children[i].totValue, mc.root.children[i].nVisits);
		System.out.println();
		// print out tree
		TreeNode tn = mc.root;
		//		while (true && !tn.isLeaf()){
		System.out.println(tn.toString());
		//			tn = tn.children[0];
		for (TreeNode tn2: tn.children)	System.out.println("  "+tn2.toString());
		//		}
	}

	@Override
	public int[] playMove(int x1, int y1, int x2, int y2) {
		if (root == null) Log.i("ASYNC_TASK", "null root");
		else Log.i("ASYNC_TASK", "current root: "+root.x1+" "+root.y1+" "+root.x2+" "+root.y2+" "
					+ (root.p == Generic3x3.P1 ? "P1" : "P2"));
		int[] r = new int[4];
		try {
			r = new GetMove(this).execute(x1, y1, x2, y2).get();
			Log.i("ASYNC_TASK", "playMove() success");
			Log.i("ASYNC_TASK", "r[] = {"+r[0]+", "+r[1]+", "+r[2]+", "+r[3]+"}");
		} catch (Exception e) {
			Log.i("ASYNC_TASK", "error in playMove()");
			e.printStackTrace();
			r = new EasyBot(x).playMove(x1, y1, x2, y2);
		}
		// code not guaranteed to work
		return r;
	}

	public int[] playMove2(int x1, int y1, int x2, int y2) {
		if (x2 == -1 && y2 == -1 && x.checkCapacity() == 0) return new int[]{1, 1, 1, 1};

		// experimental code
		boolean updated = false;
		if (root != null && root.children != null){
			for (int i = 0; i<9; i++) {
				TreeNode tn = root.children[i];
				if (tn.x1 == x1 && tn.y1 == y1 && tn.x2 == x2 && tn.y2 == 2){
					root = tn;
					updated = true;
					break;
				}
			}
		}
		if (!updated) root = null;
		playMoveHelper(x1, y1, x2, y2);
		return getMove();
	}

	public int[] getMove(){
		double maxchance = 0;
		int[] r = new int[4];
		for (TreeNode tn:root.children){
			double chance = tn.totValue/tn.nVisits;
			if (chance > maxchance) {
				maxchance = chance;
				r[0] = tn.x1;
				r[1] = tn.y1;
				r[2] = tn.x2;
				r[3] = tn.y2;
			}
		}
		// circular logic, but im too tired to fix
		for (int i = 0; i<9; i++) {
			TreeNode tn = root.children[i];
			if (tn.x1 == r[0] && tn.y1 == r[1] && tn.x2 == r[2] && tn.y2 == r[3]){
				root = tn;
				break;
			}
		}
		return r;
	}

	public void playMoveHelper(int x1, int y1, int x2, int y2){
		if (root == null) {
			root = new TreeNode(x1, y1, x2, y2, Generic3x3.P1);
			if (x2 != -1 && y2 != -1 && x.board[x2][y2].isFinished()) root.expand(true, x);
			else root.expand(false, x);
		}
		long t1 = System.currentTimeMillis();
		while ((System.currentTimeMillis() - t1) < 25000)
			for (int i=0; i<9; i++) {
				root.children[i].selectAction(x);
			}
	}
}

class GetMove extends AsyncTask<Integer, Void, int[]>{
	MonteCarlo mc;
	static final int TIME_TO_THINK = 5000;

	static final String TAG = "ASYNC_TASK";

	GetMove(MonteCarlo bot){
		mc = bot;
	}

	@Override
	protected int[] doInBackground(Integer... params) {
		int x1 = params[0];
		int y1 = params[1];
		int x2 = params[2];
		int y2 = params[3];
		if (x2 == -1 && y2 == -1 && mc.x.checkCapacity() == 0) return new int[]{1, 1, 1, 1};

		// experimental code
		boolean updated = false;
		if (mc.root != null && mc.root.children != null){
			for (int i = 0; i<9; i++) {
				TreeNode tn = mc.root.children[i];
				if (tn.x1 == x1 && tn.y1 == y1 && tn.x2 == x2 && tn.y2 == y2){
					mc.root = tn;
					updated = true;
					break;
				}
			}
		}
		if (!updated) mc.root = null;
		if (mc.root == null) {
			mc.root = new TreeNode(x1, y1, x2, y2, Generic3x3.P1);
			if (x2 != -1 && y2 != -1 && mc.x.board[x2][y2].isFinished()) mc.root.expand(true, mc.x);
			else mc.root.expand(false, mc.x);
		}
		long t1 = System.currentTimeMillis();
		while ((System.currentTimeMillis() - t1) < TIME_TO_THINK)
			for (int i=0; i<9; i++) {
				mc.root.children[i].selectAction(mc.x);
			}			
		double maxchance = -1;
		int[] r = new int[4];
		for (TreeNode tn:mc.root.children){
			double chance = tn.totValue/tn.nVisits;
			if (chance > maxchance) {
				Log.i(TAG,"old chance: "+maxchance);
				Log.i(TAG,"new chance: "+chance);
				Log.i(TAG,"square: "+tn.x1+" "+tn.y1+" "+tn.x2+" "+tn.y2);
				maxchance = chance;
				r[0] = tn.x1;
				r[1] = tn.y1;
				r[2] = tn.x2;
				r[3] = tn.y2;
			}
		}
		if (maxchance == 0) r = new MediumBot(mc.x).playMove(x1, y1, x2, y2);
		updated = false;
		// circular logic, but im too tired to fix
		for (int i = 0; i<9; i++) {
			TreeNode tn = mc.root.children[i];
			if (tn.x1 == r[0] && tn.y1 == r[1] && tn.x2 == r[2] && tn.y2 == r[3]){
				mc.root = tn;
				updated = true;
				break;
			}
		}
		if (!updated) mc.root = null;
		if (mc.root == null) {
			mc.root = new TreeNode(r[0], r[1], r[2], r[3], Generic3x3.P2);
			if (x2 != -1 && y2 != -1 && mc.x.board[x2][y2].isFinished()) mc.root.expand(true, mc.x);
			else mc.root.expand(false, mc.x);
		}
		Log.i(TAG, "returning int[]");
		return r;
	}
}