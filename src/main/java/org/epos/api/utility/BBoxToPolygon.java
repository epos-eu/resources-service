package org.epos.api.utility;

import java.util.Map;

public class BBoxToPolygon {

	private static final String NORTH_LAT = "epos:northernmostLatitude";
	private static final String EAST_LON  = "epos:easternmostLongitude";
	private static final String SOUTH_LAT = "epos:southernmostLatitude";
	private static final String WEST_LON  = "epos:westernmostLongitude";

	public static String transform(Map<String,Object> messageObject) {
		String polyString ="POLYGON((";
		polyString+=String.valueOf(messageObject.get(EAST_LON));
		polyString+=" "+String.valueOf(messageObject.get(NORTH_LAT));
		polyString+=", "+String.valueOf(messageObject.get(EAST_LON));
		polyString+=" "+String.valueOf(messageObject.get(SOUTH_LAT));
		polyString+=", "+String.valueOf(messageObject.get(WEST_LON));
		polyString+=" "+String.valueOf(messageObject.get(SOUTH_LAT));
		polyString+=", "+String.valueOf(messageObject.get(WEST_LON));
		polyString+=" "+String.valueOf(messageObject.get(NORTH_LAT));
		polyString+=", "+String.valueOf(messageObject.get(EAST_LON));
		return polyString+=" "+String.valueOf(messageObject.get(NORTH_LAT))+"))";

	}

}
