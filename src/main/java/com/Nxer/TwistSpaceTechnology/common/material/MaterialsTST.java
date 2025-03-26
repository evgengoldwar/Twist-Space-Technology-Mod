package com.Nxer.TwistSpaceTechnology.common.material;

import static gregtech.api.enums.OrePrefixes.nanite;

import java.util.Arrays;

import goodgenerator.util.CharExchanger;
import gregtech.api.enums.Dyes;
import gregtech.api.enums.Element;
import gregtech.api.enums.Materials;
import gregtech.api.enums.SubTag;
import gregtech.api.enums.TCAspects;
import gregtech.api.enums.TextureSet;
import gregtech.api.enums.TierEU;
import gregtech.common.render.items.InfinityRenderer;

/**
 * Register new material here by Gregtech Material System
 */
public class MaterialsTST {

    static final int offsetID = 260;
    // ID form 2242 ~ 2298 is available

    public static Materials NeutroniumAlloy = new Materials(
        offsetID,
        TextureSet.SET_SHINY,
        32.0F,
        98304000,
        18,
        1 | 2 | 32 | 64 | 128,
        217,
        220,
        203,
        0,
        "NeutroniumAlloy",
        "Neutronium Alloy",
        -1,
        -1,
        12000,
        12500,
        true,
        false,
        2,
        1,
        1,
        Dyes._NULL,
        Arrays.asList(
            new TCAspects.TC_AspectStack(TCAspects.ORDO, 1),
            new TCAspects.TC_AspectStack(TCAspects.ALIENIS, 2),
            new TCAspects.TC_AspectStack(TCAspects.PERDITIO, 3),
            new TCAspects.TC_AspectStack(TCAspects.METALLUM, 5))).disableAutoGeneratedBlastFurnaceRecipes()
                .disableAutoGeneratedVacuumFreezerRecipe()
                .setProcessingMaterialTierEU(TierEU.RECIPE_UEV)
                .setHasCorrespondingPlasma(false);
    public static Materials AxonisAlloy = new Materials(
        offsetID + 1,
        TextureSet.SET_SHINY,
        96.0F,
        98304000,
        18,
        1 | 2 | 32 | 64 | 128,
        197,
        67,
        36,
        0,
        "AxonisAlloy",
        "Axonis Alloy",
        -1,
        -1,
        13000,
        13200,
        true,
        false,
        2,
        1,
        1,
        Dyes._NULL,
        Arrays.asList(
            new TCAspects.TC_AspectStack(TCAspects.SENSUS, 1),
            new TCAspects.TC_AspectStack(TCAspects.PRAECANTATIO, 2),
            new TCAspects.TC_AspectStack(TCAspects.METALLUM, 4))).disableAutoGeneratedBlastFurnaceRecipes()
                .disableAutoGeneratedVacuumFreezerRecipe()
                .setProcessingMaterialTierEU(TierEU.RECIPE_UIV)
                .setHasCorrespondingPlasma(false);

    public static Materials Axonium = new Materials(
        offsetID + 2,
        TextureSet.SET_SHINY,
        480.0F,
        1048576000,
        24,
        1 | 2 | 32 | 64 | 128,
        243,
        45,
        27,
        0,
        "Axonium",
        "Axonium",
        -1,
        -1,
        23000,
        26000,
        true,
        false,
        2,
        1,
        1,
        Dyes._NULL,
        Arrays.asList(
            new TCAspects.TC_AspectStack(TCAspects.SENSUS, 1),
            new TCAspects.TC_AspectStack(TCAspects.PRAECANTATIO, 2),
            new TCAspects.TC_AspectStack(TCAspects.VICTUS, 3),
            new TCAspects.TC_AspectStack(TCAspects.METALLUM, 5))).disableAutoGeneratedBlastFurnaceRecipes()
                .disableAutoGeneratedVacuumFreezerRecipe()
                .setProcessingMaterialTierEU(TierEU.RECIPE_UMV)
                .setHasCorrespondingPlasma(true);

    public static Materials Dubnium = new Materials(
        offsetID + 3,
        TextureSet.SET_METALLIC,
        1.0F,
        0,
        2,
        1 | 2 | 32,
        200,
        200,
        200,
        0,
        "Dubnium",
        "Dubnium",
        -1,
        -1,
        3231,
        3231,
        false,
        false,
        2,
        1,
        1,
        Dyes._NULL,
        Element.Db,
        Arrays.asList(
            new TCAspects.TC_AspectStack(TCAspects.RADIO, 5),
            new TCAspects.TC_AspectStack(TCAspects.METALLUM, 3))).disableAutoGeneratedBlastFurnaceRecipes()
                .disableAutoGeneratedVacuumFreezerRecipe()
                .setProcessingMaterialTierEU(TierEU.RECIPE_LV)
                .setHasCorrespondingPlasma(true);

    // register Extra Information
    static {
        nanite.mGeneratedItems.add(Materials.CosmicNeutronium);
        nanite.mGeneratedItems.add(MaterialsTST.Axonium);

        NeutroniumAlloy.mChemicalFormula = "Nt\u2087Du\u2082Fl҉?";
        AxonisAlloy.mChemicalFormula = "۞\u2085(Hy⚶)\u2083⸎\u2082(IcMa)TbSh" + CharExchanger.shifter(9191);
        Axonium.mChemicalFormula = "A☀";

        SubTag.METAL.addTo(NeutroniumAlloy, AxonisAlloy, Axonium);
    }

    public static void initClient() {
        Axonium.renderer = new InfinityRenderer();
    }

    public static void init() {
        // to load this class~
    }

}
