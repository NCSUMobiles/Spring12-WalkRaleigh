package com.WalkRaleigh.Functionality;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.WalkRaleigh.R;
import com.WalkRaleigh.Adapters.CustomSpinnerAdapter;
import com.WalkRaleigh.Database.WalkRaleighAdapter;

/**
 * DestinationList is a sorted list of popular destinations in the current city.
 * The destinations are color-coded and sorted by distance from current
 * location, shortest to longest. The list can be filtered to only hold specific
 * types of destinations, and locations can be favorited and filtered so that
 * only they show up. The user may also set their home coordinates so that they
 * may return to their start point at the end of their journey.
 * 
 * @author David Johnson
 * 
 */
public class DestinationList extends Activity {

	// used to hold the user's current location
	Location currentLocation;
	// assigned whenever the user sets their home location,
	// They can always return to this location after their walk.
	Location home;
	// database filled with available destinations
	SQLiteDatabase destDatabase;

	LinearLayout mainLayout;

	// the list of buttons visible on screen for the destinations
	ArrayList<Button> buttons = new ArrayList<Button>();
	// used for the text on the signs.
	Typeface myriadpro;
	// adapter used to access the database of destinations
	WalkRaleighAdapter db;
	// used to set the home location and then return to it
	Button homeButton;
	// for selecting destination types
	Spinner spinner;
	// filter is set for All by default
	private String filter = "All";

