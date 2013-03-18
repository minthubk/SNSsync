/**
 * 
 */
package com.syw.SNSsync.oauth.sina;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.syw.SNSsync.Constants;
import com.syw.SNSsync.MainActivity;
import com.syw.SNSsync.MainActivity.SinaOAuthCallbackHandler;
import com.syw.SNSsync.RunningData;
import com.syw.SNSsync.oauth.AccessTokenKeeper;
import com.syw.SNSsync.oauth.CommonOAuth2AccessToken;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.StatusesAPI;

/**
 * @author songyouwei
 *新浪微博授权回调类
 */
public class SinaWeiboAuthDialogListener implements WeiboAuthListener {

	private Context context;
	private MainActivity.SinaOAuthCallbackHandler sinaOAuthCallbackHandler;
	public SinaWeiboAuthDialogListener(Context context,SinaOAuthCallbackHandler sinaOAuthCallbackHandler) {
		this.context = context;
		this.sinaOAuthCallbackHandler = sinaOAuthCallbackHandler;
	}

	@Override
	public void onCancel() {
		// TODO Auto-generated method stub
		Toast.makeText(context, "您取消了授权", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onComplete(Bundle values) {
        String token = values.getString("access_token");
        String expires_in = values.getString("expires_in");
        //生成通用token
        CommonOAuth2AccessToken accessToken = new CommonOAuth2AccessToken();
        accessToken.setToken(token);
        accessToken.setExpiresIn(expires_in);
        
        //保存token
        AccessTokenKeeper.keepAccessToken(context,accessToken,AccessTokenKeeper.PREFERENCES_NAME_TOKEN_SINA);
        //更改运行时数据
        RunningData.sinaTokenState = Constants.oauth.HAS_AUTHORIZED;
        RunningData.sinaAccessToken = accessToken;
        RunningData.statusesAPI = new StatusesAPI(accessToken);
        //发送成功消息，更改UI
        sinaOAuthCallbackHandler.sendEmptyMessage(MainActivity.SINA_OAUTH_SUCCESS);
    }

	@Override
	public void onError(WeiboDialogError arg0) {
		// TODO Auto-generated method stub
		Toast.makeText(context, "授权服务器异常", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onWeiboException(WeiboException arg0) {
		// TODO Auto-generated method stub
		Toast.makeText(context, "微博服务器异常", Toast.LENGTH_SHORT).show();
	}

}
