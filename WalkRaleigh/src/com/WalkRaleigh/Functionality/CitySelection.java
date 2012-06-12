package com.WalkRaleigh.Functionality;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;

import com.WalkRaleigh.R;

public class CitySelection extends Activity {

	private Typeface myriadpro;

	public void onCreate(Bundle savedInstanceState) {

		// Set font for text
		myriadpro = Typeface.createFromAsset(getAssets(),
				"MyriadPro-Semibold.otf");

		super.onCreate(savedInstanceState);

		// Layout setup
		setContentView(R.layout.locationselection);
	}
}
