package com.wynprice.minify.platform;

import com.wynprice.minify.Constants;
import com.wynprice.minify.platform.services.IPlatformHelper;
import com.wynprice.minify.platform.services.MinifyNetwork;

import java.util.ServiceLoader;

public class Services {

    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);
    public static final MinifyNetwork NETWORK = load(MinifyNetwork.class);

    public static <T> T load(Class<T> clazz) {

        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        Constants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
