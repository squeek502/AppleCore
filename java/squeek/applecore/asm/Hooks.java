package squeek.applecore.asm;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCake;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.FoodStats;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import squeek.applecore.api.AppleCoreAPI;
import squeek.applecore.api.food.FoodEvent;
import squeek.applecore.api.food.FoodValues;
import squeek.applecore.api.hunger.ExhaustionEvent;
import squeek.applecore.api.hunger.HealthRegenEvent;
import squeek.applecore.api.hunger.StarvationEvent;
import squeek.applecore.api.plants.FertilizationEvent;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Hooks
{
	public static String getPotionParticleType(boolean flag) {
		 return flag ? "mobSpellAmbient" : "mobSpell";
	}
	
	public static FoodValues onFoodStatsAdded(FoodStats foodStats, ItemFood itemFood, ItemStack itemStack, EntityPlayer player)
	{
		return AppleCoreAPI.accessor.getFoodValuesForPlayer(itemStack, player);
	}

	public static void onPostFoodStatsAdded(FoodStats foodStats, ItemFood itemFood, ItemStack itemStack, FoodValues foodValues, int hungerAdded, float saturationAdded, EntityPlayer player)
	{
		MinecraftForge.EVENT_BUS.post(new FoodEvent.FoodEaten(player, itemStack, foodValues, hungerAdded, saturationAdded));
	}

	public static int getItemInUseMaxDuration(EntityPlayer player, int savedMaxDuration)
	{
		EnumAction useAction = player.getItemInUse().getItemUseAction();
		if (useAction == EnumAction.EAT || useAction == EnumAction.DRINK)
			return savedMaxDuration;
		else
			return player.getItemInUse().getMaxItemUseDuration();
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

	public static Event.Result fireAllowPlantGrowthEvent(Block block, World world, BlockPos pos, IBlockState state, Random random)
	{
		return AppleCoreAPI.dispatcher.validatePlantGrowth(block, world, pos, state, random);
	}

	public static void fireOnGrowthEvent(Block block, World world, BlockPos pos, IBlockState state, int previousMetadata)
	{
		AppleCoreAPI.dispatcher.announcePlantGrowth(block, world, pos, state, previousMetadata);
	}

	public static void fireOnGrowthWithoutMetadataChangeEvent(Block block, World world, BlockPos pos, IBlockState state)
	{
		AppleCoreAPI.dispatcher.announcePlantGrowthWithoutMetadataChange(block, world, pos, state);
	}

	private static final Random fertilizeRandom = new Random();
	private static final Map<String, Class<?>[]> fertilizeMethods = new HashMap<String, Class<?>[]>();
	static 
	{
		fertilizeMethods.put(ASMConstants.HarvestCraft.BlockPamFruit, new Class<?>[] { World.class, int.class, int.class, int.class });
		fertilizeMethods.put(ASMConstants.HarvestCraft.BlockPamSapling, new Class<?>[] { World.class, int.class, int.class, int.class, Random.class });
	}

	public static void fireAppleCoreFertilizeEvent(Block block, World world, BlockPos pos, IBlockState state, Random random)
	{
		if (random == null)
			random = fertilizeRandom;

		int previousMetadata = state.getBlock().getMetaFromState(state);

		FertilizationEvent.Fertilize event = new FertilizationEvent.Fertilize(block, world, pos, state, random, previousMetadata);
		MinecraftForge.EVENT_BUS.post(event);
		Event.Result fertilizeResult = event.getResult();

		if (fertilizeResult == Event.Result.DENY)
			return;

		if (fertilizeResult == Event.Result.DEFAULT)
		{
			try
			{
				Method renamedFertilize;
				if (fertilizeMethods.containsKey(block.getClass().getName()))
				{
					Class<?>[] argTypes = fertilizeMethods.get(block.getClass().getName());
					renamedFertilize = block.getClass().getMethod("AppleCore_fertilize", argTypes);
					if (argTypes.length == 4)
						renamedFertilize.invoke(block, world, pos, state);
					else
						renamedFertilize.invoke(block, world, pos, state, random);
				}
				else
				{
					renamedFertilize = block.getClass().getMethod("AppleCore_fertilize", World.class, Random.class, int.class, int.class, int.class);
					renamedFertilize.invoke(block, world, random, pos, state);
				}
			}
			catch (RuntimeException e)
			{
				throw e;
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}

		Hooks.fireFertilizedEvent(block, world, pos, state, previousMetadata);
	}

	public static void fireFertilizedEvent(Block block, World world, BlockPos pos, IBlockState state, int previousMetadata)
	{
		FertilizationEvent.Fertilized event = new FertilizationEvent.Fertilized(block, world, pos, state, previousMetadata);
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
