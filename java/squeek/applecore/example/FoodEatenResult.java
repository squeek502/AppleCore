package squeek.applecore.example;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import squeek.applecore.api.food.FoodEvent;

public class FoodEatenResult
{
	@SubscribeEvent
	public void onFoodEaten(FoodEvent.FoodEaten event)
	{
		AppleCoreExample.LOG.info(event.player.getDisplayName() + " ate " + event.food.toString());

		if (event.hungerAdded >= 1)
			event.player.heal(1);
	}
}