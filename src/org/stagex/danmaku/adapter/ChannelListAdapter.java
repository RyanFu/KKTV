package org.stagex.danmaku.adapter;

import java.util.List;

import org.keke.player.R;
import com.nmbb.oplayer.scanner.POChannelList;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ChannelListAdapter extends BaseAdapter {
	private List<POChannelList> infos;
	private Context mContext;

	public ChannelListAdapter(Context context, List<POChannelList> infos) {
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
		View view = View.inflate(mContext, R.layout.channel_list_item2, null);
		TextView textName = (TextView) view.findViewById(R.id.channel_name);
		TextView textIndex = (TextView) view.findViewById(R.id.channel_index);
		ImageView hotView = (ImageView) view.findViewById(R.id.hot_icon);
		ImageView newView = (ImageView) view.findViewById(R.id.new_icon);
		
		textName.setText(infos.get(position).name);
		textIndex.setText(Integer.toString(position + 1));
//		Log.d("channelList", "===> list position" + position);
		
		// 判断是否是热门频道，暂时使用HOT字样
		if (infos.get(position).mode.equalsIgnoreCase("HOT"))
			hotView.setVisibility(View.VISIBLE);
		// 判断是否是新频道，暂时用NEW字样
		if (infos.get(position).mode.equalsIgnoreCase("NEW"))
			newView.setVisibility(View.VISIBLE);

		return view;
	}
}
