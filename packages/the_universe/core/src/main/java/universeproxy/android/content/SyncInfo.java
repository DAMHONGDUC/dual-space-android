package universeproxy.android.content;

import android.accounts.Account;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BConstructor;

@BClassName("android.content.SyncInfo")
public interface SyncInfo {
    @BConstructor
    SyncInfo _new(int int0, Account Account1, String String2, long long3);
}
