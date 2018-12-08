package android.app;
import android.app.IGotaKeyHandler;

interface IGotaKeyMonitor {

	IGotaKeyHandler setHandler (IGotaKeyHandler callback);
	void notifyPTTKeyDown();
	void notifyPTTKeyUp();
	void notifySOSKeyDown();
	void notifySOSKeyUp();
}