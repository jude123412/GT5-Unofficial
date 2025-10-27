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
        boolean playAudio = false;
        // Check reactor is running before starting the loop
        if (getReactorEnergyOutput() > 0.0F) {
            int limit = (int) (getWorld().getWorldTime() % 63);

            // Check every 62 ticks to run audio
            if (limit == 62) {
                playAudio = true;
            }
        }

        if (getReactorEnergyOutput() > 0.0F && playAudio) {

            float euFactor = 0.2f;
            float euLow = euFactor * 512; // 1amp HV
            float euMid = euFactor * 2048; // 1amp EV
            float euHigh = euFactor * 8192; // 1amp IV

            if (getReactorEnergyOutput() < euLow) {
                GTUtility.sendSoundToPlayers(
                    getWorld(),
                    SoundResource.IC2_REACTOR_LOW_LOOP,
                    0.2F,
                    1.0F,
                    getPosition().posX,
                    getPosition().posY,
                    getPosition().posZ);
            }

            if (getReactorEnergyOutput() >= euMid && getReactorEnergyOutput() < euHigh) {
                GTUtility.sendSoundToPlayers(
                    getWorld(),
                    SoundResource.IC2_REACTOR_MID_LOOP,
                    0.2F,
                    1.0F,
                    getPosition().posX,
                    getPosition().posY,
                    getPosition().posZ);
            }

            if (getReactorEnergyOutput() >= euHigh) {
                GTUtility.sendSoundToPlayers(
                    getWorld(),
                    SoundResource.IC2_REACTOR_HIGH_LOOP,
                    0.2F,
                    1.0F,
                    getPosition().posX,
                    getPosition().posY,
                    getPosition().posZ);
            }
        }
    }
}
