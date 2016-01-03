/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.starter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.access.KandyLoginResponseListener;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.services.chats.IKandyLocationItem;
import com.genband.kandy.api.services.chats.IKandyTransferProgress;
import com.genband.kandy.api.services.chats.KandyChatMessage;
import com.genband.kandy.api.services.chats.KandyMessageBuilder;
import com.genband.kandy.api.services.chats.KandySMSMessage;
import com.genband.kandy.api.services.common.KandyResponseListener;
import com.genband.kandy.api.services.common.KandyUploadProgressListener;
import com.genband.kandy.api.services.location.KandyCurrentLocationListener;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.parse.ParseAnalytics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

  private GoogleApiClient googleApiClient;

  private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
    private static String TAG="MainActivity";
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;

    TextView mainText;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    SharedPreferences.Editor fd;
    SharedPreferences pref;
    ArrayAdapter<String> adapter;
    ListView listView;
    List<String> arrayList = new ArrayList<String>();

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((StarterApplication)getApplication()).setMainActivity(this);

        ParseAnalytics.trackAppOpenedInBackground(getIntent());

        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        createLocationRequest();
        googleApiClient = new GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build();

        // init Kandy SDK!!
        Kandy.initialize(getApplicationContext(), "DAK35a030f39497484593f5d5372a195704", "DAS4f7751f0b9a04a9383acd3e6954edd5c");

        //mainText = (TextView)findViewById(R.id.maintext);

        listView = (ListView) findViewById(R.id.list);

        pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        //fd= pref.edit();
        Set<String> values = new HashSet<String>();
        values = pref.getStringSet("numbers", values);
        arrayList = new ArrayList<String>(values);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, android.R.id.text1, arrayList);
        listView.setAdapter(adapter);
        listView.setDividerHeight(3);
        listView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int arg2, long arg3) {
                arrayList.remove(arg2);
                adapter.notifyDataSetChanged();
                //TODO: persist
                return false;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart fired ..............");
        if(mGoogleApiClient!=null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop fired ..............");
        if(mGoogleApiClient!=null) {
            mGoogleApiClient.disconnect();
            Log.d(TAG, "isConnected ...............: " + mGoogleApiClient.isConnected());
        }
    }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement

    if (id == R.id.action_settings) {

            selectContact();

        return true;
    }

    return super.onOptionsItemSelected(item);
  }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    private void selectContact() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            Uri uri = data.getData();

            if (uri != null) {
                Cursor c = null;
                try {
                    c = getContentResolver().query(uri, new String[]{
                                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                                    ContactsContract.CommonDataKinds.Phone.TYPE },
                            null, null, null);

                    if (c != null && c.moveToFirst()) {
                        String number = c.getString(0);
                        int type = c.getInt(1);
                        //mainText.setText(number);
                        Log.d(TAG,"add("+number+")");
                        arrayList.add(number);
                        adapter.notifyDataSetChanged();
                        pref =PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                        fd= pref.edit();
                        fd.putStringSet("numbers",new HashSet<String>(arrayList));
                        fd.apply();

                        //TEST ONLY
                        sendMessages();;

                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
//        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started ..............: ");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Firing onLocationChanged..............................................");
        mCurrentLocation = location;
        String lat = "", lng = "";
        if (null != mCurrentLocation) {
            lat = String.valueOf(mCurrentLocation.getLatitude());
            lng = String.valueOf(mCurrentLocation.getLongitude());
//            login("user1@hack2016padls.gmail.com"); // TODO:....
//            sendSMSMessage("URGENT HELP! I'm here: http://maps.google.com/maps?q=" + lat + "," + lng);
        } else {
            Log.d(TAG, "location is null ...............");
        }

    }

    private void login(String username) {
/*
        Kandy.getAccess().registerNotificationListener(new KandyConnectServiceNotificationListener() {

            @Override
            public void onSocketFailedWithError(String error) {
                Log.i(TAG, "onSocketFailedWithError "+error);
            }

            @Override
            public void onSocketDisconnected() {
                Log.i(TAG, "onSocketDisconnected");
            }

            @Override
            public void onSocketConnecting() {
                Log.i(TAG, "onSocketConnecting");
            }

            @Override
            public void onSocketConnected() {
                Log.i(TAG, "onSocketConnected");
            }

            @Override
            public void onConnectionStateChanged(KandyConnectionState state) {
                Log.i(TAG, "KandyConnectionState "+state.name());
            }

            @Override
            public void onInvalidUser(String error) {
                Log.i(TAG, "onInvalidUser "+error);
            }

            @Override
            public void onSessionExpired(String error) {
                Log.i(TAG, "onSessionExpired "+error);
            }

            @Override
            public void onSDKNotSupported(String error) {
                Log.i(TAG, "onSDKNotSupported "+error);
            }
        });
*/
        KandyRecord kandyRecord = null;
        try {
            kandyRecord = new KandyRecord(username);
        } catch (KandyIllegalArgumentException e) {
            //TODO insert your code here
            return;
        }
        String password = "1harumsitvoluptat1";

        Kandy.getAccess().login(kandyRecord, password, new KandyLoginResponseListener() {

            @Override
            public void onRequestFailed(int responseCode, String err) {
                //TODO insert your code here
            }

            @Override
            public void onLoginSucceeded() {
                //TODO insert your code here
            }
        });
    }
/*
    private void sendMessage(String msg, String destination) {
        KandyRecord recipient = null;

        try {
            recipient = new KandyRecord("recipient","domain", KandyRecordType.CONTACT);
        } catch (KandyIllegalArgumentException ex) {
            //TODO insert your code here
        }

        final KandyChatMessage message = new KandyChatMessage(recipient, "message body");
        Kandy.getServices().getChatService().sendChat(message, new KandyResponseListener() {

            @Override
            public void onRequestFailed(int responseCode, String err) {
                //TODO insert your code here
            }

            @Override
            public void onRequestSucceded() {
                //TODO insert your code here
            }
        });
    }

    private void sendGroupMessage(String msg) {
        KandyRecord recipient = null;
        try {
            recipient = new KandyRecord("groupUserName@domain", KandyRecordType.GROUP);
        } catch (KandyIllegalArgumentException ex) {
            //TODO insert your code here
        }

        final KandyChatMessage message = new KandyChatMessage(recipient, "message body");
        Kandy.getServices().getChatService().sendChat(message, new KandyResponseListener() {
            @Override
            public void onRequestFailed(int responseCode, String err) {
                //TODO insert your code here
            }

            @Override
            public void onRequestSucceded() {
                //TODO insert your code here
            }
        });
    }
*/
    boolean smsfailed=true;

    private void sendSMSMessage(final String text) {

        if(arrayList.size()<1) {
            return;
        }

        for (String destination : arrayList) {
            sendSMSMessage(text, destination);
        }


    }

    private void sendSMSMessage(final String text, final String destination) {

        Log.d(TAG,"sending SMS to "+destination);

        KandySMSMessage message = null;
        try {
            message = new KandySMSMessage(destination, "Kandy SMS", text);
        } catch (KandyIllegalArgumentException e) {
            //TODO insert your code here
        }

        // Sending message
        Kandy.getServices().getChatService().sendSMS(message, new KandyResponseListener() {
            @Override
            public void onRequestFailed(int responseCode, String err) {
                Log.d(TAG, "SMS FAILED! " + err);
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(destination, null, text, null, null);
                Log.d(TAG, "SMS sent via SmsManager");
            }

            @Override
            public void onRequestSucceded() {
                Log.d(TAG,"SUCCESS - SMS sent via Kandy!");
            }
        });

    }

    public void sendMessages() {

        login("user1@hack2016padls.gmail.com");

        try {
            sendCurrentLocation();
        } catch(KandyIllegalArgumentException k) {
            Log.e(TAG,k.getLocalizedMessage());
        }

    }


    /**
     * Retrives and send current device location via KandySDk - to SMS list!
     */
    private void sendCurrentLocation() throws KandyIllegalArgumentException {
        Kandy.getServices().getLocationService().getCurrentLocation(new  KandyCurrentLocationListener() {
            @Override
            public void onCurrentLocationReceived(Location location) {

//                sendLocation(location); //TODO: send SMS instead

Log.i(TAG,"URGENT HELP! I'm here: http://maps.google.com/maps?q=" + location.getLatitude() + "," + location.getLongitude());

                sendSMSMessage("URGENT HELP! I'm here: http://maps.google.com/maps?q=" + location.getLatitude() + "," + location.getLongitude());

            }

            @Override
            public void onCurrentLocationFailed(int errorCode, String error) {
                //TODO insert your code here
            }
        });
    }

    /**
    * Send location
    * @param location location to be sent
    */
    private void sendLocation(Location location) {
        IKandyLocationItem kandyLocation = KandyMessageBuilder.createLocation("location", location);
        KandyRecord recipient = null;
        try {
            recipient = new KandyRecord("user@domain.com");
        } catch (KandyIllegalArgumentException e) {
            //TODO insert your code here
        }

        if (recipient == null)
            return;

        final KandyChatMessage message = new KandyChatMessage(recipient, kandyLocation);
        Kandy.getServices().getChatService().sendChat(message, new KandyUploadProgressListener() {
            @Override
            public void onRequestFailed(int responseCode, String err) {
                //TODO insert your code here
            }

            @Override
            public void onProgressUpdate(IKandyTransferProgress progress) {
                //TODO insert your code here
            }

            @Override
            public void onRequestSucceded() {
                //TODO insert your code here
            }
        });
    }

}
