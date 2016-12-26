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
package com.google.samples.apps.guangdong.server.schedule.model.validator;



public class Converters {
  public static final DateTimeConverter DATETIME = new DateTimeConverter();
  public static final BooleanConverter BOOLEAN = new BooleanConverter();
  public static final YoutubeURLConverter YOUTUBE_URL = new YoutubeURLConverter(true);
  public static final SessionURLConverter SESSION_URL = new SessionURLConverter();
  public static final GPlusURLConverter GPLUS_URL = new GPlusURLConverter();
  public static final TwitterURLConverter TWITTER_URL = new TwitterURLConverter();
  public static final PhotoURLConverter SESSION_PHOTO_URL = new PhotoURLConverter("sessions");
  public static final PhotoURLConverter SPEAKER_PHOTO_URL = new PhotoURLConverter("speakers");
  public static final RegexpConverter URL = new RegexpConverter("\\w{1,}:\\/\\/.*");
  public static final RegexpConverter COLOR = new RegexpConverter("#[a-zA-Z0-9]{6}");
  public static final IntegerConverter INTEGER = new IntegerConverter();
  public static final IntegerToStringConverter INTEGER_TO_STRING = new IntegerToStringConverter();
  public static final TagNameConverter TAG_NAME= new TagNameConverter();
  public static final StringObfuscateConverter OBFUSCATE= new StringObfuscateConverter();
}
