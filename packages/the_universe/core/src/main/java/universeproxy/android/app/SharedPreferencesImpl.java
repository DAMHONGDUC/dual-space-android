package universeproxy.android.app;

import java.io.File;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BConstructor;

@BClassName("android.app.SharedPreferencesImpl")
public interface SharedPreferencesImpl {
    @BConstructor
    SharedPreferencesImpl _new(File File0, int int1);
}
