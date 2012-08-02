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
import com.WalkRaleigh.Adapters.CustomSpinnerAdapter;

/**
 * This activity allows the user to select which city they are in. In the
 * future, this may be decided by GPS location. It works by dumping the DB
 * currently on the phone and downloading the selected one from GitHub. In the
 * future, this will possibly be located on WalkRaleigh's own servers.
 * 
 * @author David Johnson
 * 
 */
public class CitySelection extends Activity {
	String DBPATH = "/data/data/com.WalkRaleigh/databases";
	String DBNAME = "/data";
	String city;
	Button btn;

	/**
	 * Sets up the layout and creates the spinner containing possible city
	 * locations.
	 */
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		// Layout setup
		setContentView(R.layout.locationselection);

		btn = (Button) findViewById(R.id.button1);
		btn.setText("Get your city destinations");
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				newDatabase(
						"https://github.com/NCSUMobiles/Spring12-WalkRaleigh/raw/master/Cities/"
								+ city, DBPATH + DBNAME);
				Intent i = new Intent(CitySelection.this, DestinationList.class);
				CitySelection.this.startActivity(i);
			}

		});

		Spinner spinner = (Spinner) findViewById(R.id.spinner1);
		// I thought about parsing through the HTML and creating this city array
		// dynamically, but never got around to doing it. Future update!!!
		ArrayAdapter<CharSequence> adapter = new CustomSpinnerAdapter(
				CitySelection.this, android.R.layout.simple_spinner_item, this
						.getResources().getStringArray(R.array.cities_array));
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new spinnerListener());

	}

	/**
	 * Downloads the new database from the GitHub repo.
	 * 
	 * @param fileURL
	 *            - City-specific URL for database file.
	 * @param fileName
	 *            - File name to save the database as.
	 */
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
			output.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This class will default the Go button as unclickable so that the user
	 * must pick a location. Once a user has picked their city, they can click
	 * on Go.
	 * 
	 * @author David Johnson
	 * 
	 */
	public class spinnerListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			if (pos == 0) {
				btn.setEnabled(false);
			} else {
				city = (String) parent.getItemAtPosition(pos);
				btn.setEnabled(true);
			}
		}

		public void onNothingSelected(AdapterView parent) {
			// Do nothing.
		}
	}
}
