/**
 * 
 */
package com.syw.SNSsync.oauth.renren;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.bool;

import com.syw.SNSsync.SingleTweetListViewAdapter;
import com.syw.SNSsync.entity.Tweet;

/**
 * @author songyouwei
 * 解析人人JSON数据的工具类
 */
public class RenrenJsonUtil {
	
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
	public static String getUserNick(String jsonsString) {
		try {
			return new JSONObject(jsonsString).getString("name");
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 解析用户head
	 */
	public static String getUserHeadImgUrl(String jsonString) {
		try {
			return new JSONObject(jsonString).getString("headurl");
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 原创
	 */
	public static List<Tweet> getOriginalTweets(String jsonString) {
		List<Tweet> tweets = new ArrayList<Tweet>();
		try {
            JSONArray statusesArray = new JSONArray(jsonString);
            for (int i = 0; i < statusesArray.length(); ++i) {
                JSONObject status = (JSONObject) statusesArray.get(i);
                Tweet tweet = new Tweet(status.getString("name"),status.getString("message"),status.getString("update_time"));
                //设置id、ownerId和msgType
                tweet.setId(status.getString("source_id"));
                tweet.setOwnerId(status.getString("actor_id"));
                tweet.setMsgType(SingleTweetListViewAdapter.MSG_TYPE_AUTHOR);
                //头像
                tweet.setUserHeadImgUrl(status.getString("headurl"));
                
                //放入集合
                tweets.add(tweet);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
		return tweets;
	}
	
	/**
	 *  解析回复
	 */
	public static List<Tweet> getComments(String jsonString) {
		List<Tweet> comments = new ArrayList<Tweet>();
		try {
			JSONArray commentsArray = new JSONArray(jsonString);
			for (int i = 0; i < commentsArray.length(); i++) {
				JSONObject commentObject = (JSONObject) commentsArray.get(i);
				Tweet comment = new Tweet(commentObject.getString("name"), commentObject.getString("text"), commentObject.getString("time"));
				comment.setMsgType(SingleTweetListViewAdapter.MSG_TYPE_COMMENTER);
				//头像
				comment.setUserHeadImgUrl(commentObject.getString("tinyurl"));
				
				comments.add(comment);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return comments;
	} 
	
	/**
	 * 判断发布的信息是否成功，成功返回1，否则返回-1
	 */
	public static int publishResult(String jsonString) {
		int returnResult = 0;
		try {
			JSONObject result = new JSONObject(jsonString);
			returnResult = (result.getInt("result")==1?1:-1);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return returnResult;
	}
	
}
