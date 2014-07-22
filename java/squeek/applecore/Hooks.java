package squeek.applecore;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.FoodStats;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import squeek.applecore.api.FoodEvent;
import squeek.applecore.api.FoodValues;

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

	public static float getMaxExhaustion(EntityPlayer player)
	{
		// TODO: implement
		return 0f;
	}

	public static float getHealthRegenPeriod(EntityPlayer player)
	{
		// TODO: implement
		return 0f;
	}

	public static boolean shouldUpdateTick(Block block, World world, int x, int y, int z, Random rand)
	{
		// TODO: implement
		return true;
	}
}
