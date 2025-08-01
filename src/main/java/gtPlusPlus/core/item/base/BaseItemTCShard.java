package gtPlusPlus.core.item.base;

import static gregtech.api.enums.Mods.GTPlusPlus;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.registry.GameRegistry;
import gregtech.api.util.GTLanguageManager;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.StringUtils;
import gtPlusPlus.core.creative.AddToCreativeTab;

public class BaseItemTCShard extends Item {

    public final String unlocalName;
    public final String displayName;
    public final int itemColour;

    public BaseItemTCShard(final String DisplayName, final int colour) {
        this(DisplayName, colour, null);
    }

    public BaseItemTCShard(final String DisplayName, final int colour, final String[] Description) {
        this.unlocalName = "item" + StringUtils.sanitizeString(DisplayName);
        this.displayName = DisplayName;
        this.itemColour = colour;
        this.setCreativeTab(AddToCreativeTab.tabMisc);
        this.setUnlocalizedName(this.unlocalName);
        if (Description != null) {
            for (int i = 0; i < Description.length; i++) {
                GTLanguageManager
                    .addStringLocalization("gtplusplus." + getUnlocalizedName() + ".tooltip." + i, Description[i]);
            }
        }
        this.setMaxStackSize(64);
        this.setTextureName(GTPlusPlus.ID + ":" + "itemShard");
        GameRegistry.registerItem(this, this.unlocalName);
        GTOreDictUnificator.registerOre("shard" + DisplayName, new ItemStack(this));
        GTOreDictUnificator.registerOre("gemInfused" + DisplayName, new ItemStack(this));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void addInformation(final ItemStack stack, final EntityPlayer aPlayer, final List list, final boolean bool) {
        for (int i = 0;; i++) {
            String tooltip = GTLanguageManager
                .getTranslation("gtplusplus." + this.getUnlocalizedName() + ".tooltip" + "." + i);
            if (!("gtplusplus." + this.getUnlocalizedName() + ".tooltip" + "." + i).equals(tooltip)) {
                list.add(tooltip);
            } else break;
        }
    }

    @Override
    public int getColorFromItemStack(final ItemStack stack, final int HEX_OxFFFFFF) {
        return this.itemColour;
    }

}
