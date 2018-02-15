package com.grisoftware.updatechecker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeSuccessDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.interfaces.Closure;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * Created by umut on 8/3/17.
 */

public class GoogleChecker {

    private final String PLAY_STORE_ROOT_WEB = "https://play.google.com/store/apps/details?id=";
    private final String PLAY_STORE_HTML_TAGS_TO_GET_VERSION = "itemprop=\"softwareVersion\"> ";
    private final String PLAY_STORE_HTML_TAGS_TO_GET_VERSION_END = "</div>";
    private final String PLAY_STORE_HTML_TAGS_TO_GET_WHATS_NEW = "<div class=\"details-section whatsnew\">";
    private final String PLAY_STORE_HTML_TAGS_TO_GET_WHATS_NEW_END = "<div class=\"show-more-end\"";
    private final String PLAY_STORE_PACKAGE_NOT_PUBLISHED_IDENTIFIER = "We're sorry, the requested URL was not found on this server.";

    public boolean isThereNewVersion;
    private String marketVersion;
    private String TAG = "UpdateChecker";
    private String appPackageName;
    private Context context;
    private Spanned html = null;

    // TODO: add connection listener for later open connectivity

    public GoogleChecker(final Activity activity, final Boolean haveNoButton) {
        RequestQueue queue = Volley.newRequestQueue(activity.getBaseContext());
        appPackageName = activity.getBaseContext().getPackageName();
        context = activity;
        String url = PLAY_STORE_ROOT_WEB + appPackageName;
        control(context, activity, queue, url, haveNoButton);
    }

    public GoogleChecker(final Activity activity, final Boolean haveNoButton, String lang) {
        RequestQueue queue = Volley.newRequestQueue(activity.getBaseContext());
        appPackageName = activity.getBaseContext().getPackageName();
        context = activity;
        String url = PLAY_STORE_ROOT_WEB + appPackageName + "&hl=" + lang;
        control(context, activity, queue, url, haveNoButton);
    }

    public GoogleChecker(String packageName, final Activity activity, final Boolean haveNoButton) {
        RequestQueue queue = Volley.newRequestQueue(activity.getApplicationContext());
        appPackageName = packageName;
        String url = PLAY_STORE_ROOT_WEB + packageName;
        context = activity;
        control(context, activity, queue, url, haveNoButton);
    }

    public GoogleChecker(String packageName, final Activity activity, final Boolean haveNoButton, String lang) {
        RequestQueue queue = Volley.newRequestQueue(activity.getApplicationContext());
        appPackageName = packageName;
        String url = PLAY_STORE_ROOT_WEB + packageName + "&hl=" + lang;
        context = activity;
        control(context, activity, queue, url, haveNoButton);
    }


    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    private void control(final Context context, final Activity activity, RequestQueue queue, String url, final Boolean haveno) {

        if (isOnline()) {
            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            String output = "";
                            final InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(response.getBytes()));
                            final BufferedReader in = new BufferedReader(reader);
                            String read;
                            try {
                                while ((read = in.readLine()) != null) {
                                    output += read;
                                }
                                reader.close();

                                in.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            String string_html;
                            try {
                                string_html = output.substring(output.indexOf(PLAY_STORE_HTML_TAGS_TO_GET_WHATS_NEW), output.lastIndexOf(PLAY_STORE_HTML_TAGS_TO_GET_WHATS_NEW_END));
                                if (Build.VERSION.SDK_INT >= 24) {
                                    html = Html.fromHtml(string_html.replace("h1", "h4"), 1); // for 24 api and more
                                } else {
                                    html = Html.fromHtml(string_html.replace("h1", "h4")); // or for older api
                                }

                            } catch (Exception e) {
                                if (Build.VERSION.SDK_INT >= 24) {
                                    html = Html.fromHtml(context.getString(R.string.update_available_text), 1); // for 24 api and more
                                } else {
                                    html = Html.fromHtml(context.getString(R.string.update_available_text)); // or for older api
                                }
                            }

                            try {
                                int beginIndex = output.lastIndexOf(PLAY_STORE_HTML_TAGS_TO_GET_VERSION);
                                string_html = output.substring(beginIndex, output.indexOf(PLAY_STORE_HTML_TAGS_TO_GET_VERSION_END, beginIndex));
                                marketVersion = string_html;
                            } catch (Exception e) {
                                marketVersion = "0";
                            }


                            PackageInfo pInfo = null;
                            String version = null;
                            Integer newversion = 1;
                            Integer newMarketVersion = 0;
                            try {
                                pInfo = context.getPackageManager().getPackageInfo(context
                                        .getPackageName(), 0);
                                version = String.valueOf(pInfo.versionName);
                                newversion = Integer.valueOf(version != null ? version.replaceAll("[^\\d]", "") : "0");
                                newMarketVersion = Integer.valueOf(marketVersion.replaceAll("[^\\d]", ""));

                            } catch (PackageManager.NameNotFoundException e) {
                                Log.e(TAG, e.getMessage());
                            }


                            if (newversion < newMarketVersion) {
                                isThereNewVersion = true;
                            }
                            //Log.e("versiyonpopup", isThereNewVersion + ":" + newversion + ":" + newMarketVersion);
                            Spanned title = null;
                            String translateTitle = context.getString(R.string.update_available_title);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                title = Html.fromHtml("<u>" + translateTitle.toUpperCase() + "</u>", 1);
                            }
                            final Spanned finalTitle = title;
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (isThereNewVersion) {
                                        AwesomeSuccessDialog successDialog = new AwesomeSuccessDialog(context)
                                                .setTitle(finalTitle)
                                                .setMessage(html)
                                                .setColoredCircle(R.color.dialogSuccessBackgroundColor)
                                                .setDialogIconAndColor(R.drawable.ic_update_black_24dp, R.color.white)
                                                .setCancelable(haveno)
                                                .setPositiveButtonText(context.getString(R.string.update))
                                                .setPositiveButtonbackgroundColor(R.color.dialogSuccessBackgroundColor)
                                                .setPositiveButtonTextColor(R.color.white)
                                                .setPositiveButtonClick(new Closure() {
                                                    @Override
                                                    public void exec() {
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

                                        if (haveno) {
                                            successDialog.setNegativeButtonText(context.getString(R.string.cancel))
                                                    .setNegativeButtonbackgroundColor(R.color.dialogSuccessBackgroundColor)
                                                    .setNegativeButtonTextColor(R.color.white)
                                                    .setNegativeButtonClick(new Closure() {
                                                        @Override
                                                        public void exec() {
                                                            //click
                                                        }
                                                    });
                                        }


                                        successDialog.show();

                                    }
                                }
                            });
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error.networkResponse.statusCode == 404) {
                        Log.e(TAG, PLAY_STORE_PACKAGE_NOT_PUBLISHED_IDENTIFIER);
                    } else if (error.getMessage() != null)
                        Log.e(TAG, error.getMessage());
                    else
                        Log.e(TAG, "error.getMessage() is Empty");

                    //activity.finish();
                }
            });
            // Add the request to the RequestQueue.
            queue.add(stringRequest);
            isThereNewVersion = false;
            //queue.start();
        } else {
            Log.e("UpdateChecker", "No internet connection");
        }

    }

}