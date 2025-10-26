package gregtech.mixin.mixins.late.ic2;

import gregtech.api.enums.SoundResource;
import gregtech.api.util.GTUtility;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ic2.core.block.reactor.tileentity.TileEntityNuclearReactorElectric;
import ic2.core.block.TileEntityInventory;

@Mixin(value = TileEntityNuclearReactorElectric.class, remap = false)
public abstract class MixinIc2NuclearReactorSound extends TileEntityNuclearReactorElectric {

    @Shadow
    public abstract float getReactorEnergyOutput();

    @Shadow
    public abstract World getWorld();


    @Unique
    public int gT5_Unofficial$xCoord;
    @Unique
    public int gT5_Unofficial$yCoord;
    @Unique
    public int gT5_Unofficial$zCoord;

    @Inject(method = "onNetworkUpdate", at = @At("HEAD"), cancellable = true, remap = false)
    public void hodgepodge$fixReactorSound(CallbackInfo ci) {
        if (getReactorEnergyOutput() > 0.0F && getReactorEnergyOutput() < 40.F) {
            getWorld().playSoundEffect(gT5_Unofficial$xCoord, gT5_Unofficial$yCoord, gT5_Unofficial$zCoord, "fire.fire", 1.0F, -1.0F);
            GTUtility.sendSoundToPlayers(getWorld(), SoundResource.IC2_REACTOR_LOW_LOOP, 1.0F, -1.0F, gT5_Unofficial$xCoord, gT5_Unofficial$yCoord, gT5_Unofficial$zCoord);
        }
        if (getReactorEnergyOutput() >= 40.0F && getReactorEnergyOutput() < 80.F) {
            getWorld().playSoundEffect(gT5_Unofficial$xCoord, gT5_Unofficial$yCoord, gT5_Unofficial$zCoord, "fire.fire", 1.0F, -1.0F);
            GTUtility.sendSoundToPlayers(getWorld(), SoundResource.IC2_REACTOR_MID_LOOP, 1.0F, -1.0F, gT5_Unofficial$xCoord, gT5_Unofficial$yCoord, gT5_Unofficial$zCoord);
        }
        if (getReactorEnergyOutput() >=  80.0F) {
            getWorld().playSoundEffect(gT5_Unofficial$xCoord, gT5_Unofficial$yCoord, gT5_Unofficial$zCoord, "fire.fire", 1.0F, -1.0F);
            GTUtility.sendSoundToPlayers(getWorld(), SoundResource.IC2_REACTOR_HIGH_LOOP, 1.0F, -1.0F, gT5_Unofficial$xCoord, gT5_Unofficial$yCoord, gT5_Unofficial$zCoord);
        }
    }
}
