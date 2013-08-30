package org.stagex.danmaku.activity;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.keke.player.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class UserDefActivity extends Activity {
	/** Called when the activity is first created. */
	private static final String LOGTAG = "UserDefActivity";
	private EditText mTextUri = null;
	private Button mButtonPlay = null;

	/* 顶部标题栏的控件 */
	private TextView button_back;
	/* 文本编辑框 */
	private Button button_clear;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_def);

		/* 顶部标题栏的控件 */
		button_back = (TextView) findViewById(R.id.back_btn);
		/* 文本编辑框 */
		button_clear = (Button) findViewById(R.id.clear_play);
		mTextUri = (EditText) findViewById(R.id.test_uri);
		mButtonPlay = (Button) findViewById(R.id.test_play);

		/* 设置监听 */
		setListensers();
	}

	// Listen for button clicks
	private void setListensers() {
		button_back.setOnClickListener(goListener);
		button_clear.setOnClickListener(goListener);
		mButtonPlay.setOnClickListener(goListener);
	}

	/**
	 * 检查URL的合法性
	 * 
	 * @param str
	 * @return ftp的user@ IP形式的URL- 199.194.52.184 允许IP和DOMAIN（域名） 域名- www. 二级域名
	 *         first level domain- .com or .museum 端口- :80 a slash isn't
	 *         required if there is no file name
	 */
	private static boolean isURL(String str) {
		// 转换为小写
		str = str.toLowerCase();

		String regex = "^((https|http|ftp|rtsp|rtmp|mmsh|mms)?://)"
				+ "?(([0-9a-z_!~*'().&=+$%-]+: )?[0-9a-z_!~*'().&=+$%-]+@)?"
				+ "(([0-9]{1,3}\\.){3}[0-9]{1,3}" + "|"
				+ "([0-9a-z_!~*'()-]+\\.)*"
				+ "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\\." + "[a-z]{2,6})"
				+ "(:[0-9]{1,4})?" + "((/?)|"
				+ "(/[0-9a-z_!~*'().;?:@&=+$,%#-]+)+/?)$";

		Pattern p = Pattern.compile(regex);
		Matcher matcher = p.matcher(str);

		return matcher.matches();
	}

	// 打开网络媒体
	private void startMedia() {
		String uri = mTextUri.getText().toString();
		if (uri.length() > 0) {
			// 判断url的合法性
			if (isURL(uri)) {
				Intent intent = new Intent(UserDefActivity.this,
						PlayerActivity.class);
				ArrayList<String> playlist = new ArrayList<String>();
				playlist.add(uri);
				intent.putExtra("selected", 0);
				intent.putExtra("playlist", playlist);
				intent.putExtra("title", uri);
				startActivity(intent);
			} else {
				new AlertDialog.Builder(UserDefActivity.this)
						.setIcon(R.drawable.ic_dialog_alert)
						.setTitle("警告")
						.setMessage("请检查URL的合法性：\n" + uri)
						.setNegativeButton("知道了",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// do nothing - it will close on its own
									}
								}).show();
			}
		}
	}

	// 按键监听
	private Button.OnClickListener goListener = new Button.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.back_btn:
				// 回到上一个界面(Activity)
				finish();
				break;
			case R.id.test_play:
				startMedia();
				break;
			case R.id.clear_play:
				// 清除文本框内容
				mTextUri.setText("");
				break;
			default:
				Log.d(LOGTAG, "not supported btn id");
			}
		}
	};

}
