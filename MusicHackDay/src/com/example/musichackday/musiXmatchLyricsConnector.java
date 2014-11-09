package com.example.musichackday;

import android.app.*;
import android.preference.*;
import android.util.*;
import org.apache.http.*;
import org.apache.http.conn.scheme.*;
import org.apache.http.conn.ssl.*;
import org.apache.http.impl.conn.tsccm.*;
import org.apache.http.params.*;
import org.apache.http.conn.*;
import org.apache.http.impl.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.client.*;
import android.content.pm.*;
import android.net.*;
import java.util.*;
import android.content.*;
import android.os.*;

public class musiXmatchLyricsConnector extends Handler
{
    private Messenger mService;
    boolean mIsBound;
    private static final String FROM_NOTIFICATION = "FROM_NOTIFICATION";
    private static String LYRICS_PLUGIN_PACKAGE;
    private static final String OLD_LYRICS_PLUGIN_PACKAGE = "com.musixmatch.android.lyrify";
    private static final String LYRICS_PLUGIN_ACTIVITY = "com.musixmatch.android.activities.LyricsActivity";
    private static final String PROD_APP_MARKET_URL = "http://lyr.cx/r/droid-plugin?";
    private static final String TEST_APP_MARKET_URL = "https://mxmdownloads.s3.amazonaws.com/lyriXmatch4android.apk";
    private static String APP_MARKET_URL;
    private Activity mBindingActivity;
    private Messenger mMessenger;
    private ProgressDialog s_ProgressDialog;
    private String pendingRequestUID;
    private String TAG;
    static SharedPreferences preferences;
    private ServiceConnection mConnection;
    
    static {
        musiXmatchLyricsConnector.LYRICS_PLUGIN_PACKAGE = "com.musixmatch.android.plugin";
        musiXmatchLyricsConnector.APP_MARKET_URL = "https://mxmdownloads.s3.amazonaws.com/lyriXmatch4android.apk";
        musiXmatchLyricsConnector.preferences = null;
    }
    
    private boolean init(final Activity bindingActivity) {
        try {
            if (musiXmatchLyricsConnector.preferences == null) {
                musiXmatchLyricsConnector.preferences = PreferenceManager.getDefaultSharedPreferences((Context)bindingActivity);
                musiXmatchLyricsConnector.LYRICS_PLUGIN_PACKAGE = musiXmatchLyricsConnector.preferences.getString("MUSIXMATCH_LYRICS_PLUGIN_PACKAGE", "com.musixmatch.android.lyrify");
                Log.i(this.TAG, "Lyrics plugin package: " + musiXmatchLyricsConnector.LYRICS_PLUGIN_PACKAGE);
                final AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>() {
                    protected HttpClient client = null;
                    
                    private void init() {
                        final HttpParams params = (HttpParams)new BasicHttpParams();
                        HttpProtocolParams.setVersion(params, (ProtocolVersion)HttpVersion.HTTP_1_1);
                        HttpProtocolParams.setContentCharset(params, "utf-8");
                        HttpConnectionParams.setStaleCheckingEnabled(params, false);
                        HttpConnectionParams.setConnectionTimeout(params, 20000);
                        HttpConnectionParams.setSoTimeout(params, 20000);
                        HttpConnectionParams.setSocketBufferSize(params, 8192);
                        final SchemeRegistry schemeRegistry = new SchemeRegistry();
                        schemeRegistry.register(new Scheme("http", (SocketFactory)PlainSocketFactory.getSocketFactory(), 80));
                        schemeRegistry.register(new Scheme("https", (SocketFactory)SSLSocketFactory.getSocketFactory(), 443));
                        final ClientConnectionManager manager = (ClientConnectionManager)new ThreadSafeClientConnManager(params, schemeRegistry);
                        this.client = (HttpClient)new DefaultHttpClient(manager, params);
                    }
                    
                    protected Void doInBackground(final String... params) {
                        try {
                            this.init();
                            final String url = params[0];
                            Log.i(musiXmatchLyricsConnector.this.TAG, "updating lyrics plugin package name ");
                            final HttpGet getMethod = new HttpGet(url);
                            final ResponseHandler<String> responseHandler = (ResponseHandler<String>)new BasicResponseHandler();
                            final long start = SystemClock.uptimeMillis();
                            final String responseBody = (String)this.client.execute((HttpUriRequest)getMethod, (ResponseHandler)responseHandler);
                            if (responseBody.length() > 0 && !responseBody.equals(musiXmatchLyricsConnector.LYRICS_PLUGIN_PACKAGE)) {
                                final SharedPreferences.Editor editor = musiXmatchLyricsConnector.preferences.edit();
                                editor.putString("MUSIXMATCH_LYRICS_PLUGIN_PACKAGE", responseBody);
                                editor.commit();
                                Log.i(musiXmatchLyricsConnector.this.TAG, "updated lyrics plugin package name to " + responseBody);
                            }
                            else {
                                Log.i(musiXmatchLyricsConnector.this.TAG, "No need to update lyrics plugin package name");
                            }
                        }
                        catch (Exception ex) {}
                        return null;
                    }
                };
                task.execute((String[])new String[] { "https://s3.amazonaws.com/mxmdownloads/lyriXmatch4android.txt" });
            }
        }
        catch (Exception ex) {}
        this.mBindingActivity = bindingActivity;
        final Intent i = this.createLyricsActivityIntent();
        final List<ResolveInfo> list = (List<ResolveInfo>)this.mBindingActivity.getPackageManager().queryIntentActivities(i, 65536);
        if (list.size() > 0) {
            this.mConnection = (ServiceConnection)new MatcherSeviceConnection();
            return true;
        }
        return false;
    }
    
