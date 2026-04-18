package gregtech.common.tileentities.machines.multi;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.*;
import static gregtech.api.GregTechAPI.*;
import static gregtech.api.enums.HatchElement.*;
import static gregtech.api.enums.Textures.BlockIcons.*;
import static gregtech.api.util.GTModHandler.getModItem;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static gregtech.api.util.GTStructureUtility.chainAllGlasses;
import static gregtech.api.util.GTUtility.validMTEList;

import java.util.ArrayList;

import crazypants.enderio.material.ItemCapacitor;
import crazypants.enderio.power.ICapacitor;
import crazypants.enderio.power.ICapacitorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizon.gtnhlib.util.numberformatting.NumberFormatUtil;
import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;

import crazypants.enderio.EnderIO;
import gregtech.api.enums.Mods;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEEnhancedMultiBlockBase;
import gregtech.api.metatileentity.implementations.MTEHatchDynamo;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.recipe.check.SimpleCheckRecipeResult;
import gregtech.api.recipe.maps.FuelBackend;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTUtility;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.common.render.EndergenicBubbleRenderer;

public class MTEEndergenicEngine extends MTEEnhancedMultiBlockBase<MTEEndergenicEngine>
    implements ISurvivalConstructable {

    private static final String STRUCTURE_PIECE_MAIN = "main";

    private static final ClassValue<IStructureDefinition<MTEEndergenicEngine>> STRUCTURE_DEFINITION = new ClassValue<>() {

        @Override
        protected IStructureDefinition<MTEEndergenicEngine> computeValue(Class<?> type) {
            return StructureDefinition.<MTEEndergenicEngine>builder()
                .addShape(
                    STRUCTURE_PIECE_MAIN,
                    transpose(
                        new String[][] { { "ccccc", "chhhc", "chdhc", "chhhc", "chhhc" },
                            { "cgcgc", "gnnng", "cninc", "gnnng", "cgcgc" },
                            { "cg~gc", "gnnng", "cninc", "gnnng", "cgcgc" },
                            { "cgcgc", "gnnng", "cninc", "gnnng", "cgcgc" },
                            { "ccccc", "chhhc", "chhhc", "chhhc", "chhhc" }, }))
                .addElement('i', ofBlock(sBlockCasings4, 13))
                .addElement('c', ofBlock(sBlockCasings4, 2))
                .addElement('g', chainAllGlasses())
                .addElement('d', Dynamo.newAny(50, 2))
                .addElement(
                    'h',
                    lazy(
                        t -> buildHatchAdder(MTEEndergenicEngine.class).atLeast(InputHatch, InputHatch, Maintenance)
                            .casingIndex(50)
                            .hint(1)
                            .buildAndChain(sBlockCasings4, 2)))
                .addElement(
                    'n',
                    ofBlock(
                        FluidRegistry.getFluid("nutrient_distillation")
                            .getBlock(),
                        0))
                .build();
        }
    };

    protected int fuelConsumption = 0;
    protected int fuelValue = 0;
    protected int fuelRemaining = 0;
    protected float capacitorTier = 1.0f;

    public MTEEndergenicEngine(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public MTEEndergenicEngine(String aName) {
        super(aName);
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        final MultiblockTooltipBuilder tooltipBuilder = new MultiblockTooltipBuilder();
        tooltipBuilder.addMachineType("Endergenic Engine, LEE")
            .addInfo("Supply various Ender IO fluids to produce power")
            .addInfo("Supply 40L/s of Liquid Sunshine to boost output (optional)")
            .addInfo("Boosting is required on fuels that produce more than 1 million Eu/bucket")
            .addInfo("Produces 2048 Eu/t when not boosted, or 8192 Eu/t when boosted")
            .addInfo("Takes 50 seconds to produce full power output or 200 seconds when boosted")
            .addPollutionAmount(getPollutionPerSecond(null))
            .beginStructureBlock(5, 5, 5, true)
            .addController("Middle center")
            .addCasingInfoRange("Stable Titanium Machine Casing", 16, 22, false)
            .addOtherStructurePart("Titanium Gear Box Machine Casing", "Inner 2 blocks")
            .addOtherStructurePart("Engine Intake Machine Casing", "8x, ring around controller")
            .addStructureInfo("Engine Intake Casings must not be obstructed in front (only air blocks)")
            .addDynamoHatch("Top center", 2)
            .addMaintenanceHatch("One of the bottom center casings next to an Engine Intake", 1)
            .addInputHatch("Diesel Fuel, next to a Gear Box", 1)
            .addInputHatch("Lubricant, next to a Gear Box", 1)
            .addInputHatch("Oxygen, optional, next to a Gear Box", 1)
            .toolTipFinisher();
        return tooltipBuilder;
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, ForgeDirection side, ForgeDirection aFacing,
        int colorIndex, boolean aActive, boolean redstoneLevel) {
        if (side == aFacing) {
            if (aActive) return new ITexture[] { casingTexturePages[0][50], TextureFactory.builder()
                .addIcon(OVERLAY_FRONT_DIESEL_ENGINE_ACTIVE)
                .extFacing()
                .build(),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_DIESEL_ENGINE_ACTIVE_GLOW)
                    .extFacing()
                    .glow()
                    .build() };
            return new ITexture[] { casingTexturePages[0][50], TextureFactory.builder()
                .addIcon(OVERLAY_FRONT_DIESEL_ENGINE)
                .extFacing()
                .build(),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_DIESEL_ENGINE_GLOW)
                    .extFacing()
                    .glow()
                    .build() };
        }
        return new ITexture[] { casingTexturePages[0][50] };
    }

    @Override
    public boolean supportsPowerPanel() {
        return false;
    }

    @Override
    public boolean isCorrectMachinePart(ItemStack aStack) {
        return getMaxEfficiency(aStack) > 0;
    }

    @Override
    public RecipeMap<FuelBackend> getRecipeMap() {
        return RecipeMaps.endergenicFuels;
    }

    @Override
    protected boolean filtersFluid() {
        return false;
    }

    protected int getEfficiencyIncrease() {
        return 10;
    }

    @Override
    @NotNull
    public CheckRecipeResult checkProcessing() {
        ArrayList<FluidStack> tFluids = getStoredFluids();
        if (!tFluids.isEmpty()) {
            double boostedFuelValue = 0;
            double boostedOutput = 0;
            double extraFuelFraction = 0;
            for (FluidStack tFluid : tFluids) {
                GTRecipe tRecipe = getRecipeMap().getBackend()
                    .findFuel(tFluid);
                if (tRecipe == null) continue;
                fuelValue = tRecipe.mSpecialValue;

                ItemStack controllerSlot = this.getControllerSlot();
                FluidStack tLiquid = tFluid.copy();
                // Check capacitor tier before boost
                if (controllerSlot != null && getCapacitorTier(controllerSlot) > 1.0F) {
                    capacitorTier = getCapacitorTier(controllerSlot);
                    boostedFuelValue = GTUtility.safeInt((long) (fuelValue * getCapacitorTier(controllerSlot)));
                    boostedOutput = getCapacitorMaxExtract(controllerSlot) * getCapacitorTier(controllerSlot);

                    fuelConsumption = tLiquid.amount = (int) (getCapacitorTier(controllerSlot) * getCapacitorMaxExtract(controllerSlot)
                        / fuelValue);

                    if (boostedFuelValue * 2 > boostedOutput) {
                        extraFuelFraction = boostedOutput / boostedFuelValue;
                        extraFuelFraction = extraFuelFraction - (int) extraFuelFraction;
                        double rand = Math.random();
                        if (rand < extraFuelFraction) {
                            tLiquid.amount += 1;
                        }
                    }
                } else {
                    // Return capacitor tier to default if removed
                    if (controllerSlot == null) {
                        capacitorTier = 1.0F;
                    }
                    fuelConsumption = tLiquid.amount = getCapacitorMaxExtract(controllerSlot) / fuelValue;
                }

                // Check to prevent consuming DOTV or VOL if capacitor tier is less than melodic
                if (fuelValue > getCapacitorMaxExtract(controllerSlot) && capacitorTier < 4.0F) {
                    return SimpleCheckRecipeResult.ofFailure("capacitor_tier_too_low");
                }

                fuelRemaining = tFluid.amount;
                this.mEUt = getCapacitorMaxExtract(controllerSlot);
                this.mProgresstime = 1;
                this.mMaxProgresstime = 1;
                this.mEfficiencyIncrease = (int) (getEfficiencyIncrease() * capacitorTier);
                return CheckRecipeResultRegistry.GENERATING;
            }

            if (this.mEfficiency > 10000 && capacitorTier > 1.0F) {
                this.mEfficiency = (int) (10000 * capacitorTier);
            }
        }
        this.mEUt = 0;
        this.mEfficiency = 0;
        return CheckRecipeResultRegistry.NO_FUEL_FOUND;
    }

    private static ICapacitor getCapacitor(ItemStack capacitor) {
        if (capacitor != null) {
            if (capacitor.getItem() instanceof ItemCapacitor cap) {
                return cap.getCapacitor(capacitor);
            }
        }
        return null;
    }

    private static float getCapacitorTier(ItemStack capacitor) {
        ICapacitor cap = getCapacitor(capacitor);
        if (cap != null) return cap.getTier();
        return 1.0F;
    }

    private static int getCapacitorMaxExtract(ItemStack capacitor) {
        ICapacitor cap = getCapacitor(capacitor);
        if (cap != null) return 512 * (cap.getMaxEnergyExtracted() / 20);
        return 512;
    }

    @Override
    public IStructureDefinition<MTEEndergenicEngine> getStructureDefinition() {
        return STRUCTURE_DEFINITION.get(getClass());
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        return checkPiece(STRUCTURE_PIECE_MAIN, 2, 2, 0) && mMaintenanceHatches.size() == 1;
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTEEndergenicEngine(this.mName);
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
    }

    @Override
    public int getDamageToComponent(ItemStack aStack) {
        return 1;
    }

    @Override
    public int getMaxEfficiency(ItemStack aStack) {
        return (int) (10000 * capacitorTier);
    }

    @Override
    public boolean explodesOnComponentBreak(ItemStack aStack) {
        return true;
    }

    @Override
    public String[] getInfoData() {
        long storedEnergy = 0;
        long maxEnergy = 0;
        for (MTEHatchDynamo tHatch : validMTEList(mDynamoHatches)) {
            storedEnergy += tHatch.getBaseMetaTileEntity()
                .getStoredEU();
            maxEnergy += tHatch.getBaseMetaTileEntity()
                .getEUCapacity();
        }

        return new String[] {
            EnumChatFormatting.BLUE + StatCollector.translateToLocal("GT5U.infodata.endergenic_engine")
                + EnumChatFormatting.RESET,
            StatCollector.translateToLocal("GT5U.multiblock.energy") + ": "
                + EnumChatFormatting.GREEN
                + NumberFormatUtil.formatNumber(storedEnergy)
                + EnumChatFormatting.RESET
                + " EU / "
                + EnumChatFormatting.YELLOW
                + NumberFormatUtil.formatNumber(maxEnergy)
                + EnumChatFormatting.RESET
                + " EU",
            getIdealStatus() == getRepairStatus()
                ? EnumChatFormatting.GREEN + StatCollector.translateToLocal("GT5U.turbine.maintenance.false")
                    + EnumChatFormatting.RESET
                : EnumChatFormatting.RED + StatCollector.translateToLocal("GT5U.turbine.maintenance.true")
                    + EnumChatFormatting.RESET,
            StatCollector.translateToLocal("GT5U.engine.output") + ": "
                + EnumChatFormatting.RED
                + NumberFormatUtil.formatNumber(((long) mEUt * mEfficiency / 10000))
                + EnumChatFormatting.RESET
                + " EU/t",
            StatCollector.translateToLocal("GT5U.engine.consumption") + ": "
                + EnumChatFormatting.YELLOW
                + NumberFormatUtil.formatNumber(fuelConsumption)
                + EnumChatFormatting.RESET
                + " L/t",
            StatCollector.translateToLocal("GT5U.engine.value") + ": "
                + EnumChatFormatting.YELLOW
                + NumberFormatUtil.formatNumber(fuelValue)
                + EnumChatFormatting.RESET
                + " EU/L",
            StatCollector.translateToLocal("GT5U.turbine.fuel") + ": "
                + EnumChatFormatting.GOLD
                + NumberFormatUtil.formatNumber(fuelRemaining)
                + EnumChatFormatting.RESET
                + " L",
            StatCollector.translateToLocal("GT5U.engine.efficiency") + ": "
                + EnumChatFormatting.YELLOW
                + (mEfficiency / 100F)
                + EnumChatFormatting.YELLOW
                + " %",
            StatCollector.translateToLocal("GT5U.multiblock.pollution") + ": "
                + EnumChatFormatting.GREEN
                + getAveragePollutionPercentage()
                + EnumChatFormatting.RESET
                + " %" };
    }

    @Override
    public boolean showRecipeTextInGUI() {
        return false;
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        buildPiece(STRUCTURE_PIECE_MAIN, stackSize, hintsOnly, 2, 2, 0);
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        if (mMachine) return -1;
        return survivalBuildPiece(STRUCTURE_PIECE_MAIN, stackSize, 2, 2, 0, elementBudget, env, false, true);
    }
}
