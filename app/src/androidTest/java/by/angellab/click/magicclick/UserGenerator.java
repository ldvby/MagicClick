package by.angellab.click.magicclick;

import android.content.Context;
import android.content.Intent;
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
import android.widget.EditText;

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

import by.angellab.click.magicclick.RunGooglePlay.User;

/**
 * Created by artur.egiazarov on 20.07.2016.
 */
@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
public class UserGenerator extends UiAutomatorInstrumentationTestRunner {

    private static final String SETTINGS_PACKAGE = "com.android.settings";

    private static final int MIDDLE_TIMEOUT = 10000;
    private static final int LONG_TIMEOUT = 15000;
    private static final int TIMEOUT = 2000;

    final CountDownLatch userSignal = new CountDownLatch(1);
    private RequestQueue queue;
    private UiDevice mDevice;
    private Context context;

    @Before
    public void startMainActivityFromHomeScreen() {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        context = InstrumentationRegistry.getContext();
        // Start from the home screen
        mDevice.pressHome();
    }

    @Test
    public void createGoogleAcc() throws InterruptedException, UiObjectNotFoundException {
        queue = Volley.newRequestQueue(context);
        User user = generateNewUser();

        createUser(user);
    }

    private void createUser(User user) throws UiObjectNotFoundException {
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(SETTINGS_PACKAGE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);    // Clear out any previous instances
        context.startActivity(intent);

        mDevice.wait(Until.hasObject(By.pkg(SETTINGS_PACKAGE).depth(0)), MIDDLE_TIMEOUT);
        UiScrollable settingsScroll = new UiScrollable(new UiSelector().scrollable(true));

        settingsScroll.scrollTextIntoView("Accounts");
        UiObject accItem = mDevice.findObject(new UiSelector().text("Accounts"));
        accItem.clickAndWaitForNewWindow();
        mDevice.findObject(new UiSelector().text("Add account")).clickAndWaitForNewWindow();
        mDevice.findObject(new UiSelector().text("Google")).clickAndWaitForNewWindow();

        mDevice.wait(Until.findObject(By.desc("Or create a new account")), MIDDLE_TIMEOUT);
        mDevice.findObject(new UiSelector().description("Or create a new account")).click();
        mDevice.wait(Until.findObject(By.desc("First name")), MIDDLE_TIMEOUT);

        UiObject firstName = mDevice.findObject(new UiSelector().resourceId("firstName"));
        firstName.click();
        firstName.legacySetText(user.getFirstName());

        UiObject lastName = mDevice.findObject(new UiSelector().resourceId("lastName"));
        lastName.click();
        lastName.legacySetText(user.getLastName());

        UiObject nextButton = mDevice.findObject(new UiSelector().description("NEXT"));
        nextButton.click();


    }

    private User generateNewUser() throws InterruptedException {
        final User user = new User();

        StringRequest stringRequest = new StringRequest("http://api.randomuser.me/",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonResult = null;
                        try {
                            jsonResult = new JSONObject(response);
                            JSONObject jsonUser = jsonResult.getJSONArray("results").getJSONObject(0);
                            user.setFirstName(jsonUser.getJSONObject("name").getString("first"));
                            user.setLastName(jsonUser.getJSONObject("name").getString("last"));
                            user.setLogin(jsonUser.getJSONObject("login").getString("username"));
                            user.setPassword(jsonUser.getJSONObject("login").getString("salt"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        userSignal.countDown();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        userSignal.countDown();
                    }
                });
        queue.add(stringRequest);
        userSignal.await();
        return user;
    }

}
