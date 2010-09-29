package com.scandiaca.vksong;

import com.scandiaca.vksong.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Preferences extends Activity
{
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences);
		
		final SharedPreferences pref = getSharedPreferences("VkSongSettings",0);
		
	    Button bt = (Button)findViewById(R.id.SaveButton);
	    final EditText email = (EditText)findViewById(R.id.EditEmail);
	    final EditText pass = (EditText)findViewById(R.id.EditPass);
	    final EditText path = (EditText)findViewById(R.id.EditPath);
	    
	    //loading preferences
	    email.setText(pref.getString("login", ""));
	    pass.setText(pref.getString("pass", ""));
	    path.setText(pref.getString("path", "/sdcard/VkSong/"));
	    
	    bt.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) 
			{
				//saving preferences
				SharedPreferences.Editor editor = pref.edit();
				editor.putString("login", email.getText().toString());
				editor.putString("pass", pass.getText().toString());
				editor.putString("path", path.getText().toString());
				editor.commit();
				Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();
				finish();
			}
			
	    });
    }
}
