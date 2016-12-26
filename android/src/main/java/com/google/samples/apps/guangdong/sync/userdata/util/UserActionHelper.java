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
package com.google.samples.apps.guangdong.sync.userdata.util;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.util.Log;

import com.google.samples.apps.guangdong.provider.ScheduleContract;
import com.google.samples.apps.guangdong.provider.ScheduleContractHelper;
import com.google.samples.apps.guangdong.sync.userdata.UserAction;

import java.util.ArrayList;
import java.util.List;

import static com.google.samples.apps.guangdong.util.LogUtils.makeLogTag;

/**
 * Helper class to handle the format of the User Actions done on the device.
 * 利用userActions更新本地的数据库
 */
public class UserActionHelper {
    private static final String TAG = makeLogTag(UserActionHelper.class);

    /**
     * Update content providers as a batch command based on the given list of User Actions.
     * 利用userActions中的action.type，产生不同的ContentProvider，然后更新到数据库中
     */
    static public void updateContentProvider(Context context, List<UserAction> userActions,
            String account) {
        ArrayList<ContentProviderOperation> batch = new ArrayList<>();
        for (UserAction action: userActions) {
            batch.add(createUpdateOperation(context, action, account));
        }
        try {
            context.getContentResolver().applyBatch(ScheduleContract.CONTENT_AUTHORITY, batch);
        } catch (RemoteException e) {
            Log.e(TAG, "Could not apply operations", e);
        } catch (OperationApplicationException e) {
            Log.e(TAG, "Could not apply operations", e);
        }
    }

    /**
     * Creates the correct content provider update operation depending on the type of the user
     * action.
     * 根据不同的action.type，产生不同的ContentProviderOperation
     */
    static private ContentProviderOperation createUpdateOperation(Context context,
            UserAction action, String account) {
        if (action.type == UserAction.TYPE.ADD_STAR) {
            return ContentProviderOperation
                    .newInsert(
                            ScheduleContractHelper.addOverrideAccountName(
                                    ScheduleContract.MySchedule.CONTENT_URI, account))
                    .withValue(ScheduleContract.MySchedule.MY_SCHEDULE_DIRTY_FLAG, "0")
                    .withValue(ScheduleContract.MySchedule.SESSION_ID, action.sessionId)
                    .build();
        } else if (action.type == UserAction.TYPE.SUBMIT_FEEDBACK) {
            return ContentProviderOperation
                    .newInsert(
                            ScheduleContractHelper.addOverrideAccountName(
                                    ScheduleContract.MyFeedbackSubmitted.CONTENT_URI, account))
                    .withValue(ScheduleContract.MyFeedbackSubmitted
                            .MY_FEEDBACK_SUBMITTED_DIRTY_FLAG, "0")
                    .withValue(ScheduleContract.MyFeedbackSubmitted.SESSION_ID, action.sessionId)
                    .build();
        } else if (action.type == UserAction.TYPE.VIEW_VIDEO) {
            return ContentProviderOperation
                    .newInsert(
                            ScheduleContractHelper.addOverrideAccountName(
                                    ScheduleContract.MyViewedVideos.CONTENT_URI, account))
                    .withValue(ScheduleContract.MyViewedVideos.MY_VIEWED_VIDEOS_DIRTY_FLAG, "0")
                    .withValue(ScheduleContract.MyViewedVideos.VIDEO_ID, action.videoId)
                    .build();
        } else {
            /** REMOVE_STAR */
            return ContentProviderOperation
                    .newDelete(
                            ScheduleContractHelper.addOverrideAccountName(
                                    ScheduleContract.MySchedule.CONTENT_URI, account))
                    .withSelection(
                            ScheduleContract.MySchedule.SESSION_ID + " = ? AND " +
                            ScheduleContract.MySchedule.MY_SCHEDULE_ACCOUNT_NAME + " = ? ",
                            new String[]{action.sessionId, account}
                    )
                    .build();
        }
    }
}
