package org.stagex.danmaku.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.keke.player.R;
import org.stagex.danmaku.adapter.ChannelInfo;
import org.stagex.danmaku.adapter.ChannelLoadAdapter;
import org.stagex.danmaku.util.BackupData;
import org.stagex.danmaku.util.ParseUtil;

import com.nmbb.oplayer.scanner.DbHelper;
import com.nmbb.oplayer.scanner.POUserDefChannel;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class UserLoadActivity extends Activity {
	/** Called when the activity is first created. */
	private static final String LOGTAG = "UserLoadActivity";

	/* 顶部标题栏的控件 */
	private TextView button_back;
	private ImageView button_search;
	private ImageView button_edit;
//	private ImageView button_defFav;
	/* ListView */
	private ListView mTvList;
	private ChannelLoadAdapter mSourceAdapter;
	private List<ChannelInfo> infos;

	private WebView mWebView;

	/* 频道收藏的数据库 */
	private DbHelper<POUserDefChannel> mDbHelper;

	private SharedPreferences sharedPreferences;
	private Editor editor;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_load);

		/* 顶部标题栏的控件 */
		button_back = (TextView) findViewById(R.id.back_btn);
		button_search = (ImageView) findViewById(R.id.help_btn);
		button_edit = (ImageView) findViewById(R.id.edit_btn);
//		button_defFav = (ImageView) findViewById(R.id.fav_btn);

		mWebView = (WebView) findViewById(R.id.wv);

		sharedPreferences = getSharedPreferences("keke_player", MODE_PRIVATE);
		editor = sharedPreferences.edit();
		
		/* 设置监听 */
		setListensers();

		mTvList = (ListView) findViewById(R.id.tv_list);
		// 防止滑动黑屏
		mTvList.setCacheColorHint(Color.TRANSPARENT);

		/* 频道收藏的数据库 */
		mDbHelper = new DbHelper<POUserDefChannel>();
		
		String path = Environment.getExternalStorageDirectory().getPath()
				+ "/kekePlayer/tvlist.txt";
		File listFile = new File(path);
		if (listFile.exists()) {
			
			// ===============================================================
			if (sharedPreferences.getBoolean("no_SelfFav_help", false) == false) {
				new AlertDialog.Builder(UserLoadActivity.this)
				.setIcon(R.drawable.ic_dialog_alert)
				.setTitle("温馨提示")
				.setMessage(
						"【长按】自定义列表的频道名称即可以实现自定义收藏，并且点击工具栏的心型按钮可以跳转到自定义收藏的频道列表！\n" +
						"【长按】收藏频道列表的频道名称可以实现取消收藏功能！\n")
				.setPositiveButton("不再提醒",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(
									DialogInterface dialog,
									int which) {
								// 不再收藏
								editor.putBoolean("no_SelfFav_help", true);
								editor.commit();
							}
						})
				.setNegativeButton("知道了",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(
									DialogInterface dialog,
									int which) {
								dialog.cancel();
							}
						}).show();
			}
			// ===============================================================
			
			mTvList.setVisibility(View.VISIBLE);
			mWebView.setVisibility(View.GONE);
			// 解析本地的自定义列表
			infos = ParseUtil.parseDef(path);

			mSourceAdapter = new ChannelLoadAdapter(this, infos);
			mTvList.setAdapter(mSourceAdapter);
			// 设置监听事件
			mTvList.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					ChannelInfo info = (ChannelInfo) mTvList
							.getItemAtPosition(arg2);

					// FIXME 2013-07-31 这里的收藏就不放入是否收藏的按钮了
					startLiveMedia(info.getAllUrl(), info.getName(), false);
				}
			});
			// 增加长按频道收藏功能
			mTvList.setOnItemLongClickListener(new OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					ChannelInfo info = (ChannelInfo) mTvList
							.getItemAtPosition(arg2);
					// 转换为数据库数据结构
					POUserDefChannel POinfo = new POUserDefChannel(info, true);
					showFavMsg(arg1, POinfo);
					return true;
				}
			});
		} else {
			// 如果不存在，则显示帮助文档
			readHtmlFormAssets();
		}
	}

	// Listen for button clicks
	private void setListensers() {
		button_back.setOnClickListener(goListener);
		button_search.setOnClickListener(goListener);
		button_edit.setOnClickListener(goListener);
//		button_defFav.setOnClickListener(goListener);
	}

	// 打开网络媒体
	private void startLiveMedia(ArrayList<String> all_url, String name, Boolean isStar) {
		// 如果该节目只有一个候选源地址，那么直接进入播放界面
		if (all_url.size() == 1) {
			Intent intent = new Intent(UserLoadActivity.this,
					PlayerActivity.class);
			ArrayList<String> playlist = new ArrayList<String>();
			playlist.add(all_url.get(0));
			intent.putExtra("selected", 0);
			intent.putExtra("playlist", playlist);
			intent.putExtra("title", name);
			intent.putExtra("channelStar", isStar);
			intent.putExtra("isSelfTV", true);
			startActivity(intent);
		} else {
			// 否则进入候选源界面
			Intent intent = new Intent(UserLoadActivity.this,
					ChannelSourceActivity.class);
			intent.putExtra("all_url", all_url);
			intent.putExtra("channel_name", name);
			intent.putExtra("channelStar", isStar);
			intent.putExtra("isSelfTV", true);
			startActivity(intent);
		}
	}

	// 按键监听
	private Button.OnClickListener goListener = new Button.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.back_btn:
				// 回到上一个界面(Activity)
				
				// TODO 暂时在每次返回的时候，都进行备份数据库
