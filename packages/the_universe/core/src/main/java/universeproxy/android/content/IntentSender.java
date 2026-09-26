package universeproxy.android.content;

import android.os.IInterface;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BField;

@BClassName("android.content.IntentSender")
public interface IntentSender {
    @BField
    IInterface mTarget();
}
