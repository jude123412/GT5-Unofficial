package gregtech.common.tileentities.render;

import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.common.tileentities.machines.draconic.MTEEnergyPylon;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public class TileEntityEnergyPylon extends TileEntity {

    private boolean initialized = false;
    private int invalidTicks = 0;

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return 65536;
    }

    @Override
    public void updateEntity() {
        if (worldObj.isRemote) return;

        // --- INSTANT DELETE: block below is gone ---
        TileEntity below = worldObj.getTileEntity(xCoord, yCoord - 1, zCoord);
        if (!(below instanceof BaseMetaTileEntity)) {
            worldObj.setBlockToAir(xCoord, yCoord, zCoord);
            return;
        }

        // --- DELAY VALIDATION UNTIL WORLD IS STABLE ---
        if (!initialized) {
            if (worldObj.getTotalWorldTime() < 20) return; // wait 1 second after world load
            initialized = true;
        }

        // --- PERIODIC VALIDATION ---
        if (worldObj.getTotalWorldTime() % 20 == 0) {
            validateBelow((BaseMetaTileEntity) below);
        }
    }

    private void validateBelow(BaseMetaTileEntity base) {
        boolean valid = base.getMetaTileEntity() instanceof MTEEnergyPylon;

        if (!valid) {
            invalidTicks++;
            if (invalidTicks > 5) { // must fail 5 times in a row
                worldObj.setBlockToAir(xCoord, yCoord, zCoord);
            }
        } else {
            invalidTicks = 0;
        }
    }

    // Get the pylon MTE beneath this render tile
    public MTEEnergyPylon getPylonMTE() {
        if (worldObj == null) return null;

        TileEntity te = worldObj.getTileEntity(xCoord, yCoord - 1, zCoord);
        if (te instanceof BaseMetaTileEntity base) {
            if (base.getMetaTileEntity() instanceof MTEEnergyPylon pylon) {
                return pylon;
            }
        }
        return null;
    }
}
