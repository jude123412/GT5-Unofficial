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

            float uDistortion = (float) Math.max(-0.05f, Math.min(0.05f, (Math.sin(System.currentTimeMillis() * 0.001) * 0.1f)));
            float vDistortion = (float) Math.max(-0.05f, Math.min(0.05f, (Math.cos(System.currentTimeMillis() * 0.001) * 0.1f)));

            float minU = icon.getMinU();
            float maxU = icon.getMaxU();
            float minV = icon.getMinV();
            float maxV = icon.getMaxV();


            t.startDrawingQuads();
            t.setColorRGBA_F(1.0f, 1.0f, 1.0f, 0.6f);
            t.addVertexWithUV(0 ,  0, 0, minU + uDistortion, minV + vDistortion);
            t.addVertexWithUV(0 , 16, 0, minU + uDistortion, maxV + vDistortion);
            t.addVertexWithUV(16, 16, 0, maxU + uDistortion, maxV + vDistortion);
            t.addVertexWithUV(16,  0, 0, maxU + uDistortion, minV + vDistortion);
            t.draw();

//            tessellator.addVertexWithUV(x            , y         , z                 , uMin + uDistortion, vMin + vDistortion);
//            tessellator.addVertexWithUV(x + width    , y         , z                 , uMax + uDistortion, vMin + vDistortion);
//            tessellator.addVertexWithUV(x + width    , y + height, z                 , uMax + uDistortion, vMax + vDistortion);
//            tessellator.addVertexWithUV(x            , y + height, z                 , uMin + uDistortion, vMax + vDistortion);

            GL11.glPopMatrix();
        } else {
            GTRenderUtil.renderItem(type, icon);
        }
    }
}
