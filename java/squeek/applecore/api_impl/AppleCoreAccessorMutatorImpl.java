package squeek.applecore.api_impl;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
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

		// assume Block-based foods are edible
		if (food.getItem() == Items.CAKE || food.getItem() instanceof ItemBlock)
			return true;

		EnumAction useAction = food.getItem().getItemUseAction(food);
		return useAction == EnumAction.EAT || useAction == EnumAction.DRINK;
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
			else if (food.getItem() == Items.CAKE)
				return new FoodValues(2, 0.1f);
		}
		return null;
	}

	private FoodValues getItemFoodValues(ItemFood itemFood, ItemStack itemStack)
	{
		return new FoodValues(itemFood.getHealAmount(itemStack), itemFood.getSaturationModifier(itemStack));
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

	@Override
	public int getSaturatedHealthRegenTickPeriod(EntityPlayer player)
	{
		HealthRegenEvent.GetSaturatedRegenTickPeriod event = new HealthRegenEvent.GetSaturatedRegenTickPeriod(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event.regenTickPeriod;
	}

	@Override
	public int getMaxHunger(EntityPlayer player)
	{
		HungerEvent.GetMaxHunger event = new HungerEvent.GetMaxHunger(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event.maxHunger;
	}

	/*
	 * IAppleCoreMutator implementation
	 */
	@Override
	public void setExhaustion(EntityPlayer player, float exhaustion)
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
	public void setHunger(EntityPlayer player, int hunger)
	{
		player.getFoodStats().setFoodLevel(hunger);
	}

	@Override
	public void setSaturation(EntityPlayer player, float saturation)
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
	public void setHealthRegenTickCounter(EntityPlayer player, int tickCounter)
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
	public void setStarveDamageTickCounter(EntityPlayer player, int tickCounter)
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

	public IAppleCoreFoodStats getAppleCoreFoodStats(EntityPlayer player) throws ClassCastException
	{
		return (IAppleCoreFoodStats) player.getFoodStats();
	}
}