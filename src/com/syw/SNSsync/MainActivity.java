package com.syw.SNSsync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.renren.api.connect.android.Renren;
import com.syw.SNSsync.oauth.AccessTokenKeeper;
import com.syw.SNSsync.oauth.CommonOAuth2AccessToken;
import com.syw.SNSsync.oauth.renren.RenrenActivity;
import com.syw.SNSsync.oauth.renren.RenrenJsonUtil;
import com.syw.SNSsync.oauth.renren.RenrenOAuthListener;
import com.syw.SNSsync.oauth.renren.RenrenSquareActivity;
import com.syw.SNSsync.oauth.sina.SinaSquareActivity;
import com.syw.SNSsync.oauth.sina.SinaWeiboActivity;
import com.syw.SNSsync.oauth.sina.SinaWeiboAuthDialogListener;
import com.syw.SNSsync.oauth.sina.SinaWeiboJsonUtil;
import com.syw.SNSsync.oauth.tencent.TencentWeiboActivity;
import com.syw.SNSsync.oauth.tencent.TencentWeiboJsonUtil;
import com.syw.SNSsync.publish.PublishStatusActivity;
import com.tencent.weibo.api.TAPI;
import com.tencent.weibo.api.UserAPI;
import com.tencent.weibo.constants.OAuthConstants;
import com.tencent.weibo.oauthv2.OAuthV2;
import com.tencent.weibo.oauthv2.OAuthV2Client;
import com.tencent.weibo.webview.OAuthV2AuthorizeWebView;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.AccountAPI;
import com.weibo.sdk.android.api.UsersAPI;
import com.weibo.sdk.android.net.RequestListener;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	//组件声明
	private ViewPager tabPager;
	private ImageView homeTabImage,profileTabImage,messageTabImage,settingsTabImage;
	
	//授权状态文本视图
	private TextView sinaTokenStateTextView;
	private TextView tencentTokenStateTextView;
	private TextView renrenTokenStateTextView;
	private TextView qzoneTokenStateTextView;
	//授权管理的“X”图标按钮
	private ImageView sinaTokenStateImageView;
	private ImageView tencentTokenStateImageView;
	private ImageView renrenTokenStateImageView;
	//授权管理图标的位置
	private static final int AUTH_MANAGE_IMG_SINA = 0;
	private static final int AUTH_MANAGE_IMG_TENCENT = 1;
	private static final int AUTH_MANAGE_IMG_RENREN = 2;
	
	//设置的显示文本【显示的是当前的值】
	private TextView statusCountTextView;
	private TextView picModeTextView;
	
	//新浪授权相关
	private SinaOAuthCallbackHandler sinaOAuthCallbackHandler;
	public static int SINA_OAUTH_SUCCESS = 1;
	//腾讯授权相关
	private int REQUEST_CODE = 2;
	private OAuthV2 tencentAuthV2;
	private TencentOAuthCallbackHandler tencentOAuthCallbackHandler;
	private static int TENCENT_OAUTH_SUCCESS = 1;
	//人人授权相关
	private Renren renren;
	private RenrenOAuthCallbackHandler renrenOAuthCallbackHandler;
	public static final int RENREN_OAUTH_SUCCESS = 1;
	public static final int RENREN_OAUTH_ERROR = -1;
	//当前页卡编号
	private int currentPageIndex = 0;
	//页卡索引值
	private static final int MENU_HOME_INDEX = 0;
	private static final int MENU_PROFILE_INDEX = 1;
	private static final int MENU_MESSAGE_INDEX = 2;
	private static final int MENU_SETTINGS_INDEX = 3;
	//onBackPressed()方法用到的两个时间相关的变量
    private long oldTime = 0;
	private long currentTime = 0;
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
		//设置窗口：无标题
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //初始化人人授权对象
    	renren = new Renren(Constants.renren.API_KEY, Constants.renren.SECRET_KEY, Constants.renren.APP_ID, this);
    	//初始化腾讯授权对象
		tencentAuthV2 = new OAuthV2(Constants.tencent.clientId, Constants.tencent.clientSecret, Constants.tencent.redirectUri);
		OAuthV2Client.getQHttpClient().shutdownConnection();
        
        sinaOAuthCallbackHandler = new SinaOAuthCallbackHandler();
        tencentOAuthCallbackHandler = new TencentOAuthCallbackHandler();
        renrenOAuthCallbackHandler = new RenrenOAuthCallbackHandler();
        
        initBottomMenu();
        initViewPager();
    }
    
    /**
     * 两次返回键退出，间隔2秒
     */
	@Override
	public void onBackPressed() {
		currentTime = System.currentTimeMillis();
		if ((currentTime - oldTime) > 2000 || oldTime == 0) {
			Toast.makeText(this, "再按一次退出", 2000).show();
			oldTime = currentTime;
		} else {
			finish();
		}
	}
	
	/**
	 * 接收腾讯微博授权的返回数据
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == REQUEST_CODE) { // 对应之前设置的的myRequsetCode
			if (resultCode == OAuthV2AuthorizeWebView.RESULT_CODE) {
				// 取得返回腾讯的OAuthV2类实例，再转成通用token并存储
				tencentAuthV2 = (OAuthV2) data.getExtras().getSerializable("oauth");
				CommonOAuth2AccessToken token = new CommonOAuth2AccessToken();
				token.setToken(tencentAuthV2.getAccessToken());
				//腾讯的过期时间需要转换
				token.setExpiresIn(System.currentTimeMillis()+Long.parseLong(tencentAuthV2.getExpiresIn())*1000+"");
				token.setOpenId(tencentAuthV2.getOpenid());
				token.setOpenKey(tencentAuthV2.getOpenkey());
				AccessTokenKeeper.keepAccessToken(this, token, AccessTokenKeeper.PREFERENCES_NAME_TOKEN_TENCENT);
				//更改运行时数据
				RunningData.tencentTokenState= Constants.oauth.HAS_AUTHORIZED;
				RunningData.tencentAccessToken = token;
				//更改运行时腾讯接口调用对象
				RunningData.oAuthV2 = new OAuthV2(Constants.tencent.clientId, Constants.tencent.clientSecret, Constants.tencent.redirectUri);
				RunningData.oAuthV2.setAccessToken(token.getToken());
				RunningData.oAuthV2.setOpenid(token.getOpenId());
				RunningData.oAuthV2.setOpenkey(token.getOpenKey()); 
				//发送更新UI的消息
				tencentOAuthCallbackHandler.sendEmptyMessage(TENCENT_OAUTH_SUCCESS);
			}
		}

	}
    
    /**
     * 以下是ViewPager中按钮预先绑定了的方法，position是触发组件所在的分页名称
     */
	//positon:首页
	public void publishStatus(View v) {//"发状态"按钮
		this.startActivity(new Intent(this, PublishStatusActivity.class));
	}
    public void startSina(View v) {//点击新浪图标
    	if (RunningData.sinaTokenState.equals(Constants.oauth.HAS_AUTHORIZED)) {
			this.startActivity(new Intent(this, SinaWeiboActivity.class));
		} else {
			Weibo.getInstance(Constants.sina.KEY, Constants.sina.REDRECT_URL).authorize(this, new SinaWeiboAuthDialogListener(this,sinaOAuthCallbackHandler));
			if (RunningData.sinaTokenState.equals(Constants.oauth.HAS_AUTHORIZED)) {
				sinaTokenStateTextView.setText(Constants.oauth.HAS_AUTHORIZED);
				//显示取消授权图标
				sinaTokenStateImageView.setVisibility(View.VISIBLE);
			}
		}
    }
    public void startTencent(View v) {//点击腾讯图标
    	if (RunningData.tencentTokenState.equals(Constants.oauth.HAS_AUTHORIZED)) {
    		startActivity(new Intent(this, TencentWeiboActivity.class));
		} else {
			//启动授权，回调是onActicityResult方法
    		Intent intent = new Intent(this,OAuthV2AuthorizeWebView.class);
			intent.putExtra("oauth", tencentAuthV2);
			startActivityForResult(intent, REQUEST_CODE);
		}
    }
    public void startRenren(View v) {//点击人人图标
    	if (RunningData.renrenTokenState.equals(Constants.oauth.HAS_AUTHORIZED)) {
			startActivity(new Intent(this, RenrenActivity.class));
		}  else {
	    	//启动授权
	    	new Thread() {
	    		public void run() {
	    			Looper.prepare();
	    			String[] permissions = new String[]{"read_user_feed","read_user_status","publish_feed","publish_comment"};
	    			renren.authorize(MainActivity.this, permissions, new RenrenOAuthListener(MainActivity.this,renrenOAuthCallbackHandler));
//	    			renren.authorize(MainActivity.this, new RenrenOAuthListener(MainActivity.this,renrenOAuthCallbackHandler));
	    			Looper.loop();
	    		}
	    	}.start();
		}
    }
    public void startQzone(View v) {//点击QQ空间图标
    	Toast.makeText(getApplicationContext(), "QQ空间接口尚未开放", Toast.LENGTH_SHORT).show();
    }
    //positon:广场
    public void squareSina(View v) {
    	if (RunningData.sinaTokenState.equals(Constants.oauth.HAS_AUTHORIZED)) {
			this.startActivity(new Intent(this, SinaSquareActivity.class));
		} else {
			Toast.makeText(this, "您还没有绑定新浪微博帐号，请回首页绑定", Toast.LENGTH_SHORT).show();
		}
    }
    public void squareTencent(View v) {
    	Toast.makeText(this, "此接口尚未开放", Toast.LENGTH_SHORT).show();
    }
    public void squareRenren(View v) {
    	if (RunningData.renrenTokenState.equals(Constants.oauth.HAS_AUTHORIZED)) {
			startActivity(new Intent(this, RenrenSquareActivity.class));
		}  else {
			Toast.makeText(this, "您还没有绑定人人帐号，请回首页绑定", Toast.LENGTH_SHORT).show();
		}
    }
    //positon:消息
    public void sinaAtedWeibo(View v) {//新浪@我的微博
    	if (RunningData.sinaTokenState.equals(Constants.oauth.HAS_AUTHORIZED)) {
    		Intent intent = new Intent(this, MessageActivity.class);
    		intent.putExtra("from_code", Constants.from_code.SINA);
    		intent.putExtra("request_code", Constants.request_code.ATED_WEIBO);
			this.startActivity(intent);
		} else {
			Toast.makeText(this, "您还没有绑定新浪微博帐号，请回首页绑定", Toast.LENGTH_SHORT).show();
		}
    }
    public void sinaAtedComment(View v) {//新浪@我的评论
    	if (RunningData.sinaTokenState.equals(Constants.oauth.HAS_AUTHORIZED)) {
    		Intent intent = new Intent(this, MessageActivity.class);
    		intent.putExtra("from_code", Constants.from_code.SINA);
    		intent.putExtra("request_code", Constants.request_code.ATED_COMMENT);
			this.startActivity(intent);
		} else {
			Toast.makeText(this, "您还没有绑定新浪微博帐号，请回首页绑定", Toast.LENGTH_SHORT).show();
		}
    }
    public  void sinaReceivedComment(View v) {//新浪我的微博收到的评论
    	if (RunningData.sinaTokenState.equals(Constants.oauth.HAS_AUTHORIZED)) {
    		Intent intent = new Intent(this, MessageActivity.class);
    		intent.putExtra("from_code", Constants.from_code.SINA);
    		intent.putExtra("request_code", Constants.request_code.RECEIVED_COMMENT);
			this.startActivity(intent);
		} else {
			Toast.makeText(this, "您还没有绑定新浪微博帐号，请回首页绑定", Toast.LENGTH_SHORT).show();
		}
    }
    public void tencentAtedWeibo(View v) {//腾讯@我的微博
    	if (RunningData.tencentTokenState.equals(Constants.oauth.HAS_AUTHORIZED)) {
    		Intent intent = new Intent(this, MessageActivity.class);
    		intent.putExtra("from_code", Constants.from_code.TENCENT);
    		intent.putExtra("request_code", Constants.request_code.ATED_WEIBO);
			this.startActivity(intent);
		} else {
			Toast.makeText(this, "您还没有绑定腾讯微博帐号，请回首页绑定", Toast.LENGTH_SHORT).show();
		}
    }
    //positon:设置
    public void setStatusCount(View v) {//每次刷新获取状态的数量
    	AlertDialog.Builder pickDialog = new AlertDialog.Builder(this);
    	pickDialog.setTitle("当前数目:"+RunningData.settings.statusCount);
    	StatusCountPickDialogListener listener = new StatusCountPickDialogListener();
    	pickDialog.setPositiveButton("5", listener).setNeutralButton("10", listener).setNegativeButton("15", listener);
    	pickDialog.show();
    }
    public void setPicMode(View v) {//图片模式
    	AlertDialog.Builder pickDialog = new AlertDialog.Builder(this);
    	pickDialog.setTitle("当前模式:"+(RunningData.settings.picMode?"有图":"无图"));
    	PicModePickDialogListener listener = new PicModePickDialogListener();
    	pickDialog.setPositiveButton("有图", listener).setNegativeButton("无图", listener);
    	pickDialog.show();
    }
    public void about(View v) {//关于
    	startActivity(new Intent(MainActivity.this, AboutActivity.class));
    }
    
	/**
	 * 初始化above底部菜单上的tabPaper，包含几个分页视图
	 */
	private void initViewPager() {
		//将要分页显示的View装入数组中
        LayoutInflater mLi = LayoutInflater.from(this);
        View homeView = mLi.inflate(R.layout.main_tab_home, null);
        View profileView = mLi.inflate(R.layout.main_tab_profile, null);
        View messageView = mLi.inflate(R.layout.main_tab_message, null);
        View settingsView = mLi.inflate(R.layout.main_tab_settings, null);
        
        //初始化授权状态的文本视图
        sinaTokenStateTextView = (TextView) homeView.findViewById(R.id.token_state_sina);
        tencentTokenStateTextView = (TextView) homeView.findViewById(R.id.token_state_tencent);
        renrenTokenStateTextView = (TextView) homeView.findViewById(R.id.token_state_renren);
        qzoneTokenStateTextView = (TextView) homeView.findViewById(R.id.token_state_qzone);
        //设置文本内容
        sinaTokenStateTextView.setText(RunningData.sinaTokenState);
        tencentTokenStateTextView.setText(RunningData.tencentTokenState);
        renrenTokenStateTextView.setText(RunningData.renrenTokenState);
        qzoneTokenStateTextView.setText(RunningData.qzoneTokenState);
        
        //初始化授权管理图标
        sinaTokenStateImageView = (ImageView) homeView.findViewById(R.id.token_state_sina_img);
        tencentTokenStateImageView = (ImageView) homeView.findViewById(R.id.token_state_tencent_img);
        renrenTokenStateImageView = (ImageView) homeView.findViewById(R.id.token_state_renren_img);
        //为已授权的设置可见，并绑定点击监听
        if(RunningData.sinaTokenState.equals(Constants.oauth.HAS_AUTHORIZED)) {
        	sinaTokenStateImageView.setVisibility(View.VISIBLE);
        }
        sinaTokenStateImageView.setOnClickListener(new TokenStateImageViewOnClickListener(AUTH_MANAGE_IMG_SINA));
        if(RunningData.tencentTokenState.equals(Constants.oauth.HAS_AUTHORIZED)) {
        	tencentTokenStateImageView.setVisibility(View.VISIBLE);
        }
        tencentTokenStateImageView.setOnClickListener(new TokenStateImageViewOnClickListener(AUTH_MANAGE_IMG_TENCENT));
        if(RunningData.renrenTokenState.equals(Constants.oauth.HAS_AUTHORIZED)) {
        	renrenTokenStateImageView.setVisibility(View.VISIBLE);
        }
        renrenTokenStateImageView.setOnClickListener(new TokenStateImageViewOnClickListener(AUTH_MANAGE_IMG_RENREN));
        
        //初始化设置的值
        statusCountTextView = (TextView) settingsView.findViewById(R.id.tv_settings_status_count);
        picModeTextView = (TextView) settingsView.findViewById(R.id.tv_settings_pic_mode);
        statusCountTextView.setText(RunningData.settings.statusCount+"");
        picModeTextView.setText(RunningData.settings.picMode?"有图":"无图");
        
        //每个页面的view数据
        final ArrayList<View> views = new ArrayList<View>();
        views.add(homeView);
        views.add(profileView);
        views.add(messageView);
        views.add(settingsView);
		//为tabPaper设置适配器
		tabPager.setAdapter(new MyPaperAdapter(views));
	}

	
	/**
	 * 初始化底部菜单，并添加监听
	 */
	private void initBottomMenu() {
		tabPager = (ViewPager) findViewById(R.id.tabpager);
        
        homeTabImage = (ImageView) findViewById(R.id.img_home); 
        profileTabImage = (ImageView) findViewById(R.id.img_profile);
        messageTabImage = (ImageView) findViewById(R.id.img_message);
        settingsTabImage = (ImageView) findViewById(R.id.img_settings);
        
        tabPager.setOnPageChangeListener(new TabPagerOnPageChangeListener());
        
        homeTabImage.setOnClickListener(new BottomMenuOnClickListener(MENU_HOME_INDEX));
        profileTabImage.setOnClickListener(new BottomMenuOnClickListener(MENU_PROFILE_INDEX));
        messageTabImage.setOnClickListener(new BottomMenuOnClickListener(MENU_MESSAGE_INDEX));
        settingsTabImage.setOnClickListener(new BottomMenuOnClickListener(MENU_SETTINGS_INDEX));
	}
	
	
	/**
	 * @author songyouwei
	 * 新浪授权回调后用于更新UI的Handler
	 */
	public class SinaOAuthCallbackHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == SINA_OAUTH_SUCCESS) {
				Toast.makeText(MainActivity.this, "授权成功", Toast.LENGTH_SHORT).show();
				sinaTokenStateTextView.setText(Constants.oauth.HAS_AUTHORIZED);
				//显示"X"
				sinaTokenStateImageView.setVisibility(View.VISIBLE);
				//保存nick
				new AccountAPI(RunningData.sinaAccessToken).getUid(new GetUidListener());
			}
		}
		private class GetUidListener implements RequestListener {
			@Override
			public void onComplete(String arg0) {
				new UsersAPI(RunningData.sinaAccessToken).show(Long.parseLong(SinaWeiboJsonUtil.getLoggedInUserId(arg0)), new GetNickListener());
			}
			@Override
			public void onError(WeiboException arg0) {
				System.out.println("GetUidListener onError");
			}
			@Override
			public void onIOException(IOException arg0) {
				System.out.println("GetUidListener onIOException");
			}
		}
		private class GetNickListener implements RequestListener {
			@Override
			public void onComplete(String arg0) {
				String nick = SinaWeiboJsonUtil.getUserNick(arg0);
				String userHeadImgUrl = SinaWeiboJsonUtil.getUserHeadImgUrl(arg0);
				//保存nick并更改运行时数据
				PreferenceKeeper.saveProperty(MainActivity.this, PreferenceKeeper.USER_NICKS_SINA, nick);
				RunningData.nicks.sinaNick = nick;
				//保存用户头像url并更改运行时数据
				PreferenceKeeper.saveProperty(MainActivity.this, PreferenceKeeper.USER_HEAD_IMG_URL_SINA, userHeadImgUrl);
				RunningData.heads.sinaHead = userHeadImgUrl;
			}
			@Override
			public void onError(WeiboException arg0) {
				System.out.println("GetNickListener onError");
			}
			@Override
			public void onIOException(IOException arg0) {
				System.out.println("GetNickListener onIOException");
			}
		}
	}
	
	/**
	 * @author songyouwei
	 * 腾讯授权回调后用于更新UI的Handler
	 */
	private class TencentOAuthCallbackHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == TENCENT_OAUTH_SUCCESS) {
				Toast.makeText(MainActivity.this, "授权成功", Toast.LENGTH_SHORT).show();
				tencentTokenStateTextView.setText(Constants.oauth.HAS_AUTHORIZED);
				//显示取消授权图标
				tencentTokenStateImageView.setVisibility(View.VISIBLE);
				//保存nick,head,并更改运行时数据
				new Thread() {
					public void run() {
						try {
							String jsonString = new UserAPI(OAuthConstants.OAUTH_VERSION_2_A).info(tencentAuthV2, "json");
							
							String nick = TencentWeiboJsonUtil.getNick(jsonString);
							PreferenceKeeper.saveProperty(MainActivity.this, PreferenceKeeper.USER_NICKS_TENCENT, nick);
							RunningData.nicks.tencentNick = nick;
							
							String head = TencentWeiboJsonUtil.getHeadImgUrl(jsonString);
							PreferenceKeeper.saveProperty(MainActivity.this, PreferenceKeeper.USER_HEAD_IMG_URL_TENCENT, head);
							RunningData.heads.tencentHead = head;
						} catch (Exception e) {
							e.printStackTrace();
						}
					};
				}.start();
			}
		}
	}
	
	/**
	 * @author songyouwei
	 * 人人授权回调后用于更新UI的Handler
	 */
	public class RenrenOAuthCallbackHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case RENREN_OAUTH_SUCCESS:
				Toast.makeText(MainActivity.this, "授权成功", Toast.LENGTH_SHORT).show();
				renrenTokenStateTextView.setText(Constants.oauth.HAS_AUTHORIZED);
				//显示取消授权图标
				renrenTokenStateImageView.setVisibility(View.VISIBLE);
				//保存nick和head
				new Thread() {
					public void run() {
						Bundle params0 = new Bundle();
						params0.putString("method", "users.getLoggedInUser");
						String uid = RenrenJsonUtil.getLoggedInUserId(renren.requestJSON(params0));
						Bundle parameters = new Bundle();
						parameters.putString("method", "users.getProfileInfo");
						parameters.putString("uid", uid);
						String nick = RenrenJsonUtil.getUserNick(renren.requestJSON(parameters));
						PreferenceKeeper.saveProperty(MainActivity.this, PreferenceKeeper.USER_NICKS_RENREN, nick);
						//修改运行时数据
						RunningData.nicks.renrenNick = nick;
						
						Bundle params1 = new Bundle();
						params1.putString("method", "users.getProfileInfo");
						params1.putString("uid", uid);
						String head = RenrenJsonUtil.getUserHeadImgUrl(renren.requestJSON(params1));
						PreferenceKeeper.saveProperty(MainActivity.this, PreferenceKeeper.USER_HEAD_IMG_URL_RENREN, head);
						//修改运行时数据
						RunningData.heads.renrenHead = head;
					};
				}.start();
				break;

			default:
				Toast.makeText(MainActivity.this, "授权错误！", Toast.LENGTH_SHORT).show();
				break;
			}
		}
	}
	
	
	/**
	 * 填充ViewPager的数据适配器
	 */
	private class MyPaperAdapter extends PagerAdapter {
		
		private List<View> views = new ArrayList<View>();
		public MyPaperAdapter(ArrayList<View> views) {
			this.views = views;
		}
		
		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
		
		@Override
		public int getCount() {
			return views.size();
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager)container).removeView(views.get(position));
		}
		
		@Override
		public Object instantiateItem(View container, int position) {
			((ViewPager)container).addView(views.get(position));
			return views.get(position);
		}
		
	}
	
	
	/**
	 * 页卡切换监听类，根据选择的页卡编号来改变底部菜单的样式，并改变currentPageIndex(当前页卡编号)
	 */
	private class TabPagerOnPageChangeListener extends ViewPager.SimpleOnPageChangeListener {
		Resources res = getResources();
		@Override
		public void onPageSelected(int position) {
			switch (position) {
			case MENU_HOME_INDEX:
				homeTabImage.setImageDrawable(res.getDrawable(R.drawable.tab_home_pressed));
				setCurrentMenuImageViewAsNomal();
				break;
				
			case MENU_PROFILE_INDEX:
				profileTabImage.setImageDrawable(res.getDrawable(R.drawable.tab_profile_pressed));
				setCurrentMenuImageViewAsNomal();
				break;
				
			case MENU_MESSAGE_INDEX:
				messageTabImage.setImageDrawable(res.getDrawable(R.drawable.tab_message_pressed));
				setCurrentMenuImageViewAsNomal();
				break;
				
			case MENU_SETTINGS_INDEX:
				settingsTabImage.setImageDrawable(res.getDrawable(R.drawable.tab_settings_pressed));
				setCurrentMenuImageViewAsNomal();
				break;
			}
			currentPageIndex = position;
		}
		//设置当前菜单按钮图为nomal的内部方法
		private void setCurrentMenuImageViewAsNomal() {
			switch (currentPageIndex) {
			case MENU_HOME_INDEX:
				homeTabImage.setImageDrawable(res.getDrawable(R.drawable.tab_home_normal));
				break;

			case MENU_PROFILE_INDEX:
				profileTabImage.setImageDrawable(res.getDrawable(R.drawable.tab_profile_normal));
				break;
				
			case MENU_MESSAGE_INDEX:
				messageTabImage.setImageDrawable(res.getDrawable(R.drawable.tab_message_normal));
				break;
				
			case MENU_SETTINGS_INDEX:
				settingsTabImage.setImageDrawable(res.getDrawable(R.drawable.tab_settings_normal));
				break;
			}
		}
	}
	
	
	/**
	 * 底部菜单点击监听器类，用来改变tabPager当前页卡的索引值
	 */
	private class BottomMenuOnClickListener implements View.OnClickListener {
		int currentItem;
		public BottomMenuOnClickListener(int currentItem) {
			this.currentItem = currentItem;
		}
		@Override
		public void onClick(View v) {
			tabPager.setCurrentItem(currentItem);
		}
	}
	
	/**
	 *  取消授权“X”图标的点击监听
	 */
	private class TokenStateImageViewOnClickListener implements View.OnClickListener {
		int item;
		public TokenStateImageViewOnClickListener(int item) {
			this.item = item;
		}
		@Override
		public void onClick(View v) {
			switch (item) {
			case AUTH_MANAGE_IMG_SINA:
				AccessTokenKeeper.clear(MainActivity.this, AccessTokenKeeper.PREFERENCES_NAME_TOKEN_SINA);
				//退登要清除cookie
				CookieSyncManager.createInstance(getApplicationContext());
				CookieManager.getInstance().removeAllCookie();
				Toast.makeText(MainActivity.this, "已取消绑定", Toast.LENGTH_SHORT).show();
				sinaTokenStateTextView.setText(Constants.oauth.NOT_AUTHORIZED);
				sinaTokenStateImageView.setVisibility(View.INVISIBLE);
				 //更改运行时数据
		        RunningData.sinaTokenState = Constants.oauth.NOT_AUTHORIZED;
				break;
			case AUTH_MANAGE_IMG_TENCENT:
				AccessTokenKeeper.clear(MainActivity.this, AccessTokenKeeper.PREFERENCES_NAME_TOKEN_TENCENT);
				Toast.makeText(MainActivity.this, "已取消绑定", Toast.LENGTH_SHORT).show();
				tencentTokenStateTextView.setText(Constants.oauth.NOT_AUTHORIZED);
				tencentTokenStateImageView.setVisibility(View.INVISIBLE);
				 //更改运行时数据
		        RunningData.tencentTokenState = Constants.oauth.NOT_AUTHORIZED;
				break;
			case AUTH_MANAGE_IMG_RENREN:
				AccessTokenKeeper.clear(MainActivity.this, AccessTokenKeeper.PREFERENCES_NAME_TOKEN_RENREN);
				//人人SDK的特殊性，得把根本性的那个preference删了
				//Logout
				renren.logout(MainActivity.this);
				AccessTokenKeeper.clear(MainActivity.this, "renren_sdk_config");
				Toast.makeText(MainActivity.this, "已取消绑定", Toast.LENGTH_SHORT).show();
				renrenTokenStateTextView.setText(Constants.oauth.NOT_AUTHORIZED);
				renrenTokenStateImageView.setVisibility(View.INVISIBLE);
				 //更改运行时数据
		        RunningData.renrenTokenState = Constants.oauth.NOT_AUTHORIZED;
				break;
			}
		}
	}
	
	/**
	 * @author songyouwei
	 * 监听设置statusCount的事件
	 */
	private class StatusCountPickDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			int num = Constants.settings_default.STATUS_COUNT;
			switch (which) {
			case AlertDialog.BUTTON_POSITIVE:
				num = 5;
				break;
			case AlertDialog.BUTTON_NEUTRAL:
				num = 10;
				break;
			case AlertDialog.BUTTON_NEGATIVE:
				num = 15;
				break;
			}
			statusCountTextView.setText(num+"");
			RunningData.settings.statusCount = num;
			PreferenceKeeper.saveSetting(getApplicationContext(), PreferenceKeeper.SETTINGS_STATUS_COUNT, num+"");
			Toast.makeText(getApplicationContext(), "修改成功", Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * @author songyouwei
	 * 监听设置PicMode的事件
	 */
	private class PicModePickDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			boolean picMode = Constants.settings_default.PIC_MODE;
			switch (which) {
			case AlertDialog.BUTTON_POSITIVE:
				picMode = true;
				break;
			case AlertDialog.BUTTON_NEGATIVE:
				picMode = false;
				break;
			}
			picModeTextView.setText(picMode?"有图":"无图");
			RunningData.settings.picMode = picMode;
			PreferenceKeeper.saveSetting(getApplicationContext(), PreferenceKeeper.SETTINGS_PIC_MODE, picMode?"true":"false");
			Toast.makeText(getApplicationContext(), "修改成功", Toast.LENGTH_SHORT).show();
		}
	}
    
}
