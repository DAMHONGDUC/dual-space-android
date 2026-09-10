package universeproxy.android.app;

import android.app.PendingIntent;
import android.content.Context;

import com.duplicateapp.theuniverse.reflection.annotation.BClassName;
import com.duplicateapp.theuniverse.reflection.annotation.BMethod;

@BClassName("android.app.Notification")
public interface Notification {
    @BMethod
    void setLatestEventInfo(Context Context0, CharSequence CharSequence1, CharSequence CharSequence2, PendingIntent PendingIntent3);
}
