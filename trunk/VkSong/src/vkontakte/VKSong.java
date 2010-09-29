package vkontakte;

public class VKSong {
	public String url;
	public String artist;
	public String track;
	public String time;
	
	public VKSong(String url, String artist, String track, String time)
	{
		this.url = url;
		this.artist = artist;
		this.track = track;
		this.time = time;
	}
	
	public void printAll()
	{
		System.out.printf("%s %s - %s [%s]\n", time, artist, track, url);
	}
}
