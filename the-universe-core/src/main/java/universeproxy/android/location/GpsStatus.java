package universeproxy.android.location;


import com.duplicateapp.theuniverse.reflection.annotation.BClassName;
import com.duplicateapp.theuniverse.reflection.annotation.BMethod;

@BClassName("android.location.GpsStatus")
public interface GpsStatus {
    @BMethod
    void setStatus(int int0, int[] ints1, float[] floats2, float[] floats3, float[] floats4, int int5, int int6, int int7);
}
