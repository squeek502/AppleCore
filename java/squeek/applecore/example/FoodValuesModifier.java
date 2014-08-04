package squeek.applecore.example;

import net.minecraft.init.Items;
import squeek.applecore.api.food.FoodEvent;
import squeek.applecore.api.food.FoodValues;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class FoodValuesModifier
{
	@SubscribeEvent
	public void getFoodValues(FoodEvent.GetFoodValues event)
	{
		if (event.food.getItem() == Items.apple)
			event.foodValues = new FoodValues(5, 1f);
	}

	@SubscribeEvent
	public void getPlayerSpecificFoodValues(FoodEvent.GetPlayerFoodValues event)
	{
		if (event.food.getItem() == Items.apple)
			event.foodValues = new FoodValues(19, 1f);
		else
			event.foodValues = new FoodValues((20 - event.player.getFoodStats().getFoodLevel()) / 8, 1);
	}
}
