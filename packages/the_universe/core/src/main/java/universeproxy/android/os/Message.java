package universeproxy.android.os;


import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BStaticMethod;

@BClassName("android.os.Message")
public interface Message {
    @BStaticMethod
    void updateCheckRecycle(int int0);
}
