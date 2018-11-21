package com.ut.unilink;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.ut.unilink.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        SharedPreferences sharedPreferences = appContext.getSharedPreferences("lock", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        byte[] data = new byte[]{1, 2, 3, 4};
        try {
            editor.putString("a", new String(data, "ascii"));
            editor.commit();

            String a = sharedPreferences.getString("a", "");
            System.out.println(Log.toUnsignedHex(a.getBytes("ascii")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        assertEquals("com.ut.unilink.test", appContext.getPackageName());
    }
}
