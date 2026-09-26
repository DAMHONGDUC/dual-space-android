package universeproxy.android.telephony;


import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BConstructor;
import com.dd.the.universe.reflection.annotation.BField;

@BClassName("android.telephony.CellIdentityCdma")
public interface CellIdentityCdma {
    @BConstructor
    CellIdentityCdma _new();

    @BField
    int mBasestationId();

    @BField
    int mNetworkId();

    @BField
    int mSystemId();
}
