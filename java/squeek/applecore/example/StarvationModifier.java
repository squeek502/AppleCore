package squeek.applecore.example;

import squeek.applecore.api.FoodEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class StarvationModifier
{

	@SubscribeEvent
	public void allowStarvation(FoodEvent.Starvation.AllowStarvation event)
	{
		event.setResult(Result.ALLOW);
	}

	@SubscribeEvent
	public void onStarveTick(FoodEvent.Starvation.Tick event)
	{
		event.starveTickPeriod = 600;
	}

	@SubscribeEvent
	public void onStarve(FoodEvent.Starvation.Starve event)
	{
		event.starveDamage = 10;
	}

}
