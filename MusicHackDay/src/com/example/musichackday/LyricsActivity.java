package com.example.musichackday;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class LyricsActivity extends Activity {

    private Song song;
    int correctAnswer;
    
    TextView questionText;
    Button button1;
    Button button2;
    Button button3;
    Button button4;
    
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

        Log.wtf("TRACKSIZE?", "TRACKSIZE?: " + trackData.message.body.track_list.size());
        Log.wtf("TRACK_ID?", "TRACK_ID?: " + trackData.message.body.track_list.get(0).track.track_id);
        String lyricsUrl = "http://api.musixmatch.com/ws/1.1/track.subtitle.get?track_id=" + trackData.message.body.track_list.get(0).track.track_id + "&apikey=d8951a826384c648324e206c942b5cce";
        TrackLyrics trackLyrics = NetworkParser.getTrackLyricsFromUrl(lyricsUrl);
        Log.wtf("url", "url: " + lyricsUrl);

        Log.wtf("SIZE", "SIZE: " + trackLyrics.message.body.subtitle.subtitle_body);

        // TODO: Don't make this static.
        MainActivity.musicSrv.playSong();
        
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);
        
        button1.setOnClickListener(new OnClickListener() {           
            @Override
            public void onClick(View v) {
                checkAnswer(1);
            }    
        });

        button2.setOnClickListener(new OnClickListener() {           
            @Override
            public void onClick(View v) {
                checkAnswer(2);
            }    
        });

        button3.setOnClickListener(new OnClickListener() {           
            @Override
            public void onClick(View v) {
                checkAnswer(3);
            }     
        });

        button4.setOnClickListener(new OnClickListener() {           
            @Override
            public void onClick(View v) {
                checkAnswer(4);
            }    
        });
        
        resetQuestion();
    }
    
    public void checkAnswer(int answer) {
        if (answer == correctAnswer) {
            Toast.makeText(this, "Correct", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Incorrect", Toast.LENGTH_SHORT).show();
        }
        
        wait(500);
        MainActivity.musicSrv.resumeSong();
        
        resetQuestion();
    }
    
    public void resetQuestion() {
//        questionText.setText(text);
//        button1.setText(text);
//        button2.setText(text);
//        button3.setText(text);
//        button4.setText(text);
    }
    
    public void wait(int milliseconds) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
             // Actions to do after 10 seconds
            }
        }, milliseconds); 
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
