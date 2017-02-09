package com.example.qr_codescan;

import java.io.File;
import java.io.FileInputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.MediaStore.Video.Thumbnails;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class PlayerFullScreenActivity extends Activity {

	private static final String TAG = "PlayerFullScreenActivity";

	public static final String PACKAGE_NAME = "com.fujifilm.ea.launcher.activity.PlayerFullScreenActivity";

	public static final String PLAY_DATA = "play_data";
	public static final String PLAY_DATA_ID = "play_data_id";
	private static final int CENTER_PLAY_TAG = 0;
	private static final int SURFACE_VIEW_TAG = 1;
	private static final int SMALL_PLAY_TAG = 2;
	private static final int REWIND_DOWN_TAG = 3;
	private static final int REWIND_UP_TAG = 4;

	private static final int REWIND_SECOND = 1000 * 20;
	private static final int ANIMATION_TIME = 1000;
	private static final int COUNTDOWN_TIME = 3;

	private static final int PLAY_CLICK = 0;
	private static final int VIEW_CLICK = 1;
	private static final int PAUSE_CLICK = 2;
	private static final int PLAYER_START = 3;

	private MediaPlayer mMediaPlayer;

	private UpDateSeekBar update;
	private SeekBar seekbar;
	private TextView mCurrentTimeView;
	private Button mRewindDownBtn;
	private Button mRewindUpBtn;
	private Button mSmallPlayBtn;
	private Button mCenterPlayBtn;
	private SurfaceView mSurfaceView;
	private LinearLayout mControlBar;
	private ImageView mDefaultImage;

	private boolean isPlay = true;
	private int postSize;
	private String mFilePath;
	private Bitmap mBitmap;
	private String mDataId;
	
	// power manager
	private PowerManager mPowerManager;
	
	private PowerManager.WakeLock mWakeLock;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		// keep screen open
		mWakeLock = mPowerManager.newWakeLock(
				PowerManager.ACQUIRE_CAUSES_WAKEUP
						| PowerManager.SCREEN_DIM_WAKE_LOCK, "Tag");
		mWakeLock.acquire();
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_full_screen);
		Intent intent = getIntent();
		// TbMediaData data = (TbMediaData)
		// intent.getSerializableExtra(PLAY_DATA);
		String playData = intent.getStringExtra(PLAY_DATA);
		mDataId = intent.getStringExtra(PLAY_DATA_ID);
		if (playData != null) {
			mFilePath = playData;
		}
		initScreen();
//		findViewById(R.id.video_back).requestFocus();
		findViewById(R.id.video_back).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						finish();
					}

				});
		
	}

	private void initScreen() {
		mMediaPlayer = new MediaPlayer();
		update = new UpDateSeekBar();
		seekbar = (SeekBar) findViewById(R.id.seekbar);
		mCurrentTimeView = (TextView) findViewById(R.id.current_time);

		mCenterPlayBtn = (Button) findViewById(R.id.play);
		mCenterPlayBtn.setEnabled(false);
		mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);
		// mSurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mSurfaceView.getHolder().setKeepScreenOn(true);
		mSurfaceView.getHolder().addCallback(new PlaySurFaceView());

		mRewindDownBtn = (Button) findViewById(R.id.rewind_down);
		mRewindUpBtn = (Button) findViewById(R.id.rewind_up);
		mSmallPlayBtn = (Button) findViewById(R.id.small_play);
		mCurrentTimeView = (TextView) findViewById(R.id.current_time);

		mControlBar = (LinearLayout) findViewById(R.id.control_bar);

		mDefaultImage = (ImageView) findViewById(R.id.default_pic_view);

		mMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
			@Override
			public void onBufferingUpdate(MediaPlayer mp, int percent) {
			}
		});

		mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				isPlay = false;
				mControlHandler.sendEmptyMessage(PAUSE_CLICK);
				seekbar.setProgress(seekbar.getMax());
				setProgressText(mp.getDuration(), mp.getDuration());
			}
		});

		mCenterPlayBtn.setOnClickListener(new PlayListener()); // screen center play button
		mCenterPlayBtn.setTag(CENTER_PLAY_TAG);
		
		mSurfaceView.setOnClickListener(new PlayListener());
		mSurfaceView.setTag(SURFACE_VIEW_TAG);
		
		mSmallPlayBtn.setOnClickListener(new PlayListener()); // control bar play button
		mSmallPlayBtn.setTag(SMALL_PLAY_TAG);

		seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					int value = seekbar.getProgress() * mMediaPlayer.getDuration() / seekbar.getMax();
					mMediaPlayer.seekTo(value);
				}
			}
		});

		mRewindDownBtn.setOnClickListener(new RewindBtnListener());
		mRewindDownBtn.setTag(REWIND_DOWN_TAG);
		
		mRewindUpBtn.setOnClickListener(new RewindBtnListener());
		mRewindUpBtn.setTag(REWIND_UP_TAG);
	}

	@Override
	protected void onPause() {
		super.onPause();
		pauseMediaPlayer();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mWakeLock != null) {
			mWakeLock.release();
			mWakeLock = null;
		}
