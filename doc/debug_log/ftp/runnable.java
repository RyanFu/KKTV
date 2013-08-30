	/* 处理FTP下载的线程 */
	private Handler mHander = new Handler();

	private final Runnable mRunnable = new Runnable() {

		public void run() {
			// 到远程服务器下载直播电视播放列表
			tvPlaylistDownload();

			isTVListSuc = sharedPreferences.getBoolean("isTVListSuc", false);

			/* TODO 停止旋转（时间可能很短，来不及显示，就停下来了） */
//			button_refresh.clearAnimation();
			onRefreshEnd();

			if (isTVListSuc) {
				// 更新界面的节目表list
				RefreshList();
				
				Log.d(LOGTAG, "===> 6");
				
				// 弹出加载【成功】对话框
				if (ChannelTabActivity.this == null)
					return;
				new AlertDialog.Builder(ChannelTabActivity.this)
						.setTitle("更新成功")
						.setMessage("服务器地址更新成功")
						.setNegativeButton("知道了",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// do nothing - it will close on its
										// own
									}
								}).show();
			} else {
				// 弹出加载【失败】对话框
				if (ChannelTabActivity.this == null)
					return;
				new AlertDialog.Builder(ChannelTabActivity.this)
						.setTitle("更新失败")
						.setMessage("抱歉！服务器地址更新失败\n默认使用初始节目地址")
						.setNegativeButton("知道了",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// do nothing - it will close on its
										// own
									}
								}).show();
			}
		}
	};
