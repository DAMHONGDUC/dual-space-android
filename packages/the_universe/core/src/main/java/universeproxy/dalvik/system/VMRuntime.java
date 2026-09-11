package universeproxy.dalvik.system;


import com.duplicateapp.theuniverse.reflection.annotation.BClassName;
import com.duplicateapp.theuniverse.reflection.annotation.BMethod;
import com.duplicateapp.theuniverse.reflection.annotation.BStaticMethod;

@BClassName("dalvik.system.VMRuntime")
public interface VMRuntime {
    @BStaticMethod
    String getCurrentInstructionSet();

    @BStaticMethod
    Object getRuntime();

    @BStaticMethod
    Boolean is64BitAbi(String String0);

    @BMethod
    Boolean is64Bit();

    @BMethod
    Boolean isJavaDebuggable();

    @BMethod
    void setTargetSdkVersion(int int0);
}
