package com.dd.the.universe.fake.hook;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import com.dd.the.universe.TheUniverseCore;
import com.dd.the.universe.fake.delegate.AppInstrumentation;

import com.dd.the.universe.fake.service.HCallbackProxy;
import com.dd.the.universe.fake.service.IAccessibilityManagerProxy;
import com.dd.the.universe.fake.service.IAccountManagerProxy;
import com.dd.the.universe.fake.service.IActivityClientProxy;
import com.dd.the.universe.fake.service.IActivityManagerProxy;
import com.dd.the.universe.fake.service.IActivityTaskManagerProxy;
import com.dd.the.universe.fake.service.IAlarmManagerProxy;
import com.dd.the.universe.fake.service.IAppOpsManagerProxy;
import com.dd.the.universe.fake.service.IAppWidgetManagerProxy;
import com.dd.the.universe.fake.service.IAttributionSourceProxy;
import com.dd.the.universe.fake.service.IAutofillManagerProxy;
import com.dd.the.universe.fake.service.IInputMethodManagerProxy;
import com.dd.the.universe.fake.service.ICredentialManagerProxy;
import com.dd.the.universe.fake.service.ISensitiveContentProtectionManagerProxy;
import com.dd.the.universe.fake.service.ISettingsSystemProxy;
import com.dd.the.universe.fake.service.IConnectivityManagerProxy;
import com.dd.the.universe.fake.service.ISystemSensorManagerProxy;
import com.dd.the.universe.fake.service.IContentProviderProxy;
import com.dd.the.universe.fake.service.IXiaomiAttributionSourceProxy;
import com.dd.the.universe.fake.service.IXiaomiSettingsProxy;
import com.dd.the.universe.fake.service.IXiaomiMiuiServicesProxy;
import com.dd.the.universe.fake.service.IDnsResolverProxy;
import com.dd.the.universe.fake.service.IContextHubServiceProxy;
import com.dd.the.universe.fake.service.IDeviceIdentifiersPolicyProxy;
import com.dd.the.universe.fake.service.IDevicePolicyManagerProxy;
import com.dd.the.universe.fake.service.IDisplayManagerProxy;
import com.dd.the.universe.fake.service.IFingerprintManagerProxy;
import com.dd.the.universe.fake.service.IGraphicsStatsProxy;
import com.dd.the.universe.fake.service.IJobServiceProxy;
import com.dd.the.universe.fake.service.ILauncherAppsProxy;
import com.dd.the.universe.fake.service.ILocaleManagerProxy;
import com.dd.the.universe.fake.service.ILocationManagerProxy;
import com.dd.the.universe.fake.service.IMediaRouterServiceProxy;
import com.dd.the.universe.fake.service.IMediaSessionManagerProxy;
import com.dd.the.universe.fake.service.IAudioServiceProxy;
import com.dd.the.universe.fake.service.ISensorPrivacyManagerProxy;
import com.dd.the.universe.fake.service.ContentResolverProxy;
import com.dd.the.universe.fake.service.IWebViewUpdateServiceProxy;
import com.dd.the.universe.fake.service.IMiuiSecurityManagerProxy;
import com.dd.the.universe.fake.service.SystemLibraryProxy;
import com.dd.the.universe.fake.service.ReLinkerProxy;
import com.dd.the.universe.fake.service.WebViewProxy;
import com.dd.the.universe.fake.service.WebViewFactoryProxy;
import com.dd.the.universe.fake.service.MediaRecorderProxy;
import com.dd.the.universe.fake.service.AudioRecordProxy;
import com.dd.the.universe.fake.service.MediaRecorderClassProxy;
import com.dd.the.universe.fake.service.SQLiteDatabaseProxy;
import com.dd.the.universe.fake.service.ClassLoaderProxy;
import com.dd.the.universe.fake.service.FileSystemProxy;
import com.dd.the.universe.fake.service.GmsProxy;
import com.dd.the.universe.fake.service.LevelDbProxy;
import com.dd.the.universe.fake.service.DeviceIdProxy;
import com.dd.the.universe.fake.service.GoogleAccountManagerProxy;
import com.dd.the.universe.fake.service.AuthenticationProxy;
import com.dd.the.universe.fake.service.AndroidIdProxy;
import com.dd.the.universe.fake.service.AudioPermissionProxy;

