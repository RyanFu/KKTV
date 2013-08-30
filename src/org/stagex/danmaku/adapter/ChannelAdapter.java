package org.stagex.danmaku.adapter;

import java.util.List;

import org.keke.player.R;
import org.stagex.danmaku.activity.TvProgramActivity;

import com.fedorvlasov.lazylist.ImageLoader;
import com.nmbb.oplayer.scanner.POChannelList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ChannelAdapter extends BaseAdapter {
	private List<POChannelList> infos;
	private Context mContext;

	// 自定义的img加载类，提升加载性能，防止OOM
	public ImageLoader imageLoader;

	public ChannelAdapter(Context context, List<POChannelList> infos) {
		this.infos = infos;
		this.mContext = context;

		imageLoader = new ImageLoader(context);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return infos.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return infos.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view = View.inflate(mContext, R.layout.channel_list_item, null);
		TextView text = (TextView) view.findViewById(R.id.channel_name);
		ImageView imageView = (ImageView) view.findViewById(R.id.channel_icon);
		ImageView hotView = (ImageView) view.findViewById(R.id.hot_icon);
		ImageView newView = (ImageView) view.findViewById(R.id.new_icon);
		final ImageView favView = (ImageView) view.findViewById(R.id.fav_icon);

		// TODO 节目预告
		LinearLayout programView = (LinearLayout) view
				.findViewById(R.id.program_icon);
		programView.setTag(position);
		programView.setOnClickListener(new ImageView.OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// Toast.makeText(
				// mContext,
				// "您单击了[" + infos.get((Integer) v.getTag()).getName()
				// + "]", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(mContext, TvProgramActivity.class);
				String channel_name = infos.get((Integer) v.getTag()).name;
				String program_path = infos.get((Integer) v.getTag()).program_path;
				if (program_path == null) {
					new AlertDialog.Builder(mContext)
							.setIcon(R.drawable.ic_dialog_alert)
							.setTitle("警告")
							.setMessage("暂时没有该电视台的节目预告！")
							.setPositiveButton("知道了",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											// do nothing - it will close on
											// its own
										}
									}).show();
				} else {
					intent.putExtra("ProgramPath", program_path);
					intent.putExtra("ChannelName", channel_name);
					mContext.startActivity(intent);
				}
			}
		});
		
		// 是否显示已收藏图标
		if (infos.get(position).save)
			favView.setVisibility(View.VISIBLE);
		
		text.setText(infos.get(position).name);
		// 判断是否是热门频道，暂时使用HOT字样
		if (infos.get(position).mode.equalsIgnoreCase("HOT"))
			hotView.setVisibility(View.VISIBLE);
		// 判断是否是新频道，暂时用NEW字样
		if (infos.get(position).mode.equalsIgnoreCase("NEW"))
			newView.setVisibility(View.VISIBLE);

		// FIXME 添加对togic的部分图标的url无hostname的支持
		String iconUrl = infos.get(position).icon_url;
		if (iconUrl.startsWith("/upload")) {
			// add "http://tv.togic.com"
			StringBuffer urlBuf = new StringBuffer();
			urlBuf.append("http://tv.togic.com");
			urlBuf.append(iconUrl);
			// TODO 新方法，防止OOM
			imageLoader.DisplayImage(urlBuf.toString(), null, imageView);
		} else {
			// TODO 新方法，防止OOM
			imageLoader.DisplayImage(iconUrl, null, imageView);
		}

		return view;
	}
}
