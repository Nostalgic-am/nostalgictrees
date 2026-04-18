package com.nostalgictrees.compat.jade;

import com.nostalgictrees.block.DryingRackBlock;
import com.nostalgictrees.block.ResourceSaplingBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class NTJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(SaplingDataProvider.INSTANCE, ResourceSaplingBlock.class);
        registration.registerBlockDataProvider(DryingRackDataProvider.INSTANCE, DryingRackBlock.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(SaplingComponentProvider.INSTANCE, ResourceSaplingBlock.class);
        registration.registerBlockComponent(DryingRackComponentProvider.INSTANCE, DryingRackBlock.class);
    }
}