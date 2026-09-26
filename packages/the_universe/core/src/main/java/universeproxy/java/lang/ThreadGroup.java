package universeproxy.java.lang;

import java.util.List;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BField;

@BClassName("java.lang.ThreadGroup")
public interface ThreadGroup {
    @BField
    List<java.lang.ThreadGroup> groups();

    @BField
    java.lang.ThreadGroup parent();
}
