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
package com.google.samples.apps.guangdong.server.schedule.server.input;

import com.google.samples.apps.guangdong.server.schedule.input.fetcher.RemoteFilesEntityFetcherFactory;
import com.google.samples.apps.guangdong.server.schedule.model.InputJsonKeys;

/**
 * Encapsulation for all the input extracted from CloudStorage static files.
 */
public class VendorStaticInput extends DataSourceInput<InputJsonKeys.VendorAPISource.MainTypes> {

  public static final String RAW_SESSION_DATA_FILE = "__raw_session_data.json";

  public VendorStaticInput() {
    super(RemoteFilesEntityFetcherFactory.getBuilder().setSourceFiles(RAW_SESSION_DATA_FILE).build());
  }

  @Override
  public Class<InputJsonKeys.VendorAPISource.MainTypes> getType() {
    return InputJsonKeys.VendorAPISource.MainTypes.class;
  }

}
