package gregtech.common.render;

import com.brandon3055.draconicevolution.common.lib.References;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.common.tileentities.machines.draconic.MTEEnergyPylon;
import gregtech.common.tileentities.render.TileEntityEnergyPylon;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;

public class EnergyPylonRenderer extends TileEntitySpecialRenderer {

    private static final ResourceLocation modelTexture = new ResourceLocation(
        References.MODID.toLowerCase(),
        "textures/models/pylon_sphere_texture.png");
    private final IModelCustom model;

    public EnergyPylonRenderer() {
        model = AdvancedModelLoader
            .loadModel(new ResourceLocation(References.MODID.toLowerCase(), "models/pylon_sphere.obj"));
    }

    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float timeSinceLastTick) {
            if (!(tile instanceof TileEntityEnergyPylon base)) return;

            MetaTileEntity mte = base.getPylonMTE();
            if (!(mte instanceof MTEEnergyPylon pylon)) return;

            float scale = pylon.getModelScale() + (timeSinceLastTick *= pylon.getBaseMetaTileEntity().isAllowedToWork() ? -0.01F : 0.01F);
            float rotation = pylon.getModelRotation() + (timeSinceLastTick / 2F);

            GL11.glPushMatrix();
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

            GL11.glTranslated(x + 0.5, y - 0.5, z + 0.5);
            if (pylon.getBaseMetaTileEntity().getWorld().getBlockMetadata(pylon.getBaseMetaTileEntity().getXCoord(), pylon.getBaseMetaTileEntity().getYCoord(), pylon.getBaseMetaTileEntity().getZCoord()) == 1) {
                GL11.glTranslated(0, -1, 0);
            } else {
                GL11.glTranslated(0, 1, 0);
            }

            GL11.glAlphaFunc(GL11.GL_GREATER, 0.0F);

            bindTexture(modelTexture);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 10497.0F);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 10497.0F);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_CULL_FACE);

            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glDepthMask(false);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 200F, 200F);

            renderPass(scale % 1F, rotation);
            renderPass((scale + 0.25F) % 1F, rotation);
            renderPass((scale + 0.5F) % 1F, rotation);
            renderPass((scale + 0.75F) % 1F, rotation);

            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDepthMask(true);
            GL11.glPopAttrib();
            GL11.glPopMatrix();
    }

    private void renderPass(float scale, float rotation) {
        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, scale);
        GL11.glRotatef(rotation * 0.5F, 0F, -1F, -0.5F);
        GL11.glColor4f(1F, 1F, 1F, 1F - scale);
        model.renderAll();
        GL11.glPopMatrix();
    }
}
