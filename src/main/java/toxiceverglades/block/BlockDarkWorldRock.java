package toxiceverglades.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.enums.Textures;
import gtPlusPlus.api.interfaces.ITileTooltip;
import gtPlusPlus.core.creative.AddToCreativeTab;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import static gregtech.api.enums.Mods.Minecraft;

public class BlockDarkWorldRock extends Block implements ITileTooltip {

    protected BlockDarkWorldRock() {
        super(Material.rock);
        this.setCreativeTab(AddToCreativeTab.tabBOP);
        this.setBlockName("blockDarkWorldGround3");
        this.setHardness(1.0F);
        this.setBlockTextureName(Minecraft.ID + ":" + "stone");
        this.setStepSound(Block.soundTypeStone);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return Textures.BlockIcons.IRRADIATED_STONE.getIcon();
    }

    @Override
    public boolean isReplaceableOreGen(World world, int x, int y, int z, Block target) {
        return target == Blocks.stone || target == this;
    }

    @Override
    public int getTooltipID() {
        return 3;
    }
}
