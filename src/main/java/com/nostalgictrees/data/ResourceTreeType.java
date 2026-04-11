package com.nostalgictrees.data;

import net.minecraft.resources.ResourceLocation;
import java.util.Optional;

public record ResourceTreeType(
        String name,
        TreeTier tier,
        ResourceLocation outputItem,
        int outputCount,
        int color,
        Optional<String> requiredMod
) {
    public String saplingId() { return name + "_sapling"; }
    public String logId() { return name + "_log"; }
    public String strippedLogId() { return "stripped_" + name + "_log"; }
    public String leavesId() { return name + "_leaves"; }
    public String appleId() { return name + "_apple"; }
    public String chunkId() { return name + "_chunk"; }
    public String honeycombId() { return name + "_honeycomb"; }
}
