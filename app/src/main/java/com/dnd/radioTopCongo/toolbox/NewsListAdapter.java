package com.dnd.radioTopCongo.toolbox;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dnd.radioTopCongo.R;
import com.dnd.radioTopCongo.business.News;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class NewsListAdapter extends BaseAdapter {

	private final Context context;
		private ArrayList<News> news;
		private LayoutInflater layoutInflater;
		private Boolean imageViewFadeRefreshEnable = true;
		private Boolean manageFocusNewsDisplay = false;

		private ImageLoader imageLoader;
		private DisplayImageOptions options;
		
		public NewsListAdapter(Context context, ArrayList<News> news, Boolean manageFocusNewsDisplay) {
			this.context = context;
			this.layoutInflater = LayoutInflater.from(context);
			this.news = news;
			this.manageFocusNewsDisplay = manageFocusNewsDisplay;
			
			// https://github.com/nostra13/Android-Universal-Image-Loader
			
			this.options = new DisplayImageOptions.Builder()
				.cacheInMemory(true)
				.cacheOnDisk(true)
				.considerExifParams(true)
				.build();
			
			this.imageLoader = ImageLoader.getInstance();
		}
		
		public Boolean getImageViewFadeRefreshEnable() {
			return imageViewFadeRefreshEnable;
		}

		public void setImageViewFadeRefreshEnable(Boolean imageViewFadeRefreshEnable) {
			this.imageViewFadeRefreshEnable = imageViewFadeRefreshEnable;
		}

		@Override
		public int getCount() {
			return this.news.size();
		}

		@Override
		public Object getItem(int index) {
			return this.news.get(index);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		private class ViewHolder {
			ImageView imageView;
			TextView titleTextView;
			TextView categoryNameTextView;
			TextView publicationDateTextView;
			News news;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			
			if (convertView == null) {
				convertView = layoutInflater.inflate(R.layout.news_list_item, parent, false);
				holder = new ViewHolder();
				
				holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
				holder.titleTextView = (TextView) convertView.findViewById(R.id.titleTextView);
				holder.categoryNameTextView = (TextView) convertView.findViewById(R.id.categoryNameTextView);
				holder.publicationDateTextView = (TextView) convertView.findViewById(R.id.publicationDateTextView);
				
				convertView.setTag(holder);
				
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			
			float devicePixelDensity = DisplayProperties.getInstance((Activity) context).getPixelDensity();

			// ADJUST ROW HEIGHT
			
			AbsListView.LayoutParams newsListItemLayoutParams = (AbsListView.LayoutParams) convertView.getLayoutParams();
//			int height = manageFocusNewsDisplay && position == 0 ? 90 : 45;
			int height = manageFocusNewsDisplay && position == 0 ? 90 : 45;
			height = (int) (((float) height) * devicePixelDensity);
			newsListItemLayoutParams.height = height;
			convertView.setLayoutParams(newsListItemLayoutParams);
			convertView.invalidate();
			
			// ADJUST IMAGEVIEW WIDTH
			
			RelativeLayout.LayoutParams imageViewLayoutParams = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
			int imageViewWidth = manageFocusNewsDisplay && position == 0 ? 125 : 50;
			imageViewWidth = (int) (((float) imageViewWidth) * devicePixelDensity);
			imageViewLayoutParams.width = imageViewWidth;
			holder.imageView.setLayoutParams(imageViewLayoutParams);
			holder.imageView.invalidate();
			
			// ADJUST TITLE MARGIN-BOTTOM
			
			RelativeLayout.LayoutParams titleTextViewLayoutParams = (RelativeLayout.LayoutParams) holder.titleTextView.getLayoutParams();
			titleTextViewLayoutParams.setMargins(0, 0, 0, (manageFocusNewsDisplay && position == 0 ? -2 : -4));
			holder.titleTextView.setLayoutParams(titleTextViewLayoutParams);
			holder.titleTextView.invalidate();
			
			// ADJUST TITLE FONT-SIZE AND MAX-LINES
			
			holder.titleTextView.setTextSize(manageFocusNewsDisplay && position == 0 ? 15.0f : 12.0f);
			holder.titleTextView.setMaxLines(manageFocusNewsDisplay && position == 0 ? 4 : 2);
			holder.titleTextView.invalidate();
			holder.titleTextView.requestLayout();
			
			
			// GET NEWS DATA
			
			News news = this.news.get(position);
			final Boolean refreshNeeded = !news.equals(holder.news);
			
//			Log.i("HOLDER", "" + news + ", " + (holder.news != null ? holder.news : "null") + ", refresh needed : " + refreshNeeded);
			
//			if (refreshNeeded) {
				
				holder.news = news;
//				Log.i("position", "position : " + position);
				
				if (imageViewFadeRefreshEnable && refreshNeeded) {
					holder.imageView.setImageBitmap(null);
					holder.imageView.setAlpha(0f);
				}
				
				holder.titleTextView.setText(news.getTitle());
				holder.categoryNameTextView.setText(news.getCategoryName());
				holder.publicationDateTextView.setText(("le " + news.getPublicationDay()));
				
				String imageURL = manageFocusNewsDisplay && position == 0 ? news.getPictureThumbW250URL() : news.getPictureThumbW100URL();
				final Drawable noImageDrawable = context.getResources().getDrawable(manageFocusNewsDisplay && position == 0 ? R.drawable.news_cell_no_picture_focus_image : R.drawable.news_cell_no_picture_image);
//				Log.i("imageURL", "imageURL : x" + imageURL + "x");
				
				if (!imageURL.isEmpty()) {
					imageLoader.displayImage(imageURL, holder.imageView, options, new ImageLoadingListener() {
						@Override
						public void onLoadingStarted(String arg0, View arg1) {
							
						}
						
						@Override
						public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
							holder.imageView.setImageBitmap(((BitmapDrawable) noImageDrawable).getBitmap());
							if (imageViewFadeRefreshEnable && refreshNeeded) {
								holder.imageView.animate().alpha(1.0f).setDuration(350);
							} else {
								holder.imageView.setAlpha(1.0f);
							}
						}
						
						@Override
						public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
							if (imageViewFadeRefreshEnable && refreshNeeded) {
								holder.imageView.animate().alpha(1f).setDuration(350);
							} else {
								holder.imageView.setAlpha(1.0f);
							}
						}
						
						@Override
						public void onLoadingCancelled(String arg0, View arg1) {
							
						}
					});
					
				} else {
					
					holder.imageView.setImageBitmap(((BitmapDrawable) noImageDrawable).getBitmap());
					if (imageViewFadeRefreshEnable) {
						holder.imageView.animate().alpha(1f).setDuration(350);
					}
					
				}
//			}



			return convertView;
		}

}
