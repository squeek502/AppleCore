package squeek.applecore.example;

import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import squeek.applecore.api.hunger.HealthRegenEvent;

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

	@SubscribeEvent
	public void allowSaturatedHealthRegen(HealthRegenEvent.AllowSaturatedRegen event)
	{
		event.setResult(Result.DEFAULT);
	}

	@SubscribeEvent
	public void onSaturatedRegenTick(HealthRegenEvent.GetSaturatedRegenTickPeriod event)
	{
		event.regenTickPeriod = 20;
	}

	@SubscribeEvent
	public void onSaturatedRegen(HealthRegenEvent.SaturatedRegen event)
	{
		event.deltaHealth = 1;
		event.deltaExhaustion = 1f;
	}
}