package bartworks.common.loaders.recipes;

import gregtech.api.enums.GTValues;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.enums.TierEU;
import gtnhlanth.common.register.WerkstoffMaterialPool;

import static gregtech.api.recipe.RecipeMaps.blastFurnaceRecipes;
import static gregtech.api.util.GTRecipeBuilder.MINUTES;
import static gregtech.api.util.GTRecipeBuilder.SECONDS;
import static gregtech.api.util.GTRecipeConstants.COIL_HEAT;


public class ElectricBlastFurnace implements Runnable {

    @Override
    public void run() {
        GTValues.RA.stdBuilder()
            .itemInputs(WerkstoffMaterialPool.Hafnium.get(OrePrefixes.dust, 1))
            .fluidInputs(Materials.Oxygen.getGas(2_000))
            .circuit(24)
            .itemOutputs(WerkstoffMaterialPool.Hafnia.get(OrePrefixes.dust, 3))
            .duration(2 * MINUTES + 30 * SECONDS)
            .eut(TierEU.RECIPE_IV)
            .metadata(COIL_HEAT, 2823)
            .addTo(blastFurnaceRecipes);
    }
}
