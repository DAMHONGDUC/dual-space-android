package universeproxy.android.content.pm;


import android.content.pm.PackageParser;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BStaticMethod;

@BClassName("android.content.pm.PackageParser")
public interface PackageParserNougat {
    @BStaticMethod
    void collectCertificates(PackageParser.Package p, int flags);
}
