package squeek.applecore;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.FoodStats;
import net.minecraftforge.common.MinecraftForge;
import squeek.applecore.api.FoodEvent;
import squeek.applecore.api.FoodValues;
import cpw.mods.fml.common.eventhandler.Event.Result;

public class Hooks
{
	/**
	 * Hooks into ItemStack-aware FoodStats.addStats method
	 * @param foodStats The food stats being added to
	 * @param itemFood The item of food that is being eaten
	 * @param itemStack The ItemStack of the food that is being eaten
	 * @param player The player eating the food
	 * @return The modified food values or null if the default code should be executed
	 */
	public static FoodValues onFoodStatsAdded(FoodStats foodStats, ItemFood itemFood, ItemStack itemStack, EntityPlayer player)
	{
		return FoodValues.get(itemFood, itemStack, player);
	}

	public static void onPostFoodStatsAdded(FoodStats foodStats, ItemFood itemFood, ItemStack itemStack, FoodValues foodValues, int hungerAdded, float saturationAdded, EntityPlayer player)
	{
		MinecraftForge.EVENT_BUS.post(new FoodEvent.FoodEaten(player, itemFood, itemStack, foodValues, hungerAdded, saturationAdded));
	}

	public static FoodEvent.Exhaustion.Tick fireExhaustionTickEvent(EntityPlayer player, float foodExhaustionLevel)
	{
		FoodEvent.Exhaustion.Tick event = new FoodEvent.Exhaustion.Tick(player, foodExhaustionLevel);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}

	public static FoodEvent.Exhaustion.MaxReached fireExhaustionMaxEvent(EntityPlayer player, float maxExhaustionLevel, float foodExhaustionLevel)
	{
		FoodEvent.Exhaustion.MaxReached event = new FoodEvent.Exhaustion.MaxReached(player, maxExhaustionLevel, foodExhaustionLevel);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}

	public static Result fireAllowRegenEvent(EntityPlayer player)
	{
		FoodEvent.RegenHealth.AllowRegen event = new FoodEvent.RegenHealth.AllowRegen(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event.getResult();
	}

	public static int fireRegenTickEvent(EntityPlayer player)
	{
		FoodEvent.RegenHealth.Tick event = new FoodEvent.RegenHealth.Tick(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event.regenTickPeriod;
	}

	public static FoodEvent.RegenHealth.Regen fireRegenEvent(EntityPlayer player)
	{
		FoodEvent.RegenHealth.Regen event = new FoodEvent.RegenHealth.Regen(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}

	public static Result fireAllowStarvation(EntityPlayer player)
	{
		FoodEvent.Starvation.AllowStarvation event = new FoodEvent.Starvation.AllowStarvation(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event.getResult();
	}

	public static int fireStarvationTickEvent(EntityPlayer player)
	{
		FoodEvent.Starvation.Tick event = new FoodEvent.Starvation.Tick(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event.starveTickPeriod;
	}

	public static FoodEvent.Starvation.Starve fireStarveEvent(EntityPlayer player)
	{
		FoodEvent.Starvation.Starve event = new FoodEvent.Starvation.Starve(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}
}
