package vkontakte;
import java.net.*;
import java.io.*;
import java.util.regex.*;
import java.util.Iterator;
import java.util.LinkedList;
import android.util.Log;

// TODO сделать защиту от слишком частых запросов

public class VKontakte {
	
	public String login = "";
	public String pass = "";
	public String cookie = "";
	public String s_value = "";
	public String UA = "Mozilla/5.0 (X11; U; Linux i686; uk; rv:1.9.2.3) Gecko/20100401 Firefox/3.6.3 GTB7.0";
	public String errorString = "";
	
	public VKontakte(String login, String pass)
	{
		this.login = login;
		this.pass = pass;
	}
	
	public String get_name_by(String id, Matcher m)
	{
		//group(1) - id
		//group(2) - string
		while (m.find())
		{
			String id_album = m.group(1);
			String name = m.group(2);
			if (id_album.compareTo(id) == 0)
				return name;
		}
		return "<NONE>";
	}
	
	public LinkedList<VKSong> get_songs_list(String query)
	{
		LinkedList<String[]> l = new LinkedList<String[]>();
		LinkedList<String> urls = new LinkedList<String>();
		LinkedList<String> ids = new LinkedList<String>();
		
		String source = get_page(query);
		
		String regall = "([^<>]*)";
		Pattern pfindall = Pattern.compile("return operate\\(([[a-zA-Z_0-9]() ,']*)\\);");
		Matcher matcher = pfindall.matcher(source);
		
		while (matcher.find())
		{
			String temp = matcher.group(1);
			temp = temp.replace(",'", " ");
			temp = temp.replace("',", " ");
			temp = temp.replace(",", " ");
			String[] res = temp.split(" ");
			l.add(res);
		}
		Iterator<String[]> i = l.iterator();
		while (i.hasNext())
		{
			String[] temp = i.next();
			String id_id = temp[0];
			String id_server = temp[1];
			String id_folder = temp[2];
			String id_file = temp[3];
			
			String url = "http://cs" + id_server + ".vkontakte.ru/u" + id_folder + "/audio/" + id_file + ".mp3";
			urls.add(url);
			ids.add(id_id);
		}
		
		Pattern presult_time = Pattern.compile("<div class=\\\\\"duration\\\\\">"+regall + "<");
		Matcher matcher2 = presult_time.matcher(source);
		
		Pattern presult_album = Pattern.compile("<b id=\\\\\"performer([0-9]*)\\\\\">" + regall + "<");
		Matcher result_album = presult_album.matcher(source);
		
		
		Pattern presult_track = Pattern.compile("<span id=\\\\\"title([0-9]*)\\\\\">" + regall + "<");
		Matcher result_track = presult_track.matcher(source);
		
		LinkedList<VKSong> song_list = new LinkedList<VKSong>(); 
		
		int k = 0;
		while (matcher2.find())
		{
			String id = ids.get(k);
			String url = urls.get(k); 
			
			String album = get_name_by(id, result_album);
			String track = get_name_by(id, result_track);
			
			VKSong song = new VKSong(url, album, track, matcher2.group(1));
			song_list.add(song);
			
			k++;
		}
		
		return song_list;
	}
	
