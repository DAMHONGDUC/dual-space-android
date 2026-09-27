package universeproxy.android.location;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BField;
import com.dd.the.universe.reflection.annotation.BMethod;

@BClassName("com.dd.the.universe.compat.location.BzRoutePassport")
public interface BzRoutePassportL {
    @BField
    String provider();

    @BMethod
    String getProvider();
}
