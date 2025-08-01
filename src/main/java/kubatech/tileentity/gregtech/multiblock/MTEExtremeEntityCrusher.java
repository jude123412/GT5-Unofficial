/*
 * spotless:off
 * KubaTech - Gregtech Addon
 * Copyright (C) 2022 - 2024  kuba6000
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see <https://www.gnu.org/licenses/>.
 * spotless:on
 */

package kubatech.tileentity.gregtech.multiblock;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.isAir;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.onElementPass;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;
import static gregtech.api.enums.HatchElement.Energy;
import static gregtech.api.enums.HatchElement.Maintenance;
import static gregtech.api.enums.HatchElement.OutputBus;
import static gregtech.api.enums.HatchElement.OutputHatch;
import static gregtech.api.enums.Mods.BloodMagic;
import static gregtech.api.enums.Mods.ExtraUtilities;
import static gregtech.api.enums.Mods.InfernalMobs;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_DISTILLATION_TOWER;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_DISTILLATION_TOWER_ACTIVE;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_DISTILLATION_TOWER_ACTIVE_GLOW;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_DISTILLATION_TOWER_GLOW;
import static gregtech.api.metatileentity.BaseTileEntity.TOOLTIP_DELAY;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static gregtech.api.util.GTStructureUtility.chainAllGlasses;
import static gregtech.api.util.GTStructureUtility.ofFrame;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizon.structurelib.alignment.IAlignmentLimits;
import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.gtnewhorizons.modularui.api.drawable.Text;
import com.gtnewhorizons.modularui.api.drawable.UITexture;
import com.gtnewhorizons.modularui.api.forge.ItemStackHandler;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.CycleButtonWidget;
import com.gtnewhorizons.modularui.common.widget.DynamicPositionedRow;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.kuba6000.mobsinfo.api.utils.FastRandom;
import com.mojang.authlib.GameProfile;

import WayofTime.alchemicalWizardry.api.alchemy.energy.ReagentRegistry;
import WayofTime.alchemicalWizardry.api.event.RitualRunEvent;
import WayofTime.alchemicalWizardry.api.rituals.Rituals;
import WayofTime.alchemicalWizardry.api.soulNetwork.SoulNetworkHandler;
import WayofTime.alchemicalWizardry.api.tile.IBloodAltar;
import WayofTime.alchemicalWizardry.common.rituals.RitualEffectWellOfSuffering;
import WayofTime.alchemicalWizardry.common.tileEntity.TEMasterStone;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.EnderIO;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.Materials;
import gregtech.api.enums.Mods;
import gregtech.api.enums.SoundResource;
import gregtech.api.enums.Textures;
import gregtech.api.enums.VoltageIndex;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchEnergy;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.recipe.check.SimpleCheckRecipeResult;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTUtility;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.common.misc.GTStructureChannels;
import kubatech.Tags;
import kubatech.api.implementations.KubaTechGTMultiBlockBase;
import kubatech.api.tileentity.CustomTileEntityPacketHandler;
import kubatech.api.utils.ModUtils;
import kubatech.client.effect.EntityRenderer;
import kubatech.config.Config;
import kubatech.loaders.MobHandlerLoader;
import kubatech.network.CustomTileEntityPacket;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

