package by.angellab.click.magicclick;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.UiAutomation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.UiAutomatorInstrumentationTestRunner;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

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
    private static final int LAUNCH_TIMEOUT = 10000;
    private static final int TIMEOUT = 2000;
    private static final String STRING_TO_BE_TYPED = "UiAutomator";
    private UiDevice mDevice;

    @Before
    public void startMainActivityFromHomeScreen() {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // Start from the home screen
        mDevice.pressHome();
    }

    @Test
    public void test() throws UiObjectNotFoundException, InterruptedException {
        // Wait for launcher

        String login = "test.hiqo";
        String pass = "hiqo121212";
        String apk = "by.angellab.musiclunch";

        Bundle args = InstrumentationRegistry.getArguments();

        String lParam = args.getString("login");
        String pParam = args.getString("pass");
        String aParam = args.getString("apk");
        if (lParam != null) {
            login = lParam.trim();
        }
        if (pParam != null) {
            pParam = pParam.replace("0space0", " ");
            pParam = pParam.replace("0amper0", "&");
            pParam = pParam.replace("0less0", "<");
            pParam = pParam.replace("0more0", ">");
            pParam = pParam.replace("0openbkt0", "(");
            pParam = pParam.replace("0closebkt0", ")");
            pParam = pParam.replace("0onequote0", "'");
            pParam = pParam.replace("0twicequote0", "\"");
            pass = pParam.trim();
        }
        if (aParam != null){
            apk = aParam.trim();
        }

        final String launcherPackage = getLauncherPackageName();
        assertThat(launcherPackage, notNullValue());
        mDevice.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), LAUNCH_TIMEOUT);

        Context context = InstrumentationRegistry.getContext();
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(GOOGLE_PLAY_PACKAGE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);    // Clear out any previous instances
        context.startActivity(intent);

        // Wait for the app to appear
        mDevice.wait(Until.hasObject(By.pkg(GOOGLE_PLAY_PACKAGE).depth(0)), LAUNCH_TIMEOUT);
        Thread.sleep(TIMEOUT);

        UiObject textView = mDevice.findObject(new UiSelector().className(EditText.class).description("Enter your email "));
        textView.click();
        textView.legacySetText(login);
        UiObject nextButton = mDevice.findObject(new UiSelector().description("NEXT"));
        nextButton.click();

        mDevice.wait(Until.findObject(By.desc("Password ")), LAUNCH_TIMEOUT);
        Thread.sleep(TIMEOUT);
        textView = mDevice.findObject(new UiSelector().className(EditText.class).description("Password "));
        textView.click();
        textView.legacySetText(pass);
        nextButton = mDevice.findObject(new UiSelector().description("NEXT"));
        nextButton.click();

        mDevice.wait(Until.findObject(By.desc("ACCEPT")), LAUNCH_TIMEOUT);
        Thread.sleep(TIMEOUT);
        nextButton = mDevice.findObject(new UiSelector().description("ACCEPT"));
        nextButton.click();

        /*mDevice.wait(Until.findObject(By.desc("NEXT")), LAUNCH_TIMEOUT);
        mDevice.waitForIdle(2000);
        UiObject checkBoxAgree = mDevice.findObject(new UiSelector().className(CheckBox.class).resourceId("com.google.android.gms:id/agree_backup"));
        if (checkBoxAgree != null) {
            checkBoxAgree.click();
        }
        nextButton = mDevice.findObject(new UiSelector().description("NEXT"));
        if (nextButton != null) {
            nextButton.click();
        }*/

        Thread.sleep(7*TIMEOUT);
        mDevice.pressHome();

//        Intent intent1 = new Intent(Intent.ACTION_VIEW);
//        intent1.setData(Uri.parse("market://details?id=" + apk));
//        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        context.startActivity(intent1);
        context.startActivity(intent);
        mDevice.wait(Until.hasObject(By.pkg(GOOGLE_PLAY_PACKAGE).depth(0)), LAUNCH_TIMEOUT);

        UiObject startedButton = mDevice.findObject(new UiSelector().resourceId("com.android.vending:id/play_onboard_center_button"));
        if (startedButton.exists()) {
            startedButton.click();
        }

        Thread.sleep(TIMEOUT);
        UiObject searchImage = mDevice.findObject(new UiSelector().resourceId("com.android.vending:id/search_box_idle_text"));
        if (searchImage.exists()) {
            searchImage.click();
        }

        Thread.sleep(TIMEOUT);
        UiObject searchText = mDevice.findObject(new UiSelector().resourceId("com.android.vending:id/search_box_text_input"));
        if (searchText.exists()) {
            searchText.legacySetText(apk);
        }
        mDevice.pressEnter();

        Thread.sleep(TIMEOUT);
        UiObject titleText = mDevice.findObject(new UiSelector().resourceId("com.android.vending:id/li_title"));
        if (titleText.exists()) {
            titleText.click();
        }

        UiObject buyButton = mDevice.findObject(new UiSelector().resourceId("com.android.vending:id/buy_button"));
        if (buyButton.exists()) {
            buyButton.click();
        }

        Thread.sleep(TIMEOUT);
        UiObject acceptButton = mDevice.findObject(new UiSelector().resourceId("com.android.vending:id/continue_button"));
        if (acceptButton.exists()) {
            acceptButton.click();
        }

        Thread.sleep(5*TIMEOUT);
        UiObject openButton = mDevice.findObject(new UiSelector().resourceId("com.android.vending:id/launch_button"));
        if (openButton.exists()) {
            openButton.click();
        }
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

