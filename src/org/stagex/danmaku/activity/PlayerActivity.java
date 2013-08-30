package org.stagex.danmaku.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.keke.player.R;
import org.stagex.danmaku.util.SystemUtility;

import com.nmbb.oplayer.scanner.DbHelper;
import com.nmbb.oplayer.scanner.POChannelList;
import com.nmbb.oplayer.scanner.POUserDefChannel;
import com.togic.mediacenter.player.AbsMediaPlayer;
import com.togic.mediacenter.player.DefMediaPlayer;
import com.togic.mediacenter.player.VlcMediaPlayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class PlayerActivity extends Activity implements
		AbsMediaPlayer.OnBufferingUpdateListener,
		AbsMediaPlayer.OnCompletionListener, AbsMediaPlayer.OnErrorListener,
		AbsMediaPlayer.OnInfoListener, AbsMediaPlayer.OnPreparedListener,
		AbsMediaPlayer.OnProgressUpdateListener,
		AbsMediaPlayer.OnVideoSizeChangedListener, OnClickListener,
		OnSeekBarChangeListener {

	static final String LOGTAG = "PlayerActivity";

	private static final int SURFACE_NONE = 0;
	private static final int SURFACE_FILL = 1;
	private static final int SURFACE_ORIG = 2;
	private static final int SURFACE_4_3 = 3;
	private static final int SURFACE_16_9 = 4;
	private static final int SURFACE_16_10 = 5;
	private static final int SURFACE_MAX = 6;

	private static final int MEDIA_PLAYER_BUFFERING_UPDATE = 0x4001;
	private static final int MEDIA_PLAYER_COMPLETION = 0x4002;
	private static final int MEDIA_PLAYER_ERROR = 0x4003;
	private static final int MEDIA_PLAYER_INFO = 0x4004;
	private static final int MEDIA_PLAYER_PREPARED = 0x4005;
	private static final int MEDIA_PLAYER_PROGRESS_UPDATE = 0x4006;
	private static final int MEDIA_PLAYER_VIDEO_SIZE_CHANGED = 0x4007;

	/* the media player */
	private AbsMediaPlayer mMediaPlayer = null;

	/* */
	private ArrayList<String> mPlayListArray = null;
	private int mPlayListSelected = -1;

	/* GUI evnet handler */
	private Handler mEventHandler;

	/* player misc */
	private ProgressBar mProgressBarPreparing;
//	private TextView mLoadingTxt;
	private TextView mPercentTxt;

	/* player controls */
	private TextView mTitle;
	private TextView mSource;
	private TextView mSysTime;
	private TextView mBattery;
	private TextView mTextViewTime;
	private TextView mCodecMode;
	private SeekBar mSeekBarProgress;
	private TextView mTextViewLength;
	private TextView mSelfdef;
	// 点击阴影部分也不会导致隐藏
	private LinearLayout player_overlay_header;
	private LinearLayout interface_overlay;
	private LinearLayout seekbar_overlay;
	// end
	private ImageButton mImageButtonStar;
	private ImageButton mImageButtonToggleMessage;
	private ImageButton mImageButtonSwitchAudio;
	private ImageButton mImageButtonSwitchSubtitle;
	private ImageButton mImageButtonPrevious;
	private ImageButton mImageButtonTogglePlay;
	private ImageButton mImageButtonNext;
	private ImageButton mImageButtonSwitchAspectRatio;

	private RelativeLayout mLinearLayoutControlBar;

	/* player video */
	private SurfaceView mSurfaceViewDef;
	private SurfaceHolder mSurfaceHolderDef;
	private SurfaceView mSurfaceViewVlc;
	private SurfaceHolder mSurfaceHolderVlc;

	/* misc */
	private boolean mMediaPlayerLoaded = false;
	private boolean mMediaPlayerStarted = false;

	/* misc */
	private int mTime = -1;
	private int mLength = -1;
	private boolean mCanSeek = true;
	private int mAspectRatio = 1; // 直接全屏

	/* title name */
	private String mTitleName;
	private String mSourceName;

	// private int mAudioTrackIndex = 0;
	// private int mAudioTrackCount = 0;
	// private int mSubtitleTrackIndex = 0;
	// private int mSubtitleTrackCount = 0;

	/**
	 * 增加手势控制
	 * 
	 * @{
	 */
	private View mVolumeBrightnessLayout;
	private ImageView mOperationBg;
	private ImageView mOperationPercent;
	private AudioManager mAudioManager;
	/** 最大声音 */
	private int mMaxVolume;
	/** 当前声音 */
	private int mVolume = -1;
	/** 当前亮度 */
	private float mBrightness = -1f;
	/** 当前缩放模式 */
	// private int mLayout = VideoView.VIDEO_LAYOUT_ZOOM;
	/** 响应函数是否生效的标志位 */
	private boolean mDoHandleAll = false;
	private boolean mDoHandleClick = false;
	private boolean mDoHandleSeek = false;

	private static final int MSG_CTL_ALL = 0;
	private static final int MSG_CTL_CLICK = 1;
	private static final int MSG_CTL_SEEKBAR = 2;

	private GestureDetector mGestureDetector;
	/* @} */

	/* 记录硬解码与软解码的状态 */
	private SharedPreferences sharedPreferences;
	private Editor editor;
	private boolean isHardDec;
	/* 记录直播电视还是本地媒体状态 */
	private boolean isLiveMedia;

	/* 频道收藏的数据库 */
	private DbHelper<POChannelList> mDbHelper;
	private DbHelper<POUserDefChannel> mSelfDbHelper;
	private Boolean channelStar = false;
	List<POChannelList> channelList = null;
	private int fav_num = 0;

	/* 是否是自定义频道 */
	private Boolean isSelfTV = false;
	
	/**
	 * 判断使用的解码接口
	 * 
	 * @param obj
	 * @return
	 */
	private static boolean isDefMediaPlayer(Object obj) {
		return obj.getClass().getName()
				.compareTo(DefMediaPlayer.class.getName()) == 0;
	}

	private static boolean isVlcMediaPlayer(Object obj) {
		return obj.getClass().getName()
				.compareTo(VlcMediaPlayer.class.getName()) == 0;
	}

	/**
	 * 播放过程中的事件响应的核心处理方法
	 */
	protected void initializeEvents() {
		mEventHandler = new Handler() {
			public void handleMessage(Message msg) {
//				Log.d(LOGTAG, "===> get message [" + msg.what + "]");
				switch (msg.what) {
				case MEDIA_PLAYER_BUFFERING_UPDATE: {
					// FIXBUG bug#0023 这里的判断标志会引起第一次启动时没有
					// 缓冲百分比的提示
//					if (mMediaPlayerLoaded) {
						// Log.d(LOGTAG, "===>load   " + msg.arg1 + "%");
						mPercentTxt.setText("正在缓冲===> "
								+ String.valueOf(msg.arg1) + "%");

						mPercentTxt.setVisibility(msg.arg1 < 100 ? View.VISIBLE
								: View.GONE);

						mProgressBarPreparing
								.setVisibility(msg.arg1 < 100 ? View.VISIBLE
										: View.GONE);

//						 mLoadingTxt
//						 .setVisibility(msg.arg1 < 100 ? View.VISIBLE
//						 : View.GONE);
					}
					break;
//				}
				case MEDIA_PLAYER_COMPLETION: {
					/* TODO 播放结束后，如何处理 */
					// 使用通知窗口
					// Toast.makeText(getApplicationContext(),"播放结束，请按返回键",
					// Toast.LENGTH_LONG).show();
					// 使用警告窗口 @{
					// FIXME 判断当前是否是直播电视状态，如果是，则此时的结束播放
					// 是由于网络链接中断引起的，立即重新启动@{
					isLiveMedia = sharedPreferences.getBoolean("isLiveMedia",
							true);
					if (isLiveMedia) {
						// 缓冲环显示
						mProgressBarPreparing.setVisibility(View.VISIBLE);
						// 缓冲提示语
//						mLoadingTxt.setVisibility(View.VISIBLE);
						Log.d(LOGTAG,
								"reconnect the Media Server in LiveTV mode");
						if (sharedPreferences.getBoolean("isHardDec", false)) {
							// 硬解码重新连接媒体服务器
							destroyMediaPlayer(true);
							selectMediaPlayer(
									mPlayListArray.get(mPlayListSelected),
									false);
							createMediaPlayer(true,
									mPlayListArray.get(mPlayListSelected),
									mSurfaceHolderDef);
							mMediaPlayer.setDisplay(mSurfaceHolderDef);
						} else {
							// 软解码重新连接媒体服务器
							destroyMediaPlayer(false);
							selectMediaPlayer(
									mPlayListArray.get(mPlayListSelected), true);
							createMediaPlayer(false,
									mPlayListArray.get(mPlayListSelected),
									mSurfaceHolderVlc);
							mMediaPlayer.setDisplay(mSurfaceHolderVlc);
						}
					} else
						// @}
						new AlertDialog.Builder(PlayerActivity.this)
								.setIcon(R.drawable.ic_about)
								.setTitle("播放结束")
								.setMessage("该视频已经播放结束.")
								.setNegativeButton("知道了",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												// do nothing - it will close on
												// its
												// own
												// 关闭当前的PlayerActivity，退回listview的界面
												finish();
											}
										}).show();
					// @}
					break;
				}
				/* FIXME 这里的处理有待进一步细化 */
				case MEDIA_PLAYER_ERROR: {
					Log.e(LOGTAG, "MEDIA_PLAYER_ERROR");
					/* fall back to VlcMediaPlayer if possible */
					if (isDefMediaPlayer(msg.obj)) {
						// Log.i(LOGTAG,
						// "DefMediaPlayer selectMediaPlayer（VLC）");
						// selectMediaPlayer(
						// mPlayListArray.get(mPlayListSelected), true);
						// break;
						mProgressBarPreparing.setVisibility(View.GONE);
						// FIXME bug#0023
						mPercentTxt.setVisibility(View.GONE);
//						mLoadingTxt.setVisibility(View.GONE);
						/* TODO 用在硬解解码模式，判断不支持的源 */
						new AlertDialog.Builder(PlayerActivity.this)
								.setIcon(R.drawable.ic_dialog_alert)
								.setTitle("播放失败【硬解码】")
								.setMessage(
										"很遗憾，该视频无法播放\n请尝试该节目【其他源】\n或切换至【软解码】模式再次尝试\n现在切换解码模式吗？")
								.setPositiveButton("是",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												// do nothing - it will close on
												// its own
												Intent intent = new Intent();
												// 跳转至设置界面
												intent.setClass(
														PlayerActivity.this,
														SetupActivity.class);
												startActivity(intent);
												finish();
											}
										})
								.setNegativeButton("否",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												// do nothing - it will close on
												// its own
												// 关闭当前的PlayerActivity，退回listview的界面
												finish();
											}
										}).show();
						// @}
						// Log.i(LOGTAG, "get out of alert");
						break;
					} else if (isVlcMediaPlayer(msg.obj)) {
						// Log.i(LOGTAG, "VlcMediaPlayer");
						/* destroy media player */
						mSurfaceViewVlc.setVisibility(View.GONE);
						// Log.i(LOGTAG, "VlcMediaPlayer update UI");
						mProgressBarPreparing.setVisibility(View.GONE);
						// FIXME bug#0023
						mPercentTxt.setVisibility(View.GONE);
//						mLoadingTxt.setVisibility(View.GONE);
						// 弹出播放失败的窗口@{
						new AlertDialog.Builder(PlayerActivity.this)
								.setIcon(R.drawable.ic_dialog_alert)
								.setTitle("播放失败【软解码】")
								.setMessage(
										"很遗憾，该视频无法播放\n请切换该频道【其他地址源】\n或观看【其他频道】")
								.setNegativeButton("知道了",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												// do nothing - it will close on
												// its own
												// 关闭当前的PlayerActivity，退回listview的界面
												finish();
											}
										}).show();
						// @}
						// Log.i(LOGTAG, "get out of alert");
						break;
					}
				}
				case MEDIA_PLAYER_INFO: {
					if (msg.arg1 == MediaPlayer.MEDIA_INFO_NOT_SEEKABLE) {
						mCanSeek = false;
					}
					break;
				}
				case MEDIA_PLAYER_PREPARED: {
					Log.d(LOGTAG, "===> MEDIA_PLAYER_PREPARED");
					// FIXME bug#0023 对于rtmp的视频，不会有该message
					// 因此是个bug，暂时将mMediaPlayerLoaded = true在MEDIA_PLAYER_PROGRESS_UPDATE
					// 中也进行置位操作
					if (isDefMediaPlayer(msg.obj) || isVlcMediaPlayer(msg.obj)) {
						/* update status */
						mMediaPlayerLoaded = true;
					}
					/* update UI */
					if (mMediaPlayerLoaded) {
						mProgressBarPreparing.setVisibility(View.GONE);
//						mLoadingTxt.setVisibility(View.GONE);
						// FIXME bug#0023
						mPercentTxt.setVisibility(View.GONE);
					}
					startMediaPlayer();
					break;
				}
				case MEDIA_PLAYER_PROGRESS_UPDATE: {
					// FIXME bug#0023
					mMediaPlayerLoaded = true;
					//
					if (mMediaPlayer != null) {
						int length = msg.arg2;
						if (length >= 0) {
							mLength = length;
							mTextViewLength.setText(SystemUtility
									.getTimeString(mLength));
							mSeekBarProgress.setMax(mLength);
						}
						int time = msg.arg1;
						if (time >= 0) {
							mTime = time;
							mTextViewTime.setText(SystemUtility
									.getTimeString(mTime));
							mSeekBarProgress.setProgress(mTime);
						}
					}
					break;
				}
				case MEDIA_PLAYER_VIDEO_SIZE_CHANGED: {
					AbsMediaPlayer player = (AbsMediaPlayer) msg.obj;
					SurfaceView surface = isDefMediaPlayer(player) ? mSurfaceViewDef
							: mSurfaceViewVlc;
					int ar = mAspectRatio;
					// 根据设置，改变播放界面大小和比例
					changeSurfaceSize(player, surface, ar);
					break;
				}
				default:
					break;
				}
			}
		};
	}

	/**
	 * 播放控件初始化：创建surface、获取各子控件的id
	 */
	protected void initializeControls() {
		/* SufaceView used by VLC is a normal surface */
		mSurfaceViewVlc = (SurfaceView) findViewById(R.id.player_surface_vlc);
		mSurfaceHolderVlc = mSurfaceViewVlc.getHolder();
		mSurfaceHolderVlc.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
		mSurfaceHolderVlc.addCallback(new SurfaceHolder.Callback() {
			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				createMediaPlayer(false, mPlayListArray.get(mPlayListSelected),
						mSurfaceHolderVlc);
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
				mMediaPlayer.setDisplay(holder);
			}

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				destroyMediaPlayer(false);
			}

		});
		/* SurfaceView used by MediaPlayer is a PUSH_BUFFERS surface */
		mSurfaceViewDef = (SurfaceView) findViewById(R.id.player_surface_def);
		mSurfaceHolderDef = mSurfaceViewDef.getHolder();
		mSurfaceHolderDef.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mSurfaceHolderDef.addCallback(new SurfaceHolder.Callback() {
			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				createMediaPlayer(true, mPlayListArray.get(mPlayListSelected),
						mSurfaceHolderDef);
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
				mMediaPlayer.setDisplay(holder);
			}

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				destroyMediaPlayer(true);
			}

		});

		// TODO 2013-08-01
		mSelfdef = (TextView) findViewById(R.id.selfdef_tv);
		
		// overlay header
		mTitle = (TextView) findViewById(R.id.player_overlay_title);
		mSource = (TextView) findViewById(R.id.player_overlay_name);
		mSysTime = (TextView) findViewById(R.id.player_overlay_systime);
		mBattery = (TextView) findViewById(R.id.player_overlay_battery);
		mCodecMode = (TextView) findViewById(R.id.player_codec_mode);

		// seekbar和两端的岂止时间
		mTextViewTime = (TextView) findViewById(R.id.player_text_position);
		mSeekBarProgress = (SeekBar) findViewById(R.id.player_seekbar_progress);
		mSeekBarProgress.setOnSeekBarChangeListener(this);
		mTextViewLength = (TextView) findViewById(R.id.player_text_length);

		// 点击阴影部分也不会隐藏
		player_overlay_header = (LinearLayout) findViewById(R.id.player_overlay_header);
		player_overlay_header.setOnClickListener(this);
		interface_overlay = (LinearLayout) findViewById(R.id.interface_overlay);
		interface_overlay.setOnClickListener(this);
		seekbar_overlay = (LinearLayout) findViewById(R.id.seekbar_overlay);
		seekbar_overlay.setOnClickListener(this);

		// 播放控件
		mImageButtonStar = (ImageButton) findViewById(R.id.player_button_star);
		mImageButtonStar.setOnClickListener(this);
		mImageButtonToggleMessage = (ImageButton) findViewById(R.id.player_button_toggle_message);
		mImageButtonToggleMessage.setOnClickListener(this);
		mImageButtonSwitchAudio = (ImageButton) findViewById(R.id.player_button_switch_audio);
		mImageButtonSwitchAudio.setOnClickListener(this);
		mImageButtonSwitchSubtitle = (ImageButton) findViewById(R.id.player_button_switch_subtitle);
		mImageButtonSwitchSubtitle.setOnClickListener(this);
		mImageButtonPrevious = (ImageButton) findViewById(R.id.player_button_previous);
		mImageButtonPrevious.setOnClickListener(this);
		mImageButtonTogglePlay = (ImageButton) findViewById(R.id.player_button_toggle_play);
		mImageButtonTogglePlay.setOnClickListener(this);
		mImageButtonNext = (ImageButton) findViewById(R.id.player_button_next);
		mImageButtonNext.setOnClickListener(this);
		mImageButtonSwitchAspectRatio = (ImageButton) findViewById(R.id.player_button_switch_aspect_ratio);
		mImageButtonSwitchAspectRatio.setOnClickListener(this);

		mLinearLayoutControlBar = (RelativeLayout) findViewById(R.id.player_control_bar);

		// 缓冲进度圈
		mProgressBarPreparing = (ProgressBar) findViewById(R.id.player_prepairing);
		// 缓冲提示语言
