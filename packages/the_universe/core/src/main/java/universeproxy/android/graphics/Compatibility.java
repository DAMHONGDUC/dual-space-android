package universeproxy.android.graphics;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BStaticMethod;


@BClassName("android.graphics.Compatibility")
public interface Compatibility {
    @BStaticMethod
    void setTargetSdkVersion(int targetSdkVersion);
}
