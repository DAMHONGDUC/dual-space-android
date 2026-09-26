package universeproxy.android.view;

import java.io.File;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BStaticMethod;

@BClassName("android.view.HardwareRenderer")
public interface HardwareRenderer {
    @BStaticMethod
    void setupDiskCache(File File0);
}
