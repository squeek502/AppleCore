package squeek.applecore.asm;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCake;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.FoodStats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import squeek.applecore.api.AppleCoreAPI;
import squeek.applecore.api.food.FoodEvent;
import squeek.applecore.api.food.FoodValues;
import squeek.applecore.api.hunger.ExhaustionEvent;
import squeek.applecore.api.hunger.HealthRegenEvent;
import squeek.applecore.api.hunger.HungerRegenEvent;
import squeek.applecore.api.hunger.StarvationEvent;
import squeek.applecore.api.plants.FertilizationEvent;
import squeek.applecore.asm.util.IAppleCoreFertilizable;
import squeek.applecore.asm.util.IAppleCoreFoodStats;

import java.util.Random;

public class Hooks
{
	public static boolean onAppleCoreFoodStatsUpdate(FoodStats foodStats, EntityPlayer player)
	{
		if (!(foodStats instanceof IAppleCoreFoodStats))
			return false;

		IAppleCoreFoodStats appleCoreFoodStats = (IAppleCoreFoodStats) foodStats;

		appleCoreFoodStats.setPrevFoodLevel(foodStats.getFoodLevel());

		Result allowExhaustionResult = Hooks.fireAllowExhaustionEvent(player);
		float maxExhaustion = Hooks.fireExhaustionTickEvent(player, appleCoreFoodStats.getExhaustion());
		if (allowExhaustionResult == Result.ALLOW || (allowExhaustionResult == Result.DEFAULT && appleCoreFoodStats.getExhaustion() >= maxExhaustion))
		{
			ExhaustionEvent.Exhausted exhaustedEvent = Hooks.fireExhaustionMaxEvent(player, maxExhaustion, appleCoreFoodStats.getExhaustion());

			appleCoreFoodStats.setExhaustion(appleCoreFoodStats.getExhaustion() + exhaustedEvent.deltaExhaustion);
			if (!exhaustedEvent.isCanceled())
			{
				appleCoreFoodStats.setSaturation(Math.max(foodStats.getSaturationLevel() + exhaustedEvent.deltaSaturation, 0.0F));
				foodStats.setFoodLevel(Math.max(foodStats.getFoodLevel() + exhaustedEvent.deltaHunger, 0));
			}
		}

		boolean hasNaturalRegen = player.world.getGameRules().getBoolean("naturalRegeneration");

		Result allowSaturatedRegenResult = Hooks.fireAllowSaturatedRegenEvent(player);
		boolean shouldDoSaturatedRegen = allowSaturatedRegenResult == Result.ALLOW || (allowSaturatedRegenResult == Result.DEFAULT && hasNaturalRegen && foodStats.getSaturationLevel() > 0.0F && player.shouldHeal() && foodStats.getFoodLevel() >= 20);
		Result allowRegenResult = shouldDoSaturatedRegen ? Result.DENY : Hooks.fireAllowRegenEvent(player);
		boolean shouldDoRegen = allowRegenResult == Result.ALLOW || (allowRegenResult == Result.DEFAULT && hasNaturalRegen && foodStats.getFoodLevel() >= 18 && player.shouldHeal());
		if (shouldDoSaturatedRegen)
		{
			appleCoreFoodStats.setFoodTimer(appleCoreFoodStats.getFoodTimer()+1);

			if (appleCoreFoodStats.getFoodTimer() >= Hooks.fireSaturatedRegenTickEvent(player))
			{
				HealthRegenEvent.SaturatedRegen saturatedRegenEvent = Hooks.fireSaturatedRegenEvent(player);
				if (!saturatedRegenEvent.isCanceled())
				{
					player.heal(saturatedRegenEvent.deltaHealth);
					foodStats.addExhaustion(saturatedRegenEvent.deltaExhaustion);
				}
				appleCoreFoodStats.setFoodTimer(0);
			}
		}
		else if (shouldDoRegen)
		{
			appleCoreFoodStats.setFoodTimer(appleCoreFoodStats.getFoodTimer()+1);

			if (appleCoreFoodStats.getFoodTimer() >= Hooks.fireRegenTickEvent(player))
			{
				HealthRegenEvent.Regen regenEvent = Hooks.fireRegenEvent(player);
				if (!regenEvent.isCanceled())
				{
					player.heal(regenEvent.deltaHealth);
					foodStats.addExhaustion(regenEvent.deltaExhaustion);
				}
				appleCoreFoodStats.setFoodTimer(0);
			}
		}
		else
		{
			appleCoreFoodStats.setFoodTimer(0);
		}

		Result allowStarvationResult = Hooks.fireAllowStarvation(player);
		if (allowStarvationResult == Result.ALLOW || (allowStarvationResult == Result.DEFAULT && foodStats.getFoodLevel() <= 0))
		{
			appleCoreFoodStats.setStarveTimer(appleCoreFoodStats.getStarveTimer()+1);

			if (appleCoreFoodStats.getStarveTimer() >= Hooks.fireStarvationTickEvent(player))
			{
				StarvationEvent.Starve starveEvent = Hooks.fireStarveEvent(player);
				if (!starveEvent.isCanceled())
				{
					player.attackEntityFrom(DamageSource.starve, starveEvent.starveDamage);
				}
				appleCoreFoodStats.setStarveTimer(0);
			}
		}
		else
		{
			appleCoreFoodStats.setStarveTimer(0);
		}

		return true;
	}

