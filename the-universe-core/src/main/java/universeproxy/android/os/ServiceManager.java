package universeproxy.android.os;

import android.os.IBinder;
import android.os.IInterface;

import java.util.Map;

import com.duplicateapp.theuniverse.reflection.annotation.BClassName;
import com.duplicateapp.theuniverse.reflection.annotation.BStaticField;
import com.duplicateapp.theuniverse.reflection.annotation.BStaticMethod;

@BClassName("android.os.ServiceManager")
public interface ServiceManager {
    @BStaticField
    Map<String, IBinder> sCache();

    @BStaticField
    IInterface sServiceManager();

    @BStaticMethod
    void addService(String String0, IBinder IBinder1);

    @BStaticMethod
    IBinder checkService();

    @BStaticMethod
    IInterface getIServiceManager();

    @BStaticMethod
    IBinder getService(String name);

    @BStaticMethod
    String[] listServices();
}
