/**
 * 
 */
package com.syw.SNSsync.oauth;

import com.weibo.sdk.android.Oauth2AccessToken;

/**
 * @author songyouwei
 * 通用token类，继承新浪的OAuthV2类，并增加了腾讯的openId和openKey
 */
public class CommonOAuth2AccessToken extends Oauth2AccessToken {
	
	private String openId;
	private String openKey;
	
	public String getOpenId() {
		return openId;
	}
	public void setOpenId(String openId) {
		this.openId = openId;
	}
	public String getOpenKey() {
		return openKey;
	}
	public void setOpenKey(String openKey) {
		this.openKey = openKey;
	}
}
