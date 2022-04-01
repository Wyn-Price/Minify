package com.wynprice.minify.network;

import com.wynprice.minify.platform.Services;
import com.wynprice.minify.platform.services.MinifyNetwork;

public class MinifyNetworkRegistry {
    public static void registerPackets() {
        Services.NETWORK.registerServerBound(C2SRequestViewerData.class, C2SRequestViewerData::encode, C2SRequestViewerData::decode, C2SRequestViewerData::handle);
        Services.NETWORK.registerServerBound(C2SRequestNestedData.class, C2SRequestNestedData::encode, C2SRequestNestedData::decode, C2SRequestNestedData::handle);

        Services.NETWORK.registerClientBound(S2CSendViewerData.class, S2CSendViewerData::encode, S2CSendViewerData::decode, S2CSendViewerData::handle);
        Services.NETWORK.registerClientBound(S2CUpdateViewerData.class, S2CUpdateViewerData::encode, S2CUpdateViewerData::decode, S2CUpdateViewerData::handle);
        Services.NETWORK.registerClientBound(S2CMinifiyBlockEvent.class, S2CMinifiyBlockEvent::encode, S2CMinifiyBlockEvent::decode, S2CMinifiyBlockEvent::handle);
        Services.NETWORK.registerClientBound(S2CUpdateViewerBlockEntityData.class, S2CUpdateViewerBlockEntityData::encode, S2CUpdateViewerBlockEntityData::decode, S2CUpdateViewerBlockEntityData::handle);
    }
}
