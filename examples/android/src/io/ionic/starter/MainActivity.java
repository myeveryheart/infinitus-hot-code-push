/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package io.ionic.starter;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.nordnetab.chcp.main.HCPHelper;
import com.nordnetab.chcp.main.HCPResult;
import com.nordnetab.chcp.main.model.ChcpError;

import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends Activity implements HCPResult
{
    HCPHelper helper;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);


        helper = HCPHelper.getInstance(this);
        helper.setListener(this);


        try {
            URL url = new URL("");
            helper.setWebUrl(url);
            helper.fetchUpdate();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


    }


    @Override
    public void fetchUpdateResult(boolean needUpdate, ChcpError error) {
        Log.d("rrrrr","ddddd");
    }
}
