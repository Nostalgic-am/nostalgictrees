package com.nostalgictrees.compat.jade;

import com.nostalgictrees.block.AdvancedBeehiveBlock;
import com.nostalgictrees.block.DryingRackBlock;
import com.nostalgictrees.block.ResourceSaplingBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * 1.21.6+: Jade split its provider API. Server data collection
 * (IServerDataProvider) and client tooltip rendering (IBlockComponentProvider)
 * can no longer be implemented by the same class. Paired classes share a UID
 * so Jade's framework routes server data to the right client component.
 */
@WailaPlugin
public class NTJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(SaplingDataProvider.INSTANCE, ResourceSaplingBlock.class);
        registration.registerBlockDataProvider(DryingRackDataProvider.INSTANCE, DryingRackBlock.class);
        registration.registerBlockDataProvider(BeehiveDataProvider.INSTANCE, AdvancedBeehiveBlock.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(SaplingComponentProvider.INSTANCE, ResourceSaplingBlock.class);
        registration.registerBlockComponent(DryingRackComponentProvider.INSTANCE, DryingRackBlock.class);
        registration.registerBlockComponent(BeehiveComponentProvider.INSTANCE, AdvancedBeehiveBlock.class);
    }
}
