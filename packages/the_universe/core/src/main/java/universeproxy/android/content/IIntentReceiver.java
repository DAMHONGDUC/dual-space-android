package universeproxy.android.content;

import android.content.Intent;
import android.os.Bundle;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BMethod;

@BClassName("android.content.IIntentReceiver")
public interface IIntentReceiver {
    @BMethod
    void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser);
}
