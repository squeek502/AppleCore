package squeek.applecore.example;

import squeek.applecore.api.food.FoodEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class FoodEatenResult
{
	@SubscribeEvent
	public void onFoodEaten(FoodEvent.FoodEaten event)
	{
		if (event.hungerAdded >= 1)
			event.player.heal(1);
	}
}
