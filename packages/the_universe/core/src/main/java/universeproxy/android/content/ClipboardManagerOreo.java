package universeproxy.android.content;

import android.os.IInterface;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BField;
import com.dd.the.universe.reflection.annotation.BStaticField;

@BClassName("android.content.ClipboardManager")
public interface ClipboardManagerOreo {
    @BStaticField
    IInterface sService();

    @BField
    IInterface mService();
}
