package org.stagex.danmaku.activity;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.keke.player.R;
import org.stagex.danmaku.adapter.ChannelAdapter;
import org.stagex.danmaku.adapter.ChannelInfo;
import org.stagex.danmaku.adapter.ProvinceAdapter;
import org.stagex.danmaku.adapter.ProvinceInfo;
import org.stagex.danmaku.util.BackupData;
import org.stagex.danmaku.util.GlobalValue;
import org.stagex.danmaku.util.ParseUtil;
import org.stagex.danmaku.util.saveFavName;

import cn.waps.AppConnect;

import com.nmbb.oplayer.scanner.ChannelListBusiness;
import com.nmbb.oplayer.scanner.DbHelper;
import com.nmbb.oplayer.scanner.POChannelList;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class ChannelTabActivity extends TabActivity implements
		OnTabChangeListener {

	private static final String LOGTAG = "ChannelTabActivity";

	List<ChannelInfo> allinfos = null;

	List<POChannelList> yangshi_infos = null;
	List<POChannelList> weishi_infos = null;
	List<ProvinceInfo> difang_infos = null;
	List<POChannelList> tiyu_infos = null;
	List<POChannelList> yule_infos = null;
	List<POChannelList> qita_infos = null;

	private ListView yang_shi_list;
	private ListView wei_shi_list;
	private ListView di_fang_list;
	private ListView ti_yu_list;
	private ListView yu_le_list;
	private ListView qi_ta_list;

	private TabHost myTabhost;

	TextView view0, view1, view2, view3, view4, view5;

	/* 顶部标题栏的控件 */
	private ImageView button_home;
	private TextView button_back;
	private ImageView button_search;
	private ImageView button_refresh;

	/* 列表更新成功标志 */
	private SharedPreferences sharedPreferences;
	private Editor editor;
	private boolean isTVListSuc;
	/* 如果没有备份过服务器地址，则加载本地初始的地址 */
	private boolean hasBackup;
	/* 是否建立了频道列表数据库 */
	private boolean hasChannelDB;
	private boolean DBChanged = false;

	/* 旋转图标 */
	private Animation operatingAnim;
	private LinearInterpolator lin;

	/* 频道收藏的数据库 */
	private DbHelper<POChannelList> mDbHelper;
	private Map<String, Object> mDbWhere = new HashMap<String, Object>(2);
	private int fav_num = 0;
	
	/* 是否正在刷新频道的标志位 */
	private Boolean isRefreshing = false;
	private String serverValue;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.tab_channel);

		/* 顶部标题栏的控件 */
		button_home = (ImageView) findViewById(R.id.home_btn);
		button_back = (TextView) findViewById(R.id.back_btn);
		button_refresh = (ImageView) findViewById(R.id.refresh_btn);
		button_search = (ImageView) findViewById(R.id.search_btn);
		/* 旋转图标 */
		operatingAnim = AnimationUtils.loadAnimation(this, R.anim.refresh);
		lin = new LinearInterpolator();
		operatingAnim.setInterpolator(lin);

		/* 频道收藏的数据库 */
		mDbHelper = new DbHelper<POChannelList>();

		// 记录更新成功还是失败
		sharedPreferences = getSharedPreferences("keke_player", MODE_PRIVATE);
		editor = sharedPreferences.edit();

		setListensers();
		initializeEvents();

		myTabhost = this.getTabHost();
		myTabhost.setup();

