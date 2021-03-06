/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.apps.guangdong.util;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.ShareCompat;

import com.google.samples.apps.guangdong.BuildConfig;
import com.google.samples.apps.guangdong.R;
import com.google.samples.apps.guangdong.appwidget.ScheduleWidgetProvider;
import com.google.samples.apps.guangdong.map.MapActivity;
import com.google.samples.apps.guangdong.provider.ScheduleContract;
import com.google.samples.apps.guangdong.sync.SyncHelper;

import static com.google.samples.apps.guangdong.util.LogUtils.LOGD;
import static com.google.samples.apps.guangdong.util.LogUtils.makeLogTag;

/**
 * Helper class for dealing with common actions to take on a session.
 */
public class SessionsHelper {

    private static final String TAG = makeLogTag(SessionsHelper.class);

    private final Activity mActivity;

    public SessionsHelper(Activity activity) {
        mActivity = activity;
    }

    /**
     * 启动MapActivity，将roomId参数传入其中
     * @param roomId
     */
    public void startMapActivity(String roomId) {
        Intent intent = new Intent(mActivity.getApplicationContext(), MapActivity.class);
        intent.putExtra(MapActivity.EXTRA_ROOM, roomId);
        intent.putExtra(MapActivity.EXTRA_DETACHED_MODE, true);
        mActivity.startActivity(intent);
    }

    /**
     * 从strings中获取对应messageTemplateResId，用title、url替换其中字符串
     * @param messageTemplateResId
     * @param title
     * @param hashtags
     * @param url
     * @return
     */
    public Intent createShareIntent(int messageTemplateResId, String title, String hashtags,
            String url) {
        ShareCompat.IntentBuilder builder = ShareCompat.IntentBuilder.from(mActivity)
                .setType("text/plain")
                .setText(mActivity.getString(messageTemplateResId,
                        title, BuildConfig.CONFERENCE_HASHTAG, " " + url));
        return builder.getIntent();
    }
    /** 分享Session的方法 */
    public void shareSession(Context context, int messageTemplateResId, String title,
            String hashtags, String url) {
        // ANALYTICS EVENT: Share a session.
        // Contains: Session title.
        AnalyticsHelper.sendEvent("Session", "Shared", title);
        Intent intent = Intent.createChooser(
                createShareIntent(messageTemplateResId, title, hashtags, url),
                context.getString(R.string.title_share));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    /** 设置本session为标记过的 */
    public void setSessionStarred(Uri sessionUri, boolean starred, String title) {
        LOGD(TAG, "setSessionStarred uri=" + sessionUri + " starred=" +
                starred + " title=" + title);
        /**
         * 要向ContentValues中添加的数
         */
        String sessionId = ScheduleContract.Sessions.getSessionId(sessionUri);
        /**
         * 向ContentProvider添加时候的URI
         */
        Uri myScheduleUri = ScheduleContract.MySchedule.buildMyScheduleUri(
                AccountUtils.getActiveAccountName(mActivity));

        AsyncQueryHandler handler =
                new AsyncQueryHandler(mActivity.getContentResolver()) {
                };

        final ContentValues values = new ContentValues();
        values.put(ScheduleContract.MySchedule.SESSION_ID, sessionId);
        values.put(ScheduleContract.MySchedule.MY_SCHEDULE_IN_SCHEDULE, starred?1:0);

        handler.startInsert(-1, null, myScheduleUri, values);

        // ANALYTICS EVENT: Add or remove a session from the schedule
        // Contains: Session title, whether it was added or removed (starred or unstarred)
        AnalyticsHelper.sendEvent(
                "Session", starred ? "Starred" : "Unstarred", title);

        // Because change listener is set to null during initialization, these
        // won't fire on pageview.
        mActivity.sendBroadcast(ScheduleWidgetProvider.getRefreshBroadcastIntent(mActivity, false));

        // Request an immediate user data sync to reflect the starred user sessions in the cloud
        SyncHelper.requestManualSync(AccountUtils.getActiveAccount(mActivity), true);
    }
}
