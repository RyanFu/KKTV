package org.stagex.danmaku.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.stagex.danmaku.adapter.ChannelInfo;

import com.nmbb.oplayer.scanner.ChannelListBusiness;
import com.nmbb.oplayer.scanner.POUserDefChannel;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class BackupData extends AsyncTask<String, Void, Integer> {
	private static final String COMMAND_BACKUP = "backupDatabase";
	public static final String COMMAND_RESTORE = "restoreDatabase";
//	private Context mContext;

//	public BackupData(Context context) {
//		this.mContext = context;
//	}

	public BackupData() {
		
	}
	
	@Override
	protected Integer doInBackground(String... params) {

		String command = params[0];
		if (command.equals(COMMAND_BACKUP)) {

			Log.d("BackupData", "===>begin backup selfdefine fav tvlist");

			File backupFile = new File(
					Environment.getExternalStorageDirectory(),
					"/kekePlayer/selfDefineTVList.txt");
			try {
				FileOutputStream fos = new FileOutputStream(backupFile);
				OutputStreamWriter ow = new OutputStreamWriter(fos, "GBK");
				BufferedWriter bw = new BufferedWriter(ow);
				try {
					// 备份数据库内的自定义的收藏频道
					List<POUserDefChannel> infos = ChannelListBusiness
							.getAllDefFavChannels();
					for (POUserDefChannel info : infos) {
						ArrayList<String> urls = info.getAllUrl();
						for (String url : urls) {
//							bw.write(info.name + "," + url + "\n");
							bw.append(info.name + "," + url + "\n");
						}
					}
					bw.flush();
				} finally {
					bw.close();
					ow.close();
					fos.close();
					Log.d("BackupData",
							"===>backup selfdefine fav tvlist success");
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		} else if (command.equals(COMMAND_RESTORE)) {
			Log.d("BackupData", "===>restore backup selfdefine fav tvlist");
			
			String path = Environment.getExternalStorageDirectory().getPath()
					+ "/kekePlayer/selfDefineTVList.txt";
			File listFile = new File(path);
			if (listFile.exists()) {
				List<ChannelInfo> infos = ParseUtil.parseDef(path);
				// 重新创建自定义收藏频道数据库表格
				try {
					ChannelListBusiness.buildSeflDefDatabase(infos);
					Log.d("BackupData",
							"===>restore selfdefine fav tvlist success");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return null;
		} else {
			Log.e("BackupData", "===>invalide command");
			return null;
		}
	}
}
