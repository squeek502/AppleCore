package squeek.applecore.api.food;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

/**
 * FoodValues is a utility class used to retrieve and hold food values.
 * 
 * To get food values for any given food, use any of the static {@link #get} methods.
 * 
 * <pre>
 * {@code
 * FoodValues appleFoodValues = FoodValues.get(new ItemStack(Items.apple));
 * }
 * </pre>
 */
public class FoodValues
{
	public final int hunger;
	public final float saturationModifier;

	public FoodValues(int hunger, float saturationModifier)
	{
		this.hunger = hunger;
		this.saturationModifier = saturationModifier;
	}

	public FoodValues(FoodValues other)
	{
		this(other.hunger, other.saturationModifier);
	}

	/**
	 * @return The amount of saturation that the food values would provide.
	 */
	public float getSaturationIncrement()
	{
		return Math.min(20, hunger * saturationModifier * 2f);
	}

	/**
	 * Get unmodified (vanilla) food values.
	 */
	public static FoodValues getUnmodified(ItemStack itemStack)
	{
		if (itemStack.getItem() instanceof ItemFood)
			return FoodValues.getUnmodified((ItemFood) itemStack.getItem(), itemStack);
		else
			return null;
	}

	public static FoodValues getUnmodified(ItemFood itemFood, ItemStack itemStack)
	{
		return new FoodValues(itemFood.func_150905_g(itemStack), itemFood.func_150906_h(itemStack));
	}

	/**
	 * Get player-agnostic food values.
	 */
	public static FoodValues get(ItemStack itemStack)
	{
		if (itemStack.getItem() instanceof ItemFood)
			return FoodValues.get((ItemFood) itemStack.getItem(), itemStack);
		else
			return null;
	}

	public static FoodValues get(ItemFood itemFood, ItemStack itemStack)
	{
		return FoodValues.getUnmodified(itemFood, itemStack).applyDefaultModifiers(itemFood, itemStack);
	}

	/**
	 * Get player-specific food values.
	 */
	public static FoodValues get(ItemStack itemStack, EntityPlayer player)
	{
		if (itemStack.getItem() instanceof ItemFood)
			return FoodValues.get((ItemFood) itemStack.getItem(), itemStack, player);
		else
			return null;
	}

	public static FoodValues get(ItemFood itemFood, ItemStack itemStack, EntityPlayer player)
	{
		return FoodValues.get(itemFood, itemStack).applyContextualModifiers(itemFood, itemStack, player);
	}

	FoodValues applyDefaultModifiers(ItemFood itemFood, ItemStack itemStack)
	{
		FoodEvent.GetFoodValues event = new FoodEvent.GetFoodValues(null, itemStack, this);
		MinecraftForge.EVENT_BUS.post(event);
		return event.foodValues;
	}

	FoodValues applyContextualModifiers(ItemFood itemFood, ItemStack itemStack, EntityPlayer player)
	{
		FoodEvent.GetFoodValues event = new FoodEvent.GetFoodValues(player, itemStack, this);
		MinecraftForge.EVENT_BUS.post(event);
		return event.foodValues;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + hunger;
		result = prime * result + Float.floatToIntBits(saturationModifier);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FoodValues other = (FoodValues) obj;
		if (hunger != other.hunger)
			return false;
		if (Float.floatToIntBits(saturationModifier) != Float.floatToIntBits(other.saturationModifier))
			return false;
		return true;
	}

}
