package squeek.applecore.example;

import squeek.applecore.api.plants.PlantGrowthEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class PlantGrowthModifier
{
	@SubscribeEvent
	public void growthTickAllowed(PlantGrowthEvent.AllowGrowthTick event)
	{
		// allow growth as fast as the random ticking allows
		event.setResult(Result.ALLOW);
	}

	@SubscribeEvent
	public void onGrowthTick(PlantGrowthEvent.GrowthTick event)
	{
		AppleCoreExample.Log.info(event.block + " grew from a growth tick");
	}
}
