package org.stagex.danmaku.activity;

import org.keke.player.R;
import org.stagex.danmaku.adapter.FileBrowserAdapter;
import org.stagex.danmaku.util.SystemUtility;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.ListView;

public class FileBrowserActivity extends Activity {
	/** Called when the activity is first created. */

	private ListView mFileList;
	private FileBrowserAdapter mFileAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.file_browser);
		mFileAdapter = new FileBrowserAdapter(this, SystemUtility.getExternalStoragePath());
		mFileList = (ListView) findViewById(R.id.file_list);
		//防止滑动黑屏
		mFileList.setCacheColorHint(Color.TRANSPARENT);
		mFileList.setAdapter(mFileAdapter);
		mFileList.setOnItemClickListener(mFileAdapter);
		mFileList.setOnItemLongClickListener(mFileAdapter);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(mFileAdapter.onKey(null, keyCode, event))
			return true;
		else
			return super.onKeyDown(keyCode, event);
	}
}
