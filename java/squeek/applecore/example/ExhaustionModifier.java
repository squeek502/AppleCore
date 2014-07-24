package squeek.applecore.example;

import squeek.applecore.api.FoodEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ExhaustionModifier
{

	@SubscribeEvent
	public void onStarveTick(FoodEvent.Exhaustion.Tick event)
	{
		event.maxExhaustionLevel = 1f;
	}

	@SubscribeEvent
	public void onStarve(FoodEvent.Exhaustion.MaxReached event)
	{
		event.deltaHunger = -1;
	}

}
