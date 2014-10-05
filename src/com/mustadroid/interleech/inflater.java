package com.mustadroid.interleech;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Stack;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class inflater
{
	private static void setParameters(View view, ViewGroup parent, String attribute, String value, Context context, XmlPullParser xpp)
	{
		switch (attribute)
		{
			case "width":
				//String widthAttribute = xpp.getAttributeValue("", "width");
				String heightAttribute = xpp.getAttributeValue("", "height");
				int widthAttr = convertLayoutAttributeValue(value); 
				int heightAttr = convertLayoutAttributeValue(heightAttribute);
				if (parent != null)
					view.setLayoutParams(newParentLayoutParams(widthAttr, heightAttr, parent));
				break;
			case "height":
				//view.getLayoutParams().height = Integer.parseInt(value);
				String widthAttribute = xpp.getAttributeValue("", "width");
				int widthAttr2 = convertLayoutAttributeValue(widthAttribute); 
				int heightAttr2 = convertLayoutAttributeValue(value);
				if (parent != null)
					view.setLayoutParams(newParentLayoutParams(widthAttr2, heightAttr2, parent));
				break;
			case "alignParentRight":
				((RelativeLayout.LayoutParams) view.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				break;
			case "alignParentBottom":
				((RelativeLayout.LayoutParams) view.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				break;
			case "alignParentTop":
				((RelativeLayout.LayoutParams) view.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_TOP);
				break;
			case "alignParentLeft":
				((RelativeLayout.LayoutParams) view.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				break;
			case "textAppearance":
				int textAppearance = 0;
				textAppearance = value.equals("medium") ? android.R.style.TextAppearance_Medium : textAppearance;
				textAppearance = value.equals("large") ? android.R.style.TextAppearance_Large : textAppearance;
				((TextView) view).setTextAppearance(context, textAppearance);
				break;
			case "paddingLeft":
				view.setPadding(Integer.parseInt(value), view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
				break;
			case "paddingTop":
				view.setPadding(view.getPaddingLeft(), Integer.parseInt(value), view.getPaddingRight(), view.getPaddingBottom());
				break;
			case "paddingRight":
				view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), Integer.parseInt(value), view.getPaddingBottom());
				break;
			case "paddingBottom":
				view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), Integer.parseInt(value));
				break;
			case "scrollbar":
				if (value.equals("insideOverlay"))
					view.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
				break;
		}
	}
	
	private static View newView(XmlPullParser xpp, JSONObject json_data, Context context, ViewGroup parent)
	{
		switch (xpp.getName())
		{
			case "ScrollView":
				String orientationAttribute = xpp.getAttributeValue("", "orientation");
				if (orientationAttribute.equals("horizontal"))
					return new HorizontalScrollView(context);
				else
					return new ScrollView(context);
			case "LinearLayout":
				LinearLayout newLayout = new LinearLayout(context);
				orientationAttribute = xpp.getAttributeValue("", "orientation");
				newLayout.setOrientation(orientationAttribute.equals("vertical") ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
				
				String dataAttribute = getDataAttribute(xpp, json_data);
				if (dataAttribute != null)
				{
					String itemPictureURLs = null;
					itemPictureURLs = dataAttribute;
			    	ImageView itemPicture = null;
		            if (itemPictureURLs != null && itemPictureURLs.length() > 0)
		            {
			           	 itemPictureURLs = itemPictureURLs.replace("[", ""); 
			           	 itemPictureURLs = itemPictureURLs.replace("]", ""); 
			           	 itemPictureURLs = itemPictureURLs.replace("'", "");
			           	 String itemPictureURLArray[] = itemPictureURLs.split(",");
			           	 for (int j=0; j<itemPictureURLArray.length; j++)
			           	 {
				           	 itemPicture = new ImageView(context);
				           	 itemPicture.setPadding(1, 1, 0, 0);
				           	 newLayout.addView(itemPicture);
				             new DataManager.downloadPictureTask(itemPictureURLArray[j], itemPicture, 250, false).execute();
			           	 }
		            }	
				}
				
				return newLayout;
				
			case "RelativeLayout":
				return new RelativeLayout(context);
				
			case "ImageView":
				ImageView newImage = new ImageView(context);
				if (xpp.getAttributeValue("", "src") != null)
					newImage.setImageResource(R.drawable.ic_launcher);
				String dataAttributeImages = getDataAttribute(xpp, json_data);
				int imageWidth = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
				try {
					imageWidth = convertLayoutAttributeValue(xpp.getAttributeValue("", "width"));
				} catch (NumberFormatException e) {
					e.printStackTrace(); 
				}
				String dataImages = "";
				if (dataAttributeImages != null)
					dataImages = dataAttributeImages;
				if (dataAttributeImages.length() > 0)
				{
					dataImages = dataImages.replace("[", ""); 
					dataImages = dataImages.replace("]", ""); 
					dataImages = dataImages.replace("'", "");
				   	String itemPictureURLArray[] = dataImages.split(",");
				    new DataManager.downloadPictureTask(itemPictureURLArray[0], newImage, imageWidth, true).execute();
				}
				return newImage;
				
			case "TextView":
				TextView newView = new TextView(context);
				dataAttribute = getDataAttribute(xpp, json_data);
				newView.setText(dataAttribute);
				return newView;
			case "MapView":
				MapView newMap = new MapView(context);
				MapsInitializer.initialize(context);
				newMap.onCreate(null);
				newMap.onResume();
				GoogleMap map = newMap.getMap();
				
				String location = getDataAttribute(xpp, json_data);
		    	Geocoder coder = new Geocoder(context);
			    try {
			        ArrayList<Address> adresses = (ArrayList<Address>) coder.getFromLocationName(location, 50);
			        if (adresses.size() > 0)
			        {
				        Address add = adresses.get(0);
			        	LatLng loc = new LatLng(add.getLatitude(), add.getLongitude());

				        map.setMyLocationEnabled(true);
				        map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 13));

				        map.addMarker(new MarkerOptions()
				                .position(loc));

			        }
			    } catch (IOException e) {
			        e.printStackTrace();
			    }
			    
			    return newMap;
		}
		return null;
	}
	
	private static String getDataAttribute(XmlPullParser xpp, JSONObject json_data) {
		String dataAttribute = xpp.getAttributeValue("", "data");
		String data = dataAttribute;
		try {
			if (dataAttribute!=null && dataAttribute.contains("##"))
			{
				String[] dataParts = dataAttribute.split("##");
				data = "";
				for (int i=0; i<dataParts.length; i++)
					if (json_data.has(dataParts[i]))
						data += new String(json_data.getString(dataParts[i]).getBytes("ISO-8859-1"), "UTF8");
					else
						data += dataParts[i];
			} else if (dataAttribute != null)
			{
				data = new String(json_data.getString(dataAttribute).getBytes("ISO-8859-1"), "UTF8");
			}
		} catch (JSONException | UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		return data;
	}

	private static LayoutParams newParentLayoutParams(int width, int height, ViewGroup parent) {
		if (parent instanceof LinearLayout)
			return new LinearLayout.LayoutParams(width, height);
		if (parent instanceof RelativeLayout)
			return new RelativeLayout.LayoutParams(width, height);
		
		return new LayoutParams(width, height);
	}

	private static int convertLayoutAttributeValue(String attr) {
		if (attr.equals("wrap_content"))
			return LayoutParams.WRAP_CONTENT;
		else if (attr.equals("fill_parent") || attr.equals("match_parent"))
			return LayoutParams.MATCH_PARENT;
		else
			return Integer.parseInt(attr); 
	}

	public static View inflate(XmlPullParser xpp, ViewGroup root, JSONObject json_data, Context context) throws XmlPullParserException, IOException {
		
		View finalLayout = null;
		int eventType = xpp.getEventType();
		Stack<View> parentStack = new Stack<View>();
        while (eventType != XmlPullParser.END_DOCUMENT) {
        	
			 if(eventType == XmlPullParser.START_TAG) {
				 
				 ViewGroup curParent = null;
				 if (parentStack.isEmpty() == false && parentStack.peek() != null)
					 curParent = (ViewGroup) parentStack.peek();
				 
				 View newView = newView(xpp, json_data, context, curParent);
				 for (int i=0; i<xpp.getAttributeCount(); i++)
					 setParameters(newView, curParent, xpp.getAttributeName(i), xpp.getAttributeValue(i), context, xpp);
				 
				 // add to parent first
				 if (curParent != null)
					 curParent.addView(newView);
				 // then make parent
				 parentStack.push(newView);
				 
				 //Log.i("Inflater", "Start tag " + xpp.getName());
				 
			 } else if(eventType == XmlPullParser.END_TAG) {
				 
				 // remove from parent stack
				 finalLayout = parentStack.pop();
				 //Log.i("Inflater", "End tag " + xpp.getName());
				 
			 }
			 eventType = xpp.next();
			 
        }
        
		return finalLayout;
	}
	
}