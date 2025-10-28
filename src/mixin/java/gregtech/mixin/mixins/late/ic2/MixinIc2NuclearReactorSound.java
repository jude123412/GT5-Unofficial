package gregtech.mixin.mixins.late.ic2;

import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import gregtech.api.enums.SoundResource;
import gregtech.api.util.GTUtility;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.reactor.tileentity.TileEntityNuclearReactorElectric;

@Mixin(value = TileEntityNuclearReactorElectric.class, remap = false)
public abstract class MixinIc2NuclearReactorSound extends TileEntityInventory {

    @Shadow
    public abstract float getReactorEnergyOutput();

    @Shadow
    public abstract World getWorld();

    @Shadow
    public abstract ChunkCoordinates getPosition();

    @Inject(method = "updateEntityServer", at = @At("RETURN"), cancellable = true, remap = false)
    public void gT5_Unofficial$fixReactorSound(CallbackInfo ci) {
        if (!getWorld().isRemote) {
            boolean playAudio = false;
            // Check reactor is running before starting the loop
            if (getReactorEnergyOutput() > 0.0F) {
                long loop = getWorld().getWorldTime() % 63;

                if (loop == 20) {
                    playAudio = true;
                }
            }

            if (playAudio) {

                float euFactor = 0.2f;
                float euLow = euFactor * 512; // 1amp HV
                float euMid = euFactor * 2048; // 1amp EV
                float euHigh = euFactor * 8192; // 1amp IV

                if (getReactorEnergyOutput() > 0.0F && getReactorEnergyOutput() < euLow) {
                    GTUtility.sendSoundToPlayers(
                        getWorld(),
                        SoundResource.IC2_REACTOR_LOW_LOOP,
                        0.4F,
                        1.0F,
                        getPosition().posX,
                        getPosition().posY,
                        getPosition().posZ);
                } else if (getReactorEnergyOutput() >= euLow && getReactorEnergyOutput() < euMid) {
                    GTUtility.sendSoundToPlayers(
                        getWorld(),
                        SoundResource.IC2_REACTOR_MID_LOOP,
                        0.4F,
                        1.0F,
                        getPosition().posX,
                        getPosition().posY,
                        getPosition().posZ);
                } else if (getReactorEnergyOutput() >= euHigh) {
                    GTUtility.sendSoundToPlayers(
                        getWorld(),
                        SoundResource.IC2_REACTOR_HIGH_LOOP,
                        0.4F,
                        1.0F,
                        getPosition().posX,
                        getPosition().posY,
                        getPosition().posZ);
                }
            }
        }
    }
}
