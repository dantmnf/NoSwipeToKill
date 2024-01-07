package android.os;

public interface IDeviceIdleController extends IInterface {
    public static abstract class Stub extends Binder implements IDeviceIdleController {
        public static IDeviceIdleController asInterface(IBinder obj) {
            throw new UnsupportedOperationException();
        }
    }
    boolean isPowerSaveWhitelistApp(String name);
    String[] getFullPowerWhitelist();
}
