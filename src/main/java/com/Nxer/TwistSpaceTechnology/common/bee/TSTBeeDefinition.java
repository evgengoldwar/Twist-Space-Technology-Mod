package com.Nxer.TwistSpaceTechnology.common.bee;

import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeType;
import forestry.api.apiculture.IAlleleBeeEffect;
import forestry.api.apiculture.IAlleleBeeSpecies;
import forestry.api.apiculture.IBee;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeMutationCustom;
import forestry.api.core.EnumTemperature;
import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.IAllele;
import forestry.api.genetics.IAlleleFlowers;
import forestry.apiculture.genetics.Bee;
import forestry.apiculture.genetics.IBeeDefinition;
import forestry.core.genetics.alleles.AlleleHelper;
import forestry.core.genetics.alleles.EnumAllele;
import gregtech.api.util.GTLanguageManager;
import gregtech.api.util.GTModHandler;
import gregtech.common.bees.GTAlleleBeeSpecies;
import gregtech.common.bees.GTBeeMutation;
import gregtech.loaders.misc.GTBranchDefinition;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.text.WordUtils;

import java.awt.*;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.Consumer;

import static com.Nxer.TwistSpaceTechnology.common.bee.TSTBeeDefinitionReference.EXTRABEES;
import static com.Nxer.TwistSpaceTechnology.common.bee.TSTBeeDefinitionReference.FORESTRY;
import static com.Nxer.TwistSpaceTechnology.common.bee.TSTBeeDefinitionReference.GENDUSTRY;
import static com.Nxer.TwistSpaceTechnology.common.bee.TSTBeeDefinitionReference.GREGTECH;
import static com.Nxer.TwistSpaceTechnology.common.bee.TSTBeeDefinitionReference.MAGICBEES;
import static forestry.api.apiculture.EnumBeeChromosome.LIFESPAN;
import static forestry.api.apiculture.EnumBeeChromosome.SPECIES;
import static forestry.api.core.EnumHumidity.DAMP;
import static gregtech.api.enums.Mods.BiomesOPlenty;
import static gregtech.api.enums.Mods.Forestry;
import static gregtech.loaders.misc.GTBeeDefinition.NAQUADRIA;

class TSTBeeDefinitionReference {

    protected static final byte FORESTRY = 0;
    protected static final byte EXTRABEES = 1;
    protected static final byte GENDUSTRY = 2;
    protected static final byte MAGICBEES = 3;
    protected static final byte GREGTECH = 4;

    private TSTBeeDefinitionReference() {}
}

public enum TSTBeeDefinition implements IBeeDefinition {

    NOOB(GTBranchDefinition.ENDGAME, "Noob bee", false, new Color(0x4333A5), new Color(0x36ABFF),
        beeSpecies -> {
            beeSpecies.addProduct(GTModHandler.getModItem(Forestry.ID, "beeCombs", 1, 0), 0.30f);
            beeSpecies.addProduct(new ItemStack(Items.clay_ball, 1), 0.15f);
            beeSpecies.addSpecialty(GTModHandler.getModItem(BiomesOPlenty.ID, "mudball", 1, 0), 0.05f);
            beeSpecies.setHumidity(DAMP);
            beeSpecies.setTemperature(EnumTemperature.NORMAL);
        }, template -> AlleleHelper.instance.set(template, LIFESPAN, EnumAllele.Lifespan.SHORTEST),
        dis -> {
            IBeeMutationCustom tMutation = dis
                .registerMutation(getSpecies(FORESTRY, "Industrious"), getSpecies(FORESTRY, "Diligent"), 10);
            tMutation.requireResource(Blocks.clay, 0); // blockStainedHardenedClay
        }),

    ;

    private final GTBranchDefinition branch;
    private final GTAlleleBeeSpecies species;
    private final Consumer<GTAlleleBeeSpecies> mSpeciesProperties;
    private final Consumer<IAllele[]> mAlleles;
    private final Consumer<TSTBeeDefinition> mMutations;
    private IAllele[] template;
    private IBeeGenome genome;

    TSTBeeDefinition(GTBranchDefinition branch, String binomial, boolean dominant, Color primary, Color secondary,
                     Consumer<GTAlleleBeeSpecies> aSpeciesProperties, Consumer<IAllele[]> aAlleles,
                     Consumer<TSTBeeDefinition> aMutations) {
        this.mAlleles = aAlleles;
        this.mMutations = aMutations;
        this.mSpeciesProperties = aSpeciesProperties;
        String lowercaseName = this.toString()
            .toLowerCase(Locale.ENGLISH);
        String species = WordUtils.capitalize(lowercaseName);

        String uid = "gregtech.bee.species" + species;
        String description = "for.description." + lowercaseName;
        String name = "for.bees.species." + lowercaseName;
        GTLanguageManager.addStringLocalization("for.bees.species." + lowercaseName, species);

        String authority = GTLanguageManager.getTranslation("for.bees.authority." + lowercaseName);
        if (authority.equals("for.bees.authority." + lowercaseName)) {
            authority = "GTNH";
        }
        this.branch = branch;
        this.species = new GTAlleleBeeSpecies(
            uid,
            dominant,
            name,
            authority,
            description,
            branch.getBranch(),
            binomial,
            primary,
            secondary);
    }

