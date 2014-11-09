package com.example.musichackday;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.json.JSONObject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.MediaController.MediaPlayerControl;

import com.example.musichackday.MusicService.MusicBinder;

public class MainActivity extends Activity implements MediaPlayerControl {
    public static MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;

    private MusicController controller;
    private ArrayList<Song> songList;
    private ListView songView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy); 

        songView = (ListView)findViewById(R.id.song_list);
        songList = new ArrayList<Song>();

        getSongList();

        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        SongAdapter songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);

        // Other
        final String mArtistName = "The Beatles";
        final String mTrackName  = "Let It Be";

        String url = "http://api.musixmatch.com/ws/1.1/track.search?q_track=back%20to%20december&q_artist=taylor%20swift&f_has_lyrics=1&apikey=d8951a826384c648324e206c942b5cce";

        JSONObject jsonObject = NetworkParser.getJSONFromUrl(url);

        TrackData trackData = NetworkParser.getTrackDataFromUrl(url);

        Log.wtf("SIZE", "SIZE: " + trackData.message.body.track_list.size());

        String lyricsUrl = "http://api.musixmatch.com/ws/1.1/matcher.subtitle.get?q_track=sexy%20and%20i%20know%20it&q_artist=lmfao&f_subtitle_length=200&f_subtitle_length_max_deviation=3&apikey=d8951a826384c648324e206c942b5cce";
        TrackLyrics trackLyrics = NetworkParser.getTrackLyricsFromUrl(lyricsUrl);

        Log.wtf("SIZE", "SIZE: " + trackLyrics.message.body.subtitle.subtitle_body);

        setController();
    }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder)service;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicSrv.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    public void getSongList() {
        //retrieve song info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist));
            }
            while (musicCursor.moveToNext());
        }
    }
    
    public void songPicked(View view){
        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        Intent intent = new Intent(this, LyricsActivity.class);
        startActivity(intent);
//        musicSrv.playSong();
      }

    private void setController(){
        //set the controller up
        controller = new MusicController(this);

        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });

        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);
    }

    //play next
    private void playNext(){
        musicSrv.playNext();
        controller.show(0);
    }

    //play previous
    private void playPrev(){
        musicSrv.playPrev();
        controller.show(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public int getDuration() {
        if(musicSrv!=null && musicBound && musicSrv.isPng())
            return musicSrv.getDur();
        else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicSrv!=null && musicBound && musicSrv.isPng()) {
            return musicSrv.getPosn();
        }
        else return 0;
    }

    @Override
    public void pause() {
        musicSrv.pausePlayer();
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    @Override
    public void start() {
        musicSrv.go();
    }

    @Override
    public boolean isPlaying() {
        if(musicSrv!=null && musicBound)
            return musicSrv.isPng();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_shuffle:
          //shuffle
          break;
        case R.id.action_end:
          stopService(playIntent);
          musicSrv=null;
          System.exit(0);
          break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
      stopService(playIntent);
      musicSrv=null;
      super.onDestroy();
    }

}