//		myTabhost.setBackgroundDrawable(this.getResources().getDrawable(
//				R.drawable.bg_home));

		/* 设置每一个台类别的Tab */
		RelativeLayout tab0 = (RelativeLayout) LayoutInflater.from(this)
				.inflate(R.layout.tab_host_ctx, null);
		view0 = (TextView) tab0.findViewById(R.id.tab_label);
		view0.setText("央视");
		myTabhost.addTab(myTabhost.newTabSpec("One")// make a new Tab
				.setIndicator(tab0)
				// set the Title and Icon
				.setContent(R.id.yang_shi_tab));
		// set the layout

		RelativeLayout tab1 = (RelativeLayout) LayoutInflater.from(this)
				.inflate(R.layout.tab_host_ctx, null);
		view1 = (TextView) tab1.findViewById(R.id.tab_label);
		view1.setText("卫视");
		myTabhost.addTab(myTabhost.newTabSpec("Two")// make a new Tab
				.setIndicator(tab1)
				// set the Title and Icon
				.setContent(R.id.wei_shi_tab));
		// set the layout

		RelativeLayout tab2 = (RelativeLayout) LayoutInflater.from(this)
				.inflate(R.layout.tab_host_ctx, null);
		view2 = (TextView) tab2.findViewById(R.id.tab_label);
		view2.setText("地方");
		myTabhost.addTab(myTabhost.newTabSpec("Three")// make a new Tab
				.setIndicator(tab2)
				// set the Title and Icon
				.setContent(R.id.di_fang_tab));
		// set the layout

		RelativeLayout tab3 = (RelativeLayout) LayoutInflater.from(this)
				.inflate(R.layout.tab_host_ctx, null);
		view3 = (TextView) tab3.findViewById(R.id.tab_label);
		view3.setText("体育");
		myTabhost.addTab(myTabhost.newTabSpec("Four")// make a new Tab
				.setIndicator(tab3)
				// set the Title and Icon
				.setContent(R.id.ti_yu_tab));
		// set the layout

		RelativeLayout tab4 = (RelativeLayout) LayoutInflater.from(this)
				.inflate(R.layout.tab_host_ctx, null);
		view4 = (TextView) tab4.findViewById(R.id.tab_label);
		view4.setText("港台");
		myTabhost.addTab(myTabhost.newTabSpec("Five")// make a new Tab
				.setIndicator(tab4)
				// set the Title and Icon
				.setContent(R.id.yu_le_tab));
		// set the layout

		RelativeLayout tab5 = (RelativeLayout) LayoutInflater.from(this)
				.inflate(R.layout.tab_host_ctx, null);
		view5 = (TextView) tab5.findViewById(R.id.tab_label);
		view5.setText("其他");
		myTabhost.addTab(myTabhost.newTabSpec("Six")// make a new Tab
				.setIndicator(tab5)
				// set the Title and Icon
				.setContent(R.id.qi_ta_tab));
		// set the layout

		/* 设置Tab的监听事件 */
		myTabhost.setOnTabChangedListener(this);

		/* 获得各个台类别的list */
		yang_shi_list = (ListView) findViewById(R.id.yang_shi_tab);
		// 防止滑动黑屏
		yang_shi_list.setCacheColorHint(Color.TRANSPARENT);

		wei_shi_list = (ListView) findViewById(R.id.wei_shi_tab);
		// 防止滑动黑屏
		wei_shi_list.setCacheColorHint(Color.TRANSPARENT);

		di_fang_list = (ListView) findViewById(R.id.di_fang_tab);
		// 防止滑动黑屏
		di_fang_list.setCacheColorHint(Color.TRANSPARENT);

		ti_yu_list = (ListView) findViewById(R.id.ti_yu_tab);
		// 防止滑动黑屏
		ti_yu_list.setCacheColorHint(Color.TRANSPARENT);

		yu_le_list = (ListView) findViewById(R.id.yu_le_tab);
		// 防止滑动黑屏
		yu_le_list.setCacheColorHint(Color.TRANSPARENT);

		qi_ta_list = (ListView) findViewById(R.id.qi_ta_tab);
		// 防止滑动黑屏
		qi_ta_list.setCacheColorHint(Color.TRANSPARENT);

		// 默认显示第一个标签
		view0.setTextSize(25);
		view1.setTextSize(15);
		view2.setTextSize(15);
		view3.setTextSize(15);
		view4.setTextSize(15);
		view5.setTextSize(15);

		/* ======================================================== */
		/*
		 * FIXME 如果数据库已经建立（不管是否是最新的） 将从数据库读取频道列表数据，1.2.3（不含）之后的版本，
		 * 将不在直接将JSON数据映射到listView的adapter中去
		 */
		hasChannelDB = sharedPreferences.getBoolean("hasChannelDB", false);
		DBChanged = sharedPreferences.getBoolean("DBChanged", false);
		// FIXME 如果还没有建立频道数据库，将先不展示频道listView
		// (这种方式有问题，如果数据库版本更新了，数据会被清除掉，但是这时候，数据为空，
		// 而却不会自动更新远程直播地址，并建立新的数据库了
		if (!hasChannelDB || DBChanged) {
			// 软件升级之后，因为数据库已经存在，只是清空了，
			// 所以只是利用是否存在数据库判别是否需要重新建立数据库
			// 是不对的，所以如果数据库变化了，需要一个标志位来控制，
			// 在HomeActivity启动时，如果有变化，会置位该标志
			if (DBChanged) {
				DBChanged = false;
				editor.putBoolean("DBChanged", false);
				editor.commit();
			}
			// 正在刷新
			isRefreshing = true;
			
			// 此处的入口强制加入获取服务器tvlistVersion的代码
			// 为的是避免由此处自动升级，如果没有以下代码，
			// 回导致重启后又重复提醒更新的问题（实际已经更新至最新了）
			serverValue=AppConnect.getInstance(this).getConfig("tvlistNew", "19700101");
			editor.putString("tvlistDate", serverValue);
			editor.commit();
			
			Toast.makeText(ChannelTabActivity.this.getApplicationContext(), "正在下载直播地址", Toast.LENGTH_LONG).show();
			
			// 沒有频道数据库，则第一次启动自动加载服务器列表地址
			startRefreshList();
			Log.i(LOGTAG, "===>has no database, load remote playlist first");
		} else {
			// 加载database中的相关频道列表数据
			// 首选显示央视，所以get央视频道
			getPlayList();
			showPlayList();
		}
		/* ======================================================== */
		
		// 2013-08-05获取在线参数tvlistNew（日期形式），用以标记节目源地址是否有更新
		if (isRefreshing == false) {	/* 如果不在刷新 */
			if (tvlistNew()) {
				//  如果服务器上的节目源日期大于本地日期，那么需要更新
				new AlertDialog.Builder(this)
				.setIcon(R.drawable.ic_dialog_alert)
				.setTitle("节目源有更新")
				.setMessage("现在刷新吗？")
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
								// 正在刷新
								isRefreshing = true;
								
								editor.putString("tvlistDate", serverValue);
								editor.commit();
								
								// 刷新节目列表
								startRefreshList();
							}
						}).show();
			}
		}
		//==========================================================
	}

	// 判断节目源地址是否有更新
	private Boolean tvlistNew() {
		serverValue=AppConnect.getInstance(this).getConfig("tvlistNew", "19700101");
		Log.i(LOGTAG, "===> get netServer value : " + serverValue);
		// 获取本地存储的上次更新的日期，默认值为每次提交时的时间
		int localValue = Integer.parseInt(sharedPreferences.getString("tvlistDate", GlobalValue.tvlistVersion));
		int netValue = Integer.parseInt(serverValue);
		
		if (netValue > localValue) {
			return true;
		} else 
			return false;
	}
	
	// 从数据库listView
	private void getPlayList() {
		// 重新加载当前的播放列表
		if (allinfos != null) {
			// 清除之前的数据
			if (yangshi_infos != null)
				yangshi_infos.clear();
			if (weishi_infos != null)
				weishi_infos.clear();
			if (difang_infos != null)
				difang_infos.clear();
			if (tiyu_infos != null)
				tiyu_infos.clear();
			if (yule_infos != null)
				yule_infos.clear();
			if (qita_infos != null)
				qita_infos.clear();
		}

		// 根据JSON里面的types来区分直播频道分类
		yangshi_infos = ChannelListBusiness.getAllSearchChannels("types", "1");
		weishi_infos = ChannelListBusiness.getAllSearchChannels("types", "2");
		difang_infos = ParseUtil.getProvinceNames(ChannelTabActivity.this);
		tiyu_infos = ChannelListBusiness.getAllSearchChannels("types", "4");
		yule_infos = ChannelListBusiness.getAllSearchChannels("types", "5");
		qita_infos = ChannelListBusiness.getAllSearchChannels("types", "6");
	}

	// 显示播放listView
	private void showPlayList() {
		setYangshiView();
		setWeishiView();
		setDifangView();
		setTiyuView();
		setYuleView();
		setQitaView();
		
		// 刷新结束
		isRefreshing = false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

	@Override
	public void onTabChanged(String tagString) {
		// TODO Auto-generated method stub
		if (tagString.equals("One")) {
			view0.setTextSize(25);
			view1.setTextSize(15);
			view2.setTextSize(15);
			view3.setTextSize(15);
			view4.setTextSize(15);
			view5.setTextSize(15);
		}
		if (tagString.equals("Two")) {
			view0.setTextSize(15);
			view1.setTextSize(25);
			view2.setTextSize(15);
			view3.setTextSize(15);
			view4.setTextSize(15);
			view5.setTextSize(15);
		}
		if (tagString.equals("Three")) {
			view0.setTextSize(15);
			view1.setTextSize(15);
			view2.setTextSize(25);
			view3.setTextSize(15);
			view4.setTextSize(15);
			view5.setTextSize(15);
		}
		if (tagString.equals("Four")) {
			view0.setTextSize(15);
			view1.setTextSize(15);
			view2.setTextSize(15);
			view3.setTextSize(25);
			view4.setTextSize(15);
			view5.setTextSize(15);
		}
		if (tagString.equals("Five")) {
			view0.setTextSize(15);
			view1.setTextSize(15);
			view2.setTextSize(15);
			view3.setTextSize(15);
			view4.setTextSize(25);
			view5.setTextSize(15);
		}
		if (tagString.equals("Six")) {
			view0.setTextSize(15);
			view1.setTextSize(15);
			view2.setTextSize(15);
			view3.setTextSize(15);
			view4.setTextSize(15);
			view5.setTextSize(25);
		}
	}

	/*
	 * 设置央视台源的channel list
	 */
	private void setYangshiView() {
		ChannelAdapter adapter = new ChannelAdapter(this, yangshi_infos);
		yang_shi_list.setAdapter(adapter);
		yang_shi_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				POChannelList info = (POChannelList) yang_shi_list
						.getItemAtPosition(arg2);
				// Log.d("ChannelInfo",
				// "name = " + info.getName() + "[" + info.getUrl() + "]");

				// startLiveMedia(info.getUrl(), info.getName());
				showAllSource(info.getAllUrl(), info.name, info.program_path,
						info.save);
			}
		});

		// 增加长按频道收藏功能
		yang_shi_list.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				POChannelList info = (POChannelList) yang_shi_list
						.getItemAtPosition(arg2);
				showFavMsg(arg1, info);
				return true;
			}
		});

		yang_shi_list.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub

			}
		});
	}

	/*
	 * 设置卫视台源的channel list
	 */
	private void setWeishiView() {
		ChannelAdapter adapter = new ChannelAdapter(this, weishi_infos);
		wei_shi_list.setAdapter(adapter);
		wei_shi_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				POChannelList info = (POChannelList) wei_shi_list
						.getItemAtPosition(arg2);
				// Log.d("ChannelInfo",
				// "name = " + info.getName() + "[" + info.getUrl() + "]");

				// startLiveMedia(info.getUrl(), info.getName());
				showAllSource(info.getAllUrl(), info.name, info.program_path,
						info.save);
			}
		});

		// 增加长按频道收藏功能
		wei_shi_list.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				POChannelList info = (POChannelList) wei_shi_list
						.getItemAtPosition(arg2);
				showFavMsg(arg1, info);
				return true;
			}
		});

		wei_shi_list.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub

			}
		});
	}

	/*
	 * 设置地方台源的channel list
	 */
	private void setDifangView() {
		ProvinceAdapter adapter = new ProvinceAdapter(this, difang_infos);
		di_fang_list.setAdapter(adapter);
		di_fang_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				ProvinceInfo info = (ProvinceInfo) di_fang_list
						.getItemAtPosition(arg2);

				showProvinceChannel(info.getProvinceName());
			}
		});

		di_fang_list.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub

			}
		});
	}

	/*
	 * 设置体育台源的channel list
	 */
	private void setTiyuView() {
		ChannelAdapter adapter = new ChannelAdapter(this, tiyu_infos);
		ti_yu_list.setAdapter(adapter);
		ti_yu_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				POChannelList info = (POChannelList) ti_yu_list
						.getItemAtPosition(arg2);
				// Log.d("ChannelInfo",
				// "name = " + info.getName() + "[" + info.getUrl() + "]");

				// startLiveMedia(info.getUrl(), info.getName());
				showAllSource(info.getAllUrl(), info.name, info.program_path,
						info.save);
			}
		});

		// 增加长按频道收藏功能
		ti_yu_list.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				POChannelList info = (POChannelList) ti_yu_list
						.getItemAtPosition(arg2);
				showFavMsg(arg1, info);
				return true;
			}
		});

		ti_yu_list.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub

			}
		});
	}

	/*
	 * 设置娱乐台源的channel list
	 */
	private void setYuleView() {
		ChannelAdapter adapter = new ChannelAdapter(this, yule_infos);
		yu_le_list.setAdapter(adapter);
		yu_le_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				POChannelList info = (POChannelList) yu_le_list
						.getItemAtPosition(arg2);
				// Log.d("ChannelInfo",
				// "name = " + info.getName() + "[" + info.getUrl() + "]");

				// startLiveMedia(info.getUrl(), info.getName());
				showAllSource(info.getAllUrl(), info.name, info.program_path,
						info.save);
			}
		});

		// 增加长按频道收藏功能
		yu_le_list.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				POChannelList info = (POChannelList) yu_le_list
						.getItemAtPosition(arg2);
				showFavMsg(arg1, info);
				return true;
			}
		});

		yu_le_list.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub

			}
		});
	}

	/*
	 * 设置其他未分类台源的channel list
	 */
	private void setQitaView() {
		ChannelAdapter adapter = new ChannelAdapter(this, qita_infos);
		qi_ta_list.setAdapter(adapter);
		qi_ta_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				POChannelList info = (POChannelList) qi_ta_list
						.getItemAtPosition(arg2);
				// Log.d("ChannelInfo",
				// "name = " + info.getName() + "[" + info.getUrl() + "]");

				// startLiveMedia(info.getUrl(), info.getName());
				showAllSource(info.getAllUrl(), info.name, info.program_path,
						info.save);
			}
		});

		// 增加长按频道收藏功能
		qi_ta_list.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				POChannelList info = (POChannelList) qi_ta_list
						.getItemAtPosition(arg2);
				showFavMsg(arg1, info);
				return true;
			}
		});

		qi_ta_list.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub

			}
		});
	}

	/**
	 * 显示所有的台源
	 */
	private void showAllSource(ArrayList<String> all_url, String name,
			String path, Boolean isStar) {
		// 如果该节目只有一个候选源地址，那么直接进入播放界面
		if (all_url.size() == 1) {
			Intent intent = new Intent(ChannelTabActivity.this,
					PlayerActivity.class);
			ArrayList<String> playlist = new ArrayList<String>();
			playlist.add(all_url.get(0));
			intent.putExtra("selected", 0);
			intent.putExtra("playlist", playlist);
			intent.putExtra("title", name);
			intent.putExtra("channelStar", isStar);
			startActivity(intent);
		} else {
			// 否则进入候选源界面
			Intent intent = new Intent(ChannelTabActivity.this,
					ChannelSourceActivity.class);
			intent.putExtra("all_url", all_url);
			intent.putExtra("channel_name", name);
			intent.putExtra("program_path", path);
			intent.putExtra("channelStar", isStar);
			startActivity(intent);
		}
	}

	/**
	 * 显示省台的所有频道
	 */
	private void showProvinceChannel(String provinceName) {
		Intent intent = new Intent(ChannelTabActivity.this,
				ProvinceActivity.class);
		intent.putExtra("province_name", provinceName);
		startActivity(intent);
	}

	// Listen for button clicks
	private void setListensers() {
		button_home.setOnClickListener(goListener);
		button_back.setOnClickListener(goListener);
		button_refresh.setOnClickListener(goListener);
		button_search.setOnClickListener(goListener);
	}

	private Button.OnClickListener goListener = new Button.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.home_btn:
				// 退回主界面(homeActivity)
				// TODO 暂时在每次返回的时候，都进行备份数据库
			    new saveFavName().execute("backupDatabase");
			    
				finish();
				break;
			case R.id.back_btn:
				// 回到上一个界面(Activity)
				// TODO 暂时在每次返回的时候，都进行备份数据库
			    new saveFavName().execute("backupDatabase");
			    
				finish();
				break;
			case R.id.search_btn:
				// 打开搜索界面
				Intent intent = new Intent(ChannelTabActivity.this,
						SearchActivity.class);
				startActivity(intent);
				break;
			case R.id.refresh_btn:
				// 更新服务器地址
				// 判断是否正在刷新
				if (isRefreshing == true) {
					// 发出警告toast
					Toast.makeText(ChannelTabActivity.this.getApplicationContext(), "正在刷新中", Toast.LENGTH_LONG).show();
				} else if (tvlistNew()){
					// 正在刷新
					isRefreshing = true;
					
					editor.putString("tvlistDate", serverValue);
					editor.commit();
					
					// 刷新节目列表
					startRefreshList();
				} else {
					// 地址已经是最新，再提供一个强制更新的按钮
//					Toast.makeText(ChannelTabActivity.this.getApplicationContext(), "节目源已经是最新", Toast.LENGTH_LONG).show();
					new AlertDialog.Builder(ChannelTabActivity.this)
					.setIcon(R.drawable.ic_dialog_alert)
					.setTitle("温馨提示")
					.setMessage("已经是最新地址！\n需要强制刷新吗？")
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
									// 正在刷新
									isRefreshing = true;
									
//									editor.putString("tvlistDate", serverValue);
//									editor.commit();
									
									// 刷新节目列表
									startRefreshList();
								}
							}).show();
				}
				break;
			default:
				Log.d(LOGTAG, "not supported btn id");
			}
		}
	};

	/**
	 * 提示是否收藏
	 */
	private void showFavMsg(View view, POChannelList info) {

		final ImageView favView = (ImageView) view.findViewById(R.id.fav_icon);
		final POChannelList saveInfo = info;

		fav_num = sharedPreferences.getInt("fav_num", 0);
		Log.d(LOGTAG, "===>current fav_num = " + fav_num);

		// 为提升用户点击广告的热情，特地将收藏频道数目超过3个的的积分额度为100积分
		if (fav_num >= 3) {
			// FIXME 此处可以修改积分限制
			if (sharedPreferences.getInt("pointTotal", 0) < 100) {
				new AlertDialog.Builder(ChannelTabActivity.this)
						.setIcon(R.drawable.ic_dialog_alert)
						.setTitle("温馨提示")
						.setMessage(
								"您的积分不足100分，暂时只能收藏3个频道！\n您可以到【设置】中打开应用推荐赚取相应的积分，感谢您的支持！")
						.setPositiveButton("赚积分",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										Intent intent = new Intent();
										intent.setClass(
												ChannelTabActivity.this,
												SetupActivity.class);
										startActivity(intent);
									}
								})
						.setNegativeButton("取消",
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

		new AlertDialog.Builder(ChannelTabActivity.this)
				.setIcon(R.drawable.ic_dialog_alert).setTitle("温馨提示")
				.setMessage("确定收藏该直播频道吗？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// do nothing - it will close on its own
						// TODO 增加加入数据库操作
						favView.setVisibility(View.VISIBLE);
						updateFavDatabase(saveInfo);
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).show();
	}

	/**
	 * 更新服务器地址
	 */
	private void startRefreshList() {
		// 发送开始刷新的消息
		onRefreshStart();

		Log.d(LOGTAG, "===> start refresh playlist");

		// 这里创建一个脱离UI主线程的线程负责网络下载
		new Thread() {
			public void run() {
				// 到远程服务器下载直播电视播放列表
				tvPlaylistDownload();

				isTVListSuc = sharedPreferences
						.getBoolean("isTVListSuc", false);

				// 如果下载成功，重新加载当前的播放列表
				if (isTVListSuc) {
					// 首先清空之前的数据
					if (allinfos != null)
						allinfos.clear();

					// 重新解析XML
					allinfos = ParseUtil.parse(ChannelTabActivity.this, true);

					// 备份所有的收藏频道
					List<POChannelList> favListChannel = ChannelListBusiness
							.getAllFavChannels();
					Log.i(LOGTAG, "===> backup favourite channels over!");

					// 清除原有的数据库数据
					ChannelListBusiness.clearAllOldDatabase();
					Log.i(LOGTAG, "===> clear old database over!");

					/**
					 * 重新更新直播地址后，需要更新数据库 TODO 此方法效率可能高一点，避免反复的打开关闭数据库
					 */
					try {
						ChannelListBusiness.buildDatabase(allinfos);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Log.i(LOGTAG, "===> build new database over!");

					// 将收藏的频道写回新数据库
					try {
						ChannelListBusiness.feedBackFavChannel(favListChannel);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Log.i(LOGTAG,
							"===> feedback favourite channels to database over!");
				} else {
					// 如果加载失败，若还没有建立本地数据库，则建立之

					/* 解析所有的channel list 区分是采用默认列表还是服务器列表 */
					hasChannelDB = sharedPreferences.getBoolean("hasChannelDB",
							false);
					hasBackup = sharedPreferences
							.getBoolean("hasBackup", false);
					/* 如果还没有数据库，就创建数据库 */
					if (!hasChannelDB) {
						allinfos = ParseUtil.parse(ChannelTabActivity.this,
								hasBackup);
						if (hasBackup)
							Log.d(LOGTAG, "采用服务器更新后的播放列表");
						/* 创建数据库 */
						try {
							ChannelListBusiness.buildDatabase(allinfos);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				/* 添加数据库存在标志位 */
				editor.putBoolean("hasChannelDB", true);
				editor.commit();
				// 显示播放listView
				getPlayList();
				// 发送刷新完毕的消息
				onRefreshEnd();
				Log.d(LOGTAG, "===> end refresh playlist");
			};
		}.start();
	}

	/**
	 * 更新界面的节目表list
	 */
	private void RefreshList() {
		// 置已经备份过的标志位
		editor.putBoolean("hasBackup", true);
		editor.commit();

		// 重新显示list
		showPlayList();
	}

	/**
	 * FTP下载单个文件测试
	 */
	private void tvPlaylistDownload() {
		FTPClient ftpClient = new FTPClient();
		FileOutputStream fos = null;

		// 5秒钟，如果超过就判定超时了
		ftpClient.setConnectTimeout(5000);

		// 假设更新列表成功
		editor.putBoolean("isTVListSuc", true);
		editor.commit();

		// TODO 后续可以设置多个服务器地址，防止服务器流量不够，导致更新失败
		try {
			int reply;

			// 设置编码格式
			ftpClient.setControlEncoding("UTF-8");
			// 连接服务器
			ftpClient.connect("182.18.22.50");

			reply = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				// 断开连接
				ftpClient.disconnect();
				// 更新列表失败
				editor.putBoolean("isTVListSuc", false);
				editor.commit();
				return;
			}

			// 用户登录信息
			ftpClient.login("ftp92147", "950288@kk");

			// 此处不需要Data前面的"/"
			String remoteFileName = "/ftp92147/Data/channel_list_cn.list.api2";
			// 此处要注意必须加上channel_list_cn.list.api2前面的"/"
			fos = new FileOutputStream(Environment
					.getExternalStorageDirectory().getPath()
					+ "/kekePlayer/.channel_list_cn.list.api2");

			ftpClient.setBufferSize(1024);
			// 设置文件类型（二进制）
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			// 使用FTP被动模式（让FTP服务器每次都开同一个端口发送数据）
			ftpClient.enterLocalPassiveMode();
			// 下载文件
			ftpClient.retrieveFile(remoteFileName, fos);
		} catch (IOException e) {
			e.printStackTrace();
			// 更新列表失败
			editor.putBoolean("isTVListSuc", false);
			editor.commit();
			// throw new RuntimeException("FTP客户端出错！", e);
		} finally {
			try {
				if (fos != null) {
					// TODO 需要对文件的合法性作一定的测试，例如大小
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				// 更新列表失败
				editor.putBoolean("isTVListSuc", false);
				editor.commit();
				// throw new RuntimeException("关闭文件发生异常！", e);
			}
			try {
				// 用户注销
				ftpClient.logout();
				// FTP断开连接
				ftpClient.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
				// 更新列表失败
				editor.putBoolean("isTVListSuc", false);
				editor.commit();
				// throw new RuntimeException("关闭FTP连接发生异常！", e);
			}
		}
	}

	private static Handler mEventHandler;
	private static final int TV_LIST_REFRESH_START = 0x0001;
	private static final int TV_LIST_REFRESH_END = 0x0002;

	/**
	 * 地址刷新过程中的事件响应的核心处理方法
	 */
	private void initializeEvents() {
		mEventHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case TV_LIST_REFRESH_START:
					// 开始刷新，开始转圈
					if (operatingAnim != null) {
						button_refresh.startAnimation(operatingAnim);
					}
					break;
				case TV_LIST_REFRESH_END:
					// 刷新完毕，停止转圈
					button_refresh.clearAnimation();
					// 处理刷新结果
					dealRefreshResult();
					break;
				default:
					break;
				}
			}
		};
	}

	/**
	 * 处理刷新的结果，判断刷新成功还是失败 如果成功，就更新位服务器上的直播地址 如果失败，仍采用当前的直播地址
	 */
	private void dealRefreshResult() {
		if (isTVListSuc) {
			// 更新界面的节目表list
			RefreshList();
			// 显示对话框
			// 弹出加载【成功】对话框
			if (ChannelTabActivity.this == null)
				return;
			new AlertDialog.Builder(ChannelTabActivity.this)
					.setIcon(R.drawable.ic_about)
					.setTitle("更新成功")
					.setMessage("服务器地址更新成功！\n数据库构建成功！")
					.setNegativeButton("知道了",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// do nothing - it will close on its
									// own
								}
							}).show();
		} else {
			// 重新显示list
			showPlayList();
			// 弹出加载【失败】toast
			if (ChannelTabActivity.this == null)
				return;
			new AlertDialog.Builder(ChannelTabActivity.this)
					.setIcon(R.drawable.ic_dialog_alert)
					.setTitle("更新失败")
					.setMessage("抱歉！服务器地址更新失败\n默认构建本地数据库！")
					.setNegativeButton("知道了",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// do nothing - it will close on its
									// own
								}
							}).show();
		}
	}

	/**
	 * 以下：接收事件，做中间处理，再调用handleMessage方法处理之
	 * 
	 * @{
	 */
	private void onRefreshStart() {
		Message msg = new Message();
		msg.what = TV_LIST_REFRESH_START;
		mEventHandler.sendMessage(msg);
	}

	private void onRefreshEnd() {
		Message msg = new Message();
		msg.what = TV_LIST_REFRESH_END;
		mEventHandler.sendMessage(msg);
	}

	/**
	 * 收藏后更新某一条数据信息
	 * 
	 */
	private void updateFavDatabase(POChannelList channelList) {
		if (channelList.save == false) {
			// 如果重复点击，只算一次添加
			// 收藏频道数加1
			editor.putInt("fav_num", fav_num + 1);
			editor.commit();
		}
		channelList.save = true;
		// update
		Log.i(LOGTAG, "==============>" + channelList.name + "###"
				+ channelList.poId + "###" + channelList.save);

		mDbHelper.update(channelList);
	}
	
	/**
	 * 在主界面按下返回键，提示用户是否退出应用
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 按下键盘上返回按钮
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// TODO 暂时在每次返回的时候，都进行备份数据库
		    new saveFavName().execute("backupDatabase");
		    
		    finish();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
}
