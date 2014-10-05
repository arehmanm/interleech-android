package com.mustadroid.interleech;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class DataManager {
	public static HashMap<String, JSONArray> data = new HashMap<String, JSONArray>();
	public static HashMap<String, String> briefData = new HashMap<String, String>();
	public static HashMap<String, String> detailData = new HashMap<String, String>();
	
	public static int getLastID(Context context, String pageName)
	{
		int lastID = 0;
		if (DataManager.data != null && DataManager.data.get(pageName) != null) {
			try {
				lastID = DataManager.data.get(pageName).getJSONObject(0).getInt("Time");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return lastID;
	}
	
	public static class downloadPictureTask extends AsyncTask<Void, Integer, Bitmap> {
		
		private String imageURL;
		private ImageView imageView; 
		private int imageSize;
		private Boolean adjustWidth;
		private String imageName;
		private Boolean skipDownload;
		
		public downloadPictureTask(String url, ImageView itemPicture, int width, Boolean adjustwidth)
		{
			skipDownload = false;
			imageURL = url;
			imageView = itemPicture;
			imageSize = width;
			adjustWidth = adjustwidth;
			
			String parts[] = url.split("/");
			imageName = "/storage/sdcard0/InterLeech/Pictures/" + parts[parts.length - 1];
			File imageFile = new File(imageName);
			if (imageFile.exists())
				skipDownload = true;
		}
		
        @Override
        protected Bitmap doInBackground(Void... arg0) {
            Bitmap bmp = null;
        	if (skipDownload)
        		return BitmapFactory.decodeFile(imageName);
			try {
            	URL url = new URL(imageURL);
				Bitmap bmp2 = BitmapFactory.decodeStream(url.openConnection().getInputStream());
				float ar = ((float)bmp2.getHeight()) / bmp2.getWidth();
				bmp = Bitmap.createScaledBitmap(bmp2, 320, (int)((float)320 * ar), false);
				bmp2.recycle();
			} catch (IOException e) {
				e.printStackTrace();
			}
            return bmp;
        }
        
        @Override
        protected void onPostExecute(Bitmap result)
        {
        	if (result != null){
        		float ar = ((float)result.getHeight()) / result.getWidth();
				int width, height;
            	if (adjustWidth) {
            		width = imageSize;
            		height = (int) (imageSize * ar);
            	} else {
            		height = imageSize;
            		width = (int) ((float)imageSize / ar);
            	}
        		imageView.getLayoutParams().width = width;
        		imageView.getLayoutParams().height = height;
            	imageView.setImageBitmap(result);
            	if (!skipDownload)
            		saveImageToDisk(result, imageName);
        	}
        }
        
        private void saveImageToDisk(Bitmap bmp, String filename)
        {
        	FileOutputStream out = null;
			try {
			    out = new FileOutputStream(filename);
			    bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
			} catch (Exception e) {
			    e.printStackTrace();
			} finally {
			    try {
			        if (out != null) {
			            out.close();
			        }
			    } catch (IOException e) {
			        e.printStackTrace();
			    }
			}
        }
    }

	public static void addData(JSONArray arr, String pageName) throws JSONException {
	    JSONArray result = new JSONArray();
	    for (int i = 0; i < arr.length(); i++) {
	        result.put(arr.get(i));
	    }
	    if (DataManager.data != null && DataManager.data.get(pageName) != null)
		    for (int i = 0; i < DataManager.data.get(pageName).length(); i++) {
		        result.put(DataManager.data.get(pageName).get(i));
		    }
	    DataManager.data.put(pageName, result);
	}
	
	private static String readFromInternalStorage(Context context, String filename) {
		String toReturn = null;
	    FileInputStream fis;
	    try {
	        fis = context.openFileInput(filename);
	        ObjectInputStream oi = new ObjectInputStream(fis);
	        try {
				toReturn = (String) oi.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
	        oi.close();
	    } catch (FileNotFoundException e) {
	        Log.e("InternalStorage", e.getMessage());
	    } catch (IOException e) {
	        Log.e("InternalStorage", e.getMessage()); 
	    }
	    return toReturn;
	}
	
	private static void saveToInternalStorage(Context context, String result, String filename) {
        try {
            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream of = new ObjectOutputStream(fos);
            of.writeObject(result);
            of.flush();
            of.close();
            fos.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

	public static void loadData(Context context, String pageName) {
		Log.i("DataManager", "Loading data for page: " + pageName);
		String data = readFromInternalStorage(context, pageName + ".json");
		Log.i("DataManager", "Loaded: " + data);
		if (data != null)
		{
	    	try {
	    		JSONArray jArray = new JSONArray(data);
	    		DataManager.data.put(pageName, jArray);
			} catch (JSONException e2) {
				e2.printStackTrace();
			}
		}		
	}

	public static void saveData(Context context, String pageName) {
		Log.i("DataManager", "Saving data for page: " + pageName);
		String data = DataManager.data.get(pageName).toString();
		saveToInternalStorage(context, data, pageName + ".json");
		Log.i("DataManager", "Saved data: " + data);
	}
	
}