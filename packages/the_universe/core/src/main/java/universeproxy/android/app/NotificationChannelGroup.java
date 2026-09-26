package universeproxy.android.app;

import java.util.List;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BField;

@BClassName("android.app.NotificationChannelGroup")
public interface NotificationChannelGroup {
    @BField
    List<android.app.NotificationChannel> mChannels();

    @BField
    String mId();
}
