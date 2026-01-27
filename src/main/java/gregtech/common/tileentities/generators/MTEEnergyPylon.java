package gregtech.common.tileentities.generators;

import static gregtech.api.enums.Textures.BlockIcons.OVERLAYS_ENERGY_IN;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAYS_ENERGY_OUT;
import static gregtech.api.util.GTUtility.formatNumbers;
import static net.minecraft.util.EnumChatFormatting.AQUA;
import static net.minecraft.util.EnumChatFormatting.BLUE;
import static net.minecraft.util.EnumChatFormatting.DARK_GRAY;
import static net.minecraft.util.EnumChatFormatting.DARK_GREEN;
import static net.minecraft.util.EnumChatFormatting.DARK_RED;
import static net.minecraft.util.EnumChatFormatting.GREEN;
import static net.minecraft.util.EnumChatFormatting.LIGHT_PURPLE;
import static net.minecraft.util.EnumChatFormatting.YELLOW;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.brandon3055.draconicevolution.client.handler.ParticleHandler;
import com.brandon3055.draconicevolution.client.render.particle.Particles;
import gregtech.api.util.tooltip.TooltipHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.brandon3055.draconicevolution.common.ModBlocks;
import com.brandon3055.draconicevolution.common.blocks.multiblock.MultiblockHelper;
import com.brandon3055.draconicevolution.common.blocks.multiblock.MultiblockHelper.TileLocation;
import com.brandon3055.draconicevolution.common.tileentities.multiblocktiles.TileEnergyStorageCore;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.DrawableWidget;
import com.gtnewhorizons.modularui.common.widget.FakeSyncWidget;
import com.gtnewhorizons.modularui.common.widget.ProgressBar;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import gregtech.api.enums.Textures;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.modularui.IAddGregtechLogo;
import gregtech.api.interfaces.modularui.IAddUIWidgets;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTETieredMachineBlock;
import gregtech.api.render.TextureFactory;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

public class MTEEnergyPylon extends MTETieredMachineBlock implements IAddGregtechLogo, IAddUIWidgets {

    private final List<MultiblockHelper.TileLocation> coreLocations = new ArrayList<>();
    private int selectedCore = 0;
    private long mCoreEU = 0;
    private long mMaxCoreEu = 0;
    public float modelRotation = 0;
    public float modelScale = 0;
    private byte particleRate = 0;
    private boolean foundCore = false;

    public MTEEnergyPylon(int aID, String aName, String aNameRegional, int aTier) {
        super(
            aID,
            aName,
            aNameRegional,
            aTier,
            0,
            new String[] {
                "Inserts or Extracts energy from Draconic Evolution's Energy Core",
                "Send -> <- Receive (Use a soft mallet to change mode)",
                "Has and internal buffer based on this formular",
                "(Tier Voltage * Tier Amperage) * 200",
                "Capacity, Voltage & Amperage is based on the Energy Core's tier",
                "Tier " + TooltipHelper.coloredText("1", YELLOW) + " : "  + TooltipHelper.coloredText("512", YELLOW) + " EU/t : " + TooltipHelper.coloredText("4", YELLOW) + " Amps",
                "Tier " + TooltipHelper.coloredText("2", DARK_GRAY) + " : "  + TooltipHelper.coloredText("2,048", DARK_GRAY) + " EU/t : " + TooltipHelper.coloredText("8", DARK_GRAY) + " Amps",
                "Tier " + TooltipHelper.coloredText("3", BLUE) + " : "  + TooltipHelper.coloredText("8,192", BLUE) + " EU/t : " + TooltipHelper.coloredText("16", BLUE) + " Amps",
                "Tier " + TooltipHelper.coloredText("4", LIGHT_PURPLE) + " : "  + TooltipHelper.coloredText("32,768", LIGHT_PURPLE) + " EU/t : " + TooltipHelper.coloredText("32", LIGHT_PURPLE) + " Amps",
                "Tier " + TooltipHelper.coloredText("5", AQUA) + " : "  + TooltipHelper.coloredText("131,072", AQUA) + " EU/t : " + TooltipHelper.coloredText("64", AQUA) + " Amps",
                "Tier " + TooltipHelper.coloredText("6", DARK_GREEN) + " : "  + TooltipHelper.coloredText("524,288", DARK_GREEN) + " EU/t : " + TooltipHelper.coloredText("128", DARK_GREEN) + " Amps",
                "Tier " + TooltipHelper.coloredText("7", DARK_RED) + " : "  + TooltipHelper.coloredText("2,097,152", DARK_RED) + " EU/t : " + TooltipHelper.coloredText("256", DARK_RED) + " Amps"
            });
    }

