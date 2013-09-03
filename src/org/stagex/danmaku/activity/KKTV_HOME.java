package org.stagex.danmaku.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.keke.player.R;
import org.stagex.danmaku.adapter.KKTV_HOME_LISTVIEW_Adapter;
import org.stagex.danmaku.parser.HomeListDomParse;
import org.stagex.danmaku.type.Home_List_type;
import org.stagex.danmaku.util.GlobalValue;
import org.stagex.danmaku.util.Network;
import org.stagex.danmaku.util.SystemUtility;

import cn.waps.AppConnect;
import cn.waps.UpdatePointsNotifier;

import com.yixia.vparser.VParser;
import com.yixia.vparser.model.Video;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView.ScaleType;

public class KKTV_HOME extends BaseActivity implements OnItemClickListener, UpdatePointsNotifier {

	private static final String LOGTAG = "HomeActivity";
	private ViewPager viewPager; // android-support-v4中的滑动组件
	private List<ImageView> imageViews; // 滑动的图片集合
	public ProgressDialog progressDialog;
	private String[] titles; // 图片标题
	private int[] imageResId; // 图片ID
	private List<View> dots; // 图片标题正文的那些点

	private TextView tv_title;
	private int currentItem = 0; // 当前图片的索引号
	// An ExecutorService that can schedule commands to run after a given delay,
	// or to execute periodically.
	private ScheduledExecutorService scheduledExecutorService;
	public KKTV_HOME_LISTVIEW_Adapter adapter;
	public List<Home_List_type> lists;
	VParser vParser;

	// JGF 从源Home界面拷贝过来的代码
	private SharedPreferences sharedPreferences;
	private Editor editor;

	// FIXME 如果数据库变化了，根据版本号，需要在这里添加判别代码
	private int DBversion = GlobalValue.dataBaseVerion; /* 特别注意，这里要与数据库SQLiteHelperOrm.java中的版本号一致 */
	private boolean DBChanged = false;

	private String displayPointsText;
	private String currencyName = "积分";
	final Handler mHandler = new Handler();
	//end JGF
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init(R.layout.kktv_home, R.layout.kktv_drawmenu);
		imageResId = new int[] { R.drawable.home_1, R.drawable.home_2,
				R.drawable.home_3, R.drawable.home_4, R.drawable.home_5 };
		titles = new String[imageResId.length];
		titles[0] = "巩俐不低俗，我就不能低俗";
		titles[1] = "扑树又回来啦！再唱经典老歌引万人大合唱";
		titles[2] = "揭秘北京电影如何升级";
		titles[3] = "乐视网TV版大派送";
		titles[4] = "热血屌丝的反杀";

		imageViews = new ArrayList<ImageView>();

		// 之前Home界面的初始化代码
		initSomething();
		// end
		
		// 初始化图片资源
		for (int i = 0; i < imageResId.length; i++) {
			ImageView imageView = new ImageView(this);
			imageView.setImageResource(imageResId[i]);
			imageView.setScaleType(ScaleType.CENTER_CROP);
			imageViews.add(imageView);
		}

