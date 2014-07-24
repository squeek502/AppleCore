package squeek.applecore.example;

import squeek.applecore.api.hunger.ExhaustionEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ExhaustionModifier
{

	@SubscribeEvent
	public void onStarveTick(ExhaustionEvent.Tick event)
	{
		event.maxExhaustionLevel = 30f;
	}

	@SubscribeEvent
	public void onStarve(ExhaustionEvent.MaxReached event)
	{
		event.deltaHunger = -1;
	}

}
