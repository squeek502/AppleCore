package squeek.applecore.api_impl;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.UseAction;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraftforge.common.MinecraftForge;
import squeek.applecore.api.AppleCoreAPI;
import squeek.applecore.api.IAppleCoreAccessor;
import squeek.applecore.api.IAppleCoreMutator;
import squeek.applecore.api.food.FoodEvent;
import squeek.applecore.api.food.FoodValues;
import squeek.applecore.api.food.IEdible;
import squeek.applecore.api.hunger.ExhaustionEvent;
import squeek.applecore.api.hunger.HealthRegenEvent;
import squeek.applecore.api.hunger.HungerEvent;
import squeek.applecore.api.hunger.StarvationEvent;
import squeek.applecore.asm.util.IAppleCoreFoodStats;

import javax.annotation.Nonnull;

public enum AppleCoreAccessorMutatorImpl implements IAppleCoreAccessor, IAppleCoreMutator
{
	INSTANCE;

	AppleCoreAccessorMutatorImpl()
	{
		AppleCoreAPI.accessor = this;
		AppleCoreAPI.mutator = this;
	}

	/*
	 * IAppleCoreAccessor implementation
	 */
	@Override
	public boolean isFood(@Nonnull ItemStack food)
	{
		return isEdible(food) && getUnmodifiedFoodValues(food) != null;
	}

	private boolean isEdible(@Nonnull ItemStack food)
	{
		if (food == ItemStack.EMPTY)
			return false;

		UseAction useAction = food.getItem().getUseAction(food);
		if (useAction == UseAction.EAT || useAction == UseAction.DRINK)
			return true;

		// assume Block-based foods are edible
		return AppleCoreAPI.registry.getEdibleBlockFromItem(food.getItem()) != null;
	}

	@Override
	public FoodValues getUnmodifiedFoodValues(@Nonnull ItemStack food)
	{
		if (food != ItemStack.EMPTY)
		{
			if (food.getItem() instanceof IEdible)
				return ((IEdible) food.getItem()).getFoodValues(food);
			else if (food.getItem().isFood())
				return getItemFoodValues(food.getItem().getFood(), food);

			Block block = AppleCoreAPI.registry.getEdibleBlockFromItem(food.getItem());
			if (block instanceof IEdible)
				return ((IEdible) block).getFoodValues(food);
		}
		return null;
	}

	private FoodValues getItemFoodValues(@Nonnull Food food, @Nonnull ItemStack stack)
	{
		return new FoodValues(food.getHealing(), food.getSaturation());
	}

	@Override
	public FoodValues getFoodValues(@Nonnull ItemStack food)
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
	public FoodValues getFoodValuesForPlayer(@Nonnull ItemStack food, PlayerEntity player)
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
	public float getExhaustion(PlayerEntity player)
	{
		try
		{
			return getAppleCoreFoodStats(player).getExhaustion();
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
	public float getMaxExhaustion(PlayerEntity player)
	{
		ExhaustionEvent.GetMaxExhaustion event = new ExhaustionEvent.GetMaxExhaustion(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event.maxExhaustionLevel;
	}

	@Override
	public int getHealthRegenTickPeriod(PlayerEntity player)
	{
		HealthRegenEvent.GetRegenTickPeriod event = new HealthRegenEvent.GetRegenTickPeriod(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event.regenTickPeriod;
	}

	@Override
	public int getStarveDamageTickPeriod(PlayerEntity player)
	{
		StarvationEvent.GetStarveTickPeriod event = new StarvationEvent.GetStarveTickPeriod(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event.starveTickPeriod;
	}

	@Override
	public int getSaturatedHealthRegenTickPeriod(PlayerEntity player)
	{
		HealthRegenEvent.GetSaturatedRegenTickPeriod event = new HealthRegenEvent.GetSaturatedRegenTickPeriod(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event.regenTickPeriod;
	}

	@Override
	public int getMaxHunger(PlayerEntity player)
	{
		HungerEvent.GetMaxHunger event = new HungerEvent.GetMaxHunger(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event.maxHunger;
	}

	/*
	 * IAppleCoreMutator implementation
	 */
	@Override
	public void setExhaustion(PlayerEntity player, float exhaustion)
	{
		try
		{
			getAppleCoreFoodStats(player).setExhaustion(exhaustion);
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
	public void setHunger(PlayerEntity player, int hunger)
	{
		player.getFoodStats().setFoodLevel(hunger);
	}

	@Override
	public void setSaturation(PlayerEntity player, float saturation)
	{
		try
		{
			getAppleCoreFoodStats(player).setSaturation(saturation);
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
	public void setHealthRegenTickCounter(PlayerEntity player, int tickCounter)
	{
		try
		{
			getAppleCoreFoodStats(player).setFoodTimer(tickCounter);
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
	public void setStarveDamageTickCounter(PlayerEntity player, int tickCounter)
	{
		try
		{
			getAppleCoreFoodStats(player).setStarveTimer(tickCounter);
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

	public IAppleCoreFoodStats getAppleCoreFoodStats(PlayerEntity player) throws ClassCastException
	{
		return (IAppleCoreFoodStats) player.getFoodStats();
	}
}