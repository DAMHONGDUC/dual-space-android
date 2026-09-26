package universeproxy.android.webkit;


import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BMethod;

@BClassName("android.webkit.IWebViewUpdateService")
public interface IWebViewUpdateService {
    @BMethod
    String getCurrentWebViewPackageName();

    @BMethod
    Object waitForAndGetProvider();
}
