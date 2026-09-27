package com.dd.dual.space.guestfixture;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Records what the guest sees from inside its own sandbox so the host test can assert isolation. */
public class FixtureActivity extends Activity {
    private static final String TAG = "GuestFixture";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences("fixture", MODE_PRIVATE);
        int launches = preferences.getInt("launches", 0) + 1;
        preferences.edit().putInt("launches", launches).commit();
        String report = "launches=" + launches + "\npid=" + Process.myPid() + "\nfilesDir=" + getFilesDir().getAbsolutePath() + "\n";
        File marker = new File(getFilesDir(), "guest_marker.txt");
        try (FileOutputStream output = new FileOutputStream(marker)) {
            output.write(report.getBytes(StandardCharsets.UTF_8));
            output.getFD().sync();
            Log.i(TAG, "marker_written launches=" + launches);
        } catch (IOException error) {
            Log.e(TAG, "marker_failed", error);
        }
    }
}
