package com.nmbb.oplayer.scanner;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.stagex.danmaku.adapter.ChannelInfo;
import org.stagex.danmaku.util.Logger;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

public final class ChannelListBusiness {
	private static final String TABLE_NAME = "channeLlist";
	private static final String TAG = "ChannelListBusiness";

	// 找出所有的收藏频道
	public static List<POChannelList> getAllFavChannels() {
		SQLiteHelperOrm db = new SQLiteHelperOrm();
		try {
			Dao<POChannelList, Long> dao = db.getDao(POChannelList.class);
			return dao.queryForEq("save", true);
		} catch (SQLException e) {
			Logger.e(e);
		} finally {
			if (db != null)
				db.close();
		}
		return new ArrayList<POChannelList>();
	}

	// 清除所有的数据
	public static void clearAllOldDatabase() {
		SQLiteHelperOrm db = new SQLiteHelperOrm();
		// FIXME 此种方式，效率很高，很快，直接删除所有行数据
		db.getWritableDatabase().delete("channeLlist", null, null);
		if (db != null)
			db.close();
	}

	// 获取所有模糊查询的频道
	public static List<POChannelList> getAllSearchChannels(String columnName,
			String name) {
		SQLiteHelperOrm db = new SQLiteHelperOrm();
		try {
			Dao<POChannelList, Long> dao = db.getDao(POChannelList.class);
			QueryBuilder<POChannelList, Long> query = dao.queryBuilder();
			return query.where().like(columnName, "%" + name + "%").query();
		} catch (SQLException e) {
			Logger.e(e);
		} finally {
			if (db != null)
				db.close();
		}
		return new ArrayList<POChannelList>();
	}

	// 建立数据库所有数据
	public static void buildDatabase(List<ChannelInfo> List) throws Exception {
		SQLiteHelperOrm db = new SQLiteHelperOrm();
		final List<ChannelInfo> channelList = List;
		try {
			final Dao<POChannelList, Long> dao = db.getDao(POChannelList.class);
			// TODO 采用ormlite的事务方式，能够极大的提高数据库的操作效率
			dao.callBatchTasks(new Callable<Void>() {
				public Void call() throws SQLException {
					// insert a number of accounts at once
					for (ChannelInfo info : channelList) {
						// update our account object
						dao.create(new POChannelList(info, false));
					}
					return null;
				}
			});
		} catch (SQLException e) {
			Logger.e(e);
		} finally {
			if (db != null)
				db.close();
		}
	}

	// 将收藏的频道写回新数据库
	public static void feedBackFavChannel(List<POChannelList> List)
			throws Exception {
		SQLiteHelperOrm db = new SQLiteHelperOrm();
		final List<POChannelList> channelList = List;
		try {
			final Dao<POChannelList, Long> dao = db.getDao(POChannelList.class);
			// TODO 采用ormlite的事务方式，能够极大的提高数据库的操作效率
			dao.callBatchTasks(new Callable<Void>() {
				public Void call() throws SQLException {
					// insert a number of accounts at once
					for (POChannelList info : channelList) {
						List<POChannelList> newChannelList = dao.queryForEq(
								"name", info.name);
						if (newChannelList.size() > 0) {
							newChannelList.get(0).save = true;
							// update our account object
							dao.update(newChannelList.get(0));
						}
					}
					return null;
				}
			});
		} catch (SQLException e) {
			Logger.e(e);
		} finally {
			if (db != null)
				db.close();
		}
	}
	
	// 根据存储的名称将收藏的频道写回新数据库
	public static void feedBackNameFavChannel(List<String> name)
			throws Exception {
		SQLiteHelperOrm db = new SQLiteHelperOrm();
		final List<String> nameList = name;
		try {
			final Dao<POChannelList, Long> dao = db.getDao(POChannelList.class);
			// TODO 采用ormlite的事务方式，能够极大的提高数据库的操作效率
			dao.callBatchTasks(new Callable<Void>() {
				public Void call() throws SQLException {
					// insert a number of accounts at once
					for (String name : nameList) {
						List<POChannelList> newChannelList = dao.queryForEq(
								"name", name);
						if (newChannelList.size() > 0) {
							newChannelList.get(0).save = true;
							// update our account object
							dao.update(newChannelList.get(0));
						}
					}
					return null;
				}
			});
		} catch (SQLException e) {
			Logger.e(e);
		} finally {
			if (db != null)
				db.close();
		}
	}
	
	// 找出所有的自定义的收藏频道
	public static List<POUserDefChannel> getAllDefFavChannels() {
		SQLiteHelperOrm db = new SQLiteHelperOrm();
		try {
			Dao<POUserDefChannel, Long> dao = db.getDao(POUserDefChannel.class);
			return dao.queryForAll();
		} catch (SQLException e) {
			Logger.e(e);
		} finally {
			if (db != null)
				db.close();
		}
		return new ArrayList<POUserDefChannel>();
	}
	
	// 建立自定义收藏数据库所有数据
	public static void buildSeflDefDatabase(List<ChannelInfo> List) throws Exception {
		SQLiteHelperOrm db = new SQLiteHelperOrm();
		final List<ChannelInfo> channelList = List;
		try {
			final Dao<POUserDefChannel, Long> dao = db.getDao(POUserDefChannel.class);
			// TODO 采用ormlite的事务方式，能够极大的提高数据库的操作效率
			dao.callBatchTasks(new Callable<Void>() {
				public Void call() throws SQLException {
					// insert a number of accounts at once
					for (ChannelInfo info : channelList) {
						// update our account object
						dao.create(new POUserDefChannel(info, true));
					}
					return null;
				}
			});
		} catch (SQLException e) {
			Logger.e(e);
		} finally {
			if (db != null)
				db.close();
		}
	}
}
