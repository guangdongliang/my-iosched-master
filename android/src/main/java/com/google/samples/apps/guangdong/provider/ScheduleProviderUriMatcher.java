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

import android.content.UriMatcher;
import android.net.Uri;
import android.util.SparseArray;

/**
 * Provides methods to match a {@link android.net.Uri} to a {@link ScheduleUriEnum}.
 * <p />
 * All methods are thread safe, except {@link #buildUriMatcher()} and {@link #buildEnumsMap()},
 * which is why they are called only from the constructor.
 */
public class ScheduleProviderUriMatcher {

    /**
     * All methods on a {@link UriMatcher} are thread safe, except {@code addURI}.
     */
    private UriMatcher mUriMatcher;

    private SparseArray<ScheduleUriEnum> mEnumsMap = new SparseArray<>();

    /**
     * This constructor needs to be called from a thread-safe method as it isn't thread-safe itself.
     */
    public ScheduleProviderUriMatcher(){
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        buildUriMatcher();
    }
    /** 利用ScheduleUriEnum的枚举值建立mUriMatcher，并填充mEnumsMap */
    private void buildUriMatcher() {
        final String authority = ScheduleContract.CONTENT_AUTHORITY;

        ScheduleUriEnum[] uris = ScheduleUriEnum.values();
        for (int i = 0; i < uris.length; i++) {
            mUriMatcher.addURI(authority, uris[i].path, uris[i].code);
        }

        buildEnumsMap();
    }
    /** 利用ScheduleUriEnum中的code与枚举值，将mEnumsMap填充 */
    private void buildEnumsMap() {
        ScheduleUriEnum[] uris = ScheduleUriEnum.values();
        for (int i = 0; i < uris.length; i++) {
            mEnumsMap.put(uris[i].code, uris[i]);
        }
    }

    /**
     * Matches a {@code uri} to a {@link ScheduleUriEnum}.
     * 通过uri找到code，再通过code找到ScheduleUriEnm
     * @return the {@link ScheduleUriEnum}, or throws new UnsupportedOperationException if no match.
     */
    public ScheduleUriEnum matchUri(Uri uri){
        final int code = mUriMatcher.match(uri);
        try {
            return matchCode(code);
        } catch (UnsupportedOperationException e){
            throw new UnsupportedOperationException("Unknown uri " + uri);
        }
    }

    /**
     * Matches a {@code code} to a {@link ScheduleUriEnum}.
     *
     * @return the {@link ScheduleUriEnum}, or throws new UnsupportedOperationException if no match.
     * 利用code值从mEnumsMap中找到ScheduleUriEnum枚举值
     */
    public ScheduleUriEnum matchCode(int code){
        ScheduleUriEnum scheduleUriEnum = mEnumsMap.get(code);
        if (scheduleUriEnum != null){
            return scheduleUriEnum;
        } else {
            throw new UnsupportedOperationException("Unknown uri with code " + code);
        }
    }
}
