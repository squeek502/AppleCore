package squeek.applecore.api.hunger;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.FoodStats;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;

/**
 * Base class for all HealthRegenEvent events.<br>
 * <br>
 * All children of this event are fired on the {@link MinecraftForge#EVENT_BUS}.
 */
public abstract class HealthRegenEvent extends Event
{
	public EntityPlayer player;

	public HealthRegenEvent(EntityPlayer player)
	{
		this.player = player;
	}

	/**
	 * Fired each FoodStats update to determine whether or not health regen from food is allowed for the {@link #player}.
	 * 
	 * This event is fired in {@link FoodStats#onUpdate}.<br>
	 * <br>
	 * This event is not {@link Cancelable}.<br>
	 * <br>
	 * This event uses the {@link Result}. {@link HasResult}<br>
	 * {@link Result#DEFAULT} will use the vanilla conditionals.<br>
	 * {@link Result#ALLOW} will allow regen without condition.<br>
	 * {@link Result#DENY} will deny regen without condition.<br>
	 */
	@HasResult
	public static class AllowRegen extends HealthRegenEvent
	{
		public AllowRegen(EntityPlayer player)
		{
			super(player);
		}
	}

	/**
	 * Fired each FoodStats update to control the time between each regen.
	 * 
	 * This event is fired in {@link FoodStats#onUpdate}.<br>
	 * <br>
	 * {@link #regenTickPeriod} contains the number of ticks between each regen.<br>
	 * <br>
	 * This event is not {@link Cancelable}.<br>
	 * <br>
	 * This event does not have a {@link Result}. {@link HasResult}<br>
	 */
	public static class Tick extends HealthRegenEvent
	{
		public int regenTickPeriod = 80;

		public Tick(EntityPlayer player)
		{
			super(player);
		}
	}

	/**
	 * Fired once the time since last regen reaches regenTickPeriod (see {@link Tick}),
	 * in order to control how regen affects health/exhaustion.
	 * 
	 * This event is fired in {@link FoodStats#onUpdate}.<br>
	 * <br>
	 * {@link #deltaHealth} contains the delta to be applied to health.<br>
	 * {@link #deltaExhaustion} contains the delta to be applied to exhaustion level.<br>
	 * <br>
	 * This event is {@link Cancelable}.<br>
	 * If this event is canceled, it will skip applying the delta values to health and exhaustion.<br>
	 * <br>
	 * This event does not have a {@link Result}. {@link HasResult}<br>
	 */
	@Cancelable
	public static class Regen extends HealthRegenEvent
	{
		public float deltaHealth = 1f;
		public float deltaExhaustion = -3f;

		public Regen(EntityPlayer player)
		{
			super(player);
		}
	}
}