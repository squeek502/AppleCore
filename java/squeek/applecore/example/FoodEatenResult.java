package squeek.applecore.example;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import squeek.applecore.api.food.FoodEvent;

public class FoodEatenResult
{
	@SubscribeEvent
	public void onFoodEaten(FoodEvent.FoodEaten event)
	{
		AppleCoreExample.Log.info(event.player.getDisplayNameString() + " ate " + event.food.toString());

		if (event.hungerAdded >= 1)
			event.player.heal(1);
	}
}