package xyz.cirno.noswipetokill;

import android.util.Log;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class ProcessPolicyWrapper {
    private final MethodHandle MH_isLockedApplication;

    public ProcessPolicyWrapper(Object processPolicy) throws ReflectiveOperationException {
        MH_isLockedApplication = MethodHandles.lookup().findVirtual(processPolicy.getClass(), "isLockedApplication", MethodType.methodType(boolean.class, String.class, int.class)).bindTo(processPolicy);
    }

    public boolean isLockedApplication(String packageName, int userId) {
        try {

            return (boolean) MH_isLockedApplication.invokeExact(packageName, userId);
        } catch (Throwable e) {
            Log.e("NoSwipeToKill", "isLockedApplication failed", e);
            return false;
        }
    }
}
