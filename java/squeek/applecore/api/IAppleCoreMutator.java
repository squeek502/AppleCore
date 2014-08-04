package squeek.applecore.api;

import net.minecraft.entity.player.EntityPlayer;
import squeek.applecore.api.hunger.HealthRegenEvent;
import squeek.applecore.api.hunger.StarvationEvent;

public interface IAppleCoreMutator
{	
	/**
	 * Sets the exhaustion level of the {@link player}.
	 */
	public void setExhaustion(EntityPlayer player, float exhaustion);

	/**
	 * Sets the hunger of the {@link player} in hunger units (1 hunger unit = 1/2 hunger bar).
	 */
	public void setHunger(EntityPlayer player, int hunger);

	/**
	 * Sets the saturation level of the {@link player}.
	 */
	public void setSaturation(EntityPlayer player, float saturation);

	/**
	 * Sets the health regen tick counter of the {@link player}.
	 * 
	 * See {@link HealthRegenEvent.GetRegenTickPeriod} and {@link HealthRegenEvent.Regen}
	 */
	public void setHealthRegenTickCounter(EntityPlayer player, int tickCounter);


	/**
	 * Sets the starvation tick counter of the {@link player}.
	 * 
	 * See {@link StarvationEvent.GetStarveTickPeriod} and {@link StarvationEvent.Starve}
	 */
	public void setStarveDamageTickCounter(EntityPlayer player, int tickCounter);
}
