package gtPlusPlus.core.handler;

import static gregtech.api.enums.Mods.ExtraUtilities;
import static gregtech.api.enums.Mods.PamsHarvestCraft;
import static gregtech.api.enums.Mods.Witchery;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import net.minecraft.item.ItemStack;

import gregtech.api.enums.GTValues;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.util.GTOreDictUnificator;
import gtPlusPlus.api.interfaces.RunnableWithInfo;
import gtPlusPlus.api.objects.Logger;
import gtPlusPlus.core.common.compat.CompatExtraUtils;
import gtPlusPlus.core.common.compat.CompatHarvestCraft;
import gtPlusPlus.core.common.compat.CompatWitchery;
import gtPlusPlus.core.handler.Recipes.LateRegistrationHandler;
import gtPlusPlus.core.handler.Recipes.RegistrationHandler;
import gtPlusPlus.core.item.chemistry.IonParticles;
import gtPlusPlus.core.item.chemistry.RecipeLoaderAgriculturalChem;
import gtPlusPlus.core.item.chemistry.RecipeLoaderCoalTar;
import gtPlusPlus.core.item.chemistry.RecipeLoaderGenericChem;
import gtPlusPlus.core.item.chemistry.RecipeLoaderMilling;
import gtPlusPlus.core.item.chemistry.RecipeLoaderRocketFuels;
import gtPlusPlus.core.material.Material;
import gtPlusPlus.core.material.MaterialGenerator;
import gtPlusPlus.core.material.Particle;
import gtPlusPlus.core.recipe.RecipesGregTech;
import gtPlusPlus.core.recipe.RecipesLaserEngraver;
import gtPlusPlus.core.recipe.ShapedRecipeObject;
import gtPlusPlus.core.util.minecraft.ItemUtils;
import gtPlusPlus.core.util.minecraft.RecipeUtils;
import gtPlusPlus.xmod.gregtech.loaders.RecipeGenFluidCanning;
import gtPlusPlus.xmod.gregtech.loaders.RecipeGenRecycling;
import gtPlusPlus.xmod.gregtech.loaders.recipe.RecipeLoaderChemicalSkips;
import gtPlusPlus.xmod.gregtech.loaders.recipe.RecipeLoaderGTNH;
import gtPlusPlus.xmod.gregtech.loaders.recipe.RecipeLoaderGlueLine;
import gtPlusPlus.xmod.gregtech.loaders.recipe.RecipeLoaderIndustrialRockBreaker;
import gtPlusPlus.xmod.gregtech.loaders.recipe.RecipeLoaderNuclear;
import gtPlusPlus.xmod.gregtech.registration.gregtech.Gregtech4Content;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechAdvancedBoilers;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechAlgaeContent;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechAmazonWarehouse;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechBufferDynamos;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechCustomHatches;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechCyclotron;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechDehydrator;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechEnergyBuffer;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechFactoryGradeReplacementMultis;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechGeothermalThermalGenerator;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechHiAmpTransformer;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechIndustrialAlloySmelter;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechIndustrialArcFurnace;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechIndustrialBlastSmelter;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechIndustrialCentrifuge;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechIndustrialChisel;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechIndustrialCokeOven;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechIndustrialCuttingFactory;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechIndustrialElectrolyzer;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechIndustrialElementDuplicator;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechIndustrialExtruder;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechIndustrialFishPond;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechIndustrialFluidHeater;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechIndustrialForgeHammer;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechIndustrialFuelRefinery;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechIndustrialMacerator;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechIndustrialMassFabricator;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechIndustrialMixer;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechIndustrialPlatePress;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechIndustrialRockBreaker;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechIndustrialSifter;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechIndustrialThermalCentrifuge;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechIndustrialTreeFarm;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechIndustrialWashPlant;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechIndustrialWiremill;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechIsaMill;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechLFTR;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechLargeTurbinesAndHeatExchanger;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechMolecularTransformer;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechNuclearSaltProcessingPlant;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechPollutionDevices;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechPowerSubStation;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechQuantumForceTransformer;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechRTG;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechRedstoneButtonPanel;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechRedstoneCircuitBlock;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechRedstoneLamp;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechRedstoneStrengthDisplay;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechRedstoneStrengthScale;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechRocketFuelGenerator;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechSemiFluidgenerators;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechSimpleWasher;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechSolarTower;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechSteamMultis;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechThaumcraftDevices;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechThreadedBuffers;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechTieredFluidTanks;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechWaterPump;
import gtPlusPlus.xmod.gregtech.registration.gregtech.GregtechWirelessChargers;

public class CompatHandler {

    public static Queue<Object> RemoveRecipeQueue = new LinkedList<>();
    public static Queue<ShapedRecipeObject> AddRecipeQueue = new LinkedList<>();
    public static Boolean areInitItemsLoaded = false;

    public static void registerMyModsOreDictEntries() {

        Logger.INFO("Registering Materials with OreDict.");
        // In-house

        for (int i = 1; i <= 10; i++) {
            GTOreDictUnificator.registerOre(
                "bufferCore_" + GTValues.VN[i - 1],
                new ItemStack(ItemUtils.getItemFromFQRN("miscutils:item.itemBufferCore" + i)));
        }
        for (Particle i : Particle.aMap) {
            GTOreDictUnificator
                .registerOre(OrePrefixes.particle + i.mParticleName.replace(" ", ""), Particle.getBaseParticle(i));
        }

        for (String i : IonParticles.ions) {
            GTOreDictUnificator.registerOre(OrePrefixes.particle + i, Particle.getIon(i, 0));
        }
    }

