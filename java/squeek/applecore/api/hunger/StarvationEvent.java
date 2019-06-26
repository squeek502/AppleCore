package squeek.applecore.api.hunger;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.FoodStats;
import net.minecraft.world.Difficulty;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import squeek.applecore.api.AppleCoreAPI;

/**
 * Base class for all StarvationEvent events.<br>
 * <br>
 * All children of this event are fired on the {@link MinecraftForge#EVENT_BUS}.
 */
public abstract class StarvationEvent extends Event
{
	public final PlayerEntity player;

	public StarvationEvent(PlayerEntity player)
	{
		this.player = player;
	}

	/**
	 * Fired each FoodStats update to determine whether or not starvation is allowed for the {@link #player}.
	 * 
	 * This event is fired in {@link FoodStats#tick}.<br>
	 * <br>
	 * This event is not {@link Cancelable}.<br>
	 * <br>
	 * This event uses the {@link Result}. {@link HasResult}<br>
	 * {@link Result#DEFAULT} will use the vanilla conditionals.
	 * {@link Result#ALLOW} will allow starvation without condition.
	 * {@link Result#DENY} will deny starvation without condition.
	 */
	@HasResult
	public static class AllowStarvation extends StarvationEvent
	{
		public AllowStarvation(PlayerEntity player)
		{
			super(player);
		}
	}

	/**
	 * Fired every time the starve tick period is retrieved to allow control over its value.
	 * 
	 * This event is fired in {@link FoodStats#tick} and in {@link AppleCoreAPI}.<br>
	 * <br>
	 * {@link #starveTickPeriod} contains the number of ticks between starvation damage being done.<br>
	 * <br>
	 * This event is not {@link Cancelable}.<br>
	 * <br>
	 * This event does not have a {@link Result}. {@link HasResult}<br>
	 */
	public static class GetStarveTickPeriod extends StarvationEvent
	{
		public int starveTickPeriod = 80;

		public GetStarveTickPeriod(PlayerEntity player)
		{
			super(player);
		}
	}

	/**
	 * Fired once the time since last starvation damage reaches starveTickPeriod (see {@link GetStarveTickPeriod}),
	 * in order to control how much starvation damage to do.
	 * 
	 * This event is fired in {@link FoodStats#tick}.<br>
	 * <br>
	 * {@link #starveDamage} contains the amount of damage to deal from starvation.<br>
	 * <br>
	 * This event is {@link Cancelable}.<br>
	 * If this event is canceled, it will skip dealing starvation damage.<br>
	 * <br>
	 * This event does not have a {@link Result}. {@link HasResult}<br>
	 */
	@Cancelable
	public static class Starve extends StarvationEvent
	{
		public float starveDamage = 1f;

		public Starve(PlayerEntity player)
		{
			super(player);

			Difficulty difficulty = player.world.getDifficulty();
			boolean shouldDoDamage = player.getHealth() > 10.0F || difficulty == Difficulty.HARD || player.getHealth() > 1.0F && difficulty == Difficulty.NORMAL;

			if (!shouldDoDamage)
				starveDamage = 0f;
		}
	}
}