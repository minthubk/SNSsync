package com.syw.SNSsync.oauth.renren;

import android.content.Context;
import android.os.Bundle;
import com.renren.api.connect.android.exception.RenrenAuthError;
import com.renren.api.connect.android.view.RenrenAuthListener;
import com.syw.SNSsync.Constants;
import com.syw.SNSsync.MainActivity;
import com.syw.SNSsync.MainActivity.RenrenOAuthCallbackHandler;
import com.syw.SNSsync.PreferenceKeeper;
import com.syw.SNSsync.RunningData;
import com.syw.SNSsync.oauth.AccessTokenKeeper;
import com.syw.SNSsync.oauth.CommonOAuth2AccessToken;

/**
 * @author songyouwei
 * 人人授权回调监听类
 */
public class RenrenOAuthListener implements RenrenAuthListener {

	private Context context;
	private RenrenOAuthCallbackHandler renrenOAuthCallbackHandler;
	public RenrenOAuthListener(Context context,RenrenOAuthCallbackHandler renrenOAuthCallbackHandler) {
		this.context = context;
		this.renrenOAuthCallbackHandler = renrenOAuthCallbackHandler;
	}

	@Override
	public void onComplete(Bundle values) {
		System.out.println(values);
		String expires_in = values.getString("expires_in");
		String access_token = values.getString("access_token");
		
		//生成通用token
        CommonOAuth2AccessToken accessToken = new CommonOAuth2AccessToken();
        accessToken.setToken(access_token);
        accessToken.setExpiresIn(expires_in);
        
        //保存token
        AccessTokenKeeper.keepAccessToken(context,accessToken,AccessTokenKeeper.PREFERENCES_NAME_TOKEN_RENREN);
        
        //更改运行时数据
        RunningData.renrenTokenState = Constants.oauth.HAS_AUTHORIZED;
        RunningData.renrenAccessToken = accessToken;
        //发送成功消息，更改UI
        renrenOAuthCallbackHandler.sendEmptyMessage(MainActivity.RENREN_OAUTH_SUCCESS);
	}

	@Override
	public void onRenrenAuthError(RenrenAuthError renrenAuthError) {
		//发送失败消息，更改UI
        renrenOAuthCallbackHandler.sendEmptyMessage(MainActivity.RENREN_OAUTH_ERROR);
	}

	@Override
	public void onCancelLogin() {
		//发送失败消息，更改UI
        renrenOAuthCallbackHandler.sendEmptyMessage(MainActivity.RENREN_OAUTH_ERROR);
	}

	@Override
	public void onCancelAuth(Bundle values) {
		//发送失败消息，更改UI
        renrenOAuthCallbackHandler.sendEmptyMessage(MainActivity.RENREN_OAUTH_ERROR);
	}
	
};
