package universeproxy.com.android.internal.view.inputmethod;

import android.os.IInterface;

import com.duplicateapp.theuniverse.reflection.annotation.BClassName;
import com.duplicateapp.theuniverse.reflection.annotation.BField;

@BClassName("android.view.inputmethod.InputMethodManager")
public interface InputMethodManager {
    @BField
    IInterface mService();
}
