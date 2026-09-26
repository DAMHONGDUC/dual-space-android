package universeproxy.android.os.storage;

import android.os.storage.StorageVolume;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BStaticMethod;

@BClassName("android.os.storage.StorageManager")
public interface StorageManager {
    @BStaticMethod
    StorageVolume[] getVolumeList(int int0, int int1);
}
