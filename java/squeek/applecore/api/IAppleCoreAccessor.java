package squeek.applecore.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import squeek.applecore.api.food.FoodValues;

public interface IAppleCoreAccessor
{
	/**
	 * Get player-agnostic food values.
	 */
	public FoodValues getFoodValues(ItemStack food);

	/**
	 * Get player-specific food values.
	 */
	public FoodValues getFoodValuesForPlayer(ItemStack food, EntityPlayer player);

	/**
	 * Get unmodified (vanilla) food values.
	 */
	public FoodValues getUnmodifiedFoodValues(ItemStack food);

	public float getExhaustion(EntityPlayer player);

	public float getMaxExhaustion(EntityPlayer player);

	public int getHealthRegenTickPeriod(EntityPlayer player);

	public int getStarveDamageTickPeriod(EntityPlayer player);
}
