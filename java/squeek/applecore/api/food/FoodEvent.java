package squeek.applecore.api.food;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.FoodStats;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;

/**
 * Base class for all FoodEvent events.<br>
 * <br>
 * All children of this event are fired on the {@link MinecraftForge#EVENT_BUS}.
 */
public abstract class FoodEvent extends Event
{
	public final EntityPlayer player;

	public FoodEvent(EntityPlayer player)
	{
		this.player = player;
	}

	/**
	 * Fired every time food values are retrieved to allow control over their values.
	 * 
	 * This event is fired in {@link FoodStats#func_151686_a(ItemFood, ItemStack)} and in {@link FoodValues}.<br>
	 * <br>
	 * {@link #foodValues} can be modified in order to change the values of the {@link #food}.<br>
	 * {@link #player} is null when getting food values in a player-agnostic context (default values).<br>
	 * {@link #unmodifiedFoodValues} contains the food values of the {@link #food} before the GetFoodValues event was fired.<br>
	 * <br>
	 * This event is not {@link Cancelable}.<br>
	 * <br>
	 * This event does not have a result. {@link HasResult}<br>
	 */
	public static class GetFoodValues extends FoodEvent
	{
		public FoodValues foodValues;
		public final FoodValues unmodifiedFoodValues;
		public final ItemStack food;

		public GetFoodValues(EntityPlayer player, ItemStack itemStack, FoodValues foodValues)
		{
			super(player);
			this.food = itemStack;
			this.foodValues = foodValues;
			this.unmodifiedFoodValues = foodValues;
		}
	}

	/**
	 * Fired after {@link FoodStats#addStats}, containing the effects and context for the food that was eaten.
	 * 
	 * This event is fired in {@link FoodStats#func_151686_a(ItemFood, ItemStack)}.<br>
	 * <br>
	 * This event is not {@link Cancelable}.<br>
	 * <br>
	 * This event does not have a result. {@link HasResult}<br>
	 */
	public static class FoodEaten extends FoodEvent
	{
		public final FoodValues foodValues;
		public final int hungerAdded;
		public final float saturationAdded;
		public final ItemFood itemFood;
		public final ItemStack food;

		public FoodEaten(EntityPlayer player, ItemFood itemFood, ItemStack itemStack, FoodValues foodValues, int hungerAdded, float saturationAdded)
		{
			super(player);
			this.food = itemStack;
			this.itemFood = itemFood;
			this.foodValues = foodValues;
			this.hungerAdded = hungerAdded;
			this.saturationAdded = saturationAdded;
		}
	}

	/**
	 * Fired when hunger/saturation is added to a player's FoodStats, it 
	 * can be canceled to completely prevent the addition.
	 * 
	 * This event is fired in {@link FoodStats#addStats(int, float)}.<br>
	 * <br>
	 * This event is {@link Cancelable}.<br>
	 * If this event is canceled, the hunger and saturation of the FoodStats will not change.<br>
	 * <br>
	 * This event does not have a result. {@link HasResult}<br>
	 */
	@Cancelable
	public static class FoodStatsAddition extends FoodEvent
	{
		public final FoodValues foodValuesToBeAdded;

		public FoodStatsAddition(EntityPlayer player, FoodValues foodValuesToBeAdded)
		{
			super(player);
			this.foodValuesToBeAdded = foodValuesToBeAdded;
		}
	}
}
