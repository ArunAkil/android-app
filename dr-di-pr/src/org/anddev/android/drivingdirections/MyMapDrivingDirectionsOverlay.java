// Created by plusminus on 14:00:27 - 30.01.2008
package org.anddev.android.drivingdirections;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;

import com.google.android.maps.Overlay;
import com.google.android.maps.Point;
import com.google.googlenav.DrivingDirection;
import com.google.googlenav.map.MapPoint;

public class MyMapDrivingDirectionsOverlay extends Overlay {
	
	// ===========================================================
	// Final Fields
	// ===========================================================
	/** The tip of the pin is located at x=5, y=29 */
	private final android.graphics.Point PIN_HOTSPOT = new android.graphics.Point(5,29);
	

	// ===========================================================
	// Fields
	// ===========================================================

	MyDrivingDirectionsActivity itsMapActivity = null;
	Paint pathPaint = null;
	DrivingDirection dd = null;
	
	private Bitmap PIN_START = null;
	private Bitmap PIN_END = null;
	private Bitmap INFO_LOWER_LEFT = null;

	// ===========================================================
	// "Constructors"
	// ===========================================================
	
	public MyMapDrivingDirectionsOverlay(MyDrivingDirectionsActivity map) {
		itsMapActivity = map;
		this.pathPaint = new Paint();
		this.pathPaint.setAntiAlias(true);
		
		PIN_START = BitmapFactory.decodeResource(this.itsMapActivity.getResources(), R.drawable.mappin_blue);
		PIN_END = BitmapFactory.decodeResource(this.itsMapActivity.getResources(), R.drawable.mappin_red);
		INFO_LOWER_LEFT = BitmapFactory.decodeResource(this.itsMapActivity.getResources(), R.drawable.lower_left_info);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	/** This function does some fancy drawing of the 
	 * Driving-Directions. It could be shortened a lot. */
	public void draw(Canvas canvas, PixelCalculator pxC, boolean b) {
		super.draw(canvas, pxC, b);
		
		/* Reset our paint. */
		this.pathPaint.setStrokeWidth(4);
		this.pathPaint.setARGB(100, 113, 105, 252);
		// holders of mapped coords...
		int[] screenCoords = new int[2];

		// method in the custom map view to return the DrivingDirection object.
		dd = itsMapActivity.getDrivingDirections();
		if (dd != null) {
			/* First get Start end End Point of the route. */
			MapPoint startPoint = dd.getRouteStart();
			MapPoint endPoint = dd.getRouteEnd();

			MapPointToScreenCoords(startPoint, screenCoords, pxC);
			/* Create a path-that will be filled with map-points 
			 * and will be drawn to the canvas in the end*/
			Path thePath = new Path();
			thePath.moveTo(screenCoords[0], screenCoords[1]);
			
			/* Check to see if the route is too long. */
			if (!dd.routeTooLong()) {
				/* Retrieve all (Map)Points of the route Found. */
				MapPoint[] route = dd.getRoute();
				
				if(route == null)
					return;
				
				/* Loop through all MapPoints returned. */
				for (MapPoint current : route) {
					/* Transform current MapPoint's Lat/Lng 
					 * into corresponding point on canvas 
					 * using the pixelCalculator. */
					MapPointToScreenCoords(current, screenCoords, pxC);
					/* Add point to path. */
					thePath.lineTo(screenCoords[0], screenCoords[1]);
				}
			}
			this.pathPaint.setStyle(Paint.Style.STROKE);
			/* Draw the actual route to the canvas. */
			canvas.drawPath(thePath, this.pathPaint);
			
			/* Finally draw a fancy PIN to mark the start... */ 
			MapPointToScreenCoords(endPoint, screenCoords, pxC);
			
			canvas.drawBitmap(PIN_END, 
					screenCoords[0] - PIN_HOTSPOT.x, 
					screenCoords[1] - PIN_HOTSPOT.y, 
					pathPaint);
			
			/* ...and the end of the route.*/

			MapPointToScreenCoords(startPoint, screenCoords, pxC);
			canvas.drawBitmap(PIN_START, 
					screenCoords[0] - PIN_HOTSPOT.x, 
					screenCoords[1] - PIN_HOTSPOT.y, 
					pathPaint);
			
			/* Get the height of the underlying MapView.*/
			int mapViewHeight = this.itsMapActivity.findViewById(R.id.myMapView).getHeight();
			
			/* Now some real fancy stuff !*/
			/* Draw a info-menu for the route.*/
			canvas.drawBitmap(this.INFO_LOWER_LEFT, 0, mapViewHeight - this.INFO_LOWER_LEFT.height(), pathPaint);
			
			/* And draw i.e.the distance and time left to the info-menu.*/
			String distance = dd.getFormattedDistance();
			String time = dd.getFormattedTime().replace("Time: ", "");
			pathPaint.setARGB(255,255,255,255);
			this.pathPaint.setStrokeWidth(1);
			this.pathPaint.setStyle(Paint.Style.FILL_AND_STROKE);
			
			pathPaint.setTextSize(24);
			canvas.drawText(time, 5, mapViewHeight - 5, pathPaint);
			pathPaint.setTextSize(16);
			canvas.drawText(distance, 4, mapViewHeight - 35, pathPaint); // 2, 271
			
			/* These methods are to illustrate some 
			 * of what you can get out of the system. */
//			String startName = dd.getRouteStartLocation();
//			String info = dd.getRouteInfoDescriptor();
			Log.d("DEBUGTAG", dd.getTurns()[0]);
		}
	}
	
	private void MapPointToScreenCoords(MapPoint mp, int[] targetScreenCoords, PixelCalculator pxc){
		Point p = new Point(mp.getLatitude(), mp.getLongitude());
		pxc.getPointXY(p, targetScreenCoords);
	}
}
