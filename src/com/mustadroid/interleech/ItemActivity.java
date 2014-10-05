package com.mustadroid.interleech;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.mustadroid.interleech.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

public class ItemActivity extends Activity {

	private String pageName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_item);
		pageName = getIntent().getExtras().getString("pageName");
		populateFields();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void populateFields() {
    	//LinearLayout layout_pictures = (LinearLayout) findViewById(R.id.linearLayout_itemPictures);
    	//LinearLayout layout_desc = (LinearLayout) findViewById(R.id.linearLayout_itemDesc);
		LinearLayout layout_detail = (LinearLayout) findViewById(R.id.layout_detail);
		View newItemLayout;
    	
    	int i = this.getIntent().getExtras().getInt("index");
    	JSONObject json_data;
		try {
			json_data = DataManager.data.get(pageName).getJSONObject(i);
			XmlPullParserFactory factory;
			try {
				factory = XmlPullParserFactory.newInstance();
				factory.setNamespaceAware(true);
				XmlPullParser xpp = factory.newPullParser();
				String xmlString = DataManager.detailData.get(ItemActivity.this.pageName);
				xpp.setInput( new InputStreamReader ( new ByteArrayInputStream( xmlString.getBytes( "UTF-8" ))) );
				newItemLayout = inflater.inflate(xpp, null, json_data, ItemActivity.this);
				layout_detail.addView(newItemLayout);
			} catch (XmlPullParserException | IOException e) {
				e.printStackTrace();
			}
	    	
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	
	}

}
    
    
