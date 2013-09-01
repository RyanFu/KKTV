package org.stagex.danmaku.activity;

import org.keke.player.R;
import org.stagex.danmaku.imageloader.AbsListViewBaseActivity;

import net.simonvt.menudrawer.MenuDrawer;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class BaseActivity extends AbsListViewBaseActivity implements OnClickListener {

	public MenuDrawer mMenuDrawer;
	private float scale;
	public static final int leftMarge = 70;
	public static int flag_from = 1;

	private SharedPreferences sharedPreferences;
	private Editor editor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPreferences = getSharedPreferences("keke_player", MODE_PRIVATE);
		editor = sharedPreferences.edit();
	}

	public void init(int parent, int menu) {
		mMenuDrawer = MenuDrawer.attach(this, MenuDrawer.MENU_DRAG_WINDOW);
		mMenuDrawer.setContentView(parent);
		mMenuDrawer.setMenuView(R.layout.kktv_drawmenu);
		scale = getResources().getDisplayMetrics().density;
		mMenuDrawer.setMenuSize(dip2px(leftMarge));
		initLayout();
		mMenuDrawer.openMenu();
	}

	public void initLayout(){
		findViewById(R.id.home_first).setOnClickListener(this);
		findViewById(R.id.home_two).setOnClickListener(this);
		findViewById(R.id.home_three).setOnClickListener(this);
		findViewById(R.id.home_four).setOnClickListener(this);
		findViewById(R.id.home_five).setOnClickListener(this);
	}
	public int dip2px(float dpValue) {
		return (int) (dpValue * scale + 0.5f);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.home_first:
			if (flag_from != 1) {
				flag_from = 1;
				Intent first = new Intent();
				first.setClass(this, KKTV_HOME.class);
				startActivity(first);
			}
			break;
		case R.id.home_two:
			if (flag_from != 2) {
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
			if (flag_from != 5) {
				flag_from = 5;
				Intent five = new Intent();
				five.setClass(this, SetupActivity.class);
				startActivity(five);
			}
			break;
		}
//		mMenuDrawer.closeMenu();
	}

	public void showInfo(int info) {
		Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
	}

	public void showInfo(String info) {
		Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
	}
}
