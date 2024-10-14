package com.Nxer.TwistSpaceTechnology.common.machine;

import static bartworks.util.BWUtil.ofGlassTiered;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlocksTiered;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.onElementPass;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.withChannel;
import static gregtech.api.enums.HatchElement.Energy;
import static gregtech.api.enums.HatchElement.ExoticEnergy;
import static gregtech.api.enums.HatchElement.InputBus;
import static gregtech.api.enums.HatchElement.InputHatch;
import static gregtech.api.enums.HatchElement.OutputBus;
import static gregtech.api.enums.HatchElement.OutputHatch;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_PROCESSING_ARRAY;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_PROCESSING_ARRAY_ACTIVE;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_PROCESSING_ARRAY_ACTIVE_GLOW;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_PROCESSING_ARRAY_GLOW;
import static gregtech.api.util.GTStructureUtility.ofCoil;
import static gregtech.api.util.GTStructureUtility.ofFrame;
import static gregtech.api.util.GTUtility.validMTEList;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import com.Nxer.TwistSpaceTechnology.common.machine.multiMachineClasses.GTCM_MultiMachineBase;
import com.Nxer.TwistSpaceTechnology.common.machine.multiMachineClasses.processingLogics.GTCM_ProcessingLogic;
import com.Nxer.TwistSpaceTechnology.util.TextLocalization;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import goodgenerator.loader.Loaders;
import gregtech.api.enums.HeatingCoilLevel;
import gregtech.api.enums.Materials;
import gregtech.api.enums.MaterialsUEVplus;
import gregtech.api.enums.SoundResource;
import gregtech.api.enums.TAE;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.api.metatileentity.implementations.MTEHatchEnergy;
import gregtech.api.metatileentity.implementations.MTEHatchInput;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.HatchElementBuilder;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.OverclockCalculator;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import gtPlusPlus.core.block.ModBlocks;
import gtPlusPlus.core.util.minecraft.FluidUtils;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

public class TST_MegaVolcanus extends GTCM_MultiMachineBase<TST_MegaVolcanus> {

