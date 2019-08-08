package com.grisoftware.updatechecker;

import android.app.Activity;
import android.content.ActivityNotFoundException;
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

import androidx.core.text.HtmlCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeSuccessDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.interfaces.Closure;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


/**
 * Created by umut on 8/3/17.
 */

public class GoogleChecker {

    private final String PLAY_STORE_ROOT_WEB = "https://play.google.com/store/apps/details?id=";
    public boolean isThereNewVersion;
    private String marketVersion;
    private String TAG = "UpdateChecker";
    private String appPackageName;
    private Context context;
    private String html = null;

    // TODO: add connection listener for later open connectivity

    public GoogleChecker(final Activity activity, final Boolean haveNoButton) {
        appPackageName = activity.getBaseContext().getPackageName();
        context = activity;
        String url = PLAY_STORE_ROOT_WEB + appPackageName;
        control(context, activity, url, haveNoButton);
    }

    public GoogleChecker(final Activity activity, final Boolean haveNoButton, String lang) {
        appPackageName = activity.getBaseContext().getPackageName();
        context = activity;
        String url = PLAY_STORE_ROOT_WEB + appPackageName + "&hl=" + lang;
        control(context, activity, url, haveNoButton);
    }

    public GoogleChecker(String packageName, final Activity activity, final Boolean haveNoButton) {
        appPackageName = packageName;
        String url = PLAY_STORE_ROOT_WEB + packageName;
        context = activity;
        control(context, activity, url, haveNoButton);
    }

    public GoogleChecker(String packageName, final Activity activity, final Boolean haveNoButton, String lang) {
        appPackageName = packageName;
        String url = PLAY_STORE_ROOT_WEB + packageName + "&hl=" + lang;
        context = activity;
        control(context, activity, url, haveNoButton);
    }


    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = null;
        if (cm != null) {
            netInfo = cm.getActiveNetworkInfo();
        }
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    private void control(final Context context, final Activity activity, final String url, final Boolean noButton) {
        final Boolean[] lastIsBigger = {false};
        if (isOnline()) {
            String version = "";
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Document sayfa = Jsoup.connect(url).get();
                        Element version = sayfa.select("div.JHTxhe.IQ1z0d > div > div:nth-child(4) > span > div > span").first();
                        html = sayfa.select("c-wiz:nth-child(3) > div.W4P4ne > div.PHBdkd > div.DWPxHb").first().html();
                        marketVersion = version.text();
                        Log.e("ve",marketVersion);

                    } catch (IOException e) {
                        e.printStackTrace();

                    }
                    PackageInfo pInfo = null;
                    String version = null;
                    Integer newversion = 1;
                    Integer newMarketVersion = 0;
                    try {
                        pInfo = context.getPackageManager().getPackageInfo(context
                                .getPackageName(), 0);
                        version = String.valueOf(pInfo.versionName);
                        if(version.split(".").length==marketVersion.split(".").length){
                            newversion = Integer.valueOf(version != null ? version.replaceAll("[^\\d]", "") : "0");
                            newMarketVersion = Integer.valueOf(marketVersion.replaceAll("[^\\d]", ""));
                        }else{
                            StringBuilder sameOldVersion = new StringBuilder();
                            String[] mv= marketVersion.split(".");
                            for (String v:mv) {
                                sameOldVersion.append(v+".");
                            }
                            sameOldVersion = sameOldVersion.delete(mv.length*2-1,mv.length*2);
                            lastIsBigger[0] = true;
                            newversion = Integer.valueOf(version != null ? version.replaceAll("[^\\d]", "") : "0");
                            newMarketVersion = Integer.valueOf(sameOldVersion.toString().replaceAll("[^\\d]", ""));

                        }


                    } catch (PackageManager.NameNotFoundException e) {
                        Log.e(TAG, e.getMessage());
                    }


                    if (newversion < newMarketVersion || (lastIsBigger[0] && newversion == newMarketVersion)) {
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
                                        .setMessage(HtmlCompat.fromHtml(html,0))
                                        .setColoredCircle(R.color.dialogSuccessBackgroundColor)
                                        .setDialogIconAndColor(R.drawable.ic_update_black_24dp, R.color.white)
                                        .setCancelable(noButton)
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
                                                } catch (ActivityNotFoundException anfe) {

                                                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
                                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                                                    context.startActivity(i);

                                                }
                                                activity.finish();
                                            }
                                        });

                                if (noButton) {
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
            }).start();
            
            isThereNewVersion = false;
        } else {
            Log.e("UpdateChecker", "No internet connection");
        }

    }

}