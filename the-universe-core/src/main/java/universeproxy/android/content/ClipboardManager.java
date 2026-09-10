package universeproxy.android.content;

import android.os.IInterface;

import com.duplicateapp.theuniverse.reflection.annotation.BClassName;
import com.duplicateapp.theuniverse.reflection.annotation.BStaticField;
import com.duplicateapp.theuniverse.reflection.annotation.BStaticMethod;

@BClassName("android.content.ClipboardManager")
public interface ClipboardManager {
    @BStaticField
    IInterface sService();

    @BStaticMethod
    IInterface getService();
}
