package com.nmbb.oplayer.ui;

import org.stagex.danmaku.OPlayerApplication;
import org.stagex.danmaku.OPreference;
import org.keke.player.R;
import org.stagex.danmaku.activity.HomeActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nmbb.oplayer.scanner.MediaScannerService;

public class MainActivity extends FragmentActivity implements OnClickListener {

	private static final String LOGTAG = "MainActivity";
	private ViewPager mPager;
	// private RadioButton mRadioFile;
	// private RadioButton mRadioOnline;
	// public FileDownloadHelper mFileDownload;

	/* 顶部标题栏的控件 */
	private ImageView button_home;
	private TextView button_back;
	private ImageView button_refresh;

	/* 旋转图标 */
	private Animation operatingAnim;
	private LinearInterpolator lin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// LibsChecker.checkVitamioLibs(ctx)
		// if (!LibsChecker.checkVitamioLibs(this, R.string.init_decoders))
		// return;

		// Log.v(LOGTAG, "===>mainActivity onCreate");

		OPreference pref = new OPreference(this);
		// 首次运行，扫描SD卡
		if (pref.getBoolean(OPlayerApplication.PREF_KEY_FIRST, true)) {
			Log.v(LOGTAG, "===>首次运行，扫描SD卡媒体内容");
			startMediaScannerService();
		}

		setContentView(R.layout.fragment_pager);

		// ~~~~~~ 绑定控件
		mPager = (ViewPager) findViewById(R.id.pager);
		// mRadioFile = (RadioButton) findViewById(R.id.radio_file);
		// mRadioOnline = (RadioButton) findViewById(R.id.radio_online);
		button_home = (ImageView) findViewById(R.id.home_btn);
		button_back = (TextView) findViewById(R.id.back_btn);
		button_refresh = (ImageView) findViewById(R.id.refresh_btn);

		/* 旋转图标 */
		operatingAnim = AnimationUtils.loadAnimation(this, R.anim.refresh);
		lin = new LinearInterpolator();
		operatingAnim.setInterpolator(lin);

		// ~~~~~~ 绑定事件
		// mRadioFile.setOnClickListener(this);
		// mRadioOnline.setOnClickListener(this);
		mPager.setOnPageChangeListener(mPagerListener);
		setListensers();

		// 扫描事件初始化
		initializeEvents();

		// ~~~~~~ 绑定数据
		mPager.setAdapter(mAdapter);
	}

	/** 启动媒体扫描服务 */
	private void startMediaScannerService() {
		getApplicationContext().startService(
				new Intent(getApplicationContext(), MediaScannerService.class)
						.putExtra(MediaScannerService.EXTRA_DIRECTORY,
								Environment.getExternalStorageDirectory()
										.getAbsolutePath()));
	}

	// Listen for button clicks
	private void setListensers() {
		button_home.setOnClickListener(goListener);
		button_back.setOnClickListener(goListener);
		button_refresh.setOnClickListener(goListener);
	}

	private Button.OnClickListener goListener = new Button.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.home_btn:
				// 退回主界面(homeActivity)
				finish();
				break;
			case R.id.back_btn:
				// 回到上一个界面(Activity)
				finish();
				break;
			case R.id.refresh_btn:
				/* 开始旋转 */
				if (operatingAnim != null) {
					button_refresh.startAnimation(operatingAnim);
				}
				Log.v(LOGTAG, "===>重新扫描SD卡媒体内容");
				Toast.makeText(MainActivity.this, "重新扫描SD卡媒体内容",
						Toast.LENGTH_LONG).show();
				startMediaScannerService();
				/* TODO 何时停止旋转 */
				break;
			default:
				Log.d(LOGTAG, "not supported btn id");
			}
		}
	};

	@Override
	public void onBackPressed() {
		if (getFragmentByPosition(mPager.getCurrentItem()).onBackPressed())
			return;
		else
			super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// if (mFileDownload != null)
		// mFileDownload.stopALl();
	}

	/** 查找Fragment */
	private FragmentBase getFragmentByPosition(int position) {
		return (FragmentBase) getSupportFragmentManager().findFragmentByTag(
				"android:switcher:" + mPager.getId() + ":" + position);
	}

	private FragmentPagerAdapter mAdapter = new FragmentPagerAdapter(
			getSupportFragmentManager()) {

		/** 仅执行一次 */
		@Override
		public Fragment getItem(int position) {
			Fragment result = null;
			switch (position) {
			case 1:
				// result = new FragmentOnline();// 在线视频
				break;
			case 0:
			default:
				result = new FragmentFile();// 本地视频
				// mFileDownload = new FileDownloadHelper(
				// ((FragmentFileOld) result).mDownloadHandler);
				break;
			}
			return result;
		}

		@Override
		public int getCount() {
			// return 2;
			return 1;
		}
	};

	private ViewPager.SimpleOnPageChangeListener mPagerListener = new ViewPager.SimpleOnPageChangeListener() {
		@Override
		public void onPageSelected(int position) {
			// switch (position) {
			// case 0:// 本地视频
			// mRadioFile.setChecked(true);
			// break;
			// case 1:// 在线视频
			// // mRadioOnline.setChecked(true);
			// break;
			// }
		}
	};

	@Override
	public void onClick(View v) {
		// switch (v.getId()) {
		// case R.id.radio_file:
		// mPager.setCurrentItem(0);
		// break;
		// case R.id.radio_online:
		// // mPager.setCurrentItem(1);
		// break;
		// }
	}

	private static Handler mEventHandler;
	private static final int MEDIA_SEAN_DOWN = 0x0001;

	/**
	 * 播放过程中的事件响应的核心处理方法
	 */
	private void initializeEvents() {
		mEventHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MEDIA_SEAN_DOWN:
					// 媒体扫描完毕
					button_refresh.clearAnimation();
					break;
				default:
					break;
				}
			}
		};
	}

	/**
	 * 以下：接收事件，做中间处理，再调用handleMessage方法处理之
	 * 
	 * @{
	 */
	public static void onMediaScanDown() {
		Message msg = new Message();
		msg.what = MEDIA_SEAN_DOWN;
		mEventHandler.sendMessage(msg);
	}

}
