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

package com.google.samples.apps.guangdong.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.samples.apps.guangdong.Config;
import com.google.samples.apps.guangdong.util.UIUtils;
import com.google.samples.apps.guangdong.welcome.WelcomeActivity;

import java.util.TimeZone;

import static com.google.samples.apps.guangdong.util.LogUtils.makeLogTag;

/**
 * Utilities and constants related to app settings_prefs.
 */
public class SettingsUtils {

    private static final String TAG = makeLogTag(SettingsUtils.class);

    /**
     * This is changed each year to effectively reset certain preferences that should be re-asked
     * each year. Note, res/xml/settings_prefs.xml must be updated when this value is updated.
     */
    private static final String CONFERENCE_YEAR_PREF_POSTFIX = "_2015";

    /**
     * Boolean preference indicating the user would like to see times in their local timezone
     * throughout the app.
     */
    public static final String PREF_LOCAL_TIMES = "pref_local_times";

    /**
     * Boolean preference indicating that the user will be attending the conference.
     */
    public static final String PREF_ATTENDEE_AT_VENUE = "pref_attendee_at_venue" +
            CONFERENCE_YEAR_PREF_POSTFIX;

    /**
     * Boolean preference indicating whether the app has
     * {@code com.google.samples.apps.iosched.ui.BaseActivity.performDataBootstrap installed} the
     * {@code R.raw.bootstrap_data bootstrap data}.
     */
    public static final String PREF_DATA_BOOTSTRAP_DONE = "pref_data_bootstrap_done";

    /**
     * Boolean indicating whether the app should attempt to sign in on startup (default true).
     */
    public static final String PREF_USER_REFUSED_SIGN_IN = "pref_user_refused_sign_in" +
            CONFERENCE_YEAR_PREF_POSTFIX;

    /**
     * Boolean indicating whether the debug build warning was already shown.
     */
    public static final String PREF_DEBUG_BUILD_WARNING_SHOWN = "pref_debug_build_warning_shown";

    /**
     * Boolean indicating whether ToS has been accepted.
     */
    public static final String PREF_TOS_ACCEPTED = "pref_tos_accepted" +
            CONFERENCE_YEAR_PREF_POSTFIX;

    /**
     * Boolean indicating whether CoC has been accepted.
     */
    private static final String PREF_CONDUCT_ACCEPTED = "pref_conduct_accepted" +
            CONFERENCE_YEAR_PREF_POSTFIX;

    /**
     * Boolean indicating whether ToS has been accepted.
     */
    public static final String PREF_DECLINED_WIFI_SETUP = "pref_declined_wifi_setup" +
            CONFERENCE_YEAR_PREF_POSTFIX;

    /**
     * Boolean indicating whether user has answered if they are local or remote.
     */
    public static final String PREF_ANSWERED_LOCAL_OR_REMOTE = "pref_answered_local_or_remote" +
            CONFERENCE_YEAR_PREF_POSTFIX;

    /**
     * Long indicating when a sync was last ATTEMPTED (not necessarily succeeded).
     */
    public static final String PREF_LAST_SYNC_ATTEMPTED = "pref_last_sync_attempted";

    /**
     * Long indicating when a sync last SUCCEEDED.
     */
    public static final String PREF_LAST_SYNC_SUCCEEDED = "pref_last_sync_succeeded";

    /**
     * Long storing the sync interval that's currently configured.
     */
    public static final String PREF_CUR_SYNC_INTERVAL = "pref_cur_sync_interval";

    /**
     * Boolean indicating app should sync sessions with local calendar
     */
    public static final String PREF_SYNC_CALENDAR = "pref_sync_calendar";

    /**
     * Boolean indicating whether the app has performed the (one-time) welcome flow.
     */
    public static final String PREF_WELCOME_DONE = "pref_welcome_done" +
            CONFERENCE_YEAR_PREF_POSTFIX;

    /**
     * Boolean indicating if the app can collect Analytics.
     */
    public static final String PREF_ANALYTICS_ENABLED = "pref_analytics_enabled";

    /**
     * Boolean indicating whether to show session reminder notifications.
     */
    public static final String PREF_SHOW_SESSION_REMINDERS = "pref_show_session_reminders" +
            CONFERENCE_YEAR_PREF_POSTFIX;

    /**
     * Boolean indicating whether to show session feedback notifications.
     */
    public static final String PREF_SHOW_SESSION_FEEDBACK_REMINDERS =
            "pref_show_session_feedback_reminders" + CONFERENCE_YEAR_PREF_POSTFIX;

