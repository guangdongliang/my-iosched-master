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

package com.google.samples.apps.guangdong.provider;

import android.net.Uri;
import android.text.TextUtils;

/**
 * Provides helper methods for specifying query parameters on {@code Uri}s.
 */
public class ScheduleContractHelper {

    public static final String QUERY_PARAMETER_DISTINCT = "distinct";

    private static final String QUERY_PARAMETER_OVERRIDE_ACCOUNT_NAME = "overrideAccountName";

    private static final String QUERY_PARAMETER_CALLER_IS_SYNC_ADAPTER = "callerIsSyncAdapter";

    /** 根据uri查询参数中是否有"callerIsSyncAdapter"，判断是否是呼叫同步的uri */
    public static boolean isUriCalledFromSyncAdapter(Uri uri) {
        return uri.getBooleanQueryParameter(QUERY_PARAMETER_CALLER_IS_SYNC_ADAPTER, false);
    }
    /** 将参数uri设置成呼叫同步的uri */
    public static Uri setUriAsCalledFromSyncAdapter(Uri uri) {
        /** [scheme:][//authority][path][?query][#fragment]
         * 除了scheme、authority是必须要有的，其它的几个path、query、fragment，
         * 它们每一个可以选择性的要或不要，但顺序不能变
         * */
        return uri.buildUpon().appendQueryParameter(QUERY_PARAMETER_CALLER_IS_SYNC_ADAPTER, "true")
                .build();
    }
    /** 是否在查询distinct */
    public static boolean isQueryDistinct(Uri uri){
        return !TextUtils.isEmpty(uri.getQueryParameter(QUERY_PARAMETER_DISTINCT));
    }
    /** 将参数parameter添加"distinct "来标准化查询参数 */
    public static String formatQueryDistinctParameter(String parameter){
        return ScheduleContractHelper.QUERY_PARAMETER_DISTINCT + " " + parameter;
    }
    /** 从uri中获得"overrideAccountName"的值 */
    public static String getOverrideAccountName(Uri uri) {
        return uri.getQueryParameter(QUERY_PARAMETER_OVERRIDE_ACCOUNT_NAME);
    }

    /**
     * Adds an account override parameter to the {@code uri}. This is used by the
     * {@link ScheduleProvider} when fetching account-specific data.
     * 向查询参数中添加"overrideAccountName"参数
     */
    public static Uri addOverrideAccountName(Uri uri, String accountName) {
        return uri.buildUpon().appendQueryParameter(
                QUERY_PARAMETER_OVERRIDE_ACCOUNT_NAME, accountName).build();
    }
}
