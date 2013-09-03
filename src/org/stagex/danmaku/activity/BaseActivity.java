package org.stagex.danmaku.activity;

import org.keke.player.R;
import org.stagex.danmaku.imageloader.AbsListViewBaseActivity;

import net.simonvt.menudrawer.MenuDrawer;
import net.simonvt.menudrawer.Position;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

public class BaseActivity extends AbsListViewBaseActivity implements
		OnClickListener {

	public MenuDrawer mMenuDrawer;
	private static float scale;
	public static final int leftMarge = 110;
	public static int flag_from = 1;
	public ConnectivityManager con;
	private SharedPreferences sharedPreferences;
	private Editor editor;
	public ImageButton[] KKTV_HOME_MENU;
	public int[] KKTV_HOME_MENU_Normal = {0, R.drawable.tab_recommend_normal, R.drawable.tab_channel_normal, R.drawable.btn_favorite_icon, R.drawable.default_img, R.drawable.radio, R.drawable.tab_more_normal, R.drawable.tab_more_normal};
	public int[] KKTV_HOME_MENU_Select = {0, R.drawable.tab_recommend_select, R.drawable.tab_channel_select, R.drawable.btn_favorite_icon_select, R.drawable.default_img_select, R.drawable.radio_select, R.drawable.tab_more_normal_select, R.drawable.tab_more_normal_select};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPreferences = getSharedPreferences("keke_player", MODE_PRIVATE);
		editor = sharedPreferences.edit();
	}

	public void init(int parent, int menu) {
		mMenuDrawer = MenuDrawer.attach(this, MenuDrawer.Type.BEHIND,
				Position.LEFT, MenuDrawer.MENU_DRAG_WINDOW);
		mMenuDrawer.setContentView(parent);
		mMenuDrawer.setMenuView(R.layout.kktv_drawmenu);
		scale = getResources().getDisplayMetrics().density;
		mMenuDrawer.setMenuSize(dip2px(leftMarge));
		initLayout();
		mMenuDrawer.openMenu();
	}

	public void initLayout() {
		KKTV_HOME_MENU = new ImageButton[8];
		KKTV_HOME_MENU[1] = (ImageButton)findViewById(R.id.home_first);
		KKTV_HOME_MENU[1].setBackgroundResource(KKTV_HOME_MENU_Select[1]);
		KKTV_HOME_MENU[1].setOnClickListener(this);
		KKTV_HOME_MENU[2] = (ImageButton)findViewById(R.id.home_two);
		KKTV_HOME_MENU[2].setOnClickListener(this);
		KKTV_HOME_MENU[3] = (ImageButton)findViewById(R.id.home_three);
		KKTV_HOME_MENU[3].setOnClickListener(this);
		KKTV_HOME_MENU[4] = (ImageButton)findViewById(R.id.home_four);
		KKTV_HOME_MENU[4].setOnClickListener(this);
		KKTV_HOME_MENU[5] = (ImageButton)findViewById(R.id.home_five);
		KKTV_HOME_MENU[5].setOnClickListener(this);
		KKTV_HOME_MENU[6] = (ImageButton)findViewById(R.id.home_six);
		KKTV_HOME_MENU[6].setOnClickListener(this);
		KKTV_HOME_MENU[7] = (ImageButton)findViewById(R.id.home_senen);
		KKTV_HOME_MENU[7].setOnClickListener(this);
	}

	public static int dip2px(float dpValue) {
		return (int) (dpValue * scale + 0.5f);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.home_first:
			if (flag_from != 1) {
				unCheckIcon(flag_from, 1);
				flag_from = 1;
				Intent first = new Intent();
				first.setClass(this, KKTV_HOME.class);
				startActivity(first);
			}
			break;
		case R.id.home_two:
			if (flag_from != 2) {
				unCheckIcon(flag_from, 2);
				flag_from = 2;
				// 标记为直播电视媒体
				editor.putBoolean("isLiveMedia", true);
				editor.commit();
				Intent two = new Intent();
				two.setClass(this, ChannelTabActivity.class);
				startActivity(two);
			}
			break;
		case R.id.home_three:
			if (flag_from != 3) {
				unCheckIcon(flag_from, 3);
				flag_from = 3;
				// 标记为直播电视媒体
				editor.putBoolean("isLiveMedia", true);
				editor.commit();
				Intent three = new Intent();
				three.setClass(this, FavouriteActivity.class);
				startActivity(three);
			}
			break;
		case R.id.home_four:
			if (flag_from != 4) {
				unCheckIcon(flag_from, 4);
				flag_from = 4;
				// 标记为直播电视媒体
				editor.putBoolean("isLiveMedia", true);
				editor.commit();
				Intent four = new Intent();
				four.setClass(this, UserLoadActivity.class);
				startActivity(four);
			}
			break;
		case R.id.home_five:
			if (flag_from != 5 && checkNetwork()) {
				unCheckIcon(flag_from, 5);
				flag_from = 5;
				Intent five = new Intent();
				five.setClass(this, RadioActivity.class);
				startActivity(five);
			}
			break;
		case R.id.home_six:
			if (flag_from != 6) {
				unCheckIcon(flag_from, 6);
				flag_from = 6;
				Intent six = new Intent();
				six.setClass(this, SetupActivity.class);
				startActivity(six);
			}
			break;
		case R.id.home_senen:
			if (flag_from != 7) {
				unCheckIcon(flag_from, 7);
				flag_from = 7;
				Intent six = new Intent();
				six.setClass(this, ThreeCustomExpand.class);
				startActivity(six);
			}
			break;
		}
	}
	public void unCheckIcon(int pre, int cur){
		KKTV_HOME_MENU[pre].setBackgroundResource(KKTV_HOME_MENU_Normal[pre]);
		KKTV_HOME_MENU[cur].setBackgroundResource(KKTV_HOME_MENU_Select[cur]);
	}
	public void showInfo(int info) {
		Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
	}

	public void showInfo(String info) {
		Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
	}

	// 添加网络检查
	public boolean checkNetwork() {
		if (con == null) {
			con = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
		}
		boolean wifi = con.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
				.isConnectedOrConnecting();
		boolean internet = con.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
				.isConnectedOrConnecting();
		if (!wifi && !internet) {
			showInfo("请检查网络环境，稍后再试");
			return false;
		} else {
			return true;
		}
	}
}
