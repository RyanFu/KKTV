package org.stagex.danmaku.activity;

import java.util.ArrayList;
import java.util.List;

import net.simonvt.menudrawer.MenuDrawer;
import net.simonvt.menudrawer.Position;
import net.simonvt.menudrawer.MenuDrawer.Type;

import org.keke.player.R;
import org.stagex.danmaku.adapter.RadioMenuAdapter;
import org.stagex.danmaku.parser.RadioChannelParse;
import org.stagex.danmaku.type.RadioListType;
import org.stagex.danmaku.type.RadioType;

import cn.waps.AppLog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;

public class RadioActivity extends Activity implements
		RadioMenuAdapter.MenuListener, OnClickListener {

	private static final String radioURL = "http://rushplayer.com/wapstream.aspx?v=1.54&t=1&g=8&app=1000";
	private static final String STATE_ACTIVE_POSITION = "org.stagex.danmaku.activity.RadioActivity.activePosition";
	private static final String STATE_CONTENT_TEXT = "org.stagex.danmaku.activity.RadioActivity.contentText";

	public List<RadioType> ParentChannel;
	public List<RadioListType> channlesList;
	public ListView parentRadioListView;
	public ListView childRadioListView;
	private MenuDrawer mMenuDrawer;
	private RadioMenuAdapter mAdapter, mChildAdapter;
	private int mActivePosition = -1;
	private String mContentText;
	public ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle inState) {
		super.onCreate(inState);
		if (inState != null) {
			mActivePosition = inState.getInt(STATE_ACTIVE_POSITION);
			mContentText = inState.getString(STATE_CONTENT_TEXT);
		}
		mMenuDrawer = MenuDrawer.attach(this, MenuDrawer.Type.BEHIND,
				Position.LEFT, MenuDrawer.MENU_DRAG_CONTENT);
		mMenuDrawer.setContentView(R.layout.radio_channel_content);
		mMenuDrawer.setAllowIndicatorAnimation(true);

		parentRadioListView = new ListView(this);
		parentRadioListView.setCacheColorHint(00000000);
		mAdapter = new RadioMenuAdapter(this);
		mAdapter.setListener(this);
		mAdapter.setActivePosition(mActivePosition);
		parentRadioListView.setAdapter(mAdapter);
		parentRadioListView.setOnItemClickListener(mItemClickListener);

		mMenuDrawer.setMenuSize(BaseActivity.dip2px(150));
		mMenuDrawer.setMenuView(parentRadioListView);

		findViewById(R.id.back_btn).setOnClickListener(this);
		childRadioListView = (ListView) findViewById(R.id.radio_channel_listview);
		childRadioListView.setOnItemClickListener(childClickListener);
		mChildAdapter = new RadioMenuAdapter(this);
		childRadioListView.setAdapter(mChildAdapter);
		childRadioListView.setCacheColorHint(00000000);
		channlesList = new ArrayList<RadioListType>();
		progressDialog = new ProgressDialog(RadioActivity.this);
		progressDialog.setMessage("解析中...");
		progressDialog.setCancelable(false);
		new InitParentChannelData(radioURL).execute();

	}

	public AdapterView.OnItemClickListener childClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			RadioType type = ChildCurrentChannel.get(position);
			AppLog.e("test", type.getName()+"  : "+type.getUrl());
			ArrayList<String> url = new ArrayList<String>();
			url.add(type.getUrl());
			startLiveMedia(url, type.getName());
		}
	};

	private void startLiveMedia(ArrayList<String> liveUrls, String name) {
		Intent intent = new Intent(RadioActivity.this, PlayerActivity.class);
		intent.putExtra("selected", 0);
		intent.putExtra("playlist", liveUrls);
		intent.putExtra("title", name);
		startActivity(intent);
	}

	public AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			RadioType type = ParentChannel.get(position);
			String name = type.getName();
			boolean isHas = false;
			int size = channlesList.size();
			for (int i = 0; i < size; i++) {
				RadioListType listType = channlesList.get(i);
				if (name.equals(listType.getName())) {
					isHas = true;
					mChildAdapter.setListItems(listType.getList());
					break;
				}
			}
			if (!isHas) {
				new InitChildChannelData(type).execute();
			}
			mActivePosition = position;
			mMenuDrawer.setActiveView(view, position);
		}
	};

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_ACTIVE_POSITION, mActivePosition);
		outState.putString(STATE_CONTENT_TEXT, mContentText);
	}

	@Override
	public void onActiveViewChanged(View v) {
		mMenuDrawer.setActiveView(v, mActivePosition);
	}

	public List<RadioType> ChildCurrentChannel;

	class InitChildChannelData extends AsyncTask<Void, Void, Void> {
		RadioType type;

		InitChildChannelData(RadioType type) {
			this.type = type;
		}

		@Override
		protected Void doInBackground(Void... paramArrayOfVoid) {
			try {
				ChildCurrentChannel = RadioChannelParse.parseRadioChild(type
						.getUrl());
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
			RadioListType listType = new RadioListType();
			listType.setName(type.getName());
			listType.setList(ChildCurrentChannel);
			channlesList.add(listType);
			mChildAdapter.setListItems(ChildCurrentChannel);
			if (!mMenuDrawer.isMenuVisible()) {
				mMenuDrawer.openMenu();
			}
			progressDialog.cancel();
		}
	}

	class InitParentChannelData extends AsyncTask<Void, Void, Void> {
		String url;

		InitParentChannelData(String url) {
			this.url = url;
		}

		@Override
		protected Void doInBackground(Void... paramArrayOfVoid) {
			try {
				ParentChannel = RadioChannelParse.parseRadioChannel(url);
				ChildCurrentChannel = RadioChannelParse
						.parseRadioChild(ParentChannel.get(0).getUrl());
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
			RadioListType listType = new RadioListType();
			listType.setName(ParentChannel.get(0).getName());
			listType.setList(ChildCurrentChannel);
			channlesList.add(listType);
			mAdapter.setListItems(ParentChannel);
			mChildAdapter.setListItems(ChildCurrentChannel);
			mMenuDrawer.openMenu();
			progressDialog.cancel();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_btn:
			if (mMenuDrawer.isMenuVisible()) {
				mMenuDrawer.closeMenu();
			} else {
				mMenuDrawer.openMenu();
			}
			break;
		}
	}
}
