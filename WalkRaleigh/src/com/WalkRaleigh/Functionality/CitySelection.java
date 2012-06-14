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
import android.widget.Button;

import com.WalkRaleigh.R;

public class CitySelection extends Activity {

	private Typeface myriadpro;
	String DBPATH = "/data/data/com.WalkRaleigh/databases";


	public void onCreate(Bundle savedInstanceState) {

		// Set font for text
		myriadpro = Typeface.createFromAsset(getAssets(),
				"MyriadPro-Semibold.otf");

		super.onCreate(savedInstanceState);

		// Layout setup
		setContentView(R.layout.locationselection);
		
		Button btn = (Button) findViewById(R.id.button1);
		btn.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {
				newDatabase("https://github.com/NCSUMobiles/Spring12-WalkRaleigh/raw/master/tampa", DBPATH + "/data");
				Intent i = new Intent(CitySelection.this, DestinationList.class);
				CitySelection.this.startActivity(i);
			}
			
		});
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
			output.close();
			
		} catch (IOException e) {

		}
	}
}