//		if (getApplication() instanceof EAApplication) {
//			((EAApplication) getApplication()).setStartScreen(true);
//		}

		if (mControlHandler != null) {
			mControlHandler.removeMessages(0);
			mControlHandler = null;
		}

		if (mHandler != null) {
			mHandler.removeMessages(0);
			mHandler = null;
		}
		mDefaultImage.setImageBitmap(null);
		if (mBitmap != null) {
			mBitmap.recycle();
			mBitmap = null;
		}

		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (mMediaPlayer == null) {
				isPlay = false;
			} else if (mMediaPlayer.isPlaying()) {
				isPlay = true;
				int position = mMediaPlayer.getCurrentPosition();
				long sMax = seekbar.getMax();
				long mMax = mMediaPlayer.getDuration();
				seekbar.setProgress((int) (position * sMax / mMax));

				// System.out.println("seekbar.getProgress() = " +
				// seekbar.getProgress());
				// System.out.println("position = " + position);
				// System.out.println("mMax = " + mMax);
				// System.out.println("sMax = " + sMax);
				if (position > 0) {
					setProgressText(position, mMax);
				}
			} else {
				return;
			}
		};
	};

	Handler mControlHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case PLAY_CLICK:
				countDown();
//				if (getApplication() instanceof EAApplication) {
//					((EAApplication) getApplication()).setStartScreen(false);
//				}
				break;
			case VIEW_CLICK:
				if (mMediaPlayer.isPlaying()) {
					// mPlayBtn.setVisibility(VISIBLE);
					mControlBar.clearAnimation();
					mControlBar.setVisibility(View.VISIBLE);
//					findViewById(R.id.video_back).requestFocus();
					if (mControlHandler.hasMessages(PLAY_CLICK)) {
						mControlHandler.removeMessages(PLAY_CLICK);
					}
					mCountdownTime = COUNTDOWN_TIME;
					countDown();
				}
				break;
			case PAUSE_CLICK:
				if (mControlHandler.hasMessages(PLAY_CLICK)) {
					mControlHandler.removeMessages(PLAY_CLICK);
				}
				mCenterPlayBtn.setBackgroundResource(R.drawable.btn_movie_play);
				mCenterPlayBtn.setVisibility(View.VISIBLE);
				mSmallPlayBtn.setBackgroundResource(R.drawable.btn_small_play);
				mControlBar.clearAnimation();
				mControlBar.setVisibility(View.VISIBLE);
//				findViewById(R.id.video_back).requestFocus();

				mCountdownTime = COUNTDOWN_TIME;
//				if (getApplication() instanceof EAApplication) {
//					((EAApplication) getApplication()).setStartScreen(true);
//				}
				break;
			case PLAYER_START:
				if (isPlay == false) {
					isPlay = true;
					new Thread(update).start();
				}
				mMediaPlayer.start();
				// mPlayBtn.setBackgroundResource(R.drawable.movie_stop_bt);
				mCenterPlayBtn.setVisibility(View.GONE);
				mDefaultImage.setVisibility(View.GONE);
				mSmallPlayBtn.setBackgroundResource(R.drawable.btn_small_pause);
				mControlHandler.sendEmptyMessage(PLAY_CLICK);

