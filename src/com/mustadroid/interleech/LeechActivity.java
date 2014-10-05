package com.mustadroid.interleech;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.mustadroid.interleech.R;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

public class LeechActivity extends Activity {

	private String pageName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_leech);

		File blocketFolder = new File("/storage/sdcard0/InterLeech");
		blocketFolder.mkdirs();
		File imagesFolder = new File("/storage/sdcard0/InterLeech/Pictures");
		imagesFolder.mkdirs();
		
		pageName = getIntent().getExtras().getString("pageName");
		this.setTitle(pageName);
		
		new getItemsTask().execute();
	}

	class getItemsTask extends AsyncTask<Void, Integer, JSONArray> {
        @Override
        protected JSONArray doInBackground(Void... arg0) {
        	int lastID = DataManager.getLastID(getApplicationContext(), pageName);
        	String url = "leechpage.php?time=" + Integer.toString(lastID) + "&name=" + pageName;
        	return json.fetchData("http://83.252.202.28", url);
        }
        
        @Override
        protected void onPostExecute(JSONArray result)
        {
			try {
				if (result != null && result.length() > 0) {
					DataManager.addData(result, pageName);
					DataManager.saveData(getApplicationContext(), pageName);
				}
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			result = DataManager.data.get(pageName);
	        for(int i=0; i<result.length(); i++) {
	             try {
					new getSingleItemTask(DataManager.data.get(pageName).getJSONObject(i), i).execute();
				} catch (JSONException e) {
					e.printStackTrace();
				}
            }
            //*/
        }
    }
	
	class getSingleItemTask extends AsyncTask<Void, Integer, LinearLayout> {
		
		private JSONObject json_data;
		private int index;
		
		public getSingleItemTask(JSONObject jsonData, int i)
		{
			json_data = jsonData;
			index = i;
		}
		
        @Override
        protected LinearLayout doInBackground(Void... arg0) {
        	LinearLayout newItemLayout = null;

			XmlPullParserFactory factory;
			try {
				factory = XmlPullParserFactory.newInstance();
				factory.setNamespaceAware(true);
				XmlPullParser xpp = factory.newPullParser();
				String xmlString = DataManager.briefData.get(LeechActivity.this.pageName);
				xpp.setInput( new InputStreamReader ( new ByteArrayInputStream( xmlString.getBytes( "UTF-8" ))) );
				newItemLayout = (LinearLayout) inflater.inflate(xpp, null, json_data, LeechActivity.this); 
			} catch (XmlPullParserException | IOException e) {
				e.printStackTrace();
			}
			 
			 final int j = index;
			 newItemLayout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent newIntent = new Intent(LeechActivity.this, ItemActivity.class);
					newIntent.putExtra("index", j);
					newIntent.putExtra("pageName", LeechActivity.this.pageName);
					LeechActivity.this.startActivity(newIntent);
				}
			 });
        	return newItemLayout;
        }
        
        @Override
        protected void onPostExecute(LinearLayout result)
        {
        	LinearLayout layout_main = (LinearLayout) findViewById(R.id.layout_leech);
        	layout_main.addView(result);
        }
    }
	
	
}
    
    
