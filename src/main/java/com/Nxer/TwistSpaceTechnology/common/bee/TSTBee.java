package com.Nxer.TwistSpaceTechnology.common.bee;

import gregtech.api.enums.GTValues;
import gregtech.api.enums.Materials;
import gregtech.common.items.CombType;
import gregtech.loaders.misc.GTBees;
import gtPlusPlus.api.objects.Logger;
import gtPlusPlus.core.item.base.ingots.BaseItemIngotOld;
import gtPlusPlus.core.item.base.misc.BaseItemMisc;
import gtPlusPlus.core.util.Utils;
import gtPlusPlus.core.util.minecraft.FluidUtils;
import gtPlusPlus.core.util.minecraft.ItemUtils;
import gtPlusPlus.xmod.forestry.bees.custom.GTPPBeeDefinition;
import gtPlusPlus.xmod.forestry.bees.custom.ItemCustomComb;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import static gregtech.api.enums.Mods.Forestry;
import static gregtech.api.recipe.RecipeMaps.fluidExtractionRecipes;
import static gregtech.api.util.GTRecipeBuilder.SECONDS;
import static gregtech.api.util.GTRecipeBuilder.TICKS;

public class TSTBee {
    public TSTBee() {
        if (Forestry.isModLoaded()) {
            try {
                TSTBeeDefinition.initBees();
            } catch (Throwable t) {
                Logger.BEES("Failed to load bees, probably due to an ancient forestry version");
                t.printStackTrace();
            }
        }
    }



}
