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

	public ChannelDefFavAdapter(Context context, List<POUserDefChannel> infos) {
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
		TextView data_txt = (TextView)view.findViewById(R.id.save_date);
		text.setText(infos.get(position).name);
		data_txt.setText(infos.get(position).date);
		data_txt.setVisibility(View.VISIBLE);

		return view;
	}
}
