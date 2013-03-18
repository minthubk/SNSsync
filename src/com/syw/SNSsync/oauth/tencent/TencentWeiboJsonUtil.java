/**
 * 
 */
package com.syw.SNSsync.oauth.tencent;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.syw.SNSsync.SingleTweetListViewAdapter;
import com.syw.SNSsync.entity.Tweet;

/**
 * @author songyouwei
 * 解析腾讯JSON数据的工具类
 */
public class TencentWeiboJsonUtil {
	
	/**
	 * 解析登录用户nick
	 */
	public static String getNick(String jsonString) {
		try {
			return new JSONObject(jsonString).getJSONObject("data").getString("nick");
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 解析head
	 */
	public static String getHeadImgUrl(String jsonString) {
		try {
			return new JSONObject(jsonString).getJSONObject("data").getString("head")+"/50";
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
            JSONObject data = (JSONObject) jo.get("data");
            JSONArray info = (JSONArray) data.get("info");
            for (int i = 0; i < info.length(); ++i) {
                JSONObject status = (JSONObject) info.get(i);
                Tweet tweet = new Tweet(status.getString("nick"), status.getString("text"), getCSTimeString(status.getString("timestamp")));
                //对于第一条记录，为翻页所用的pagetime赋值
                if (i == info.length()-1) {
					TencentWeiboActivity.pageTime = status.getString("timestamp");
				}
                //设置id和msgType
                tweet.setId(status.getString("id"));
                tweet.setMsgType(SingleTweetListViewAdapter.MSG_TYPE_AUTHOR);
                //设置headUrl
                tweet.setUserHeadImgUrl(status.getString("head")+"/50");
                
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
			JSONArray commentsArray = new JSONObject(jsonString).getJSONObject("data").getJSONArray("info");
			for (int i = 0; i < commentsArray.length(); i++) {
				JSONObject commentObject = (JSONObject) commentsArray.get(i);
				Tweet comment = new Tweet(commentObject.getString("nick"), commentObject.getString("text"), getCSTimeString(commentObject.getString("timestamp")));
				comment.setMsgType(SingleTweetListViewAdapter.MSG_TYPE_COMMENTER);
				//设置头像
				comment.setUserHeadImgUrl(commentObject.getString("head")+"/50");
				
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
			returnResult = (result.getString("msg").equals("ok")?1:-1);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return returnResult;
	}
	
	private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss",Locale.ENGLISH);
	/**
	 * 转化timestamp为标准显示格式
	 */
	private static String getCSTimeString(String timestamp) {
		return simpleDateFormat.format(new Timestamp(Long.parseLong(timestamp)*1000));
	}
}