		dots = new ArrayList<View>();
		dots.add(findViewById(R.id.v_dot0));
		dots.add(findViewById(R.id.v_dot1));
		dots.add(findViewById(R.id.v_dot2));
		dots.add(findViewById(R.id.v_dot3));
		dots.add(findViewById(R.id.v_dot4));

		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_title.setText(titles[0]);//
		findViewById(R.id.back_btn).setOnClickListener(this);
		viewPager = (ViewPager) findViewById(R.id.vp);
		viewPager.setAdapter(new MyAdapter());// 设置填充ViewPager页面的适配器
		// 设置一个监听器，当ViewPager中的页面改变时调用
		viewPager.setOnPageChangeListener(new MyPageChangeListener());
		lists = HomeListDomParse.parseXml(this);
		adapter = new KKTV_HOME_LISTVIEW_Adapter(this, imageLoader, lists);
		listView = (ListView) findViewById(R.id.kktv_channel_info);
		((ListView) listView).setAdapter(adapter);
		listView.setOnItemClickListener(this);
		vParser = new VParser(this);
		progressDialog = new ProgressDialog(KKTV_HOME.this);
		progressDialog.setMessage("解析中...");
		progressDialog.setCancelable(false);
	}

	/**
	 * 从原来的Home界面拷贝过来的代码
	 */
	private void initSomething() {
		sharedPreferences = getSharedPreferences("keke_player", MODE_PRIVATE);
		editor = sharedPreferences.edit();

		// 判别数据库是否变化了，仅在升级或安装后，变化一次
		if (DBversion > sharedPreferences.getInt("DBversion", 0)) {
			Log.d(LOGTAG, "===>数据库版本过旧，即将更新！");
			// 版本号比之前记录的版本号高，说明变化了
			editor.putInt("DBversion", DBversion);
			editor.putBoolean("DBChanged", true);
			// 2013-07-28， 配合backup用户自定义的收藏列表
			editor.putBoolean("needSelfDevFavbkp", true);
			editor.commit();
		}

		// 判断CPU类型，如果低于ARMV6，则不让其运行
		if (SystemUtility.getArmArchitecture() <= 6) {
			// 如果已经是硬解码模式，则无需设置
			boolean isHardDec = sharedPreferences
					.getBoolean("isHardDec", false);
			if (isHardDec) {
				// do nothing
			} else
				new AlertDialog.Builder(KKTV_HOME.this)
						.setIcon(R.drawable.ic_dialog_alert)
						.setTitle("警告")
						.setMessage(
								"抱歉！软件解码库暂时不支持您的CPU\n\n请到设置中选择【硬解码】模式，且只能使用硬解码")
						// .setMessage("抱歉！软件解码库暂时不支持您的CPU")
						.setPositiveButton("设置",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// do nothing - it will close on
										// its own
										Intent intent = new Intent();
										// 跳转至设置界面
										intent.setClass(KKTV_HOME.this,
												SetupActivity.class);
										startActivity(intent);
									}
								}).show();
		}

		Network network = new Network(this);
		// 判断是否打开了网络
		if (network.isOpenNetwork()) {
			// 如果连接的是移动网络，对用户作出警告
			if (network.isMobileNetwork())
				new AlertDialog.Builder(KKTV_HOME.this)
						.setIcon(R.drawable.ic_dialog_alert)
						.setTitle("警告")
						.setMessage(
								"您正在使用2G/3G网络，由此产生的流量费用由网络运营商收取！\n\n是否切换至Wi-Fi网络？")
						.setPositiveButton("是",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// do nothing - it will close on its own
										Intent intent = null;
										try {
											intent = new Intent(
											// android.provider.Settings.ACTION_WIRELESS_SETTINGS);
											// 直接跳转到WIFI网络设置
													android.provider.Settings.ACTION_WIFI_SETTINGS);
											startActivity(intent);
										} catch (Exception e) {
											Log.w(LOGTAG,
													"open network settings failed, please check...");
											e.printStackTrace();
										}
									}
								})
						.setNegativeButton("否",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.cancel();
									}
								}).show();
		} else {
			// 如果没有网络连接
			new AlertDialog.Builder(KKTV_HOME.this)
					.setIcon(R.drawable.ic_dialog_alert)
					.setTitle("没有可用的网络")
					.setMessage("推荐您只在Wi-Fi模式下观看直播电视节目！\n\n是否对Wi-Fi网络进行设置？")
					.setPositiveButton("是",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Intent intent = null;
									try {
										intent = new Intent(
										// android.provider.Settings.ACTION_WIRELESS_SETTINGS);
										// 直接跳转到WIFI网络设置
												android.provider.Settings.ACTION_WIFI_SETTINGS);
										startActivity(intent);
									} catch (Exception e) {
										Log.w(LOGTAG,
												"open network settings failed, please check...");
										e.printStackTrace();
									}
								}
							})
					.setNegativeButton("否",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
								}
							}).show();
		}

		// 启动广告
		AppConnect.getInstance(this);

		//========================================================
		// 2013-08-06
		// 由于新版1.3.1之后加入了积分要求在线配置的功能，所以可能会有调节功能
		// 如某段时间搞活动，要求的积分较少，过一段时间，可能要上调
		// 主要根据收益情况调整（这部分工作放到HomeActivity中去做）
		// 在线获取需要的积分参数，以便随时可以控制积分值
		String noAdPoint=AppConnect.getInstance(KKTV_HOME.this).getConfig("noAdPoint", "88888");
		if (sharedPreferences.getInt("pointTotal", 0) <=  Integer.parseInt(noAdPoint)) {
			editor.putBoolean("noAd", false);
			editor.commit();
			Log.d(LOGTAG, "===> reset Ad mode, has ad agatin");
		}
		
		// 2013-08-08
		// 为了不重复在source界面出现广告积分活动，每次启动程序只会运行一次广告活动
		// 同时，广告活动只会在有广告出现是才显示
		editor.putBoolean("showActivity", true);
		editor.commit();
		//========================================================
		
		// 创建应用程序工作目录
		File dir = new File(Environment.getExternalStorageDirectory().getPath()
				+ "/kekePlayer");
		if (dir.exists()) {
			/* do nothing */
		} else {
			dir.mkdirs();
		}
	}
	
	@Override
	protected void onResume() {
		unCheckIcon(flag_from, 1);
		flag_from = 1;
		
		// 从服务器端获取当前用户的虚拟货币.
		// 返回结果在回调函数getUpdatePoints(...)中处理
		AppConnect.getInstance(this).getPoints(this);
		
		super.onResume();
	}

	// 切换当前显示的图片
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			viewPager.setCurrentItem(currentItem);// 切换当前显示的图片
		};
	};

	@Override
	protected void onStart() {
		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		// 当Activity显示出来后，每两秒钟切换一次图片显示
		scheduledExecutorService.scheduleAtFixedRate(new ScrollTask(), 1, 2,
				TimeUnit.SECONDS);
		super.onStart();
	}

	@Override
	protected void onStop() {
		// 当Activity不可见的时候停止切换
		scheduledExecutorService.shutdown();
		super.onStop();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_btn:
			if (mMenuDrawer.isMenuVisible()) {
				mMenuDrawer.closeMenu();
			}else {
				mMenuDrawer.openMenu();
			}
			break;
		}
		super.onClick(v);
	}

	/**
	 * 在主界面按下返回键，提示用户是否退出应用
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 按下键盘上返回按钮
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			new AlertDialog.Builder(this)
					.setIcon(R.drawable.ic_lock_power_off)
					.setTitle(R.string.prompt)
					.setMessage(R.string.quit_desc)
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							})
					.setPositiveButton(R.string.confirm,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									finish();
								}
							}).show();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	/**
	 * 换行切换任务
	 */
	private class ScrollTask implements Runnable {
		public void run() {
			synchronized (viewPager) {
				currentItem = (currentItem + 1) % imageViews.size();
				handler.obtainMessage().sendToTarget(); // 通过Handler切换图片
			}
		}
	}

	/**
	 * 当ViewPager中页面的状态发生改变时调用
	 * 
	 * @author Administrator
	 * 
	 */
	private class MyPageChangeListener implements OnPageChangeListener {
		private int oldPosition = 0;

		/**
		 * This method will be invoked when a new page becomes selected.
		 * position: Position index of the new selected page.
		 */
		public void onPageSelected(int position) {
			currentItem = position;
			tv_title.setText(titles[position]);
			dots.get(oldPosition).setBackgroundResource(R.drawable.dot_normal);
			dots.get(position).setBackgroundResource(R.drawable.dot_focused);
			oldPosition = position;
		}

		public void onPageScrollStateChanged(int arg0) {

		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}
	}

	/**
	 * 填充ViewPager页面的适配器
	 */
	private class MyAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return imageResId.length;
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			((ViewPager) arg0).addView(imageViews.get(arg1));
			return imageViews.get(arg1);
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView((View) arg2);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
		}

		@Override
		public void finishUpdate(View arg0) {

		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		try {
			if (checkNetwork()) {
				Home_List_type type = lists.get(position);
				new InitData(type).execute();
			}
		} catch (Exception e) {
		}

	}

	private void startLiveMedia(ArrayList<String> liveUrls, String name) {
		Intent intent = new Intent(KKTV_HOME.this, PlayerActivity.class);
		intent.putExtra("selected", 0);
		intent.putExtra("playlist", liveUrls);
		intent.putExtra("title", name);
		startActivity(intent);
	}

	class InitData extends AsyncTask<Void, Void, Void> {
		Home_List_type type;

		InitData(Home_List_type type) {
			this.type = type;
		}

		@Override
		protected Void doInBackground(Void... paramArrayOfVoid) {
			try {
				ArrayList<String> urlStrings = new ArrayList<String>();
				if (type.getId() == 0) {
					if (type.getUrl().endsWith("html")) {
						Video video = vParser.parse(type.getUrl());
						if (video != null && video.videoUri != null) {
							urlStrings.add(video.videoUri);
						}
					} else if (type.getUrl().endsWith("m3u8")) {
						urlStrings.add(type.getUrl());
					} else if (type.getType().equals("直播")) {
						urlStrings.add(type.getUrl());
					}
					if (urlStrings.size() > 0) {
						startLiveMedia(urlStrings, type.getName());
					} else {
						showInfo("地址可能失效!!!");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog.show();
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			progressDialog.cancel();
		}
	}
	
	/**
	 * AppConnect.getPoints()方法的实现，必须实现
	 * 
	 * @param currencyName
	 *            虚拟货币名称.
	 * @param pointTotal
	 *            虚拟货币余额.
	 */
	public void getUpdatePoints(String currencyName, int pointTotal) {
		this.currencyName = currencyName;
		displayPointsText = currencyName + ": " + pointTotal;
		// 保存积分值
		editor.putInt("pointTotal", pointTotal);
		editor.commit();
		Log.d(LOGTAG, "===>" + displayPointsText);
	}

	/**
	 * AppConnect.getPoints() 方法的实现，必须实现
	 * 
	 * @param error
	 *            请求失败的错误信息
	 */
	public void getUpdatePointsFailed(String error) {
		displayPointsText = error;
		Log.e(LOGTAG, "===>" + displayPointsText);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 关闭广告
		AppConnect.getInstance(this).close();
		// System.exit(0);
		// 或者下面这种方式
		android.os.Process.killProcess(android.os.Process.myPid());
	}
}
