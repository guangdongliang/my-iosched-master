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

package com.google.samples.apps.guangdong.feedback;


import com.google.common.annotations.VisibleForTesting;
import com.google.samples.apps.guangdong.framework.Model;
import com.google.samples.apps.guangdong.framework.QueryEnum;
import com.google.samples.apps.guangdong.framework.UserActionEnum;
import com.google.samples.apps.guangdong.provider.ScheduleContract;
import com.google.samples.apps.guangdong.util.AnalyticsHelper;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

public class SessionFeedbackModel implements Model {

    protected final static String DATA_RATING_INT = "DATA_RATING_INT";

    protected final static String DATA_SESSION_RELEVANT_ANSWER_INT = "DATA_SESSION_RELEVANT_ANSWER_INT";

    protected final static String DATA_CONTENT_ANSWER_INT = "DATA_CONTENT_ANSWER_INT";

    protected final static String DATA_SPEAKER_ANSWER_INT = "DATA_SPEAKER_ANSWER_INT";

    protected final static String DATA_COMMENT_STRING = "DATA_COMMENT_STRING";

    private final Context mContext;

    private FeedbackHelper mFeedbackHelper;

    private Uri mSessionUri;

    private String mTitleString;

    private String mSpeakersString;

    public SessionFeedbackModel(Uri sessionUri, Context context, FeedbackHelper feedbackHelper) {
        mContext = context;
        mSessionUri = sessionUri;
        mFeedbackHelper = feedbackHelper;
    }

    public String getSessionTitle() {
        return mTitleString;
    }

    public String getSessionSpeakers() {
        return mSpeakersString;
    }
    /** 获取SessionFeedbackQuery中的枚举值 */
    @Override
    public QueryEnum[] getQueries() {
        return SessionFeedbackQueryEnum.values();
    }
    /** 从cursor中读取枚举SESSION中所指的列 */
    @Override
    public boolean readDataFromCursor(Cursor cursor, QueryEnum query) {
        if (!cursor.moveToFirst()) {
            return false;
        }

        if (SessionFeedbackQueryEnum.SESSION == query) {
            mTitleString = cursor.getString(cursor.getColumnIndex(
                    ScheduleContract.Sessions.SESSION_TITLE));

            mSpeakersString = cursor.getString(cursor.getColumnIndex(
                    ScheduleContract.Sessions.SESSION_SPEAKER_NAMES));

            return true;
        }

        return false;
    }
    /** 利用uri和SESSION中所指的列创建CursorLoader */
    @Override
    public Loader<Cursor> createCursorLoader(int loaderId, Uri uri, Bundle args) {
        CursorLoader loader = null;
        if (loaderId == SessionFeedbackQueryEnum.SESSION.getId()) {
            loader = getCursorLoaderInstance(mContext, uri,
                    SessionFeedbackQueryEnum.SESSION.getProjection(), null, null, null);
        }
        return loader;
    }
    /** 利用uri和其他参数创建CursorLoader */
    @VisibleForTesting
    public CursorLoader getCursorLoaderInstance(Context context, Uri uri, String[] projection,
                                                String selection, String[] selectionArgs, String sortOrder) {
        return new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
    }
    /** 更新DB中的SessionFeedbackData值，然后申请Google分析 */
    @Override
    public boolean requestModelUpdate(UserActionEnum action, @Nullable Bundle args) {
        if (SessionFeedbackUserActionEnum.SUBMIT == action) {
            mFeedbackHelper.saveSessionFeedback(new SessionFeedbackData(getSessionId(mSessionUri),
                    args.getInt(DATA_RATING_INT), args.getInt(DATA_SESSION_RELEVANT_ANSWER_INT),
                    args.getInt(DATA_CONTENT_ANSWER_INT), args.getInt(DATA_SPEAKER_ANSWER_INT),
                    args.getString(DATA_COMMENT_STRING)));

            // ANALYTICS EVENT: Send session feedback
            // Contains: Session title.  Feedback is NOT included.
            sendAnalyticsEvent("Session", "Feedback", mTitleString);

            return true;
        } else {
            return false;
        }
    }

    @VisibleForTesting
    public String getSessionId(Uri uri) {
        return ScheduleContract.Sessions.getSessionId(uri);
    }
    /** 发送分析指令进行分析 */
    @VisibleForTesting
    public void sendAnalyticsEvent(String category, String action, String label) {
        AnalyticsHelper.sendEvent(category, action, label);
    }

    public enum SessionFeedbackQueryEnum implements QueryEnum {
        SESSION(0, new String[]{
                ScheduleContract.Sessions.SESSION_TITLE,
                ScheduleContract.Sessions.SESSION_SPEAKER_NAMES});

        private int id;

        private String[] projection;

        SessionFeedbackQueryEnum(int id, String[] projection) {
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

    public enum SessionFeedbackUserActionEnum implements UserActionEnum {
        SUBMIT(1);

        private int id;

        SessionFeedbackUserActionEnum(int id) {
            this.id = id;
        }

        @Override
        public int getId() {
            return id;
        }

    }

    public static class SessionFeedbackData {

        public String sessionId;

        public int sessionRating;

        public int sessionRelevantAnswer;

        public int contentAnswer;

        public int speakerAnswer;

        public String comments;

        public SessionFeedbackData(String sessionId, int sessionRating, int sessionRelevantAnswer,
                                   int contentAnswer, int speakerAnswer, String comments) {
            this.sessionId = sessionId;
            this.sessionRating = sessionRating;
            this.sessionRelevantAnswer = sessionRelevantAnswer;
            this.contentAnswer = contentAnswer;
            this.speakerAnswer = speakerAnswer;
            this.comments = comments;
        }

        @Override
        public String toString() {
            return "SessionId: " + sessionId +
                    " SessionRating: " + sessionRating +
                    " SessionRelevantAnswer: " + sessionRelevantAnswer +
                    " ContentAnswer: " + contentAnswer +
                    " SpeakerAnswer: " + speakerAnswer +
                    " Comments: " + comments;
        }
    }

}