package gtPlusPlus.xmod.gregtech.registration.gregtech;

import static gregtech.api.enums.MetaTileEntityIDs.Industrial_Solar_Tower;
import static gregtech.api.enums.MetaTileEntityIDs.Solar_Tower_Reflector;

import gregtech.api.enums.GTValues;
import gtPlusPlus.api.objects.Logger;
import gtPlusPlus.api.recipe.GTPPRecipeMaps;
import gtPlusPlus.core.material.MaterialMisc;
import gtPlusPlus.xmod.gregtech.api.enums.GregtechItemList;
import gtPlusPlus.xmod.gregtech.common.tileentities.machines.multi.production.MTESolarTower;
import gtPlusPlus.xmod.gregtech.common.tileentities.misc.MTESolarHeater;

public class GregtechSolarTower {

    public static void run() {
        Logger.INFO("Gregtech5u Content | Registering Solar Tower.");
        run1();
    }

    private static void run1() {
        // Solar Tower
        GregtechItemList.Industrial_Solar_Tower.set(
            new MTESolarTower(Industrial_Solar_Tower.ID, "solartower.controller.tier.single", "Solar Tower")
                .getStackForm(1L));
        GregtechItemList.Solar_Tower_Reflector.set(
            new MTESolarHeater(
                Solar_Tower_Reflector.ID,
                "solarreflector.simple.single",
                "Solar Reflector",
                8,
                "Part of the Clean Green energy movement",
                0).getStackForm(1L));

        // NEI recipe
        GTValues.RA.stdBuilder()
            .fluidInputs(MaterialMisc.SOLAR_SALT_COLD.getFluidStack(1000))
            .fluidOutputs(MaterialMisc.SOLAR_SALT_HOT.getFluidStack(1000))
            .duration(0)
            .eut(0)
            .addTo(GTPPRecipeMaps.solarTowerRecipes);
    }
}
