/*
 * Copyright (c) 2018-2020 bartimaeusnek Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions: The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package bartworks.common.loaders;

import static bartworks.API.recipe.BartWorksRecipeMaps.bacterialVatRecipes;
import static bartworks.API.recipe.BartWorksRecipeMaps.bioLabRecipes;
import static bartworks.common.loaders.FluidLoader.BioLabFluidMaterials;
import static bartworks.common.loaders.ItemRegistry.BIOITEMS;
import static gregtech.api.enums.GTValues.RA;
import static gregtech.api.enums.Materials.NaquadahEnriched;
import static gregtech.api.enums.Materials.Plutonium;
import static gregtech.api.enums.Materials.Uranium;
import static gregtech.api.enums.Mods.PamsHarvestCraft;
import static gregtech.api.recipe.RecipeMaps.centrifugeRecipes;
import static gregtech.api.recipe.RecipeMaps.chemicalReactorRecipes;
import static gregtech.api.recipe.RecipeMaps.fluidExtractionRecipes;
import static gregtech.api.recipe.RecipeMaps.mixerRecipes;
import static gregtech.api.recipe.RecipeMaps.multiblockChemicalReactorRecipes;
import static gregtech.api.util.GTModHandler.getModItem;
import static gregtech.api.util.GTRecipeBuilder.MINUTES;
import static gregtech.api.util.GTRecipeBuilder.SECONDS;
import static gregtech.api.util.GTRecipeBuilder.TICKS;
import static gregtech.api.util.GTRecipeConstants.GLASS;
import static gregtech.api.util.GTRecipeConstants.SIEVERT;
import static gregtech.api.util.GTRecipeConstants.UniversalChemical;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import bartworks.util.BWUtil;
import bartworks.util.BioCulture;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.TierEU;
import gregtech.api.util.GTModHandler;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTUtility;
import gregtech.api.util.recipe.Sievert;

@SuppressWarnings({ "PointlessArithmeticExpression" })
public class BioRecipeLoader {

    public static void run() {
        registerWaterBasedBioLabIncubations();
        registerBacterialVatRecipes();
        registerBioItemRecipes();
    }

    public static void registerWaterBasedBioLabIncubations() {
        FluidStack[] easyFluids = { Materials.Water.getFluid(1_000), GTModHandler.getDistilledWater(1_000) };
        for (FluidStack fluidStack : easyFluids) {

            GTValues.RA.stdBuilder()
                .itemInputs(BioItemList.getPetriDish(null), new ItemStack(Items.rotten_flesh))
                .itemOutputs(BioItemList.getPetriDish(BioCultureLoader.rottenFleshBacteria))
                .outputChances(33_00)
                .fluidInputs(fluidStack)
                .duration(25 * SECONDS)
                .eut(TierEU.RECIPE_HV)
                .addTo(bioLabRecipes);

            GTValues.RA.stdBuilder()
                .itemInputs(BioItemList.getPetriDish(null), new ItemStack(Items.fermented_spider_eye))
                .itemOutputs(BioItemList.getPetriDish(BioCultureLoader.eColi))
                .outputChances(45_00)
                .fluidInputs(fluidStack)
                .duration(25 * SECONDS)
                .eut(TierEU.RECIPE_HV)
                .addTo(bioLabRecipes);

            GTValues.RA.stdBuilder()
                .itemInputs(BioItemList.getPetriDish(null), ItemList.Food_Dough.get(1L))
                .itemOutputs(BioItemList.getPetriDish(BioCultureLoader.CommonYeast))
                .outputChances(75_00)
                .fluidInputs(fluidStack)
                .duration(25 * SECONDS)
                .eut(TierEU.RECIPE_HV)
                .addTo(bioLabRecipes);

            GTValues.RA.stdBuilder()
                .itemInputs(BioItemList.getPetriDish(null), ItemList.Food_Dough_Sugar.get(1L))
                .itemOutputs(BioItemList.getPetriDish(BioCultureLoader.WhineYeast))
                .outputChances(25_00)
                .fluidInputs(fluidStack)
                .duration(25 * SECONDS)
                .eut(TierEU.RECIPE_HV)
                .addTo(bioLabRecipes);

            GTValues.RA.stdBuilder()
                .itemInputs(BioItemList.getPetriDish(null), ItemList.Bottle_Wine.get(1L))
                .itemOutputs(BioItemList.getPetriDish(BioCultureLoader.WhineYeast))
                .outputChances(33_00)
                .fluidInputs(fluidStack)
                .duration(25 * SECONDS)
                .eut(TierEU.RECIPE_HV)
                .addTo(bioLabRecipes);

            GTValues.RA.stdBuilder()
                .itemInputs(BioItemList.getPetriDish(null), ItemList.Bottle_Beer.get(1L))
                .itemOutputs(BioItemList.getPetriDish(BioCultureLoader.BeerYeast))
                .outputChances(25_00)
                .fluidInputs(fluidStack)
                .duration(25 * SECONDS)
                .eut(TierEU.RECIPE_HV)
                .addTo(bioLabRecipes);

            GTValues.RA.stdBuilder()
                .itemInputs(BioItemList.getPetriDish(null), ItemList.Bottle_Dark_Beer.get(1L))
                .itemOutputs(BioItemList.getPetriDish(BioCultureLoader.BeerYeast))
                .outputChances(33_00)
                .fluidInputs(fluidStack)
                .duration(25 * SECONDS)
                .eut(TierEU.RECIPE_HV)
                .addTo(bioLabRecipes);

            GTValues.RA.stdBuilder()
                .itemInputs(BioItemList.getPetriDish(null), new ItemStack(Blocks.dirt))
                .itemOutputs(BioItemList.getPetriDish(BioCultureLoader.anaerobicOil))
                .outputChances(100)
                .fluidInputs(fluidStack)
                .duration(1 * MINUTES + 15 * SECONDS)
                .eut(TierEU.RECIPE_EV)
                .addTo(bioLabRecipes);
        }
    }

    public static void registerBioItemRecipes() {
        GTValues.RA.stdBuilder()
            .itemInputs(Materials.MeatRaw.getDust(2), new ItemStack(Items.bone, 1))
            .itemOutputs(new ItemStack(BIOITEMS, 2, 0))
            .fluidInputs(Materials.DilutedSulfuricAcid.getFluid(1000))
            .fluidOutputs(Materials.Water.getFluid(1000))
            .duration(1 * MINUTES + 20 * SECONDS)
            .eut(TierEU.RECIPE_HV)
            .addTo(UniversalChemical);

        GTValues.RA.stdBuilder()
            .itemInputs(Materials.MeatRaw.getDust(1), Materials.Bone.getDust(2))
            .itemOutputs(new ItemStack(BIOITEMS, 1, 0))
            .fluidInputs(Materials.DilutedSulfuricAcid.getFluid(500))
            .fluidOutputs(Materials.Water.getFluid(500))
            .duration(40 * SECONDS)
            .eut(TierEU.RECIPE_HV)
            .addTo(UniversalChemical);

        GTValues.RA.stdBuilder()
            .itemInputs(new ItemStack(BIOITEMS, 4, 0), Materials.Water.getCells(3))
            .itemOutputs(Materials.Empty.getCells(3))
            .fluidInputs(Materials.PhosphoricAcid.getFluid(1000))
            .fluidOutputs(new FluidStack(BioLabFluidMaterials[4], 4000))
            .duration(1 * MINUTES + 20 * SECONDS)
            .eut(TierEU.RECIPE_HV)
            .addTo(UniversalChemical);

        GTValues.RA.stdBuilder()
            .itemInputs(new ItemStack(BIOITEMS, 4, 0), Materials.PhosphoricAcid.getCells(1))
            .itemOutputs(Materials.Empty.getCells(1))
            .fluidInputs(Materials.Water.getFluid(3000))
            .fluidOutputs(new FluidStack(BioLabFluidMaterials[4], 4000))
            .duration(1 * MINUTES + 20 * SECONDS)
            .eut(TierEU.RECIPE_HV)
            .addTo(chemicalReactorRecipes);

        GTValues.RA.stdBuilder()
            .circuit(1)
            .itemOutputs(Materials.Phosphorus.getDust(1), new ItemStack(BIOITEMS, 4, 1))
            .fluidInputs(new FluidStack(BioLabFluidMaterials[4], 6000))
            .duration(2 * MINUTES)
            .eut(TierEU.RECIPE_HV)
            .addTo(centrifugeRecipes);

        RA.stdBuilder()
            .itemInputs(new ItemStack(BIOITEMS, 1, 1))
            .circuit(11)
            .itemOutputs(new ItemStack(BIOITEMS, 1, 2))
            .fluidInputs(GTModHandler.getDistilledWater(1000))
            .duration(30 * SECONDS)
            .eut(TierEU.RECIPE_HV)
            .addTo(mixerRecipes);

        GTValues.RA.stdBuilder()
            .itemInputs(Materials.MeatRaw.getDust(1))
            .fluidOutputs(new FluidStack(BioLabFluidMaterials[5], 125))
            .duration(15 * SECONDS)
            .eut(TierEU.RECIPE_MV)
            .addTo(fluidExtractionRecipes);

        GTValues.RA.stdBuilder()
            .itemInputs(
                new ItemStack(BIOITEMS, 8, 2),
                ItemList.Circuit_Chip_Stemcell.get(16),
                Materials.Salt.getDust(64))
            .fluidInputs(
                FluidRegistry.getFluidStack("unknowwater", 4000),
                Materials.PhthalicAcid.getFluid(3000),
                new FluidStack(BioLabFluidMaterials[5], 1000))
            .fluidOutputs(new FluidStack(BioLabFluidMaterials[6], 8000))
            .duration(60 * SECONDS)
            .eut(TierEU.RECIPE_UV)
            .addTo(multiblockChemicalReactorRecipes);

        GTValues.RA.stdBuilder()
            .itemInputs(
                ItemList.IC2_Energium_Dust.get(8),
                Materials.Mytryl.getDust(1),
                getModItem(PamsHarvestCraft.ID, "seaweedItem", 64))
            .special(BioItemList.getPetriDish(BioCulture.getBioCulture("TcetiEBac")))
            .fluidInputs(new FluidStack(BioLabFluidMaterials[5], 50))
            .fluidOutputs(new FluidStack(BioLabFluidMaterials[6], 50))
            .duration(1 * MINUTES)
            .eut(TierEU.RECIPE_UV)
            .metadata(GLASS, 8)
            .metadata(SIEVERT, new Sievert(100, false))
            .addTo(bacterialVatRecipes);

        for (int i = 0; i < OreDictionary.getOres("cropTcetiESeaweed")
            .size(); i++) {
            GTValues.RA.stdBuilder()
                .circuit(i + 1)
                .itemOutputs(
                    OreDictionary.getOres("cropTcetiESeaweed")
                        .get(i)
                        .copy()
                        .splitStack(64))
                .fluidInputs(new FluidStack(BioLabFluidMaterials[7], 1000))
                .duration(2 * SECONDS)
                .eut(TierEU.RECIPE_UV)
                .addTo(centrifugeRecipes);
        }

        GTValues.RA.stdBuilder()
            .itemInputs(
                Materials.MeatRaw.getDust(4),
                Materials.Salt.getDust(4),
                Materials.Calcium.getDust(4),
                new ItemStack(BIOITEMS, 4, 2))
            .special(BioItemList.getPetriDish(BioCulture.getBioCulture("OvumBac")))
            .fluidInputs(FluidRegistry.getFluidStack("binnie.bacteria", 4))
            .fluidOutputs(Materials.GrowthMediumRaw.getFluid(1))
            .duration(1 * MINUTES)
            .eut(TierEU.RECIPE_IV)
            .metadata(GLASS, 5)
            .metadata(SIEVERT, new Sievert(BWUtil.calculateSv(Uranium), false))
            .addTo(bacterialVatRecipes);

        GTValues.RA.stdBuilder()
            .itemInputs(
                Materials.MeatRaw.getDust(8),
                Materials.Salt.getDust(8),
                Materials.Calcium.getDust(8),
                new ItemStack(BIOITEMS, 4, 2))
            .special(BioItemList.getPetriDish(BioCulture.getBioCulture("OvumBac")))
            .fluidInputs(FluidRegistry.getFluidStack("bacterialsludge", 4))
            .fluidOutputs(Materials.GrowthMediumRaw.getFluid(2))
            .duration(1 * MINUTES)
            .eut(TierEU.RECIPE_LuV)
            .metadata(GLASS, 6)
            .metadata(SIEVERT, new Sievert(BWUtil.calculateSv(Plutonium), false))
            .addTo(bacterialVatRecipes);

        GTValues.RA.stdBuilder()
            .itemInputs(
                Materials.MeatRaw.getDust(12),
                Materials.Salt.getDust(12),
                Materials.Calcium.getDust(12),
                new ItemStack(BIOITEMS, 4, 2))
            .special(BioItemList.getPetriDish(BioCulture.getBioCulture("OvumBac")))
            .fluidInputs(FluidRegistry.getFluidStack("mutagen", 4))
            .fluidOutputs(Materials.GrowthMediumRaw.getFluid(4))
            .duration(1 * MINUTES)
            .eut(TierEU.RECIPE_ZPM)
            .metadata(GLASS, 7)
            .metadata(SIEVERT, new Sievert(BWUtil.calculateSv(NaquadahEnriched), true))
            .addTo(bacterialVatRecipes);
    }

    @SuppressWarnings({ "PointlessArithmeticExpression", "RedundantSuppression" })
    public static void registerWaterBasedBacterialVatRecipes() {
        FluidStack[] easyFluids = { Materials.Water.getFluid(1_000), GTModHandler.getDistilledWater(1_000) };
        for (ItemStack grape : GTOreDictUnificator.getOres("cropGrape")) {
            for (FluidStack fluidStack : easyFluids) {
                GTValues.RA.stdBuilder()
                    .itemInputs(GTUtility.copyAmount(16, grape))
                    .special(BioItemList.getPetriDish(BioCultureLoader.WhineYeast))
                    .fluidInputs(new FluidStack(fluidStack, 100))
                    .fluidOutputs(FluidRegistry.getFluidStack("potion.wine", 12))
                    .metadata(GLASS, 3)
                    .duration(10 * SECONDS)
                    .eut(TierEU.RECIPE_MV)
                    .addTo(bacterialVatRecipes);
            }
        }
    }

    @SuppressWarnings({ "PointlessArithmeticExpression", "RedundantSuppression" })
    public static void registerBacterialVatRecipes() {
        registerWaterBasedBacterialVatRecipes();

        GTValues.RA.stdBuilder()
            .special(BioItemList.getPetriDish(BioCultureLoader.WhineYeast))
            .fluidInputs(FluidRegistry.getFluidStack("potion.grapejuice", 100))
            .fluidOutputs(FluidRegistry.getFluidStack("potion.wine", 12))
            .metadata(GLASS, 3)
            .duration(20 * SECONDS)
            .eut(TierEU.RECIPE_LV)
            .addTo(bacterialVatRecipes);

        GTValues.RA.stdBuilder()
            .special(BioItemList.getPetriDish(BioCultureLoader.anaerobicOil))
            .fluidInputs(Materials.FermentedBiomass.getFluid(10_000))
            .fluidOutputs(new FluidStack(FluidLoader.fulvicAcid, 1_000))
            .metadata(GLASS, 3)
            .duration(2 * MINUTES + 17 * SECONDS + 8 * TICKS)
            .eut(TierEU.RECIPE_LV)
            .addTo(bacterialVatRecipes);
    }

    public static void runOnServerStarted() {}
}
