package universeproxy.android.os;

import android.os.Parcel;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BField;

@BClassName("android.os.BaseBundle")
public interface BaseBundle {
    @BField
    Parcel mParcelledData();
}
