package com.syw.SNSsync.oauth.sina;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.syw.SNSsync.SingleTweetListViewAdapter;
import com.syw.SNSsync.entity.Tweet;

/**
 * @author songyouwei
 * 解析新浪json数据的工具类
 */
public class SinaWeiboJsonUtil {
	
	//转换cst用到的类
	private static SimpleDateFormat gmtDateFormat = new SimpleDateFormat("EEE MMM dd hh:mm:ss zzzzz yyyy", Locale.ENGLISH);
	private static SimpleDateFormat cstDateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss", Locale.ENGLISH);
	
	/**
	 * 解析当前登录用户的uid
	 */
	public static String getLoggedInUserId(String jsonsString) {
		try {
			return new JSONObject(jsonsString).getString("uid");
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 解析用户的nick
	 */
	public static String getUserNick(String jsonString) {
		try {
			return new JSONObject(jsonString).getString("name");
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 解析用户头像url
	 */
	public static String getUserHeadImgUrl(String jsonString) {
		try {
			return new JSONObject(jsonString).getString("profile_image_url");
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 解析原创Tweet
	 */
	public static List<Tweet> getOriginalTweets(String jsonString) {
		List<Tweet> tweets = new ArrayList<Tweet>();
		try {
            JSONObject jo = new JSONObject(jsonString);
            JSONArray statusesArray = (JSONArray) jo.get("statuses");
            for (int i = 0; i < statusesArray.length(); ++i) {
                JSONObject status = (JSONObject) statusesArray.get(i);
                JSONObject user = status.getJSONObject("user");
                Tweet tweet = new Tweet(user.getString("name"), status.getString("text"),getCSTDate(status.getString("created_at")));
                //设置id和msgType
                tweet.setId(status.getString("id"));
                tweet.setMsgType(SingleTweetListViewAdapter.MSG_TYPE_AUTHOR);
                //设置userHeadImgUrl
                tweet.setUserHeadImgUrl(user.getString("profile_image_url"));
                //加入集合
                tweets.add(tweet);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
		return tweets;
	}
	
	/**
	 * 解析评论
	 */
	public static List<Tweet> getComments(String jsonString) {
		List<Tweet> comments = new ArrayList<Tweet>();
		try {
			JSONArray commentsArray = new JSONObject(jsonString).getJSONArray("comments");
			//时间不是倒序
			for (int i = 0; i < commentsArray.length() ; i++) {
				JSONObject commentObject = (JSONObject) commentsArray.get(i);
				JSONObject userObject = commentObject.getJSONObject("user");
				Tweet comment = new Tweet(userObject.getString("name"), commentObject.getString("text"), getCSTDate(commentObject.getString("created_at")));
				comment.setId(commentObject.getString("id"));
				comment.setFid(commentObject.getJSONObject("status").getString("id"));
				comment.setMsgType(SingleTweetListViewAdapter.MSG_TYPE_COMMENTER);
				//设置头像
				comment.setUserHeadImgUrl(userObject.getString("profile_image_url"));
				
				comments.add(comment);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return comments;
	}
	
	/**
	 * 将GMT时间转化为标准格式
	 */
	private static String getCSTDate(String gmtDatetime) {
		try {
			return cstDateFormat.format(gmtDateFormat.parse(gmtDatetime));
		} catch (ParseException e) {
			e.printStackTrace();
			return gmtDatetime;
		}
	}
}
