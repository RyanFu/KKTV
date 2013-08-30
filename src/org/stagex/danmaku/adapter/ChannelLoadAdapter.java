package org.stagex.danmaku.adapter;

import java.util.List;

import org.keke.player.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ChannelLoadAdapter extends BaseAdapter {
	private List<ChannelInfo> infos;
	private Context mContext;

	public ChannelLoadAdapter(Context context, List<ChannelInfo> infos) {
		this.infos = infos;
		this.mContext = context;
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
		View view = View.inflate(mContext, R.layout.user_load_item, null);
		TextView text = (TextView) view.findViewById(R.id.channel_name);
//		ImageView imageView = (ImageView) view.findViewById(R.id.channel_icon);
		text.setText(infos.get(position).getName());

		return view;
	}
}
