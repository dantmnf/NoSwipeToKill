package xyz.cirno.noswipetokill;

import android.content.pm.ApplicationInfo;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import de.robv.android.xposed.XposedHelpers;

public class ProcessRecordWrapper {
    private static Class<?> cls;
    private static MethodHandle MH_info;
    private static MethodHandle MH_userId;

    public static void initialize(ClassLoader classLoader) throws ReflectiveOperationException {
        cls = Class.forName("com.android.server.am.ProcessRecord", false, classLoader);
        var fldInfo = cls.getDeclaredField("info");
        fldInfo.setAccessible(true);
        var fldUserId = cls.getDeclaredField("userId");
        fldUserId.setAccessible(true);

        MH_info = MethodHandles.lookup().unreflectGetter(fldInfo).asType(MethodType.methodType(ApplicationInfo.class, Object.class));
        MH_userId = MethodHandles.lookup().unreflectGetter(fldUserId).asType(MethodType.methodType(int.class, Object.class));
    }

    public static Class<?> getWrappedClass() {
        return cls;
    }

    private final Object wrapped;
    public ProcessRecordWrapper(Object processRecord) {
        if (processRecord == null) {
            throw new NullPointerException();
        }
        if (!cls.isInstance(processRecord)) {
            throw new IllegalArgumentException();
        }
        wrapped = processRecord;
    }

    public ApplicationInfo getInfo() {
        try {
            return (ApplicationInfo) MH_info.invokeExact(wrapped);
        } catch (Throwable e) {
            return null;
        }
    }

    public int getUserId() {
        try {
            return (int) MH_userId.invokeExact(wrapped);
        } catch (Throwable e) {
            return -1;
        }
    }

}
