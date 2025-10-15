package com.Nxer.TwistSpaceTechnology.common.machine;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlocksTiered;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofChain;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.onElementPass;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.withChannel;
import static gregtech.api.GregTechAPI.sBlockCasings6;
import static gregtech.api.GregTechAPI.sBlockCasingsNH;
import static gregtech.api.GregTechAPI.sBlockTintedGlass;
import static gregtech.api.enums.HatchElement.Energy;
import static gregtech.api.enums.HatchElement.InputBus;
import static gregtech.api.enums.HatchElement.InputHatch;
import static gregtech.api.enums.HatchElement.OutputBus;
import static gregtech.api.enums.HatchElement.OutputHatch;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_ELECTRIC_BLAST_FURNACE;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_ELECTRIC_BLAST_FURNACE_ACTIVE;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_ELECTRIC_BLAST_FURNACE_ACTIVE_GLOW;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_ELECTRIC_BLAST_FURNACE_GLOW;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static gregtech.api.util.GTStructureUtility.ofFrame;
import static tectech.thing.casing.TTCasingsContainer.StabilisationFieldGenerators;
import static tectech.thing.casing.TTCasingsContainer.sBlockCasingsTT;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.Nxer.TwistSpaceTechnology.system.DysonSphereProgram.machines.TST_ArtificialStar;
import com.Nxer.TwistSpaceTechnology.util.enums.TierEU;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import com.Nxer.TwistSpaceTechnology.common.machine.multiMachineClasses.GTCM_MultiMachineBase;
import com.Nxer.TwistSpaceTechnology.system.DimensionSystem.DimensionSystem;
import com.Nxer.TwistSpaceTechnology.util.TextLocalization;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;

import gregtech.api.enums.Materials;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.VoidProtectionHelper;
import gregtech.common.blocks.BlockCasingsNH;
import org.apache.commons.lang3.tuple.Pair;

public class TST_GasCollector extends GTCM_MultiMachineBase<TST_GasCollector> {

