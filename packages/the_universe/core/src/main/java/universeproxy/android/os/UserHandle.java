package universeproxy.android.os;


import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BStaticMethod;

@BClassName("android.os.UserHandle")
public interface UserHandle {
    @BStaticMethod
    Integer myUserId();
}
