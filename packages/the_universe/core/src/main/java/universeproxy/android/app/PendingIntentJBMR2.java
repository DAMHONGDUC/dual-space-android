package universeproxy.android.app;

import android.content.Intent;
import android.os.IBinder;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BConstructor;
import com.dd.the.universe.reflection.annotation.BMethod;

@BClassName("android.app.PendingIntent")
public interface PendingIntentJBMR2 {
    @BConstructor
    PendingIntentJBMR2 _new(IBinder IBinder0);

    @BMethod
    Intent getIntent();
}
