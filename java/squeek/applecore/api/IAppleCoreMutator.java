package squeek.applecore.api;

import net.minecraft.entity.player.EntityPlayer;

public interface IAppleCoreMutator
{	
	/**
	 * Set the exhaustion level of the {@link player}.
	 */
	public void setExhaustion(EntityPlayer player, float exhaustion);

	/**
	 * Set the hunger of the {@link player}.
	 * 
	 * {@link hunger} is in hunger units (1 hunger unit = 1/2 hunger bar).
	 */
	public void setHunger(EntityPlayer player, int hunger);

	/**
	 * Set the saturation level of the {@link player}.
	 */
	public void setSaturation(EntityPlayer player, float saturation);
}
