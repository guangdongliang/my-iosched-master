/*
 * Copyright 2015 Google Inc. All rights reserved.
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

package com.google.samples.apps.guangdong.session;

import com.google.common.annotations.VisibleForTesting;
import com.google.samples.apps.guangdong.Config;
import com.google.samples.apps.guangdong.R;
import com.google.samples.apps.guangdong.feedback.SessionFeedbackActivity;
import com.google.samples.apps.guangdong.framework.Model;
import com.google.samples.apps.guangdong.framework.QueryEnum;
import com.google.samples.apps.guangdong.framework.UserActionEnum;
import com.google.samples.apps.guangdong.model.TagMetadata;
import com.google.samples.apps.guangdong.provider.ScheduleContract;
import com.google.samples.apps.guangdong.service.SessionAlarmService;
import com.google.samples.apps.guangdong.service.SessionCalendarService;
import com.google.samples.apps.guangdong.util.AccountUtils;
import com.google.samples.apps.guangdong.util.AnalyticsHelper;
import com.google.samples.apps.guangdong.util.SessionsHelper;
import com.google.samples.apps.guangdong.util.UIUtils;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import static com.google.samples.apps.guangdong.util.LogUtils.LOGD;
import static com.google.samples.apps.guangdong.util.LogUtils.makeLogTag;

public class SessionDetailModel implements Model {

    protected final static String TAG = makeLogTag(SessionDetailModel.class);

    /**
     * The cursor fields for the links. The corresponding resource ids for the links descriptions
     * are in  {@link #LINKS_DESCRIPTION_RESOURCE_IDS}.
     */
    private static final String[] LINKS_CURSOR_FIELDS = {
            ScheduleContract.Sessions.SESSION_YOUTUBE_URL,
            ScheduleContract.Sessions.SESSION_MODERATOR_URL,
            ScheduleContract.Sessions.SESSION_PDF_URL,
            ScheduleContract.Sessions.SESSION_NOTES_URL
    };

    /**
     * The resource ids for the links descriptions. The corresponding cursor fields for the links
     * are in {@link #LINKS_CURSOR_FIELDS}.
     */
    private static final int[] LINKS_DESCRIPTION_RESOURCE_IDS = {
            R.string.session_link_youtube,/** YouTube视频 */
            R.string.session_link_moderator,/** 主持人 */
            R.string.session_link_pdf,/** 幻灯片 */
            R.string.session_link_notes,/** 官方备注 */
    };

    private final Context mContext;

    private final SessionsHelper mSessionsHelper;

    private String mSessionId;

    private Uri mSessionUri;

    private boolean mSessionLoaded = false;

    private String mTitle;

    private String mSubtitle;

    private int mSessionColor;
    /** 判断本Session是否在计划范围内 */
    private boolean mInSchedule;

    private boolean mInScheduleWhenSessionFirstLoaded;
    /** 本Session是否是Keynote */
    private boolean mIsKeynote;

    private long mSessionStart;

    private long mSessionEnd;

    private String mSessionAbstract;

    private String mHashTag;

    private String mUrl = "";

    private String mRoomId;

    private String mRoomName;
    /** 本Session的TagsString */
    private String mTagsString;

    private String mLiveStreamId;

    private String mYouTubeUrl;

    private String mPhotoUrl;

    private boolean mHasLiveStream = false;

    private boolean mLiveStreamVideoWatched = false;

    private boolean mHasFeedback = false;

    private String mRequirements;

    private String mSpeakersNames;

    private TagMetadata mTagMetadata;

    /**
     * Holds a list of links for the session. The first element of the {@code Pair} is the resource
     * id for the string describing the link, the second is the {@code Intent} to launch when
     * selecting the link.
     */
    private List<Pair<Integer, Intent>> mLinks = new ArrayList<Pair<Integer, Intent>>();

    private List<Speaker> mSpeakers = new ArrayList<Speaker>();

    private StringBuilder mBuffer = new StringBuilder();

    public SessionDetailModel(Uri sessionUri, Context context, SessionsHelper sessionsHelper) {
        mContext = context;
        mSessionsHelper = sessionsHelper;
        mSessionUri = sessionUri;
    }

    public String getSessionId() {
        return mSessionId;
    }

    public String getSessionTitle() {
        return mTitle;
    }

    public String getSessionSubtitle() {
        return mSubtitle;
    }

    public String getSessionUrl() {
        return mUrl;
    }

    public String getLiveStreamId() {
        return mLiveStreamId;
    }

    public String getYouTubeUrl() {
        return mYouTubeUrl;
    }

    public int getSessionColor() {
        return mSessionColor;
    }

    public String getSessionAbstract() {
        return mSessionAbstract;
    }

    public boolean getLiveStreamVideoWatched() {
        return mLiveStreamVideoWatched;
    }
    /** 根据 现在时间、startTime、endTime 判断Session是否结束 */
    public boolean isSessionOngoing() {
        long currentTimeMillis = UIUtils.getCurrentTime(mContext);
        return currentTimeMillis > mSessionStart && currentTimeMillis <= mSessionEnd;
    }
    /** 根据 根据 现在时间、startTime 判断Session是否开始 */
    public boolean hasSessionStarted() {
        long currentTimeMillis = UIUtils.getCurrentTime(mContext);
        return currentTimeMillis > mSessionStart;
    }
    /** 根据 现在时间、endTime判断是否结束 */
    public boolean hasSessionEnded() {
        long currentTimeMillis = UIUtils.getCurrentTime(mContext);
        return currentTimeMillis > mSessionEnd;
    }

    /**
     * Returns the number of minutes, rounded down, since session has started, or 0 if not started
     * yet.
     * Session已经进行的时间
     */
    public long minutesSinceSessionStarted() {
        if (!hasSessionStarted()) {
            return 0l;
        } else {
            long currentTimeMillis = UIUtils.getCurrentTime(mContext);
            // Rounded down number of minutes.
            return (currentTimeMillis - mSessionStart) / 60000;
        }
    }

    /**
     * Returns the number of minutes, rounded up, until session stars, or 0 if already started.
     * 还有多长时间开始
     */
    public long minutesUntilSessionStarts() {
        if (hasSessionStarted()) {
            return 0l;
        } else {
            long currentTimeMillis = UIUtils.getCurrentTime(mContext);
            // Rounded up number of minutes.
            return (mSessionStart - currentTimeMillis) / 60000 + 1;
        }
    }
    /** 根据时间判断是否可以开始Feedback */
    public boolean isSessionReadyForFeedback() {
        long currentTimeMillis = UIUtils.getCurrentTime(mContext);
        return currentTimeMillis
                > mSessionEnd - SessionDetailConstants.FEEDBACK_MILLIS_BEFORE_SESSION_END_MS;
    }
    /** 是否有直播视频流 */
    public boolean hasLiveStream() {
        return mHasLiveStream || !TextUtils.isEmpty(mYouTubeUrl);
    }

    public boolean isInSchedule() {
        return mInSchedule;
    }

    public boolean isInScheduleWhenSessionFirstLoaded() {
        return mInScheduleWhenSessionFirstLoaded;
    }

    public boolean isKeynote() {
        return mIsKeynote;
    }

    public boolean hasFeedback() {
        return mHasFeedback;
    }

    public boolean hasPhotoUrl() {
        return !TextUtils.isEmpty(mPhotoUrl);
    }

    public String getPhotoUrl() {
        return mPhotoUrl;
    }

    public String getRequirements() {
        return mRequirements;
    }

    public String getHashTag() {
        return mHashTag;
    }

    public TagMetadata getTagMetadata() {
        return mTagMetadata;
    }

    public String getTagsString() {
        return mTagsString;
    }

    public List<Pair<Integer, Intent>> getLinks() {
        return mLinks;
    }

    public List<Speaker> getSpeakers() {
        return mSpeakers;
    }

    public boolean hasSummaryContent() {
        return !TextUtils.isEmpty(mSessionAbstract) || !TextUtils.isEmpty(mRequirements);
    }

    @Override
    public QueryEnum[] getQueries() {
        return SessionDetailQueryEnum.values();
    }

    @Override
    public boolean readDataFromCursor(Cursor cursor, QueryEnum query) {
        boolean success = false;

        if (cursor != null && cursor.moveToFirst()) {
            if (SessionDetailQueryEnum.SESSIONS == query) {
                /** 读取Session的数据 */
                readDataFromSessionCursor(cursor);
                mSessionLoaded = true;
                success = true;
            } else if (SessionDetailQueryEnum.TAG_METADATA == query) {
                /** 读取TagMetadata的数据 */
                readDataFromTagMetadataCursor(cursor);
                success = true;
            } else if (SessionDetailQueryEnum.FEEDBACK == query) {
                readDataFromFeedbackCursor(cursor);
                success = true;
            } else if (SessionDetailQueryEnum.SPEAKERS == query) {
                readDataFromSpeakersCursor(cursor);
                success = true;
            } else if (SessionDetailQueryEnum.MY_VIEWED_VIDEOS == query) {
                readDataFromMyViewedVideosCursor(cursor);
                success = true;
            }
        }

        return success;
    }
    /** 如果从DB中获得的videoID等于本类中的mLiveStreamId，则mLiveStreamVideoWatched=true */
    private void readDataFromMyViewedVideosCursor(Cursor cursor) {
        String videoID = cursor.getString(cursor.getColumnIndex(
                ScheduleContract.MyViewedVideos.VIDEO_ID));
        if (videoID != null && videoID.equals(mLiveStreamId)) {
            mLiveStreamVideoWatched = true;
        }
    }

    private void readDataFromSessionCursor(Cursor cursor) {
        mTitle = cursor.getString(cursor.getColumnIndex(
                ScheduleContract.Sessions.SESSION_TITLE));

        mInSchedule = cursor.getInt(cursor.getColumnIndex(
                ScheduleContract.Sessions.SESSION_IN_MY_SCHEDULE)) != 0;
        if (!mSessionLoaded) {
            mInScheduleWhenSessionFirstLoaded = mInSchedule;
        }
        mTagsString = cursor
                .getString(cursor.getColumnIndex(ScheduleContract.Sessions.SESSION_TAGS));
        mIsKeynote = mTagsString != null && mTagsString.contains(Config.Tags.SPECIAL_KEYNOTE);

        mSessionColor = cursor.getInt(
                cursor.getColumnIndex(ScheduleContract.Sessions.SESSION_COLOR));
        if (mSessionColor == 0) {
            mSessionColor = mContext.getResources().getColor(R.color.default_session_color);
        } else {
            mSessionColor = UIUtils.setColorOpaque(mSessionColor);
        }

        mLiveStreamId = cursor
                .getString(cursor.getColumnIndex(ScheduleContract.Sessions.SESSION_LIVESTREAM_ID));

        mHasLiveStream = !TextUtils.isEmpty(mLiveStreamId);

        mYouTubeUrl = cursor.getString(cursor.getColumnIndex(
                ScheduleContract.Sessions.SESSION_YOUTUBE_URL));

        mSessionStart = cursor
                .getLong(cursor.getColumnIndex(ScheduleContract.Sessions.SESSION_START));
        mSessionEnd = cursor.getLong(cursor.getColumnIndex(ScheduleContract.Sessions.SESSION_END));

        mRoomName = cursor.getString(cursor.getColumnIndex(ScheduleContract.Sessions.ROOM_NAME));
        mRoomId = cursor.getString(cursor.getColumnIndex(ScheduleContract.Sessions.ROOM_ID));

        mHashTag = cursor
                .getString(cursor.getColumnIndex(ScheduleContract.Sessions.SESSION_HASHTAG));

        mPhotoUrl =
                cursor.getString(
                        cursor.getColumnIndex(ScheduleContract.Sessions.SESSION_PHOTO_URL));
        mUrl = cursor.getString(cursor.getColumnIndex(ScheduleContract.Sessions.SESSION_URL));

        mSessionAbstract = cursor
                .getString(cursor.getColumnIndex(ScheduleContract.Sessions.SESSION_ABSTRACT));

        mSpeakersNames = cursor
                .getString(cursor.getColumnIndex(ScheduleContract.Sessions.SESSION_SPEAKER_NAMES));

        mRequirements = cursor
                .getString(cursor.getColumnIndex(ScheduleContract.Sessions.SESSION_REQUIREMENTS));

        formatSubtitle();

        buildLinks(cursor);
    }

    @VisibleForTesting
    public void formatSubtitle() {
        mSubtitle = UIUtils.formatSessionSubtitle(
                mSessionStart, mSessionEnd, mRoomName, mBuffer, mContext);
        if (mHasLiveStream) {
            mSubtitle += " " + UIUtils.getLiveBadgeText(mContext, mSessionStart, mSessionEnd);
        }
    }
    /** 将直播链接、反馈链接、youTube视频、幻灯片等的链接加入mLinks中 */
    private void buildLinks(Cursor cursor) {
        mLinks.clear();

        if (hasLiveStream() && isSessionOngoing()) {
            mLinks.add(new Pair<Integer, Intent>(
                    R.string.session_link_livestream,/** 观看直播 */
                    getWatchLiveIntent()));
        }

        if (!hasFeedback() && isSessionReadyForFeedback()) {
            mLinks.add(new Pair<Integer, Intent>(
                    R.string.session_feedback_submitlink,/** 提交反馈 */
                    getFeedbackIntent()
            ));
        }

        for (int i = 0; i < LINKS_CURSOR_FIELDS.length; i++) {
            final String linkUrl = cursor.getString(cursor.getColumnIndex(LINKS_CURSOR_FIELDS[i]));
            if (TextUtils.isEmpty(linkUrl)) {
                continue;
            }

            mLinks.add(new Pair<Integer, Intent>(
                    LINKS_DESCRIPTION_RESOURCE_IDS[i],
                    new Intent(Intent.ACTION_VIEW, Uri.parse(linkUrl))
                            .addFlags(getFlagForUrlLink())
            ));
        }
    }
    /** 如果一个Intent中包含此属性，则它转向的那个Activity以及在那个Activity其上的所有Activity都会在task重置时被清除出task */
    private int getFlagForUrlLink() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
        } else {
            return Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
        }
    }
    /** 获得观看直播的Intent */
    public Intent getWatchLiveIntent() {
        final String youtubeLink = String.format(
                Locale.US, Config.VIDEO_LIBRARY_URL_FMT,
                TextUtils.isEmpty(mLiveStreamId) ? "" : mLiveStreamId
        );
        return new Intent(Intent.ACTION_VIEW, Uri.parse(youtubeLink));
    }
    /** 获得进行反馈的Intent */
    public Intent getFeedbackIntent() {
        return new Intent(Intent.ACTION_VIEW, mSessionUri, mContext,
                SessionFeedbackActivity.class);
    }
    /** 从cursor中产生一个TagMetadata */
    private void readDataFromTagMetadataCursor(Cursor cursor) {
        mTagMetadata = new TagMetadata(cursor);
    }
    /** 从带有feedback的Cursor那里，根据cursor的行数是否有feedback */
    private void readDataFromFeedbackCursor(Cursor cursor) {
        mHasFeedback = cursor.getCount() > 0;
    }
    /** 从带有Speaker信息的cursor中读取Speaker并加入mSpeakers中 */
    private void readDataFromSpeakersCursor(Cursor cursor) {
        mSpeakers.clear();

        // Not using while(cursor.moveToNext()) because it would lead to issues when writing tests.
        // Either we would mock cursor.moveToNext() to return true and the test would have infinite
        // loop, or we would mock cursor.moveToNext() to return false, and the test would be for an
        // empty cursor.
        int count = cursor.getCount();
        for (int i = 0; i < count; i++) {
            cursor.moveToPosition(i);
            final String speakerName =
                    cursor.getString(cursor.getColumnIndex(ScheduleContract.Speakers.SPEAKER_NAME));
            if (TextUtils.isEmpty(speakerName)) {
                continue;
            }

            final String speakerImageUrl = cursor.getString(
                    cursor.getColumnIndex(ScheduleContract.Speakers.SPEAKER_IMAGE_URL));
            final String speakerCompany = cursor.getString(
                    cursor.getColumnIndex(ScheduleContract.Speakers.SPEAKER_COMPANY));
            final String speakerUrl = cursor.getString(
                    cursor.getColumnIndex(ScheduleContract.Speakers.SPEAKER_URL));
            final String speakerPlusoneUrl = cursor.getString(
                    cursor.getColumnIndex(ScheduleContract.Speakers.SPEAKER_PLUSONE_URL));
            final String speakerTwitterUrl = cursor.getString(
                    cursor.getColumnIndex(ScheduleContract.Speakers.SPEAKER_TWITTER_URL));
            final String speakerAbstract = cursor.getString(
                    cursor.getColumnIndex(ScheduleContract.Speakers.SPEAKER_ABSTRACT));

            mSpeakers.add(new Speaker(speakerName, speakerImageUrl, speakerCompany, speakerUrl,
                    speakerPlusoneUrl, speakerTwitterUrl, speakerAbstract));
        }
    }
    /** 根据不同的loaderId，利用SessionDetailQueryEnum，建立不同的CursorLoader */
    @Override
    public Loader<Cursor> createCursorLoader(int loaderId, Uri uri, Bundle args) {
        CursorLoader loader = null;

        if (loaderId == SessionDetailQueryEnum.SESSIONS.getId()) {
            mSessionUri = uri;
            mSessionId = getSessionId(uri);
            loader = getCursorLoaderInstance(mContext, uri,
                    SessionDetailQueryEnum.SESSIONS.getProjection(), null, null, null);
        } else if (loaderId == SessionDetailQueryEnum.SPEAKERS.getId() && mSessionUri != null) {
            Uri speakersUri = getSpeakersDirUri(mSessionId);
            loader = getCursorLoaderInstance(mContext, speakersUri,
                    SessionDetailQueryEnum.SPEAKERS.getProjection(), null, null,
                    ScheduleContract.Speakers.DEFAULT_SORT);
        } else if (loaderId == SessionDetailQueryEnum.FEEDBACK.getId()) {
            Uri feedbackUri = getFeedbackUri(mSessionId);
            loader = getCursorLoaderInstance(mContext, feedbackUri,
                    SessionDetailQueryEnum.FEEDBACK.getProjection(), null, null, null);
        } else if (loaderId == SessionDetailQueryEnum.TAG_METADATA.getId()) {
            loader = getTagMetadataLoader();
        } else if (loaderId == SessionDetailQueryEnum.MY_VIEWED_VIDEOS.getId()) {
            LOGD(TAG, "Starting My Viewed Videos query");
            Uri myPlayedVideoUri = ScheduleContract.MyViewedVideos.buildMyViewedVideosUri(
                    AccountUtils.getActiveAccountName(mContext));
            loader = getCursorLoaderInstance(mContext, myPlayedVideoUri,
                    SessionDetailQueryEnum.MY_VIEWED_VIDEOS.getProjection(), null, null, null);
        }
        return loader;
    }

    @VisibleForTesting
    public CursorLoader getCursorLoaderInstance(Context context, Uri uri, String[] projection,
                                                String selection, String[] selectionArgs, String sortOrder) {
        return new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
    }

    @VisibleForTesting
    public CursorLoader getTagMetadataLoader() {
        return TagMetadata.createCursorLoader(mContext);
    }

    @VisibleForTesting
    public Uri getFeedbackUri(String sessionId) {
        return ScheduleContract.Feedback.buildFeedbackUri(sessionId);
    }

    @VisibleForTesting
    public Uri getSpeakersDirUri(String sessionId) {
        return ScheduleContract.Sessions.buildSpeakersDirUri(sessionId);
    }

    @VisibleForTesting
    public String getSessionId(Uri uri) {
        return ScheduleContract.Sessions.getSessionId(uri);
    }
    /** 添加本Session到计划中，或者删除已添加计划
     *  在Map中查看本Room
     *  分享本Session
     */
    @Override
    public boolean requestModelUpdate(UserActionEnum action, @Nullable Bundle args) {
        boolean success = false;
        if (action == SessionDetailUserActionEnum.STAR) {
            mInSchedule = true;
            mSessionsHelper.setSessionStarred(mSessionUri, true, null);
            amendCalendarAndSetUpNotificationIfRequired();
            success = true;
            sendAnalyticsEventForStarUnstarSession(true);
        } else if (action == SessionDetailUserActionEnum.UNSTAR) {
            mInSchedule = false;
            mSessionsHelper.setSessionStarred(mSessionUri, false, null);
            amendCalendarAndSetUpNotificationIfRequired();
            success = true;
            sendAnalyticsEventForStarUnstarSession(false);
        } else if (action == SessionDetailUserActionEnum.SHOW_MAP) {
            // ANALYTICS EVENT: Click on Map action in Session Details page.
            // Contains: Session title/subtitle
            sendAnalyticsEvent("Session", "Map", mTitle);
            mSessionsHelper.startMapActivity(mRoomId);
            success = true;
        } else if (action == SessionDetailUserActionEnum.SHOW_SHARE) {
            // On ICS+ devices, we normally won't reach this as ShareActionProvider will handle
            // sharing.
            mSessionsHelper.shareSession(mContext, R.string.share_template, mTitle,
                    mHashTag, mUrl);
            success = true;
        }
        return success;
    }
    /** 利用SessionCalendarService进行ADD或者REMOVE */
    private void amendCalendarAndSetUpNotificationIfRequired() {
        /** 如果Session已经开始则直接跳出 */
        if (!hasSessionStarted()) {
            Intent intent;
            if (mInSchedule) {
                intent = new Intent(SessionCalendarService.ACTION_ADD_SESSION_CALENDAR,
                        mSessionUri);
                intent.putExtra(SessionCalendarService.EXTRA_SESSION_START,
                        mSessionStart);
                intent.putExtra(SessionCalendarService.EXTRA_SESSION_END,
                        mSessionEnd);
                intent.putExtra(SessionCalendarService.EXTRA_SESSION_ROOM, mRoomName);
                intent.putExtra(SessionCalendarService.EXTRA_SESSION_TITLE, mTitle);
            } else {
                intent = new Intent(SessionCalendarService.ACTION_REMOVE_SESSION_CALENDAR,
                        mSessionUri);
                intent.putExtra(SessionCalendarService.EXTRA_SESSION_START,
                        mSessionStart);
                intent.putExtra(SessionCalendarService.EXTRA_SESSION_END,
                        mSessionEnd);
                intent.putExtra(SessionCalendarService.EXTRA_SESSION_TITLE, mTitle);
            }
            intent.setClass(mContext, SessionCalendarService.class);
            mContext.startService(intent);

            if (mInSchedule) {
                setUpNotification();
            }
        }
    }
    /** 利用SessionAlarmService设置开始、feedback的Notification */
    private void setUpNotification() {
        Intent scheduleIntent;

        // Schedule session notification
        if (!hasSessionStarted()) {
            LOGD(TAG, "Scheduling notification about session start.");
            scheduleIntent = new Intent(
                    SessionAlarmService.ACTION_SCHEDULE_STARRED_BLOCK,
                    null, mContext, SessionAlarmService.class);
            scheduleIntent.putExtra(SessionAlarmService.EXTRA_SESSION_START, mSessionStart);
            scheduleIntent.putExtra(SessionAlarmService.EXTRA_SESSION_END, mSessionEnd);
            mContext.startService(scheduleIntent);
        } else {
            LOGD(TAG, "Not scheduling notification about session start, too late.");
        }

        // Schedule feedback notification
        if (!hasSessionEnded()) {
            LOGD(TAG, "Scheduling notification about session feedback.");
            scheduleIntent = new Intent(
                    SessionAlarmService.ACTION_SCHEDULE_FEEDBACK_NOTIFICATION,
                    null, mContext, SessionAlarmService.class);
            scheduleIntent.putExtra(SessionAlarmService.EXTRA_SESSION_ID, mSessionId);
            scheduleIntent.putExtra(SessionAlarmService.EXTRA_SESSION_START, mSessionStart);
            scheduleIntent.putExtra(SessionAlarmService.EXTRA_SESSION_END, mSessionEnd);
            scheduleIntent.putExtra(SessionAlarmService.EXTRA_SESSION_TITLE, mTitle);
            mContext.startService(scheduleIntent);
        } else {
            LOGD(TAG, "Not scheduling feedback notification, too late.");
        }
    }

    @VisibleForTesting
    public void sendAnalyticsEvent(String category, String action, String label) {
        AnalyticsHelper.sendEvent(category, action, label);
    }

    private void sendAnalyticsEventForStarUnstarSession(boolean starred) {
        // ANALYTICS EVENT: Add or remove a session from My Schedule (starred vs unstarred)
        // Contains: Session title, whether it was added or removed (starred or unstarred)
        sendAnalyticsEvent("Session", starred ? "Starred" : "Unstarred", mTitle);
    }

    public static class Speaker {

        private String mName;

        private String mImageUrl;

        private String mCompany;

        private String mUrl;

        private String mPlusoneUrl;

        private String mTwitterUrl;

        private String mAbstract;

        public Speaker(String name, String imageUrl, String company, String url, String plusoneUrl,
                       String twitterUrl, String anAbstract) {
            mName = name;
            mImageUrl = imageUrl;
            mCompany = company;
            mUrl = url;
            mPlusoneUrl = plusoneUrl;
            mTwitterUrl = twitterUrl;
            mAbstract = anAbstract;
        }

        public String getName() {
            return mName;
        }

        public String getImageUrl() {
            return mImageUrl;
        }

        public String getCompany() {
            return mCompany;
        }

        public String getUrl() {
            return mUrl;
        }

        public String getPlusoneUrl() {
            return mPlusoneUrl;
        }

        public String getTwitterUrl() {
            return mTwitterUrl;
        }

        public String getAbstract() {
            return mAbstract;
        }
    }

    public enum SessionDetailQueryEnum implements QueryEnum {
        SESSIONS(0, new String[]{ScheduleContract.Sessions.SESSION_START,
                ScheduleContract.Sessions.SESSION_END,
                ScheduleContract.Sessions.SESSION_LEVEL,
                ScheduleContract.Sessions.SESSION_TITLE,
                ScheduleContract.Sessions.SESSION_ABSTRACT,
                ScheduleContract.Sessions.SESSION_REQUIREMENTS,
                ScheduleContract.Sessions.SESSION_IN_MY_SCHEDULE,
                ScheduleContract.Sessions.SESSION_HASHTAG,
                ScheduleContract.Sessions.SESSION_URL,
                ScheduleContract.Sessions.SESSION_YOUTUBE_URL,
                ScheduleContract.Sessions.SESSION_PDF_URL,
                ScheduleContract.Sessions.SESSION_NOTES_URL,
                ScheduleContract.Sessions.SESSION_LIVESTREAM_ID,
                ScheduleContract.Sessions.SESSION_MODERATOR_URL,
                ScheduleContract.Sessions.ROOM_ID,
                ScheduleContract.Rooms.ROOM_NAME,
                ScheduleContract.Sessions.SESSION_COLOR,
                ScheduleContract.Sessions.SESSION_PHOTO_URL,
                ScheduleContract.Sessions.SESSION_RELATED_CONTENT,
                ScheduleContract.Sessions.SESSION_TAGS,
                ScheduleContract.Sessions.SESSION_SPEAKER_NAMES}),
        SPEAKERS(1, new String[]{ScheduleContract.Speakers.SPEAKER_NAME,
                ScheduleContract.Speakers.SPEAKER_IMAGE_URL,
                ScheduleContract.Speakers.SPEAKER_COMPANY,
                ScheduleContract.Speakers.SPEAKER_ABSTRACT,
                ScheduleContract.Speakers.SPEAKER_URL,
                ScheduleContract.Speakers.SPEAKER_PLUSONE_URL,
                ScheduleContract.Speakers.SPEAKER_TWITTER_URL}),
        FEEDBACK(2, new String[]{ScheduleContract.Feedback.SESSION_ID}),
        TAG_METADATA(3, null),
        MY_VIEWED_VIDEOS(4, new String[]{ScheduleContract.MyViewedVideos.VIDEO_ID});

        private int id;

        private String[] projection;

        SessionDetailQueryEnum(int id, String[] projection) {
            this.id = id;
            this.projection = projection;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public String[] getProjection() {
            return projection;
        }

    }

    public enum SessionDetailUserActionEnum implements UserActionEnum {
        STAR(1),
        UNSTAR(2),
        SHOW_MAP(3),
        SHOW_SHARE(4);

        private int id;

        SessionDetailUserActionEnum(int id) {
            this.id = id;
        }

        @Override
        public int getId() {
            return id;
        }

    }
}