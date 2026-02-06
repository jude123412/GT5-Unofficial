package gregtech.common.tileentities.machines.draconic;

import static com.gtnewhorizon.gtnhlib.util.numberformatting.NumberFormatUtil.formatNumber;
import static gregtech.api.enums.GTValues.V;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAYS_ENERGY_IN;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAYS_ENERGY_OUT;
import static gregtech.api.util.GTModHandler.getModItem;

import com.gtnewhorizon.gtnhlib.util.numberformatting.NumberFormatUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Mods;
import gregtech.common.blocks.BlockEnergyPylon;
import gregtech.common.tileentities.render.TileEntityEnergyPylon;
import net.minecraft.util.EnumChatFormatting;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.brandon3055.draconicevolution.client.handler.ParticleHandler;
import com.brandon3055.draconicevolution.client.render.particle.Particles;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;
import com.gtnewhorizons.modularui.common.widget.SlotGroup;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.VoltageIndex;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.util.GTUtility;
import gregtech.api.util.tooltip.TooltipHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.brandon3055.draconicevolution.common.ModBlocks;
import com.brandon3055.draconicevolution.common.blocks.multiblock.MultiblockHelper;
import com.brandon3055.draconicevolution.common.blocks.multiblock.MultiblockHelper.TileLocation;
import com.brandon3055.draconicevolution.common.tileentities.multiblocktiles.TileEnergyStorageCore;

import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.modularui.IAddUIWidgets;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTETieredMachineBlock;
import gregtech.api.render.TextureFactory;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

public class MTEEnergyPylon extends MTETieredMachineBlock implements IAddUIWidgets {

