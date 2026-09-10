package universeproxy.android.telephony;


import com.duplicateapp.theuniverse.reflection.annotation.BClassName;
import com.duplicateapp.theuniverse.reflection.annotation.BConstructor;
import com.duplicateapp.theuniverse.reflection.annotation.BField;

@BClassName("android.telephony.CellIdentityGsm")
public interface CellIdentityGsm {
    @BConstructor
    CellIdentityGsm _new();

    @BField
    int mCid();

    @BField
    int mLac();

    @BField
    int mMcc();

    @BField
    int mMnc();
}
