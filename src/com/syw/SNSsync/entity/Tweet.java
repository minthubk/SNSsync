package com.syw.SNSsync.entity;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class Tweet implements Serializable {
	public Tweet(String name,String text,String time) {
		// TODO Auto-generated constructor stub
		this.name = name;
		this.text = text;
		this.time = time;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "name:"+getName()+" text:"+getText()+" time:"+getTime();
	}
	
	private String id;
	private String fid;//若它是评论，则存在fid，为评论的的内容微博的id
	private String ownerId;
	private String name;
	private String userHeadImgUrl;
	private String text;
	private String time;
	private int msgType;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public int getMsgType() {
		return msgType;
	}
	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getOwnerId() {
		return ownerId;
	}
	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}
	public String getFid() {
		return fid;
	}
	public void setFid(String fid) {
		this.fid = fid;
	}
	public String getUserHeadImgUrl() {
		return userHeadImgUrl;
	}
	public void setUserHeadImgUrl(String userHeadImgUrl) {
		this.userHeadImgUrl = userHeadImgUrl;
	}
}