package com.nostalgictrees.compat.jade;

import com.nostalgictrees.block.DryingRackBlock;
import com.nostalgictrees.block.ResourceSaplingBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * 1.21.6+: Jade split its provider API. Server data collection (IServerDataProvider) and
 * client tooltip rendering (IBlockComponentProvider) can no longer be implemented by the
 * same class. Paired classes share a UID so Jade's framework routes server data to the
 * right client component.
 *
 * The advanced beehive intentionally has no provider here — vanilla Jade already has a
 * built-in handler for blocks extending BeehiveBlock that shows honey level, bee count,
 * and inventory. Adding our own would produce duplicate lines.
 */
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