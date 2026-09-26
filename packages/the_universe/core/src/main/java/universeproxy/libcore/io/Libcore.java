package universeproxy.libcore.io;


import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BStaticField;

@BClassName("libcore.io.Libcore")
public interface Libcore {
    @BStaticField
    Object os();
}
