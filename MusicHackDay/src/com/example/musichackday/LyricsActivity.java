package com.example.musichackday;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class LyricsActivity extends Activity {

    private Song song;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyrics);
        
        song = MainActivity.musicSrv.songs.get(MainActivity.musicSrv.songPosn);
        String artist = song.getArtist();
        artist = artist.replaceAll(" ", "%20");
        String title = song.getTitle();
        title = title.replaceAll(" ", "%20");

        String url = "http://api.musixmatch.com/ws/1.1/track.search?q_track=" + title + "&q_artist=" + artist + "&f_has_lyrics=1&apikey=d8951a826384c648324e206c942b5cce";
        TrackData trackData = NetworkParser.getTrackDataFromUrl(url);
        Log.wtf("url", "url: " + url);

        String lyricsUrl = "http://api.musixmatch.com/ws/1.1/matcher.subtitle.get?track.subtitle.get?track_id=" + trackData.message.body.track_list.get(0).track_id + "&apikey=d8951a826384c648324e206c942b5cce";
        TrackLyrics trackLyrics = NetworkParser.getTrackLyricsFromUrl(lyricsUrl);
        Log.wtf("url", "url: " + lyricsUrl);

        Log.wtf("SIZE", "SIZE: " + trackLyrics.message.body.subtitle.subtitle_body);

        // TODO: Don't make this static.
        MainActivity.musicSrv.playSong();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.lyrics, menu);
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
    
    @Override
    protected void onPause() {
        super.onPause();
        MainActivity.musicSrv.player.stop();
    }
}
