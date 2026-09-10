package universeproxy.android.nfc;

import android.os.IBinder;
import android.os.IInterface;

import com.duplicateapp.theuniverse.reflection.annotation.BClassName;
import com.duplicateapp.theuniverse.reflection.annotation.BStaticMethod;

@BClassName("android.nfc.INfcAdapter")
public interface INfcAdapter {
    @BClassName("android.nfc.INfcAdapter$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
