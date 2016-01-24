package squeek.applecore.api.hunger;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import static net.minecraftforge.fml.common.eventhandler.Event.Result;
import static net.minecraftforge.fml.common.eventhandler.Event.HasResult;

/**
 * Base class for all HealthRegenEvent events.<br>
 * <br>
 * All children of this event are fired on the {@link MinecraftForge#EVENT_BUS}.
 */
public class HungerRegenEvent extends Event
{
	public final EntityPlayer player;

	public HungerRegenEvent(EntityPlayer player)
	{
		this.player = player;
	}

	/**
	 * Fired twice every second for each player while in Peaceful difficulty,
	 * in order to control how much hunger to passively regenerate.
	 * 
	 * This event is fired in {@link EntityPlayer#onLivingUpdate}.<br>
	 * <br>
	 * This event is never fired if the game rule "naturalRegeneration" is false.<br>
	 * <br>
	 * {@link #deltaHunger} contains the delta to be applied to hunger.<br>
	 * <br>
	 * This event is {@link Cancelable}.<br>
	 * If this event is canceled, it will skip adding hunger to the player.<br>
	 * <br>
	 * This event does not have a {@link Result}. {@link HasResult}<br>
	 */
	@Cancelable
	public static class PeacefulRegen extends HungerRegenEvent
	{
		public int deltaHunger = 1;

		public PeacefulRegen(EntityPlayer player)
		{
			super(player);
		}
	}
}
