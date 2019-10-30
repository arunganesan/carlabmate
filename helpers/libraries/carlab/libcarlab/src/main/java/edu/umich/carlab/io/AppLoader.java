package edu.umich.carlab.io;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.loadable.App;
import edu.umich.carlab.loadable.Middleware;


public class AppLoader {
    final static String TAG = "AppLoader";

    private static AppLoader instance = null;
    private Set<Class<?>> loadedApps = new HashSet<>();
    private Map<String, Middleware> loadedMiddleware = new HashMap<>();

    private AppLoader() {
    }

    public static AppLoader getInstance() {
        if (instance == null) {
            instance = new AppLoader();
        }

        return instance;
    }

    public AppLoader loadApp(Class<?> cls) {
        if (loadedApps.contains(cls))
            loadedApps.remove(cls);
        loadedApps.add(cls);
        return this;
    }

    public AppLoader loadApps(Class<?>[] classes) {
        for (Class<?> cls : classes) {
            if (loadedApps.contains(cls))
                loadedApps.remove(cls);
            loadedApps.add(cls);
        }
        return this;
    }

    public List<App> instantiateApps(CLDataProvider clDataProvider, Context context) {
        List<App> instantiatedApps = new ArrayList<>();

        for (Class<?> app : loadedApps) {
            try {
                Constructor<?> constructor = app.getConstructor(CLDataProvider.class, Context.class);
                App appInstance = (App) constructor.newInstance(clDataProvider, context);
                instantiatedApps.add(appInstance);
            } catch (Exception e) {
                Log.e(TAG, "Error creating alive app: " + e + app.getCanonicalName());
            }
        }

        return instantiatedApps;
    }


}
