package squeek.applecore.api_impl;

import java.lang.reflect.Field;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.FoodStats;
import net.minecraftforge.common.MinecraftForge;
import squeek.applecore.api.AppleCoreAPI;
import squeek.applecore.api.IAppleCoreAccessor;
import squeek.applecore.api.IAppleCoreMutator;
import squeek.applecore.api.food.FoodEvent;
import squeek.applecore.api.food.FoodValues;
import squeek.applecore.api.food.IEdible;
import squeek.applecore.api.hunger.ExhaustionEvent;
import squeek.applecore.api.hunger.HealthRegenEvent;
import squeek.applecore.api.hunger.StarvationEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;

public enum AppleCoreAccessorMutatorImpl implements IAppleCoreAccessor, IAppleCoreMutator
{
	INSTANCE;

	private AppleCoreAccessorMutatorImpl()
	{
		AppleCoreAPI.accessor = this;
		AppleCoreAPI.mutator = this;
	}

	/*
	 * IAppleCoreAccessor implementation
	 */
	@Override
	public boolean isFood(ItemStack food)
	{
		return isEdible(food) && getUnmodifiedFoodValues(food) != null;
	}

	private boolean isEdible(ItemStack food)
	{
		if (food == null || food.getItem() == null)
			return false;

		EnumAction useAction = food.getItem().getItemUseAction(food);
		if (useAction == EnumAction.eat || useAction == EnumAction.drink)
			return true;

		// assume Block-based foods are edible
		return AppleCoreAPI.registry.getEdibleBlockFromItem(food.getItem()) != null;
	}

	@Override
	public FoodValues getUnmodifiedFoodValues(ItemStack food)
	{
		if (food != null && food.getItem() != null)
		{
			if (food.getItem() instanceof IEdible)
				return ((IEdible) food.getItem()).getFoodValues(food);
			else if (food.getItem() instanceof ItemFood)
				return getItemFoodValues((ItemFood) food.getItem(), food);

			Block block = AppleCoreAPI.registry.getEdibleBlockFromItem(food.getItem());
			if (block != null && block instanceof IEdible)
				return ((IEdible) block).getFoodValues(food);
		}
		return null;
	}

	private FoodValues getItemFoodValues(ItemFood itemFood, ItemStack itemStack)
	{
		return new FoodValues(itemFood.func_150905_g(itemStack), itemFood.func_150906_h(itemStack));
	}

	@Override
	public FoodValues getFoodValues(ItemStack food)
	{
		FoodValues foodValues = getUnmodifiedFoodValues(food);
		if (foodValues != null)
		{
			FoodEvent.GetFoodValues event = new FoodEvent.GetFoodValues(food, foodValues);
			MinecraftForge.EVENT_BUS.post(event);
			return event.foodValues;
		}
		return null;
	}

	@Override
	public FoodValues getFoodValuesForPlayer(ItemStack food, EntityPlayer player)
	{
		FoodValues foodValues = getFoodValues(food);
		if (foodValues != null)
		{
			FoodEvent.GetPlayerFoodValues event = new FoodEvent.GetPlayerFoodValues(player, food, foodValues);
			MinecraftForge.EVENT_BUS.post(event);
			return event.foodValues;
		}
		return null;
	}

	@Override
	public float getExhaustion(EntityPlayer player)
	{
		try
		{
			return foodExhaustion.getFloat(player.getFoodStats());
		}
		catch (RuntimeException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			return 0f;
		}
	}

	@Override
	public float getMaxExhaustion(EntityPlayer player)
	{
		ExhaustionEvent.GetMaxExhaustion event = new ExhaustionEvent.GetMaxExhaustion(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event.maxExhaustionLevel;
	}

	@Override
	public int getHealthRegenTickPeriod(EntityPlayer player)
	{
		HealthRegenEvent.GetRegenTickPeriod event = new HealthRegenEvent.GetRegenTickPeriod(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event.regenTickPeriod;
	}

	@Override
	public int getStarveDamageTickPeriod(EntityPlayer player)
	{
		StarvationEvent.GetStarveTickPeriod event = new StarvationEvent.GetStarveTickPeriod(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event.starveTickPeriod;
	}

	/*
	 * IAppleCoreMutator implementation
	 */
	@Override
	public void setExhaustion(EntityPlayer player, float exhaustion)
	{
		try
		{
			foodExhaustion.setFloat(player.getFoodStats(), exhaustion);
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

	@Override
	public void setHunger(EntityPlayer player, int hunger)
	{
		try
		{
			foodLevel.setInt(player.getFoodStats(), hunger);
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

	@Override
	public void setSaturation(EntityPlayer player, float saturation)
	{
		try
		{
			foodSaturationLevel.setFloat(player.getFoodStats(), saturation);
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

	@Override
	public void setHealthRegenTickCounter(EntityPlayer player, int tickCounter)
	{
		try
		{
			foodTimer.setInt(player.getFoodStats(), tickCounter);
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

	@Override
	public void setStarveDamageTickCounter(EntityPlayer player, int tickCounter)
	{
		try
		{
			starveTimer.setInt(player.getFoodStats(), tickCounter);
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

	// reflection
	static final Field foodLevel = ReflectionHelper.findField(FoodStats.class, "foodLevel", "field_75127_a", "a");
	static final Field foodSaturationLevel = ReflectionHelper.findField(FoodStats.class, "foodSaturationLevel", "field_75125_b", "b");
	static final Field foodExhaustion = ReflectionHelper.findField(FoodStats.class, "foodExhaustionLevel", "field_75126_c", "c");
	static final Field foodTimer = ReflectionHelper.findField(FoodStats.class, "foodTimer", "field_75123_d", "d");
	static final Field starveTimer = ReflectionHelper.findField(FoodStats.class, "starveTimer");
	static
	{
		foodLevel.setAccessible(true);
		foodSaturationLevel.setAccessible(true);
		foodExhaustion.setAccessible(true);
		foodTimer.setAccessible(true);
		starveTimer.setAccessible(true);
	}
}
