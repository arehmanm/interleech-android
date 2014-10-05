package com.mustadroid.interleech;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

public class json {
	
	public static String fetchFile(String url) {
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		InputStream is = null;
		try{
		     HttpClient httpclient = new DefaultHttpClient();
		
		     HttpPost httppost = new HttpPost(url);
		     httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		     HttpResponse response = httpclient.execute(httppost);
		     HttpEntity entity = response.getEntity();
		     is = entity.getContent();
	     } catch(Exception e){
	         Log.e("log_tag", "Error in http connection"+e.toString());
	     }
		
		String result = null;
		try {
           BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF8"));
	       StringBuilder sb = new StringBuilder();
	       sb.append(reader.readLine() + "\n");
	
	       String line="0";
	       while ((line = reader.readLine()) != null) {
	                      sb.append(line + "\n");
	        }
	        is.close();
	        result=sb.toString();
        } catch(Exception e) {
           Log.e("log_tag", "Error converting result "+e.toString());
        }
		return result;
	}
	
	public static JSONArray fetchData(String url, String file) {
		String result = fetchFile(url + "/" + file);
		
		JSONArray jArray = null;
		try{
			if (result != null)
				jArray = new JSONArray(result);
		} catch(JSONException e1) {
			e1.printStackTrace();
		}
		
		return jArray;
	}
}
