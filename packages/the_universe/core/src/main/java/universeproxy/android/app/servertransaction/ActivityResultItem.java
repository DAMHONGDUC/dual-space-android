package universeproxy.android.app.servertransaction;

import java.util.List;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BField;

@BClassName("android.app.servertransaction.ActivityResultItem")
public interface ActivityResultItem {
    @BField
    List mResultInfoList();
}
