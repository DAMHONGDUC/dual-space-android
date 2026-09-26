package universeproxy.android.os;


import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BStaticMethod;

@BClassName("android.os.Process")
public interface Process {
    @BStaticMethod
    void setArgV0(String String0);
}
