package universeproxy.android.app;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BStaticField;
import com.dd.the.universe.reflection.annotation.BStaticMethod;

@BClassName("android.app.ActivityTaskManager")
public interface ActivityTaskManager {

    @BStaticMethod
    Object getService();

    @BStaticField
    Object IActivityTaskManagerSingleton();
}