    private final List<MultiblockHelper.TileLocation> coreLocations = new ArrayList<>();
    private int selectedCore = 0;
    private long mCoreMaxAmperage = 0;
    private int mCoreVoltageTier = 0;
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
            2,
            new String[] {
                "Inserts or Extracts energy from Draconic Evolution's Energy Core.",
                "Send -> Receive (Use a soft mallet to change mode).",
                " ",
                "Has and internal buffer based on this formula,",
                "(Voltage * Amperage) * 200.",
                " ",
                "Voltage is based on what tier of field generator is placed within the device.",
                "Amperage is based on how many cores are placed within in the device.",
                TooltipHelper.coloredText("Draconic Core, ", EnumChatFormatting.AQUA) + TooltipHelper.coloredText(NumberFormatUtil.formatNumber(1), EnumChatFormatting.AQUA) + " Amp per core.",
                TooltipHelper.coloredText("Wyvern Core, ", EnumChatFormatting.DARK_PURPLE) + TooltipHelper.coloredText(NumberFormatUtil.formatNumber(4), EnumChatFormatting.DARK_PURPLE) + " Amps per core.",
                TooltipHelper.coloredText("Awakened Core, ", EnumChatFormatting.GOLD) + TooltipHelper.coloredText(NumberFormatUtil.formatNumber(256), EnumChatFormatting.GOLD) + " Amps per core.",
                TooltipHelper.coloredText("Chaotic Core, ", EnumChatFormatting.DARK_GRAY) + TooltipHelper.coloredText(NumberFormatUtil.formatNumber(16384), EnumChatFormatting.DARK_GRAY) + " Amps per core.",
                " ",
            });
    }

    public MTEEnergyPylon(String aName, int aTier, int aSlotCount, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, aSlotCount, aDescription, aTextures);
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
            this.mInventory.length,
            this.mDescriptionArray,
            this.mTextures);
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        aNBT.setInteger("mSelectedCore", selectedCore);
        aNBT.setInteger("mCoreMaxVoltageTier", mCoreVoltageTier);
        aNBT.setBoolean("mFoundCore", foundCore);
        aNBT.setFloat("mModelScale", modelScale);
        aNBT.setFloat("mModelRotation", modelRotation);
        aNBT.setLong("mCoreMaxAmperage", mCoreMaxAmperage);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        if (aNBT.hasKey("mSelectedCore")) selectedCore = aNBT.getInteger("mSelectedCore");
        if (aNBT.hasKey("mCoreMaxVoltageTier")) mCoreVoltageTier = aNBT.getInteger("mCoreMaxVoltageTier");
        if (aNBT.hasKey("mFoundCore")) foundCore = aNBT.getBoolean("mFoundCore");
        if (aNBT.hasKey("mModelScale")) modelScale = aNBT.getFloat("mModelScale");
        if (aNBT.hasKey("mModelRotation")) modelRotation = aNBT.getFloat("mModelRotation");
        if (aNBT.hasKey("mCoreMaxAmperage")) mCoreMaxAmperage = aNBT.getLong("mCoreMaxAmperage");
    }

    @Override
    public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
        openGui(aPlayer);
        return true;
    }

    @Override
    public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext buildContext) {
        builder.widget(
            SlotGroup.ofItemHandler(inventoryHandler, 1)
                .startFromSlot(0)
                .endAtSlot(0)
                .slotCreator(index -> new BaseSlot(inventoryHandler, 0) {
                    @Override
                    public int getSlotStackLimit() {
                        return 64;
                    }
                })
                .background(getGUITextureSet().getItemSlot(), GTUITextures.OVERLAY_SLOT_DRACONIC_CORE)
                .build()
                .setPos(79, 25));
        builder.widget(
            SlotGroup.ofItemHandler(inventoryHandler, 1)
                .startFromSlot(0)
                .endAtSlot(0)
                .slotCreator(index -> new BaseSlot(inventoryHandler, 1) {
                    @Override
                    public int getSlotStackLimit() {
                        return 1;
                    }
                })
                .background(getGUITextureSet().getItemSlot(), GTUITextures.OVERLAY_SLOT_FIELD_GENERATOR)
                .build()
                .setPos(79, 43));
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        if (aBaseMetaTileEntity.isClientSide()) {
            modelRotation += 1.5F;
            modelScale += !aBaseMetaTileEntity.isAllowedToWork() ? 0.01F : -0.01F;
            if (modelScale < 0) {
                modelScale = !aBaseMetaTileEntity.isAllowedToWork() ? 0F : 10000F;
            }
        }

        if (aBaseMetaTileEntity.isServerSide()) {
            if (foundCore) {
                spawnParticles();
                placeRenderTile();
            }
            if (foundCore && particleRate > 0) particleRate--;
            if (aTick % 400 == 0) nextCore();
            syncEnergy(aBaseMetaTileEntity);
            setFoundCore();
            updateAmperageFromCoreItem();
            updateVoltageTierFromFieldGenerator();
        }

        super.onPostTick(aBaseMetaTileEntity, aTick);
    }

    @Override
    public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {
        nextCore();
        super.onFirstTick(aBaseMetaTileEntity);
    }

    public void placeRenderTile() {
        World world = getBaseMetaTileEntity().getWorld();
        int x = getBaseMetaTileEntity().getXCoord();
        int y = getBaseMetaTileEntity().getYCoord() + 1;
        int z = getBaseMetaTileEntity().getZCoord();

        if (world.isAirBlock(x, y, z) || world.getBlock(x, y, z).isReplaceable(world, x, y, z) && !(world.getBlock(x, y, z) instanceof BlockEnergyPylon)) {
            world.setBlock(x, y, z, GregTechAPI.sEnergyPylonRender);
        }
    }


    public float getModelScale() {
        return modelScale;
    }

    public float getModelRotation() {
        return modelRotation;
    }

    @SideOnly(Side.CLIENT)
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

    private TileEnergyStorageCore getMaster() {
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

    private void nextCore() {
        findCores();
        selectedCore++;
        if (selectedCore >= coreLocations.size()) selectedCore = 0;
        getWorld().markBlockForUpdate(getXCoord(), getYCoord(), getZCoord());
    }

    private void setFoundCore() {
        TileEnergyStorageCore core = getMaster();
        foundCore = !coreLocations.isEmpty() && core != null && core.isOnline();
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
        if (foundCore) {
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
        } else {
            mCoreEU = 0;
            mMaxCoreEu = 0;
        }
    }

    private void extractEnergy(long requested, TileEnergyStorageCore core) {
        long stored = getBaseMetaTileEntity().getStoredEU();
        long space = getBaseMetaTileEntity().getEUCapacity() - stored;
        if (space <= 0 || requested <= 0) return;

        long coreAvailable = mCoreEU;
        long remaining = Math.min(requested, Math.min(coreAvailable, space));

        if (remaining <= 0) return;

        // Particle rate based on total transfer
        particleRate = calculateParticleRate(remaining);

        long transferred = 0;

        while (remaining > 0) {
            int packet = (int) Math.min(Integer.MAX_VALUE, remaining);

            int extracted = core.extractEnergy(packet, false);
            if (extracted <= 0) break;

            transferred += extracted;
            remaining -= extracted;

            if (extracted < packet) break; // core hit its limit
        }

        if (transferred > 0) {
            this.setEUVar(stored + transferred);
        }
    }

    private void receiveEnergy(long requested, TileEnergyStorageCore core) {
        long stored = getBaseMetaTileEntity().getStoredEU();
        if (stored <= 0 || requested <= 0) return;

        long coreSpace = mMaxCoreEu - mCoreEU;
        if (coreSpace <= 0) return;

        long remaining = Math.min(requested, Math.min(stored, coreSpace));

        if (remaining <= 0) return;

        // Particle rate based on total transfer
        particleRate = calculateParticleRate(remaining);

        long transferred = 0;

        while (remaining > 0) {
            int packet = (int) Math.min(Integer.MAX_VALUE, remaining);

            int received = core.receiveEnergy(packet, false);
            if (received <= 0) break;

            transferred += received;
            remaining -= received;

            if (received < packet) break; // core hit its limit
        }

        if (transferred > 0) {
            this.setEUVar(stored - transferred);
        }
    }

    private byte calculateParticleRate(long remaining) {
        int rate = remaining < 500 ? 1 : (int) (remaining / 500);
        if (rate > 20) rate = 20;
        return (byte) rate;
    }

    public long getmCoreEU() {
        return mCoreEU;
    }

    public long getmMaxCoreEu() {
        return mMaxCoreEu;
    }

    private void updateAmperageFromCoreItem() {
        if (mInventory[0] != null) {
            if (mInventory[0].isItemEqual(getModItem(Mods.DraconicEvolution.ID, "draconicCore", 1L, 0)))
                mCoreMaxAmperage = mInventory[0].stackSize;
            else if (mInventory[0].isItemEqual(getModItem(Mods.DraconicEvolution.ID, "wyvernCore", 1L, 0)))
                mCoreMaxAmperage = mInventory[0].stackSize * 4L;
            else if (mInventory[0].isItemEqual(getModItem(Mods.DraconicEvolution.ID, "awakenedCore", 1L, 0)))
                mCoreMaxAmperage = mInventory[0].stackSize * 256L;
            else if (mInventory[0].isItemEqual(getModItem(Mods.DraconicEvolution.ID, "chaoticCore", 1L, 0)))
                mCoreMaxAmperage = mInventory[0].stackSize * 16384L;
        } else {
            mCoreMaxAmperage = 0;
        }
    }

    private void updateVoltageTierFromFieldGenerator() {
        if (mInventory[1] != null) {
            if (mInventory[1].isItemEqual(ItemList.Field_Generator_LV.get(1))) mCoreVoltageTier = VoltageIndex.LV;
            else if (mInventory[1].isItemEqual(ItemList.Field_Generator_MV.get(1))) mCoreVoltageTier = VoltageIndex.MV;
            else if (mInventory[1].isItemEqual(ItemList.Field_Generator_HV.get(1))) mCoreVoltageTier = VoltageIndex.HV;
            else if (mInventory[1].isItemEqual(ItemList.Field_Generator_EV.get(1))) mCoreVoltageTier = VoltageIndex.EV;
            else if (mInventory[1].isItemEqual(ItemList.Field_Generator_IV.get(1))) mCoreVoltageTier = VoltageIndex.IV;
            else if (mInventory[1].isItemEqual(ItemList.Field_Generator_LuV.get(1))) mCoreVoltageTier = VoltageIndex.LuV;
            else if (mInventory[1].isItemEqual(ItemList.Field_Generator_ZPM.get(1))) mCoreVoltageTier = VoltageIndex.ZPM;
            else if (mInventory[1].isItemEqual(ItemList.Field_Generator_UV.get(1))) mCoreVoltageTier = VoltageIndex.UV;
            else if (mInventory[1].isItemEqual(ItemList.Field_Generator_UHV.get(1))) mCoreVoltageTier = VoltageIndex.UHV;
            else if (mInventory[1].isItemEqual(ItemList.Field_Generator_UEV.get(1))) mCoreVoltageTier = VoltageIndex.UEV;
            else if (mInventory[1].isItemEqual(ItemList.Field_Generator_UIV.get(1))) mCoreVoltageTier = VoltageIndex.UIV;
            else if (mInventory[1].isItemEqual(ItemList.Field_Generator_UMV.get(1))) mCoreVoltageTier = VoltageIndex.UMV;
            else if (mInventory[1].isItemEqual(ItemList.Field_Generator_UXV.get(1))) mCoreVoltageTier = VoltageIndex.UXV;
            else if (mInventory[1].isItemEqual(ItemList.Field_Generator_MAX.get(1))) mCoreVoltageTier = VoltageIndex.MAX;
        } else {
            mCoreVoltageTier = 0;
        }
    }

    @Override
    public void getWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y,
        int z) {
        tag.setBoolean("foundCore", foundCore);
        tag.setBoolean("mode", getBaseMetaTileEntity().isAllowedToWork());
        tag.setByte("CoreVoltageTier", (byte) mCoreVoltageTier);
        tag.setLong("coreEu", mCoreEU);
        tag.setLong("maxCoreEu", mMaxCoreEu);
        tag.setLong("storedEu", getBaseMetaTileEntity().getStoredEU());
        tag.setLong("maxStoredEu", getBaseMetaTileEntity().getEUCapacity());
        tag.setLong("AvgIn", getBaseMetaTileEntity().getAverageElectricInput());
        tag.setLong("AvgOut", getBaseMetaTileEntity().getAverageElectricOutput());
        super.getWailaNBTData(player, tile, tag, world, x, y, z);
    }

    @Override
    public void getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        final NBTTagCompound tag = accessor.getNBTData();
        if (tag.hasKey("foundCore")) currenttip.add(
            tag.getBoolean("foundCore")
                ? EnumChatFormatting.GREEN + StatCollector.translateToLocal("GT5U.waila.generating.foundCore")
                : EnumChatFormatting.RED + StatCollector.translateToLocal("GT5U.waila.generating.missingCore"));
        if (tag.hasKey("mode")) currenttip.add(
            tag.getBoolean("mode")
                ? EnumChatFormatting.GREEN + StatCollector.translateToLocal("GT5U.waila.generating.exportMode")
                : EnumChatFormatting.RED + StatCollector.translateToLocal("GT5U.waila.generating.importMode"));
        if (tag.hasKey("coreEu") && tag.hasKey("maxCoreEu")) currenttip.add(
            EnumChatFormatting.GREEN + formatNumber(tag.getLong("coreEu"))
                + EnumChatFormatting.GRAY
                + " / "
                + EnumChatFormatting.YELLOW
                + formatNumber(tag.getLong("maxCoreEu"))
                + EnumChatFormatting.GRAY
                + " EU");
        if (tag.hasKey("storedEu") && tag.hasKey("maxStoredEu")) currenttip.add(
            EnumChatFormatting.GREEN + formatNumber(tag.getLong("storedEu"))
                + EnumChatFormatting.GRAY
                + " / "
                + EnumChatFormatting.YELLOW
                + formatNumber(tag.getLong("maxStoredEu"))
                + EnumChatFormatting.GRAY
                + " EU");
        if (tag.hasKey("AvgIn") && tag.hasKey("CoreVoltageTier")) currenttip.add(
            StatCollector.translateToLocalFormatted(
                "GT5U.waila.energy.avg_in_with_amperage",
                formatNumber(tag.getLong("AvgIn")),
                GTUtility.getAmperageForTier(tag.getLong("AvgIn"), tag.getByte("CoreVoltageTier")),
                GTUtility.getColoredTierNameFromTier(tag.getByte("CoreVoltageTier"))));
        if (tag.hasKey("AvgOut") && tag.hasKey("CoreVoltageTier")) currenttip.add(
            StatCollector.translateToLocalFormatted(
                "GT5U.waila.energy.avg_out_with_amperage",
                formatNumber(tag.getLong("AvgOut")),
                GTUtility.getAmperageForTier(tag.getLong("AvgOut"), tag.getByte("CoreVoltageTier")),
                GTUtility.getColoredTierNameFromTier(tag.getByte("CoreVoltageTier"))));
        super.getWailaBody(itemStack, currenttip, accessor, config);
    }

    @Override
    public long maxEUStore() {
        TileEnergyStorageCore core = getMaster();
        if (core == null || !core.isOnline()) return 0;
        long voltage = mCoreVoltageTier > 0 ? V[mCoreVoltageTier] : 0;
        long amperage = mCoreMaxAmperage;
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
        if (mCoreVoltageTier > 0 && getBaseMetaTileEntity().isAllowedToWork()) {
            return V[mCoreVoltageTier];
        } else return 0;
    }

    @Override
    public long maxEUInput() {
        if (mCoreVoltageTier > 0 && !getBaseMetaTileEntity().isAllowedToWork()) {
            return V[mCoreVoltageTier];
        } else return 0;
    }

    @Override
    public long maxAmperesOut() {
        return getBaseMetaTileEntity().isAllowedToWork() ? mCoreMaxAmperage : 0;
    }

    @Override
    public long maxAmperesIn() {
        return getBaseMetaTileEntity().isAllowedToWork() ? 0 : mCoreMaxAmperage;
    }

    @Override
    public long getInputTier() {
        return mCoreVoltageTier;
    }

    @Override
    public long getOutputTier() {
        return mCoreVoltageTier;
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
}
