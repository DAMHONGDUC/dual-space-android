package universeproxy.android.location;

import android.util.ArrayMap;

import com.duplicateapp.theuniverse.reflection.annotation.BClassName;
import com.duplicateapp.theuniverse.reflection.annotation.BField;

@BClassName("android.location.LocationManager")
public interface LocationManagerQ {
    @BField
    ArrayMap mGnssNmeaListeners();

    @BField
    ArrayMap mGnssStatusListeners();

    @BField
    ArrayMap mGpsNmeaListeners();

    @BField
    ArrayMap mGpsStatusListeners();

    @BField
    ArrayMap mListeners();
}
