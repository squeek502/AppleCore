package squeek.applecore.example;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import squeek.applecore.api.AppleCoreAPI;
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

		AppleCoreExample.LOG.info("onExhausted exhaustion=" + AppleCoreAPI.accessor.getExhaustion(event.player));
	}

	@SubscribeEvent
	public void onExhaustionAddition(ExhaustionEvent.ExhaustionAddition event)
	{
		// scale all exhaustion additions by 1.5x
		event.deltaExhaustion = event.deltaExhaustion * 1.5f;
	}

	@SubscribeEvent
	public void onExhaustingAction(ExhaustionEvent.ExhaustingAction event)
	{
		if (event.source == ExhaustionEvent.ExhaustingActions.NORMAL_JUMP)
		{
			// random exhaustion each jump
			event.deltaExhaustion *= Math.random();
		}
		else if (event.source == ExhaustionEvent.ExhaustingActions.SPRINTING_JUMP)
		{
			// note: this is over the default cap of 40, but also over the modified cap
			event.deltaExhaustion = 100.0f;
		}
	}

	@SubscribeEvent
	public void getExhaustionCap(ExhaustionEvent.GetExhaustionCap event)
	{
		event.exhaustionLevelCap = 90.0f;
	}
}