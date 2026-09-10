package universeproxy.android.os;


import com.duplicateapp.theuniverse.reflection.annotation.BClassName;
import com.duplicateapp.theuniverse.reflection.annotation.BStaticMethod;

@BClassName("android.os.Message")
public interface Message {
    @BStaticMethod
    void updateCheckRecycle(int int0);
}
