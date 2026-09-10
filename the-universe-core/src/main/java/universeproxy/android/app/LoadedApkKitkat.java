package universeproxy.android.app;


import com.duplicateapp.theuniverse.reflection.annotation.BClassName;
import com.duplicateapp.theuniverse.reflection.annotation.BField;

@BClassName("android.app.LoadedApk")
public interface LoadedApkKitkat {
    @BField
    Object mDisplayAdjustments();
}
