package universeproxy.android.content.pm;

import android.content.pm.PackageManager;
import android.os.IInterface;
import android.os.UserManager;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BField;

@BClassName("android.content.pm.LauncherApps")
public interface LauncherApps {
    @BField
    PackageManager mPm();

    @BField
    IInterface mService();

    @BField
    UserManager mUserManager();
}