    // region Class Constructor
    public TST_MegaVolcanus(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public TST_MegaVolcanus(String aName) {
        super(aName);
    }

    // endregion

    // region Processing Logic
    public byte glassTier;
    private static final int BASE_CASING_COUNT = 362;
    private static final int MAX_HATCHES_ALLOWED = 16;
    private byte fluidInput = 1;
    private double EuDiscount = 0.9;
    @Nullable
    public HeatingCoilLevel coilLevel;
    private int tierComponentCasing = -2;
    private int mCasingAmount;
    private int consumptionFluidInput;

    public HeatingCoilLevel getCoilLevel() {
        return coilLevel;
    }

    public void setCoilLevel(HeatingCoilLevel coilLevel) {
        this.coilLevel = coilLevel;
    }

    private byte getGlassTier() {
        return glassTier;
    }

    private void setGlassTier(byte tier) {
        glassTier = tier;
    }

    private FluidStack getFluidInput() {
        int fluidConsumption = 10_000;
        switch (fluidInput) {
            case 2 -> {
                consumptionFluidInput = 1_000;
                return Materials.Helium.getPlasma(fluidConsumption / 10);
            }
            case 3 -> {
                consumptionFluidInput = 100;
                return MaterialsUEVplus.SpaceTime.getMolten(fluidConsumption / 1_000);
            }
            default -> {
                consumptionFluidInput = 10_000;
                return FluidUtils.getFluidStack("pyrotheum", fluidConsumption);
            }
        }
    }

    private double getEuDiscount() {
        switch (fluidInput) {
            case 2 -> {
                return EuDiscount = 0.75;
            }
            case 3 -> {
                return EuDiscount = 0.6;
            }
            default -> {
                return EuDiscount = 0.9;
            }
        }
    }

    private void onCasingAdded() {
        mCasingAmount++;
    }

    @Override
    protected ProcessingLogic createProcessingLogic() {
        return new GTCM_ProcessingLogic() {

            @NotNull
            @Override
            protected CheckRecipeResult validateRecipe(@NotNull GTRecipe recipe) {
                return recipe.mSpecialValue <= getCoilLevel().getHeat() ? CheckRecipeResultRegistry.SUCCESSFUL
                    : CheckRecipeResultRegistry.insufficientHeat(recipe.mSpecialValue);
            }

            @NotNull
            @Override
            protected OverclockCalculator createOverclockCalculator(@NotNull GTRecipe recipe) {
                return super.createOverclockCalculator(recipe).setHeatOC(true)
                    .setHeatDiscount(true)
                    .setRecipeHeat(recipe.mSpecialValue)
                    .setMachineHeat((int) getCoilLevel().getHeat());
            }

            @NotNull
            @Override
            public CheckRecipeResult process() {
                speedBoost = getSpeedBonus();
                euModifier = getEuDiscount();
                return super.process();
            }

        }.setMaxParallelSupplier(this::getMaxParallelRecipes);
    }

    @Override
    protected boolean isEnablePerfectOverclock() {
        return false;
    }

    @Override
    protected float getSpeedBonus() {
        return 1f / (2 + (float) tierComponentCasing * 5 / 100);
    }

    @Override
    protected int getMaxParallelRecipes() {
        return 2048;
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return RecipeMaps.blastFurnaceRecipes;
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        repairMachine();
        if (!checkPiece(STRUCTURE_PIECE_MAIN, horizontalOffSet, verticalOffSet, depthOffSet)) {
            return false;
        }

        if (mCasingAmount < (BASE_CASING_COUNT - MAX_HATCHES_ALLOWED)) {
            return false;
        }

        if (this.glassTier < 8) {
            for (MTEHatch hatch : this.mExoticEnergyHatches) {
                if (hatch.getConnectionType() == MTEHatch.ConnectionType.LASER) {
                    return false;
                }
                if (this.glassTier < hatch.mTier) {
                    return false;
                }
            }
            for (MTEHatchEnergy mEnergyHatch : this.mEnergyHatches) {
                if (this.glassTier < mEnergyHatch.mTier) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void clearHatches() {
        super.clearHatches();

        mCasingAmount = 0;
        tierComponentCasing = -2;
        glassTier = 0;
        setCoilLevel(HeatingCoilLevel.None);
    }

    // endregion

    // region Structure
    // spotless:off
    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        this.buildPiece(STRUCTURE_PIECE_MAIN, stackSize, hintsOnly, horizontalOffSet, verticalOffSet, depthOffSet);
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        if (this.mMachine) return -1;
        return this.survivialBuildPiece(
            STRUCTURE_PIECE_MAIN,
            stackSize,
            horizontalOffSet,
            verticalOffSet,
            depthOffSet,
            elementBudget,
            env,
            false,
            true);
    }

    private static final String STRUCTURE_PIECE_MAIN = "mainMegaVolcanus";
    private final int horizontalOffSet = 12;
    private final int verticalOffSet = 6;
    private final int depthOffSet = 3;
    private static IStructureDefinition<TST_MegaVolcanus> STRUCTURE_DEFINITION = null;

    @Override
    public IStructureDefinition<TST_MegaVolcanus> getStructureDefinition() {
        if (STRUCTURE_DEFINITION == null) {
            STRUCTURE_DEFINITION = StructureDefinition.<TST_MegaVolcanus>builder()
                .addShape(STRUCTURE_PIECE_MAIN, transpose(shapeMain))
                .addElement(
                    'E',
                    HatchElementBuilder.<TST_MegaVolcanus>builder()
                        .atLeast(InputBus, InputHatch, OutputBus, OutputHatch, Energy.or(ExoticEnergy))
                        .adder(TST_MegaVolcanus::addToMachineList)
                        .dot(1)
                        .casingIndex(TAE.getIndexFromPage(2, 11))
                        .buildAndChain(
                            onElementPass(TST_MegaVolcanus::onCasingAdded,
                                ofBlock(ModBlocks.blockCasings3Misc, 11))))
                .addElement(
                    'A',
                    withChannel(
                        "glass",
                        ofGlassTiered(
                            (byte) 1, (byte) 127, (byte) 0,
                            TST_MegaVolcanus::setGlassTier,
                            TST_MegaVolcanus::getGlassTier,
                            2))
                )
                .addElement(
                    'C',
                    withChannel("coil", ofCoil(TST_MegaVolcanus::setCoilLevel, TST_MegaVolcanus::getCoilLevel)))
                .addElement('D', ofFrame(Materials.Americium))
                .addElement(
                    'B',
                    withChannel(
                        "component",
                        ofBlocksTiered(
                            (block, meta) -> block == Loaders.componentAssemblylineCasing ? meta : -1,
                            IntStream.range(0, 14)
                                .mapToObj(i -> Pair.of(Loaders.componentAssemblylineCasing, i))
                                .collect(Collectors.toList()),
                            -2,
                            (t, meta) -> t.tierComponentCasing = meta,
                            t -> t.tierComponentCasing)))
                .build();
        }
        return STRUCTURE_DEFINITION;
    }

    /*
     * Blocks:
     * A -> ofBlock...(BW_GlasBlocks2, 0, ...); Glass
     * B -> ofBlock...(componentAssemblyLineCasing, 12, ...); TiersBlock
     * C -> ofBlock...(gt.blockcasings5, 13, ...); Coil
     * D -> ofBlock...(gt.blockframes, 103, ...); Americium Frame
     * E -> ofBlock...(gtplusplus.blockcasings.3, 11, ...); Hatch
     */

    private final String[][] shapeMain = new String[][]{
        {"                         ","                         ","                         ","                         ","                         ","          AAAAA          ","         AEEEEEA         ","        AEEEEEEEA        ","        AEEDBDEEA        ","        AEEBCBEEA        ","        AEEDBDEEA        ","        AEEEEEEEA        ","         AEEEEEA         ","          AAAAA          ","                         ","                         ","                         ","                         ","                         ","                         ","                         ","                         "},
        {"                         ","                         ","                         ","                         ","                         ","          AAAAA          ","         A     A         ","        A       A        ","        A  DBD  A        ","        A  BCB  A        ","        A  DBD  A        ","        A       A        ","         A     A         ","          AAAAA          ","                         ","                         ","                         ","                         ","                         ","                         ","                         ","                         "},
        {"                         ","                         ","                         ","                         ","          AAAAA          ","         A     A         ","        A       A        ","       A         A       "," E     A   DBD   A     E ","EEE    A   BCB   A    EEE"," E     A   DBD   A     E ","       A         A       ","        A       A        ","         A     A         ","          AAAAA          ","                         ","                         ","                         ","                         ","            E            ","           EEE           ","            E            "},
        {"                         ","                         ","                         ","                         ","          AAAAA          ","         A     A         ","        A       A        ","       A         A       "," A     A   DBD   A     A ","ACA    A   BCB   A    ACA"," A     A   DBD   A     A ","       A         A       ","        A       A        ","         A     A         ","          AAAAA          ","                         ","                         ","                         ","                         ","            A            ","           ACA           ","            A            "},
        {"    E               E    ","   EEE             EEE   ","    E               E    ","          EEEEE          ","         ECCCCCE         ","        EC     CE        ","       EC       CE       ","      EC         CE      "," A    EC   DBD   CE    A ","ACA   EC   BCB   CE   ACA"," A    EC   DBD   CE    A ","      EC         CE      ","       EC       CE       ","        EC     CE        ","         ECCCCCE         ","          EEEEE          ","    E               E    ","   EEE             EEE   ","    E               E    ","            A            ","           ACA           ","            A            "},
        {"    A               A    ","   ACA             ACA   ","    A               A    ","          EEEEE          ","         ECCCCCE         ","        EC     CE        ","       EC       CE       ","      EC         CE      "," A    EC   DBD   CE    A ","ACA   EC   BCB   CE   ACA"," A    EC   DBD   CE    A ","      EC         CE      ","       EC       CE       ","        EC     CE        ","         ECCCCCE         ","          EEEEE          ","    A               A    ","   ACA             ACA   ","    A               A    ","            A            ","           ACA           ","            A            "},
        {"    A               A    ","   ACA             ACA   ","    A               A    ","          EE~EE          ","         ECCCCCE         ","        EC     CE        ","       EC       CE       ","      EC         CE      "," A    EC   DBD   CE    A ","ACA   EC   BCB   CE   ACA"," A    EC   DBD   CE    A ","      EC         CE      ","       EC       CE       ","        EC     CE        ","         ECCCCCE         ","          EEEEE          ","    A               A    ","   ACA             ACA   ","    A               A    ","            A            ","           ACA           ","            A            "},
        {"    A               A    ","   ACA             ACA   ","    A               A    ","          EEEEE          ","         ECCCCCE         ","        EC     CE        ","       EC       CE       ","      EC         CE      "," A    EC   DBD   CE    A ","ACA   EC   BCB   CE   AC "," A    EC   DBD   CE    A ","      EC         CE      ","       EC       CE       ","        EC     CE        ","         ECCCCCE         ","          EEEEE          ","    A               A    ","   ACA             ACA   ","    A               A    ","            A            ","           ACA           ","            A            "},
        {"    E               E    ","   EEE             EEE   ","    E               E    ","          EEEEE          ","         EEEEEEE         ","        EEEEEEEEE        ","       EEEEEEEEEEE       ","      EEEEEEEEEEEEE      "," E    EEEEEEEEEEEEE    E ","EEE   EEEEEEEEEEEEE   EEE"," E    EEEEEEEEEEEEE    E ","      EEEEEEEEEEEEE      ","       EEEEEEEEEEE       ","        EEEEEEEEE        ","         EEEEEEE         ","          EEEEE          ","    E               E    ","   EEE             EEE   ","    E               E    ","            E            ","           EEE           ","            E            "}
    };

    @Override
    public boolean addToMachineList(IGregTechTileEntity aTileEntity, int aBaseCasingIndex) {
        return super.addToMachineList(aTileEntity, aBaseCasingIndex)
            || addExoticEnergyInputToMachineList(aTileEntity, aBaseCasingIndex);
    }


    // spotless:on
    // endregion

    // region Overrides

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        final MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType(TextLocalization.Tooltip_MegaVolcanus_MachineType)
            .addInfo(TextLocalization.Tooltip_MegaVolcanus_00)
            .addInfo(TextLocalization.Tooltip_MegaVolcanus_01)
            .addInfo(TextLocalization.Tooltip_MegaVolcanus_02)
            .addInfo(TextLocalization.Tooltip_MegaVolcanus_03)
            .addInfo(TextLocalization.textScrewdriverChangeLiquid)
            .addSeparator()
            .addInfo(TextLocalization.StructureTooComplex)
            .addInfo(TextLocalization.BLUE_PRINT_INFO)
            .addInputHatch(TextLocalization.textUseBlueprint, 1)
            .addOutputHatch(TextLocalization.textUseBlueprint, 1)
            .addInputBus(TextLocalization.textUseBlueprint, 1)
            .addOutputBus(TextLocalization.textUseBlueprint, 1)
            .addEnergyHatch(TextLocalization.textUseBlueprint, 1)
            .toolTipFinisher(TextLocalization.ModName);
        return tt;
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new TST_MegaVolcanus(this.mName);
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, ForgeDirection side, ForgeDirection facing,
        int colorIndex, boolean aActive, boolean aRedstone) {
        if (side == facing) {
            if (aActive) return new ITexture[] { Textures.BlockIcons.getCasingTextureForId(TAE.getIndexFromPage(2, 11)),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_PROCESSING_ARRAY_ACTIVE)
                    .extFacing()
                    .build(),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_PROCESSING_ARRAY_ACTIVE_GLOW)
                    .extFacing()
                    .glow()
                    .build() };
            return new ITexture[] { Textures.BlockIcons.getCasingTextureForId(TAE.getIndexFromPage(2, 11)),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_PROCESSING_ARRAY)
                    .extFacing()
                    .build(),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_PROCESSING_ARRAY_GLOW)
                    .extFacing()
                    .glow()
                    .build() };
        }
        return new ITexture[] { Textures.BlockIcons.getCasingTextureForId(TAE.getIndexFromPage(2, 11)) };
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setInteger("tierComponentCasing", tierComponentCasing);
        aNBT.setByte("fluidInput", fluidInput);
    }

    @Override
    public void loadNBTData(final NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        tierComponentCasing = aNBT.getInteger("tierComponentCasing");
        fluidInput = aNBT.getByte("fluidInput");
        if (!aNBT.hasKey(INPUT_SEPARATION_NBT_KEY)) {
            inputSeparation = aNBT.getBoolean("mSeparate");
        }
    }

    @Override
    public void getWailaBody(ItemStack itemStack, List<String> currentTip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currentTip, accessor, config);
        final NBTTagCompound tag = accessor.getNBTData();
        currentTip.add(
            EnumChatFormatting.BLUE + "Parallels"
                + EnumChatFormatting.RESET
                + ": "
                + EnumChatFormatting.RED
                + tag.getInteger("maxParallel")
                + EnumChatFormatting.RESET
                + " / "
                + EnumChatFormatting.AQUA
                + tag.getInteger("curParallel"));
        currentTip.add(
            EnumChatFormatting.GOLD + "Current speed"
                + EnumChatFormatting.RESET
                + ": "
                + EnumChatFormatting.AQUA
                + tag.getFloat("CurrentSpeed")
                + EnumChatFormatting.RESET
                + " %");
        currentTip.add(
            EnumChatFormatting.GOLD + "Current fluid input"
                + EnumChatFormatting.RESET
                + ": "
                + EnumChatFormatting.AQUA
                + tag.getString("fluidInput")
                + EnumChatFormatting.RESET);
        currentTip.add(
            EnumChatFormatting.GOLD + "Fluid consumption"
                + EnumChatFormatting.RESET
                + ": "
                + EnumChatFormatting.AQUA
                + tag.getInteger("fluidConsumption")
                + EnumChatFormatting.RESET
                + " mb");
        currentTip.add(
            EnumChatFormatting.GOLD + "Current Eu usage"
                + EnumChatFormatting.RESET
                + ": "
                + EnumChatFormatting.AQUA
                + tag.getDouble("euDiscount")
                + EnumChatFormatting.RESET
                + " %");

    }

    @Override
    public void getWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y,
        int z) {
        super.getWailaNBTData(player, tile, tag, world, x, y, z);
        final IGregTechTileEntity tileEntity = getBaseMetaTileEntity();
        if (tileEntity != null) {
            tag.setFloat("CurrentSpeed", 100 + (tierComponentCasing * 5));
            switch (fluidInput) {
                case 2 -> {
                    tag.setInteger("fluidConsumption", 1_000);
                    tag.setString("fluidInput", "Helium Plasma");
                }
                case 3 -> {
                    tag.setInteger("fluidConsumption", 10);
                    tag.setString("fluidInput", "SpaceTime");
                }
                default -> {
                    tag.setInteger("fluidConsumption", 10_000);
                    tag.setString("fluidInput", "Pyrotheum");
                }
            }
            tag.setDouble("euDiscount", getEuDiscount() * 100);
            tag.setInteger("maxParallel", getMaxParallelRecipes());
            tag.setInteger("curParallel", processingLogic.getCurrentParallels());

        }
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        super.onPostTick(aBaseMetaTileEntity, aTick);
        if (this.mStartUpCheck < 0) {
            if (this.mMaxProgresstime > 0 && this.mProgresstime != 0 || this.getBaseMetaTileEntity()
                .hasWorkJustBeenEnabled()) {
                if (aTick % 20 == 0 || this.getBaseMetaTileEntity()
                    .hasWorkJustBeenEnabled()) {
                    if (!this.depleteInputFromRestrictedHatches(this.mInputHatches, consumptionFluidInput)) {
                        this.stopMachine(ShutDownReasonRegistry.outOfFluid(Objects.requireNonNull(getFluidInput())));
                    }
                }
            }
        }
    }

    protected boolean depleteInputFromRestrictedHatches(Collection<MTEHatchInput> aHatches, int aAmount) {
        for (final MTEHatchInput tHatch : validMTEList(aHatches)) {
            FluidStack tLiquid = tHatch.getFluid();
            if (tLiquid == null || tLiquid.amount < aAmount) {
                continue;
            }
            if (!tLiquid.isFluidEqual(getFluidInput())) return false;
            tLiquid = tHatch.drain(aAmount, false);
            if (tLiquid != null && tLiquid.amount >= aAmount) {
                tLiquid = tHatch.drain(aAmount, true);
                return tLiquid != null && tLiquid.amount >= aAmount;
            }
        }
        return false;
    }

    @Override
    public final void onScrewdriverRightClick(ForgeDirection side, EntityPlayer aPlayer, float aX, float aY, float aZ) {
        if (getBaseMetaTileEntity().isServerSide()) {
            fluidInput++;
            if (fluidInput > 3) fluidInput = 1;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected ResourceLocation getActivitySoundLoop() {
        return SoundResource.GT_MACHINES_MEGA_BLAST_FURNACE_LOOP.resourceLocation;
    }

    // endregion
}
