package com.hipipal.whip;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;

import com.zuowuxuxi.base.MyApp;
import com.zuowuxuxi.lib.ShakeDetector;
import com.zuowuxuxi.util.NAction;
import com.zuowuxuxi.util.VeDate;
import greendroid.widget.ActionBarItem;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class WhipAct extends _ABaseAct
{
    private final Handler mHandler = new Handler();

	ShakeDetector mShaker;
    Vibrator vibe ;

    SoundPool mySoundpool;  
    HashMap<Integer,Integer> soundPoolMap;  
    
    private MediaPlayer myMediaplayer;  
    private AudioManager AM;
    
    private boolean LongVoice = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarContentView(R.layout.main);
        
        MyApp.getInstance().addActivity(this, CONF.BASE_PATH, ""); 

        //((TextView) findViewById(R.id.text)).setText(R.string.shake_whip);

        /*addActionBarItem(Type.Refresh, R.id.action_bar_refresh);
        addActionBarItem(
            getActionBar().newActionBarItem(NormalActionBarItem.class)
                          .setDrawable(R.drawable.ic_title_export)
                          .setContentDescription(R.string.gd_export),
            R.id.action_bar_export);
        addActionBarItem(Type.Locate, R.id.action_bar_locate);*/
        initAction();
        
		checkUpdate(CONF.BASE_PATH);
		
        String[] appConf = NAction.getAppConf(getApplicationContext());
        String feed = appConf[2];

        TextView feedLink = (TextView)findViewById(R.id.feed_link);
        if (!feed.equals("")) {
        	feedLink.setText(feed);
        	feedLink.setVisibility(View.VISIBLE);
        }
        

    }
    
    @Override
    public void onDestroy() {
    	//myMediaplayer.release();
    	mShaker.pause();
    	

    	super.onDestroy();
    	MyApp.getInstance().exit(); 

    }

    @Override
    public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
        return true;
    }
    
    
    private void doShaker() {
        mShaker = new ShakeDetector(this);
        vibe = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        mShaker.setOnShakeListener(new ShakeDetector.OnShakeListener () {
        	public void onShake() {
            		_shake();
        	}
        });
    }
    public void _shake() {
    	//playSound(1,0);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        if (LongVoice) {
            myMediaplayer = MediaPlayer.create(getApplicationContext(), R.raw.whipcrack2); 

        } else {
        	myMediaplayer = MediaPlayer.create(getApplicationContext(), R.raw.whipcrack1); 
        }
        //myMediaplayer.setDataSource();
        myMediaplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        setMediaListener(myMediaplayer);
        if(myMediaplayer != null){
        	myMediaplayer.stop();
        }
        try {
			myMediaplayer.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//vibe.vibrate(100);
    }
    @Override
	public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    }
    
	protected void initAction() {  		
        //myMediaplayer.prepare();
		/*mySoundpool = new SoundPool(100,AudioManager.STREAM_MUSIC,100); 
		soundPoolMap = new HashMap<Integer,Integer>();  
	
		soundPoolMap.put(1,mySoundpool.load(this,R.raw.msg,1)); 
		//mySoundpool.setVolume(1, 1, 1);*/
        doShaker();

	}  
	
    private void setMediaListener(MediaPlayer mediaPlayer){  
        mediaPlayer.setOnCompletionListener(new OnCompletionListener() {  
            @Override  
            public void onCompletion(MediaPlayer mp) {
                try {  
                	myMediaplayer.release();  
                	myMediaplayer = null;
                } catch (Exception e) {  
                }
            }  
        });  
        mediaPlayer.setOnErrorListener(new OnErrorListener() {  
            @Override  
            public boolean onError(MediaPlayer mp, int what, int extra) { 
				StringBuilder sb = new StringBuilder();
				sb.append("Media Player Error: ");
				switch (what) {
					case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
						sb.append("Not Valid for Progressive Playback");
						break;
					case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
						sb.append("Server Died");
						break;
					case MediaPlayer.MEDIA_ERROR_UNKNOWN:
						sb.append("Unknown");
						break;
					default:
						sb.append(" Non standard (");
						sb.append(what);
						sb.append(")");
				}
				sb.append(" (" + what + ") ");
				sb.append(extra);
				  
				try {  
					myMediaplayer.release();  
                	myMediaplayer = null;
				} catch (Exception e) {  
                    e.printStackTrace();  
				}
				return false;  
            }  
        }); 
        
        mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
		        myMediaplayer.start();
			}
        });
    } 
	
	protected void playSound(int sound , int loop){  
		AudioManager mgr = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
		myMediaplayer.seekTo(0);
		myMediaplayer.start();
		/*try {
			myMediaplayer.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
    	// ��ʼ��������С
		//((Activity) getApplicationContext()).setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
        /*int currentVol = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
        mgr.setStreamVolume(AudioManager.STREAM_MUSIC, currentVol, 0);
		
		//mgr.setSpeakerphoneOn(true);
		float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);  
		float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);  
		float volume = streamVolumeCurrent/streamVolumeMax;  
		mySoundpool.play(soundPoolMap.get(sound), volume, volume, 1, loop, 1f);  */
	}  
	
    public void onShare(View v) {
		NAction.recordUserLog(getApplicationContext(), "ishare", "");

        String[] appConf = NAction.getAppConf(getApplicationContext());
        String about = appConf[0];
        String link = appConf[1];
        String feed = appConf[2];
        String feedUrl = appConf[3];
        
        if (feedUrl.equals("")) {
        	feedUrl = getString(R.string.market_url);
        }
		String shareContent = MessageFormat.format(getString(R.string.share_info), feedUrl);
		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("text/plain");
		share.putExtra(Intent.EXTRA_TEXT, shareContent);

		startActivity(Intent.createChooser(share, getString(R.string.share)));
    }
    
	public void checkUpdate(View v) {
		//if (NUtil.netCheckin(getApplicationContext())) {
			//String[] conf = NAction.getAppConf(getApplicationContext());
			//if (conf[6].equals("")) {
				checkUpdate(CONF.BASE_PATH);
				
			/*} else {
				NAction.recordAdLog(getApplicationContext(), "feedback", "");
				Intent intent = NAction.openRemoteLink(this, conf[6]);
				this.startActivity(intent);	
			}*/
		/*} else {
			Toast.makeText(getApplicationContext(), R.string.net_error, Toast.LENGTH_SHORT).show();
		}*/
	}
	public void onSwitch(View v) {
		ImageView iv = (ImageView)findViewById(R.id.whip_logo);
		if (LongVoice) {
			LongVoice = false;
			iv.setImageResource(R.drawable.whip_2);
		} else {

			LongVoice = true;
			iv.setImageResource(R.drawable.whip_1);


		}
	}
	public void onLike(View v) {
		Intent intent = NAction.openRemoteLink(this, getString(R.string.facebook_link));
		this.startActivity(intent);	
	}
	
}
