package squeek.applecore.api.hunger;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.FoodStats;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.common.MinecraftForge;
import squeek.applecore.api.AppleCoreAPI;

/**
 * Base class for all HungerEvent events.<br>
 * <br>
 * All children of this event are fired on the {@link MinecraftForge#EVENT_BUS}.
 */
public abstract class HungerEvent extends Event
{
	/**
	 * Fired every time max hunger level is retrieved to allow control over its value.
	 * <p>
	 * Note: This also affects max saturation, as saturation is bounded by the player's
	 * current hunger level (that is, a max of 40 hunger would also mean a max of 40
	 * saturation).
	 * </p>
	 * This event is fired in {@link FoodStats#needFood()} and in {@link AppleCoreAPI}.<br>
	 * <br>
	 * {@link #maxHunger} contains the max hunger of the player.<br>
	 * {@link #player} contains the player.<br>
	 * <br>
	 * This event is not {@link Cancelable}.<br>
	 * <br>
	 * This event does not have a result. {@link HasResult}<br>
	 */
	public static class GetMaxHunger extends HungerEvent
	{
		public int maxHunger = 20;
		public final EntityPlayer player;

		public GetMaxHunger(EntityPlayer player)
		{
			this.player = player;
		}
	}
}
