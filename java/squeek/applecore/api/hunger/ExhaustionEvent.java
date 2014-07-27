package squeek.applecore.api.hunger;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.FoodStats;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;

/**
 * Base class for all ExhaustionEvent events.<br>
 * <br>
 * All children of this event are fired on the {@link MinecraftForge#EVENT_BUS}.
 */
public abstract class ExhaustionEvent extends Event
{
	public EntityPlayer player;

	public ExhaustionEvent(EntityPlayer player)
	{
		this.player = player;
	}

	/**
	 * Fired each FoodStats update to control exhaustion level and max exhaustion level
	 * 
	 * This event is fired in {@link FoodStats#onUpdate}.<br>
	 * <br>
	 * {@link #exhaustionLevel} contains the exhaustion level of the {@link #player}.<br>
	 * {@link #maxExhaustionLevel} determines the exhaustion level that will trigger a hunger/saturation decrement.<br>
	 * <br>
	 * This event is {@link Cancelable}.<br>
	 * If this event is canceled, it will skip any potential hunger/saturation decrements for this update tick.<br>
	 * <br>
	 * This event does not have a result. {@link HasResult}<br>
	 */
	@Cancelable
	public static class Tick extends ExhaustionEvent
	{
		public float exhaustionLevel;
		public float maxExhaustionLevel = 4f;

		public Tick(EntityPlayer player, float exhaustionLevel)
		{
			super(player);
			this.exhaustionLevel = exhaustionLevel;
		}
	}

	/**
	 * Fired once exchaustionLevel exceeds maxExhaustionLevel (see {@link Tick}),
	 * in order to control how exhaustion affects hunger/saturation.
	 * 
	 * This event is fired in {@link FoodStats#onUpdate}.<br>
	 * <br>
	 * {@link #currentExhaustionLevel} contains the exhaustion level of the {@link #player}.<br>
	 * {@link #deltaExhaustion} contains the delta to be applied to exhaustion level (default: {@link Tick#maxExhaustionLevel}).<br>
	 * {@link #deltaHunger} contains the delta to be applied to hunger.<br>
	 * {@link #deltaSaturation} contains the delta to be applied to saturation.<br>
	 * <br>
	 * Note: {@link #deltaHunger} and {@link #deltaSaturation} will vary depending on their vanilla conditionals.
	 * For example, deltaHunger will be 0 when this event is fired in Peaceful difficulty.<br> 
	 * <br>
	 * This event is {@link Cancelable}.<br>
	 * If this event is canceled, it will skip applying the delta values to exhaustion, hunger, and saturation.<br>
	 * <br>
	 * This event does not have a result. {@link HasResult}<br>
	 */
	@Cancelable
	public static class MaxReached extends ExhaustionEvent
	{
		public final float currentExhaustionLevel;
		public float deltaExhaustion;
		public int deltaHunger = -1;
		public float deltaSaturation = -1f;

		public MaxReached(EntityPlayer player, float exhaustionToRemove, float currentExhaustionLevel)
		{
			super(player);
			this.deltaExhaustion = -exhaustionToRemove;
			this.currentExhaustionLevel = currentExhaustionLevel;

			boolean shouldDecreaseSaturationLevel = player.getFoodStats().getSaturationLevel() > 0f;

			if (!shouldDecreaseSaturationLevel)
				deltaSaturation = 0f;

			EnumDifficulty difficulty = player.worldObj.difficultySetting;
			boolean shouldDecreaseFoodLevel = !shouldDecreaseSaturationLevel && difficulty != EnumDifficulty.PEACEFUL;

			if (!shouldDecreaseFoodLevel)
				deltaHunger = 0;
		}
	}
}