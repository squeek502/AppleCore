package squeek.applecore.api.food;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * An interface for edible objects.<br>
 * <br>
 * Intended for use by classes that don't set a Food.<br>
 * <br>
 * When extending Item,
 * {@link Item#getFood()}
 * should be used instead.<br>
 * <br>
 * Note: {@link IEdible#getFoodValues} will take precedence over the
 * {@link Item} methods when getting an item's food values
 */
public interface IEdible
{
	FoodValues getFoodValues(@Nonnull ItemStack itemStack);
}