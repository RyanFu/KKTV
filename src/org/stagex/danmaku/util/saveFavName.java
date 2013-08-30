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

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.nmbb.oplayer.scanner.ChannelListBusiness;
import com.nmbb.oplayer.scanner.POChannelList;
import com.nmbb.oplayer.scanner.POUserDefChannel;

public class saveFavName extends AsyncTask<String, Void, Integer> {
	private static final String COMMAND_BACKUP = "backupDatabase";
	public static final String COMMAND_RESTORE = "restoreDatabase";
//	private Context mContext;

//	public BackupData(Context context) {
//		this.mContext = context;
//	}

	public saveFavName() {
		
	}
	
	@Override
	protected Integer doInBackground(String... params) {

		String command = params[0];
		if (command.equals(COMMAND_BACKUP)) {

			Log.d("BackupData", "===>begin backup fav tv name");

			File backupFile = new File(
					Environment.getExternalStorageDirectory(),
					"/kekePlayer/.favName.txt");
			try {
				FileOutputStream fos = new FileOutputStream(backupFile);
				OutputStreamWriter ow = new OutputStreamWriter(fos, "UTF-8");
				BufferedWriter bw = new BufferedWriter(ow);
				try {
					// 备份数据库内的收藏频道节目名称
					List<POChannelList> infos = ChannelListBusiness
							.getAllFavChannels();
					for (POChannelList info : infos) {
						ArrayList<String> urls = info.getAllUrl();
							bw.append(info.name +"\n");
					}
					bw.flush();
				} finally {
					bw.close();
					ow.close();
					fos.close();
					Log.d("BackupData",
							"===>backup fav tv name success");
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
			Log.d("BackupData", "===>restore fav tv name");
			
			String path = Environment.getExternalStorageDirectory().getPath()
					+ "/kekePlayer/.favName.txt";
			File listFile = new File(path);
			if (listFile.exists()) {
				List<String> nameList = ParseUtil.parseName(path);
				// 重新创建自定义收藏频道数据库表格
				try {
					ChannelListBusiness.feedBackNameFavChannel(nameList);
					Log.d("BackupData",
							"===>restore fav tv name success");
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
