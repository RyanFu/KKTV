package org.stagex.danmaku.activity;

import java.util.ArrayList;

import org.keke.player.R;
import org.stagex.danmaku.adapter.ChannelSourceAdapter;
import org.stagex.danmaku.util.SourceName;

import br.com.dina.ui.widget.UITableView;
import br.com.dina.ui.widget.UITableView.ClickListener;

import cn.waps.AdView;
import cn.waps.AppConnect;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class ChannelSourceActivity extends Activity {
	/** Called when the activity is first created. */

	private static final String LOGTAG = "ChannelSourceActivity";
	// private ListView mFileList;
	private ChannelSourceAdapter mSourceAdapter;
	private ArrayList<String> infos;
	private String channel_name;
	private String program_path;

	/* 顶部标题栏的控件 */
	private ImageView button_source;
	private ImageView button_applist;
	private ImageView button_tuangou;
	private TextView button_back;

	private SharedPreferences sharedPreferences;
	private Editor editor;

	private Boolean channel_star = false;
	private Boolean isSelfTV = false;

	UITableView tableView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.channel_source);

		/* 顶部标题栏的控件 */
		button_source = (ImageView) findViewById(R.id.source_btn);
		button_applist = (ImageView) findViewById(R.id.applist_btn);
		button_tuangou = (ImageView) findViewById(R.id.tuangou_btn);
		button_back = (TextView) findViewById(R.id.back_btn);

		//
		// mFileList = (ListView) findViewById(R.id.channel_source);
		// // 防止滑动黑屏
		// mFileList.setCacheColorHint(Color.TRANSPARENT);
		//
		// 检测是否需要显示广告
		sharedPreferences = getSharedPreferences("keke_player", MODE_PRIVATE);
		editor = sharedPreferences.edit();
		if (sharedPreferences.getBoolean("noAd", false)) {
			// nothing
		} else {
			/* 广告栏控件 */
			LinearLayout container = (LinearLayout) findViewById(R.id.AdLinearLayout);
			new AdView(this, container).DisplayAd();
			
			// TODO 2013-08-08
			if (sharedPreferences.getBoolean("showActivity", true)) {
				// TODO 2013-08-06
				// 此处加载去广告积分优惠活动
				String serverValue=AppConnect.getInstance(this).getConfig("adactivity", null);
				if (serverValue != null) {
					new AlertDialog.Builder(ChannelSourceActivity.this)
					.setIcon(R.drawable.ic_dialog_alert)
					.setTitle("去广告活动")
					.setMessage(serverValue)
					.setPositiveButton("退出前不再提醒",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										DialogInterface dialog,
										int which) {
									// TODO
									// 2013-08-08
									// 为了不重复在source界面出现广告积分活动，每次启动程序只会运行一次广告活动
									// 同时，广告活动只会在有广告出现是才显示
									editor.putBoolean("showActivity", false);
									editor.commit();
								}
							})
					.setNegativeButton("赚积分",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										DialogInterface dialog,
										int which) {
									// 打开设置界面
									Intent intent = new Intent();
									intent.setClass(ChannelSourceActivity.this, SetupActivity.class);
									startActivity(intent);
									//===
									dialog.cancel();
								}
							}).show();
				}
				//==============
			}
		}
		//
		// // 设置监听事件
		// mFileList.setOnItemClickListener(new OnItemClickListener() {
		//
		// @Override
		// public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
		// long arg3) {
		// // TODO Auto-generated method stub
		// String info = (String) mFileList.getItemAtPosition(arg2);
		//
		// startLiveMedia(info, channel_name, arg2);
		// }
		// });
		//
		Intent intent = getIntent();
		infos = intent.getStringArrayListExtra("all_url");
		if (infos == null)
			Log.e(LOGTAG, "infos is null");
		channel_name = intent.getStringExtra("channel_name");
		program_path = intent.getStringExtra("program_path");
		channel_star = intent.getBooleanExtra("channelStar", false);
		isSelfTV = intent.getBooleanExtra("isSelfTV", false);
		button_back.setText(channel_name);
		//
		// mSourceAdapter = new ChannelSourceAdapter(this, infos);
		// mFileList.setAdapter(mSourceAdapter);

		// =====================================================
		tableView = (UITableView) findViewById(R.id.tableView);
		createList();
		Log.d(LOGTAG, "total items: " + tableView.getCount());
		tableView.commit();
		// =====================================================

		/* 设置监听 */
		setListensers();
	}

	// ===========================================================================
	/**
	 * 采用圆角布局的ListView
	 */
	private void createList() {
		CustomClickListener listener = new CustomClickListener();
		tableView.setClickListener(listener);
		int index = 0;
		String url = null;
		for (String info : infos) {
			url = SourceName.whichName(info);
//			if (SourceName.mHd || SourceName.mHot) {
				// 添加每一项
				tableView.addBasicItem(++index + ".\t" + url, "\t\t"
						+ ((SourceName.mHd == true) ? "HD\t" : "")
						+ ((SourceName.mHot == true) ? "HOT\t" : ""));
//			} else {
//				// 添加每一项
//				tableView.addBasicItem(++index + ".\t" + url);
//			}
		}
	}

	/**
	 * 设置监听事件
	 * 
	 * @author jgf
	 * 
	 */
	private class CustomClickListener implements ClickListener {
		@Override
		public void onClick(int index) {
			startLiveMedia(infos.get(index), channel_name, index);
		}
	}

	// ===========================================================================

	/**
	 * 启动播放器界面
	 * 
	 * @param liveUrl
	 * @param name
	 * @param pos
	 */
	private void startLiveMedia(String liveUrl, String name, int pos) {
		Intent intent = new Intent(ChannelSourceActivity.this,
				PlayerActivity.class);
		ArrayList<String> playlist = new ArrayList<String>();
		playlist.add(liveUrl);
		intent.putExtra("selected", 0);
		intent.putExtra("playlist", playlist);
		intent.putExtra("title", name);
		intent.putExtra("channelStar", channel_star);
		intent.putExtra("source", "地址" + Integer.toString(pos + 1) + "："
		// + mSourceAdapter.whichName(liveUrl));
				+ SourceName.whichName(liveUrl));
		if (isSelfTV)
			intent.putExtra("isSelfTV", true);
		startActivity(intent);
	}

	// Listen for button clicks
	private void setListensers() {
		button_source.setOnClickListener(goListener);
		button_applist.setOnClickListener(goListener);
		button_tuangou.setOnClickListener(goListener);
		button_back.setOnClickListener(goListener);
	}

	// 按键监听
	private Button.OnClickListener goListener = new Button.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.source_btn:
				// 显示帮助
				Intent intent = new Intent(ChannelSourceActivity.this,
						TvProgramActivity.class);
				// intent.putExtra("msgPath", "source.html");
				// intent.putExtra("msgName", "节目源介绍");
				if (program_path == null) {
					new AlertDialog.Builder(ChannelSourceActivity.this)
							.setIcon(R.drawable.ic_dialog_alert)
							.setTitle("警告")
							.setMessage("暂时没有该电视台的节目预告！")
							.setPositiveButton("知道了",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											// do nothing - it will close on
											// its own
										}
									}).show();
				} else {
					intent.putExtra("ProgramPath", program_path);
					intent.putExtra("ChannelName", channel_name);
					startActivity(intent);
				}
				break;
			case R.id.back_btn:
				// 回到上一个界面(Activity)
				finish();
				break;
			case R.id.applist_btn:
				// 显示精品应用推荐
				AppConnect.getInstance(ChannelSourceActivity.this).showOffers(
						ChannelSourceActivity.this);
				break;
			case R.id.tuangou_btn:
				// 显示团购
				AppConnect.getInstance(ChannelSourceActivity.this)
						.showTuanOffers(ChannelSourceActivity.this);
				break;
			default:
				Log.d(LOGTAG, "not supported btn id");
			}
		}
	};
}
