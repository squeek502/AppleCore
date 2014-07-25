package squeek.applecore.example;

import squeek.applecore.api.hunger.ExhaustionEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ExhaustionModifier
{

	@SubscribeEvent
	public void onExhaustionTick(ExhaustionEvent.Tick event)
	{
		event.maxExhaustionLevel = 30f;
	}

	@SubscribeEvent
	public void onExhausted(ExhaustionEvent.MaxReached event)
	{
		event.deltaHunger = -1;
	}

}
