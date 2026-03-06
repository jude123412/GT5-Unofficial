package gregtech.api.metatileentity.implementations;

import static net.minecraft.util.StatCollector.translateToLocal;

import java.util.EnumSet;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.core.AppEng;
import appeng.core.sync.GuiBridge;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import gregtech.api.enums.ItemList;
import gregtech.api.interfaces.IMEConnectable;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.grid.IDigiVisSource;
import thaumicenergistics.common.integration.tc.DigiVisSourceData;

public class MTEMagicalMaintenanceHatchME extends MTEMagicalMaintenanceHatch implements IGridProxyable, IMEConnectable {

    private boolean additionalConnection = false;
    private @Nullable AENetworkProxy gridProxy = null;

    private final DigiVisSourceData visSourceInfo = new DigiVisSourceData();

    public MTEMagicalMaintenanceHatchME(int aID, String aName, String aNameRegional, int aTier) {
        super(aID, aName, aNameRegional, aTier);
    }

    public MTEMagicalMaintenanceHatchME(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, aDescription, aTextures);
    }

    @Override
    public String[] getDescription() {
        return new String[] { translateToLocal("gt.blockmachines.magical.maintenance.desc.0"),
            translateToLocal("gt.blockmachines.magical.maintenance.desc.1"),
            translateToLocal("gt.blockmachines.magical.maintenance.desc.2"),
            translateToLocal("gt.blockmachines.magical.maintenance.desc.3") };
    }

    @Override
    public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {
        super.onFirstTick(aBaseMetaTileEntity);
        getProxy().onReady();
        IGridNode node = getProxy().getNode();
        if (node != null) node.updateState();
    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTEMagicalMaintenanceHatchME(this.mName, this.mTier, this.mDescriptionArray, this.mTextures);
    }

    @Override
    public void saveNBTData(NBTTagCompound nbt) {
        super.saveNBTData(nbt);
        nbt.setBoolean("additionalConnection", additionalConnection);
        this.visSourceInfo.writeToNBT(nbt, "visSourceInfo");
    }

    @Override
    public void loadNBTData(NBTTagCompound nbt) {
        super.loadNBTData(nbt);
        additionalConnection = nbt.getBoolean("additionalConnection");
        this.visSourceInfo.readFromNBT(nbt, "visSourceInfo");
    }

    @Override
    public IGridNode getGridNode(ForgeDirection dir) {
        return getProxy().getNode();
    }

    @Override
    public AECableType getCableConnectionType(ForgeDirection forgeDirection) {
        return (additionalConnection || getBaseMetaTileEntity().getFrontFacing() == forgeDirection) ? AECableType.SMART : AECableType.NONE;
    }

    private void updateValidGridProxySides() {
        if (additionalConnection) {
            getProxy().setValidSides(EnumSet.complementOf(EnumSet.of(ForgeDirection.UNKNOWN)));
        } else {
            getProxy().setValidSides(EnumSet.of(getBaseMetaTileEntity().getFrontFacing()));
        }
        IGridNode node = getProxy().getNode();
        if (node != null) node.updateState();
    }

    @Override
    public void onFacingChange() {
        super.onFacingChange();
        updateValidGridProxySides();
    }

    @Override
    public void securityBreak() {}

    @Override
    public boolean onWireCutterRightClick(ForgeDirection side, ForgeDirection wrenchingSide, EntityPlayer aPlayer,
        float aX, float aY, float aZ, ItemStack aTool) {
        if (aPlayer.isSneaking()) {
            IGregTechTileEntity te = getBaseMetaTileEntity();
            aPlayer.openGui(
                AppEng.instance(),
                GuiBridge.GUI_RENAMER.ordinal() << 5 | (side.ordinal()),
                te.getWorld(),
                te.getXCoord(),
                te.getYCoord(),
                te.getZCoord());
            return true;
        }

        additionalConnection = !additionalConnection;
        updateValidGridProxySides();
        aPlayer.addChatComponentMessage(
            new ChatComponentTranslation("GT5U.hatch.additionalConnection." + additionalConnection));
        return true;
    }

    @Override
    public boolean connectsToAllSides() {
        return additionalConnection;
    }

    @Override
    public void setConnectsToAllSides(boolean connects) {
        additionalConnection = connects;
        updateValidGridProxySides();
    }

    @Override
    public AENetworkProxy getProxy() {
        if (gridProxy == null) {
            gridProxy = new AENetworkProxy(this, "proxy", ItemList.MagicalMaintenanceHatchME.get(1), true);
            gridProxy.setFlags(GridFlags.REQUIRE_CHANNEL);
            updateValidGridProxySides();
            if (getBaseMetaTileEntity().getWorld() != null) gridProxy.setOwner(
                getBaseMetaTileEntity().getWorld()
                    .getPlayerEntityByName(getBaseMetaTileEntity().getOwnerName()));
        }

        return this.gridProxy;
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(
            getBaseMetaTileEntity().getWorld(),
            getBaseMetaTileEntity().getXCoord(),
            getBaseMetaTileEntity().getYCoord(),
            getBaseMetaTileEntity().getZCoord());
    }

    @Override
    public int fillIfBelowCap(int buffer, Aspect aspect) {
        IDigiVisSource visSource = null;

        if (buffer >= centiVisCap) return buffer;
        int space = centiVisCap - buffer;
        int drained = 0;

        if (!this.getProxy()
            .isReady()) {
            return buffer;
        }

        try {
            visSource = this.visSourceInfo.tryGetSource(
                this.getProxy()
                    .getGrid());
        } catch (GridAccessException ignored) {}

        if (visSource != null) drained = visSource.consumeVis(aspect, Math.min(space, 5));

        return buffer + drained;
    }

    // Code was taken from Thaumic Energistics TileArcaneAssembler
    public void onMemoryCardActivate(final EntityPlayer player, final IMemoryCard memoryCard,
        final ItemStack playerHolding) {
        // Get the stored name
        String settingsName = memoryCard.getSettingsName(playerHolding);

        if (settingsName.equals(DigiVisSourceData.SOURCE_UNLOC_NAME)) {
            // Get the data
            NBTTagCompound data = memoryCard.getData(playerHolding);

            this.visSourceInfo.readFromNBT(data);

            // Ensure there is valid data
            if (this.visSourceInfo.hasSourceData()) {
                // Inform the user
                memoryCard.notifyUser(player, MemoryCardMessages.SETTINGS_LOADED);
            }

            // Mark that we need to save
            this.markDirty();
        }
        // Is the memory card empty?
        else if (settingsName.equals("gui.appliedenergistics2.Blank")) {
            // Clear the source info
            this.visSourceInfo.clearData();

            // Inform the user
            memoryCard.notifyUser(player, MemoryCardMessages.SETTINGS_CLEARED);

            // Mark dirty
            this.markDirty();
        }
    }

    @Override
    public boolean onRightclick(IGregTechTileEntity tileEntity, EntityPlayer player, ForgeDirection side, float X,
        float Y, float Z) {
        if ((player != null) && (player.getHeldItem() != null)
            && (player.getHeldItem()
                .getItem() instanceof IMemoryCard)) {
            onMemoryCardActivate(
                player,
                ((IMemoryCard) player.getHeldItem()
                    .getItem()),
                player.getHeldItem());
        }
        return super.onRightclick(tileEntity, player, side, X, Y, Z);
    }
}
