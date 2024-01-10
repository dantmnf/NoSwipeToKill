package xyz.cirno.noswipetokill;

import android.content.Context;
import android.os.IDeviceIdleController;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;

import java.util.Arrays;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SystemServerHook {
    private ProcessPolicyWrapper mProcessPolicy;
    private SmartPowerPolicyManagerWrapper mSmartPowerPolicyManager;

    private ProcessPolicyWrapper getProcessPolicy() {
        if (mProcessPolicy == null) {
            var procmgr = ServiceManager.getService("ProcessManager");
            var policy = XposedHelpers.callMethod(procmgr, "getProcessPolicy");
            try {
                mProcessPolicy = new ProcessPolicyWrapper(policy);
            } catch (Exception e) {
                // ignore
            }
        }
        return mProcessPolicy;
    }

    private SmartPowerPolicyManagerWrapper getSmartPowerPolicyManager() {
        if (mSmartPowerPolicyManager == null) {
            var procmgr = ServiceManager.getService("smartpower");
            var policy = XposedHelpers.getObjectField(procmgr, "mSmartPowerPolicyManager");
            if (policy == null) {
                return null;
            }
            try {
                mSmartPowerPolicyManager = new SmartPowerPolicyManagerWrapper(policy);
            } catch (Exception e) {
                // ignore
            }
        }
        return mSmartPowerPolicyManager;
    }

    public void handleLoadSystemServer(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            SmartPowerPolicyManagerWrapper.initialize(lpparam.classLoader);
            ProcessPolicyWrapper.initialize(lpparam.classLoader);
            ProcessRecordWrapper.initialize(lpparam.classLoader);
        } catch (ReflectiveOperationException e) {
            Log.e("NoSwipeToKill", "initialize failed", e);
            return;
        }

        var cleanerBase = XposedHelpers.findClass("com.android.server.am.ProcessCleanerBase", lpparam.classLoader);

        for (var method : cleanerBase.getDeclaredMethods()) {
            if (!method.getName().equals("killOnce")) {
                continue;
            }
            var parameterTypes = method.getParameterTypes();
            if (parameterTypes.length >= 1 && parameterTypes[0].equals(ProcessRecordWrapper.getWrappedClass())) {
                XposedBridge.hookMethod(method, new HookKillOnce());
                XposedBridge.log("NoSwipeToKill: hooked " + method);
            }
        }
    }

    private class HookKillOnce extends XC_MethodHook {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            super.beforeHookedMethod(param);
            var processRecord = new ProcessRecordWrapper(param.args[0]);
            var info = processRecord.getInfo();
            var packageName = info.packageName;
            var userId = processRecord.getUserId();

            boolean isLocked = false, isNoRestrict = false;
            var policy = getProcessPolicy();
            if (policy != null) {
                isLocked = policy.isLockedApplication(packageName, userId);
            }
            var smartPowerPolicyManager = getSmartPowerPolicyManager();
            if (smartPowerPolicyManager != null) {
                isNoRestrict = smartPowerPolicyManager.isNoRestrictApp(packageName);
            }
            Log.d("NoSwipeToKill", String.format("killOnce, package=%s userId=%d isLocked=%b isNoRestrict=%b", packageName, userId, isLocked, isNoRestrict));
            if (isLocked || isNoRestrict) {
                param.setResult(null);
            }
        }
    }
}
