package universeproxy.android.telephony;


import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BConstructor;
import com.dd.the.universe.reflection.annotation.BField;

@BClassName("android.telephony.CellSignalStrengthCdma")
public interface CellSignalStrengthCdma {
    @BConstructor
    CellSignalStrengthCdma _new();

    @BField
    int mCdmaDbm();

    @BField
    int mCdmaEcio();

    @BField
    int mEvdoDbm();

    @BField
    int mEvdoEcio();

    @BField
    int mEvdoSnr();
}
