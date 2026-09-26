package universeproxy.android.content.pm;


import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BConstructor;
import com.dd.the.universe.reflection.annotation.BStaticField;

@BClassName("android.content.pm.UserInfo")
public interface UserInfo {
    @BConstructor
    Object _new(int id, String name, int flags);

    @BStaticField
    int FLAG_PRIMARY();
}
