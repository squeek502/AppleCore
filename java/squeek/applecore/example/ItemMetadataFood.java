package squeek.applecore.example;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import javax.annotation.Nonnull;

/**
 * An example implementation of a metadata-based food item
 * that is AppleCore compatible
 */
public class ItemMetadataFood extends ItemFood
{
	public int[] hungerValues;
	public float[] saturationModifiers;

	public ItemMetadataFood(int[] hungerValues, float[] saturationModifiers)
	{
		super(0, 0f, false);
		this.hungerValues = hungerValues;
		this.saturationModifiers = saturationModifiers;

		setHasSubtypes(true);
	}

	/**
	 * @return The hunger value of the ItemStack
	 */
	@Override
	public int getHealAmount(@Nonnull ItemStack stack)
	{
		return hungerValues[stack.getItemDamage()];
	}

	/**
	 * @return The saturation modifier of the ItemStack
	 */
	@Override
	public float getSaturationModifier(@Nonnull ItemStack stack)
	{
		return saturationModifiers[stack.getItemDamage()];
	}

	@Override
	public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> subItems)
	{
		if (this.isInCreativeTab(tab))
		{
			for (int meta = 0; meta < Math.min(hungerValues.length, saturationModifiers.length); meta++)
			{
				subItems.add(new ItemStack(this, 1, meta));
			}
		}
	}
}