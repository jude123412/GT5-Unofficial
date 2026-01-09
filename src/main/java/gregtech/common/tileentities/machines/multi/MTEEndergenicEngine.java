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

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

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
                            .dot(1)
                            .buildAndChain(sBlockCasings4, 2)))
                .addElement(
                    'n',
                    ofChain(
                        isAir(),
                        ofBlock(
                            FluidRegistry.getFluid("nutrient_distillation")
                                .getBlock(),
                            0)))
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

    protected int getNominalOutput() {
        return 2048;
    }

    protected int getEfficiencyIncrease() {
        return 10;
    }

    @Override
    @NotNull
    public CheckRecipeResult checkProcessing() {
        ArrayList<FluidStack> tFluids = getStoredFluids();

        int runtime = this.mRuntime % 20;
        if (runtime == 15) {
            Minecraft minecraft = Minecraft.getMinecraft();
            World world = minecraft.thePlayer.worldObj;
            float random = world.rand.nextFloat();

            int xCoord = getBaseMetaTileEntity().getBackFacing().offsetX;
            int yCoord = getBaseMetaTileEntity().getBackFacing().offsetY;

            // Get Facing direction
            IGregTechTileEntity aBaseMetaTileEntity = this.getBaseMetaTileEntity();
            int mDirectionX = aBaseMetaTileEntity.getBackFacing().offsetX;
            int mCurrentDirectionX;
            int mCurrentDirectionZ;
            int mOffsetX_Lower = -1;
            int mOffsetX_Upper = -1;
            int mOffsetZ_Lower = -1;
            int mOffsetZ_Upper = -1;

            if (mDirectionX == 0) {
                mCurrentDirectionX = 0;
                mCurrentDirectionZ = 3;
                mOffsetX_Lower = 3;
                mOffsetX_Upper = 0;
                mOffsetZ_Lower = 3;
                mOffsetZ_Upper = 0;
            } else {
                mCurrentDirectionX = 3;
                mCurrentDirectionZ = 0;
                mOffsetX_Lower = 0;
                mOffsetX_Upper = 3;
                mOffsetZ_Lower = 0;
                mOffsetZ_Upper = 3;
            }

            final int xDir = aBaseMetaTileEntity.getBackFacing().offsetX * mCurrentDirectionX;
            final int zDir = aBaseMetaTileEntity.getBackFacing().offsetZ * mCurrentDirectionZ;

            // Play bubble sound every 15 ticks with random pitch
            world.playSound(
                xDir + 0.5,
                yCoord + 1,
                zDir + 0.5,
                EnderIO.MODID + ":generator.zombie.bubble",
                0.100F,
                world.rand.nextFloat() * 0.75F,
                false);

            // Spawn bubbles in sync with bubble sound
            for (float x = mOffsetX_Lower; x < mOffsetX_Upper; x++) {
                for (float z = mOffsetZ_Lower; z < mOffsetZ_Upper; z++) {
                    if (x == 1 && z == 1) {
                        continue; // Skip center of 3x3, Thanks ChatGPT xD.
                    }

                    float aOffset = 0.1F * random;
                    float bOffset = random > 0.25f ? (float) 0.25 : random;

                    EntityFX bubbleFX = new EndergenicBubbleRenderer(
                        world,
                        xDir + x,
                        yCoord,
                        zDir + z,
                        aOffset,
                        0.5f,
                        aOffset);
                    minecraft.effectRenderer.addEffect(bubbleFX);
                }
            }
        }

        // fast track lookup
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
                    boostedOutput = getNominalOutput() * getCapacitorTier(controllerSlot);

                    fuelConsumption = tLiquid.amount = (int) (getCapacitorTier(controllerSlot) * getNominalOutput()
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
                    fuelConsumption = tLiquid.amount = getNominalOutput() / fuelValue;
                }

                // Check to prevent consuming DOTV or VOL if capacitor tier is less than melodic
                if (fuelValue > getNominalOutput() && capacitorTier < 4.0F) {
                    return SimpleCheckRecipeResult.ofFailure("capacitor_tier_too_low");
                }

                fuelRemaining = tFluid.amount;
                this.mEUt = getNominalOutput();
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

    public static float getCapacitorTier(ItemStack capacitor) {
        ItemStack capacitorDouble = getModItem(Mods.EnderIO.ID, "itemBasicCapacitor", 1L, 1);
        ItemStack capacitorOctadic = getModItem(Mods.EnderIO.ID, "itemBasicCapacitor", 1L, 2);
        ItemStack capacitorCrystalline = getModItem(Mods.EnderIO.ID, "itemBasicCapacitor", 1L, 3);
        ItemStack capacitorMelodic = getModItem(Mods.EnderIO.ID, "itemBasicCapacitor", 1L, 4);
        ItemStack capacitorStellar = getModItem(Mods.EnderIO.ID, "itemBasicCapacitor", 1L, 5);
        ItemStack capacitorTotemic = getModItem(Mods.EnderIO.ID, "itemBasicCapacitor", 1L, 7);
        ItemStack capacitorEndergetic = getModItem(Mods.EnderIO.ID, "itemBasicCapacitor", 1L, 8);
        ItemStack capacitorEndergized = getModItem(Mods.EnderIO.ID, "itemBasicCapacitor", 1L, 9);

        if (capacitor.isItemEqual(capacitorDouble) || capacitor.isItemEqual(capacitorEndergetic)) {
            return 2.0F;
        } else if (capacitor.isItemEqual(capacitorOctadic) || capacitor.isItemEqual(capacitorEndergized)) {
            return 3.0F;
        } else if (capacitor.isItemEqual(capacitorCrystalline) || capacitor.isItemEqual(capacitorTotemic)) {
            return 3.5F;
        } else if (capacitor.isItemEqual(capacitorMelodic)) {
            return 4.0F;
        } else if (capacitor.isItemEqual(capacitorStellar)) {
            return 5.0F;
        } else {
            return 1.0F;
        }
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
                + GTUtility.formatNumbers(storedEnergy)
                + EnumChatFormatting.RESET
                + " EU / "
                + EnumChatFormatting.YELLOW
                + GTUtility.formatNumbers(maxEnergy)
                + EnumChatFormatting.RESET
                + " EU",
            getIdealStatus() == getRepairStatus()
                ? EnumChatFormatting.GREEN + StatCollector.translateToLocal("GT5U.turbine.maintenance.false")
                    + EnumChatFormatting.RESET
                : EnumChatFormatting.RED + StatCollector.translateToLocal("GT5U.turbine.maintenance.true")
                    + EnumChatFormatting.RESET,
            StatCollector.translateToLocal("GT5U.engine.output") + ": "
                + EnumChatFormatting.RED
                + GTUtility.formatNumbers(((long) mEUt * mEfficiency / 10000))
                + EnumChatFormatting.RESET
                + " EU/t",
            StatCollector.translateToLocal("GT5U.engine.consumption") + ": "
                + EnumChatFormatting.YELLOW
                + GTUtility.formatNumbers(fuelConsumption)
                + EnumChatFormatting.RESET
                + " L/t",
            StatCollector.translateToLocal("GT5U.engine.value") + ": "
                + EnumChatFormatting.YELLOW
                + GTUtility.formatNumbers(fuelValue)
                + EnumChatFormatting.RESET
                + " EU/L",
            StatCollector.translateToLocal("GT5U.turbine.fuel") + ": "
                + EnumChatFormatting.GOLD
                + GTUtility.formatNumbers(fuelRemaining)
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
