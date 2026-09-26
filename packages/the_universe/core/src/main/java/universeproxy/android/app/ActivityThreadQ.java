package universeproxy.android.app;

import android.os.IBinder;

import java.util.List;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BMethod;

@BClassName("android.app.ActivityThread")
public interface ActivityThreadQ {
    @BMethod
    void handleNewIntent(IBinder IBinder0, List List1);
}