//				if (getApplication() instanceof EAApplication) {
//					((EAApplication) getApplication()).setStartScreen(false);
//				}
				break;
			}
		};
	};

	private int mCountdownTime = COUNTDOWN_TIME;

	// TODO
	private void countDown() {
		if (mCountdownTime >= 0) {
			mCountdownTime--;
			mControlHandler.sendEmptyMessageDelayed(PLAY_CLICK, 1000);
		} else {
			// mCenterPlayBtn.setVisibility(GONE);
			Animation anim = getAnimDown();
			anim.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationEnd(Animation arg0) {
					// mControlBar.setVisibility(GONE);
				}

				@Override
				public void onAnimationRepeat(Animation arg0) {

				}

				@Override
				public void onAnimationStart(Animation arg0) {

				}

			});
			mControlBar.startAnimation(anim);
		}
	}

	/**
	 * controller move down
	 * 
	 * @return Animation
	 */
	private Animation getAnimDown() {
		Animation anim = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_SELF, 0,
				TranslateAnimation.RELATIVE_TO_SELF, 0f, TranslateAnimation.RELATIVE_TO_PARENT, 0f,
				TranslateAnimation.RELATIVE_TO_PARENT, 1f);
		LinearInterpolator inter = new LinearInterpolator();
		anim.setInterpolator(inter);
		anim.setDuration(ANIMATION_TIME);
		anim.setFillBefore(false);
		anim.setFillAfter(true);
		return anim;
	}

	private void setProgressText(long position, long mMax) {

		StringBuffer hour1Str = new StringBuffer();
		StringBuffer hour2Str = new StringBuffer();
		int hour1 = (int) (position / 1000 / 60 / 60);
		int hour2 = (int) (mMax / 1000 / 60 / 60);
		if (hour1 < 10) {
			hour1Str = hour1Str.append("0");
			hour1Str = hour1Str.append(hour1);
			hour1Str = hour1Str.append(":");
		}
		if (hour2 < 10) {
			hour2Str = hour2Str.append("0");
			hour2Str = hour2Str.append(hour2);
			hour2Str = hour2Str.append(":");
		}

		// TODO DateFormat.format("HH:mm:ss", position) get hour is 8 ? after
		// will slove it
		StringBuffer textSb = new StringBuffer();
		textSb.append(hour1Str);
		textSb.append(DateFormat.format("mm:ss", position)).append("/");
		textSb.append(hour2Str);
		textSb.append(DateFormat.format("mm:ss", mMax));
//		String textProgress = hour1Str.toString() + DateFormat.format("mm:ss", position) + "/" + hour2Str.toString()
//				+ DateFormat.format("mm:ss", mMax);
		mCurrentTimeView.setText(textSb.toString());
	}

	private void fixVideoSize() {
		int videoWidth = mMediaPlayer.getVideoWidth();
		int videoHeight = mMediaPlayer.getVideoHeight();

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		float widthRatio = dm.widthPixels / (float) videoWidth;
		float heightRatio = dm.heightPixels / (float) videoHeight;
		// int seekBarWidth = seekbar.getWidth();
		if (widthRatio > heightRatio) {
			videoHeight = (int) (videoHeight * heightRatio);
			videoWidth = (int) (videoWidth * heightRatio);
		} else {
			videoHeight = (int) (videoHeight * widthRatio);
			videoWidth = (int) (videoWidth * widthRatio);
		}

		mSurfaceView.getHolder().setFixedSize(videoWidth, videoHeight);

		android.view.ViewGroup.LayoutParams lp = mDefaultImage.getLayoutParams();
		lp.width = videoWidth;
		lp.height = videoHeight;
		mDefaultImage.setLayoutParams(lp);
		mDefaultImage.invalidate();
		mBitmap = ThumbnailUtils.createVideoThumbnail(mFilePath, Thumbnails.FULL_SCREEN_KIND);
		;
		if (mBitmap != null) {
			mDefaultImage.setImageBitmap(mBitmap);
		}
	}

	class UpDateSeekBar implements Runnable {

		@Override
		public void run() {
			mHandler.sendMessage(Message.obtain());
			if (isPlay) {
				mHandler.postDelayed(update, 25);
			}
		}
	}

	class PlaySurFaceView implements Callback {

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			if (mDataId != null) {
//				String position = SharedPreferencesUtil.getFromFile(PlayerFullScreenActivity.this, mDataId);
//				if (position != null && position != "") {
//					postSize = Integer.valueOf(SharedPreferencesUtil.getFromFile(PlayerFullScreenActivity.this, mDataId));
//				} else {
//					postSize = 0;
//				}
			}
			if (postSize > 0 && mFilePath != null) {
				System.out.println("surfaceCreated  surfaceCreated  surfaceCreated  ");
				new PlayMovie(postSize).start();
				isPlay = true;
				long sMax = seekbar.getMax();
				long mMax = mMediaPlayer.getDuration();
				seekbar.setProgress((int) (postSize * sMax / mMax));
				postSize = 0;
			} else {
				new PlayMovie(0).start();
			}
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
				isPlay = false;
			}