import com.dd.the.universe.fake.service.INetworkManagementServiceProxy;
import com.dd.the.universe.fake.service.INotificationManagerProxy;
import com.dd.the.universe.fake.service.IPackageManagerProxy;
import com.dd.the.universe.fake.service.IPermissionManagerProxy;
import com.dd.the.universe.fake.service.IPersistentDataBlockServiceProxy;
import com.dd.the.universe.fake.service.IPhoneSubInfoProxy;
import com.dd.the.universe.fake.service.IPhoneSubInfoServiceProxy;
import com.dd.the.universe.fake.service.IPowerManagerProxy;
import com.dd.the.universe.fake.service.ApkAssetsProxy;
import com.dd.the.universe.fake.service.ResourcesManagerProxy;
import com.dd.the.universe.fake.service.IRoleManagerProxy;
import com.dd.the.universe.fake.service.IShortcutManagerProxy;
import com.dd.the.universe.fake.service.IStorageManagerProxy;
import com.dd.the.universe.fake.service.IStorageStatsManagerProxy;
import com.dd.the.universe.fake.service.ISystemUpdateProxy;
import com.dd.the.universe.fake.service.ISubServiceProxy;
import com.dd.the.universe.fake.service.ITelephonyManagerProxy;
import com.dd.the.universe.fake.service.ITelephonyRegistryProxy;
import com.dd.the.universe.fake.service.IUiModeManagerProxy;
import com.dd.the.universe.fake.service.IUserManagerProxy;
import com.dd.the.universe.fake.service.IVibratorServiceProxy;
import com.dd.the.universe.fake.service.IVpnManagerProxy;
import com.dd.the.universe.fake.service.IWifiManagerProxy;
import com.dd.the.universe.fake.service.IWifiScannerProxy;
import com.dd.the.universe.fake.service.IWindowManagerProxy;
import com.dd.the.universe.fake.service.context.ContentServiceStub;
import com.dd.the.universe.fake.service.context.RestrictionsManagerStub;
import com.dd.the.universe.fake.service.libcore.OsStub;
import com.dd.the.universe.utils.Slog;
import com.dd.the.universe.utils.compat.BuildCompat;
import com.dd.the.universe.fake.service.ISettingsProviderProxy;
import com.dd.the.universe.fake.service.FeatureFlagUtilsProxy;
import com.dd.the.universe.fake.service.WorkManagerProxy;



public class HookManager {
    public static final String TAG = "HookManager";

    private static final HookManager sHookManager = new HookManager();

    private final Map<Class<?>, IInjectHook> mInjectors = new HashMap<>();

    public static HookManager get() {
        return sHookManager;
    }

    public void init() {
        if (TheUniverseCore.get().isBlackProcess() || TheUniverseCore.get().isServerProcess()) {
            addInjector(new IDisplayManagerProxy());
            addInjector(new OsStub());
            addInjector(new IActivityManagerProxy());
            addInjector(new IPackageManagerProxy());
            addInjector(new ITelephonyManagerProxy());
            addInjector(new HCallbackProxy());
            addInjector(new IAppOpsManagerProxy());
            addInjector(new INotificationManagerProxy());
            addInjector(new IAlarmManagerProxy());
            addInjector(new IAppWidgetManagerProxy());
            addInjector(new IInputMethodManagerProxy());
            addInjector(new ContentServiceStub());
            addInjector(new IWindowManagerProxy());
            addInjector(new IUserManagerProxy());
            addInjector(new RestrictionsManagerStub());
            addInjector(new IMediaSessionManagerProxy());
            addInjector(new IAudioServiceProxy());
            addInjector(new ISensorPrivacyManagerProxy());
            addInjector(new ContentResolverProxy());
            addInjector(new IWebViewUpdateServiceProxy());
            addInjector(new SystemLibraryProxy());
            addInjector(new ReLinkerProxy());
            addInjector(new WebViewProxy());
            addInjector(new WebViewFactoryProxy());
            addInjector(new WorkManagerProxy());
            addInjector(new MediaRecorderProxy());
            addInjector(new AudioRecordProxy());
            addInjector(new IMiuiSecurityManagerProxy());
            addInjector(new ISettingsProviderProxy());
            addInjector(new FeatureFlagUtilsProxy());
            addInjector(new MediaRecorderClassProxy());
            addInjector(new SQLiteDatabaseProxy());
            addInjector(new ClassLoaderProxy());
            addInjector(new FileSystemProxy());
            addInjector(new GmsProxy());
            addInjector(new LevelDbProxy());
            addInjector(new DeviceIdProxy());
            addInjector(new GoogleAccountManagerProxy());
            addInjector(new AuthenticationProxy());
            addInjector(new AndroidIdProxy());
            addInjector(new AudioPermissionProxy());
            addInjector(new ILocationManagerProxy());
            addInjector(new IStorageManagerProxy());
            addInjector(new ILauncherAppsProxy());
            addInjector(new ILocaleManagerProxy());
            addInjector(new IJobServiceProxy());
            addInjector(new IAccessibilityManagerProxy());
            addInjector(new ITelephonyRegistryProxy());
            addInjector(new IDevicePolicyManagerProxy());
            addInjector(new IAccountManagerProxy());
            addInjector(new IConnectivityManagerProxy());
            addInjector(new IDnsResolverProxy());
                    addInjector(new IAttributionSourceProxy());
        addInjector(new IContentProviderProxy());
        addInjector(new ISettingsSystemProxy());
        addInjector(new ISystemSensorManagerProxy());

            if (android.os.Build.VERSION.SDK_INT >= 34) {
                addInjector(new ICredentialManagerProxy());
            }
        
        
        addInjector(new IXiaomiAttributionSourceProxy());
        addInjector(new IXiaomiSettingsProxy());
        addInjector(new IXiaomiMiuiServicesProxy());
            addInjector(new IPhoneSubInfoServiceProxy());
            addInjector(new ISubServiceProxy());
            addInjector(new IPhoneSubInfoProxy());
            addInjector(new IMediaRouterServiceProxy());
            addInjector(new IPowerManagerProxy());
            addInjector(new IContextHubServiceProxy());
            
            addInjector(new IVibratorServiceProxy());
            addInjector(new IPersistentDataBlockServiceProxy());
            addInjector(AppInstrumentation.get());
            
            addInjector(new IWifiManagerProxy());
            addInjector(new IWifiScannerProxy());
            addInjector(new ApkAssetsProxy());
            addInjector(new ResourcesManagerProxy());
            
            if (BuildCompat.isS()) {
                addInjector(new IActivityClientProxy(null));
                addInjector(new IVpnManagerProxy());
            }
            
            if (BuildCompat.isS()) {
                addInjector(new ISensitiveContentProtectionManagerProxy());
            }
            
            if (BuildCompat.isR()) {
                addInjector(new IPermissionManagerProxy());
            }
            
            if (BuildCompat.isQ()) {
                addInjector(new IActivityTaskManagerProxy());
                addInjector(new IRoleManagerProxy());
            }
            
            if (BuildCompat.isPie()) {
                addInjector(new ISystemUpdateProxy());
            }
            
            if (BuildCompat.isOreo()) {
                addInjector(new IAutofillManagerProxy());
                addInjector(new IDeviceIdentifiersPolicyProxy());
                addInjector(new IStorageStatsManagerProxy());
            }
            
            if (BuildCompat.isN_MR1()) {
            addInjector(new IUiModeManagerProxy());
                addInjector(new IShortcutManagerProxy());
            }
            
            if (BuildCompat.isN()) {
                addInjector(new INetworkManagementServiceProxy());
            }
            
            if (BuildCompat.isM()) {
                addInjector(new IFingerprintManagerProxy());
                addInjector(new IGraphicsStatsProxy());
            }
            
            if (BuildCompat.isL()) {
                addInjector(new IJobServiceProxy());
            }
        }
        injectAll();
    }

