package squeek.applecore.example;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;

import java.util.List;

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
	public int getHealAmount(ItemStack itemStack)
	{
		return hungerValues[itemStack.getItemDamage()];
	}

	/**
	 * @return The saturation modifier of the ItemStack
	 */
	@Override
	public float getSaturationModifier(ItemStack itemStack)
	{
		return saturationModifiers[itemStack.getItemDamage()];
	}

	@Override
	public void getSubItems(Item item, CreativeTabs creativeTabs, List<ItemStack> subItems)
	{
		for (int meta = 0; meta < Math.min(hungerValues.length, saturationModifiers.length); meta++)
		{
			subItems.add(new ItemStack(item, 1, meta));
		}
	}
}