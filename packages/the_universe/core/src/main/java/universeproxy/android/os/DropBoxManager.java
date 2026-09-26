package universeproxy.android.os;

import android.os.IInterface;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BField;

@BClassName("android.os.DropBoxManager")
public interface DropBoxManager {
    @BField
    IInterface mService();
}
