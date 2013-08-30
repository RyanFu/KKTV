package org.stagex.danmaku.activity;

import org.keke.player.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

public class MessageActivity extends Activity {
	/** Called when the activity is first created. */
	private static final String LOGTAG = "MessageActivity";

	/* 顶部标题栏的控件 */
	private TextView button_back;
	/* 需要显示的文本信息 */
	private WebView mWebView;
	private String mMsgPath;
	private String mMsgName;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message);

		/* 顶部标题栏的控件 */
		button_back = (TextView) findViewById(R.id.back_btn);
		mWebView = (WebView) findViewById(R.id.wv);
		/* 设置监听 */
		setListensers();

		Intent intent = getIntent();
		mMsgPath = intent.getStringExtra("msgPath");
		mMsgName = intent.getStringExtra("msgName");
		
		button_back.setText(mMsgName);
		readHtmlFormAssets();
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

		webSettings.setLoadWithOverviewMode(true);
		// WebView双击变大，再双击后变小，当手动放大后，双击可以恢复到原始大小
		// webSettings.setUseWideViewPort(true);
		// 设置WebView可触摸放大缩小：
		// webSettings.setBuiltInZoomControls(true);
		// WebView 背景透明效果
		mWebView.setBackgroundColor(Color.TRANSPARENT);
		mWebView.loadUrl("file:///android_asset/html/" + mMsgPath);
	}
}
