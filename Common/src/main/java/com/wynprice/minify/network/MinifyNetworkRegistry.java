package com.wynprice.minify.network;

import com.wynprice.minify.platform.Services;
import com.wynprice.minify.platform.services.MinifyNetwork;

public class MinifyNetworkRegistry {
    public static void registerPackets() {
        Services.NETWORK.registerServerBound(C2SRequestViewerData.class, C2SRequestViewerData::encode, C2SRequestViewerData::decode, C2SRequestViewerData::handle);

        Services.NETWORK.registerClientBound(S2CSendViewerData.class, S2CSendViewerData::encode, S2CSendViewerData::decode, S2CSendViewerData::handle);
        Services.NETWORK.registerClientBound(S2CUpdateViewerData.class, S2CUpdateViewerData::encode, S2CUpdateViewerData::decode, S2CUpdateViewerData::handle);
    }
}
