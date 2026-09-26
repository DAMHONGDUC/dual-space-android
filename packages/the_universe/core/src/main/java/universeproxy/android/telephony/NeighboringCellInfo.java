package universeproxy.android.telephony;


import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BField;

@BClassName("android.telephony.NeighboringCellInfo")
public interface NeighboringCellInfo {
    @BField
    int mCid();

    @BField
    int mLac();

    @BField
    int mRssi();
}
