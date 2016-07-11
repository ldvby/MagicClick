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
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
public class RunGooglePlay extends UiAutomatorInstrumentationTestRunner {

    private static final String GOOGLE_PLAY_PACKAGE
            = "com.android.vending";
    private static final String IP = "185.80.233.58";
    private static final String APPS_LINK = "http://5.9.73.226/apps/n584757.php";

    private static final String EVENT_START = "start";
    private static final String EVENT_BAD_CREDS = "bad_creds";
    private static final String EVENT_INSTALLED_OR_NOT_COMPATIBLE = "installed_or_not_compatible";
    private static final String EVENT_SUCCESS = "success";
    private static final String EVENT_DONE = "done";
    private static final String EVENT_BAD_CREDS_LINK = "bad_creds_link";

    private static final int MIDDLE_TIMEOUT = 10000;
    private static final int LONG_TIMEOUT = 15000;
    private static final int TIMEOUT = 2000;
    private static final String STRING_TO_BE_TYPED = "UiAutomator";
    final CountDownLatch signal = new CountDownLatch(1);
    private UiDevice mDevice;
    private String login = "test.hiqo";
    private String pass = "hiqo121212";
    private Context context;
    private RequestQueue queue;


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
        queue = Volley.newRequestQueue(context);
        /*String url = "http://" + IP + "/cred.txt";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        int aIndex = response.indexOf("@");
                        login = response.substring(0, aIndex);
                        pass = response.substring(aIndex+1);
                        try {
                            launchClickers();
                        } catch (InterruptedException e) {
                            signal.countDown();
                            e.printStackTrace();
                        } catch (UiObjectNotFoundException e) {
                            sendEvent(EVENT_BAD_CREDS);
                            signal.countDown();
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        sendEventWithSleep(EVENT_BAD_CREDS_LINK);
                        signal.countDown();
                    }
                });

        queue.add(stringRequest);*/

        try {
            launchClickers();
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }

        signal.await(10, TimeUnit.MINUTES);
    }

    private void sendEvent(String event) {
        String url = "http://" + IP + "/event/" + event;

        StringRequest objectRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
        queue.add(objectRequest);
    }

    private void sendEventWithSleep(String event) {
        sendEvent(event);
        try {
            Thread.sleep(TIMEOUT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void launchClickers() throws InterruptedException, UiObjectNotFoundException {
        //Start a Google Play application
        sendEvent(EVENT_START);
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(GOOGLE_PLAY_PACKAGE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);    // Clear out any previous instances
        context.startActivity(intent);

        // Wait for the app to appear
        mDevice.wait(Until.hasObject(By.pkg(GOOGLE_PLAY_PACKAGE).depth(0)), MIDDLE_TIMEOUT);
        Thread.sleep(LONG_TIMEOUT);

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

        // TODO: 08.07.2016
        for (int i = 0; i < 3; i++) {

            mDevice.pressHome();

            Intent intent1 = new Intent(Intent.ACTION_VIEW);
            // TODO: 08.07.2016
//            intent1.setData(Uri.parse(APPS_LINK));
            intent1.setData(Uri.parse("market://details?id=by.angellab.musiclunch"));
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent1);

            // TODO: 08.07.2016
//            Thread.sleep(LONG_TIMEOUT);
            Thread.sleep(LONG_TIMEOUT);

            UiObject buyButton = mDevice.findObject(new UiSelector().resourceId("com.android.vending:id/buy_button"));
            if (buyButton.exists()) {
                buyButton.click();

                Thread.sleep(TIMEOUT);
                UiObject acceptButton = mDevice.findObject(new UiSelector().resourceId("com.android.vending:id/continue_button"));
                if (acceptButton.exists()) {
                    acceptButton.click();

                    Thread.sleep(LONG_TIMEOUT);
                    mDevice.wait(Until.findObject(By.clazz(Button.class).text("OPEN")), TIMEOUT);
                    UiObject openButton = mDevice.findObject(new UiSelector().resourceId("com.android.vending:id/launch_button"));
                    if (openButton.exists()) {
                        openButton.click();
                        Thread.sleep(LONG_TIMEOUT);
                        sendEvent(EVENT_SUCCESS);
                    }
                }
            } else {
                sendEvent(EVENT_INSTALLED_OR_NOT_COMPATIBLE);
            }
        }
        sendEvent(EVENT_DONE);
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

