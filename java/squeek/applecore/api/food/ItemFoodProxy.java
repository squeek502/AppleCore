package squeek.applecore.api.food;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * Useful for adding AppleCore compatibility to edible item
 * implementations that do not extend ItemFood for whatever reason.<br>
 * <br>
 * To use it, simply have your item implement {@link IEdible}, and then
 * instead of calling {@code player.getFoodStats().addStats(int, float)}
 * in your item's {@code onEaten} method, you'd instead call:<br>
 * <br>
 * {@code new ItemFoodProxy(this).onEaten(itemStack, player);}
 */
// TODO
//public class ItemFoodProxy extends Item
//{
//	public IEdible proxyEdible;
//
//	public ItemFoodProxy(IEdible proxyEdible)
//	{
//		super(new Item.Properties());
//		this.proxyEdible = proxyEdible;
//	}
//
//	/**
//	 * Applies the food values of the edible item to the player
//	 */
//	public void onEaten(@Nonnull ItemStack itemStack, PlayerEntity player)
//	{
//		player.getFoodStats().addStats(this, itemStack);
//	}
//
//	/**
//	 * @return The hunger value of the edible item
//	 */
//	@Override
//	public int getHealAmount(@Nonnull ItemStack stack)
//	{
//		return proxyEdible.getFoodValues(stack).hunger;
//	}
//
//	/**
//	 * @return The saturation modifier of the edible item
//	 */
//	@Override
//	public float getSaturationModifier(@Nonnull ItemStack stack)
//	{
//		return proxyEdible.getFoodValues(stack).saturationModifier;
//	}
//}