package xyz.cirno.noswipetokill;

import android.util.Log;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class ProcessPolicyWrapper {
    private static Class<?> cls;
    private static MethodHandle MH_isLockedApplication;

    public static void initialize(ClassLoader classLoader) throws ReflectiveOperationException {
        cls = Class.forName("com.android.server.am.ProcessPolicy", false, classLoader);
        var method = cls.getDeclaredMethod("isLockedApplication", String.class, int.class);
        MH_isLockedApplication = MethodHandles.lookup().unreflect(method).asType(MethodType.methodType(boolean.class, Object.class, String.class, int.class));
    }
    private final Object wrapped;
    public ProcessPolicyWrapper(Object processPolicy) {
        if (processPolicy == null) {
            throw new NullPointerException();
        }
        if (!cls.isInstance(processPolicy)) {
            throw new IllegalArgumentException();
        }
        wrapped = processPolicy;
    }

    public boolean isLockedApplication(String packageName, int userId) {
        try {
            return (boolean) MH_isLockedApplication.invokeExact(wrapped, packageName, userId);
        } catch (Throwable e) {
            Log.e("NoSwipeToKill", "isLockedApplication failed", e);
            return false;
        }
    }
}