    public MTEEnergyPylon(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, 0, aDescription, aTextures);
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity baseMetaTileEntity, ForgeDirection sideDirection,
        ForgeDirection facingDirection, int colorIndex, boolean active, boolean redstoneLevel) {
        if (sideDirection == ForgeDirection.UP) {
            if (getBaseMetaTileEntity().isAllowedToWork()) {
                return new ITexture[] { Textures.BlockIcons.MACHINE_CASINGS[mTier][colorIndex + 1],
                    TextureFactory.of(Textures.BlockIcons.OVERLAY_TOP_ENERGY_PYLON_IN) };
            } else {
                return new ITexture[] { Textures.BlockIcons.MACHINE_CASINGS[mTier][colorIndex + 1],
                    TextureFactory.of(Textures.BlockIcons.OVERLAY_TOP_ENERGY_PYLON_OUT) };
            }}
        if (sideDirection == facingDirection) {
            if (getBaseMetaTileEntity().isAllowedToWork()) {
                return new ITexture[] { Textures.BlockIcons.MACHINE_CASINGS[mTier][colorIndex + 1],
                    OVERLAYS_ENERGY_IN[mTier + 1] };
            } else {
                return new ITexture[] { Textures.BlockIcons.MACHINE_CASINGS[mTier][colorIndex + 1],
                    OVERLAYS_ENERGY_OUT[mTier + 1] };
            }
        }
        return new ITexture[] { Textures.BlockIcons.MACHINE_CASINGS[mTier][colorIndex + 1] };
    }

    @Override
    public ITexture[][][] getTextureSet(ITexture[] aTextures) {
        return null;
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTEEnergyPylon(
            this.mName,
            this.mTier,
            this.mDescriptionArray,
            this.mTextures);
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {

    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {

    }

    @Override
    public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer, ForgeDirection side,
        float aX, float aY, float aZ) {
        openGui(aPlayer);
        return true;
    }

    protected long clientEU;

    @Override
    public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext buildContext) {
        addGregTechLogo(builder);
        addConditionalImages(builder);
        builder.widget(
                new ProgressBar().setProgress(() -> (float) (getBaseMetaTileEntity().getStoredEU() + mCoreEU) / (getBaseMetaTileEntity().getEUCapacity() + mMaxCoreEu))
                    .setDirection(ProgressBar.Direction.RIGHT)
                    .setTexture(GTUITextures.PROGRESSBAR_STORED_EU, 147)
                    .setPos(14, 74)
                    .setSize(147, 5))
            .widget(
                new TextWidget().setStringSupplier(() -> formatNumbers(clientEU) + "/" + formatNumbers(getBaseMetaTileEntity().getEUCapacity() + mMaxCoreEu) + " EU")
                    .setTextAlignment(Alignment.Center)
                    .setPos(14, 66)
                    .setSize(147, 5))
            .widget(new FakeSyncWidget.LongSyncer(() -> (getBaseMetaTileEntity().getStoredEU() + mCoreEU) , val -> clientEU = val));
    }

    public void addConditionalImages(ModularWindow.Builder builder) {
        builder
            .widget(
                new DrawableWidget()
                    .setDrawable(
                        () -> foundCore ? GTUITextures.OVERLAY_BUTTON_CHECKMARK : GTUITextures.OVERLAY_BUTTON_CROSS)
                    .setPos(5, 6)
                    .setSize(16, 16))
            .widget(new TextWidget(StatCollector.translateToLocal("GT5U.machines.energy_pylon.core_found")).setPos(21, 11))
            .widget(new FakeSyncWidget.BooleanSyncer(() -> foundCore, val -> foundCore = val));
    }

    @Override
    public void addGregTechLogo(ModularWindow.Builder builder) {
        builder.widget(
            new DrawableWidget().setDrawable(GTUITextures.PICTURE_GT_LOGO_17x17_TRANSPARENT)
                .setSize(17, 17)
                .setPos(154, 5));
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {

        // Update Render
        if (aBaseMetaTileEntity.isClientSide()) {
            modelRotation += 1.5F;
            modelScale += !aBaseMetaTileEntity.isAllowedToWork() ? 0.01F : -0.01F;
            if (modelScale < 0) {
                modelScale = !aBaseMetaTileEntity.isAllowedToWork() ? 0F : 10000F;
            }
        }

        if (aBaseMetaTileEntity.isServerSide()) {
            if (foundCore) {
                // Spawn Link Particles If core is found
                spawnParticles();
                if (particleRate > 0) particleRate--;
            }
            // Keep energy level synced to the core
            syncEnergy(aBaseMetaTileEntity);

            // Check every 5 seconds to find Another core
            if (aTick % 100 == 0) {
                nextCore();
            }
        }
        super.onPostTick(aBaseMetaTileEntity, aTick);
    }

    @Override
    public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {
        nextCore();
        super.onFirstTick(aBaseMetaTileEntity);
    }

    public float getModelScale() {
        return modelScale;
    }

    public float getModelRotation() {
        return modelRotation;
    }

    private void spawnParticles() {
        Random rand = getWorld().rand;

        TileEnergyStorageCore core = getMaster();
        if (core == null || !core.isOnline()) return;

        int x = core.xCoord;
        int y = core.yCoord;
        int z = core.zCoord;
        int cYCoord = getWorld().getBlockMetadata(getXCoord(), getYCoord(), getZCoord()) == 1 ? getYCoord() - 1 : getYCoord() + 1;

        float disMod = switch (core.getTier()) {
            case 0 -> 0.5F;
            case 1, 2 -> 1F;
            case 3, 4 -> 2F;
            case 5 -> 3F;
            default -> 4F;
        };

        if (particleRate > 20) particleRate = 20;
        double sourceX = x + 0.5 - disMod + (rand.nextFloat() * (disMod * 2));
        double sourceY = y + 0.5 - disMod + (rand.nextFloat() * (disMod * 2));
        double sourceZ = z + 0.5 - disMod + (rand.nextFloat() * (disMod * 2));
        double targetX = getXCoord() + 0.5;
        double targetY = cYCoord + 0.5;
        double targetZ = getZCoord() + 0.5;
        if (rand.nextFloat() < 0.05F) {
            Particles.EnergyTransferParticle passiveParticle = !getBaseMetaTileEntity().isAllowedToWork()
                ? new Particles.EnergyTransferParticle(
                getWorld(),
                targetX,
                targetY,
                targetZ,
                sourceX,
                sourceY,
                sourceZ,
                true)
                : new Particles.EnergyTransferParticle(
                getWorld(),
                sourceX,
                sourceY,
                sourceZ,
                targetX,
                targetY,
                targetZ,
                true);
            ParticleHandler.spawnCustomParticle(passiveParticle, 35);
        }
        if (particleRate > 0) {
            if (particleRate > 10 || rand.nextInt(Math.max(1, 10 - particleRate)) == 0) {
                int iterations = particleRate > 10 ? particleRate / 10 : 1;
                for (int i = 0; i <= iterations; i++) {
                    sourceX = x + 0.5 - disMod + (rand.nextFloat() * (disMod * 2));
                    sourceY = y + 0.5 - disMod + (rand.nextFloat() * (disMod * 2));
                    sourceZ = z + 0.5 - disMod + (rand.nextFloat() * (disMod * 2));
                    Particles.EnergyTransferParticle passiveParticle = !getBaseMetaTileEntity().isAllowedToWork()
                        ? new Particles.EnergyTransferParticle(
                        getWorld(),
                        targetX,
                        targetY,
                        targetZ,
                        sourceX,
                        sourceY,
                        sourceZ,
                        false)
                        : new Particles.EnergyTransferParticle(
                        getWorld(),
                        sourceX,
                        sourceY,
                        sourceZ,
                        targetX,
                        targetY,
                        targetZ,
                        false);
                    ParticleHandler.spawnCustomParticle(passiveParticle, 35);
                }
            }
        }
    }

    public TileEnergyStorageCore getMaster() {
        if (coreLocations.isEmpty()) return null;
        if (selectedCore >= coreLocations.size()) selectedCore = coreLocations.size() - 1;
        MultiblockHelper.TileLocation location = coreLocations.get(selectedCore);
        if (location != null) {
            TileEntity tile = location.getTileEntity(getWorld());
            if (tile instanceof TileEnergyStorageCore core) {
                return core;
            }
        }
        return null;
    }

    private void findCores() {
        int range = 15;
        List<MultiblockHelper.TileLocation> locations = new ArrayList<>();

        for (int x = getXCoord() - range; x <= getXCoord() + range; x++) {
            for (int y = getYCoord() - range; y <= getYCoord() + range; y++) {
                for (int z = getZCoord() - range; z <= getZCoord() + range; z++) {
                    if (getWorld().getBlock(x, y, z) == ModBlocks.energyStorageCore) {
                        TileLocation helper = new TileLocation(x, y, z);
                        locations.add(helper);
                    }
                }
            }
        }

        if (locations != coreLocations) {
            coreLocations.clear();
            coreLocations.addAll(locations);
            selectedCore = selectedCore >= coreLocations.size() ? 0 : selectedCore;
            getWorld().markBlockForUpdate(getXCoord(), getYCoord(), getZCoord());
        }
    }

    public void nextCore() {
        findCores();
        selectedCore++;
        if (selectedCore >= coreLocations.size()) selectedCore = 0;
        TileEnergyStorageCore core = getMaster();
        foundCore = !coreLocations.isEmpty() && core != null && core.isOnline();
        getWorld().markBlockForUpdate(getXCoord(), getYCoord(), getZCoord());
    }

    private int getXCoord() {
        return getBaseMetaTileEntity().getXCoord();
    }

    private int getYCoord() {
        return getBaseMetaTileEntity().getYCoord();
    }

    private int getZCoord() {
        return getBaseMetaTileEntity().getZCoord();
    }

    private World getWorld() {
        return getBaseMetaTileEntity().getWorld();
    }

    private void syncEnergy(IGregTechTileEntity aBaseMetaTileEntity) {
        TileEnergyStorageCore core = getMaster();
        if (core != null) {
            mCoreEU = core.getEnergyStored();
            mMaxCoreEu = core.getMaxEnergyStored();
            aBaseMetaTileEntity.setActive(aBaseMetaTileEntity.isAllowedToWork());

            // Dynamically request exactly what we need to fill the buffer
            long stored = getBaseMetaTileEntity().getStoredEU();
            long spaceLeft = getBaseMetaTileEntity().getEUCapacity() - stored;

            if (getBaseMetaTileEntity().isAllowedToWork()) {
                // Pull from core if we have space
                if (spaceLeft > 0 && mCoreEU > 0) {
                    extractEnergy(spaceLeft, core);
                }
            } else {
                // Push to core if we are over some threshold
                if (stored > 0 && mCoreEU < mMaxCoreEu) {
                    receiveEnergy(stored, core);
                }
            }
        }
    }

    private void extractEnergy(long requested, TileEnergyStorageCore core) {
        long stored = getBaseMetaTileEntity().getStoredEU();
        long space = getBaseMetaTileEntity().getEUCapacity() - stored;
        if (space <= 0 || requested <= 0) return;

        long coreAvailable = mCoreEU;

        long amount = Math.min(requested, Math.min(coreAvailable, space));

        if (amount > 0) {
            particleRate = (byte) Math.min(20, amount < 500 ? 1 : amount / 500);
            core.extractEnergy((int) amount, false);
            this.setEUVar(stored + amount);
        }
    }

    private void receiveEnergy(long requested, TileEnergyStorageCore core) {
        long stored = getBaseMetaTileEntity().getStoredEU();
        if (stored <= 0 || requested <= 0) return;

        long coreSpace = mMaxCoreEu - mCoreEU;
        if (coreSpace == 0) return;

        long amount = Math.min(requested, Math.min(stored, coreSpace));

        if (amount > 0) {
            particleRate = (byte) Math.min(20, amount < 500 ? 1 : amount / 500);
            core.receiveEnergy((int) amount, false);
            this.setEUVar(stored - amount);
        }
    }

    @Override
    public void getWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y,
        int z) {
        IGregTechTileEntity aBase = getBaseMetaTileEntity();
        tag.setBoolean("foundCore", foundCore);
        tag.setBoolean("mode", getBaseMetaTileEntity().isAllowedToWork());
        tag.setLong("storedEu", (getBaseMetaTileEntity().getStoredEU() + mCoreEU));
        tag.setLong("maxStoredEu", (getBaseMetaTileEntity().getEUCapacity() + mMaxCoreEu));
        super.getWailaNBTData(player, tile, tag, world, x, y, z);
    }

    @Override
    public void getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        final NBTTagCompound tag = accessor.getNBTData();
        if (tag.hasKey("foundCore")) currenttip.add(
            tag.getBoolean("foundCore")
                ? GREEN + StatCollector.translateToLocal("GT5U.waila.generating.foundCore")
                : EnumChatFormatting.RED + StatCollector.translateToLocal("GT5U.waila.generating.missingCore"));
        if (tag.hasKey("mode")) currenttip.add(
            tag.getBoolean("mode")
                ? GREEN + StatCollector.translateToLocal("GT5U.waila.generating.exportMode")
                : EnumChatFormatting.RED + StatCollector.translateToLocal("GT5U.waila.generating.importMode"));
        if (tag.hasKey("storedEu") && tag.hasKey("maxStoredEu")) currenttip.add(
            GREEN + formatNumbers(tag.getLong("storedEu"))
                + EnumChatFormatting.GRAY
                + " / "
                + EnumChatFormatting.YELLOW
                + formatNumbers(tag.getLong("maxStoredEu"))
                + EnumChatFormatting.GRAY
                + " EU");
        super.getWailaBody(itemStack, currenttip, accessor, config);
    }

    @Override
    public long maxEUStore() {
        TileEnergyStorageCore core = getMaster();
        if (core == null || !core.isOnline()) return 0;
        long voltage = DECoreTierSpecs.fromTier(core.getTier()).voltage;
        long amperage = DECoreTierSpecs.fromTier(core.getTier()).amperage;
        long maxPower = voltage * amperage;
        return maxPower * 200;
    }

    @Override
    public boolean isEnetOutput() {
        return true;
    }

    @Override
    public boolean isEnetInput() {
        return true;
    }

    @Override
    public long maxEUOutput() {
        TileEnergyStorageCore core = getMaster();
        if (core == null || !core.isOnline()) return 0;
        long voltage = DECoreTierSpecs.fromTier(core.getTier()).voltage;

        return getBaseMetaTileEntity().isAllowedToWork() ? voltage : 0;
    }

    @Override
    public long maxEUInput() {
        TileEnergyStorageCore core = getMaster();
        if (core == null || !core.isOnline()) return 0;
        long voltage = DECoreTierSpecs.fromTier(core.getTier()).voltage;

        return getBaseMetaTileEntity().isAllowedToWork() ? 0 : voltage;
    }

    @Override
    public long maxAmperesOut() {
        TileEnergyStorageCore core = getMaster();
        if (core == null || !core.isOnline()) return 0;
        long amperage = DECoreTierSpecs.fromTier(core.getTier()).amperage;

        return getBaseMetaTileEntity().isAllowedToWork() ? amperage : 0;
    }

    @Override
    public long maxAmperesIn() {
        TileEnergyStorageCore core = getMaster();
        if (core == null || !core.isOnline()) return 0;
        long amperage = DECoreTierSpecs.fromTier(core.getTier()).amperage;

        return getBaseMetaTileEntity().isAllowedToWork() ? 0 : amperage;
    }

    @Override
    public boolean isFacingValid(ForgeDirection side) {
        return side != ForgeDirection.UP;
    }

    @Override
    public boolean isInputFacing(ForgeDirection side) {
        ForgeDirection blockFrontFacing = getBaseMetaTileEntity().getFrontFacing();

        if (getBaseMetaTileEntity().isAllowedToWork()) {
            return false;
        } else {
            return side == blockFrontFacing;
        }
    }

    @Override
    public boolean isOutputFacing(ForgeDirection side) {
        ForgeDirection blockFrontFacing = getBaseMetaTileEntity().getFrontFacing();

        if (getBaseMetaTileEntity().isAllowedToWork()) {
            return side == blockFrontFacing;
        } else {
            return false;
        }
    }

    @Override
    public boolean allowPullStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
        ItemStack aStack) {
        return false;
    }

    @Override
    public boolean allowPutStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
        ItemStack aStack) {
        return false;
    }

    @Override
    public boolean hasAlternativeModeText() {
        return true;
    }

    @Override
    public String getAlternativeModeText() {
        return (getBaseMetaTileEntity().isAllowedToWork() ? StatCollector.translateToLocal("Extracting Energy From Core")
            : StatCollector.translateToLocal("Injecting Energy Into Core"));
    }

    @Override
    public boolean shouldJoinIc2Enet() {
        return true;
    }

    public enum DECoreTierSpecs {

        TIER_1(512, 4),
        TIER_2(2048, 8),
        TIER_3(8192, 16),
        TIER_4(32768, 32),
        TIER_5(131072, 64),
        TIER_6(524288, 128),
        TIER_7(2097152, 256);

        public final long voltage;
        public final long amperage;

        DECoreTierSpecs(long voltage, long amperage) {
            this.voltage = voltage;
            this.amperage = amperage;
        }

        public static DECoreTierSpecs fromTier(int tier) {
            if (tier < 0 || tier >= values().length) return TIER_1;
            return values()[tier];
        }

    }
}