//			postSize = mMediaPlayer.getCurrentPosition();
//			if (mDataId != null) {
//				SharedPreferencesUtil.saveToFile(PlayerFullScreenActivity.this, mDataId, String.valueOf(postSize));
//			}
		}
	}

	class PlayMovie extends Thread {

		int post = 0;

		public PlayMovie(int post) {
			this.post = post;

		}

		@Override
		public void run() {
			Message message = Message.obtain();
			try {
				Log.i(TAG, "runrun  " + mFilePath);
				mMediaPlayer.reset();
				File file = new File(mFilePath);
				FileInputStream fis = new FileInputStream(file);
				mMediaPlayer.setDataSource(fis.getFD());
				mMediaPlayer.setDisplay(mSurfaceView.getHolder());
				mMediaPlayer.setOnPreparedListener(new Ok(post));
				mMediaPlayer.prepare();

				mControlHandler.sendEmptyMessage(PLAYER_START);
			} catch (Exception e) {
				message.what = 2;
				Log.e(TAG, e.toString());
			}

			super.run();
		}
	}

	class Ok implements OnPreparedListener {
		int postSize;

		public Ok(int postSize) {
			this.postSize = postSize;
		}

		@Override
		public void onPrepared(MediaPlayer mp) {
			Log.i(TAG, "play");
			Log.i(TAG, "post " + postSize);
			mCenterPlayBtn.setEnabled(true);
			if (mMediaPlayer != null) {
				setProgressText(0, mMediaPlayer.getDuration());
				// fit size
				fixVideoSize();
			} else {
				return;
			}
			if (postSize > 0) {
				Log.i(TAG, "seekTo ");
				mMediaPlayer.seekTo(postSize);
			}
			new Thread(update).start();
		}
	}

	class RewindBtnListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			int tag = (Integer) v.getTag();
			int position = mMediaPlayer.getCurrentPosition();
			switch (tag) {
			case REWIND_DOWN_TAG:
				position = position - REWIND_SECOND;
				if (position <= 0) {
					position = 0;
				}
				break;
			case REWIND_UP_TAG:
				position = position + REWIND_SECOND;
				if (position >= mMediaPlayer.getDuration()) {
					position = mMediaPlayer.getDuration();
				}
				break;
			}

			postSize = position;
			long sMax = seekbar.getMax();
			long mMax = mMediaPlayer.getDuration();
			seekbar.setProgress((int) (postSize * sMax / mMax));
			setProgressText(position, mMax);

			mMediaPlayer.seekTo(position);
			mControlHandler.sendEmptyMessage(VIEW_CLICK);
		}
	}

	class PlayListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			int tag = (Integer) v.getTag();
			switch (tag) {
			case CENTER_PLAY_TAG:
				// if (mediaPlayer.isPlaying()) {
				// mPlayBtn.setBackgroundResource(R.drawable.movie_play_bt);
				// mediaPlayer.pause();
				// postSize = mediaPlayer.getCurrentPosition();
				// mControlHandler.sendEmptyMessage(PAUSE_CLICK);
				// } else {
				if (isPlay == false) {
					isPlay = true;
					new Thread(update).start();
				}
				mDefaultImage.setVisibility(View.GONE);
				mMediaPlayer.start();
				// mPlayBtn.setBackgroundResource(R.drawable.movie_stop_bt);
				mCenterPlayBtn.setVisibility(View.GONE);
				mSmallPlayBtn.setBackgroundResource(R.drawable.btn_small_pause);
				mControlHandler.sendEmptyMessage(PLAY_CLICK);
				// }
//				if (getApplication() instanceof EAApplication) {
//					((EAApplication) getApplication()).setStartScreen(false);
//				}
				break;
			case SURFACE_VIEW_TAG:
				if (mMediaPlayer.isPlaying()) {
					pauseMediaPlayer();
				} else {
					if (isPlay == false) {
						isPlay = true;
						new Thread(update).start();
					}
					mDefaultImage.setVisibility(View.GONE);
					mMediaPlayer.start();
					mCenterPlayBtn.setVisibility(View.GONE);
					mSmallPlayBtn.setBackgroundResource(R.drawable.btn_small_pause);
					mControlHandler.sendEmptyMessage(PLAY_CLICK);
				}
				break;
			case SMALL_PLAY_TAG:
				if (mMediaPlayer.isPlaying()) {
					pauseMediaPlayer();
				} else {
					if (isPlay == false) {
						isPlay = true;
						new Thread(update).start();
					}
					mDefaultImage.setVisibility(View.GONE);
					mMediaPlayer.start();
					mCenterPlayBtn.setVisibility(View.GONE);
					mSmallPlayBtn.setBackgroundResource(R.drawable.btn_small_pause);
					mControlHandler.sendEmptyMessage(PLAY_CLICK);
					
				}
				break;
			}
		}
	}

	public void pauseMediaPlayer() {
		mMediaPlayer.pause();
		postSize = mMediaPlayer.getCurrentPosition();
		mControlHandler.sendEmptyMessage(PAUSE_CLICK);
	}
	
