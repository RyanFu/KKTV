package org.stagex.danmaku.adapter;

import java.util.ArrayList;

import org.keke.player.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ProgramAdapter extends BaseAdapter {
	private static final String LOGTAG = "ProgramAdapter";
	private ArrayList<ProgramInfo> infos;
	private Context mContext;

	public ProgramAdapter(Context context, ArrayList<ProgramInfo> infos) {
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
		// Log.d(LOGTAG, infos.get(position));
		// TODO Auto-generated method stub
		View view = View.inflate(mContext, R.layout.program_list_item, null);
		TextView text1 = (TextView) view.findViewById(R.id.time);
		TextView text2 = (TextView) view.findViewById(R.id.program);

		text1.setText(infos.get(position).getTime());
		text2.setText(infos.get(position).getProgram());

		if (infos.get(position).getCurProgram()) {
			text1.setTextColor(mContext.getResources().getColor(R.color.green));
			text2.setTextColor(mContext.getResources().getColor(R.color.green));
		}

		return view;
	}

}
