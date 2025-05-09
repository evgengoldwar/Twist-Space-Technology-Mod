package com.Nxer.TwistSpaceTechnology.recipe.machineRecipe.expanded;

import com.Nxer.TwistSpaceTechnology.common.material.MaterialPool;
import com.Nxer.TwistSpaceTechnology.common.recipeMap.GTCMRecipe;

import gregtech.api.enums.GTValues;
import gregtech.api.enums.Materials;
import gregtech.api.util.GTUtility;

public class MassFabricatorGenesisRecipePool {

    public static void loadRecipes() {
        GTValues.RA.stdBuilder()
            .itemInputs(GTUtility.getIntegratedCircuit(1))
            .fluidOutputs(Materials.UUMatter.getFluid(1000))
            .eut(200000)
            .duration(1000)
            .addTo(GTCMRecipe.MassFabricatorGenesis);

        GTValues.RA.stdBuilder()
            .itemInputs(GTUtility.getIntegratedCircuit(2))
            .fluidOutputs(MaterialPool.ConcentratedUUMatter.getFluidOrGas(1))
            .eut(2000000000)
            .duration(1000)
            .addTo(GTCMRecipe.MassFabricatorGenesis);
    }
}
