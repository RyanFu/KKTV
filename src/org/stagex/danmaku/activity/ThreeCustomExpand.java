package org.stagex.danmaku.activity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.keke.player.R;
import org.stagex.danmaku.adapter.ThreeCustomExpandableAdapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.Toast;

public class ThreeCustomExpand extends Activity implements OnChildClickListener {

	private List<String> groupArray;
	private List<List<String[]>> childArray;
	ThreeCustomExpandableAdapter adapter;
	public ProgressDialog progressDialog;
	ExpandableListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.threecustomexpandablelistview);
		groupArray = new ArrayList<String>();
		childArray = new ArrayList<List<String[]>>();
		int flag = ReadSelfChannel("kekePlayer/three_tvlist.txt");
		if (flag == 1) {
			new InitData().execute();
		}
		listView = (ExpandableListView) findViewById(R.id.expandableListView);
		adapter = new ThreeCustomExpandableAdapter(this, groupArray, childArray);
		listView.setAdapter(adapter);
		listView.setOnChildClickListener(this);
		progressDialog = new ProgressDialog(ThreeCustomExpand.this);
		progressDialog.setMessage("解析中...");
		// progressDialog.setCancelable(false);

	}

	class InitData extends AsyncTask<Void, Void, Void> {
		String urlString = "http://rushplayer.com/wapstream.aspx?v=1.54&t=2&g=278&app=1000";
		String headString = "http://rushplayer.com/";
		String filePathString = "/mnt/sdcard/kekePlayer/three_tvlist.txt";
		InitData() {
		}

		@Override
		protected Void doInBackground(Void... paramArrayOfVoid) {
			try {
				PareChannel(urlString, headString, filePathString);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
//			progressDialog.show();
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			ReadSelfChannel("kekePlayer/three_tvlist.txt");
//			progressDialog.cancel();
		}
	}

	public void PareChannel(String urlString, String headString, String path) {
		try {
			Document doc = Jsoup.connect(urlString).get();
			Element divGroup = doc.getElementById("divGroup");
			if (divGroup != null) {
				File file = new File(path);
				if (!file.exists()) {
					file.createNewFile();
				}else {
					file.delete();
					file.createNewFile();
				}
				FileOutputStream fout = new FileOutputStream(file);
				Elements videos = divGroup.getElementsByTag("a");
				for (Element video : videos) {
					String url = video.attr("href");
					if (url.startsWith("http://")) {
					} else {
						url = headString + url;
					}
					String name = video.text();

					ParseChild(name, url, fout);
				}
				fout.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public boolean isFist = true;
	public void ParseChild(String name1, String urlString, FileOutputStream fout) {
		try {
			Document doc = Jsoup.connect(urlString).get();
			Element contentwrapper = doc.getElementById("contentwrapper");
			if (contentwrapper != null) {
				Elements radios = contentwrapper.getElementsByTag("a");
				for (Element radio : radios) {
					if (!isFist) {
						String url = radio.attr("href");
						String name = name1 + "," + radio.text() + "," + url
								+ "\r\n";
						fout.write(name.getBytes());
					}else {
						isFist = false;
						fout.write("\r\n".getBytes());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int ReadSelfChannel(String filename) {
		String path = null;
		String code = "GBK";
		File file = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
		if (sdCardExist) {
			path = Environment.getExternalStorageDirectory() + "/" + filename;
			file = new File(path);
			if (!file.exists()) {
				Toast.makeText(this, "第一次进行更新中", Toast.LENGTH_LONG).show();
				// finish();
				return 1;
			}
		} else {
			Toast.makeText(this, "请检查SD卡是否存在", Toast.LENGTH_LONG).show();
			finish();
			return -1;
		}
		try {
			// 探测txt文件的编码格式
			code = codeString(path);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try {
			InputStream inStream = new FileInputStream(file);
			if (inStream != null) {
				InputStreamReader inputreader = new InputStreamReader(inStream,
						code);
				BufferedReader buffreader = new BufferedReader(inputreader);
				String line;
				String[] splitArray;
				int lastGroup = 0;
				while ((line = buffreader.readLine()) != null) {
					splitArray = line.split(",");
					if (splitArray != null && splitArray.length == 3) {
						lastGroup = groupArray.size() - 1;
						if (lastGroup == -1) {
							lastGroup = 0;
							groupArray.add(splitArray[0]);
						} else if (groupArray.get(lastGroup).equals(
								splitArray[0])) {
							// 首先检测最后一个类型
						} else {
							lastGroup = CheckGroupName(splitArray[0]);
							if (lastGroup == -1) {
								groupArray.add(splitArray[0]);
								lastGroup = groupArray.size() - 1;
							}
						}
						String[] tempArray = new String[2];
						tempArray[0] = splitArray[1];
						tempArray[1] = splitArray[2];
						List<String[]> temlist;
						int size = childArray.size();
						if (size == 0 || (size - 1) < lastGroup) {
							temlist = new ArrayList<String[]>();
						} else {
							temlist = childArray.get(lastGroup);
							childArray.remove(lastGroup);
						}
						temlist.add(tempArray);
						childArray.add(lastGroup, temlist);
					}
				}
				buffreader.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public int CheckGroupName(String name) {
		int count = groupArray.size();
		for (int i = 0; i < count; i++) {
			if (name.equals(groupArray.get(i))) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 判断文件的编码格式
	 * 
	 * @param fileName
	 *            :file
	 * @return 文件编码格式
	 * @throws Exception
	 */
	private static String codeString(String fileName) throws Exception {
		BufferedInputStream bin = new BufferedInputStream(new FileInputStream(
				fileName));
		int p = (bin.read() << 8) + bin.read();
		String code = null;

		switch (p) {
		case 0xefbb:
			code = "UTF-8";
			break;
		case 0xfffe:
			code = "Unicode";
			break;
		case 0xfeff:
			code = "UTF-16BE";
			break;
		default:
			code = "GBK";
		}

		bin.close();

		Log.d("Parseutil", "find text code ===>" + code);

		return code;
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		String[] channel = childArray.get(groupPosition).get(childPosition);
		ArrayList<String> urlStrings = new ArrayList<String>();
		urlStrings.add(channel[1]);
		startLiveMedia(urlStrings, channel[0]);
		return true;
	}

	private void startLiveMedia(ArrayList<String> liveUrls, String name) {
		Intent intent = new Intent(ThreeCustomExpand.this, PlayerActivity.class);
		intent.putExtra("selected", 0);
		intent.putExtra("playlist", liveUrls);
		intent.putExtra("title", name);
		startActivity(intent);
	}
}