package xyz.cirno.noswipetokill;

import android.util.Log;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class SmartPowerPolicyManagerWrapper {
    private final MethodHandle MH_isLockedApplication;
    public SmartPowerPolicyManagerWrapper(Object smartPowerPolicyManager) throws ReflectiveOperationException {
        MH_isLockedApplication = MethodHandles.lookup().findVirtual(smartPowerPolicyManager.getClass(), "isNoRestrictApp", MethodType.methodType(boolean.class, String.class)).bindTo(smartPowerPolicyManager);
    }

    public boolean isNoRestrictApp(String packageName) {
        try {
            return (boolean) MH_isLockedApplication.invokeExact(packageName);
        } catch (Throwable e) {
            Log.e("NoSwipeToKill", "isNoRestrictApp failed", e);
            return false;
        }
    }
}