    public static void initBees() {
        for (TSTBeeDefinition bee : values()) {
            bee.init();
        }
        for (TSTBeeDefinition bee : values()) {
            bee.registerMutations();
        }
    }

    static IAlleleBeeEffect getEffect(byte modid, String name) {
        String s = switch (modid) {
            case EXTRABEES -> "extrabees.effect." + name;
            case GENDUSTRY -> "gendustry.effect." + name;
            case MAGICBEES -> "magicbees.effect" + name;
            case GREGTECH -> "gregtech.effect" + name;
            default -> "forestry.effect" + name;
        };
        return (IAlleleBeeEffect) AlleleManager.alleleRegistry.getAllele(s);
    }

    static IAlleleFlowers getFlowers(byte modid, String name) {
        String s = switch (modid) {
            case EXTRABEES -> "extrabees.flower." + name;
            case GENDUSTRY -> "gendustry.flower." + name;
            case MAGICBEES -> "magicbees.flower" + name;
            case GREGTECH -> "gregtech.flower" + name;
            default -> "forestry.flowers" + name;
        };
        return (IAlleleFlowers) AlleleManager.alleleRegistry.getAllele(s);
    }

    private static IAlleleBeeSpecies getSpecies(byte modid, String name) {
        String s = switch (modid) {
            case EXTRABEES -> "extrabees.species." + name;
            case GENDUSTRY -> "gendustry.bee." + name;
            case MAGICBEES -> "magicbees.species" + name;
            case GREGTECH -> "gregtech.species" + name;
            default -> "forestry.species" + name;
        };
        IAlleleBeeSpecies ret = (IAlleleBeeSpecies) AlleleManager.alleleRegistry.getAllele(s);
        if (ret == null) {
            ret = NAQUADRIA.getSpecies();
        }

        return ret;
    }

    private void setSpeciesProperties(GTAlleleBeeSpecies beeSpecies) {
        this.mSpeciesProperties.accept(beeSpecies);
    }

    private void setAlleles(IAllele[] template) {
        this.mAlleles.accept(template);
    }

    private void registerMutations() {
        this.mMutations.accept(this);
    }

    private void init() {
        setSpeciesProperties(species);

        template = branch.getTemplate();
        AlleleHelper.instance.set(template, SPECIES, species);
        setAlleles(template);

        genome = BeeManager.beeRoot.templateAsGenome(template);

        BeeManager.beeRoot.registerTemplate(template);
    }

    private IBeeMutationCustom registerMutation(IAlleleBeeSpecies parent1, IAlleleBeeSpecies parent2, int chance) {
        return registerMutation(parent1, parent2, chance, 1f);
    }

    private IBeeMutationCustom registerMutation(TSTBeeDefinition parent1, IAlleleBeeSpecies parent2, int chance) {
        return registerMutation(parent1, parent2, chance, 1f);
    }

    private IBeeMutationCustom registerMutation(IAlleleBeeSpecies parent1, TSTBeeDefinition parent2, int chance) {
        return registerMutation(parent1, parent2, chance, 1f);
    }

    private IBeeMutationCustom registerMutation(TSTBeeDefinition parent1, TSTBeeDefinition parent2, int chance) {
        return registerMutation(parent1, parent2, chance, 1f);
    }

    /**
     * Diese neue Funtion erlaubt Mutationsraten unter 1%. Setze dazu die Mutationsrate als Bruch mit chance /
     * chanceDivider This new function allows Mutation percentages under 1%. Set them as a fraction with chance /
     * chanceDivider
     */
    private IBeeMutationCustom registerMutation(IAlleleBeeSpecies parent1, IAlleleBeeSpecies parent2, int chance,
                                                float chanceDivider) {
        return new GTBeeMutation(parent1, parent2, this.getTemplate(), chance, chanceDivider);
    }

    private IBeeMutationCustom registerMutation(TSTBeeDefinition parent1, IAlleleBeeSpecies parent2, int chance,
                                                float chanceDivider) {
        return registerMutation(parent1.species, parent2, chance, chanceDivider);
    }

    private IBeeMutationCustom registerMutation(IAlleleBeeSpecies parent1, TSTBeeDefinition parent2, int chance,
                                                float chanceDivider) {
        return registerMutation(parent1, parent2.species, chance, chanceDivider);
    }

    private IBeeMutationCustom registerMutation(TSTBeeDefinition parent1, TSTBeeDefinition parent2, int chance,
                                                float chanceDivider) {
        return registerMutation(parent1.species, parent2, chance, chanceDivider);
    }

    @Override
    public final IAllele[] getTemplate() {
        return Arrays.copyOf(template, template.length);
    }

    @Override
    public final IBeeGenome getGenome() {
        return genome;
    }

    @Override
    public final IBee getIndividual() {
        return new Bee(genome);
    }

    @Override
    public final ItemStack getMemberStack(EnumBeeType beeType) {
        return BeeManager.beeRoot.getMemberStack(getIndividual(), beeType.ordinal());
    }

    public GTAlleleBeeSpecies getSpecies() {
        return species;
    }
}
