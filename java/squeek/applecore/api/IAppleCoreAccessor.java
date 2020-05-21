package squeek.applecore.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import squeek.applecore.api.food.FoodValues;

import javax.annotation.Nonnull;

public interface IAppleCoreAccessor
{
	/**
	 * Check whether or not the given ItemStack is an edible food.
	 * 
	 * Any ItemStack that gives a return of true in
	 * this method will also return valid FoodValues from
	 * the {@link #getFoodValues}/{@link #getFoodValuesForPlayer} methods.<br>
	 * <br>
	 * This method should be preferred when doing something like
	 * determining whether or not to show food values in an
	 * item's tooltip, as it is more inclusive than a simple
	 * {@code instanceof ItemFood} check.
	 */
	boolean isFood(@Nonnull ItemStack food);

	/**
	 * Check if the given ItemStack can currently be eaten by the player, taking into account their
	 * max hunger, and if the food item is always edible.<br>
	 * <br>
	 * In particular, this method will always return {@code true} if
	 * {@link net.minecraft.util.FoodStats#getFoodLevel} {@code <} {@link #getMaxHunger}
	 * or if this ItemStack's Item is an instance of ItemFood and has its alwaysEdible field set.<br>
	 * <br>
	 * Note: {@link ItemStack#EMPTY} can be passed to this function in order to check whether
	 * the player's hunger is currently below maximum.
	 *
	 * @return {@code true} if the player is currently able to eat the food item, {@code false} otherwise.
	 */
	boolean canPlayerEatFood(@Nonnull ItemStack food, @Nonnull EntityPlayer player);
	
	/**
	 * Get player-agnostic food values.
	 * 
	 * @return The food values, or {@link ItemStack#EMPTY} if none were found.
	 */
	FoodValues getFoodValues(@Nonnull ItemStack food);

	/**
	 * Get player-specific food values.
	 * 
	 * @return The food values, or {@link ItemStack#EMPTY} if none were found.
	 */
	FoodValues getFoodValuesForPlayer(@Nonnull ItemStack food, EntityPlayer player);

	/**
	 * Get unmodified (vanilla) food values.
	 * 
	 * @return The food values, or {@link ItemStack#EMPTY} if none were found.
	 */
	FoodValues getUnmodifiedFoodValues(@Nonnull ItemStack food);

	/**
	 * @return The current exhaustion level of the {@code player}.
	 */
	float getExhaustion(EntityPlayer player);

	/**
	 * @return The maximum exhaustion level of the {@code player}.<br>
	 * <br>
	 * Note: Maximum exhaustion refers to the amount of exhaustion that
	 * will trigger {@link squeek.applecore.api.hunger.ExhaustionEvent.Exhausted} events;
	 * exhaustion can exceed the maximum exhaustion value.
	 */
	float getMaxExhaustion(EntityPlayer player);

	/**
	 * @return The number of ticks between health being regenerated by the {@code player}.
	 */
	int getHealthRegenTickPeriod(EntityPlayer player);

	/**
	 * @return The number of ticks between starvation damage being dealt to the {@code player}.
	 */
	int getStarveDamageTickPeriod(EntityPlayer player);

	/**
	 * @return The number of ticks between health being regenerated by the {@code player} when at full hunger and > 0 saturation.
	 */
	int getSaturatedHealthRegenTickPeriod(EntityPlayer player);

	/**
	 * @return The maximum hunger level of the {@code player}.
	 */
	int getMaxHunger(EntityPlayer player);
}