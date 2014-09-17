package squeek.applecore.asm;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCake;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.FoodStats;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import squeek.applecore.api.AppleCoreAPI;
import squeek.applecore.api.food.FoodEvent;
import squeek.applecore.api.food.FoodValues;
import squeek.applecore.api.hunger.ExhaustionEvent;
import squeek.applecore.api.hunger.HealthRegenEvent;
import squeek.applecore.api.hunger.StarvationEvent;
import squeek.applecore.api.plants.PlantGrowthEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;

public class Hooks
{
	public static FoodValues onFoodStatsAdded(FoodStats foodStats, ItemFood itemFood, ItemStack itemStack, EntityPlayer player)
	{
		return AppleCoreAPI.accessor.getFoodValuesForPlayer(itemStack, player);
	}

	public static void onPostFoodStatsAdded(FoodStats foodStats, ItemFood itemFood, ItemStack itemStack, FoodValues foodValues, int hungerAdded, float saturationAdded, EntityPlayer player)
	{
		MinecraftForge.EVENT_BUS.post(new FoodEvent.FoodEaten(player, itemStack, foodValues, hungerAdded, saturationAdded));
	}

	// should this be moved elsewhere?
	private static ItemStack getFoodFromBlock(Block block)
	{
		if (block instanceof BlockCake)
			return new ItemStack(Items.cake);

		return null;
	}

	public static FoodValues onBlockFoodEaten(Block block, World world, EntityPlayer player)
	{
		ItemStack itemStack = getFoodFromBlock(block);

		if (itemStack != null)
			return AppleCoreAPI.accessor.getFoodValuesForPlayer(itemStack, player);
		else
			return null;
	}

	public static void onPostBlockFoodEaten(Block block, FoodValues foodValues, int prevFoodLevel, float prevSaturationLevel, EntityPlayer player)
	{
		ItemStack itemStack = getFoodFromBlock(block);
		int hungerAdded = player.getFoodStats().getFoodLevel() - prevFoodLevel;
		float saturationAdded = player.getFoodStats().getSaturationLevel() - prevSaturationLevel;

		if (itemStack != null)
			MinecraftForge.EVENT_BUS.post(new FoodEvent.FoodEaten(player, itemStack, foodValues, hungerAdded, saturationAdded));
	}

	public static Result fireAllowExhaustionEvent(EntityPlayer player)
	{
		ExhaustionEvent.AllowExhaustion event = new ExhaustionEvent.AllowExhaustion(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event.getResult();
	}

	public static float fireExhaustionTickEvent(EntityPlayer player, float foodExhaustionLevel)
	{
		return AppleCoreAPI.accessor.getMaxExhaustion(player);
	}

	public static ExhaustionEvent.Exhausted fireExhaustionMaxEvent(EntityPlayer player, float maxExhaustionLevel, float foodExhaustionLevel)
	{
		ExhaustionEvent.Exhausted event = new ExhaustionEvent.Exhausted(player, maxExhaustionLevel, foodExhaustionLevel);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}

	public static Result fireAllowRegenEvent(EntityPlayer player)
	{
		HealthRegenEvent.AllowRegen event = new HealthRegenEvent.AllowRegen(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event.getResult();
	}

	public static int fireRegenTickEvent(EntityPlayer player)
	{
		return AppleCoreAPI.accessor.getHealthRegenTickPeriod(player);
	}

	public static HealthRegenEvent.Regen fireRegenEvent(EntityPlayer player)
	{
		HealthRegenEvent.Regen event = new HealthRegenEvent.Regen(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}

	public static HealthRegenEvent.PeacefulRegen firePeacefulRegenEvent(EntityPlayer player)
	{
		HealthRegenEvent.PeacefulRegen event = new HealthRegenEvent.PeacefulRegen(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}

	public static Result fireAllowStarvation(EntityPlayer player)
	{
		StarvationEvent.AllowStarvation event = new StarvationEvent.AllowStarvation(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event.getResult();
	}

	public static int fireStarvationTickEvent(EntityPlayer player)
	{
		return AppleCoreAPI.accessor.getStarveDamageTickPeriod(player);
	}

	public static StarvationEvent.Starve fireStarveEvent(EntityPlayer player)
	{
		StarvationEvent.Starve event = new StarvationEvent.Starve(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}

	public static boolean fireFoodStatsAdditionEvent(EntityPlayer player, FoodValues foodValuesToBeAdded)
	{
		FoodEvent.FoodStatsAddition event = new FoodEvent.FoodStatsAddition(player, foodValuesToBeAdded);
		MinecraftForge.EVENT_BUS.post(event);
		return event.isCancelable() ? event.isCanceled() : false;
	}

	public static Result fireAllowPlantGrowthEvent(Block block, World world, int x, int y, int z, Random random)
	{
		PlantGrowthEvent.AllowGrowthTick event = new PlantGrowthEvent.AllowGrowthTick(block, world, x, y, z, random);
		MinecraftForge.EVENT_BUS.post(event);
		return event.getResult();
	}

	public static void fireOnGrowthEvent(Block block, World world, int x, int y, int z)
	{
		PlantGrowthEvent.GrowthTick event = new PlantGrowthEvent.GrowthTick(block, world, x, y, z);
		MinecraftForge.EVENT_BUS.post(event);
	}

	public static int toolTipX, toolTipY, toolTipW, toolTipH;

	public static void onDrawHoveringText(int x, int y, int w, int h)
	{
		toolTipX = x;
		toolTipY = y;
		toolTipW = w;
		toolTipH = h;
	}
}
