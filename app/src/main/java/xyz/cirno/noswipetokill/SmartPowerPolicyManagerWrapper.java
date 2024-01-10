package xyz.cirno.noswipetokill;

import android.util.Log;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class SmartPowerPolicyManagerWrapper {
    private static Class<?> cls;
    private static MethodHandle MH_isNoRestrictApp;

    public static void initialize(ClassLoader classLoader) throws ReflectiveOperationException {
        cls = Class.forName("com.miui.server.smartpower.SmartPowerPolicyManager", false, classLoader);
        var method = cls.getDeclaredMethod("isNoRestrictApp", String.class);
        MH_isNoRestrictApp = MethodHandles.lookup().unreflect(method).asType(MethodType.methodType(boolean.class, Object.class, String.class));
    }

    private final Object wrapped;

    public SmartPowerPolicyManagerWrapper(Object smartPowerPolicyManager) {
        if (smartPowerPolicyManager == null) {
            throw new NullPointerException();
        }
        if (!cls.isInstance(smartPowerPolicyManager)) {
            throw new IllegalArgumentException();
        }
        wrapped = smartPowerPolicyManager;
    }

    public boolean isNoRestrictApp(String packageName) {
        try {
            return (boolean) MH_isNoRestrictApp.invokeExact(wrapped, packageName);
        } catch (Throwable e) {
            Log.e("NoSwipeToKill", "isNoRestrictApp failed", e);
            return false;
        }
    }
}
