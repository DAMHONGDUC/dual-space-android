package universeproxy.android.app;

import android.content.Intent;
import android.os.IBinder;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BMethod;

@BClassName("android.app.IActivityManager")
public interface IActivityManagerL {
    @BMethod
    Boolean finishActivity(IBinder IBinder0, int int1, Intent Intent2, boolean boolean3);
}
