package com.example.musichackday;

import java.io.Serializable;
import java.util.ArrayList;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class MusicService extends Service implements
MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
MediaPlayer.OnCompletionListener, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2856656681827687363L;
    //media player
    // TODO: Don't make public
    public MediaPlayer player;
    //song list
    // TODO: Don't make public
    public ArrayList<Song> songs;
    //current position
    // TODO: Don't make public
    public int songPosn;

    private String songTitle = "";
    private static final int NOTIFY_ID=1;

    private final IBinder musicBind = new MusicBinder();

    public void onCreate() {
        //create the service
        super.onCreate();
        //initialize position
        songPosn=0;
        //create player
        player = new MediaPlayer();

        initMusicPlayer();
    }

    public void initMusicPlayer(){
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    public void playSong(){
        //play a song
        player.reset();
        //get song
        Song playSong = songs.get(songPosn);
        //get id
        long currSong = playSong.getID();
        //set uri
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);

        try{
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        player.prepareAsync();
    }

    public void setList(ArrayList<Song> theSongs){
        songs = theSongs;
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public boolean isPng(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        player.pause();
    }

    public void seek(int posn) {
        player.seekTo(posn);
    }

    public void go(){
        player.start();

        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)
        .setSmallIcon(R.drawable.play)
        .setTicker(songTitle)
        .setOngoing(true)
        .setContentTitle("Playing")
        .setContentText(songTitle);
        Notification not = builder.build();

        startForeground(NOTIFY_ID, not);
    }

    public void playPrev(){
        songPosn--;
        if(songPosn < 0) songPosn=songs.size()-1;
        playSong();
    }

    //skip to next
    public void playNext(){
        songPosn++;
        if(songPosn >= songs.size()) songPosn=0;
        playSong();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
      //start playback
      mp.start();
    }
    
    public void setSong(int songIndex){
        songPosn=songIndex;
      }
}
