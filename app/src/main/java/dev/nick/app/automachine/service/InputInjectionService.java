package dev.nick.app.automachine.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.chrisplus.rootmanager.RootManager;

import dev.nick.app.automachine.IInputInjector;

public class InputInjectionService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new ServiceBinder();
    }

    class ServiceBinder extends IInputInjector.Stub {

        private static final String SPACE = " ";

        private boolean ensureRoot() {
            return RootManager.getInstance().obtainPermission();
        }

        @Override
        public void home() throws RemoteException {
            if (ensureRoot()) {
                RootManager.getInstance().runCommand("input keyevent 3");
            }
        }

        @Override
        public void back() throws RemoteException {
            if (ensureRoot()) {
                RootManager.getInstance().runCommand("input keyevent 4");
            }
        }

        @Override
        public void menu() throws RemoteException {
            if (ensureRoot()) {
                RootManager.getInstance().runCommand("input keyevent 4");
            }
        }

        @Override
        public void tap(int x, int y) throws RemoteException {
            if (ensureRoot()) {
                RootManager.getInstance().runCommand("input touchscreen tap" + SPACE + x + SPACE + y);
            }
        }

        @Override
        public void swipe(int x1, int y1, int x2, int y2, long duration) throws RemoteException {
            if (ensureRoot()) {
                RootManager.getInstance().runCommand("input touchscreen swpie"
                        + SPACE + x1 + SPACE + y1
                        + SPACE + x2 + SPACE + y2
                        + SPACE + duration);
            }
        }

        @Override
        public void text(String text) throws RemoteException {
            if (ensureRoot()) {
                RootManager.getInstance().runCommand("input text" + SPACE + text);
            }
        }
    }
}
