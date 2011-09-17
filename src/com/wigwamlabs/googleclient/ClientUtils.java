/*
 * Copyright 2011 Jonas Bengtsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wigwamlabs.googleclient;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

public class ClientUtils {
	public static String getApplicationName(Context context) {
		String appName = "Unnamed";
		String versionName = "1.0";
		try {
			final PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			appName = context.getString(packageInfo.applicationInfo.labelRes);
			versionName = packageInfo.versionName;
		} catch (final NameNotFoundException e) {
			e.printStackTrace();
		}

		return "WigwamLabs-" + appName.replaceAll("\\s", "") + "/" + versionName;
	}
}
