package squeek.applecore.example;

import squeek.applecore.api.food.FoodEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class FoodStatsAdditionCanceler
{
	@SubscribeEvent
	public void onFoodStatsAddition(FoodEvent.FoodStatsAddition event)
	{
		if (event.player.getFoodStats().getFoodLevel() > 10)
			event.setCanceled(true);
	}
}
