package universeproxy.android.os;

import android.os.Handler.Callback;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BField;

@BClassName("android.os.Handler")
public interface Handler {
    @BField
    Callback mCallback();
}
