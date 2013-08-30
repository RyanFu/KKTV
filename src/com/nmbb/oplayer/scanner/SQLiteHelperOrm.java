package com.nmbb.oplayer.scanner;

import java.sql.SQLException;

import org.stagex.danmaku.OPlayerApplication;
import org.stagex.danmaku.util.GlobalValue;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class SQLiteHelperOrm extends OrmLiteSqliteOpenHelper {
	private static final String DATABASE_NAME = "kekePlayer.db";
	private static final int DATABASE_VERSION = GlobalValue.dataBaseVerion;	

	public SQLiteHelperOrm(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public SQLiteHelperOrm() {
		super(OPlayerApplication.getContext(), DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, POMedia.class);
			TableUtils.createTable(connectionSource, POChannelList.class);
			TableUtils.createTable(connectionSource, POUserDefChannel.class);
			Log.i(SQLiteHelperOrm.class.getName(), "创建数据库成功！"); 
		} catch (SQLException e) {
			Log.i(SQLiteHelperOrm.class.getName(), "创建数据库失败！", e);  
			e.printStackTrace();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int arg2, int arg3) {
		try {
			TableUtils.dropTable(connectionSource, POMedia.class, true);
			TableUtils.dropTable(connectionSource, POChannelList.class, true);
			TableUtils.dropTable(connectionSource, POUserDefChannel.class, true);
			onCreate(db, connectionSource);
			Log.i(SQLiteHelperOrm.class.getName(), "更新数据库成功！"); 
		} catch (SQLException e) {
			Log.i(SQLiteHelperOrm.class.getName(), "更新数据库失败！", e);
			e.printStackTrace();
		}
	}
}