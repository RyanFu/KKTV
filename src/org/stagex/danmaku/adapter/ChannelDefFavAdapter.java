package org.stagex.danmaku.adapter;

import java.util.List;

import org.keke.player.R;

import com.nmbb.oplayer.scanner.POUserDefChannel;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ChannelDefFavAdapter extends BaseAdapter {
	private List<POUserDefChannel> infos;
	private Context mContext;
	// 判断是否是在播放界面切台
	private Boolean isAtPlaylist;

	public ChannelDefFavAdapter(Context context, List<POUserDefChannel> infos, Boolean isAtPlaylist) {
		this.infos = infos;
		this.mContext = context;
		this.isAtPlaylist = isAtPlaylist;
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
		//TODO 两种模式，如果在列表界面，则加载user_load_item.xml
		// 如果在播放界面，则加载透明的user_load_item2.xml
		if (isAtPlaylist) {
			View view = View.inflate(mContext, R.layout.user_load_item2, null);
			TextView text = (TextView) view.findViewById(R.id.channel_name);
			TextView textIndex = (TextView) view.findViewById(R.id.channel_index);
			textIndex.setText(Integer.toString(position + 1));
			text.setText(infos.get(position).name);
			return view;
		} else {
			View view = View.inflate(mContext, R.layout.user_load_item, null);
			TextView text = (TextView) view.findViewById(R.id.channel_name);
			TextView data_txt = (TextView)view.findViewById(R.id.save_date);
			text.setText(infos.get(position).name);
			data_txt.setText(infos.get(position).date);
			data_txt.setVisibility(View.VISIBLE);
			return view;
		}
	}
}