//		mLoadingTxt = (TextView) findViewById(R.id.player_loading);
		// 缓冲比例
		mPercentTxt = (TextView) findViewById(R.id.buffer_percent);

		// 初始化手势
		initGesture();

		// 初始化电量监测
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		// filter.addAction(VLCApplication.SLEEP_INTENT);
		registerReceiver(mReceiver, filter);
	}

	protected void initializeData() {
		Intent intent = getIntent();
		String action = intent.getAction();
		if (action != null && action.equals(Intent.ACTION_VIEW)) {
			String one = intent.getDataString();
			mPlayListSelected = 0;
			mPlayListArray = new ArrayList<String>();
			mPlayListArray.add(one);
		} else {
			mPlayListSelected = intent.getIntExtra("selected", 0);
			mPlayListArray = intent.getStringArrayListExtra("playlist");
			channelStar = intent.getBooleanExtra("channelStar", false);
			// Log.d(LOGTAG, "===>>>" + mTitleName);
			mTitleName = intent.getStringExtra("title");
			mSourceName = intent.getStringExtra("source");
			isSelfTV = intent.getBooleanExtra("isSelfTV", false);
		}
		if (mPlayListArray == null || mPlayListArray.size() == 0) {
			Log.e(LOGTAG, "initializeData(): empty");
			finish();
			return;
		}
	}

	/**
	 * 重新设置播放器，控制各控件界面显示与否
	 */
	protected void resetMediaPlayer() {
		int resource = -1;
		/* initial status */
		mMediaPlayerLoaded = false;
		mTime = -1;
		mLength = -1;
		mCanSeek = true;
		mAspectRatio = 1; // 直接全屏
		/* */
		mImageButtonToggleMessage.setVisibility(View.GONE);
		mImageButtonSwitchAudio.setVisibility(View.GONE);
		mImageButtonSwitchSubtitle.setVisibility(View.GONE);
		mImageButtonPrevious
				.setVisibility((mPlayListArray.size() == 1) ? View.GONE
						: View.VISIBLE);

		// 判断是否以收藏
		if (channelStar) {
			resource = SystemUtility.getDrawableId("ic_fav_pressed");
			mImageButtonStar.setBackgroundResource(resource);
		} else {
			resource = SystemUtility.getDrawableId("ic_fav");
			mImageButtonStar.setBackgroundResource(resource);
		}
		
		// TODO 2013-08-01 自定义频道暂时不支持在播放界面收藏
		if (isSelfTV) {
			mImageButtonStar.setVisibility(View.GONE);
			mSelfdef.setVisibility(View.VISIBLE);
		}

		mImageButtonTogglePlay.setVisibility(View.VISIBLE);
		resource = SystemUtility.getDrawableId("btn_play_1");
		mImageButtonTogglePlay.setBackgroundResource(resource);
		mImageButtonNext.setVisibility((mPlayListArray.size() == 1) ? View.GONE
				: View.VISIBLE);
		mImageButtonSwitchAspectRatio.setVisibility(View.VISIBLE);
		resource = SystemUtility.getDrawableId("btn_aspect_ratio_0");
		mImageButtonSwitchAspectRatio.setBackgroundResource(resource);
		/* */
		mLinearLayoutControlBar.setVisibility(View.GONE);
	}

	/**
	 * TODO 选择播放器：软解（VLC）或者硬解（MP），后续可以通过设置选项让用户来选择
	 * 
	 * @param uri
	 * @param forceVlc
	 */
	protected void selectMediaPlayer(String uri, boolean forceVlc) {
		/* TODO: do this through configuration */
		boolean useDefault = true;
		// int indexOfDot = uri.lastIndexOf('.');
		// if (indexOfDot != -1) {
		// String extension = uri.substring(indexOfDot).toLowerCase();
		// /* used for mms network radio */
		// boolean mms_radio_flag = uri.contains("mms://");
		// boolean http_live_flag = uri.contains("http://");
		// if (extension.compareTo(".flv") == 0
		// || extension.compareTo(".hlv") == 0
		// || extension.compareTo(".m3u8") == 0
		// || extension.compareTo(".mkv") == 0
		// || extension.compareTo(".rm") == 0
		// || extension.compareTo(".rmvb") == 0
		// || extension.compareTo(".ts") == 0
		// || mms_radio_flag
		// || http_live_flag) {
		// useDefault = false;
		// }
		// }

		if (forceVlc) {
			useDefault = false;
		}
		mSurfaceViewDef.setVisibility(useDefault ? View.VISIBLE : View.GONE);
		mSurfaceViewVlc.setVisibility(useDefault ? View.GONE : View.VISIBLE);
	}

	/**
	 * 创建MP
	 * 
	 * @param useDefault
	 * @param uri
	 * @param holder
	 */
	protected void createMediaPlayer(boolean useDefault, String uri,
			SurfaceHolder holder) {
		Log.d(LOGTAG, "createMediaPlayer() " + uri);
		/* */
		resetMediaPlayer();
		/* */
		mMediaPlayer = AbsMediaPlayer.getMediaPlayer(useDefault);
		mMediaPlayer.setOnBufferingUpdateListener(this);
		mMediaPlayer.setOnCompletionListener(this);
		mMediaPlayer.setOnErrorListener(this);
		mMediaPlayer.setOnInfoListener(this);
		mMediaPlayer.setOnPreparedListener(this);
		mMediaPlayer.setOnProgressUpdateListener(this);
		mMediaPlayer.setOnVideoSizeChangedListener(this);
		mMediaPlayer.reset();
		mMediaPlayer.setDisplay(holder);
		if (mMediaPlayer.setDataSource(uri) == false) {
			/* 隐藏缓冲圈 */
			mProgressBarPreparing.setVisibility(View.GONE);
			// FIXME bug#0023
			mPercentTxt.setVisibility(View.GONE);
//			mLoadingTxt.setVisibility(View.GONE);
			/* TODO 用在硬解解码模式，判断不支持的源 */
			new AlertDialog.Builder(PlayerActivity.this)
					.setIcon(R.drawable.ic_dialog_alert)
					.setTitle("播放失败【硬解码】")
					.setMessage("很遗憾，您的硬件解码器无法播放该视频\n请切换至【软解码】再次尝试\n现在切换解码模式吗？")
					.setPositiveButton("是",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// do nothing - it will close on
									// its own
									Intent intent = new Intent();
									// 跳转至设置界面
									intent.setClass(PlayerActivity.this,
											SetupActivity.class);
									startActivity(intent);
									finish();
								}
							})
					.setNegativeButton("否",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// do nothing - it will close on its own
									// 关闭当前的PlayerActivity，退回listview的界面
									finish();
								}
							}).show();
		}
		mMediaPlayer.prepareAsync();
	}

	/**
	 * 销毁MP
	 * 
	 * @param isDefault
	 */
	protected void destroyMediaPlayer(boolean isDefault) {
		// FIXME 2013-07-02
		if (mMediaPlayer != null) {
			boolean testDefault = isDefMediaPlayer(mMediaPlayer);
			// add by juguofeng 2013-06-23
			mMediaPlayerStarted = false;
			// end add
			if (isDefault == testDefault) {
				mMediaPlayer.setDisplay(null);
				mMediaPlayer.release();
				mMediaPlayer = null;
			}
		}
	}

	/**
	 * 启动播放器
	 */
	protected void startMediaPlayer() {
		// FIXME bug#0023 rtmp的视频可能不走这里，但是如何开始播放的呢？
		// Log.i(LOGTAG, "startMediaPlayer() ");
		if (mMediaPlayerStarted || !mMediaPlayerLoaded) {
			// Log.i(LOGTAG,
			// "(mMediaPlayerStarted || !mMediaPlayerLoaded) return");
			return;
		}
		if (mMediaPlayer != null) {
//			 Log.i(LOGTAG, "===> mMediaPlayer.start()");
			mMediaPlayer.start();
			mMediaPlayerStarted = true;
		}
	}

	/**
	 * TODO 处理surface的界面比例
	 * 
	 * @param player
	 * @param surface
	 * @param ar
	 */
	protected void changeSurfaceSize(AbsMediaPlayer player,
			SurfaceView surface, int ar) {
		int videoWidth = player.getVideoWidth();
		int videoHeight = player.getVideoHeight();
		if (videoWidth <= 0 || videoHeight <= 0) {
			return;
		}
		SurfaceHolder holder = surface.getHolder();
		holder.setFixedSize(videoWidth, videoHeight);
		int displayWidth = getWindowManager().getDefaultDisplay().getWidth();
		int displayHeight = getWindowManager().getDefaultDisplay().getHeight();
		int targetWidth = -1;
		int targetHeight = -1;
		switch (ar) {
		case SURFACE_NONE: {
			targetWidth = videoWidth;
			targetHeight = videoHeight;
			break;
		}
		case SURFACE_FILL: {
			break;
		}
		case SURFACE_ORIG: {
			displayWidth = videoWidth;
			displayHeight = videoHeight;
			break;
		}
		case SURFACE_4_3: {
			targetWidth = 4;
			targetHeight = 3;
			break;
		}
		case SURFACE_16_9: {
			targetWidth = 16;
			targetHeight = 9;
			break;
		}
		case SURFACE_16_10: {
			targetWidth = 16;
			targetHeight = 10;
			break;
		}
		default:
			break;
		}
		if (targetWidth > 0 && targetHeight > 0) {
			double ard = (double) displayWidth / (double) displayHeight;
			double art = (double) targetWidth / (double) targetHeight;
			if (ard > art) {
				displayWidth = displayHeight * targetWidth / targetHeight;
			} else {
				displayHeight = displayWidth * targetHeight / targetWidth;
			}
		}
		LayoutParams lp = surface.getLayoutParams();
		lp.width = displayWidth;
		lp.height = displayHeight;
		surface.setLayoutParams(lp);
		surface.invalidate();
	}

	/**
	 * 入口方法
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 播放事件初始化
		initializeEvents();
		// 加载布局
		setContentView(R.layout.player);
		// 播放控件初始化
		initializeControls();
		// 缓冲环显示
		mProgressBarPreparing.setVisibility(View.VISIBLE);
		// 缓冲提示语
//		mLoadingTxt.setVisibility(View.VISIBLE);
		// 数据初始化
		initializeData();
		String uri = mPlayListArray.get(mPlayListSelected);

		/* 频道收藏的数据库 */
		mDbHelper = new DbHelper<POChannelList>();
		mSelfDbHelper  = new DbHelper<POUserDefChannel>();
		
		// 选择播放器
		/* 判断解码器状态 */
		sharedPreferences = getSharedPreferences("keke_player", MODE_PRIVATE);
		editor = sharedPreferences.edit();
		isHardDec = sharedPreferences.getBoolean("isHardDec", false);
		if (isHardDec) {
			// 选择系统硬解码
			selectMediaPlayer(uri, false);
			// 应用运行时，保持屏幕高亮，不锁屏
			getWindow()
					.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} else {
			// 强制选择VLC播放器
			selectMediaPlayer(uri, true);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// 注销电量检测事件
		unregisterReceiver(mReceiver);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mMediaPlayer != null) {
			mMediaPlayer.pause();
		}
	}

	/**
	 * 对上述控件的子控件的操作响应
	 */
	@Override
	public void onClick(View v) {
		// FIXME 由于rtmp的视频没有 MEDIA_PLAYER_PREPARED message
		// 所以点击不会出现播放控件界面
		if (!mMediaPlayerLoaded)
			return;

		// 如果有click事件，也阻止控件隐藏
		mDoHandleAll = false;
		mDoHandleClick = true;
		mDoHandleSeek = false;
		endCTLGesture(MSG_CTL_CLICK);

		int id = v.getId();
		switch (id) {
		case R.id.player_button_star: {
			// TODO 决定是否收藏该频道
			if (isSelfTV) {
				// 用户自定义的频道
				// TODO 2013-08-01 暂时不支持自定义的频道在播放界面收藏
//				updateSelfFavDatabase(mTitleName);
			} else {
				// 官方频道
				updateFavDatabase(mTitleName);
			}
			break;
		}
		case R.id.player_button_switch_audio: {
			// TODO 暂不做处理
			break;
		}
		case R.id.player_button_switch_subtitle: {
			// TODO 暂不做处理
			break;
		}
		case R.id.player_button_previous: {
			// TODO 暂不做处理
			break;
		}
		case R.id.player_button_toggle_play: {
			boolean playing = false;
			if (mMediaPlayer != null)
				playing = mMediaPlayer.isPlaying();
			if (playing) {
				if (mMediaPlayer != null)
					mMediaPlayer.pause();
			} else {
				if (mMediaPlayer != null)
					mMediaPlayer.start();
			}
			String name = String.format("btn_play_%d", !playing ? 1 : 0);
			int resouce = SystemUtility.getDrawableId(name);
			mImageButtonTogglePlay.setBackgroundResource(resouce);
			break;
		}
		case R.id.player_button_next: {
			// TODO 暂不做处理
			break;
		}
		case R.id.player_button_switch_aspect_ratio: {
			mAspectRatio = (mAspectRatio + 1) % SURFACE_MAX;
			if (mMediaPlayer != null)
				changeSurfaceSize(mMediaPlayer,
						isDefMediaPlayer(mMediaPlayer) ? mSurfaceViewDef
								: mSurfaceViewVlc, mAspectRatio);
			String name = String.format("btn_aspect_ratio_%d", mAspectRatio);
			int resource = SystemUtility.getDrawableId(name);
			mImageButtonSwitchAspectRatio.setBackgroundResource(resource);
			break;
		}
		default:
			break;
		}
	}

	/**
	 * seekbar的响应方法
	 * 
	 * @{
	 */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		/* not used */
		// Log.v(LOGTAG, "-----Progress-----");
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		/* not used */
		// Log.v(LOGTAG, "-----start seek---------");
		mDoHandleAll = false;
		mDoHandleClick = false;
		mDoHandleSeek = true;
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if (!mMediaPlayerLoaded)
			return;
		int id = seekBar.getId();
		switch (id) {
		case R.id.player_seekbar_progress: {
			if (mCanSeek && mLength > 0) {
				int position = seekBar.getProgress();
				if (mMediaPlayer != null)
					mMediaPlayer.seekTo(position);
				// Log.v(LOGTAG, "-------seek end--------");
				/* seek结束了，可做控件隐藏的相应处理 */
				endCTLGesture(MSG_CTL_SEEKBAR);
			}
			break;
		}
		default:
			break;
		}
	}

	/** @} */

	/**
	 * 以下：接收事件，做中间处理，再调用handleMessage方法处理之
	 * 
	 * @{
	 */
	@Override
	public void onBufferingUpdate(AbsMediaPlayer mp, int percent) {
		Message msg = new Message();
		msg.obj = mp;
		msg.what = MEDIA_PLAYER_BUFFERING_UPDATE;
		msg.arg1 = percent;
		mEventHandler.sendMessage(msg);
	}

	@Override
	public void onCompletion(AbsMediaPlayer mp) {
		Message msg = new Message();
		msg.obj = mp;
		msg.what = MEDIA_PLAYER_COMPLETION;
		mEventHandler.sendMessage(msg);
	}

	@Override
	public boolean onError(AbsMediaPlayer mp, int what, int extra) {
		Message msg = new Message();
		msg.obj = mp;
		msg.what = MEDIA_PLAYER_ERROR;
		msg.arg1 = what;
		msg.arg2 = extra;
		mEventHandler.sendMessage(msg);
		return true;
	}

	@Override
	public boolean onInfo(AbsMediaPlayer mp, int what, int extra) {
		Message msg = new Message();
		msg.obj = mp;
		msg.what = MEDIA_PLAYER_INFO;
		msg.arg1 = what;
		msg.arg2 = extra;
		mEventHandler.sendMessage(msg);
		return true;
	}

	@Override
	public void onPrepared(AbsMediaPlayer mp) {
		Message msg = new Message();
		msg.obj = mp;
		msg.what = MEDIA_PLAYER_PREPARED;
		mEventHandler.sendMessage(msg);
	}

	@Override
	public void onProgressUpdate(AbsMediaPlayer mp, int time, int length) {
		Message msg = new Message();
		msg.obj = mp;
		msg.what = MEDIA_PLAYER_PROGRESS_UPDATE;
		msg.arg1 = time;
		msg.arg2 = length;
		mEventHandler.sendMessage(msg);
	}

	@Override
	public void onVideoSizeChangedListener(AbsMediaPlayer mp, int width,
			int height) {
		Message msg = new Message();
		msg.obj = mp;
		msg.what = MEDIA_PLAYER_VIDEO_SIZE_CHANGED;
		msg.arg1 = width;
		msg.arg2 = height;
		mEventHandler.sendMessage(msg);
	}

	/** @} */

	/**
	 * 初始化手势控制
	 */
	protected void initGesture() {
		mVolumeBrightnessLayout = findViewById(R.id.operation_volume_brightness);
		mOperationBg = (ImageView) findViewById(R.id.operation_bg);
		mOperationPercent = (ImageView) findViewById(R.id.operation_percent);

		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mMaxVolume = mAudioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

		mGestureDetector = new GestureDetector(this, new MyGestureListener());
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		/* 首先处理touch事件（因为废弃了onTouch事件了） */
		if (!mMediaPlayerLoaded) {
			return true;
		}

		// TODO 更新当前时间信息
		mSysTime.setText(DateFormat.format("kk:mm", System.currentTimeMillis()));
		mTitle.setText(mTitleName);
		mSource.setText(mSourceName);
		mCodecMode.setText(isHardDec ? "[硬解码]" : "[软解码]");

		// 仅在触摸按下时，响应触摸事件
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			int visibility = mLinearLayoutControlBar.getVisibility();
			// 加上判断之后可以在连续触摸的时候，到达其延时后仍可隐藏
			if (visibility != View.VISIBLE) {
				mLinearLayoutControlBar.setVisibility(View.VISIBLE);
				// 延时一段时间后隐藏
				mDoHandleAll = true;
				mDoHandleClick = false;
				mDoHandleSeek = false;
				endCTLGesture(MSG_CTL_ALL);
			} else {
				mDoHandleAll = false;
				mDoHandleClick = false;
				mDoHandleSeek = false;
				mLinearLayoutControlBar.setVisibility(View.GONE);
			}
		}

		// 处理音量和亮度调节手势事件
		if (mGestureDetector.onTouchEvent(event))
			return true;

		// 处理手势结束
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_UP:
			endALGesture(); // 结束音量和亮度调节手势
			break;
		}

		return super.onTouchEvent(event);
	}

	/** 结束音量和亮度调节手势 */
	private void endALGesture() {
		mVolume = -1;
		mBrightness = -1f;

		// 隐藏
		mDismissALHandler.removeMessages(0);
		mDismissALHandler.sendEmptyMessageDelayed(0, 500);
	}

	/** 结束控制接口触摸 */
	private void endCTLGesture(int msg) {
		// 隐藏
		mDismissCTLHandler.removeMessages(msg);
		mDismissCTLHandler.sendEmptyMessageDelayed(msg, 5000);
	}

	private class MyGestureListener extends SimpleOnGestureListener {

		/** TODO 双击（改变分辨率） */
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			// if (mLayout == VideoView.VIDEO_LAYOUT_ZOOM)
			// mLayout = VideoView.VIDEO_LAYOUT_ORIGIN;
			// else
			// mLayout++;
			// if (mVideoView != null)
			// mVideoView.setVideoLayout(mLayout, 0);
			return true;
		}

		/** 滑动 */
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			// 异常处理
			if (e1 == null || e2 == null) {
				Log.e(LOGTAG, "get MotionEvent value null");
				return true;
			}

			float mOldX = e1.getX(), mOldY = e1.getY();
			int y = (int) e2.getRawY();
			Display disp = getWindowManager().getDefaultDisplay();
			int windowWidth = disp.getWidth();
			int windowHeight = disp.getHeight();

			if (mOldX > windowWidth * 4.0 / 5)// 右边滑动
				onVolumeSlide((mOldY - y) / windowHeight);
			else if (mOldX < windowWidth / 5.0)// 左边滑动
				onBrightnessSlide((mOldY - y) / windowHeight);

			return super.onScroll(e1, e2, distanceX, distanceY);
		}
	}

	/** 定时隐藏音量和亮度图标 */
	private Handler mDismissALHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mVolumeBrightnessLayout.setVisibility(View.GONE);
		}
	};

	/** 定时隐藏播放控件 */
	private Handler mDismissCTLHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// Log.v(LOGTAG, "-----msg.what------" + msg.what);
			switch (msg.what) {
			case MSG_CTL_ALL:
				if (mDoHandleAll) {
					mLinearLayoutControlBar.setVisibility(View.GONE);
					mDoHandleAll = false;
				}
				break;
			case MSG_CTL_CLICK:
				if (mDoHandleClick) {
					mLinearLayoutControlBar.setVisibility(View.GONE);
					mDoHandleClick = false;
				}
				break;
			case MSG_CTL_SEEKBAR:
				if (mDoHandleSeek) {
					mLinearLayoutControlBar.setVisibility(View.GONE);
					mDoHandleSeek = false;
				}
				break;
			default:
				break;
			}
		}
	};

	/**
	 * 滑动改变声音大小
	 * 
	 * @param percent
	 */
	private void onVolumeSlide(float percent) {
		if (mVolume == -1) {
			mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			if (mVolume < 0)
				mVolume = 0;

			// 显示
			mOperationBg.setImageResource(R.drawable.video_volumn_bg);
			mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
		}

		int index = (int) (percent * mMaxVolume) + mVolume;
		if (index > mMaxVolume)
			index = mMaxVolume;
		else if (index < 0)
			index = 0;

		// 变更声音
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);

		// 变更进度条
		ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
		lp.width = findViewById(R.id.operation_full).getLayoutParams().width
				* index / mMaxVolume;
		mOperationPercent.setLayoutParams(lp);
	}

	/**
	 * 滑动改变亮度
	 * 
	 * @param percent
	 */
	private void onBrightnessSlide(float percent) {
		if (mBrightness < 0) {
			mBrightness = getWindow().getAttributes().screenBrightness;
			if (mBrightness <= 0.00f)
				mBrightness = 0.50f;
			if (mBrightness < 0.01f)
				mBrightness = 0.01f;

			// 显示
			mOperationBg.setImageResource(R.drawable.video_brightness_bg);
			mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
		}
		WindowManager.LayoutParams lpa = getWindow().getAttributes();
		lpa.screenBrightness = mBrightness + percent;
		if (lpa.screenBrightness > 1.0f)
			lpa.screenBrightness = 1.0f;
		else if (lpa.screenBrightness < 0.01f)
			lpa.screenBrightness = 0.01f;
		getWindow().setAttributes(lpa);

		ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
		lp.width = (int) (findViewById(R.id.operation_full).getLayoutParams().width * lpa.screenBrightness);
		mOperationPercent.setLayoutParams(lp);
	}

	// 电池电量检测
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equalsIgnoreCase(Intent.ACTION_BATTERY_CHANGED)) {
				int batteryLevel = intent.getIntExtra("level", 0);
				// Log.v(LOGTAG, "---->get batteryLevel = " + batteryLevel);
				if (batteryLevel >= 50)
					mBattery.setTextColor(Color.GREEN);
				else if (batteryLevel >= 30)
					mBattery.setTextColor(Color.YELLOW);
				else
					mBattery.setTextColor(Color.RED);
				mBattery.setText(String.format("%d%%", batteryLevel));
			}
			// else if (action.equalsIgnoreCase(VLCApplication.SLEEP_INTENT)) {
			// finish();
			// }
		}
	};

	/**
	 * 菜单、返回键响应
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exitBy2Click(); // 调用双击退出函数
		}
		return false;
	}

	/**
	 * 双击退出函数
	 */
	private static Boolean isExit = false;

	private void exitBy2Click() {
		Timer tExit = null;
		if (isExit == false) {
			isExit = true; // 准备退出
			Toast.makeText(this, "再按一次退出播放", Toast.LENGTH_SHORT).show();
			tExit = new Timer();
			TimerTask mTimerTask = new TimerTask() {
				@Override
				public void run() {
					isExit = false; // 取消退出
				}
			};
			tExit.schedule(mTimerTask, 3000); // 如果3秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务
		} else {
			finish();
		}
	}

	/**
	 * 收藏后更新某一条数据信息
	 * 
	 */
	private void updateFavDatabase(String name) {
		int resource = -1;

		fav_num = sharedPreferences.getInt("fav_num", 0);
		Log.d(LOGTAG, "===>current fav_num = " + fav_num);

		// 为提升用户点击广告的热情，特地将收藏频道数目超过3个的的积分额度为100积分
		if (fav_num >= 3) {
			// FIXME 此处可以修改积分限制
			if (sharedPreferences.getInt("pointTotal", 0) < 100) {
				new AlertDialog.Builder(PlayerActivity.this)
						.setIcon(R.drawable.ic_dialog_alert)
						.setTitle("温馨提示")
						.setMessage(
								"您的积分不足100分，暂时只能收藏3个频道！\n您可以到【设置】中打开应用推荐赚取相应的积分，感谢您的支持！")
						.setNegativeButton("知道了",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.cancel();
									}
								}).show();

				return;
			}
		}

		List<POChannelList> channelList = mDbHelper.queryForEq(
				POChannelList.class, "name", name);
		for (POChannelList channel : channelList) {
			if (channel.save) {
				channel.save = false;
				resource = SystemUtility.getDrawableId("ic_fav");
				mImageButtonStar.setBackgroundResource(resource);

				// 收藏频道数加1
				editor.putInt("fav_num", fav_num - 1);
				editor.commit();

				Toast.makeText(getApplicationContext(), "取消收藏",
						Toast.LENGTH_SHORT).show();
			} else {
				channel.save = true;
				resource = SystemUtility.getDrawableId("ic_fav_pressed");
				mImageButtonStar.setBackgroundResource(resource);

				// 收藏频道数加1
				editor.putInt("fav_num", fav_num + 1);
				editor.commit();

				Toast.makeText(getApplicationContext(), "添加收藏",
						Toast.LENGTH_SHORT).show();
			}
			// update
			Log.i(LOGTAG, "==============>" + channel.name + "###"
					+ channel.poId + "###" + channel.save);

			mDbHelper.update(channel);
		}
	}
	
//	/**
//	 * 自定义收藏后更新某一条数据信息
//	 * 
//	 */
//	private void updateSelfFavDatabase(String name) {
//		int resource = -1;
//
//		List<POUserDefChannel> channelList = mSelfDbHelper.queryForEq(
//				POUserDefChannel.class, "name", name);
//		for (POUserDefChannel channel : channelList) {
//			if (channel.save) {
//				channel.save = false;
//				resource = SystemUtility.getDrawableId("ic_fav");
//				mImageButtonStar.setBackgroundResource(resource);
//
//				Toast.makeText(getApplicationContext(), "取消收藏",
//						Toast.LENGTH_SHORT).show();
//			} else {
//				channel.save = true;
//				resource = SystemUtility.getDrawableId("ic_fav_pressed");
//				mImageButtonStar.setBackgroundResource(resource);
//
//				Toast.makeText(getApplicationContext(), "添加收藏",
//						Toast.LENGTH_SHORT).show();
//			}
//			// update
//			Log.i(LOGTAG, "==============>" + channel.name + "###"
//					+ channel.poId + "###" + channel.save);
//
//			mSelfDbHelper.update(channel);
//		}
//	}
}
