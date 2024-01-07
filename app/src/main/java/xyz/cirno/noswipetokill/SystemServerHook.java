package xyz.cirno.noswipetokill;

import android.os.IDeviceIdleController;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;

import java.util.Arrays;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SystemServerHook {
    private IDeviceIdleController mDeviceIdleController;
    private ProcessPolicyWrapper mProcessPolicy;
    private SmartPowerPolicyManagerWrapper mSmartPowerPolicyManager;

    private IDeviceIdleController getDeviceIdleController() {
        if (mDeviceIdleController == null) {
            mDeviceIdleController = IDeviceIdleController.Stub.asInterface(
                android.os.ServiceManager.getService("deviceidle"));
        }
        return mDeviceIdleController;
    }

    private ProcessPolicyWrapper getProcessPolicy() {
        if (mProcessPolicy == null) {
            var procmgr = ServiceManager.getService("ProcessManager");
            var policy = XposedHelpers.callMethod(procmgr, "getProcessPolicy");
            try {
                mProcessPolicy = new ProcessPolicyWrapper(policy);
            } catch (ReflectiveOperationException e) {
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
            } catch (ReflectiveOperationException e) {
                // ignore
            }
        }
        return mSmartPowerPolicyManager;
    }

    public void handleLoadSystemServer(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod("com.android.server.am.ProcessSceneCleaner", lpparam.classLoader, "sceneKillProcess", "miui.process.ProcessConfig", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (SystemProperties.getBoolean("persist.sys.miui.noswipetokill.ignore_all", false)) {
                    param.setResult(false);
                    return;
                }
                boolean isLocked = false, isNoRestrict = false;
                var config = (miui.process.ProcessConfig) param.args[0];
                var packageName = config.getKillingPackage();
                var userId = config.getUserId();
                var policy = getProcessPolicy();
                if (policy != null) {
                    isLocked = policy.isLockedApplication(packageName, userId);
                }
                var smartPowerPolicyManager = getSmartPowerPolicyManager();
                if (smartPowerPolicyManager != null) {
                    isNoRestrict = smartPowerPolicyManager.isNoRestrictApp(packageName);
                }
                Log.d("NoSwipeToKill", "sceneKillProcess called, killing package=" + packageName + " isLocked=" + isLocked + " isNoRestrict=" + isNoRestrict);
                if (isLocked || isNoRestrict) {
                    param.setResult(false);
                }
            }
        });
    }
}
