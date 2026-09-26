package universeproxy.android.telephony;


import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BMethod;

@BClassName("android.telephony.SmsManager")
public interface SmsManager {
    @BMethod
    Boolean getAutoPersisting();

    @BMethod
    void setAutoPersisting(boolean boolean0);
}
