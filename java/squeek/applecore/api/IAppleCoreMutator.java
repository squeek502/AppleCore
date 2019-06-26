package squeek.applecore.api;

import net.minecraft.entity.player.PlayerEntity;

public interface IAppleCoreMutator
{	
	/**
	 * Sets the exhaustion level of the {@code player}.
	 */
	void setExhaustion(PlayerEntity player, float exhaustion);

	/**
	 * Sets the hunger of the {@code player} in hunger units (1 hunger unit = 1/2 hunger bar).
	 */
	void setHunger(PlayerEntity player, int hunger);

	/**
	 * Sets the saturation level of the {@code player}.
	 */
	void setSaturation(PlayerEntity player, float saturation);

	/**
	 * Sets the health regen tick counter of the {@code player}.
	 * 
	 * See {@link squeek.applecore.api.hunger.HealthRegenEvent.GetRegenTickPeriod} 
	 * and {@link squeek.applecore.api.hunger.HealthRegenEvent.Regen}
	 */
	void setHealthRegenTickCounter(PlayerEntity player, int tickCounter);
	
	/**
	 * Sets the starvation tick counter of the {@code player}.
	 * 
	 * See {@link squeek.applecore.api.hunger.StarvationEvent.GetStarveTickPeriod} 
	 * and {@link squeek.applecore.api.hunger.StarvationEvent.Starve}
	 */
	void setStarveDamageTickCounter(PlayerEntity player, int tickCounter);
}