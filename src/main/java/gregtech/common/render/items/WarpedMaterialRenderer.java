package gregtech.common.render.items;

import gregtech.common.render.GTRenderUtil;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import org.lwjgl.opengl.GL11;


public class WarpedMaterialRenderer extends GeneratedMaterialRenderer {
    @Override
    protected void renderRegularItem(ItemRenderType type, ItemStack aStack, IIcon icon, boolean shouldModulateColor) {
        if (type == ItemRenderType.INVENTORY) {
            Tessellator t = Tessellator.instance;

            GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glColor3f(255, 255, 255);

            float distortionFactor = 0.0005f;
            float uDistortion = (float) Math.sin(System.currentTimeMillis() * 0.001) * distortionFactor;

            float minU = icon.getMinU();
            float maxU = icon.getMaxU();
            float minV = icon.getMinV();
            float maxV = icon.getMaxV();

            float centerX = 8.0f / 16.0f;
            float centerY = 8.0f / 16.0f;

            t.startDrawingQuads();
            t.setColorRGBA_F(1.0f, 1.0f, 1.0f, 0.6f);
            t.addVertexWithUV(0 ,  0, 0, minU + (uDistortion * (0 - centerX)), minV + (uDistortion * (0 - centerY)));
            t.addVertexWithUV(0 , 16, 0, minU + (uDistortion * (1 - centerX)), maxV + (uDistortion * (0 - centerY)));
            t.addVertexWithUV(16, 16, 0, maxU + (uDistortion * (1 - centerX)), maxV + (uDistortion * (1 - centerY)));
            t.addVertexWithUV(16,  0, 0, maxU + (uDistortion * (0 - centerX)), minV + (uDistortion * (1 - centerY)));
            t.draw();

//            tessellator.addVertexWithUV(x        , y         , z, uMin + (bendAmount * (0 - centerX)), vMin + (bendAmount * (0 - centerY)));
//            tessellator.addVertexWithUV(x + width, y         , z, uMax + (bendAmount * (1 - centerX)), vMin + (bendAmount * (0 - centerY)));
//            tessellator.addVertexWithUV(x + width, y + height, z, uMax + (bendAmount * (1 - centerX)), vMax + (bendAmount * (1 - centerY)));
//            tessellator.addVertexWithUV(x        , y + height, z, uMin + (bendAmount * (0 - centerX)), vMax + (bendAmount * (1 - centerY)));

            GL11.glPopMatrix();
        } else {
            GTRenderUtil.renderItem(type, icon);
        }
    }
}
