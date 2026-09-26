package universeproxy.android.os.storage;

import java.io.File;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BField;

@BClassName("android.os.storage.StorageVolume")
public interface StorageVolume {
    @BField
    File mInternalPath();

    @BField
    File mPath();
}
