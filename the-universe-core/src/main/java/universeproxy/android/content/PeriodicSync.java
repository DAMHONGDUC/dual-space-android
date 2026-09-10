package universeproxy.android.content;


import com.duplicateapp.theuniverse.reflection.annotation.BClassName;
import com.duplicateapp.theuniverse.reflection.annotation.BField;

@BClassName("android.content.PeriodicSync")
public interface PeriodicSync {
    @BField
    long flexTime();
}
