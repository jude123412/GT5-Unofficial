package gregtech.common.render.items;

import fox.spiteful.avaritia.render.CosmicRenderShenanigans;
import gregtech.api.enums.Textures;
import gregtech.api.items.MetaGeneratedItem;
import gregtech.common.render.GTRenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL20;

import java.util.HashMap;
import java.util.Map;

import static gregtech.api.enums.Mods.Avaritia;

public class UniversiumMetaItemRenderer implements IItemRenderer {

    private final float opacity;
    private final Textures.ItemIcons mask;

    public UniversiumMetaItemRenderer(Textures.ItemIcons cosmicMask, float cosmicOpacity) {
        opacity = cosmicOpacity;
        mask = cosmicMask;
    }

    @Override
    public boolean handleRenderType(final ItemStack item, final ItemRenderType type) {
        return type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON
            || type == ItemRenderType.INVENTORY
            || type == ItemRenderType.ENTITY;

    }

    @Override
    public boolean shouldUseRenderHelper(final ItemRenderType type, final ItemStack item,
                                         final ItemRendererHelper helper) {
        return type == ItemRenderType.ENTITY && helper == ItemRendererHelper.ENTITY_BOBBING
            || (helper == ItemRendererHelper.ENTITY_ROTATION && Minecraft.getMinecraft().gameSettings.fancyGraphics);

    }

    @Override
    public void renderItem(final ItemRenderType type, final ItemStack item, final Object... data) {
        GL11.glPushMatrix();

        if (item.getItem() instanceof MetaGeneratedItem mgItem) {
            IIcon[] icons = mgItem.mIconList[item.getItemDamage() - mgItem.mOffset];

            if (icons != null && icons.length > 0 && icons[0] != null) {

                if (Avaritia.isModLoaded()) {
                    processLightLevel(type, data);

                    GL11.glPushMatrix();
                    GL11.glEnable(GL11.GL_BLEND);
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                    if (type == ItemRenderType.INVENTORY) {

                        GL11.glDisable(GL11.GL_ALPHA_TEST);
                        GL11.glDisable(GL11.GL_DEPTH_TEST);

                        GTRenderUtil.renderItem(type, icons[0]);

                        GL11.glEnable(GL11.GL_BLEND);
                        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                        GL11.glDisable(GL11.GL_ALPHA_TEST);
                        GL11.glDisable(GL11.GL_DEPTH_TEST);

                        CosmicRenderShenanigans.cosmicOpacity = opacity;
                        CosmicRenderShenanigans.inventoryRender = true;
                        CosmicRenderShenanigans.useShader();

                        GL11.glColor4d(1, 1, 1, 1);

                        // Draw cosmic overlay
                        GTRenderUtil.renderItem(type, mask.getIcon());

                        CosmicRenderShenanigans.releaseShader();
                        CosmicRenderShenanigans.inventoryRender = false;

                        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                    } else {
                        // RENDER ITEM
                        GTRenderUtil.renderItem(type, icons[0]);

                        int program = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);

                        GL11.glDisable(GL11.GL_ALPHA_TEST);
                        GL11.glDepthFunc(GL11.GL_EQUAL);
                        CosmicRenderShenanigans.cosmicOpacity = opacity;
                        CosmicRenderShenanigans.useShader();

                        // RENDER COSMIC OVERLAY
                        GTRenderUtil.renderItem(type, mask.getIcon());
                        CosmicRenderShenanigans.releaseShader();
                        GL11.glDepthFunc(GL11.GL_LEQUAL);

                        GL20.glUseProgram(program);
                    }

                    GL11.glEnable(GL11.GL_DEPTH_TEST);
                    GL11.glEnable(GL11.GL_ALPHA_TEST);
                    GL11.glDisable(GL11.GL_BLEND);
                    GL11.glPopMatrix();
                } else {
                    GTRenderUtil.renderItem(type, icons[0]);
                }
            }

        }

        GL11.glPopMatrix();
    }

    private void processLightLevel(ItemRenderType type, Object... data) {
        switch (type) {
            case ENTITY -> {
                EntityItem ent = (EntityItem) (data[1]);
                if (ent != null) {
                    CosmicRenderShenanigans.setLightFromLocation(
                        ent.worldObj,
                        MathHelper.floor_double(ent.posX),
                        MathHelper.floor_double(ent.posY),
                        MathHelper.floor_double(ent.posZ));
                }
            }
            case EQUIPPED, EQUIPPED_FIRST_PERSON -> {
                EntityLivingBase ent = (EntityLivingBase) (data[1]);
                if (ent != null) {
                    CosmicRenderShenanigans.setLightFromLocation(
                        ent.worldObj,
                        MathHelper.floor_double(ent.posX),
                        MathHelper.floor_double(ent.posY),
                        MathHelper.floor_double(ent.posZ));
                }
            }
            case INVENTORY -> {
                CosmicRenderShenanigans.setLightLevel(10.2f);
            }
            default -> {
                CosmicRenderShenanigans.setLightLevel(1.0f);
            }
        }
    }
}
