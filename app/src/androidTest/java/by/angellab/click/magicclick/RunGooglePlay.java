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
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
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
    private static final String SETTINGS_PACKAGE = "com.android.settings";
    private static final String IP = "185.80.233.58";
    private static final String APPS_LINK = "http://5.9.73.226/apps/n584757.php";

    private static final String EVENT_START = "start";
    private static final String EVENT_BAD_CREDS = "bad_creds";
    private static final String EVENT_INSTALLED_OR_NOT_COMPATIBLE = "installed_or_not_compatible";
    private static final String EVENT_SUCCESS = "success";
    private static final String EVENT_DONE = "done";
    private static final String EVENT_BAD_CREDS_LINK = "bad_creds_link";

    private static final int TIMEOUT = 2000;
    private static final int MIDDLE_TIMEOUT = 10000;
    private static final int LONG_TIMEOUT = 15000;
    private static final int VERY_LONG_TIMEOUT = 60000;
    private static final String STRING_TO_BE_TYPED = "UiAutomator";
    final CountDownLatch endSignal = new CountDownLatch(1);
    private UiDevice mDevice;
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

        final User user = getUserFromServer();

        if (user != null && !TextUtils.isEmpty(user.getLogin())) {
            try {
                launchClickers(user);
            } catch (InterruptedException e) {
                e.printStackTrace();
                sendEvent(EVENT_DONE);
            } catch (UiObjectNotFoundException e) {
                e.printStackTrace();
                sendEvent(EVENT_DONE);
            }
        } else {
            sendEvent(EVENT_DONE);
        }

        //User user = generateNewUser();

//        user.setLogin("alexsanderson410");
//        user.setPassword("qwerty5656");

//        if (user!= null && !TextUtils.isEmpty(user.getLogin())) {
//            try {
//                launchClickers(user);
//            } catch (UiObjectNotFoundException e) {
//                e.printStackTrace();
//            }
//        } else {
//            sendEvent(EVENT_DONE);
//        }
        endSignal.await(5, TimeUnit.MINUTES);
    }

    private User getUserFromServer() throws InterruptedException {
        final User user = new User();
        String url = "http://" + IP + "/api/getCreds";
        final CountDownLatch userSignal = new CountDownLatch(1);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonResult = null;
                        try {
                            jsonResult = new JSONObject(response);
                            String login = jsonResult.getString("login");
                            user.setLogin(login.substring(0, login.indexOf("@")));
                            user.setPassword(jsonResult.getString("pass"));
                            user.setPhone(jsonResult.getString("phone"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

//                        int aIndex = response.indexOf("@");
//                        login = response.substring(0, aIndex);
//                        pass = response.substring(aIndex+1);
                        userSignal.countDown();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        sendEvent(EVENT_BAD_CREDS_LINK);
                        userSignal.countDown();
                    }
                });
        queue.add(stringRequest);

        userSignal.await();
        return user;
    }

    private void sendEvent(final String event) {
        String url = "http://" + IP + "/event/" + event;

        StringRequest objectRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (event == EVENT_DONE ||
                                event == EVENT_BAD_CREDS_LINK) {
                            endSignal.countDown();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (event == EVENT_DONE ||
                                event == EVENT_BAD_CREDS_LINK) {
                            endSignal.countDown();
                        }
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

    private void launchClickers(User user) throws InterruptedException, UiObjectNotFoundException {
        sendEvent(EVENT_START);

        //Clear settings
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(SETTINGS_PACKAGE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);    // Clear out any previous instances
        context.startActivity(intent);

        mDevice.wait(Until.hasObject(By.pkg(SETTINGS_PACKAGE).depth(0)), MIDDLE_TIMEOUT);
        UiScrollable settingsScroll = new UiScrollable(new UiSelector().scrollable(true));

        settingsScroll.scrollTextIntoView("Apps");
        UiObject appsItem = mDevice.findObject(new UiSelector().text("Apps"));
        appsItem.clickAndWaitForNewWindow();
        mDevice.swipe(mDevice.getDisplayWidth() - 10, mDevice.getDisplayHeight() / 2,
                10, mDevice.getDisplayHeight() / 2, 10);
        mDevice.swipe(mDevice.getDisplayWidth() - 10, mDevice.getDisplayHeight() / 2,
                10, mDevice.getDisplayHeight() / 2, 10);
        mDevice.findObject(new UiSelector().description("More options").className(ImageButton.class)).click();
        Thread.sleep(TIMEOUT);
        mDevice.findObject(new UiSelector().text("Sort by size")).click();
        Thread.sleep(TIMEOUT);
        mDevice.findObject(new UiSelector().text("Google Play services")).click();
        Thread.sleep(TIMEOUT);
        mDevice.findObject(new UiSelector().text("Force stop")).click();
        Thread.sleep(TIMEOUT);
        mDevice.findObject(new UiSelector().text("OK").className(Button.class)).click();
        Thread.sleep(TIMEOUT);

        //Start a Google Play application
        intent = context.getPackageManager().getLaunchIntentForPackage(GOOGLE_PLAY_PACKAGE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);    // Clear out any previous instances
        context.startActivity(intent);

        // Wait for the app to appear
        Thread.sleep(MIDDLE_TIMEOUT);
        Thread.sleep(MIDDLE_TIMEOUT);
        //mDevice.wait(Until.hasObject(By.pkg(GOOGLE_PLAY_PACKAGE).depth(0)), MIDDLE_TIMEOUT);

        mDevice.wait(Until.findObject(By.clazz(EditText.class).descContains("Enter your email")), VERY_LONG_TIMEOUT);
        UiObject textView = mDevice.findObject(new UiSelector().className(EditText.class).description("Enter your email "));
        textView.click();
        textView.legacySetText(user.getLogin());
        UiObject nextButton = mDevice.findObject(new UiSelector().description("NEXT"));
        nextButton.click();

        mDevice.wait(Until.findObject(By.desc("Password ")), MIDDLE_TIMEOUT);
        Thread.sleep(TIMEOUT);
        textView = mDevice.findObject(new UiSelector().className(EditText.class).description("Password "));
        textView.click();
        textView.legacySetText(user.getPassword());
        nextButton = mDevice.findObject(new UiSelector().description("NEXT"));
        nextButton.click();

        nextButton.waitUntilGone(VERY_LONG_TIMEOUT);
        Thread.sleep(TIMEOUT);
        //Phone confirmation
        if (mDevice.hasObject(By.desc("Confirm your recovery phone number"))) {
            mDevice.findObject(new UiSelector().description("Confirm your recovery phone number")).click();
            mDevice.wait(Until.findObject(By.descContains("Recovery phone").clazz(EditText.class)), MIDDLE_TIMEOUT);
            UiObject recPhone = mDevice.findObject(new UiSelector().descriptionContains("Recovery phone").className(EditText.class));
            recPhone.click();
            recPhone.legacySetText(user.getPhone());
            nextButton = mDevice.findObject(new UiSelector().description("NEXT"));
            nextButton.click();
            Thread.sleep(MIDDLE_TIMEOUT);
        }

        mDevice.wait(Until.findObject(By.desc("ACCEPT")), MIDDLE_TIMEOUT);
        nextButton = mDevice.findObject(new UiSelector().description("ACCEPT"));
        nextButton.click();
        nextButton.waitUntilGone(VERY_LONG_TIMEOUT);
        Thread.sleep(TIMEOUT);

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

        // TODO: 08.07.2016
        for (int i = 0; i < 1; i++) {

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

    public static class User {

        private String firstName;
        private String lastName;
        private String login;
        private String password;
        private String phone;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }
}

