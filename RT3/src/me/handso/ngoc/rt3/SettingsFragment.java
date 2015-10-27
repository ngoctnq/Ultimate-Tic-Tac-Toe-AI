package me.handso.ngoc.rt3;

import me.handso.ngoc.rt3.RT3Fragment.DIFFICULTY;
import me.handso.ngoc.rt3.RT3Fragment.PIECES;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.ToggleButton;

public class SettingsFragment extends DialogFragment {
	
	Button newGame;
	RT3Fragment gameFragment;
	
	boolean AI = false;
	boolean AIgoFirst = false;
	DIFFICULTY botDifficulty = DIFFICULTY.MEDIUM;
	
	ToggleButton mode2P, firstAI;
	RadioButton modeEasy, modeMedium, modeHard;
	TextView lazyDesc, botDesc;
	LinearLayout AILayout;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View view = inflater.inflate(R.layout.settings_fragment, container, true);
		
		AILayout = (LinearLayout) view.findViewById(R.id.AIOptions);
		lazyDesc = (TextView) view.findViewById(R.id.lazyDesc);
		botDesc = (TextView) view.findViewById(R.id.botDescriptionTextView);
		AILayout.setVisibility(View.GONE);
		
		mode2P = (ToggleButton) view.findViewById(R.id.settings_2PButton);
		firstAI = (ToggleButton) view.findViewById(R.id.settings_AIButton);
		modeEasy = (RadioButton) view.findViewById(R.id.settings_easyButton);
		modeMedium = (RadioButton) view.findViewById(R.id.settings_mediumButton);
		modeHard = (RadioButton) view.findViewById(R.id.settings_hardButton);
		mode2P.setChecked(false);
		firstAI.setChecked(false);
		modeEasy.setChecked(false);
		modeMedium.setChecked(true);
		modeHard.setChecked(false);
		
		mode2P.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AI = mode2P.isChecked(); 
				if (!AI) AILayout.setVisibility(View.GONE);
				else AILayout.setVisibility(View.VISIBLE);
			}
		});
		
		firstAI.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AIgoFirst = firstAI.isChecked();
			}
		});
		
		OnClickListener modeChoice = new OnClickListener() {
			@Override
			public void onClick(View v) {
				int id = ((RadioButton) v).getId();
				switch (id){
					case R.id.settings_easyButton:
						botDifficulty = DIFFICULTY.EASY;
						botDesc.setText(R.string.easy_desc);
						return;
					case R.id.settings_mediumButton:
						botDifficulty = DIFFICULTY.MEDIUM;
						botDesc.setText(R.string.medium_desc);
						return;
					case R.id.settings_hardButton:
						botDifficulty = DIFFICULTY.HARD;
						botDesc.setText(R.string.hard_desc);
						return;
				}
					
			}
		};
		
		modeEasy.setOnClickListener(modeChoice);
		modeMedium.setOnClickListener(modeChoice);
		modeHard.setOnClickListener(modeChoice);
		
		newGame = (Button) view.findViewById(R.id.settings_newGame);
		newGame.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				gameFragment = (RT3Fragment) getFragmentManager().findFragmentById(R.id.game_fragment);
				gameFragment.gameEnded = true;
				gameFragment.AIGame = AI;
				if (AI) {
					if (AIgoFirst) gameFragment.bot = PIECES.X;
					else gameFragment.bot = PIECES.O;
					gameFragment.difficulty = botDifficulty;
				}
				dismiss();
				gameFragment.newGame();
			}
		});
		return view;
	}
}
