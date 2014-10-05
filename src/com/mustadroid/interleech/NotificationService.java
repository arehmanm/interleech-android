package com.mustadroid.interleech;

import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mustadroid.interleech.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class NotificationService extends Service {

	final static String ACTION = "NotifyServiceAction";
	final static String STOP_SERVICE = "";
	final static int RQS_STOP_SERVICE = 1;
	
	NotifyServiceReceiver notifyServiceReceiver;
	
	@Override
	public void onCreate() {
		notifyServiceReceiver = new NotifyServiceReceiver();
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ACTION);
		registerReceiver(notifyServiceReceiver, intentFilter);
		
		Log.i("Notification", "Starting service..");
		
		DataManager.loadData(getApplicationContext(), "leechlist");
		JSONArray result = DataManager.data.get(MainActivity.pageName);
		if (result != null)
		for(int i=0; i<result.length(); i++) {
			String pageName;
			try {
				JSONObject json_data = (JSONObject) result.get(i);
				pageName = json_data.getString("name");
				DataManager.loadData(getApplicationContext(), pageName);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		Timer timer = new Timer();
		timer.schedule(new checkItemsTask(), 0, 60000);
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	class checkItemsTask extends TimerTask {

		@Override
		public void run() {
			JSONArray result = DataManager.data.get(MainActivity.pageName);
			if (result == null)
				return;
			for(int i=0; i<result.length(); i++) {
				String pageName;
				try {
					JSONObject json_data = (JSONObject) result.get(i);
					pageName = json_data.getString("name");
					int lastID = DataManager.getLastID(getApplicationContext(), pageName);
					checkSinglePage(lastID, pageName);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		
		private void checkSinglePage(int lastID, String pageName)
		{
			String url = "leechpage.php?time=" + Integer.toString(lastID) + "&name=" + pageName;
			JSONArray result = json.fetchData("http://83.252.202.28", url);
			Log.i("Notification", "Checking for new items. pageName = " + pageName + ", last id = " + lastID);
			if (result != null && result.length() > 0)
			{
				Log.i("Notification", "Found new item! count=" + result.length());
				try {
					DataManager.addData(result, pageName);
					DataManager.saveData(getApplicationContext(), pageName);
					JSONObject json_data = result.getJSONObject(0);
					//lastID = json_data.getInt("Time");
					String title = "";
					if (json_data.has("Address"))
						title = new String(json_data.getString("Address").getBytes("ISO-8859-1"), "UTF8");
					else if (json_data.has("Title"))
						title = new String(json_data.getString("Title").getBytes("ISO-8859-1"), "UTF8");
					else
						title = new String(json_data.getString("id").getBytes("ISO-8859-1"), "UTF8");
					
					String description = "";
					if (json_data.has("Area") && json_data.getString("Area").length() > 0)
						description += new String(json_data.getString("Area").getBytes("ISO-8859-1"), "UTF8") + ",";
					if (json_data.has("Rooms") && json_data.getString("Rooms").length() > 0)
						description += new String(json_data.getString("Rooms").getBytes("ISO-8859-1"), "UTF8") + ",";
					if (json_data.has("Price") && json_data.getString("Price").length() > 0)
						description += new String(json_data.getString("Price").getBytes("ISO-8859-1"), "UTF8") + ",";
					if (description.length() <=0 && json_data.has("User") && json_data.getString("User").length() > 0)
						description += new String(json_data.getString("User").getBytes("ISO-8859-1"), "UTF8");
					
					if (description.substring(description.length()-1).equals(","))
						description = description.substring(0, description.length()-1);
					sendNotification(lastID, title, description, pageName);
				} catch (JSONException | UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		
		private void sendNotification(int id, String title, String description, String pageName) {
			long[] pattern = {0, 500, 500, 500};
			NotificationCompat.Builder mBuilder =
			        new NotificationCompat.Builder(NotificationService.this)
			        .setSmallIcon(R.drawable.sweden_flag)
			        .setContentTitle(title)
			        .setVibrate(pattern)
			        .setContentText(description);
			Intent resultIntent = new Intent(NotificationService.this, LeechActivity.class);
			resultIntent.putExtra("index", 0);
			resultIntent.putExtra("pageName", pageName);

			TaskStackBuilder stackBuilder = TaskStackBuilder.create(NotificationService.this);
			PendingIntent pendingIntent =
					stackBuilder.create(NotificationService.this)
			                        .addNextIntentWithParentStack(resultIntent)
			                        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
			
			mBuilder.setContentIntent(pendingIntent);
			NotificationManager mNotificationManager =
			    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			Notification notif = mBuilder.build();
			mNotificationManager.notify(id, notif);
		}
		
	}

	@Override
	public void onDestroy() {
		this.unregisterReceiver(notifyServiceReceiver);
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	public class NotifyServiceReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			 int rqs = arg1.getIntExtra("RQS", 0);
			 if (rqs == RQS_STOP_SERVICE){
			  stopSelf();
			 }
		}
	}

}