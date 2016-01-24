package squeek.applecore.example;

import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockStem;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import squeek.applecore.api.plants.PlantGrowthEvent;

public class PlantGrowthModifier
{
	@SubscribeEvent
	public void growthTickAllowed(PlantGrowthEvent.AllowGrowthTick event)
	{
		if (event.block instanceof BlockCocoa)
			// disable cocoa growth completely
			event.setResult(Result.DENY);
		else if (event.block instanceof BlockStem)
			// normal growth for melon/pumpkin
			event.setResult(Result.DEFAULT);
		else
			// everything else grows as fast as the random ticking allows
			event.setResult(Result.ALLOW);
	}

	@SubscribeEvent
	public void onGrowthTick(PlantGrowthEvent.GrowthTick event)
	{
		AppleCoreExample.Log.info(event.block + " grew from a growth tick (from " + event.previousState.toString() + " to " + event.currentState.toString() + ")");
	}
}