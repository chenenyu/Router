package com.chenenyu.router;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

public class RouterService extends Service {
    public RouterService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mStub;
    }

    private IRouterInterface.Stub mStub = new IRouterInterface.Stub() {
        @Override
        public RouteResponse route(RouteRequest request) throws RemoteException {
            return MainRouter.getInstance().route(request);
        }
    };
}
