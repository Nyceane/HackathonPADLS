/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.starter;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseInstallation;
import com.parse.ParseUser;


public class StarterApplication extends Application {

  private MainActivity mainActivity;

  @Override
  public void onCreate() {
    super.onCreate();

    // Enable Local Datastore.
    Parse.enableLocalDatastore(this);

    // Add your initialization code here
    Parse.initialize(this, "COLFRcv3xi8naPtnDUHK4QwXpY8E49t9OuBYzKmN", "NJsKhQZ9pSJXPQ1EnMdzptxWxvLyzAp4AihbEdVp");
    ParseInstallation.getCurrentInstallation().saveInBackground();

    ParseUser.enableAutomaticUser();
    ParseACL defaultACL = new ParseACL();
    // Optionally enable public read access.
    // defaultACL.setPublicReadAccess(true);
    ParseACL.setDefaultACL(defaultACL, true);

  }

  public void setMainActivity(MainActivity activity) {
      mainActivity = activity;
  }
    public MainActivity getMainActivity() {
        return mainActivity;
    }

}
