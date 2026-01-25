package gregtech.common.tileentities.generators;

import static gregtech.api.enums.GTValues.V;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAYS_ENERGY_OUT;
import static gregtech.api.util.GTUtility.formatNumbers;

import java.util.ArrayList;
import java.util.List;

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
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;
import com.gtnewhorizons.modularui.common.widget.DrawableWidget;
import com.gtnewhorizons.modularui.common.widget.FakeSyncWidget;
import com.gtnewhorizons.modularui.common.widget.ProgressBar;
import com.gtnewhorizons.modularui.common.widget.SlotGroup;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import gregtech.api.enums.GTValues;
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

    private List<MultiblockHelper.TileLocation> coreLocations = new ArrayList<>();
    private int selectedCore = 0;
    private long mStored = 0;
    private long mMax = 0;

    public MTEEnergyPylon(int aID, String aName, String aNameRegional, int aTier) {
        super(
            aID,
            aName,
            aNameRegional,
            aTier,
            4,
            new String[] { "Intefaces between GregTechs ENet and the DE Energy Core" });
    }

    public MTEEnergyPylon(String aName, int aTier, int aInvSlotCount, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, aInvSlotCount, aDescription, aTextures);
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity baseMetaTileEntity, ForgeDirection sideDirection,
        ForgeDirection facingDirection, int colorIndex, boolean active, boolean redstoneLevel) {
        if (sideDirection == ForgeDirection.UP) {
            return new ITexture[] { Textures.BlockIcons.MACHINE_CASINGS[mTier][colorIndex + 1],
                TextureFactory.of(Textures.BlockIcons.OVERLAY_SOLAR_PANEL) };
        }
        if (sideDirection == facingDirection) {
            return new ITexture[] { Textures.BlockIcons.MACHINE_CASINGS[mTier][colorIndex + 1],
                OVERLAYS_ENERGY_OUT[mTier + 1] };
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
            this.mInventory.length,
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
    public int rechargerSlotCount() {
        return 4;
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
            SlotGroup.ofItemHandler(inventoryHandler, 2)
                .startFromSlot(0)
                .endAtSlot(3)
                .slotCreator(index -> new BaseSlot(inventoryHandler, index) {

                    @Override
                    public int getSlotStackLimit() {
                        return 1;
                    }
                })
                .background(getGUITextureSet().getItemSlot(), GTUITextures.OVERLAY_SLOT_CHARGER)
                .build()
                .setPos(100, 15))
            .widget(
                new ProgressBar().setProgress(() -> (float) mStored / mMax)
                    .setDirection(ProgressBar.Direction.RIGHT)
                    .setTexture(GTUITextures.PROGRESSBAR_STORED_EU, 147)
                    .setPos(14, 74)
                    .setSize(147, 5))
            .widget(
                new TextWidget().setStringSupplier(() -> formatNumbers(clientEU) + "/" + formatNumbers(mMax) + " EU")
                    .setTextAlignment(Alignment.Center)
                    .setPos(14, 66)
                    .setSize(147, 5))
            .widget(new FakeSyncWidget.LongSyncer(() -> mStored, val -> clientEU = val));
    }

    public void addConditionalImages(ModularWindow.Builder builder) {
        builder
            .widget(
                new DrawableWidget()
                    .setDrawable(
                        () -> foundCore ? GTUITextures.OVERLAY_BUTTON_CHECKMARK : GTUITextures.OVERLAY_BUTTON_CROSS)
                    .setPos(5, 26)
                    .setSize(16, 16))
            .widget(new TextWidget(StatCollector.translateToLocal("GT5U.machines.foundcoreindicator")).setPos(21, 31))
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
        // Keep energy level synced to the core
        syncEnergy();

        // Check every 5 seconds to find Another core
        if (aTick % 100 == 0) {
            nextCore();
        }
        super.onPostTick(aBaseMetaTileEntity, aTick);
    }

    @Override
    public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {
        nextCore();
        super.onFirstTick(aBaseMetaTileEntity);
    }

    private boolean foundCore = false;

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
        getWorld().markBlockForUpdate(getXCoord(), getYCoord(), getZCoord());

        if (!coreLocations.isEmpty()) foundCore = true;
        syncEnergy();
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

    private void syncEnergy() {
        TileEnergyStorageCore core = getMaster();
        if (core != null) {
            mStored = core.getEnergyStored();
            mMax = core.getMaxEnergyStored();
            if (mStored > GTValues.V[mTier]) {
                core.extractEnergy((int) GTValues.V[mTier], false);
                this.setEUVar(getBaseMetaTileEntity().getStoredEU() + GTValues.V[mTier]);
            }
        }
    }

    @Override
    public void getWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y,
        int z) {
        IGregTechTileEntity aBase = getBaseMetaTileEntity();
        tag.setBoolean("foundCore", foundCore);
        tag.setLong("storedeu", mStored);
        tag.setLong("maxeu", mMax);
        super.getWailaNBTData(player, tile, tag, world, x, y, z);
    }

    @Override
    public void getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        final NBTTagCompound tag = accessor.getNBTData();
        if (tag.hasKey("foundCore")) currenttip.add(
            tag.getBoolean("foundCore")
                ? EnumChatFormatting.GREEN + StatCollector.translateToLocal("GT5U.waila.generating.on")
                : EnumChatFormatting.RED + StatCollector.translateToLocal("GT5U.waila.generating.off"));
        if (tag.hasKey("storedeu") && tag.hasKey("maxeu")) currenttip.add(
            EnumChatFormatting.GREEN + formatNumbers(tag.getLong("storedeu"))
                + EnumChatFormatting.GRAY
                + " / "
                + EnumChatFormatting.YELLOW
                + formatNumbers(tag.getLong("maxeu"))
                + EnumChatFormatting.GRAY
                + " EU");
        super.getWailaBody(itemStack, currenttip, accessor, config);
    }

    @Override
    public long maxEUStore() {
        TileEnergyStorageCore core = getMaster();
        if (core == null || !core.isOnline()) return V[mTier] * 32;

        return core.getMaxEnergyStored();
    }

    @Override
    public long maxEUOutput() {
        return GTValues.V[mTier];
    }

    @Override
    public boolean isEnetOutput() {
        return true;
    }

    @Override
    public boolean isFacingValid(ForgeDirection side) {
        return side != ForgeDirection.UP;
    }

    @Override
    public boolean isOutputFacing(ForgeDirection side) {
        return side == getBaseMetaTileEntity().getFrontFacing();
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

}
