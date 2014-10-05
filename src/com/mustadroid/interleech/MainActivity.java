package com.mustadroid.interleech;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mustadroid.interleech.R;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

	public static String pageName = "leechlist";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		File blocketFolder = new File("/storage/sdcard0/InterLeech");
		blocketFolder.mkdirs();
		File imagesFolder = new File("/storage/sdcard0/InterLeech/Pictures");
		imagesFolder.mkdirs();
		
		DataManager.loadData(getApplicationContext(), pageName);
		
		new getLeechListTask().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	class getLeechListTask extends AsyncTask<Void, Integer, JSONArray> {
        @Override
        protected JSONArray doInBackground(Void... arg0) {
        	String url = "http://83.252.202.28";
        	JSONArray result = json.fetchData(url, "leechlist.php");
        	if (result != null)
        	for (int i=0; i<result.length(); i++)
		    {
		    	try {
		    		JSONObject json_data = (JSONObject) result.get(i);
			    	String briefXML = json.fetchFile(url + "/data/" + json_data.getString("name") + "/mobile/brief.xml");
			    	DataManager.briefData.put(json_data.getString("name"), briefXML);
			    	String detailXML = json.fetchFile(url + "/data/" + json_data.getString("name") + "/mobile/detail.xml");
					DataManager.detailData.put(json_data.getString("name"), detailXML);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
        	return result;
        }
        
        @Override
        protected void onPostExecute(JSONArray result)
        {
        	if (result != null && result.length() > 0 && DataManager.data.get(pageName) == null) {
				try {
					DataManager.addData(result, MainActivity.pageName);
					DataManager.saveData(getApplicationContext(), MainActivity.pageName);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
        	result = DataManager.data.get(MainActivity.pageName);
			for(int i=0; i<result.length(); i++) {
				JSONObject json_data;
				LinearLayout layout_main = (LinearLayout) MainActivity.this.findViewById(R.id.layout_main);
				LinearLayout newItem = (LinearLayout) MainActivity.this.getLayoutInflater().inflate(R.layout.layout_main, null);
				String name = null;
				try {
					json_data = (JSONObject) result.get(i);
					name = json_data.getString("name");
					((TextView)newItem.findViewById(R.id.textView_pageName)).setText(name);
					((TextView)newItem.findViewById(R.id.textView_pageURL)).setText(json_data.getString("url"));
					DataManager.loadData(getApplicationContext(), name);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				layout_main.addView(newItem);
				final int j = i;
				final String pageName = name;
				 newItem.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent newIntent = new Intent(MainActivity.this, LeechActivity.class);
						newIntent.putExtra("index", j);
						newIntent.putExtra("pageName", pageName);
						MainActivity.this.startActivity(newIntent);
					}
				 });
			}
	        
	        startService(new Intent(MainActivity.this, NotificationService.class));
            //*/
        }
    }
	
}
    
    
