package universeproxy.android.view;

import android.os.IInterface;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BStaticField;

@BClassName("android.view.Display")
public interface Display {
    @BStaticField
    IInterface sWindowManager();
}