//	@Override
//	public boolean dispatchKeyEvent(KeyEvent event) {
//		mControlBar.setVisibility(View.VISIBLE);
//		findViewById(R.id.video_back).requestFocus();
//		return super.dispatchKeyEvent(event);
//	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		View focusedView = getCurrentFocus();
		mControlBar.clearAnimation();
		mControlBar.setVisibility(View.VISIBLE);
		findViewById(R.id.small_play).requestFocus();		
		if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
			switch(focusedView.getId()){
			case (R.id.rewind_down):
				findViewById(R.id.video_back).requestFocus();
				return true;
			case (R.id.rewind_up):
				findViewById(R.id.rewind_down).requestFocus();
				return true;
			case (R.id.small_play):
				findViewById(R.id.rewind_up).requestFocus();
				return true;			
			}
		}	
		else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
				switch(focusedView.getId()){
				case (R.id.video_back):
					findViewById(R.id.rewind_down).requestFocus();
					return true;
				case (R.id.rewind_down):
					findViewById(R.id.rewind_up).requestFocus();
					return true;
				case (R.id.rewind_up):
					findViewById(R.id.small_play).requestFocus();
					return true;				
				}		
		}
		else if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
			switch(focusedView.getId()){
			case (R.id.rewind_up):
				findViewById(R.id.rewind_up).performClick();
				findViewById(R.id.rewind_up).requestFocus();
				return true;
			case (R.id.rewind_down):
				findViewById(R.id.rewind_down).performClick();
				findViewById(R.id.rewind_down).requestFocus();
				return true;
			case (R.id.small_play):
				findViewById(R.id.small_play).performClick();
				findViewById(R.id.small_play).requestFocus();
				return true;
			case (R.id.video_back):
				findViewById(R.id.video_back).performClick();
				return true;
			
			}
					
	}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return true;
		
	}
}
