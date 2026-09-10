package universeproxy.android.rms.resource;

import java.util.Map;

import com.duplicateapp.theuniverse.reflection.annotation.BClassName;
import com.duplicateapp.theuniverse.reflection.annotation.BField;

@BClassName("android.rms.resource.ReceiverResource")
public interface ReceiverResourceO {
    @BField
    Map<Integer, java.util.List<String>> mWhiteListMap();
}
