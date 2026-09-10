package universeproxy.android.os.storage;

import java.io.File;

import com.duplicateapp.theuniverse.reflection.annotation.BClassName;
import com.duplicateapp.theuniverse.reflection.annotation.BField;

@BClassName("android.os.storage.StorageVolume")
public interface StorageVolume {
    @BField
    File mInternalPath();

    @BField
    File mPath();
}
