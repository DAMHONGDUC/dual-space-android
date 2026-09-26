package universeproxy.android.app;

import java.io.File;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BField;

@BClassName("android.app.ContextImpl")
public interface ContextImplICS {
    @BField
    File mExternalCacheDir();

    @BField
    File mExternalFilesDir();
}
