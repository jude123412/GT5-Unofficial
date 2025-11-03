package gregtech.common.render;

import net.minecraft.client.particle.EntityBubbleFX;
import net.minecraft.world.World;

public class EndergenicBubbleRenderer extends EntityBubbleFX {

    private final double yLimit;

    public EndergenicBubbleRenderer(World aWorld, double aXCoord, double aYCoord, double aZCoord, double aMotionX,
        double aMotionY, double aMotionZ) {
        super(aWorld, aXCoord, aYCoord, aZCoord, aMotionX, aMotionY, aMotionZ);
        setRBGColorF(0.6f, 0.6f, 0.5f);
        yLimit = Math.floor(aYCoord) + 2.75F;
        this.particleMaxAge = (int) (4.0F / (this.rand.nextFloat() * 0.9F + 0.1F));
    }

    @Override
    public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        motionY += 0.002D;
        moveEntity(motionX, motionY, motionZ);
        motionX *= 0.8500000238418579D;
        motionY *= 0.8500000238418579D;
        motionZ *= 0.8500000238418579D;

        if (posY > yLimit) {
            setDead();
        }
    }
}
