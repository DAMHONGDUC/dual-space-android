package universeproxy.android.rms.resource;

import java.util.List;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BField;

@BClassName("android.rms.resource.ReceiverResource")
public interface ReceiverResourceN {
    @BField
    List<String> mWhiteList();
}
