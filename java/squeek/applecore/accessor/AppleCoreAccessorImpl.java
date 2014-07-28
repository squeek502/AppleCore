package squeek.applecore.accessor;

import java.lang.reflect.Field;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.FoodStats;
import net.minecraftforge.common.MinecraftForge;
import squeek.applecore.api.AppleCoreAccessor;
import squeek.applecore.api.IAppleCoreAccessor;
import squeek.applecore.api.food.FoodEvent;
import squeek.applecore.api.food.FoodValues;
import squeek.applecore.api.hunger.ExhaustionEvent;
import squeek.applecore.api.hunger.HealthRegenEvent;
import squeek.applecore.api.hunger.StarvationEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;

public enum AppleCoreAccessorImpl implements IAppleCoreAccessor
{
	INSTANCE;

	private AppleCoreAccessorImpl()
	{
		try
		{
			Field apiAccessorImpl = AppleCoreAccessor.class.getDeclaredField("accessorImpl");
			apiAccessorImpl.setAccessible(true);
			apiAccessorImpl.set(null, this);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public boolean isFood(ItemStack food)
	{
		return getUnmodifiedFoodValues(food) != null;
	}

	@Override
	public FoodValues getUnmodifiedFoodValues(ItemStack food)
	{
		if (food.getItem() instanceof ItemFood)
			return getItemFoodValues((ItemFood) food.getItem(), food);
		else if (food.getItem() == Items.cake)
			return new FoodValues(2, 0.1f);
		else
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

	public static void setExhaustion(EntityPlayer player, float exhaustion)
	{
		try
		{
			foodExhaustion.setFloat(player.getFoodStats(), exhaustion);
		}
		catch (Exception e)
		{
		}
	}

	// reflection
	static final Field foodExhaustion = ReflectionHelper.findField(FoodStats.class, "foodExhaustionLevel", "field_75126_c", "c");
	static
	{
		foodExhaustion.setAccessible(true);
	}
}
