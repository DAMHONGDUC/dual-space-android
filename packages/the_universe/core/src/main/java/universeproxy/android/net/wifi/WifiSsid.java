package universeproxy.android.net.wifi;


import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BStaticMethod;

@BClassName("android.net.wifi.WifiSsid")
public interface WifiSsid {
    @BStaticMethod
    Object createFromAsciiEncoded(String asciiEncoded);
}
