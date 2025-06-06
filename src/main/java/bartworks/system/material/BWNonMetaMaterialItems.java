/*
 * Copyright (c) 2018-2020 bartimaeusnek Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions: The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package bartworks.system.material;

import static gregtech.api.util.GTRecipeBuilder.WILDCARD;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import gregtech.api.interfaces.IItemContainer;
import gregtech.api.util.GTModHandler;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTUtility;

public enum BWNonMetaMaterialItems implements IItemContainer {

    Depleted_Tiberium_1,
    Depleted_Tiberium_2,
    Depleted_Tiberium_4,
    TiberiumCell_1,
    TiberiumCell_2,
    TiberiumCell_4,
    TheCoreCell,
    Depleted_TheCoreCell;

    private ItemStack mStack;
    private boolean mHasNotBeenSet = true;

    @Override
    public IItemContainer set(Item aItem) {
        this.mHasNotBeenSet = false;
        if (aItem == null) return this;
        ItemStack aStack = new ItemStack(aItem, 1, 0);
        this.mStack = GTUtility.copyAmount(1, aStack);
        return this;
    }

    @Override
    public IItemContainer set(ItemStack aStack) {
        this.mHasNotBeenSet = false;
        this.mStack = GTUtility.copyAmount(1, aStack);
        return this;
    }

    @Override
    public IItemContainer hidden() {
        codechicken.nei.api.API.hideItem(get(1L));
        return this;
    }

    @Override
    public Item getItem() {
        if (this.mHasNotBeenSet)
            throw new IllegalAccessError("The Enum '" + this.name() + "' has not been set to an Item at this time!");
        if (GTUtility.isStackInvalid(this.mStack)) return null;
        return this.mStack.getItem();
    }

    @Override
    public Block getBlock() {
        if (this.mHasNotBeenSet)
            throw new IllegalAccessError("The Enum '" + this.name() + "' has not been set to an Item at this time!");
        return GTUtility.getBlockFromItem(this.getItem());
    }

    @Override
    public final boolean hasBeenSet() {
        return !this.mHasNotBeenSet;
    }

    @Override
    public boolean isStackEqual(Object aStack) {
        return this.isStackEqual(aStack, false, false);
    }

    @Override
    public boolean isStackEqual(Object aStack, boolean aWildcard, boolean aIgnoreNBT) {
        if (GTUtility.isStackInvalid(aStack)) return false;
        return GTUtility
            .areUnificationsEqual((ItemStack) aStack, aWildcard ? this.getWildcard(1) : this.get(1), aIgnoreNBT);
    }

    @Override
    public ItemStack get(long aAmount, Object... aReplacements) {
        if (this.mHasNotBeenSet)
            throw new IllegalAccessError("The Enum '" + this.name() + "' has not been set to an Item at this time!");
        if (GTUtility.isStackInvalid(this.mStack)) return GTUtility.copyAmount(aAmount, aReplacements);
        return GTUtility.copyAmount(aAmount, GTOreDictUnificator.get(this.mStack));
    }

    @Override
    public ItemStack getWildcard(long aAmount, Object... aReplacements) {
        if (this.mHasNotBeenSet)
            throw new IllegalAccessError("The Enum '" + this.name() + "' has not been set to an Item at this time!");
        if (GTUtility.isStackInvalid(this.mStack)) return GTUtility.copyAmount(aAmount, aReplacements);
        return GTUtility.copyAmountAndMetaData(aAmount, WILDCARD, GTOreDictUnificator.get(this.mStack));
    }

    @Override
    public ItemStack getUndamaged(long aAmount, Object... aReplacements) {
        if (this.mHasNotBeenSet)
            throw new IllegalAccessError("The Enum '" + this.name() + "' has not been set to an Item at this time!");
        if (GTUtility.isStackInvalid(this.mStack)) return GTUtility.copyAmount(aAmount, aReplacements);
        return GTUtility.copyAmountAndMetaData(aAmount, 0, GTOreDictUnificator.get(this.mStack));
    }

    @Override
    public ItemStack getAlmostBroken(long aAmount, Object... aReplacements) {
        if (this.mHasNotBeenSet)
            throw new IllegalAccessError("The Enum '" + this.name() + "' has not been set to an Item at this time!");
        if (GTUtility.isStackInvalid(this.mStack)) return GTUtility.copyAmount(aAmount, aReplacements);
        return GTUtility
            .copyAmountAndMetaData(aAmount, this.mStack.getMaxDamage() - 1, GTOreDictUnificator.get(this.mStack));
    }

    @Override
    public ItemStack getWithName(long aAmount, String aDisplayName, Object... aReplacements) {
        ItemStack rStack = this.get(1, aReplacements);
        if (GTUtility.isStackInvalid(rStack)) return null;
        rStack.setStackDisplayName(aDisplayName);
        return GTUtility.copyAmount(aAmount, rStack);
    }

    @Override
    public ItemStack getWithCharge(long aAmount, int aEnergy, Object... aReplacements) {
        ItemStack rStack = this.get(1, aReplacements);
        if (GTUtility.isStackInvalid(rStack)) return null;
        GTModHandler.chargeElectricItem(rStack, aEnergy, Integer.MAX_VALUE, true, false);
        return GTUtility.copyAmount(aAmount, rStack);
    }

    @Override
    public ItemStack getWithDamage(long aAmount, long aMetaValue, Object... aReplacements) {
        if (this.mHasNotBeenSet)
            throw new IllegalAccessError("The Enum '" + this.name() + "' has not been set to an Item at this time!");
        if (GTUtility.isStackInvalid(this.mStack)) return GTUtility.copyAmount(aAmount, aReplacements);
        return GTUtility.copyAmountAndMetaData(aAmount, aMetaValue, GTOreDictUnificator.get(this.mStack));
    }

    @Override
    public IItemContainer registerOre(Object... aOreNames) {
        if (this.mHasNotBeenSet)
            throw new IllegalAccessError("The Enum '" + this.name() + "' has not been set to an Item at this time!");
        for (Object tOreName : aOreNames) GTOreDictUnificator.registerOre(tOreName, this.get(1));
        return this;
    }

    @Override
    public IItemContainer registerWildcardAsOre(Object... aOreNames) {
        if (this.mHasNotBeenSet)
            throw new IllegalAccessError("The Enum '" + this.name() + "' has not been set to an Item at this time!");
        for (Object tOreName : aOreNames) GTOreDictUnificator.registerOre(tOreName, this.getWildcard(1));
        return this;
    }
}