    // region Class Constructor
    public TST_GasCollector(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public TST_GasCollector(String aName) {
        super(aName);
    }

    // endregion

    // region Processing Logic
    private final List<DimensionSystem> validDimension = new ArrayList<>() {

        {
            add(DimensionSystem.Overworld);
            add(DimensionSystem.Nether);
            add(DimensionSystem.TheEnd);
        }
    };
    private FluidStack generateGas = null;
    private int mCasing = 0;

    @Override
    public ProcessingLogic createProcessingLogic() {
        return new ProcessingLogic() {

            @Override
            @Nonnull
            public CheckRecipeResult process() {
                List<FluidStack> outputs = new ArrayList<>();

                if (generateGas == null || tierHermeticCasings < 0) return CheckRecipeResultRegistry.NO_RECIPE;

                generateGas.amount = tierHermeticCasings;
                outputs.add(generateGas.copy());

                this.outputFluids = outputs.toArray(new FluidStack[0]);

                VoidProtectionHelper voidProtection = new VoidProtectionHelper().setMachine(machine)
                    .setFluidOutputs(this.outputFluids)
                    .build();

                if (voidProtection.isFluidFull()) {
                    return CheckRecipeResultRegistry.FLUID_OUTPUT_FULL;
                }

                duration = 20;

                return CheckRecipeResultRegistry.SUCCESSFUL;
            }
        };
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        this.mCasing = 0;
        this.tierHermeticCasings = -1;

        if (aBaseMetaTileEntity != null) {
            int idWorld = aBaseMetaTileEntity.getWorld().provider.dimensionId;
            for (DimensionSystem dimension : validDimension) {
                if (idWorld == dimension.getId()) {
                    this.generateGas = dimension.getGenerateGas();
                    break;
                }
            }
        }

        if (!checkPiece(STRUCTURE_PIECE_MAIN, horizontalOffSet, verticalOffSet, depthOffSet)) return false;
        if (this.tierHermeticCasings == -1) return false;

        return (this.mCasing >= 10);
    }

    // endregion

    // region Structure
    // spotless:off
    @Override
    public void construct(ItemStack itemStack, boolean b) {
        buildPiece(STRUCTURE_PIECE_MAIN, itemStack, b, horizontalOffSet, verticalOffSet, depthOffSet);
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        return survivalBuildPiece(STRUCTURE_PIECE_MAIN, stackSize, horizontalOffSet, verticalOffSet, depthOffSet, elementBudget, env, false, true);
    }

    private static final String STRUCTURE_PIECE_MAIN = "main";
    private final int horizontalOffSet = 2;
    private final int verticalOffSet = 5;
    private final int depthOffSet = 0;
    private final int mainTextureID = ((BlockCasingsNH) sBlockCasingsNH).getTextureIndex(0);
    private static IStructureDefinition<TST_GasCollector> STRUCTURE_DEFINITION = null;
    private int tierHermeticCasings = -1;

    @Override
    public IStructureDefinition<TST_GasCollector> getStructureDefinition() {
        if (STRUCTURE_DEFINITION == null) {
            STRUCTURE_DEFINITION = StructureDefinition.<TST_GasCollector>builder()
                .addShape(STRUCTURE_PIECE_MAIN,
                    transpose(shapeMain))
                .addElement(
                    'B',
                    ofChain(
                        buildHatchAdder(TST_GasCollector.class)
                            .atLeast(InputHatch, OutputHatch, InputBus, OutputBus, Energy)
                            .casingIndex(mainTextureID)
                            .dot(1)
                            .build(),
                        onElementPass(x -> ++x.mCasing, ofBlock(sBlockCasingsNH, 0))))
                .addElement('C', ofFrame(Materials.Iron))
                .addElement('D', ofBlock(sBlockTintedGlass, 0))
                .addElement(
                    'A',
                    withChannel("hermetic_casing",
                        ofBlocksTiered(
                            TST_GasCollector::getBlockHermeticCasing,
                            ImmutableList.of(
                                Pair.of(sBlockCasings6, 0),
                                Pair.of(sBlockCasings6, 1),
                                Pair.of(sBlockCasings6, 2),
                                Pair.of(sBlockCasings6, 3),
                                Pair.of(sBlockCasings6, 4),
                                Pair.of(sBlockCasings6, 5),
                                Pair.of(sBlockCasings6, 6),
                                Pair.of(sBlockCasings6, 7),
                                Pair.of(sBlockCasings6, 8),
                                Pair.of(sBlockCasings6, 9),
                                Pair.of(sBlockCasings6, 10),
                                Pair.of(sBlockCasings6, 11),
                                Pair.of(sBlockCasings6, 12),
                                Pair.of(sBlockCasings6, 13),
                                Pair.of(sBlockCasings6, 14)),
                            -1,
                            (t, m) -> t.tierHermeticCasings = m,
                            t -> t.tierHermeticCasings)))
                .build();
        }
        return STRUCTURE_DEFINITION;
    }

	/*
	Blocks:
A -> ofBlock...(gt.blockcasings6, 10, ...);
B -> ofBlock...(gt.blockcasingsNH, 0, ...);
C -> ofBlock...(gt.blockframes, 32, ...);
D -> ofBlock...(gt.blocktintedglass, 0, ...);
	 */

    private final String[][] shapeMain = new String[][]{
        {"     ","     ","  C  ","     ","     "},
        {"     ","     ","  C  ","     ","     "},
        {"     "," BBB "," BAB "," BBB ","     "},
        {"     "," CDC "," DAD "," CDC ","     "},
        {"     "," CDC "," DAD "," CDC ","     "},
        {"BB~BB","BAAAB","BAAAB","BAAAB","CBBBB"},
        {"C   C","     ","     ","     ","C   C"},
        {"C   C","     ","     ","     ","C   C"}
    };

    public static int getBlockHermeticCasing(Block block, int meta) {
        if (block == sBlockCasings6 && (meta >= 0 && meta <= 14)) return meta + 1;
        return 0;
    }
    // spotless:on
    // endregion

    // region Overrides
    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        final MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType("Machine type")
            .addInfo("Other info...")
            .addInfo("Author")
            .addInputHatch(TextLocalization.textUseBlueprint, 1)
            .addDynamoHatch(TextLocalization.textUseBlueprint, 1)
            .toolTipFinisher(TextLocalization.ModName);
        return tt;
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new TST_GasCollector(this.mName);
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, ForgeDirection side, ForgeDirection facing,
        int colorIndex, boolean aActive, boolean aRedstone) {
        if (side == facing) {
            if (aActive) return new ITexture[] { Textures.BlockIcons.getCasingTextureForId(mainTextureID),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_ELECTRIC_BLAST_FURNACE_ACTIVE)
                    .extFacing()
                    .build(),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_ELECTRIC_BLAST_FURNACE_ACTIVE_GLOW)
                    .extFacing()
                    .glow()
                    .build() };
            return new ITexture[] { Textures.BlockIcons.getCasingTextureForId(mainTextureID), TextureFactory.builder()
                .addIcon(OVERLAY_FRONT_ELECTRIC_BLAST_FURNACE)
                .extFacing()
                .build(),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_ELECTRIC_BLAST_FURNACE_GLOW)
                    .extFacing()
                    .glow()
                    .build() };
        }
        return new ITexture[] { Textures.BlockIcons.getCasingTextureForId(mainTextureID) };
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setInteger("tierHermeticCasings", tierHermeticCasings);
    }

    @Override
    public void loadNBTData(final NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        tierHermeticCasings = aNBT.getInteger("tierHermeticCasings");
    }
    // endregion
}
