/*
 * Copyright (C) 2016 Nishant Srivastava
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.imm.kk.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class RuntimePermissionUtil {

  private RuntimePermissionUtil() {

  }

  public static void onRequestPermissionsResult(int[] grantResults,
      RPResultListener rpResultListener) {
    if (grantResults.length > 0) {
      for (int grantResult : grantResults) {
        if (grantResult == PackageManager.PERMISSION_GRANTED) {
          rpResultListener.onPermissionGranted();
        } else {
          rpResultListener.onPermissionDenied();
        }
      }
    }
  }

  public static void requestPermission(final Activity activity, final String[] permissions,
                                       final int REQUEST_CODE) {
    // No explanation needed, we can request the permission.
    ActivityCompat.requestPermissions(activity, permissions, REQUEST_CODE);
  }

  public static void requestPermission(final Activity activity, final String permission,
                                       final int REQUEST_CODE) {
    // No explanation needed, we can request the permission.
    ActivityCompat.requestPermissions(activity, new String[] { permission }, REQUEST_CODE);
  }

  public static boolean checkPermissonGranted(Context context, String permission) {
    return (ActivityCompat.checkSelfPermission(context, permission)
        == PackageManager.PERMISSION_GRANTED);
  }

  private Context mContext;

  public RuntimePermissionUtil(Context context) {
    mContext = context.getApplicationContext();
  }

  // 判断权限集合
  public boolean lacksPermissions(String... permissions) {
    for (String permission : permissions) {
      if (lacksPermission(permission)) {
        return true;
      }
    }
    return false;
  }

  // 判断是否缺少权限
  private boolean lacksPermission(String permission) {
    return ContextCompat.checkSelfPermission(mContext, permission) ==
            PackageManager.PERMISSION_DENIED;
  }



}
