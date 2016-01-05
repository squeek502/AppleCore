package squeek.applecore.api.food;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;

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
public class ItemFoodProxy extends ItemFood
{
	public IEdible proxyEdible;

	public ItemFoodProxy(IEdible proxyEdible)
	{
		super(0, false);
		this.proxyEdible = proxyEdible;
	}

	/**
	 * Applies the food values of the edible item to the player
	 */
	public void onEaten(ItemStack itemStack, EntityPlayer player)
	{
		player.getFoodStats().addStats(this, itemStack);
	}

	/**
	 * @return The hunger value of the edible item
	 */
	@Override
	public int getHealAmount(ItemStack stack)
	{
		return proxyEdible.getFoodValues(stack).hunger;
	}

	/**
	 * @return The saturation modifier of the edible item
	 */
	@Override
	public float getSaturationModifier(ItemStack stack)
	{
		return proxyEdible.getFoodValues(stack).saturationModifier;
	}
}