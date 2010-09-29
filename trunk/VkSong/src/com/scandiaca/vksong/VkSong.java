package com.scandiaca.vksong;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import com.scandiaca.vksong.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import vkontakte.*;
import android.view.*;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.*;
import android.view.inputmethod.InputMethodManager;


public class VkSong extends Activity implements OnClickListener, OnItemClickListener 
{
	
	private static ArrayList<VKSong> todoItems;
	private static EditText myEditText;
	private static VKontakte vk;
	private static VKSong_Adapter aa;
	private static LinkedList<VKSong> ll;
	private static VKSong song;
	private static AlertDialog errorDialog;
	
	private static int ID = 1;
	private static NotificationManager mNM;
	private static PendingIntent contentIntent;
	private static Notification notification;
	private static Context ac;
	private static String errorMessage = "";
	
	private void callError()
	{
		if (errorMessage=="") return;
		errorDialog.setTitle("Error");
		errorDialog.setMessage(errorMessage);
		errorDialog.setButton("OK", new DialogInterface.OnClickListener() {
		      public void onClick(DialogInterface dialog, int which) {
			    	 dialog.cancel();
			      }
			});
		errorMessage = "";
		errorDialog.show();
	}
	
	private class VKSong_Adapter extends ArrayAdapter<VKSong> {
        private ArrayList<VKSong> items;
        public VKSong_Adapter(Context context, int textViewResourceId, ArrayList<VKSong> items) {
                super(context, textViewResourceId, items);
                this.items = items;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.listview, null);
                }
                VKSong song = items.get(position);
                if (song != null) {
                		TextView T1 = (TextView) v.findViewById(R.id.TextView01);
                		TextView T2 = (TextView) v.findViewById(R.id.TextView02);
                        if (T1 != null) {
                              T1.setText(song.artist);
                        }
                        if(T2 != null){
                              T2.setText(song.track);
                        }                 
                }
                return v;
        }        
    }
	
	private class DownloadingList extends AsyncTask<Void, Void, Void>
	{
		ProgressDialog dialog;
		@Override
	     protected void onPreExecute() 
		 {
			dialog = ProgressDialog.show(VkSong.this, "Downloading data", "Please wait..");
	     }
		
		@Override
		protected Void doInBackground(Void... arg0) 
		{
			try
			{
				SharedPreferences pref = getSharedPreferences("VkSongSettings",0);
				vk.login = pref.getString("login", "");
				vk.pass = pref.getString("pass", "");
				boolean res = vk.auth();
				if (!res)
				{
					errorMessage = vk.errorString;
					return null;
				}
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(myEditText.getWindowToken(), 0);
				String query = myEditText.getText().toString();
				todoItems.clear();
				
				Log.v("vklog","Button Click");
				Log.v("vklog", query);
				
				ll = vk.get_songs_list(query);
				Iterator<VKSong> li = ll.iterator();
				while (li.hasNext())
				{
					VKSong song = li.next();
					if (!song.track.equals(""))
					{
						String text = song.artist + " - " + song.track;
						Log.v("vklog", text);
						todoItems.add(song);
					}
				}
			} catch (Exception e)
			{
				Log.v("vklog", e.getMessage());
				errorMessage = e.getMessage();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) 
	    {
			dialog.dismiss();
			callError();
			aa.notifyDataSetChanged();
	    }
		
	}
	
	public static class DownloadService extends Service 
	{
		
		String artist = song.artist;
		String track = song.track;
		Notification nf;
		int nID;
		
		@Override
	    public void onCreate()
		{
			Log.v("vklog", "downloading started");
			String surl = song.url;
			artist = song.artist;
			track = song.track;
			nf = notification;
			nID = ID;
			try
			{
				SharedPreferences pref = getSharedPreferences("VkSongSettings",0);
				URL url = new URL(surl);
				URLConnection con = url.openConnection();
				
				BufferedInputStream in =  new BufferedInputStream(con.getInputStream());
				
				String path = pref.getString("path", "")+"/";
				File f = new File(path);
				if (f.isDirectory())
				{
					Log.v("dlog", "Ok Dir");
				} else 
					if (!f.isFile()&&f.mkdirs())
					{
						Log.v("dlog", "Create Dir");
					}
				
				int len = con.getContentLength();
				int size = 0;
				
				FileOutputStream out =  new FileOutputStream(path + artist + " - "+ track + ".mp3"); 
				
				int i = 0;
				int oldps = 0;
				byte[] bytesIn = new byte[1024];
				
				Log.v("dlog", "Downloading...");
				mNM.notify(ID, notification);
				while ((i = in.read(bytesIn)) >= 0)
			    {
					out.write(bytesIn, 0, i);
					size += i;
					double ps = size/((double)len/100.0);
					if (oldps != (int)ps)
					{
						String format = String.format("%d", (int)ps);
						Log.v("dlog", format+"%");
						nf.setLatestEventInfo(ac, artist + " - " + track , format+"%", contentIntent);
						mNM.notify(nID,nf);
					}
					oldps = (int)ps;
			    }
			    out.close();
			    in.close();
			    Log.v("dlog", "OK");
			}
			catch (Exception e)
			{
				Log.v("dlog","Error:" + e.getMessage());
				mNM.cancel(nID);
				errorMessage = e.getMessage();
			}
		}

		@Override
		public IBinder onBind(Intent arg0)
		{
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	public class DownloadFileTask extends AsyncTask<Void, Void, Void>
	{
		Context context;
		Intent intent;
		@Override
		protected Void doInBackground(Void... arg0) 
		{
			context = getBaseContext();
			intent = new Intent(context, DownloadService.class);
			context.startService(intent);
			
			return null;
			
		}
		
		@Override
		protected void onPostExecute(Void result) 
	    {
			
			context.stopService(intent);
			if (errorMessage == "") 
				Toast.makeText(ac, "Downloading done", Toast.LENGTH_LONG).show();
			callError();
			//mNM.cancel(ID);й
	    }
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	Log.v("vklog", "=======================");
    	Log.v("vklog", "Create Activity");
    	
    	errorDialog = new AlertDialog.Builder(VkSong.this).create();
    	
    	Log.v("vklog","Init");
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);
        
    	ListView myListView = (ListView)findViewById(R.id.myListView);
    	myEditText = (EditText)findViewById(R.id.myEditText);
    	Button mySearchButton = (Button)findViewById(R.id.mySearchButton);
        
    	todoItems = new ArrayList<VKSong>();
    	aa = new VKSong_Adapter(this, R.layout.listview, todoItems);
        
    	myListView.setAdapter(aa);
    	vk = new VKontakte("","");
        
    	mySearchButton.setOnClickListener(this);
    	myListView.setOnItemClickListener(this);
    	
    }
    
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
    	VKSong vk = (VKSong)parent.getItemAtPosition(position);
		Log.v("vklog", vk.artist + " " + vk.track);
		song = vk;
      	contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, VkSong.class), 0);
      	ac = getApplicationContext();
		
		AlertDialog alertDialog = new AlertDialog.Builder(VkSong.this).create();
		alertDialog.setTitle("Download song?");
		alertDialog.setMessage(vk.artist + " - " + vk.track);
		
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
		      public void onClick(DialogInterface dialog, int which) {
		    	  
		    	DownloadFileTask dft = new DownloadFileTask();
		  		
		      	mNM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		      	notification = new Notification(R.drawable.icon, "Downloading " + song.artist + " - " + song.track, System.currentTimeMillis());
		      	notification.setLatestEventInfo(ac, song.artist + " - " + song.track, "0%", contentIntent);
		      	ID++;    
		  		dft.execute(null);
		} });
		
		
		alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {
		      public void onClick(DialogInterface dialog, int which) {
		    	 dialog.cancel();
		      }
		});
		alertDialog.show();
		Log.v("dlog", "errors2");
    }
    
    public void onClick(View v) 
	{
    	DownloadingList dl = new DownloadingList();
    	dl.execute(null);
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      super.onCreateOptionsMenu(menu);
      // Create and add new menu items.
      MenuItem itemAdd = menu.add(0, Menu.FIRST, Menu.NONE, "Settings");

      itemAdd.setOnMenuItemClickListener(new OnMenuItemClickListener() {
		@Override
		public boolean onMenuItemClick(MenuItem arg0) {
			Intent i = new Intent(VkSong.this, Preferences.class);
			startActivity(i);
			return false;
		}});
      return true;
    }
    
    

}