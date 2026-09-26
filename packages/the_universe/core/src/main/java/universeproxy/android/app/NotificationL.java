package universeproxy.android.app;

import android.app.Notification;
import android.content.Context;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BStaticMethod;

@BClassName("android.app.Notification")
public interface NotificationL {
    @BClassName("android.app.Notification$Builder")
    interface Builder {
        @BStaticMethod
        Notification rebuild(Context Context0, Notification Notification1);
    }
}