//			    new BackupData(UserLoadActivity.this).execute("backupDatabase");
			    new BackupData().execute("backupDatabase");
			    
				finish();
				break;
			case R.id.help_btn:
				showHelp();
				break;
			case R.id.edit_btn:
				// 用户自己输入网址
				Intent intent = new Intent();
				intent.setClass(UserLoadActivity.this, UserDefActivity.class);
				startActivity(intent);
				break;
//			case R.id.fav_btn:
//				// TODO 暂时在每次打开自定义收藏的时候，都进行备份数据库
//			    new BackupData(UserLoadActivity.this).execute("backupDatabase");
//				// 打开自定义的收藏频道
//				Intent intent_defFav = new Intent();
//				intent_defFav.setClass(UserLoadActivity.this,
//						UserDefFavActivity.class);
//				startActivity(intent_defFav);
//				break;
			default:
				Log.d(LOGTAG, "not supported btn id");
			}
		}
	};

	// 显示帮助对话框
	private void showHelp() {
		readHtmlFormAssets();
	}

	// 利用webview来显示帮助的文本信息
	private void readHtmlFormAssets() {
		mTvList.setVisibility(View.GONE);
		mWebView.setVisibility(View.VISIBLE);
		WebSettings webSettings = mWebView.getSettings();

		webSettings.setLoadWithOverviewMode(true);
		// WebView双击变大，再双击后变小，当手动放大后，双击可以恢复到原始大小
		// webSettings.setUseWideViewPort(true);
		// 设置WebView可触摸放大缩小：
		// webSettings.setBuiltInZoomControls(true);
		// WebView 背景透明效果
		mWebView.setBackgroundColor(Color.TRANSPARENT);
		mWebView.loadUrl("file:///android_asset/html/tvList_help.html");
	}

	/**
	 * 提示是否收藏为个性频道
	 */
	private void showFavMsg(View view, POUserDefChannel info) {

		final POUserDefChannel saveInfo = info;

//		// TODO 需要判断是否已经收藏过，先按名称，再判断地址
//		List<POUserDefChannel> exitInfo = mDbHelper.queryForEq(POUserDefChannel.class, "name", saveInfo.name);
//		int size = exitInfo.size();
//		if (size == 1) {
//			ArrayList<String> loadUrl = info.getAllUrl();
//			ArrayList<String> exitUrl = exitInfo.get(0).getAllUrl();
//			int size1 = loadUrl.size();
//			int size2 = exitUrl.size();
//			for (int m = 0; m < size1; m++) {
//				for (int n = 0; n < size1; n++) {
//					// TODO 判断地址是否相同，不同则合并之
//				}
//			}
//			
//		} else {
			new AlertDialog.Builder(UserLoadActivity.this)
					.setIcon(R.drawable.ic_dialog_alert)
					.setTitle("温馨提示")
					.setMessage("确定收藏该自定义频道吗？")
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO 增加加入数据库操作
							saveInfo.date = DateFormat.format("MM月dd日",
									System.currentTimeMillis()).toString();
							mDbHelper.create(saveInfo);
							
//							// TODO 暂时在每次打开自定义收藏的时候，都进行备份数据库
//						    new BackupData(UserLoadActivity.this).execute("backupDatabase");
							
						}
					})
					.setNegativeButton("取消", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					}).show();
		}
//	}
	
	/**
	 * 在主界面按下返回键，提示用户是否退出应用
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 按下键盘上返回按钮
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// TODO 暂时在每次返回的时候，都进行备份数据库
//		    new BackupData(UserLoadActivity.this).execute("backupDatabase");
		    new BackupData().execute("backupDatabase");
		    
		    finish();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
}