public class MTEExtremeEntityCrusher extends KubaTechGTMultiBlockBase<MTEExtremeEntityCrusher>
    implements CustomTileEntityPacketHandler, ISurvivalConstructable {

    public static final double DIAMOND_SPIKES_DAMAGE = 9d;
    // Powered spawner with octadic capacitor spawns ~22/min ~= 0.366/sec ~= 2.72s/spawn ~= 54.54t/spawn
    public static final int MOB_SPAWN_INTERVAL = 55;
    public final Random rand = new FastRandom();
    private final WeaponCache weaponCache;
    private EECEventHandler eventHandler;

    @SuppressWarnings("unused")
    public MTEExtremeEntityCrusher(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
        weaponCache = new WeaponCache(mInventory);
    }

    public MTEExtremeEntityCrusher(String aName) {
        super(aName);
        weaponCache = new WeaponCache(mInventory);
        if (BloodMagic.isModLoaded() && FMLCommonHandler.instance()
            .getEffectiveSide()
            .isServer()) {
            eventHandler = new EECEventHandler();
            MinecraftForge.EVENT_BUS.register(eventHandler);
        }
    }

    @Override
    public void onRemoval() {
        if (eventHandler != null) MinecraftForge.EVENT_BUS.unregister(eventHandler);
        if (getBaseMetaTileEntity().isClientSide() && entityRenderer != null) {
            entityRenderer.setDead();
        }
    }

    @Override
    public void onUnload() {
        if (eventHandler != null) MinecraftForge.EVENT_BUS.unregister(eventHandler);
    }

    private static final String WellOfSufferingRitualName = "AW013Suffering";

    private static final Item poweredSpawnerItem = Item.getItemFromBlock(EnderIO.blockPoweredSpawner);
    private static final int CASING_INDEX = 16;
    private static final String STRUCTURE_PIECE_MAIN = "main";
    private static final IStructureDefinition<MTEExtremeEntityCrusher> STRUCTURE_DEFINITION = StructureDefinition
        .<MTEExtremeEntityCrusher>builder()
        .addShape(
            STRUCTURE_PIECE_MAIN,
            transpose(
                new String[][] { // spotless:off
                    {"ccccc", "ccccc", "ccccc", "ccccc", "ccccc"},
                    {"fgggf", "g---g", "g---g", "g---g", "fgggf"},
                    {"fgggf", "g---g", "g---g", "g---g", "fgggf"},
                    {"fgggf", "g---g", "g---g", "g---g", "fgggf"},
                    {"fgggf", "g---g", "g---g", "g---g", "fgggf"},
                    {"fgggf", "gsssg", "gsssg", "gsssg", "fgggf"},
                    {"CC~CC", "CCCCC", "CCCCC", "CCCCC", "CCCCC"},
                })) // spotless:on
        .addElement('c', onElementPass(t -> t.mCasing++, ofBlock(GregTechAPI.sBlockCasings2, 0)))
        .addElement(
            'C',
            buildHatchAdder(MTEExtremeEntityCrusher.class).atLeast(OutputBus, OutputHatch, Energy, Maintenance)
                .casingIndex(CASING_INDEX)
                .dot(1)
                .buildAndChain(onElementPass(t -> t.mCasing++, ofBlock(GregTechAPI.sBlockCasings2, 0))))
        .addElement('g', chainAllGlasses(-1, (te, t) -> te.glassTier = t, te -> te.glassTier))
        .addElement('f', ofFrame(Materials.Steel))
        .addElement(
            's',
            ExtraUtilities.isModLoaded() ? ofBlock(Block.getBlockFromName("ExtraUtilities:spike_base_diamond"), 0)
                : isAir())
        .build();

    private TileEntity masterStoneRitual = null;
    private TileEntity tileAltar = null;
    private boolean isInRitualMode = false;
    private int mCasing = 0;
    private int glassTier = -1;
    private boolean mAnimationEnabled = true;
    private boolean mIsProducingInfernalDrops = true;
    private boolean voidAllDamagedAndEnchantedItems = false;

    private EntityRenderer entityRenderer = null;
    private boolean renderEntity = false;
    public EECFakePlayer EECPlayer = null;

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setBoolean("isInRitualMode", isInRitualMode);
        aNBT.setBoolean("mAnimationEnabled", mAnimationEnabled);
        aNBT.setBoolean("mIsProducingInfernalDrops", mIsProducingInfernalDrops);
        aNBT.setBoolean("voidAllDamagedAndEnchantedItems", voidAllDamagedAndEnchantedItems);
        if (weaponCache.getStackInSlot(0) != null) aNBT.setTag(
            "weaponCache",
            weaponCache.getStackInSlot(0)
                .writeToNBT(new NBTTagCompound()));
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        isInRitualMode = aNBT.getBoolean("isInRitualMode");
        mAnimationEnabled = !aNBT.hasKey("mAnimationEnabled") || aNBT.getBoolean("mAnimationEnabled");
        mIsProducingInfernalDrops = !aNBT.hasKey("mIsProducingInfernalDrops")
            || aNBT.getBoolean("mIsProducingInfernalDrops");
        voidAllDamagedAndEnchantedItems = aNBT.getBoolean("voidAllDamagedAndEnchantedItems");
        if (aNBT.hasKey("weaponCache"))
            weaponCache.setStackInSlot(0, ItemStack.loadItemStackFromNBT(aNBT.getCompoundTag("weaponCache")));
    }

    @Override
    public boolean isOverclockingInfinite() {
        return true;
    }

    @Override
    protected int getOverclockTimeLimit() {
        return (batchMode ? 16 : 1) * 20;
    }

    @Override
    public IStructureDefinition<MTEExtremeEntityCrusher> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    @Override
    protected IAlignmentLimits getInitialAlignmentLimits() {
        return (d, r, f) -> d.offsetY == 0 && r.isNotRotated();
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType("Powered Spawner, EEC")
            .addInfo("Spawns and kills monsters for you.")
            .addInfo("You have to insert the powered spawner in the controller.")
            .addInfo("Base energy usage: 1,920 EU/t")
            .addInfo("Supports perfect OC, minimum time: 20 ticks, after that multiplies the outputs.")
            .addInfo("Recipe time is based on mob health.")
            .addInfo("You can additionally put a weapon inside the GUI.")
            .addInfo("It will speed up the process and apply the looting level from the weapon (maximum 4 levels).")
            .addInfo(EnumChatFormatting.RED + "Enchanting the spikes inside the structure does nothing!")
            .addInfo("Also produces 120 Liquid XP per operation.")
            .addInfo("If the mob spawns infernal, it will drain 8 times more power.")
            .addInfo("You can prevent infernal spawns by shift clicking with a screwdriver.")
            .addInfo("Note: If the mob has forced infernal spawn, it will do it anyway.")
            .addInfo("You can enable ritual mode with a screwdriver.")
            .addInfo(
                "When in ritual mode and the Well Of Suffering ritual is built directly centered on top of the machine,")
            .addInfo("the mobs will start to buffer and die very slowly by the ritual.")
            .addInfo("You can disable mob animation with a soldering iron.")
            .addInfo("You can enable batch mode with wire cutters." + EnumChatFormatting.BLUE + " 16x Time 16x Output")
            .addGlassEnergyLimitInfo(VoltageIndex.UV)
            .beginStructureBlock(5, 7, 5, true)
            .addController("Front Bottom Center")
            .addCasingInfoMin("Solid Steel Machine Casing", 35, false)
            .addCasingInfoExactly("Any Tiered Glass", 60, false)
            .addCasingInfoExactly("Steel Frame Box", 20, false)
            .addCasingInfoExactly("Diamond Spike", 9, false)
            .addOutputBus("Any bottom casing", 1)
            .addOutputHatch("Any bottom casing", 1)
            .addEnergyHatch("Any bottom casing", 1)
            .addMaintenanceHatch("Any bottom casing", 1)
            .addSubChannelUsage(GTStructureChannels.BOROGLASS)
            .toolTipFinisher(GTValues.AuthorKuba);
        return tt;
    }

    @Override
    public void construct(ItemStack itemStack, boolean b) {
        buildPiece(STRUCTURE_PIECE_MAIN, itemStack, b, 2, 6, 0);
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        return survivalBuildPiece(STRUCTURE_PIECE_MAIN, stackSize, 2, 6, 0, elementBudget, env, true, true);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTEExtremeEntityCrusher(this.mName);
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, ForgeDirection side, ForgeDirection facing,
        int colorIndex, boolean aActive, boolean aRedstone) {
        if (side == facing) {
            if (aActive) return new ITexture[] { Textures.BlockIcons.getCasingTextureForId(CASING_INDEX),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_DISTILLATION_TOWER_ACTIVE)
                    .extFacing()
                    .build(),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_DISTILLATION_TOWER_ACTIVE_GLOW)
                    .extFacing()
                    .glow()
                    .build() };
            return new ITexture[] { Textures.BlockIcons.getCasingTextureForId(CASING_INDEX), TextureFactory.builder()
                .addIcon(OVERLAY_FRONT_DISTILLATION_TOWER)
                .extFacing()
                .build(),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_DISTILLATION_TOWER_GLOW)
                    .extFacing()
                    .glow()
                    .build() };
        }
        return new ITexture[] { Textures.BlockIcons.getCasingTextureForId(CASING_INDEX) };
    }

    @SideOnly(Side.CLIENT)
    private void setupEntityRenderer(IGregTechTileEntity aBaseMetaTileEntity, int time) {
        if (entityRenderer == null) {
            ChunkCoordinates coords = this.getBaseMetaTileEntity()
                .getCoords();
            int[] abc = new int[] { 0, -2, 2 };
            int[] xyz = new int[] { 0, 0, 0 };
            this.getExtendedFacing()
                .getWorldOffset(abc, xyz);
            xyz[0] += coords.posX;
            xyz[1] += coords.posY;
            xyz[2] += coords.posZ;
            entityRenderer = new EntityRenderer(aBaseMetaTileEntity.getWorld(), xyz[0], xyz[1], xyz[2], time);
        } else {
            entityRenderer.setDead();
            entityRenderer = new EntityRenderer(entityRenderer, time);
        }
        Minecraft.getMinecraft().effectRenderer.addEffect(entityRenderer);
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        super.onPostTick(aBaseMetaTileEntity, aTick);
        if (aBaseMetaTileEntity.isClientSide()) {
            if (renderEntity && aBaseMetaTileEntity.isActive() && aTick % 40 == 0) {
                setupEntityRenderer(aBaseMetaTileEntity, 40);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void HandleCustomPacket(CustomTileEntityPacket message) {
        if (message.getDataBoolean() && Mods.MobsInfo.isModLoaded()) {
            renderEntity = true;
            String mobType = message.getDataString();
            MobHandlerLoader.MobEECRecipe r = MobHandlerLoader.recipeMap.get(mobType);
            if (r != null) {
                if (entityRenderer == null) setupEntityRenderer(getBaseMetaTileEntity(), 40);
                entityRenderer.setEntity(r.entityCopy);
            } else entityRenderer.setEntity(null);
        } else {
            renderEntity = false;
            if (entityRenderer != null) {
                entityRenderer.setDead();
                entityRenderer = null;
            }
        }
    }

    @Override
    public void onScrewdriverRightClick(ForgeDirection side, EntityPlayer aPlayer, float aX, float aY, float aZ,
        ItemStack aTool) {
        if (this.mMaxProgresstime > 0) {
            GTUtility.sendChatToPlayer(aPlayer, "Can't change mode when running !");
            return;
        }
        if (aPlayer.isSneaking()) {
            if (!InfernalMobs.isModLoaded()) return;
            mIsProducingInfernalDrops = !mIsProducingInfernalDrops;
            if (!mIsProducingInfernalDrops)
                GTUtility.sendChatToPlayer(aPlayer, "Mobs will now be prevented from spawning infernal");
            else GTUtility.sendChatToPlayer(aPlayer, "Mobs can spawn infernal now");
        } else {
            if (!BloodMagic.isModLoaded()) return;
            isInRitualMode = !isInRitualMode;
            if (!isInRitualMode) {
                GTUtility.sendChatToPlayer(aPlayer, "Ritual mode disabled");
            } else {
                GTUtility.sendChatToPlayer(aPlayer, "Ritual mode enabled");
                if (connectToRitual()) GTUtility.sendChatToPlayer(aPlayer, "Successfully connected to the ritual");
                else GTUtility.sendChatToPlayer(aPlayer, "Can't connect to the ritual");
            }
        }
    }

    @Override
    public boolean onSolderingToolRightClick(ForgeDirection side, ForgeDirection wrenchingSide, EntityPlayer aPlayer,
        float aX, float aY, float aZ, ItemStack aTool) {
        if (wrenchingSide == getBaseMetaTileEntity().getFrontFacing()) {
            mAnimationEnabled = !mAnimationEnabled;
            GTUtility.sendChatToPlayer(aPlayer, "Animations are " + (mAnimationEnabled ? "enabled" : "disabled"));
            return true;
        } else return super.onSolderingToolRightClick(side, wrenchingSide, aPlayer, aX, aY, aZ, aTool);
    }

    @Override
    public boolean onWireCutterRightClick(ForgeDirection side, ForgeDirection wrenchingSide, EntityPlayer aPlayer,
        float aX, float aY, float aZ, ItemStack aTool) {
        this.batchMode = !this.batchMode;
        GTUtility.sendChatToPlayer(aPlayer, "Batch Mode: " + (this.batchMode ? "Enabled" : "Disabled"));
        return true;
    }

    // We place the event handler in an inner
    // class to prevent high costs of registering
    // the event because forge event bus reflects
    // through all the methods and super of the class
    // in order to find the @SubscribeEvent annotations
    public class EECEventHandler {

        @SuppressWarnings("unused")
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void onRitualPerform(RitualRunEvent event) {
            if (!isInRitualMode) return;
            if (masterStoneRitual == null) return;
            if (mMaxProgresstime == 0) return;
            if (event.mrs.equals(masterStoneRitual) && event.ritualKey.equals(WellOfSufferingRitualName)) {
                Rituals ritual = Rituals.ritualMap.get(WellOfSufferingRitualName);
                if (ritual != null && ritual.effect instanceof RitualEffectWellOfSuffering effect) {
                    event.setCanceled(true); // we will handle that
                    String owner = event.mrs.getOwner();
                    int currentEssence = SoulNetworkHandler.getCurrentEssence(owner);
                    World world = event.mrs.getWorld();
                    int x = event.mrs.getXCoord();
                    int y = event.mrs.getYCoord();
                    int z = event.mrs.getZCoord();

                    if (world.getWorldTime() % RitualEffectWellOfSuffering.timeDelay != 0) return;

                    if (tileAltar == null || tileAltar.isInvalid()) {
                        tileAltar = null;
                        for (int i = -5; i <= 5; i++) for (int j = -5; j <= 5; j++) for (int k = -10; k <= 10; k++)
                            if (world.getTileEntity(x + i, y + k, z + j) instanceof IBloodAltar)
                                tileAltar = world.getTileEntity(x + i, y + k, z + j);
                    }
                    if (tileAltar == null) return;

                    if (currentEssence < effect.getCostPerRefresh() * 100) {
                        SoulNetworkHandler.causeNauseaToPlayer(owner);
                        return;
                    }

                    ((IBloodAltar) tileAltar).sacrificialDaggerCall(
                        100 * RitualEffectWellOfSuffering.amount
                            * (effect.canDrainReagent(
                                event.mrs,
                                ReagentRegistry.offensaReagent,
                                RitualEffectWellOfSuffering.offensaDrain,
                                true) ? 2 : 1)
                            * (effect.canDrainReagent(
                                event.mrs,
                                ReagentRegistry.tenebraeReagent,
                                RitualEffectWellOfSuffering.tennebraeDrain,
                                true) ? 2 : 1),
                        true);

                    SoulNetworkHandler.syphonFromNetwork(owner, effect.getCostPerRefresh() * 100);
                }
            }
        }
    }

    private CustomTileEntityPacket mobPacket = null;

    private static class WeaponCache extends ItemStackHandler {

        boolean isValid = false;
        int looting = 0;
        double attackDamage = 0;

        public WeaponCache(ItemStack[] inventory) {
            super(inventory);
        }

        @Override
        protected void onContentsChanged(int slot) {
            if (slot != 0) return;
            if (ModUtils.isClientThreaded()) return;
            ItemStack stack = getStackInSlot(0);
            if (stack == null) {
                isValid = false;
                return;
            }
            attackDamage = stack.getAttributeModifiers()
                .get(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName())
                .stream()
                .mapToDouble(
                    attr -> attr.getAmount()
                        + (double) EnchantmentHelper.func_152377_a(stack, EnumCreatureAttribute.UNDEFINED))
                .sum();
            looting = Math.min(4, EnchantmentHelper.getEnchantmentLevel(Enchantment.looting.effectId, stack));
            isValid = true;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return Enchantment.looting.canApply(stack);
        }
    }

    @Override
    public boolean isValidSlot(int aIndex) {
        return aIndex >= 0;
    }

    @SuppressWarnings("unlikely-arg-type")
    @Override
    @NotNull
    public CheckRecipeResult checkProcessing() {
        if (getBaseMetaTileEntity().isClientSide()) return CheckRecipeResultRegistry.NO_RECIPE;
        ItemStack aStack = mInventory[1];
        if (aStack == null) return SimpleCheckRecipeResult.ofFailure("EEC_nospawner");

        if (aStack.getItem() != poweredSpawnerItem) return SimpleCheckRecipeResult.ofFailure("EEC_nospawner");

        if (aStack.getTagCompound() == null) return SimpleCheckRecipeResult.ofFailure("EEC_invalidspawner");
        String mobType = aStack.getTagCompound()
            .getString("mobType");
        if (mobType.isEmpty()) return SimpleCheckRecipeResult.ofFailure("EEC_invalidspawner");

        if (mobType.equals("Skeleton") && getBaseMetaTileEntity().getWorld().provider instanceof WorldProviderHell
            && rand.nextInt(5) > 0) mobType = "witherSkeleton";

        MobHandlerLoader.MobEECRecipe recipe = MobHandlerLoader.recipeMap.get(mobType);

        if (recipe == null) return CheckRecipeResultRegistry.NO_RECIPE;
        if (!recipe.recipe.isPeacefulAllowed && this.getBaseMetaTileEntity()
            .getWorld().difficultySetting == EnumDifficulty.PEACEFUL && !Config.MobHandler.ignorePeacefulCheck)
            return SimpleCheckRecipeResult.ofFailure("EEC_peaceful");

        if (isInRitualMode && isRitualValid()) {
            if (getMaxInputEu() < recipe.mEUt / 4) return CheckRecipeResultRegistry.insufficientPower(recipe.mEUt / 4);
            this.mOutputFluids = new FluidStack[] { FluidRegistry.getFluidStack("xpjuice", 5000) };
            this.mOutputItems = recipe
                .generateOutputs(rand, this, 3, 0, mIsProducingInfernalDrops, voidAllDamagedAndEnchantedItems);
            this.lEUt /= 4L;
            this.mMaxProgresstime = 400;
        } else {
            if (getMaxInputEu() < recipe.mEUt) return CheckRecipeResultRegistry.insufficientPower(recipe.mEUt);
            if (recipe.recipe.alwaysinfernal && getMaxInputEu() < recipe.mEUt * 8)
                return CheckRecipeResultRegistry.insufficientPower(recipe.mEUt * 8);

            double attackDamage = DIAMOND_SPIKES_DAMAGE; // damage from spikes

            if (weaponCache.isValid) attackDamage += weaponCache.attackDamage;

            if (EECPlayer == null) EECPlayer = new EECFakePlayer(this);
            EECPlayer.currentWeapon = weaponCache.getStackInSlot(0);
            this.mOutputItems = recipe.generateOutputs(
                rand,
                this,
                attackDamage,
                weaponCache.isValid ? weaponCache.looting : 0,
                mIsProducingInfernalDrops,
                voidAllDamagedAndEnchantedItems);

            EECPlayer.currentWeapon = null;
            if (batchMode) {
                for (ItemStack item : mOutputItems) {
                    item.stackSize *= 16;
                }
                this.mMaxProgresstime *= 16;
            }
            this.mOutputFluids = new FluidStack[] {
                FluidRegistry.getFluidStack("xpjuice", 120 * (batchMode ? 16 : 1)) };
            ItemStack weapon = weaponCache.getStackInSlot(0);
            int times = this.calculatePerfectOverclock(this.lEUt, this.mMaxProgresstime);
            if (weaponCache.isValid && weapon.isItemStackDamageable()) {
                EECPlayer.currentWeapon = weapon;
                Item lootingHolderItem = weapon.getItem();
                for (int i = 0; i < times + 1; i++) {
                    if (!lootingHolderItem.hitEntity(weapon, recipe.recipe.entity, EECPlayer)) break;
                    if (weapon.stackSize == 0) {
                        weaponCache.setStackInSlot(0, null);
                        break;
                    }
                }
                EECPlayer.currentWeapon = null;
            }
        }
        if (this.lEUt > 0) this.lEUt = -this.lEUt;
        this.mEfficiency = (10000 - (getIdealStatus() - getRepairStatus()) * 1000);
        this.mEfficiencyIncrease = 10000;

        if (mobPacket == null) mobPacket = new CustomTileEntityPacket((TileEntity) this.getBaseMetaTileEntity(), null);
        mobPacket.resetHelperData();
        mobPacket.addData(mAnimationEnabled);
        if (mAnimationEnabled) mobPacket.addData(mobType);
        mobPacket.sendToAllAround(16);

        this.updateSlots();
        return CheckRecipeResultRegistry.SUCCESSFUL;
    }

    private boolean isRitualValid() {
        if (!isInRitualMode) return false;
        if (masterStoneRitual == null) return false;
        if (masterStoneRitual.isInvalid() || !(((TEMasterStone) masterStoneRitual).getCurrentRitual()
            .equals(WellOfSufferingRitualName))) {
            masterStoneRitual = null;
            return false;
        }
        return true;
    }

    private boolean connectToRitual() {
        if (!BloodMagic.isModLoaded()) return false;
        ChunkCoordinates coords = this.getBaseMetaTileEntity()
            .getCoords();
        int[] abc = new int[] { 0, -8, 2 };
        int[] xyz = new int[] { 0, 0, 0 };
        this.getExtendedFacing()
            .getWorldOffset(abc, xyz);
        xyz[0] += coords.posX;
        xyz[1] += coords.posY;
        xyz[2] += coords.posZ;
        TileEntity te = this.getBaseMetaTileEntity()
            .getTileEntity(xyz[0], xyz[1], xyz[2]);
        if (te instanceof TEMasterStone) {
            if (((TEMasterStone) te).getCurrentRitual()
                .equals(WellOfSufferingRitualName)) {
                masterStoneRitual = te;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        glassTier = -1;
        mCasing = 0;
        if (!checkPiece(STRUCTURE_PIECE_MAIN, 2, 6, 0)) return false;
        if (mCasing < 35 || mEnergyHatches.isEmpty()) return false;
        if (glassTier < VoltageIndex.UV)
            for (MTEHatchEnergy hatch : mEnergyHatches) if (hatch.mTier > glassTier) return false;
        if (isInRitualMode) connectToRitual();
        return true;
    }

    @Override
    public String[] getInfoData() {
        ArrayList<String> info = new ArrayList<>(Arrays.asList(super.getInfoData()));
        String mobName = getCurrentMob();
        info.add(
            mobName != null ? StatCollector.translateToLocalFormatted("kubatech.infodata.eec.current_mob", mobName)
                : StatCollector.translateToLocal("kubatech.infodata.eec.current_mob.none"));
        info.add(
            mAnimationEnabled ? StatCollector.translateToLocal("kubatech.infodata.eec.animations.enabled")
                : StatCollector.translateToLocal("kubatech.infodata.eec.animations.disabled"));
        info.add(
            mIsProducingInfernalDrops
                ? StatCollector.translateToLocal("kubatech.infodata.eec.produce_infernal_drops.allowed")
                : StatCollector.translateToLocal("kubatech.infodata.eec.produce_infernal_drops.not_allowed"));
        info.add(
            voidAllDamagedAndEnchantedItems ? StatCollector.translateToLocal("kubatech.infodata.eec.void_damaged.yes")
                : StatCollector.translateToLocal("kubatech.infodata.eec.void_damaged.no"));
        info.add(
            isInRitualMode ? StatCollector.translateToLocal("kubatech.infodata.eec.in_ritual_mode.yes")
                : StatCollector.translateToLocal("kubatech.infodata.eec.in_ritual_mode.no"));
        if (isInRitualMode) info.add(
            isRitualValid() ? StatCollector.translateToLocal("kubatech.infodata.eec.connected_to_ritual.yes")
                : StatCollector.translateToLocal("kubatech.infodata.eec.connected_to_ritual.no"));
        else {
            info.add(
                weaponCache.isValid ? StatCollector.translateToLocal("kubatech.infodata.eec.inserted_weapon.yes")
                    : StatCollector.translateToLocal("kubatech.infodata.eec.inserted_weapon.no"));
            if (weaponCache.isValid) {
                info.add(
                    StatCollector
                        .translateToLocalFormatted("kubatech.infodata.eec.weapon.damage", weaponCache.attackDamage));
                info.add(
                    StatCollector
                        .translateToLocalFormatted("kubatech.infodata.eec.weapon.looting_level", weaponCache.looting));
                info.add(
                    StatCollector.translateToLocalFormatted(
                        "kubatech.infodata.eec.total_damage",
                        DIAMOND_SPIKES_DAMAGE + weaponCache.attackDamage));
            } else info.add(
                StatCollector.translateToLocalFormatted("kubatech.infodata.eec.total_damage", DIAMOND_SPIKES_DAMAGE));
        }
        return info.toArray(new String[0]);
    }

    @Override
    protected void addConfigurationWidgets(DynamicPositionedRow configurationElements, UIBuildContext buildContext) {
        configurationElements.setSynced(true);
        configurationElements.widget(new CycleButtonWidget().setToggle(() -> isInRitualMode, v -> {
            if (this.mMaxProgresstime > 0) {
                GTUtility.sendChatToPlayer(buildContext.getPlayer(), "Can't change mode when running !");
                return;
            }

            isInRitualMode = v;

            if (!(buildContext.getPlayer() instanceof EntityPlayerMP)) return;
            if (!isInRitualMode) {
                GTUtility.sendChatToPlayer(buildContext.getPlayer(), "Ritual mode disabled");
            } else {
                GTUtility.sendChatToPlayer(buildContext.getPlayer(), "Ritual mode enabled");
                if (connectToRitual())
                    GTUtility.sendChatToPlayer(buildContext.getPlayer(), "Successfully connected to the ritual");
                else GTUtility.sendChatToPlayer(buildContext.getPlayer(), "Can't connect to the ritual");
            }
        })
            .setTextureGetter(toggleButtonTextureGetter)
            .setVariableBackgroundGetter(toggleButtonBackgroundGetter)
            .setSize(16, 16)
            .addTooltip(StatCollector.translateToLocal("kubatech.gui.tooltip.eec.ritual_mode"))
            .setTooltipShowUpDelay(TOOLTIP_DELAY));
        configurationElements.widget(new CycleButtonWidget().setToggle(() -> mIsProducingInfernalDrops, v -> {
            if (this.mMaxProgresstime > 0) {
                GTUtility.sendChatToPlayer(buildContext.getPlayer(), "Can't change mode when running !");
                return;
            }

            mIsProducingInfernalDrops = v;

            if (!(buildContext.getPlayer() instanceof EntityPlayerMP)) return;
            if (!mIsProducingInfernalDrops) GTUtility
                .sendChatToPlayer(buildContext.getPlayer(), "Mobs will now be prevented from spawning infernal");
            else GTUtility.sendChatToPlayer(buildContext.getPlayer(), "Mobs can spawn infernal now");
        })
            .setTextureGetter(toggleButtonTextureGetter)
            .setVariableBackgroundGetter(toggleButtonBackgroundGetter)
            .setSize(16, 16)
            .addTooltip(StatCollector.translateToLocal("kubatech.gui.text.eec.infernal_drop"))
            .addTooltip(
                new Text(StatCollector.translateToLocal("kubatech.gui.text.eec.infernal_drop.always"))
                    .color(Color.GRAY.normal))
            .setTooltipShowUpDelay(TOOLTIP_DELAY));
        configurationElements.widget(new CycleButtonWidget().setToggle(() -> voidAllDamagedAndEnchantedItems, v -> {
            if (this.mMaxProgresstime > 0) {
                GTUtility.sendChatToPlayer(buildContext.getPlayer(), "Can't change mode when running !");
                return;
            }

            voidAllDamagedAndEnchantedItems = v;

            if (!(buildContext.getPlayer() instanceof EntityPlayerMP)) return;
            if (!voidAllDamagedAndEnchantedItems) GTUtility.sendChatToPlayer(buildContext.getPlayer(), "Void nothing");
            else GTUtility.sendChatToPlayer(buildContext.getPlayer(), "Void all damaged and enchanted items");
        })
            .setTextureGetter(toggleButtonTextureGetter)
            .setVariableBackgroundGetter(toggleButtonBackgroundGetter)
            .setSize(16, 16)
            .addTooltip(StatCollector.translateToLocal("kubatech.gui.text.eec.void_all_damaged"))
            .addTooltip(
                new Text(StatCollector.translateToLocal("kubatech.gui.text.eec.void_all_damaged.warning"))
                    .color(Color.GRAY.normal))
            .setTooltipShowUpDelay(TOOLTIP_DELAY));
    }

    @Override
    public void createInventorySlots() {
        final SlotWidget spawnerSlot = new SlotWidget(inventoryHandler, 1);
        spawnerSlot.setBackground(
            GTUITextures.SLOT_DARK_GRAY,
            UITexture.fullImage(Tags.MODID, "gui/slot/gray_spawner")
                .withFixedSize(16, 16)
                .withOffset(1, 1));
        spawnerSlot.setFilter(stack -> stack.getItem() == poweredSpawnerItem);
        slotWidgets.add(spawnerSlot);
        final SlotWidget weaponSlot = new SlotWidget(weaponCache, 0);
        weaponSlot.setBackground(
            GTUITextures.SLOT_DARK_GRAY,
            UITexture.fullImage(Tags.MODID, "gui/slot/gray_sword")
                .withFixedSize(16, 16)
                .withOffset(1, 1));
        slotWidgets.add(weaponSlot);
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected SoundResource getActivitySoundLoop() {
        return SoundResource.GT_MACHINES_EXTREME_ENTITY_CRUSHER_LOOP;
    }

    private String getCurrentMob() {
        ItemStack spawner = mInventory[1];
        if (spawner != null && spawner.getTagCompound() != null) {
            return spawner.getTagCompound()
                .getString("mobType");
        }
        return null;
    }

    @Override
    public void getWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y,
        int z) {
        super.getWailaNBTData(player, tile, tag, world, x, y, z);
        String mob = getCurrentMob();
        if (mob != null) {
            tag.setString("eecMobType", mob);
        }
    }

    @Override
    public void getWailaBody(ItemStack itemStack, List<String> currentTip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currentTip, accessor, config);
        NBTTagCompound tag = accessor.getNBTData();

        if (tag.hasKey("eecMobType", Constants.NBT.TAG_STRING)) {
            String mob = tag.getString("eecMobType");
            String mobKey = "entity." + mob + ".name";
            if (StatCollector.canTranslate(mobKey)) {
                currentTip.add(
                    StatCollector.translateToLocalFormatted(
                        "kubatech.waila.eec.mob_type",
                        StatCollector.translateToLocal(mobKey)));
            } else {
                currentTip.add(StatCollector.translateToLocalFormatted("kubatech.waila.eec.mob_type", mob));
            }
        } else {
            currentTip.add(
                StatCollector.translateToLocalFormatted(
                    "kubatech.waila.eec.mob_type",
                    StatCollector.translateToLocal("kubatech.waila.eec.no_mob")));
        }
    }

    @Override
    public boolean supportsBatchMode() {
        return true;
    }

    private static class EECFakePlayer extends FakePlayer {

        MTEExtremeEntityCrusher mte;
        ItemStack currentWeapon;

        public EECFakePlayer(MTEExtremeEntityCrusher mte) {
            super(
                (WorldServer) mte.getBaseMetaTileEntity()
                    .getWorld(),
                new GameProfile(
                    UUID.nameUUIDFromBytes("[EEC Fake Player]".getBytes(StandardCharsets.UTF_8)),
                    "[EEC Fake Player]"));
            this.mte = mte;
        }

        @Override
        public void renderBrokenItemStack(ItemStack p_70669_1_) {}

        @Override
        public Random getRNG() {
            return mte.rand;
        }

        @Override
        public void destroyCurrentEquippedItem() {}

        @Override
        public ItemStack getCurrentEquippedItem() {
            return currentWeapon;
        }

        @Override
        public ItemStack getHeldItem() {
            return currentWeapon;
        }
    }
}
