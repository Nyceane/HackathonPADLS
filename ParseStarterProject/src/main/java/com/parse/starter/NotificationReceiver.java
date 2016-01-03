package com.parse.starter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;

public class NotificationReceiver extends ParsePushBroadcastReceiver {

private static final String LOG_TAG = NotificationReceiver.class.getSimpleName();

	@Override
    protected Notification getNotification(Context context, Intent intent) {
	    Log.v(LOG_TAG, "getNotification called");
        Intent trigger = new Intent(context, TriggerActivity.class);
        trigger.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(trigger);
        return null;
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        Log.v(LOG_TAG, "onPushOpen called");

    }

    @Override
    protected Class<? extends Activity> getActivity(Context context, Intent intent) {
        Log.v(LOG_TAG, "getActivity called");
        return super.getActivity(context, intent);
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        Log.v(LOG_TAG, "onPushReceive called");
        super.onPushReceive(context, intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(LOG_TAG, "onReceive Called");
        super.onReceive(context, intent);
    }
}