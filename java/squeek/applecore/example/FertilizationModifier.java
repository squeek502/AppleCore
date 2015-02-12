package squeek.applecore.example;

import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockStem;
import squeek.applecore.api.plants.FertilizationEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class FertilizationModifier
{
	@SubscribeEvent
	public void onFertilize(FertilizationEvent.Fertilize event)
	{
		// disable cocoa fertilization completely
		if (event.block instanceof BlockCocoa)
		{
			AppleCoreExample.Log.info(event.block + " fertilization denied");
			event.setResult(Result.DENY);
		}
		// custom fertilization effect for stems
		else if (event.block instanceof BlockStem)
		{
			// randomize the meta
			AppleCoreExample.Log.info("randomizing meta of " + event.block + " using custom fertilization handling");
			event.world.setBlockMetadataWithNotify(event.x, event.y, event.z, event.random.nextInt(7), 1);
			// mark as handled
			event.setResult(Result.ALLOW);
		}
		else
		{
			AppleCoreExample.Log.info(event.block + " is about to be fertilized (current meta: " + event.metadata + ")");
		}
	}

	@SubscribeEvent
	public void onFertilized(FertilizationEvent.Fertilized event)
	{
		int currentMetadata = event.world.getBlockMetadata(event.x, event.y, event.z);
		AppleCoreExample.Log.info(event.block + " was fertilized from meta " + event.previousMetadata + " to " + currentMetadata);
	}
}
