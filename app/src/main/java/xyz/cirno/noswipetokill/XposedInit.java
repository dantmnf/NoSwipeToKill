package xyz.cirno.noswipetokill;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedInit implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if ("android".equals(lpparam.packageName) && "android".equals(lpparam.processName)){
            new SystemServerHook().handleLoadSystemServer(lpparam);
        }
    }
}
