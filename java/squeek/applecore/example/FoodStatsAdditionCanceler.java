package squeek.applecore.example;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import squeek.applecore.api.AppleCoreAPI;
import squeek.applecore.api.food.FoodEvent;

public class FoodStatsAdditionCanceler
{
	@SubscribeEvent
	public void onFoodStatsAddition(FoodEvent.FoodStatsAddition event)
	{
		if (event.player.getFoodStats().getFoodLevel() > AppleCoreAPI.accessor.getMaxHunger(event.player) / 2)
			event.setCanceled(true);
	}
}