    /**
     * Return the {@link TimeZone} the app is set to use (either user or conference).
     * 获得App使用的是之前的时区、还是App配置的时区
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static TimeZone getDisplayTimeZone(Context context) {
        TimeZone defaultTz = TimeZone.getDefault();
        return (isUsingLocalTime(context) && defaultTz != null)
                ? defaultTz : Config.CONFERENCE_TIMEZONE;
    }

    /**
     * Return true if the user has indicated they want the schedule in local times, false if they
     * want to use the conference time zone. This preference is enabled/disabled by the user in the
     * {@link SettingsActivity}.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     *
     * App是否在使用之前的时区
     */
    public static boolean isUsingLocalTime(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_LOCAL_TIMES, false);
    }

    /**
     * Return true if the user has indicated they're attending I/O in person. This preference can be
     * enabled/disabled by the user in the
     * {@link SettingsActivity}.
     *  是否出席了会议
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean isAttendeeAtVenue(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_ATTENDEE_AT_VENUE, true);
    }

    /**
     * Mark that the app has finished loading the {@code R.raw.bootstrap_data bootstrap data}.
     * 将 自带数据读取 标志置为true
     * @param context Context to be used to edit the {@link android.content.SharedPreferences}.
     */
    public static void markDataBootstrapDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_DATA_BOOTSTRAP_DONE, true).apply();
    }

    /**
     * Return true when the {@code R.raw.bootstrap_data_json bootstrap data} has been marked loaded.
     * 自带数据是否已经读取
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean isDataBootstrapDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_DATA_BOOTSTRAP_DONE, false);
    }

    /**
     * Set the attendee preference indicating whether they'll be attending Google I/O on site.
     * 设置是否出席会议
     * @param context  Context to be used to edit the {@link android.content.SharedPreferences}.
     * @param newValue New value that will be set.
     */
    public static void setAttendeeAtVenue(final Context context, final boolean newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_ATTENDEE_AT_VENUE, newValue).apply();
    }

    /**
     * Mark that the user explicitly chose not to sign in so app doesn't ask them again.
     * 设置User拒绝登陆，防止不停地弹出提醒登陆对话框
     * @param context Context to be used to edit the {@link android.content.SharedPreferences}.
     */
    public static void markUserRefusedSignIn(final Context context, final boolean refused) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_USER_REFUSED_SIGN_IN, refused).apply();
    }

    /**
     * Return true if user refused to sign in, false if they haven't refused (yet).
     * 根据Shared中的值，判断User是否拒绝登陆
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean hasUserRefusedSignIn(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_USER_REFUSED_SIGN_IN, false);
    }

    /**
     * Return true if the
     * {@code com.google.samples.apps.iosched.welcome.WelcomeActivity.displayDogfoodWarningDialog() Dogfood Build Warning}
     * has already been marked as shown, false if not.
     * DebugWarning对话框是否已经出现过
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean wasDebugWarningShown(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_DEBUG_BUILD_WARNING_SHOWN, false);
    }

    /**
     * Mark the
     * {@code com.google.samples.apps.iosched.welcome.WelcomeActivity.displayDogfoodWarningDialog() Dogfood Build Warning}
     * shown to user.
     * 设置Shared中的DebugWarning对话框已经出现过
     * @param context Context to be used to edit the {@link android.content.SharedPreferences}.
     */
    public static void markDebugWarningShown(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_DEBUG_BUILD_WARNING_SHOWN, true).apply();
    }

    /**
     * Return true if user has accepted the
     * {@link WelcomeActivity Tos}, false if they haven't (yet).
     * 是否已经接受TOS协议
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean isTosAccepted(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_TOS_ACCEPTED, false);
    }

    /**
     * Return true if user has accepted the Code of
     * {@link com.google.samples.apps.guangdong.welcome.ConductFragment Conduct}, false if they haven't (yet).
     * COC是否已经被接受
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean isConductAccepted(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_CONDUCT_ACCEPTED, false);
    }

    /**
     * Mark {@code newValue whether} the user has accepted the TOS so the app doesn't ask again.
     * 标定Tos已经被经受
     * @param context Context to be used to edit the {@link android.content.SharedPreferences}.
     * @param newValue New value that will be set.
     */
    public static void markTosAccepted(final Context context, boolean newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_TOS_ACCEPTED, newValue).apply();
    }

    /**
     * Mark {@code newValue whether} the user has accepted the Code of Conduct so the app doesn't ask again.
     * 标定CoC被接受
     * @param context Context to be used to edit the {@link android.content.SharedPreferences}.
     * @param newValue New value that will be set.
     */
    public static void markConductAccepted(final Context context, boolean newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_CONDUCT_ACCEPTED, newValue).apply();
    }

    /**
     * Return true if user has already declined WiFi setup, but false if they haven't yet.
     * 是否已经拒绝WifiSetup
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean hasDeclinedWifiSetup(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_DECLINED_WIFI_SETUP, false);
    }

    /**
     * Mark that the user has explicitly declined WiFi setup assistance.
     * 标定拒绝WifiSetup
     * @param context  Context to be used to edit the {@link android.content.SharedPreferences}.
     * @param newValue New value that will be set.
     */
    public static void markDeclinedWifiSetup(final Context context, boolean newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_DECLINED_WIFI_SETUP, newValue).apply();
    }

    /**
     * Returns true if user has already indicated whether they're a local or remote I/O attendee,
     * false if they haven't answered yet.
     * User是否已经填写 远程或参会 选项
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean hasAnsweredLocalOrRemote(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_ANSWERED_LOCAL_OR_REMOTE, false);
    }

    /**
     * Mark that the user answered whether they're a local or remote I/O attendee.
     * 标定User已经回答是否参会选项
     * @param context  Context to be used to edit the {@link android.content.SharedPreferences}.
     * @param newValue New value that will be set.
     */
    public static void markAnsweredLocalOrRemote(final Context context, boolean newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_ANSWERED_LOCAL_OR_REMOTE, newValue).apply();
    }

    /**
     * Return true if the first-app-run-activities have already been executed.
     * 第一次启动app的那个Activity是否已经执行
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean isFirstRunProcessComplete(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_WELCOME_DONE, false);
    }

    /**
     * Mark {@code newValue whether} this is the first time the first-app-run-processes have run.
     * Managed by {@link com.google.samples.apps.guangdong.ui.BaseActivity the}
     * {@link com.google.samples.apps.guangdong.core.activities.BaseActivity two} base activities.
     * 标定第一次启动app要显示的那个activity已经显示过了
     * @param context  Context to be used to edit the {@link android.content.SharedPreferences}.
     * @param newValue New value that will be set.
     */
    public static void markFirstRunProcessesDone(final Context context, boolean newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_WELCOME_DONE, newValue).apply();
    }

    /**
     * Return a long representing the last time a sync was attempted (regardless of success).
     * 返回上一次企图同步的时间
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static long getLastSyncAttemptedTime(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(PREF_LAST_SYNC_ATTEMPTED, 0L);
    }

    /**
     * Mark a sync was attempted (stores current time as 'last sync attempted' preference).
     * 使用 现在时间 标定最新一次企图标定
     * @param context Context to be used to edit the {@link android.content.SharedPreferences}.
     */
    public static void markSyncAttemptedNow(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putLong(PREF_LAST_SYNC_ATTEMPTED, UIUtils.getCurrentTime(context)).apply();
    }

    /**
     * Return a long representing the last time a sync succeeded.
     * 返回上一次同步成功的时间
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static long getLastSyncSucceededTime(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(PREF_LAST_SYNC_SUCCEEDED, 0L);
    }

    /**
     * Mark that a sync succeeded (stores current time as 'last sync succeeded' preference).
     * 利用现在时间标定最新的同步成功时间
     * @param context Context to be used to edit the {@link android.content.SharedPreferences}.
     */
    public static void markSyncSucceededNow(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putLong(PREF_LAST_SYNC_SUCCEEDED, UIUtils.getCurrentTime(context)).apply();
    }

    /**
     * Return true if analytics are enabled, false if user has disabled them.
     * 返回 分析 是否可用
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean isAnalyticsEnabled(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_ANALYTICS_ENABLED, true);
    }

    /**
     * Return true if session reminders are enabled, false if user has disabled them.
     * 是否应该显示 session提醒服务
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean shouldShowSessionReminders(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_SHOW_SESSION_REMINDERS, true);
    }

    /**
     * Return true if session feedback reminders are enabled, false if user has disabled them.
     * 是否需要提醒Feedback提醒
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean shouldShowSessionFeedbackReminders(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_SHOW_SESSION_FEEDBACK_REMINDERS, true);
    }

    /**
     * Return a long representing the current data sync interval time.
     * 获取同步间隔时间
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static long getCurSyncInterval(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(PREF_CUR_SYNC_INTERVAL, 0L);
    }

    /**
     * Set a new interval for the data sync time.
     * 设置同步间隔时间
     * @param context  Context to be used to edit the {@link android.content.SharedPreferences}.
     * @param newValue New value that will be set.
     */
    public static void setCurSyncInterval(final Context context, long newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putLong(PREF_CUR_SYNC_INTERVAL, newValue).apply();
    }

    /**
     * Return true if calendar sync is enabled, false if disabled.
     * 是否同步calendar
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean shouldSyncCalendar(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_SYNC_CALENDAR, false);
    }

    /**
     * Helper method to register a settings_prefs listener. This method does not automatically handle
     * {@code unregisterOnSharedPreferenceChangeListener() un-registering} the listener at the end
     * of the {@code context} lifecycle.
     *
     * @param context  Context to be used to lookup the {@link android.content.SharedPreferences}.
     * @param listener Listener to register.
     */
    public static void registerOnSharedPreferenceChangeListener(final Context context,
                                                                SharedPreferences.OnSharedPreferenceChangeListener listener) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.registerOnSharedPreferenceChangeListener(listener);
    }

    /**
     * Helper method to un-register a settings_prefs listener typically registered with
     * {@code registerOnSharedPreferenceChangeListener()}
     *
     * @param context  Context to be used to lookup the {@link android.content.SharedPreferences}.
     * @param listener Listener to un-register.
     */
    public static void unregisterOnSharedPreferenceChangeListener(final Context context,
                                                                  SharedPreferences.OnSharedPreferenceChangeListener listener) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.unregisterOnSharedPreferenceChangeListener(listener);
    }
}
