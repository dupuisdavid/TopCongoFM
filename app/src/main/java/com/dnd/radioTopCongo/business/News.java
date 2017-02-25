package com.dnd.radioTopCongo.business;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class News implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int identifier;
	private String publicationDate;
	private String publicationDay;
	private String publicationTime;
	private int categoryId;
	private String categoryName;
	private String pictureThumbW250URL;
	private String pictureThumbW100URL;
	private String title;
	private String contentURL;
	
	public int getIdentifier() {
		return identifier;
	}
	
	public void setIdentifier(int identifier) {
		this.identifier = identifier;
	}
	
	public String getPublicationDate() {
		return publicationDate;
	}
	
	public void setPublicationDate(String publicationDate) {
		this.publicationDate = publicationDate;
	}
	
	public String getPublicationDay() {
		return publicationDay;
	}
	
	public void setPublicationDay(String publicationDay) {
		this.publicationDay = publicationDay;
	}
	
	public String getPublicationTime() {
		return publicationTime;
	}
	
	public void setPublicationTime(String publicationTime) {
		this.publicationTime = publicationTime;
	}
	
	public int getCategoryId() {
		return categoryId;
	}
	
	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}
	
	public String getCategoryName() {
		return categoryName;
	}
	
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	
	public String getPictureThumbW250URL() {
		return pictureThumbW250URL;
	}
	
	public void setPictureThumbW250URL(String pictureThumbW250URL) {
		this.pictureThumbW250URL = pictureThumbW250URL;
	}
	
	public String getPictureThumbW100URL() {
		return pictureThumbW100URL;
	}
	
	public void setPictureThumbW100URL(String pictureThumbW100URL) {
		this.pictureThumbW100URL = pictureThumbW100URL;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getContentURL() {
		return contentURL;
	}
	
	public void setContentURL(String contentURL) {
		this.contentURL = contentURL;
	}

	public News() {
		super();
		this.identifier = 0;
		this.publicationDate = "";
		this.publicationDay = "";
		this.publicationTime = "";
		this.categoryId = 0;
		this.categoryName = "";
		this.pictureThumbW250URL = "";
		this.pictureThumbW100URL = "";
		this.title = "";
		this.contentURL = "";
	}
	
	public News(int identifier, String publicationDate, String publicationDay, String publicationTime, int categoryId, String categoryName, String pictureThumbW250URL, String pictureThumbW100URL, String title, String contentURL) {
		super();
		this.identifier = identifier;
		this.publicationDate = publicationDate;
		this.publicationDay = publicationDay;
		this.publicationTime = publicationTime;
		this.categoryId = categoryId;
		this.categoryName = categoryName;
		this.pictureThumbW250URL = pictureThumbW250URL;
		this.pictureThumbW100URL = pictureThumbW100URL;
		this.title = title;
		this.contentURL = contentURL;
	}
	
	public static News extractNewsDataFromJSONObject(JSONObject JSONNewsData) {
		
		News news = new News();
		
		try {
			
			int identifier = JSONNewsData.getInt("id");
			String publicationDate = JSONNewsData.getString("publicationDate");
			String publicationDay = JSONNewsData.getString("publicationDay");
			String publicationTime = JSONNewsData.getString("publicationTime");
			int categoryId = JSONNewsData.getInt("categoryId");
			String categoryName = JSONNewsData.getString("categoryName");
			String pictureThumbW250URL = JSONNewsData.getString("pictureThumbW250URL");
			String pictureThumbW100URL = JSONNewsData.getString("pictureThumbW100URL");
			String title = JSONNewsData.getString("title");
			String contentURL = JSONNewsData.getString("contentURL");
			
			if (identifier != 0) {
				news.setIdentifier(identifier);
			}
			
			if (publicationDate != null) {
				news.setPublicationDate(publicationDate);
			}
			
			if (publicationDay != null) {
				news.setPublicationDay(publicationDay);
			}
			
			if (publicationTime != null) {
				news.setPublicationTime(publicationTime);
			}
			
			if (categoryId != 0) {
				news.setCategoryId(categoryId);
			}
			
			if (categoryName != null) {
				news.setCategoryName(categoryName);
			}
			
			if (pictureThumbW250URL != null) {
				news.setPictureThumbW250URL(pictureThumbW250URL);
			}
			
			if (pictureThumbW100URL != null) {
				news.setPictureThumbW100URL(pictureThumbW100URL);
			}
			
			if (title != null) {
				news.setTitle(title);
			}
			
			if (contentURL != null) {
				news.setContentURL(contentURL);
			}
    		
    	} catch (JSONException e) {
    		Log.i("JSONException",  "" + e.toString());
    	}

		return news;
	}

	public String toString() {
		return "News {identifier: " + identifier + ", categoryId: " + categoryId + ", title: " + title + ", contentURL: " + contentURL + "}";
	}
	
}
