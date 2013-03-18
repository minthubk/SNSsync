package com.syw.SNSsync.entity;

public class PublishStatusEntity {
	public PublishStatusEntity(String content, int msgType) {
		this.content = content;
		this.setMsgType(msgType);
	}
	private String content;
	private int msgType;

	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public int getMsgType() {
		return msgType;
	}
	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}
}
