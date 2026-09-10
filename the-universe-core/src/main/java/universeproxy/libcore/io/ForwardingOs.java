package universeproxy.libcore.io;


import com.duplicateapp.theuniverse.reflection.annotation.BClassName;
import com.duplicateapp.theuniverse.reflection.annotation.BField;

@BClassName("libcore.io.ForwardingOs")
public interface ForwardingOs {
    @BField
    Object os();
}
