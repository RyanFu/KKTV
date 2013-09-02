package org.stagex.danmaku.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import org.keke.player.R;
import org.stagex.danmaku.type.RadioType;

public class RadioMenuAdapter extends BaseAdapter {

	public interface MenuListener {

		void onActiveViewChanged(View v);
	}

	private Context mContext;

	private List<RadioType> mItems;

	private MenuListener mListener;

	private int mActivePosition = -1;

	public RadioMenuAdapter(Context convext) {
		mContext = convext;
	}

	public RadioMenuAdapter(Context context, List<RadioType> items) {
		mContext = context;
		mItems = items;
	}

	public void setListener(MenuListener listener) {
		mListener = listener;
	}

	public void setActivePosition(int activePosition) {
		mActivePosition = activePosition;
	}

	public void setListItems(List<RadioType> mItems) {
		this.mItems = mItems;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (mItems != null) {
			return mItems.size();
		} else {
			return 0;
		}

	}

	@Override
	public Object getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

//	@Override
//	public int getItemViewType(int position) {
//		return getItem(position) instanceof Item ? 0 : 1;
//	}

//	@Override
//	public int getViewTypeCount() {
//		return 1;
//	}

//	@Override
//	public boolean isEnabled(int position) {
//		return getItem(position) instanceof Item;
//	}
//
//	@Override
//	public boolean areAllItemsEnabled() {
//		return false;
//	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.menu_row_item, null);
		}
		TextView tv = (TextView) convertView;
		tv.setText(mItems.get(position).getName());
		tv.setTextColor(mContext.getResources().getColor(R.color.white));
		convertView.setTag(R.id.mdActiveViewPosition, position);

		if (position == mActivePosition) {
			mListener.onActiveViewChanged(convertView);
		}

		return convertView;
	}
}
