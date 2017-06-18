package squeek.applecore.example;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import squeek.applecore.api.hunger.ExhaustionEvent;

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
		// this will enable hunger loss in peaceful difficulty
		if (event.player.getFoodStats().getSaturationLevel() <= 0)
			event.deltaHunger = -1;
	}

	@SubscribeEvent
	public void onExhaustionAddition(ExhaustionEvent.ExhaustionAddition event)
	{
		// scale all exhaustion additions by 1.5x
		event.deltaExhaustion = event.deltaExhaustion * 1.5f;
	}
}