	public static FoodValues onFoodStatsAdded(FoodStats foodStats, ItemFood itemFood, ItemStack itemStack, EntityPlayer player)
	{
		return AppleCoreAPI.accessor.getFoodValuesForPlayer(itemStack, player);
	}

	public static void onPostFoodStatsAdded(FoodStats foodStats, ItemFood itemFood, ItemStack itemStack, FoodValues foodValues, int hungerAdded, float saturationAdded, EntityPlayer player)
	{
		MinecraftForge.EVENT_BUS.post(new FoodEvent.FoodEaten(player, itemStack, foodValues, hungerAdded, saturationAdded));
	}

	public static int getItemInUseMaxCount(EntityLivingBase entityLiving, int savedMaxDuration)
	{
		EnumAction useAction = entityLiving.getActiveItemStack().getItemUseAction();
		if (useAction == EnumAction.EAT || useAction == EnumAction.DRINK)
			return savedMaxDuration;
		else
			return entityLiving.getActiveItemStack().getMaxItemUseDuration();
	}

	// should this be moved elsewhere?
	private static ItemStack getFoodFromBlock(Block block)
	{
		if (block instanceof BlockCake)
			return new ItemStack(Items.CAKE);

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

	public static Result fireAllowSaturatedRegenEvent(EntityPlayer player)
	{
		HealthRegenEvent.AllowSaturatedRegen event = new HealthRegenEvent.AllowSaturatedRegen(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event.getResult();
	}

	public static int fireRegenTickEvent(EntityPlayer player)
	{
		return AppleCoreAPI.accessor.getHealthRegenTickPeriod(player);
	}

	public static int fireSaturatedRegenTickEvent(EntityPlayer player)
	{
		return AppleCoreAPI.accessor.getSaturatedHealthRegenTickPeriod(player);
	}

	public static HealthRegenEvent.Regen fireRegenEvent(EntityPlayer player)
	{
		HealthRegenEvent.Regen event = new HealthRegenEvent.Regen(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}

	public static HealthRegenEvent.SaturatedRegen fireSaturatedRegenEvent(EntityPlayer player)
	{
		HealthRegenEvent.SaturatedRegen event = new HealthRegenEvent.SaturatedRegen(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}

	public static HealthRegenEvent.PeacefulRegen firePeacefulRegenEvent(EntityPlayer player)
	{
		HealthRegenEvent.PeacefulRegen event = new HealthRegenEvent.PeacefulRegen(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}

	public static HungerRegenEvent.PeacefulRegen firePeacefulHungerRegenEvent(EntityPlayer player)
	{
		HungerRegenEvent.PeacefulRegen event = new HungerRegenEvent.PeacefulRegen(player);
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

	private static final Random fertilizeRandom = new Random();

	public static void fireAppleCoreFertilizeEvent(Block block, World world, BlockPos pos, IBlockState state, Random random)
	{
		// if the block does not implement our dummy interface, then the transformation did occur
		if (!(block instanceof IAppleCoreFertilizable))
			return;

		if (random == null)
			random = fertilizeRandom;

		IBlockState previousState = state;

		FertilizationEvent.Fertilize event = new FertilizationEvent.Fertilize(block, world, pos, state, random);
		MinecraftForge.EVENT_BUS.post(event);
		Event.Result fertilizeResult = event.getResult();

		if (fertilizeResult == Event.Result.DENY)
			return;

		if (fertilizeResult == Event.Result.DEFAULT)
		{
			IAppleCoreFertilizable fertilizableBlock = (IAppleCoreFertilizable) block;
			try
			{
				fertilizableBlock.AppleCore_fertilize(world, random, pos, state);
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

		Hooks.fireFertilizedEvent(block, world, pos, previousState);
	}

	public static void fireFertilizedEvent(Block block, World world, BlockPos pos, IBlockState previousState)
	{
		FertilizationEvent.Fertilized event = new FertilizationEvent.Fertilized(block, world, pos, previousState);
		MinecraftForge.EVENT_BUS.post(event);
	}
}
