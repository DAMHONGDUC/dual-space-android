package universeproxy.android.service.notification;


import com.duplicateapp.theuniverse.reflection.annotation.BClassName;
import com.duplicateapp.theuniverse.reflection.annotation.BField;

@BClassName("android.service.notification.StatusBarNotification")
public interface StatusBarNotification {
    @BField
    Integer id();

    @BField
    String opPkg();

    @BField
    String pkg();

    @BField
    String tag();
}
