package by.angellab.click.magicclick;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;
import android.widget.ScrollView;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Created by yegia on 18.06.2016.
 */
public class Home {
    private static final String BASIC_SAMPLE_PACKAGE = "com.android.vending";
    private static final String SETTINGS_PACKAGE = "com.android.settings";
    private static final int LAUNCH_TIMEOUT = 5000;
    private static final String STRING_TO_BE_TYPED = "UiAutomator";
    private UiDevice mDevice;
    private String appPackageName = "com.keuwl.wifi";
    private Context context;

    @Before
    public void startMainActivityFromHomeScreen() {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        context = InstrumentationRegistry.getContext();

    }

    @Test
    public void test() throws UiObjectNotFoundException, InterruptedException {

        startPackage(SETTINGS_PACKAGE);

        UiScrollable listScroll = new UiScrollable(new UiSelector().className(ScrollView.class));
        UiObject usersItem = listScroll.getChildByText(new UiSelector().className(TextView.class), "Users");
        if (usersItem.exists()) {
            usersItem.click();
        }

        for (int i = 0; i < 5; i++) {
            UiObject guestTextView = mDevice.findObject(new UiSelector().text("Guest"));
            if (guestTextView.exists()) {
                guestTextView.click();
            }

            Thread.sleep(15000);

            startPackage(SETTINGS_PACKAGE);

            // Wait for the app to appear
            mDevice.wait(Until.hasObject(By.pkg(SETTINGS_PACKAGE).depth(0)), LAUNCH_TIMEOUT);

            usersItem = mDevice.findObject(new UiSelector().text("Users"));
            if (usersItem.exists()) {
                usersItem.click();
            }
            guestTextView = mDevice.findObject(new UiSelector().text("Remove guest"));
            if (guestTextView.exists()) {
                guestTextView.click();
            }
            UiObject removeBtn = mDevice.findObject(new UiSelector().resourceId("android:id/button1"));
            if (removeBtn.exists()) {
                removeBtn.click();
            }
            Thread.sleep(3000);
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

    private void startPackage(String packageName){
        // Start from the home screen
        //mDevice.pressHome();

        // Wait for launcher
        final String launcherPackage = getLauncherPackageName();
        assertThat(launcherPackage, notNullValue());
        mDevice.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), LAUNCH_TIMEOUT);

        // Launch the blueprint app
        final Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(packageName);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);    // Clear out any previous instances
        context.startActivity(intent);

        // Wait for the app to appear
        mDevice.wait(Until.hasObject(By.pkg(packageName).depth(0)), LAUNCH_TIMEOUT);
    }
}
