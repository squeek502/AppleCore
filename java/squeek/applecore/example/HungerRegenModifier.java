package squeek.applecore.example;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import squeek.applecore.api.hunger.HungerRegenEvent;

public class HungerRegenModifier
{
	@SubscribeEvent
	public void onRegen(HungerRegenEvent.PeacefulRegen event)
	{
		if (event.player.getFoodStats().getFoodLevel() > 10)
			event.deltaHunger = 0;
		else
			event.deltaHunger = 1;
	}
}
