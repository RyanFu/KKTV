package org.stagex.danmaku.activity;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.keke.player.R;
import org.stagex.danmaku.adapter.ProgramAdapter;
import org.stagex.danmaku.adapter.ProgramInfo;

import cn.waps.AdView;
import cn.waps.AppConnect;
import cn.waps.MiniAdView;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class TvProgramActivity extends Activity {
	/** Called when the activity is first created. */
	private static final String LOGTAG = "TvProgramActivity";

	/* 顶部标题栏的控件 */
	private TextView button_back;
	/* 需要显示的文本信息 */
	private WebView mWebView;
	private String mProgramPath;
	private String mChannelName;

	private TextView program_txt;
	private TextView date_txt;
	private ListView program_list;

	private int listPosition = 0;

	private ProgramAdapter mProgramAdapter;
	private SharedPreferences sharedPreferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tv_program);

		/* 顶部标题栏的控件 */
		button_back = (TextView) findViewById(R.id.back_btn);
		mWebView = (WebView) findViewById(R.id.wv);
		program_txt = (TextView) findViewById(R.id.program_txt);
		date_txt = (TextView) findViewById(R.id.date_txt);
		program_list = (ListView) findViewById(R.id.program_list);
		// 防止滑动黑屏
		program_list.setCacheColorHint(Color.TRANSPARENT);
		/* 设置监听 */
		setListensers();

		Intent intent = getIntent();
		mProgramPath = intent.getStringExtra("ProgramPath");
		mChannelName = intent.getStringExtra("ChannelName");

		button_back.setText("节目预告");

		/* ====================================================== */
		/* 用webview方式显示节目预告 */
		// readHtmlFormAssets();
		/* ====================================================== */
		/* TODO 以listView文本方式显示节目预告 */
		Document doc = null;
		try {
			doc = Jsoup.connect(
					"http://www.tvmao.com/ext/show_tv.jsp?p=" + mProgramPath)
					.get();

			Elements links = doc.select("li"); // 带有href属性的a元素

			ArrayList<ProgramInfo> infos = new ArrayList<ProgramInfo>();

			Date fromDate = new Date();
			SimpleDateFormat simple1 = new SimpleDateFormat("kk:mm");

			// 当前时间
			String timeStr = DateFormat.format("kk:mm",
					System.currentTimeMillis()).toString();
			try {
				fromDate = simple1.parse(timeStr);
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			long curTime = fromDate.getTime();
			Boolean findFlag = false;

			for (Element link : links) {
				String[] pair = link.text().split(" ");
				if (pair.length < 2)
					continue;
				String time = pair[0].trim();
				String program = pair[1].trim();

				if (!findFlag) {
					listPosition++;
					try {
						fromDate = simple1.parse(time);
						/* 找到第一个比当前时间大的节目，而正在播放的实际是前一个节目 */
						if (fromDate.getTime() >= curTime) {
							findFlag = true;
						}
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				ProgramInfo info = new ProgramInfo(time, program, false);
				infos.add(info);
			}

			// 显示节目名称和当前日期和星期
			program_txt.setText(mChannelName);
			date_txt.setText(DateFormat.format("MM月dd日",
					System.currentTimeMillis())
					+ "  " + getWeekOfDate());

			// 显示节目列表，并且突出当前播放的节目
			mProgramAdapter = new ProgramAdapter(this, infos);
			program_list.setAdapter(mProgramAdapter);

			// 在listView中突出显示当前的播放节目
			if (!findFlag) {
				// FIXME bug#0022 有些节目预告有内容，但是不是真正的节目单，此时的失败是因为没有节目单
				if (infos.size() == 0) {
					date_txt.setVisibility(View.GONE);
					program_txt.setText("抱歉，暂时无法获取节目预告！");
				} else {
					// FIXME bug#0022 此处的没找到是因为有节目预告，但是处于24：00分左右的临界情况
					/* 如果没有大于当前时间值的节目，说明当日的最后一个节目就是当前播放的节目 */
					infos.get(infos.size() - 1).SetProgram(true);
					program_list.setSelection(infos.size() - 1);
				}
			} else if (listPosition == 1) {
				/* 如果第一个节目的时间指就大于当前时间，实际是前一天的最后一个节目，在新的一天什么都不显示 */
			} else {
				/* 其他正常情况，如果找到一个大于当前时间值的节目，置前一个节目为正在播放节目 */
				infos.get(listPosition - 2).SetProgram(true);
				program_list.setSelection(listPosition - 2);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			program_txt.setText("抱歉，暂时无法获取节目预告！");
		}
		/* ====================================================== */

		// 检测是否需要显示广告
		sharedPreferences = getSharedPreferences("keke_player", MODE_PRIVATE);
		if (sharedPreferences.getBoolean("noAd", false)) {
			// nothing
		} else {
			/* 广告栏控件 */
			LinearLayout container = (LinearLayout) findViewById(R.id.AdLinearLayout);
			new AdView(this, container).DisplayAd();
		}
	}

	// Listen for button clicks
	private void setListensers() {
		button_back.setOnClickListener(goListener);
	}

	// 按键监听
	private Button.OnClickListener goListener = new Button.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.back_btn:
				// 回到上一个界面(Activity)
				finish();
				break;
			default:
				Log.d(LOGTAG, "not supported btn id");
			}
		}
	};

	// 利用webview来显示帮助的文本信息
	private void readHtmlFormAssets() {
		WebSettings webSettings = mWebView.getSettings();

		// 充满全屏
		webSettings.setLoadWithOverviewMode(true);
		// WebView双击变大，再双击后变小，当手动放大后，双击可以恢复到原始大小
		// webSettings.setUseWideViewPort(true);
		// 设置WebView可触摸放大缩小：
		webSettings.setBuiltInZoomControls(true);
		// WebView 背景透明效果
		mWebView.setBackgroundColor(Color.TRANSPARENT);

		// webview中的新链接仍在当前browser中响应
		mWebView.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
		});
		mWebView.loadUrl("http://www.tvmao.com/ext/show_tv.jsp?p="
				+ mProgramPath);
	}

	// 首先响应webview的返回事件
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
			mWebView.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 获取星期值
	 */
	public static String getWeekOfDate() {
		String[] weekDays = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
		Calendar cal = Calendar.getInstance();
		Date curDate = new Date(System.currentTimeMillis());
		cal.setTime(curDate);
		int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
		if (w < 0)
			w = 0;
		return weekDays[w];
	}
}
