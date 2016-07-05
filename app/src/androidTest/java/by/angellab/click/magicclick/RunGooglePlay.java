package by.angellab.click.magicclick;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiAutomatorInstrumentationTestRunner;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Created by yegia on 12.06.2016.
 */
@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
public class RunGooglePlay extends UiAutomatorInstrumentationTestRunner {

    private static final String GOOGLE_PLAY_PACKAGE
            = "com.android.vending";
    private static final int MIDDLE_TIMEOUT = 10000;
    private static final int LONG_TIMEOUT = 15000;
    private static final int TIMEOUT = 2000;
    private static final String STRING_TO_BE_TYPED = "UiAutomator";
    private UiDevice mDevice;
    private String login = "test.hiqo";
    private String pass = "hiqo121212";
    private Context context;
    final CountDownLatch signal = new CountDownLatch(1);

    private boolean canBeEnded = false;

    @Before
    public void startMainActivityFromHomeScreen() {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        context = InstrumentationRegistry.getContext();
        // Start from the home screen
        mDevice.pressHome();
    }

    @Test
    public void test() throws InterruptedException {
        final String launcherPackage = getLauncherPackageName();
        assertThat(launcherPackage, notNullValue());
        mDevice.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), MIDDLE_TIMEOUT);

        //Get Google Creds
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "http://freegift4.me/data.php";

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            login = response.getString("login");
                            pass = response.getString("pass");
                            launchClickers();
                        } catch (JSONException e) {
                            signal.countDown();
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            signal.countDown();
                            e.printStackTrace();
                        } catch (UiObjectNotFoundException e) {
                            signal.countDown();
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        signal.countDown();
                    }
                });
        queue.add(objectRequest);

        signal.await();
    }

    private void launchClickers() throws InterruptedException, UiObjectNotFoundException {
        //Start a Google Play application
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(GOOGLE_PLAY_PACKAGE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);    // Clear out any previous instances
        context.startActivity(intent);

        // Wait for the app to appear
        mDevice.wait(Until.hasObject(By.pkg(GOOGLE_PLAY_PACKAGE).depth(0)), MIDDLE_TIMEOUT);
        Thread.sleep(TIMEOUT);

        UiObject textView = mDevice.findObject(new UiSelector().className(EditText.class).description("Enter your email "));
        textView.click();
        textView.legacySetText(login);
        UiObject nextButton = mDevice.findObject(new UiSelector().description("NEXT"));
        nextButton.click();

        mDevice.wait(Until.findObject(By.desc("Password ")), MIDDLE_TIMEOUT);
        Thread.sleep(TIMEOUT);
        textView = mDevice.findObject(new UiSelector().className(EditText.class).description("Password "));
        textView.click();
        textView.legacySetText(pass);
        nextButton = mDevice.findObject(new UiSelector().description("NEXT"));
        nextButton.click();

        mDevice.wait(Until.findObject(By.desc("ACCEPT")), MIDDLE_TIMEOUT);
        Thread.sleep(TIMEOUT);
        nextButton = mDevice.findObject(new UiSelector().description("ACCEPT"));
        nextButton.click();

        /*mDevice.wait(Until.findObject(By.desc("NEXT")), MIDDLE_TIMEOUT);
        mDevice.waitForIdle(2000);
        UiObject checkBoxAgree = mDevice.findObject(new UiSelector().className(CheckBox.class).resourceId("com.google.android.gms:id/agree_backup"));
        if (checkBoxAgree != null) {
            checkBoxAgree.click();
        }
        nextButton = mDevice.findObject(new UiSelector().description("NEXT"));
        if (nextButton != null) {
            nextButton.click();
        }*/

        Thread.sleep(LONG_TIMEOUT);

        for (int i = 0; i < 20; i++) {

            mDevice.pressHome();

            Intent intent1 = new Intent(Intent.ACTION_VIEW);
            intent1.setData(Uri.parse("http://5.9.73.226/apps/link.php"));
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent1);

//      context.startActivity(intent);
//      mDevice.wait(Until.hasObject(By.pkg(GOOGLE_PLAY_PACKAGE).depth(0)), MIDDLE_TIMEOUT);
            Thread.sleep(LONG_TIMEOUT);
            Thread.sleep(LONG_TIMEOUT);
//        UiObject startedButton = mDevice.findObject(new UiSelector().resourceId("com.android.vending:id/play_onboard_center_button"));
//        if (startedButton.exists()) {
//            startedButton.click();
//        }
//
//        Thread.sleep(MIDDLE_TIMEOUT);
//        UiObject searchImage = mDevice.findObject(new UiSelector().resourceId("com.android.vending:id/search_box_idle_text"));
//        if (searchImage.exists()) {
//            searchImage.click();
//        }
//
//        Thread.sleep(TIMEOUT);
//        UiObject searchText = mDevice.findObject(new UiSelector().resourceId("com.android.vending:id/search_box_text_input"));
//        if (searchText.exists()) {
//            searchText.legacySetText(apk);
//        }
//        mDevice.pressEnter();
//
//        Thread.sleep(TIMEOUT);
//        UiObject titleText = mDevice.findObject(new UiSelector().resourceId("com.android.vending:id/li_title"));
//        if (titleText.exists()) {
//            titleText.click();
//        }

            UiObject buyButton = mDevice.findObject(new UiSelector().resourceId("com.android.vending:id/buy_button"));
            if (buyButton.exists()) {
                buyButton.click();

                Thread.sleep(TIMEOUT);
                UiObject acceptButton = mDevice.findObject(new UiSelector().resourceId("com.android.vending:id/continue_button"));
                if (acceptButton.exists()) {
                    acceptButton.click();

                    Thread.sleep(LONG_TIMEOUT);
                    UiObject openButton = mDevice.findObject(new UiSelector().resourceId("com.android.vending:id/launch_button"));
                    if (openButton.exists()) {
                        openButton.click();
                        Thread.sleep(LONG_TIMEOUT);
                    }
                }
            }
        }
        signal.countDown();
    }

    private String getLauncherPackageName() {
        // Create launcher Intent
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);

        // Use PackageManager to get the launcher package name
        PackageManager pm = InstrumentationRegistry.getContext().getPackageManager();
        ResolveInfo resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo.activityInfo.packageName;
    }
}

