package universeproxy.android.app;


import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BField;

@BClassName("android.app.Notification")
public interface NotificationO {
    @BField
    String mChannelId();

    @BField
    String mGroupKey();
}
