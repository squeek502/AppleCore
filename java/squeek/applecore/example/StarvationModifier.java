package squeek.applecore.example;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import squeek.applecore.api.hunger.StarvationEvent;

public class StarvationModifier
{
	@SubscribeEvent
	public void allowStarvation(StarvationEvent.AllowStarvation event)
	{
		event.setResult(Event.Result.ALLOW);
	}

	@SubscribeEvent
	public void onStarveTick(StarvationEvent.GetStarveTickPeriod event)
	{
		event.starveTickPeriod = 60;
	}

	@SubscribeEvent(priority= EventPriority.LOWEST)
	public void onStarve(StarvationEvent.Starve event)
	{
		event.starveDamage = 1;
	}
}