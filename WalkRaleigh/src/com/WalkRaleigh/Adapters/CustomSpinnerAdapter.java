package com.WalkRaleigh.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustomSpinnerAdapter extends ArrayAdapter<CharSequence> {

	public CustomSpinnerAdapter(Context context, int textViewResourceId, String[] objects) {
		super(context, textViewResourceId, objects);
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getCustomView(position, convertView, parent);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return getCustomView(position, convertView, parent);
	}

	public View getCustomView(int position, View convertView, ViewGroup parent) {
		View view = super.getDropDownView(position, convertView, parent);
		((TextView) view).setGravity(Gravity.CENTER);
		String content = ((TextView) view).getText().toString();
		if (content.equalsIgnoreCase("Outdoor/Park")) {
			view.setBackgroundColor(0xFF01944E);
			view.setMinimumHeight(50);
		} else if (content.equalsIgnoreCase("Commercial")) {
			view.setBackgroundColor(0xFF6C2C8C);
			view.setMinimumHeight(50);
		} else if (content.equalsIgnoreCase("Civic/Municipal")) {
			view.setBackgroundColor(0xFF0078B7);
			view.setMinimumHeight(50);
		} else if (content.equalsIgnoreCase("Amusement")) {
			view.setBackgroundColor(0xFFFF8F35);
			view.setMinimumHeight(50);
		} else {
			view.setBackgroundColor(Color.WHITE);
			view.setMinimumHeight(50);
		}
		return view;
	}

}