    private Intent createLyricsActivityIntent() {
        final Intent i = new Intent();
        i.setClassName(musiXmatchLyricsConnector.LYRICS_PLUGIN_PACKAGE, "com.musixmatch.android.activities.LyricsActivity");
        i.putExtra("FROM_NOTIFICATION", true);
        return i;
    }
    
    private Intent createOldLyricsActivityIntent() {
        final Intent i = new Intent();
        i.setClassName("com.musixmatch.android.lyrify", "com.musixmatch.android.activities.LyricsActivity");
        i.putExtra("FROM_NOTIFICATION", true);
        return i;
    }
    
    private Intent createLyricsServiceIntent() {
        Intent i = null;
        if (this.checkInstalledApp((Context)this.mBindingActivity, musiXmatchLyricsConnector.LYRICS_PLUGIN_PACKAGE)) {
            i = new Intent("com.musixmatch.android.services.RemoteScrobblingService");
            i.setClassName(musiXmatchLyricsConnector.LYRICS_PLUGIN_PACKAGE, "com.musixmatch.android.services.ScrobblerService");
        }
        else {
            i = new Intent("com.musixmatch.android.services.RemoteScrobblingService");
        }
        return i;
    }
    
    private musiXmatchLyricsConnector() {
        super();
        this.mService = null;
        this.mBindingActivity = null;
        this.mMessenger = new Messenger((Handler)this);
        this.pendingRequestUID = "";
        this.TAG = "musiXmatchLyricsConnector";
        this.mConnection = (ServiceConnection)new MatcherSeviceConnection();
    }
    
    public musiXmatchLyricsConnector(final Activity bindingActivity) {
        super();
        this.mService = null;
        this.mBindingActivity = null;
        this.mMessenger = new Messenger((Handler)this);
        this.pendingRequestUID = "";
        this.TAG = "musiXmatchLyricsConnector";
        this.mConnection = (ServiceConnection)new MatcherSeviceConnection();
        final boolean res = this.init(bindingActivity);
        if (res) {
            this.doBindService();
        }
    }
    
    public boolean getIsBound() {
        return this.mIsBound && this.mService != null;
    }
    
    public boolean getIsLyricsPluginInstalled() {
        final Intent i = this.createLyricsActivityIntent();
        final Intent oldi = this.createOldLyricsActivityIntent();
        final List<ResolveInfo> list = (List<ResolveInfo>)this.mBindingActivity.getPackageManager().queryIntentActivities(i, 65536);
        final List<ResolveInfo> oldlist = (List<ResolveInfo>)this.mBindingActivity.getPackageManager().queryIntentActivities(oldi, 65536);
        return list.size() > 0 || oldlist.size() > 0;
    }
    
    private boolean checkInstalledApp(final Context ctx, final String uri) {
        final PackageManager pm = ctx.getPackageManager();
        boolean app_installed = false;
        try {
            pm.getPackageInfo(uri, 1);
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }
    
    public void setTestMode(final boolean testMode) {
        if (testMode) {
            musiXmatchLyricsConnector.APP_MARKET_URL = "https://mxmdownloads.s3.amazonaws.com/lyriXmatch4android.apk";
        }
        else {
            musiXmatchLyricsConnector.APP_MARKET_URL = "http://lyr.cx/r/droid-plugin?";
        }
    }
    
    public boolean getTestMode() {
        return musiXmatchLyricsConnector.APP_MARKET_URL.equals("https://mxmdownloads.s3.amazonaws.com/lyriXmatch4android.apk");
    }
    
    private String getAppMarketUrl() {
        if (this.getTestMode()) {
            return musiXmatchLyricsConnector.APP_MARKET_URL;
        }
        final String s = String.valueOf(musiXmatchLyricsConnector.APP_MARKET_URL) + "&referrer=" + Uri.encode(this.mBindingActivity.getApplicationContext().getPackageName());
        Log.d(this.TAG, "Market download url: " + s);
        return s;
    }
    
    public void openLyricsActivityOrDownloadLyricsPlugin() {
        final Intent i = this.createLyricsActivityIntent();
        final Intent oldi = this.createOldLyricsActivityIntent();
        final List<ResolveInfo> list = (List<ResolveInfo>)this.mBindingActivity.getPackageManager().queryIntentActivities(i, 65536);
        final List<ResolveInfo> oldlist = (List<ResolveInfo>)this.mBindingActivity.getPackageManager().queryIntentActivities(oldi, 65536);
        if (list.size() > 0) {
            Log.d(this.TAG, "Opening new plugin");
            this.mBindingActivity.startActivity(i);
        }
        else if (oldlist.size() > 0) {
            Log.d(this.TAG, "Opening old plugin");
            this.mBindingActivity.startActivity(oldi);
        }
        else {
            final Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(this.getAppMarketUrl()));
            this.mBindingActivity.startActivity(intent);
        }
    }
    
