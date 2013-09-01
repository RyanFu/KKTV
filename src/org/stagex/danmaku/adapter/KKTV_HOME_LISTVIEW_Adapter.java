package org.stagex.danmaku.adapter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.keke.player.R;
import org.stagex.danmaku.type.Home_List_type;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class KKTV_HOME_LISTVIEW_Adapter extends BaseAdapter {

	public List<Home_List_type> listItmes;
	private LayoutInflater inflater;
	DisplayImageOptions options;
	ImageLoader imageLoader;
	public Context context;
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

	public KKTV_HOME_LISTVIEW_Adapter(Context context, ImageLoader imageLoader,
			List<Home_List_type> list) {
		inflater = LayoutInflater.from(context);
		options = new DisplayImageOptions.Builder()
				.showStubImage(R.drawable.television_icon)
				.showImageForEmptyUri(R.drawable.ic_empty)
				.showImageOnFail(R.drawable.ic_error).cacheInMemory(true)
				.cacheOnDisc(true).displayer(new RoundedBitmapDisplayer(20))
				.build();
		this.context = context;
		this.imageLoader = imageLoader;
		listItmes = list;
	}

	@Override
	public int getCount() {
		if (listItmes != null && listItmes.size() != 0) {
			return listItmes.size();
		} else {
			return 0;
		}
	}

	@Override
	public Object getItem(int position) {
		return listItmes.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		int type = listItmes.get(position).getId();
		if (type == 1) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = inflater.inflate(R.layout.kktv_home_listview_item,
					null);
			viewHolder.img = (ImageView) convertView
					.findViewById(R.id.kktv_home_item_img);
			viewHolder.title = (TextView) convertView
					.findViewById(R.id.kktv_home_item_title);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		Home_List_type type = listItmes.get(position);
		if (type.getId() == 1) {
			viewHolder.title.setText(type.getType());
		} else {
			viewHolder.title.setText(type.getName());
		}
		imageLoader.displayImage(type.getImg(), viewHolder.img, options, animateFirstListener);
		return convertView;
	}

	private static class AnimateFirstDisplayListener extends
			SimpleImageLoadingListener {
		static final List<String> displayedImages = Collections
				.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
			}
		}
	}

	class ViewHolder {
		ImageView img;
		TextView title;
	}

}