	public void get_cookie()
	{
		try {
			URL host = new URL("http://vkontakte.ru/login.php?op=slogin");
			String data = URLEncoder.encode("s", "UTF-8") + "=" + URLEncoder.encode(s_value, "UTF-8");
			HttpURLConnection connection = (HttpURLConnection) host.openConnection();
			connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            
            connection.setRequestProperty("User-Agent", UA);
            connection.setRequestProperty("Host", "vkontakte.ru");
            connection.setRequestProperty("Referer", "http://login.vk.com/?act=login");
            connection.setRequestProperty("Connection", "close");
            connection.setRequestProperty("Cookie", "remixchk=5; remixsid=nonenone");
            connection.setRequestProperty("Pragma", "no-cache");
            connection.setRequestProperty("Cache-Control", "no-cache");
			
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(data);
            writer.close();
            
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            	//System.out.println("Cookie HTTP OK");
            	String cookie_src = connection.getHeaderField("Set-Cookie");
            	//System.out.println(cookie_src);
            	cookie = cookie_src;
            }
            
		} catch (Exception e)
		{
			Log.v("vklog", e.getMessage());
		} finally
		{
		
		}
		
	}
	
	public String get_page(String search)
	{
		this.get_cookie();
		try {
			URL url = new URL("http://vkontakte.ru/gsearch.php?section=audio&q=vasya#c[q]=some%20id&c[section]=audio&name=1");
			
			String data = URLEncoder.encode("c[q]", "UTF-8") + "=" + URLEncoder.encode(search, "UTF-8");
            data += "&" + URLEncoder.encode("c[section]", "UTF-8") + "=" + URLEncoder.encode("audio", "UTF-8");
            
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            
            connection.setRequestProperty("User-Agent", UA);
            connection.setRequestProperty("Host", "vkontakte.ru");
            connection.setRequestProperty("Refer", "http://vkontakte.ru/index.php");
            connection.setRequestProperty("X-Requested-With","XMLHttpRequest");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            connection.setRequestProperty("Connection", "close");
            connection.setRequestProperty("Cookie", "remixlang=0; remixchk=5; audio_vol=100; " + this.cookie);
            connection.setRequestProperty("Pragma", "no-cache");
            connection.setRequestProperty("Cache-Control", "no-cache");
            
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(data);
            writer.close();
            
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            	//System.out.println("GetPage HTTP OK");
            	BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            	String src = rd.readLine();
            	if (src.length()==0)
                {
            		errorString = "Downloading page error";
            		return "";
                }
            	//while ((line = rd.readLine()) != null) {
                //System.out.println(line);
                //}
            	
            	//Pattern p = Pattern.compile("return operate\(([\w() ,']*)\);");
            	
            	
            	return src;
            }
            
		} catch (Exception e)
		{
			Log.v("vklog", e.getMessage());
			errorString = e.getMessage();
			return "";
		} finally
		{
			
		}
		
		
		
		return "";
	}
	
	public boolean auth()
	{
		try {
			Log.v("vksong", "Request");
			/*request*/
			URL url = new URL("http://login.vk.com/?act=login");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            
            connection.setRequestProperty("User-Agent", UA);
            connection.setRequestProperty("Host", "login.vk.com");
            connection.setRequestProperty("Referer", "http://vkontakte.ru/index.php");
            connection.setRequestProperty("Pragma", "no-cache");
            connection.setRequestProperty("Cache-Control", "no-cache");
            
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            String data = URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(login, "UTF-8");
            data += "&" + URLEncoder.encode("expire", "UTF-8") + "=" + URLEncoder.encode("", "UTF-8");
            data += "&" + URLEncoder.encode("pass", "UTF-8") + "=" + URLEncoder.encode(pass, "UTF-8");
            data += "&" + URLEncoder.encode("vk", "UTF-8") + "=" + URLEncoder.encode("", "UTF-8");
            
            writer.write(data);
            writer.close();
            
            String res = "";
            
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            	//System.out.println("Auth HTTP OK");
            	Log.v("vksong", "Auth HTTP OK");
            	BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            	String line = "";
            	while ((line = rd.readLine()) != null) 
            	{
            		res += line;
                }   	
            	
            	
            	Pattern pattern = Pattern.compile("name='s' value='(.*?)'");
            	Matcher matcher = pattern.matcher(res);
            	if (matcher.find())
            	{
            		String response = matcher.group(1);
            		//System.out.println("Found: " + response);
            	
            		this.s_value = response;
            		return true;
            	} else
            	{
            		errorString = "Authentication error";
            		return false;
            	}
            } else
            {
            	Log.v("vklog", "Auth HTTP FALSE");
            	errorString = "Bad login/password";
            	return false;
            }
		} catch (Exception e)
		{
			Log.v("vklog", e.getMessage());
			errorString = e.getMessage();
			return false;
		} finally
		{
			
		}
	}
}
