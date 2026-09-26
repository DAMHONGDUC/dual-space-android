package universeproxy.android.content;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BField;
import com.dd.the.universe.reflection.annotation.BMethod;


@BClassName("android.content.AttributionSource")
public interface AttributionSource {
    @BField
    Object mAttributionSourceState();

    @BMethod
    Object getNext();
}
