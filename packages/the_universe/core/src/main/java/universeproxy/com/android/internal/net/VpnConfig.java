package universeproxy.com.android.internal.net;

import java.util.List;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BField;


@BClassName("com.android.internal.net.VpnConfig")
public interface VpnConfig {
    @BField
    String user();

    @BField
    List<String> disallowedApplications();

    @BField
    List<String> allowedApplications();
}
