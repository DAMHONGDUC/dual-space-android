package universeproxy.android.location;

import com.duplicateapp.theuniverse.reflection.annotation.BClassName;
import com.duplicateapp.theuniverse.reflection.annotation.BField;
import com.duplicateapp.theuniverse.reflection.annotation.BMethod;

@BClassName("com.duplicateapp.theuniverse.compat.location.BzRoutePassport")
public interface BzRoutePassportL {
    @BField
    String provider();

    @BMethod
    String getProvider();
}
