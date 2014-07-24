package squeek.applecore.api.food;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.eventhandler.Event;

public abstract class FoodEvent extends Event
{
	public EntityPlayer player;

	public FoodEvent(EntityPlayer player)
	{
		this.player = player;
	}

	public static class GetFoodValues extends FoodEvent
	{
		public FoodValues foodValues;
		public final FoodValues unmodifiedFoodValues;
		public final ItemStack food;

		public GetFoodValues(EntityPlayer player, ItemStack itemStack, FoodValues foodValues)
		{
			super(player);
			this.food = itemStack;
			this.foodValues = foodValues;
			this.unmodifiedFoodValues = foodValues;
		}
	}

	public static class FoodEaten extends FoodEvent
	{
		public FoodValues foodValues;
		public int hungerAdded;
		public float saturationAdded;
		public ItemFood itemFood;
		public ItemStack food;

		public FoodEaten(EntityPlayer player, ItemFood itemFood, ItemStack itemStack, FoodValues foodValues, int hungerAdded, float saturationAdded)
		{
			super(player);
			this.food = itemStack;
			this.itemFood = itemFood;
			this.foodValues = foodValues;
			this.hungerAdded = hungerAdded;
			this.saturationAdded = saturationAdded;
		}
	}
}
