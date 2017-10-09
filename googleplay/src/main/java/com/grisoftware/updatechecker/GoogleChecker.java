package com.grisoftware.updatechecker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


/**
 * Created by umut on 8/3/17.
 */

public class GoogleChecker {
    private static final String PLAY_STORE_ROOT_WEB = "https://play.google.com/store/apps/details?id=";
    private static final String PLAY_STORE_HTML_TAGS_TO_GET_RIGHT_POSITION = "itemprop=\"softwareVersion\"> ";
    private static final String PLAY_STORE_HTML_TAGS_TO_REMOVE_USELESS_CONTENT = "  </div> </div>";
    private static final String PLAY_STORE_PACKAGE_NOT_PUBLISHED_IDENTIFIER = "We're sorry, the requested URL was not found on this server.";
    private static final int VERSION_DOWNLOADABLE_FOUND = 0;
    private static final int MULTIPLE_APKS_PUBLISHED = 1;
    private static final int NETWORK_ERROR = 2;
    private static final int PACKAGE_NOT_PUBLISHED = 3;
    private static final int STORE_ERROR = 4;
    private static GoogleChecker mInstance;
    private static Context mCtx;
    public boolean isThereNewVersion;
    String mVersionDownloadable;
    String marketVersion;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private String TAG = "GoogleChecker.java";
    private String appPackageName;
    private Context context;

    public GoogleChecker(final Activity activity,final Boolean haveNoButton) {
        RequestQueue queue = Volley.newRequestQueue(activity.getBaseContext());
        appPackageName = activity.getBaseContext().getPackageName();
        context = activity;
        String url = PLAY_STORE_ROOT_WEB + appPackageName;
        control(context, activity, queue, url,haveNoButton);
    }

    public GoogleChecker(String packageName, final Activity activity, final Boolean haveNoButton) {
        RequestQueue queue = Volley.newRequestQueue(activity.getApplicationContext());
        appPackageName = packageName;
        String url = PLAY_STORE_ROOT_WEB + packageName;
        context = activity;
        control(context, activity, queue, url, haveNoButton);
    }

    public static void AlertDialog(int message, Context c, boolean haveno, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(c);
        alertDialog.setCancelable(false);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(R.string.ok, listener);
        if (haveno) {
            alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        }

        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    private void control(final Context context, final Activity activity, RequestQueue queue, String url, final Boolean haveno) {
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            if (response.contains(PLAY_STORE_HTML_TAGS_TO_GET_RIGHT_POSITION)) { // Obtain HTML line contaning version available in Play Store
                                String containingVersion = response.substring(response.lastIndexOf(PLAY_STORE_HTML_TAGS_TO_GET_RIGHT_POSITION) + 28);  // Get the String starting with version available + Other HTML tags
                                String[] removingUnusefulTags = containingVersion.split(PLAY_STORE_HTML_TAGS_TO_REMOVE_USELESS_CONTENT); // Remove useless HTML tags
                                marketVersion = removingUnusefulTags[0]; // Obtain version available
                            } else if (response.contains(PLAY_STORE_PACKAGE_NOT_PUBLISHED_IDENTIFIER)) { // This packages has not been found in Play Store
                                marketVersion = String.valueOf(PACKAGE_NOT_PUBLISHED);
                            }

                            //Log.e("ilkMarketVersion:", marketVersion);


                            PackageInfo pInfo = null;
                            String version = null;
                            try {
                                pInfo = context.getPackageManager().getPackageInfo(context
                                        .getPackageName(), 0);
                                version = String.valueOf(pInfo.versionName);
                            } catch (PackageManager.NameNotFoundException e) {
                                Log.e("Error", e.getMessage());
                            }


                            Integer newversion = Integer.valueOf(version != null ? version.replaceAll("[^\\d-]", "") : "0");
                            Integer newMarketVersion = Integer.valueOf(marketVersion.replaceAll("[^\\d-]", ""));

                            if (newversion < newMarketVersion) {
                                isThereNewVersion = true;
                            }
                            //Log.e("versiyonpopup", isThereNewVersion + ":" + newversion + ":" + newMarketVersion);
                            if (isThereNewVersion) {

                                AlertDialog(R.string.update_available, context, haveno, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {


                                        //onEvent();
//                                        new Listener() {
//                                            @Override
//                                            public void onEvent() {
//                                                Log.e("cins","cins");
//                                            }
//                                        }.onEvent();
                                        try {
                                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
                                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                                            context.startActivity(i);
                                        } catch (android.content.ActivityNotFoundException anfe) {

                                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
                                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                                            context.startActivity(i);

                                        }
                                        activity.finish();
                                    }
                                });
                            }

                        } catch (Exception e) {
                            Log.e("Error", e.getMessage());
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error != null) {
                    if (error.getMessage() != null)
                        Log.e(TAG, error.getMessage());
                    else
                        Log.e(TAG, "error.getMessage() is Empty");
                } else {
                    Log.e(TAG, "Catch's error is empty");
                }

                activity.finish();
            }
        });
// Add the request to the RequestQueue.
        queue.add(stringRequest);
        isThereNewVersion = false;
        //queue.start();
    }
}