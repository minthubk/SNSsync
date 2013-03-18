/**
 * 
 */
package com.syw.SNSsync.widget;

import com.syw.SNSsync.LauncherActivity;
import com.syw.SNSsync.R;
import com.syw.SNSsync.RunningData;
import com.syw.SNSsync.publish.PublishStatusActivity;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * @author songyouwei WidgetProvider
 */
public class PublishWidgetProvider extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,int[] appWidgetIds) {
		// 可能有多个AppWidget，每个都要绑定处理器
		for (int i = 0; i < appWidgetIds.length; i++) {
			// 这是真是的Intent
			Intent intent = new Intent(context, LauncherForWidgetActivity.class);
			// 这是待定Intent，可以用于Activity跳转
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,intent, 0);
			// 拿到远程视图
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.app_widget);
			// 为控件绑定处理器
			remoteViews.setOnClickPendingIntent(R.id.et_send_status, pendingIntent);
			// 更新AppWidget
			appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
		}
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

}
