package squeek.applecore.example;

import squeek.applecore.api.hunger.ExhaustionEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ExhaustionModifier
{

	@SubscribeEvent
	public void onExhaustionTick(ExhaustionEvent.GetMaxExhaustion event)
	{
		event.maxExhaustionLevel = 30f;
	}

	@SubscribeEvent
	public void onExhausted(ExhaustionEvent.Exhausted event)
	{
		event.deltaHunger = -1;
	}

}
