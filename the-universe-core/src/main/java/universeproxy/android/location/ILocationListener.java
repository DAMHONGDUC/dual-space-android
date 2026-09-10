package universeproxy.android.location;

import android.location.Location;
import android.os.IBinder;
import android.os.IInterface;

import com.duplicateapp.theuniverse.reflection.annotation.BClassName;
import com.duplicateapp.theuniverse.reflection.annotation.BMethod;
import com.duplicateapp.theuniverse.reflection.annotation.BStaticMethod;

@BClassName("android.location.ILocationListener")
public interface ILocationListener {
    @BMethod
    void onLocationChanged(Location Location0);

    @BClassName("android.location.ILocationListener$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
