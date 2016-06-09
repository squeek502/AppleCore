package squeek.applecore.api.hunger;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.FoodStats;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import squeek.applecore.api.AppleCoreAPI;

/**
 * Base class for all HealthRegenEvent events.<br>
 * <br>
 * All children of this event are fired on the {@link MinecraftForge#EVENT_BUS}.
 */
public abstract class HealthRegenEvent extends Event
{
	public final EntityPlayer player;

	public HealthRegenEvent(EntityPlayer player)
	{
		this.player = player;
	}

	/**
	 * Fired each FoodStats update to determine whether or not health regen from food is allowed for the {@link #player}.
	 * However, this event will not be fired if saturated regen occurs, as saturated health regen will take precedence
	 * over normal health regen (see {@link AllowSaturatedRegen}).
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
	 * Fired every time the regen tick period is retrieved to allow control over its value.
	 * 
	 * This event is fired in {@link FoodStats#onUpdate} and in {@link AppleCoreAPI}.<br>
	 * <br>
	 * {@link #regenTickPeriod} contains the number of ticks between each regen.<br>
	 * <br>
	 * This event is not {@link Cancelable}.<br>
	 * <br>
	 * This event does not have a {@link Result}. {@link HasResult}<br>
	 */
	public static class GetRegenTickPeriod extends HealthRegenEvent
	{
		public int regenTickPeriod = 80;

		public GetRegenTickPeriod(EntityPlayer player)
		{
			super(player);
		}
	}

	/**
	 * Fired once the ticks since last regen reaches regenTickPeriod (see {@link GetRegenTickPeriod}),
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
		public float deltaExhaustion = 4f;

		public Regen(EntityPlayer player)
		{
			super(player);
		}
	}

	/**
	 * Fired every second for each player while in Peaceful difficulty,
	 * in order to control how much health to passively regenerate.
	 * 
	 * This event is fired in {@link EntityPlayer#onLivingUpdate}.<br>
	 * <br>
	 * This event is never fired if the game rule "naturalRegeneration" is false.<br>
	 * <br>
	 * {@link #deltaHealth} contains the delta to be applied to health.<br>
	 * <br>
	 * This event is {@link Cancelable}.<br>
	 * If this event is canceled, it will skip healing the player.<br>
	 * <br>
	 * This event does not have a {@link Result}. {@link HasResult}<br>
	 */
	@Cancelable
	public static class PeacefulRegen extends HealthRegenEvent
	{
		public float deltaHealth = 1f;

		public PeacefulRegen(EntityPlayer player)
		{
			super(player);
		}
	}

	/**
	 * Fired each FoodStats update to determine whether or health regen from full hunger + saturation is allowed for the {@link #player}.
	 *
	 * Saturated health regen will take precedence over normal health regen.
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
	public static class AllowSaturatedRegen extends HealthRegenEvent
	{
		public AllowSaturatedRegen(EntityPlayer player)
		{
			super(player);
		}
	}

	/**
	 * Fired every time the saturated regen tick period is retrieved to allow control over its value.
	 *
	 * This event is fired in {@link FoodStats#onUpdate} and in {@link AppleCoreAPI}.<br>
	 * <br>
	 * {@link #regenTickPeriod} contains the number of ticks between each saturated regen.<br>
	 * <br>
	 * This event is not {@link Cancelable}.<br>
	 * <br>
	 * This event does not have a {@link Result}. {@link HasResult}<br>
	 */
	public static class GetSaturatedRegenTickPeriod extends HealthRegenEvent
	{
		public int regenTickPeriod = 10;

		public GetSaturatedRegenTickPeriod(EntityPlayer player)
		{
			super(player);
		}
	}

	/**
	 * Fired once the ticks since last regen reaches regenTickPeriod (see {@link GetSaturatedRegenTickPeriod}),
	 * in order to control how regen affects health/exhaustion.
	 *
	 * By default, the amount of health restored depends on the player's current saturation level.
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
	public static class SaturatedRegen extends HealthRegenEvent
	{
		public float deltaHealth;
		public float deltaExhaustion;

		public SaturatedRegen(EntityPlayer player)
		{
			super(player);
			this.deltaExhaustion = Math.min(player.getFoodStats().getSaturationLevel(), 4.0F);
			this.deltaHealth = deltaExhaustion / 4.0F;
		}
	}
}