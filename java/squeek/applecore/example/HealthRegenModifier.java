package squeek.applecore.example;

import squeek.applecore.api.hunger.HealthRegenEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class HealthRegenModifier
{

	@SubscribeEvent
	public void allowHealthRegen(HealthRegenEvent.AllowRegen event)
	{
		event.setResult(Result.ALLOW);
	}

	@SubscribeEvent
	public void onRegenTick(HealthRegenEvent.GetRegenTickPeriod event)
	{
		event.regenTickPeriod = 6;
	}

	@SubscribeEvent
	public void onRegen(HealthRegenEvent.Regen event)
	{
		event.deltaHealth = 2;
		event.deltaExhaustion = 5f;
	}

	@SubscribeEvent
	public void onPeacefulRegen(HealthRegenEvent.PeacefulRegen event)
	{
		event.deltaHealth = 0f;
	}

}