    public void downloadLyricsPlugin() {
        final Intent i = this.createLyricsActivityIntent();
        final Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(this.getAppMarketUrl()));
        this.mBindingActivity.startActivity(intent);
    }
    
    public void handleMessage(final Message msg) {
        try {
            switch (msg.what) {
                case 0: {
                    Log.d(this.TAG, "Lyrics Id>> " + String.valueOf(msg.getData().getLong("lyrics_id")));
                    final String current_request_id = msg.getData().getString("request_id");
                    Log.d(this.TAG, "Response Id>> " + current_request_id);
                    if (this.pendingRequestUID.equals(current_request_id)) {
                        synchronized (this) {
                            Log.d(this.TAG, "Processing the response");
                            if (this.s_ProgressDialog != null && this.s_ProgressDialog.isShowing()) {
                                this.s_ProgressDialog.dismiss();
                            }
                            this.s_ProgressDialog = null;
                            this.openLyricsActivityOrDownloadLyricsPlugin();
                            return;
                        }
                    }
                    return;
                }
            }
            super.handleMessage(msg);
        }
        catch (Exception ex) {
            Log.d(this.TAG, "", (Throwable)ex);
        }
    }
    
    public void doBindService() {
        final Intent i = this.createLyricsServiceIntent();
        this.mBindingActivity.bindService(i, this.mConnection, 1);
        this.mIsBound = true;
    }
    
    public void doUnbindService() {
        if (this.mIsBound) {
            this.mBindingActivity.unbindService(this.mConnection);
            this.mIsBound = false;
        }
    }
    
    public void startLyricsActivity(final String artist, final String track) throws RemoteException {
        this.startLyricsActivity(artist, track, null, 0L);
    }
    
    public void startLyricsActivity(final String artist, final String track, final long duration) throws RemoteException {
        this.startLyricsActivity(artist, track, null, duration);
    }
    
    public void startLyricsActivity(final String artist, final String track, final String album) throws RemoteException {
        this.startLyricsActivity(artist, track, album, 0L);
    }
    
    public void startLyricsActivity(final String artist, final String track, final String album, final long duration) throws RemoteException {
        final Bundle b = new Bundle();
        b.putString("artist", artist);
        b.putString("track", track);
        if (album != null) {
            b.putString("album", album);
        }
        b.putLong("duration", duration);
        this.startLyricsActivity(b);
    }
    
    private void startLyricsActivity(final Bundle parameters) throws RemoteException {
        final Message msg = Message.obtain((Handler)null, 0, this.hashCode(), 0);
        msg.replyTo = this.mMessenger;
        parameters.putString("request_id", this.pendingRequestUID = UUID.randomUUID().toString());
        Log.d(this.TAG, "Request Id>> " + this.pendingRequestUID);
        msg.setData(parameters);
        synchronized (this) {
            if (this.s_ProgressDialog != null && this.s_ProgressDialog.isShowing()) {
                this.s_ProgressDialog.dismiss();
            }
            this.s_ProgressDialog = ProgressDialog.show((Context)this.mBindingActivity, (CharSequence)"Please wait", (CharSequence)"Searching for the lyrics", true);
        }
        this.mService.send(msg);
    }
    
    static /* synthetic */ void access$0(final musiXmatchLyricsConnector musiXmatchLyricsConnector, final Messenger mService) {
        musiXmatchLyricsConnector.mService = mService;
    }
    
    private class MatcherSeviceConnection implements ServiceConnection
    {
        public void onServiceConnected(final ComponentName className, final IBinder service) {
            musiXmatchLyricsConnector.access$0(musiXmatchLyricsConnector.this, new Messenger(service));
        }
        
        public void onServiceDisconnected(final ComponentName className) {
            musiXmatchLyricsConnector.access$0(musiXmatchLyricsConnector.this, null);
            musiXmatchLyricsConnector.this.doUnbindService();
        }
    }
}
