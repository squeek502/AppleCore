package squeek.applecore.api.food;

import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;

/**
 * An interface for edible objects.<br>
 * <br>
 * Intended for use by classes that don't extend ItemFood.<br>
 * <br>
 * When extending ItemFood, 
 * {@link ItemFood#func_150905_g} (for hunger values) and
 * {@link ItemFood#func_150906_h} (for saturation modifiers)
 * should be used instead.<br>
 * <br>
 * Note: {@link IEdible#getFoodValues} will take precedence over the
 * {@link ItemFood} methods when getting an item's food values
 */
public interface IEdible
{
	public FoodValues getFoodValues(ItemStack itemStack);
}