	/**
	 * Creates the layout of destinations and the buttons to sort them and set
	 * the home destination
	 */
	public void onCreate(Bundle savedInstanceState) {

		// Set font for text
		myriadpro = Typeface.createFromAsset(getAssets(),
				"MyriadPro-Semibold.otf");

		super.onCreate(savedInstanceState);

		// Layout setup
		setContentView(R.layout.main);
		mainLayout = (LinearLayout) findViewById(R.id.ButtonLayout);
		homeButton = (Button) findViewById(R.id.homebutton);
		homeButton.setText("Set Home");
		homeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (((Button) v).getText().equals("Set Home")) {
					home = currentLocation;
					((Button) v).setText("Go Home");
				} else {
					onDestClick(v, currentLocation.getLatitude(),
							currentLocation.getLongitude(), home.getLatitude(),
							home.getLongitude());
					((Button) v).setText("Set Home");
				}
			}
		});
		TextView types = (TextView) findViewById(R.id.routeTypeText);
		types.setTypeface(myriadpro);
		spinner = (Spinner) findViewById(R.id.typeSpinner);
		spinner.setOnItemSelectedListener(new spinnerListener());
		ArrayAdapter<CharSequence> adapter = new CustomSpinnerAdapter(
				DestinationList.this, android.R.layout.simple_spinner_item,
				this.getResources().getStringArray(R.array.colors_array));
		spinner.setAdapter(adapter);
		// End layout setup

		// Acquire a reference to the system Location Manager
		final LocationManager locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		// Define a listener that responds to location updates
		// Default location is set so there's no force close when a location
		// can't be found.
		LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				// Called when a new location is found by the network location
				// provider.
				currentLocation = location;
				setButtons();

			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			// If the GPS is not on, or it's turned off, an alert is created
			public void onProviderDisabled(String provider) {
				buildAlertMessageNoGps();
			}
		};

		// Creates new writeable database connection
		db = new WalkRaleighAdapter(this);
		try {
			db.createDataBase();
		} catch (IOException ioe) {
			throw new Error("Unable to create database");
		}
		try {
			destDatabase = db.openDatabase();

		} catch (SQLException sqle) {
			throw sqle;
		}

		try {
			currentLocation = new Location(
					locationManager
							.getLastKnownLocation(locationManager.GPS_PROVIDER));

		}
		// This is where it creates a default location at the Raleigh Capitol
		// Building, if one can't be found.
		catch (NullPointerException e) {
			currentLocation = new Location("");
			currentLocation.setLatitude(35.780378);
			currentLocation.setLongitude(-78.639107);
		}

		setButtons();

		// Register the listener with the Location Manager to receive location
		// updates
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				60000, 0, locationListener);

	}

	/**
	 * Queries the database and creates buttons for all of the destinations that
	 * fit the type. It orders them from closest to farthest as well. Before
	 * doing so, it removes all the previous buttons so there are no memory
	 * leaks.
	 */

	public void setButtons() {
		mainLayout.removeAllViews();
		Cursor cur;
		// Queries database to retrieve and ordered list by distance from the
		// database.
		cur = destDatabase.rawQuery("SELECT * FROM destinations ORDER BY (("
				+ currentLocation.getLatitude() + " - latitude) * ("
				+ currentLocation.getLatitude() + " - latitude) + ("
				+ currentLocation.getLongitude() + " - longitude) * ("
				+ currentLocation.getLongitude() + " - longitude))", null);
		startManagingCursor(cur);
		cur.moveToFirst();
		// Loops until all locations have been iterated through
		Location temp = new Location(currentLocation);
		while (!cur.isAfterLast()) {
			// Sets up a Location object for the closest point
			temp.setLongitude(cur.getDouble(1));
			temp.setLatitude(cur.getDouble(2));
			// Calculates the distance, and then the time in minutes from there.
			DecimalFormat df = new DecimalFormat("#.##");

			// Converts meters to miles
			double distance = (currentLocation.distanceTo(temp)) * 0.000621371192 * 1.5;

			// Time in minutes, based on average walking speed
			// of 3.4 MPH multiplied by 1.5 to account for turns
			int time = (int) (distance / 0.0567);
			Button btn = new Button(this);

			// Sets appropriate background on button for destination type.
			if (cur.getString(4).equalsIgnoreCase("commercial")) {
				// Draws a distinction between favorite Int stored in the 6th
				// row.
				if (cur.getInt(6) == 0) {
					btn.setBackgroundDrawable(getResources().getDrawable(
							R.drawable.purpledest));
				} else {
					btn.setBackgroundDrawable(getResources().getDrawable(
							R.drawable.purplefave));
				}

			} else if (cur.getString(4).equalsIgnoreCase("civic/municipal")) {
				if (cur.getInt(6) == 0) {
					btn.setBackgroundDrawable(getResources().getDrawable(
							R.drawable.bluedest));
				} else {
					btn.setBackgroundDrawable(getResources().getDrawable(
							R.drawable.bluefave));
				}

			} else if (cur.getString(4).equalsIgnoreCase("outdoor/park")) {
				if (cur.getInt(6) == 0) {
					btn.setBackgroundDrawable(getResources().getDrawable(
							R.drawable.greendest));
				} else {
					btn.setBackgroundDrawable(getResources().getDrawable(
							R.drawable.greenfave));
				}
			} else if (cur.getString(4).equalsIgnoreCase("amusement")) {
				if (cur.getInt(6) == 0) {
					btn.setBackgroundDrawable(getResources().getDrawable(
							R.drawable.orangedest));
				} else {
					btn.setBackgroundDrawable(getResources().getDrawable(
							R.drawable.orangefave));
				}

			}

			// Sets visibility for button based on filter parameter.
			if (filter.equalsIgnoreCase("All")) {
				btn.setVisibility(0);
			} else if (filter.equalsIgnoreCase("Favorites")) {
				if (cur.getString(6).equalsIgnoreCase("1")) {
					btn.setVisibility(0);
				} else {
					btn.setVisibility(8);
				}
			} else {
				if (cur.getString(4).equalsIgnoreCase(filter)) {
					btn.setVisibility(0);
				} else {
					btn.setVisibility(8);
				}
			}

			// Button Formatting
			btn.setPadding(0, 0, 0, 100);
			btn.setTypeface(myriadpro);
			btn.setTextSize(21);
			btn.setText("It's a " + time + " minute walk to\n"
					+ cur.getString(3) + "\n(~" + df.format(distance) + " mi.)");
			btn.setTag(cur.getString(3));

			// When clicked an alert comes up with a description of options to
			// walk there, not walk there, and to set the destination as a
			// favorite.
			btn.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Cursor cur = destDatabase.rawQuery(
							"SELECT * FROM destinations WHERE name='"
									+ (String) v.getTag() + "'", null);
					cur.moveToFirst();
					buildAlertMessageDescription(v,
							currentLocation.getLatitude(),
							currentLocation.getLongitude(), cur);
				}

			});
			mainLayout.getChildCount();
			mainLayout.addView(btn);
			// Goes to next closest location and repeats
			cur.moveToNext();

		}
	}

	/**
	 * This builds an alert message that tells the user that their GPS is turned
	 * off. They are given the option to go into the settings and turn it on.
	 */
	private void buildAlertMessageNoGps() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Your GPS is disabled! Would you like to enable it?")
				.setCancelable(false)
				.setPositiveButton("Enable GPS",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Intent gpsOptionsIntent = new Intent(
										android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								startActivity(gpsOptionsIntent);
							}
						});
		builder.setNegativeButton("Do nothing",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	/**
	 * Creates a window giving the description of a location and three options:
	 * to go there, to cancel, and to set it as a favorite location.
	 * 
	 * @param v
	 *            The button and corresponding location for which the dialog is
	 *            relating to
	 * @param curLat
	 *            Latitude of the location passed in so that it can be passed to
	 *            Google Navigation
	 * @param curLong
	 *            Longitude of the location passed in so that it can be passed
	 *            to Google Navigation
	 * @param cur
	 *            database result used to get information about the location
	 */
	private void buildAlertMessageDescription(final View v,
			final double curLat, final double curLong, final Cursor cur) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(cur.getString(5)).setCancelable(false)
				.setPositiveButton("Go", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						onDestClick(v, curLat, curLong, cur.getDouble(2),
								cur.getDouble(1));
					}
				});
		builder.setNeutralButton("Favorite?",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						ContentValues args = new ContentValues();
						if (cur.getInt(6) == 0) {
							args.put("Favorite", "1");
						} else {
							args.put("Favorite", "0");
						}
						destDatabase.update("destinations", args,
								"_id=" + cur.getInt(0), null);
						dialog.cancel();
						setButtons();
					}
				});
		builder.setNegativeButton("Back",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	/**
	 * onClickListener to pass the location coordinates to google maps
	 * 
	 * @param v
	 *            the buttons which is pressed
	 * @param srcLat
	 *            latitude of current location
	 * @param srcLong
	 *            longitude of current location
	 * @param destLat
	 *            latitude of destination
	 * @param destLong
	 *            longitude of destination
	 */
	public void onDestClick(View v, double srcLat, double srcLong,
			double destLat, double destLong) {

		Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
				Uri.parse("http://maps.google.com/maps?saddr=" + srcLat + ","
						+ srcLong + "&daddr=" + destLat + "," + destLong
						+ "&dirflg=w"));
		intent.setClassName("com.google.android.apps.maps",
				"com.google.android.maps.MapsActivity");
		startActivity(intent);
	}

	/**
	 * Populates the spinner with the possible values of the filter
	 * 
	 * @author David Johnson
	 * 
	 */
	public class spinnerListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			filter = (String) parent.getItemAtPosition(pos);

			setButtons();
		}

		public void onNothingSelected(AdapterView<?> parent) {
			filter = "All";
			setButtons();
		}
	}
}
