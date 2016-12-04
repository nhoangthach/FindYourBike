package com.android.app.fybike.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PlaceJSONParser {
	
	/** Receives a JSONObject and returns a list */
	public List<HashMap<String,String>> parse(JSONObject jObject){		
		
		JSONArray jPlaces = null;
		try {			
			/** Retrieves all the elements in the 'places' array */
			jPlaces = jObject.getJSONArray("results");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		/** Invoking getPlaces with the array of json object
		 * where each json object represent a place
		 */
		return getPlaces(jPlaces);
	}
	
	private List<HashMap<String, String>> getPlaces(JSONArray jPlaces){
		int placesCount = jPlaces.length();
		List<HashMap<String, String>> placesList = new ArrayList<HashMap<String,String>>();
		HashMap<String, String> place = null;	

		/** Taking each place, parses and adds to list object */
		for(int i=0; i<placesCount;i++){
			try {
				/** Call getPlace with place JSON object to parse the place */
				place = getPlace((JSONObject)jPlaces.get(i));
				placesList.add(place);

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		return placesList;
	}
	
	/** Parsing the Place JSON object */
	private HashMap<String, String> getPlace(JSONObject jPlace){

		HashMap<String, String> place = new HashMap<String, String>();
		String placeName = "-NA-";
		String vicinity="-NA-";
		String latitude="";
		String longitude="";
				
		
		try {
			// Extracting Place name, if available
			if(!jPlace.isNull("name")){
				placeName = jPlace.getString("name");
			}

			// Extracting Place Vicinity, if available
			if(!jPlace.isNull("vicinity")){
				vicinity = jPlace.getString("vicinity");
			}	
			
			latitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lat");
			longitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lng");			
			
			
			place.put("place_name", placeName);
			place.put("vicinity", vicinity);
			place.put("lat", latitude);
			place.put("lng", longitude);
			
			
		} catch (JSONException e) {			
			e.printStackTrace();
		}		
		return place;
	}

	public List<HashMap<String,String>> parseAutoComplete(JSONObject jObject){

		JSONArray jPlaces = null;
		try {
			/** Retrieves all the elements in the 'places' array */
			jPlaces = jObject.getJSONArray("predictions");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		/** Invoking getPlaces with the array of json object
		 * where each json object represent a place
		 */
		return getPlacesAutoComplete(jPlaces);
	}

	private List<HashMap<String, String>> getPlacesAutoComplete(JSONArray jPlaces){
		int placesCount = jPlaces.length();
		List<HashMap<String, String>> placesList = new ArrayList<HashMap<String,String>>();
		HashMap<String, String> place = null;

		/** Taking each place, parses and adds to list object */
		for(int i=0; i<placesCount;i++){
			try {
				/** Call getPlace with place JSON object to parse the place */
				place = getPlaceAutoComplete((JSONObject)jPlaces.get(i));
				placesList.add(place);

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return placesList;
	}

	/** Parsing the Place JSON object */
	private HashMap<String, String> getPlaceAutoComplete(JSONObject jPlace){

		HashMap<String, String> place = new HashMap<String, String>();

		String id="";
		String reference="";
		String description="";

		try {

			description = jPlace.getString("description");
			id = jPlace.getString("id");
			reference = jPlace.getString("reference");

			place.put("description", description);
			place.put("_id",id);
			place.put("reference",reference);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return place;
	}

	/** Receives a JSONObject and returns a list */
	public List<HashMap<String,String>> parseLocalLocation(JSONArray jsonArray){

		JSONArray jPlaces = null;

        jPlaces = jsonArray;
//		try {
//			/** Retrieves all the elements in the 'places' array */
//			jPlaces = jObject.getJSONArray("");
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
		/** Invoking getPlaces with the array of json object
		 * where each json object represent a place
		 */
		return getPlacesLocalLocation(jPlaces);
	}

	private List<HashMap<String, String>> getPlacesLocalLocation(JSONArray jPlaces){
		int placesCount = jPlaces.length();
		List<HashMap<String, String>> placesList = new ArrayList<HashMap<String,String>>();
		HashMap<String, String> place = null;

		/** Taking each place, parses and adds to list object */
		for(int i=0; i<placesCount;i++){
			try {
				/** Call getPlace with place JSON object to parse the place */
				place = getPlaceLocalLocation((JSONObject)jPlaces.get(i));
				placesList.add(place);

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return placesList;
	}

	/** Parsing the Place JSON object */
	private HashMap<String, String> getPlaceLocalLocation(JSONObject jPlace){

		HashMap<String, String> place = new HashMap<String, String>();

        String id ;
        String username = "-NA-";
        String address = "-NA-";
        String types = "-NA-";
        int rating = 0;
        String title = "-NA-";
        String latitude="";
        String longitude="";
        String htmlverified = "-NA-";
        String icon = "-NA-";
        String opening_hours = "-NA-";
        String photos = "-NA-";





		try {
			// Extracting Place name, if available

            id = jPlace.getString("_id");
			/*if(!jPlace.isNull("title")){
                title = jPlace.getString("title");
			}

			// Extracting Place Vicinity, if available
			if(!jPlace.isNull("icon")){
                icon = jPlace.getString("icon");
			}*/

			latitude = jPlace.getJSONArray("location").getString(1);
			longitude = jPlace.getJSONArray("location").getString(0);


			/*place.put("title", title);
			place.put("icon", icon);*/
			place.put("lat", latitude);
			place.put("lng", longitude);


		} catch (JSONException e) {
			e.printStackTrace();
		}
		return place;
	}
}
