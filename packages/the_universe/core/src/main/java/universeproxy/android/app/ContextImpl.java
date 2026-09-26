package universeproxy.android.app;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BField;
import com.dd.the.universe.reflection.annotation.BMethod;
import com.dd.the.universe.reflection.annotation.BStaticMethod;

@BClassName("android.app.ContextImpl")
public interface ContextImpl {
    @BField
    String mBasePackageName();

    @BField
    ContentResolver mContentResolver();

    @BField
    Object mPackageInfo();

    @BField
    PackageManager mPackageManager();

    @BStaticMethod
    Object createAppContext();

    @BMethod
    Context getReceiverRestrictedContext();

    @BMethod
    void setOuterContext(Context Context0);

    @BMethod
    Object getAttributionSource();

    @BMethod
    PackageManager getPackageManager();
}
