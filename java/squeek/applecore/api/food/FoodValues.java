package squeek.applecore.api.food;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import squeek.applecore.api.AppleCoreAPI;
import squeek.applecore.api.IAppleCoreAccessor;

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
	 * See {@link IAppleCoreAccessor#getUnmodifiedFoodValues}
	 */
	public static FoodValues getUnmodified(ItemStack itemStack)
	{
		return AppleCoreAPI.accessor.getUnmodifiedFoodValues(itemStack);
	}

	/**
	 * See {@link IAppleCoreAccessor#getFoodValues}
	 */
	public static FoodValues get(ItemStack itemStack)
	{
		return AppleCoreAPI.accessor.getFoodValues(itemStack);
	}

	/**
	 * See {@link IAppleCoreAccessor#getFoodValuesForPlayer}
	 */
	public static FoodValues get(ItemStack itemStack, EntityPlayer player)
	{
		return AppleCoreAPI.accessor.getFoodValuesForPlayer(itemStack, player);
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
