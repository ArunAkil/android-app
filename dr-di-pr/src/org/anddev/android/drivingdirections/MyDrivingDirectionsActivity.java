package org.anddev.android.drivingdirections;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayController;
import com.google.googlenav.DrivingDirection;
import com.google.googlenav.map.MapPoint;

public class MyDrivingDirectionsActivity extends MapActivity {

	private DrivingDirection myDD = null;
	private boolean foundDirections = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.main);
		
		/* Find the MapView we defined in the main.xml. */
		MapView mapViewFromXML = (MapView)this.findViewById(R.id.myMapView);
		/* Retrieve its OverlayController. */
		OverlayController myOC = mapViewFromXML.createOverlayController();
		/* Add a new instance of our fancy Overlay-Class to the MapView. */
		myOC.add(new MyMapDrivingDirectionsOverlay(this), true);
		
		/* Submit one dummy search */
		findViewById(R.id.cmd_submit).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				GeoPoint mpFrom = new GeoPoint(37423157, -122085008); // 37.423157,-122.085008
				GeoPoint mpTo = new GeoPoint(37495999, -122457508); // 37.495999,-122.457508
				MyDrivingDirectionsActivity.this.startFetchDirections(mpFrom,"", mpTo, "");
			}
		});
	}

	/** Offers the DrivingDirections to the Overlay. */
	public DrivingDirection getDrivingDirections() {
		return this.myDD;
	}

	private void startFetchDirections(GeoPoint from_pos, String from_name,
			GeoPoint to_pos, String to_name) {
		/* mDD is a class variable for the activity that will
		 * hold an instance of the DrivingDirection object created here. */
		this.myDD = new DrivingDirection(from_pos, from_name, to_pos, to_name);
		if (this.myDD != null) {
			/* Add the request the dispatcher */
			this.getDispatcher().addDataRequest(this.myDD);

			Thread t = new Thread(new Runnable() {
				public void run() {
					/* Wait for the search to be complete... */
					while (!MyDrivingDirectionsActivity.this.myDD.isComplete()) { }
					/* Check to see if any Placemarks were found.. 
					 * if 0 then there is no route! */
					if (MyDrivingDirectionsActivity.this.myDD.numPlacemarks() > 0) {
						/* Set a flag to let the program know 
						 * the directions are done... */
						MyDrivingDirectionsActivity.this.foundDirections = true;
					} else{ /* no route.. */
						MyDrivingDirectionsActivity.this.foundDirections = true;
						MyDrivingDirectionsActivity.this.myDD = null;
						/* Let the user know that no route was found... */
					}
				}
			});
			t.start();
		}
	}
}