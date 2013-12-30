package com.hipipal.whip;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;

import com.zuowuxuxi.base.MyApp;
import com.zuowuxuxi.base._WBase;
import com.zuowuxuxi.lib.ShakeDetector;
import com.zuowuxuxi.util.NAction;
import com.zuowuxuxi.util.NUtil;
import com.zuowuxuxi.util.VeDate;
import greendroid.widget.ActionBarItem;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

@SuppressLint("NewApi")
public class WhipAct extends _ABaseAct implements OnInitListener,
		CreateNdefMessageCallback, OnNdefPushCompleteCallback {
	private final Handler mHandler = new Handler();

	ShakeDetector mShaker;
	Vibrator vibe;
	TextToSpeech speak;
	SoundPool mySoundpool;
	HashMap<Integer, Integer> soundPoolMap;

	private MediaPlayer myMediaplayer;
	private AudioManager AM;

	private int GameStatus = 0;
	private boolean GameMaster = true;

	private boolean LongVoice = true;

	private int count = 0;
	private static final int REQ_TTS_STATUS_CHECK = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarContentView(R.layout.main);

		MyApp.getInstance().addActivity(this, CONF.BASE_PATH, "");
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		// ((TextView) findViewById(R.id.text)).setText(R.string.shake_whip);

		/*
		 * addActionBarItem(Type.Refresh, R.id.action_bar_refresh);
		 * addActionBarItem(
		 * getActionBar().newActionBarItem(NormalActionBarItem.class)
		 * .setDrawable(R.drawable.ic_title_export)
		 * .setContentDescription(R.string.gd_export), R.id.action_bar_export);
		 * addActionBarItem(Type.Locate, R.id.action_bar_locate);
		 */

		// 显示ready文字
		// TextView display_msg = (TextView) findViewById(R.id.display_message);
		// display_msg.setText("Ready");
		// display_msg.setTextSize(70);

		//
		if (!NUtil.checkAppInstalledByName(getApplicationContext(),
				"com.google.android.tts")) {
			Log.d("tag", "tts not installed");
			Toast.makeText(this, "PLEASE INSTALL TTS", Toast.LENGTH_SHORT)
					.show();

			Intent intent = NAction
					.openRemoteLink(getApplicationContext(),
							"https://play.google.com/store/apps/details?id=com.google.android.tts");
			startActivity(intent);
		} else {
			// Log.d("tag",Integer.toString(TextToSpeech.Engine.CHECK_VOICE_DATA_PASS));
			display_msg("Ready");

			speak = new TextToSpeech(this, this);
			speak.setLanguage(Locale.US);

			initConnection();

			/*
			 * checkUpdate(CONF.BASE_PATH);
			 * 
			 * String[] appConf = NAction.getAppConf(getApplicationContext());
			 * String feed = appConf[2];
			 * 
			 * TextView feedLink = (TextView)findViewById(R.id.feed_link); if
			 * (!feed.equals("")) { feedLink.setText(feed);
			 * feedLink.setVisibility(View.VISIBLE); }
			 */
		}

	}

	@Override
	public void onDestroy() {
		// myMediaplayer.release();
		mShaker.pause();

		if (speak != null) {
			speak.shutdown();
		}

		super.onDestroy();
		MyApp.getInstance().exit();

	}

	@Override
	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
		return true;
	}

	private void playVoice(String voice) {

		speak.speak(voice, TextToSpeech.QUEUE_ADD, null);
	}

	// 两个手机touch后，进入游戏，然后开始倒计时
	private void countDown() {
		Thread thread = new Thread() {
			@Override
			public void run() {
				int count = 30;
				while (count > 0) {
					count--;
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} //
					// Log.d("countdown", Integer.toString(count));
					/*
					 * switch (count) { case 3: { playVoice("3"); } break; case
					 * 2: { playVoice("2"); } break; case 1: { playVoice("1"); }
					 * break; default: break; }
					 */
				}
				GameStatus = 2;
				mShaker.pause();
				playVoice("game over");
			}
		};
		// if (GameStatus) {
		thread.start();
		// }
	}

	private void doShaker() {
		mShaker.setOnShakeListener(new ShakeDetector.OnShakeListener() {
			public void onShake() {
				_shake();
				count = _count_shake(count);
			}
		});
	}

	// 再首页的textview中显示内容
	private void display_msg(String msg) {
		TextView dis_msg = (TextView) findViewById(R.id.display_message);
		dis_msg.setText(msg);
		dis_msg.setTextSize(70);
	}

	// 统计甩鞭子的次数
	public int _count_shake(int count) {
		count += 1;
		display_msg(Integer.toString(count));
		return count;
	}

	public void _shake() {
		// playSound(1,0);

		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		if (GameMaster) {
			myMediaplayer = MediaPlayer.create(getApplicationContext(),
					R.raw.whipcrack2);
		} else {
			myMediaplayer = MediaPlayer.create(getApplicationContext(),
					R.raw.whipcrack1);
		}
		// myMediaplayer.setDataSource();
		myMediaplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		setMediaListener(myMediaplayer);
		if (myMediaplayer != null) {
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
		// vibe.vibrate(100);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	protected void initAction() {
		if (GameStatus == 0) {
			mShaker.resume();
		}
		// myMediaplayer.prepare();
		/*
		 * mySoundpool = new SoundPool(100,AudioManager.STREAM_MUSIC,100);
		 * soundPoolMap = new HashMap<Integer,Integer>();
		 * 
		 * soundPoolMap.put(1,mySoundpool.load(this,R.raw.msg,1));
		 * //mySoundpool.setVolume(1, 1, 1);
		 */
		GameStatus = 1;
		playVoice("ready");
		try {
			Thread.currentThread().sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		playVoice("go");
		count = 0;
		display_msg(Integer.toString(count));
		doShaker();
		countDown();
	}

	private void setMediaListener(MediaPlayer mediaPlayer) {
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

	protected void playSound(int sound, int loop) {
		AudioManager mgr = (AudioManager) this
				.getSystemService(Context.AUDIO_SERVICE);
		myMediaplayer.seekTo(0);
		myMediaplayer.start();
		/*
		 * try { myMediaplayer.prepare(); } catch (IllegalStateException e) { //
		 * TODO Auto-generated catch block e.printStackTrace(); } catch
		 * (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */

		// ��ʼ��������С
		// ((Activity)
		// getApplicationContext()).setVolumeControlStream(AudioManager.STREAM_MUSIC);

		/*
		 * int currentVol = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
		 * mgr.setStreamVolume(AudioManager.STREAM_MUSIC, currentVol, 0);
		 * 
		 * //mgr.setSpeakerphoneOn(true); float streamVolumeCurrent =
		 * mgr.getStreamVolume(AudioManager.STREAM_MUSIC); float streamVolumeMax
		 * = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC); float volume =
		 * streamVolumeCurrent/streamVolumeMax;
		 * mySoundpool.play(soundPoolMap.get(sound), volume, volume, 1, loop,
		 * 1f);
		 */
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
		String shareContent = MessageFormat.format(
				getString(R.string.share_info), feedUrl);
		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("text/plain");
		share.putExtra(Intent.EXTRA_TEXT, shareContent);

		startActivity(Intent.createChooser(share, getString(R.string.share)));
	}

	public void checkUpdate(View v) {
		// if (NUtil.netCheckin(getApplicationContext())) {
		// String[] conf = NAction.getAppConf(getApplicationContext());
		// if (conf[6].equals("")) {
		checkUpdate(CONF.BASE_PATH);

		/*
		 * } else { NAction.recordAdLog(getApplicationContext(), "feedback",
		 * ""); Intent intent = NAction.openRemoteLink(this, conf[6]);
		 * this.startActivity(intent); }
		 */
		/*
		 * } else { Toast.makeText(getApplicationContext(), R.string.net_error,
		 * Toast.LENGTH_SHORT).show(); }
		 */
	}

	public void onSwitch(View v) {
		ImageView iv = (ImageView) findViewById(R.id.whip_logo);
		if (LongVoice) {
			LongVoice = false;
			// iv.setImageResource(R.drawable.whip_2);
		} else {

			LongVoice = true;
			// iv.setImageResource(R.drawable.whip_1);

		}
	}

	public void onLike(View v) {
		Intent intent = NAction.openRemoteLink(this,
				getString(R.string.facebook_link));
		this.startActivity(intent);
	}

	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
		if (status == TextToSpeech.SUCCESS) {

		}
	}

	// ////////////////////////////////River ADDED
	NfcAdapter _nfcAdapter;
	private PendingIntent _nfcPendingIntent;
	IntentFilter[] _readTagFilters;

	@SuppressLint("NewApi")
	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");

		super.onPause();

		_nfcAdapter.disableForegroundDispatch(this);
	}

	@SuppressLint("NewApi")
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
		recvMsg();
		_nfcAdapter.enableForegroundDispatch(this, _nfcPendingIntent,
				_readTagFilters, null);
	}

	@SuppressLint("NewApi")
	@Override
	protected void onNewIntent(Intent intent) {
		Log.d(TAG, "onNewIntent");

		if (intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
			NdefMessage[] msgs = getNdefMessagesFromIntent(intent);
			NdefRecord record = msgs[0].getRecords()[0];
			byte[] payload = record.getPayload();

			String payloadString = new String(payload);

			Toast.makeText(getApplicationContext(), payloadString,
					Toast.LENGTH_SHORT).show();
			GameMaster = false;
			Log.d("gamestatus at challenge", Integer.toString(GameStatus));
			if (GameStatus == 2) {
				whoWin(Integer.parseInt(payloadString), count);
				GameStatus = 0;
			} else if (GameStatus == 1) {

			} else if (GameStatus == 0) {
				initAction();
			}

		} else if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
			Toast.makeText(this, "This NFC tag has no NDEF data.",
					Toast.LENGTH_LONG).show();
		}
	}

	@SuppressLint("NewApi")
	public void initConnection() {
		_nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		mShaker = new ShakeDetector(this);
		vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		if (_nfcAdapter == null) {

			WBase.setTxtDialogParam(R.drawable.alert_dialog_icon,
					"NFC is not available, please update your mobile",
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
						}
					});
			showDialog(_WBase.DIALOG_NOTIFY_MESSAGE + dialogIndex);
			dialogIndex++;
		} else {

			if (!_nfcAdapter.isEnabled()) {
				WBase.setTxtDialogParam(R.drawable.alert_dialog_icon,
						"NFC is closed, please enable it with beam",
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								startActivity(new Intent(
										Settings.ACTION_NFC_SETTINGS));
							}
						});
				showDialog(_WBase.DIALOG_NOTIFY_MESSAGE + dialogIndex);
				dialogIndex++;
			} else {
				// send msg
				_nfcAdapter.setNdefPushMessageCallback(this, this);
				_nfcAdapter.setOnNdefPushCompleteCallback(this, this);

				// start NFC Connection
				_nfcPendingIntent = PendingIntent.getActivity(this, 0,
						new Intent(this, getClass())
								.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

				IntentFilter ndefDetected = new IntentFilter(
						NfcAdapter.ACTION_NDEF_DISCOVERED);
				try {
					ndefDetected
							.addDataType("application/com.hipipal.whip.nfc");
				} catch (MalformedMimeTypeException e) {
					throw new RuntimeException("Could not add MIME type.", e);
				}

				_readTagFilters = new IntentFilter[] { ndefDetected };

			}

		}
	}

	@SuppressLint("NewApi")
	public void recvMsg() {
		Log.d(TAG, "recvMsg");
		if (_nfcAdapter == null) {

			WBase.setTxtDialogParam(R.drawable.alert_dialog_icon,
					"NFC is not available, please update your mobile",
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
						}
					});
			showDialog(_WBase.DIALOG_NOTIFY_MESSAGE + dialogIndex);
			dialogIndex++;
		} else {

			if (!_nfcAdapter.isEnabled()) {
				WBase.setTxtDialogParam(R.drawable.alert_dialog_icon,
						"NFC is closed, please enable it with beam",
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								startActivity(new Intent(
										Settings.ACTION_NFC_SETTINGS));
							}
						});
				showDialog(_WBase.DIALOG_NOTIFY_MESSAGE + dialogIndex);
				dialogIndex++;
			} else {
				// OK
				if (getIntent().getAction() != null) {

					if (getIntent().getAction().equals(
							NfcAdapter.ACTION_NDEF_DISCOVERED)) {
						NdefMessage[] msgs = getNdefMessagesFromIntent(getIntent());
						NdefRecord record = msgs[0].getRecords()[0];
						byte[] payload = record.getPayload();

						String payloadString = new String(payload);

						// Toast.makeText(this, payloadString,
						// Toast.LENGTH_SHORT).show();
						GameMaster = false;
						Log.d("gamestatus", Integer.toString(GameStatus));
						if (GameStatus == 2) {
							whoWin(Integer.parseInt(payloadString), count);
							GameStatus = 0;
						} else if (GameStatus == 1) {

						} else if (GameStatus == 0) {
							initAction();
						}

					} else {

					}
				}

				_nfcAdapter.enableForegroundDispatch(this, _nfcPendingIntent,
						_readTagFilters, null);

			}
		}
	}

	@SuppressLint("NewApi")
	NdefMessage[] getNdefMessagesFromIntent(Intent intent) {
		// Parse the intent
		NdefMessage[] msgs = null;
		String action = intent.getAction();
		if (action.equals(NfcAdapter.ACTION_TAG_DISCOVERED)
				|| action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
			Parcelable[] rawMsgs = intent
					.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			if (rawMsgs != null) {
				msgs = new NdefMessage[rawMsgs.length];
				for (int i = 0; i < rawMsgs.length; i++) {
					msgs[i] = (NdefMessage) rawMsgs[i];
				}

			} else {
				// Unknown tag type
				byte[] empty = new byte[] {};
				NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN,
						empty, empty, empty);
				NdefMessage msg = new NdefMessage(new NdefRecord[] { record });
				msgs = new NdefMessage[] { msg };
			}

		} else {
			Log.e(TAG, "Unknown intent.");
			finish();
		}
		return msgs;
	}

	@SuppressLint("NewApi")
	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
		Log.d("Game Status.....", Integer.toString(GameStatus));
		String data;
		if (GameStatus == 2) {
			data = Integer.toString(count);
		} else {
			data = "Whip challenger";
		}

		String mimeType = "application/com.hipipal.whip.nfc";

		byte[] mimeBytes = mimeType.getBytes(Charset.forName("UTF-8"));
		byte[] dataBytes = data.getBytes(Charset.forName("UTF-8"));
		byte[] id = new byte[0];

		NdefRecord record = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
				mimeBytes, id, dataBytes);

		NdefMessage message = new NdefMessage(new NdefRecord[] { record });

		return message;
	}

	Handler mhandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Toast.makeText(getApplicationContext(), "Whip Master",
					Toast.LENGTH_SHORT).show();
			GameMaster = true;
			if (GameStatus == 0) {
				initAction();
			} else if (GameStatus == 1) {

			} else if (GameStatus == 2) {
				// whoWin();
				GameStatus = 0;
			}

		}
	};

	@Override
	public void onNdefPushComplete(NfcEvent arg0) {
		Log.d(TAG, "onNdefPushComplete");
		mhandler.sendEmptyMessage(0);
	}

	@Override
	public boolean onKeyDown(int keycode, KeyEvent event) {
		if (keycode == KeyEvent.KEYCODE_BACK) {
			WBase.setTxtDialogParam(R.drawable.alert_dialog_icon,
					"Confirm to exit ?", new OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							finish();
						}
					});
			showDialog(_WBase.DIALOG_NOTIFY_MESSAGE + dialogIndex);
			dialogIndex++;
		}
		return super.onKeyDown(keycode, event);
	}

	private void whoWin(int master_value, int challenge_value) {
		if (master_value > challenge_value) {
			try {
				Thread.currentThread().sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			playVoice("i am loser");
		} else if (master_value < challenge_value) {
			try {
				Thread.currentThread().sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			playVoice("i am winner");
		} else if (master_value == challenge_value) {
			try {
				Thread.currentThread().sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			playVoice("draw");
			try {
				Thread.currentThread().sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			playVoice("try again");
		}
	}

}// end class
