package universeproxy.android.ddm;


import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BStaticMethod;

@BClassName("android.ddm.DdmHandleAppName")
public interface DdmHandleAppName {
    @BStaticMethod
    void setAppName(String String0, int i);
}