    public void checkEnv(Class<?> clazz) {
        IInjectHook iInjectHook = mInjectors.get(clazz);
        if (iInjectHook != null && iInjectHook.isBadEnv()) {
            Log.d(TAG, "checkEnv: " + clazz.getSimpleName() + " is bad env");
            iInjectHook.injectHook();
        }
    }

    public void checkAll() {
        for (Class<?> aClass : mInjectors.keySet()) {
            IInjectHook iInjectHook = mInjectors.get(aClass);
            if (iInjectHook != null && iInjectHook.isBadEnv()) {
                Log.d(TAG, "checkEnv: " + aClass.getSimpleName() + " is bad env");
                iInjectHook.injectHook();
            }
        }
    }

    void addInjector(IInjectHook injectHook) {
        mInjectors.put(injectHook.getClass(), injectHook);
    }

    void injectAll() {
        for (IInjectHook value : mInjectors.values()) {
            try {
                Slog.d(TAG, "hook: " + value);
                value.injectHook();
            } catch (Exception e) {
                Slog.d(TAG, "hook error: " + value);
                
                handleHookError(value, e);
            }
        }
    }

    
    private void handleHookError(IInjectHook hook, Exception e) {
        String hookName = hook.getClass().getSimpleName();
        
        
        Slog.e(TAG, "Hook failed: " + hookName + " - " + e.getMessage(), e);
        
        
        if (hookName.contains("ActivityManager") || 
            hookName.contains("PackageManager") ||
            hookName.contains("WebView") ||
            hookName.contains("ContentProvider")) {
            
            Slog.w(TAG, "Critical hook failed: " + hookName + ", attempting recovery");
            
            try {
                
                if (hook.isBadEnv()) {
                    Slog.d(TAG, "Attempting to recover hook: " + hookName);
                    hook.injectHook();
                }
            } catch (Exception recoveryException) {
                Slog.e(TAG, "Hook recovery failed: " + hookName, recoveryException);
            }
        }
    }

    
    public boolean areCriticalHooksInstalled() {
        String[] criticalHooks = {
            "IActivityManagerProxy",
            "IPackageManagerProxy", 
            "WebViewProxy",
            "IContentProviderProxy"
        };
        
        for (String hookName : criticalHooks) {
            boolean found = false;
            for (Class<?> hookClass : mInjectors.keySet()) {
                if (hookClass.getSimpleName().equals(hookName)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                Slog.w(TAG, "Critical hook missing: " + hookName);
                return false;
            }
        }
        
        Slog.d(TAG, "All critical hooks are installed");
        return true;
    }

    
    public void reinitializeHooks() {
        Slog.d(TAG, "Reinitializing all hooks");
        
        
        mInjectors.clear();
        
        
        init();
        
        Slog.d(TAG, "Hook reinitialization completed");
    }
}
