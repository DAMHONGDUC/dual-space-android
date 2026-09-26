package universeproxy.android.app;

import android.graphics.drawable.Icon;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BField;

@BClassName("android.app.Notification")
public interface NotificationM {
    @BField
    Icon mLargeIcon();

    @BField
    Icon mSmallIcon();
}
