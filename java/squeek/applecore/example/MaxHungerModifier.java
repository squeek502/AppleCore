package squeek.applecore.example;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import squeek.applecore.api.hunger.HungerEvent;

public class MaxHungerModifier
{
	@SubscribeEvent
	public void onGetMaxHunger(HungerEvent.GetMaxHunger event)
	{
		event.maxHunger = 60;
	}
}