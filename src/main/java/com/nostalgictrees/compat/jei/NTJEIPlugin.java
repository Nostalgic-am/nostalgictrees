package com.nostalgictrees.compat.jei;

import com.nostalgictrees.NTBlocks;
import com.nostalgictrees.NostalgicTrees;
import com.nostalgictrees.data.NTTreeRegistry;
import com.nostalgictrees.data.ResourceTreeType;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class NTJEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(NostalgicTrees.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new MalletRecipeCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<MalletRecipe> recipes = new ArrayList<>();

        for (ResourceTreeType tree : NTTreeRegistry.getAllTrees()) {
            Block log = NTBlocks.getLogBlock(tree.name());
            Block strippedLog = NTBlocks.getStrippedLogBlock(tree.name());

            if (log != null && strippedLog != null) {
                recipes.add(new MalletRecipe(
                        new ItemStack(log),
                        new ItemStack(strippedLog),
                        tree.name()
                ));
            }
        }

        registration.addRecipes(MalletRecipeCategory.RECIPE_TYPE, recipes);
    }
}
