package universeproxy.android.view;

import java.io.File;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BStaticMethod;

@BClassName("android.view.ThreadedRenderer")
public interface ThreadedRenderer {
    @BStaticMethod
    void setupDiskCache(File File0);
}
