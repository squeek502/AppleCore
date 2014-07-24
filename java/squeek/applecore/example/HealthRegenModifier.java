package squeek.applecore.example;

import squeek.applecore.api.FoodEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class HealthRegenModifier
{

	@SubscribeEvent
	public void allowHealthRegen(FoodEvent.RegenHealth.AllowRegen event)
	{
		event.setResult(Result.ALLOW);
	}

	@SubscribeEvent
	public void onRegenTick(FoodEvent.RegenHealth.Tick event)
	{
		event.regenTickPeriod = 6;
	}

	@SubscribeEvent
	public void onRegen(FoodEvent.RegenHealth.Regen event)
	{
		event.deltaHealth = 2;
		event.deltaExhaustion = 5f;
	}

}
