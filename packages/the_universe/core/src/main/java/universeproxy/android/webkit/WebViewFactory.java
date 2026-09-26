package universeproxy.android.webkit;


import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BStaticField;
import com.dd.the.universe.reflection.annotation.BStaticMethod;

@BClassName("android.webkit.WebViewFactory")
public interface WebViewFactory {
    @BStaticField
    Boolean sWebViewSupported();

    @BStaticMethod
    Object getUpdateService();
}