    public static void registerGregtechMachines() {
        // Free IDs
        /*
         * --- 859 to 868 --- 911 to 940
         */

        new RecipesLaserEngraver();
        GregtechEnergyBuffer.run();
        GregtechLFTR.run();
        GregtechNuclearSaltProcessingPlant.run();
        GregtechIndustrialCentrifuge.run();
        GregtechIndustrialCokeOven.run();
        GregtechIndustrialPlatePress.run();
        GregtechRocketFuelGenerator.run();
        GregtechIndustrialElectrolyzer.run();
        GregtechIndustrialMacerator.run();
        GregtechIndustrialWiremill.run();
        GregtechIndustrialMassFabricator.run();
        GregtechWaterPump.run();
        GregtechIndustrialBlastSmelter.run();
        GregtechQuantumForceTransformer.run();
        GregtechPowerSubStation.run();
        GregtechDehydrator.run();
        GregtechAdvancedBoilers.run();
        GregtechPollutionDevices.run();
        GregtechTieredFluidTanks.run();
        GregtechGeothermalThermalGenerator.run();
        Gregtech4Content.run();
        GregtechIndustrialFuelRefinery.run();
        GregtechIndustrialTreeFarm.run();
        GregtechIndustrialSifter.run();
        GregtechSimpleWasher.run();
        GregtechRTG.run();
        GregtechCyclotron.run();
        GregtechHiAmpTransformer.run();
        GregtechIndustrialThermalCentrifuge.run();
        GregtechIndustrialWashPlant.run();
        GregtechSemiFluidgenerators.run();
        GregtechWirelessChargers.run();
        GregtechIndustrialCuttingFactory.run();
        GregtechIndustrialFishPond.run();
        GregtechIndustrialExtruder.run();
        GregtechBufferDynamos.run();
        GregtechAmazonWarehouse.run();
        GregtechFactoryGradeReplacementMultis.run();
        GregtechThaumcraftDevices.run();
        GregtechThreadedBuffers.run();
        GregtechIndustrialMixer.run();
        GregtechCustomHatches.run();
        GregtechIndustrialArcFurnace.run();
        GregtechSolarTower.run();
        GregtechLargeTurbinesAndHeatExchanger.run();
        GregtechAlgaeContent.run();
        GregtechIndustrialAlloySmelter.run();
        GregtechIsaMill.run();
        GregtechSteamMultis.run();
        GregtechIndustrialForgeHammer.run();
        GregtechMolecularTransformer.run();
        GregtechIndustrialElementDuplicator.run();
        GregtechIndustrialRockBreaker.run();
        GregtechIndustrialChisel.run();
        GregtechIndustrialFluidHeater.run();
        GregtechRedstoneButtonPanel.run();
        GregtechRedstoneCircuitBlock.run();
        GregtechRedstoneLamp.run();
        GregtechRedstoneStrengthDisplay.run();
        GregtechRedstoneStrengthScale.run();
    }

    // InterMod
    public static void intermodOreDictionarySupport() {
        if (ExtraUtilities.isModLoaded()) {
            CompatExtraUtils.OreDict();
        }
        if (PamsHarvestCraft.isModLoaded()) {
            CompatHarvestCraft.OreDict();
        }
        if (Witchery.isModLoaded()) {
            CompatWitchery.OreDict();
        }
    }

    public static void RemoveRecipesFromOtherMods() {
        // Removal of Recipes
        for (final Object item : RemoveRecipeQueue) {
            RecipeUtils.removeCraftingRecipe(item);
        }
    }

    public static void InitialiseHandlerThenAddRecipes() {
        RegistrationHandler.run();
    }

    public static void InitialiseLateHandlerThenAddRecipes() {
        LateRegistrationHandler.run();
    }

    public static void startLoadingGregAPIBasedRecipes() {
        // Add hand-made recipes
        RecipesGregTech.run();
        RecipeLoaderGTNH.generate();
        RecipeLoaderNuclear.generate();
        RecipeLoaderGlueLine.generate();
        RecipeLoaderChemicalSkips.generate();
        RecipeLoaderIndustrialRockBreaker.run();
        RecipeLoaderCoalTar.generate();
        RecipeLoaderGenericChem.generate();
        RecipeLoaderAgriculturalChem.generate();
        RecipeLoaderRocketFuels.generate();
        RecipeLoaderMilling.generate();
        // Add autogenerated Recipes from Item Components
        for (Set<RunnableWithInfo<Material>> m : MaterialGenerator.mRecipeMapsToGenerate) {
            for (RunnableWithInfo<Material> r : m) {
                r.run();
                Logger.INFO(
                    "[FIND] " + r.getInfoData()
                        .getLocalizedName() + " recipes generated.");
            }
        }
        RecipeGenRecycling.executeGenerators();

        // Do Fluid Canning Last, because they're not executed on demand, but rather queued.
        RecipeGenFluidCanning.init();
    }

    public static final ArrayList<RunnableWithInfo<String>> mRecipesToGenerate = new ArrayList<>();
    public static final ArrayList<RunnableWithInfo<String>> mGtRecipesToGenerate = new ArrayList<>();

    public static void runQueuedRecipes() {
        // Add autogenerated Recipes from Item Components
        for (RunnableWithInfo<String> m : mRecipesToGenerate) {
            m.run();
        }
        for (RunnableWithInfo<String> m : mGtRecipesToGenerate) {
            m.run();
        }
    }
}
