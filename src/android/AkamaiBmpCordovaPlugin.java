package com.akamai.botman;

import android.app.Activity;
import android.app.Application;

import com.akamai.botman.CYFMonitor;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
/**
 * Class of Akamai BMP SDK plugin for Cordova
 *
 * 1. Initialize BMP SDK in plugin initialization
 * 2. Expose BMP SDK methods (setLogLevel and getSensorData) to Cordova via js wrapper
 */
public class AkamaiBmpCordovaPlugin extends CordovaPlugin {

    private static final String TAG = "AkamaiBmpCordovaPlugin";

    // APIs to be exposed from Akamai BMP SDK
    private final static String SET_LOG_LEVEL = "setLogLevel";
    private final static String GET_SENSOR_DATA = "getSensorData";
    private final static String INITIALIZE = "initialize";
    private final static String COLLECTDATA = "collectTestData";
    private final static String CONFIGURE_CHALLENGE_ACTION = "configureChallengeAction";
    private final static String SHOW_CHALLENGE_ACTION = "showChallengeAction";

    // Table for transfering data index from Integer to String
    private final static String[] DATAINDEX = {
        "touch_totalCount",
        "touch_totalMoveCount",
        "moveLimit",
        "touch_totalUpDownCount",
        "upDownLimit",
        "motion_totalCount",
        "motion_count",
        "motionLimit",
        "ori_totalCount",
        "ori_count",
        "oriLimit",
        "text_totalCount",
        "currentKeyCount",
        "keyLimit",
        "pow_status"
    };


    @Override
    public void initialize(final CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, JSONArray argcoms, final CallbackContext callbackContext)
            throws JSONException {
        // hookup to exposed API
        if (SET_LOG_LEVEL.equals(action)) {
            // get log level from js caller
            int logLevel = argcoms.getInt(0);
            // set log level by calling BMP SDK API
            CYFMonitor.setLogLevel(logLevel);
            return true;
        } else if (GET_SENSOR_DATA.equals(action)) {
            // get sensor data from UI thread to avoid possible delay caused by Cordova
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    // get sensor data by calling BMP SDK API
                    String sd = CYFMonitor.getSensorData();
                    // send sensor data to js caller
                    callbackContext.success(sd);
                }
            });
            return true;
        } else if (INITIALIZE.equals(action)) {

            String baseUrl = argcoms.getString(0);

            // initialize Akamai BMP SDK in UI thread
            this.cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // get activiry and application object
                    Activity activity = cordova.getActivity();
                    Application app = activity.getApplication();
                    // we need to start collecting sensor data expelicitly
                    // as the activity is already resumed when the plugin is initialized
                    com.cyberfend.cyfsecurity.CYFMonitor.startCollectingSensorData(activity);

                    // initialize Akamai BMP SDK
                    if(baseUrl.equals("null")){
                        CYFMonitor.initialize(app);
                    }
                    else {
                        CYFMonitor.initialize(app, baseUrl);
                    }
                    com.cyberfend.cyfsecurity.CYFMonitor.setActivityVisible(true);
                }
            });
            return true;
        } else if (COLLECTDATA.equals(action)) {
            this.cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // get sensor data by calling BMP SDK API
                    HashMap<Integer, String> td = CYFMonitor.collectTestData();
                    if (td != null) {
                        JSONObject testData = new JSONObject();

                        // TODO: Put data into JSONObject
                        for (int i = 0; i < td.size(); i++) {
                            try {
                                testData.put(DATAINDEX[i], td.get(i));
                            } catch (JSONException e) {
                                Log.e(TAG, "JSON Exception");
                                e.printStackTrace();
                            }
                        }
                        callbackContext.success(testData);
                    } else {
                        callbackContext.error("Failed to get test data");
                    }
                }
            });
            return true;
        } else if (CONFIGURE_CHALLENGE_ACTION.equals(action)) {
            String baseUrl = argcoms.getString(0);
            Activity activity = this.cordova.getActivity();
            Application app = activity.getApplication();

            CYFMonitor.configureChallengeAction(app, baseUrl);
        
            return true;
        } else if (SHOW_CHALLENGE_ACTION.equals(action)) {
            CYFMonitor.ChallengeActionCallback challengeActionCallback = new CYFMonitor.ChallengeActionCallback() {
                @Override
                public void onChallengeActionCancel() {
                    JSONObject response = prepareResponseObject(0, null);
                    callbackContext.success(response);
                }
                @Override
                public void onChallengeActionFailure(String failMessage) {
                    JSONObject response = prepareResponseObject(-1, failMessage);
                    callbackContext.success(response);
                }
                @Override
                public void onChallengeActionSuccess() {
                    JSONObject response = prepareResponseObject(1, null);
                    callbackContext.success(response);
                }
            };

            Activity activity = this.cordova.getActivity();

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject params = argcoms.getJSONObject(0);

                        String context = params.get("context").toString();
                        String title = params.get("title").toString();
                        String message = params.get("message").toString();
                        String cancelButtonTitle = params.get("cancelButtonTitle").toString();
                        if (context != null && context.trim().length() != 0) {
                            CYFMonitor.showChallengeAction(activity, context, title, message, cancelButtonTitle, challengeActionCallback);
                            com.cyberfend.cyfsecurity.CYFMonitor.setActivityVisible(true);
                        } else {
                            callbackContext.error("Context is missing");
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON Exception");
                        e.printStackTrace();
                    }
                }
            });

            return true;
        }

        return false;
    }

    JSONObject prepareResponseObject(int status, String message) {
        JSONObject response = new JSONObject();
        try {
            response.put("status", status);
            if (message != null) {
                response.put("message", message);
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON Exception");
            e.printStackTrace();
        }

        return response;
    }
}
