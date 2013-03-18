/**
 * 
 */
package com.syw.SNSsync;

import com.syw.SNSsync.oauth.AccessTokenKeeper;
import com.syw.SNSsync.oauth.CommonOAuth2AccessToken;
import com.tencent.weibo.oauthv2.OAuthV2;
import com.weibo.sdk.android.api.CommentsAPI;
import com.weibo.sdk.android.api.StatusesAPI;

import android.app.Activity;

/**
 * @author songyouwei
 * 启动程序的基础Activity，加载数据用
 */
public class LauncherBaseActivity extends Activity {
	
	//初始化设置信息
	public void initSettings() {
		RunningData.settings.statusCount = Integer.parseInt(PreferenceKeeper.readSetting(getApplicationContext(), PreferenceKeeper.SETTINGS_STATUS_COUNT));
		RunningData.settings.picMode = PreferenceKeeper.readSetting(getApplicationContext(), PreferenceKeeper.SETTINGS_PIC_MODE).equals("true")?true:false;
	}
	//初始化Token的授权状态
	public void initTokenState() {
		RunningData.sinaTokenState = getTokenStateByPreferencesName(AccessTokenKeeper.PREFERENCES_NAME_TOKEN_SINA);
		RunningData.tencentTokenState = getTokenStateByPreferencesName(AccessTokenKeeper.PREFERENCES_NAME_TOKEN_TENCENT);
		RunningData.renrenTokenState = getTokenStateByPreferencesName(AccessTokenKeeper.PREFERENCES_NAME_TOKEN_RENREN);
		RunningData.qzoneTokenState = getTokenStateByPreferencesName(AccessTokenKeeper.PREFERENCES_NAME_TOKEN_QZONE);
	}
	//为已授权SNS---初始化Token，接口调用对象，和nick，和head
	public void initToken() {
		if (RunningData.sinaTokenState.equals(Constants.oauth.HAS_AUTHORIZED)) {
			RunningData.sinaAccessToken = AccessTokenKeeper.readAccessToken(getApplicationContext(), AccessTokenKeeper.PREFERENCES_NAME_TOKEN_SINA);
			//初始化新浪接口调用对象
			RunningData.statusesAPI = new StatusesAPI(RunningData.sinaAccessToken);
			RunningData.commentsAPI = new CommentsAPI(RunningData.sinaAccessToken);
			//nick
			RunningData.nicks.sinaNick = PreferenceKeeper.readProperty(getApplicationContext(), PreferenceKeeper.USER_NICKS_SINA);
			//head
			RunningData.heads.sinaHead = PreferenceKeeper.readProperty(getApplicationContext(), PreferenceKeeper.USER_HEAD_IMG_URL_SINA);
		}
		if (RunningData.tencentTokenState.equals(Constants.oauth.HAS_AUTHORIZED)) {
			RunningData.tencentAccessToken = AccessTokenKeeper.readAccessToken(getApplicationContext(), AccessTokenKeeper.PREFERENCES_NAME_TOKEN_TENCENT);
			//初始化腾讯接口调用对象
			RunningData.oAuthV2 = new OAuthV2(Constants.tencent.clientId, Constants.tencent.clientSecret, Constants.tencent.redirectUri);
			CommonOAuth2AccessToken token = RunningData.tencentAccessToken;
			RunningData.oAuthV2.setAccessToken(token.getToken());
			RunningData.oAuthV2.setOpenid(token.getOpenId());
			RunningData.oAuthV2.setOpenkey(token.getOpenKey()); 
			//nick
			RunningData.nicks.tencentNick = PreferenceKeeper.readProperty(getApplicationContext(), PreferenceKeeper.USER_NICKS_TENCENT);
			//head
			RunningData.heads.tencentHead = PreferenceKeeper.readProperty(getApplicationContext(), PreferenceKeeper.USER_HEAD_IMG_URL_TENCENT);
		}
		if (RunningData.renrenTokenState.equals(Constants.oauth.HAS_AUTHORIZED)) {
			RunningData.renrenAccessToken = AccessTokenKeeper.readAccessToken(getApplicationContext(), AccessTokenKeeper.PREFERENCES_NAME_TOKEN_RENREN);
			//nick
			RunningData.nicks.renrenNick = PreferenceKeeper.readProperty(getApplicationContext(), PreferenceKeeper.USER_NICKS_RENREN);
			//head
			RunningData.heads.renrenHead = PreferenceKeeper.readProperty(getApplicationContext(), PreferenceKeeper.USER_HEAD_IMG_URL_RENREN);
		}
		if (RunningData.qzoneTokenState.equals(Constants.oauth.HAS_AUTHORIZED)) {
			RunningData.qzoneAccessToken = AccessTokenKeeper.readAccessToken(getApplicationContext(), AccessTokenKeeper.PREFERENCES_NAME_TOKEN_QZONE);
		}
	}
	// 通过prefrencesName得到SNS的授权信息
	public String getTokenStateByPreferencesName(String preferencesName) {
		boolean containsToken = AccessTokenKeeper.containsToken(getApplicationContext(), preferencesName);
		if (containsToken == false) {
			// 未曾授权
			return Constants.oauth.NOT_AUTHORIZED;
		} else {
			// 已授权或已过期
			CommonOAuth2AccessToken accessToken = AccessTokenKeeper.readAccessToken(getApplicationContext(),preferencesName);
			return (accessToken.isSessionValid() == true) ? Constants.oauth.HAS_AUTHORIZED: Constants.oauth.EXPIRED;
		}
	}
}
