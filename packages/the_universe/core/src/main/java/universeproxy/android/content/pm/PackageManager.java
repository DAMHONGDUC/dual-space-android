package universeproxy.android.content.pm;

import com.duplicateapp.theuniverse.reflection.annotation.BClassName;
import com.duplicateapp.theuniverse.reflection.annotation.BField;
import com.duplicateapp.theuniverse.reflection.annotation.BMethod;
import com.duplicateapp.theuniverse.reflection.annotation.BStaticMethod;

@BClassName("android.content.pm.PackageManager")
public interface PackageManager {
    @BStaticMethod
    void disableApplicationInfoCache();
}
