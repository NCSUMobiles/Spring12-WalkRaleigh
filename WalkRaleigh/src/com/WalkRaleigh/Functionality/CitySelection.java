package com.WalkRaleigh.Functionality;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.util.ByteArrayBuffer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.WalkRaleigh.R;

public class CitySelection extends Activity {
	private Typeface myriadpro;
	String DBPATH = "/data/data/com.WalkRaleigh/databases";
	String DBNAME = "/data";
	String city;
	Button btn;

	public void onCreate(Bundle savedInstanceState) {

		// Set font for text
		myriadpro = Typeface.createFromAsset(getAssets(),
				"MyriadPro-Semibold.otf");

		super.onCreate(savedInstanceState);

		// Layout setup
		setContentView(R.layout.locationselection);

		btn = (Button) findViewById(R.id.button1);
		btn.setText("Get your city destinations");
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				newDatabase(
						"https://github.com/NCSUMobiles/Spring12-WalkRaleigh/raw/master/Cities/" + city, DBPATH + DBNAME);
				Intent i = new Intent(CitySelection.this, DestinationList.class);
				CitySelection.this.startActivity(i);
			}

		});

//		Button def = (Button) findViewById(R.id.button2);
//		def.setText("Default Database");
//		def.setOnClickListener(new OnClickListener() {
//
//			public void onClick(View view) {
//				File del = new File(DBPATH + DBNAME);
//				del.delete();
//				Intent i = new Intent(CitySelection.this, DestinationList.class);
//				CitySelection.this.startActivity(i);
//			}
//
//		});
		Spinner spinner = (Spinner) findViewById(R.id.spinner1);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.cities_array,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new spinnerListener());

	}

	public void newDatabase(String fileURL, String fileName) {
		try {
			URL url = new URL(fileURL);
			File file = new File(fileName);
			URLConnection connection = url.openConnection();
			InputStream is = connection.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			ByteArrayBuffer buf = new ByteArrayBuffer(50);
			int current = 0;
			while ((current = bis.read()) != -1) {
				buf.append((byte) current);
			}

			/* Convert the Bytes read to a String. */
			FileOutputStream output = new FileOutputStream(file);
			output.write(buf.toByteArray());
			output.flush();
			Log.i("herp", file.length()+"");
			output.close();

		} catch (IOException e) {

		}
	}

	public class spinnerListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			if (pos == 0) {
				btn.setEnabled(false);
			} else {
				city = (String) parent.getItemAtPosition(pos);
				Log.i("herp", city);
				btn.setEnabled(true);
			}
		}

		public void onNothingSelected(AdapterView parent) {
			// Do nothing.
		}
	}
}
