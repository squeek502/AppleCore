package squeek.applecore.example;

import java.util.Random;
import net.minecraft.block.BlockCrops;
import net.minecraft.world.World;
import squeek.applecore.api.AppleCoreAPI;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.Event.Result;

public class BlockCropsExample extends BlockCrops
{
	// abstract the AppleCoreAPI reference into an Optional.Method so that AppleCore is not a hard dependency
	@Optional.Method(modid = "AppleCore")
	public Event.Result validateAppleCoreGrowthTick(World world, int x, int y, int z, Random random)
	{
		return AppleCoreAPI.dispatcher.validatePlantGrowth(this, world, x, y, z, random);
	}

	// abstract the AppleCoreAPI reference into an Optional.Method so that AppleCore is not a hard dependency
	@Optional.Method(modid = "AppleCore")
	public void announceAppleCoreGrowthTick(World world, int x, int y, int z, int previousMetadata)
	{
		AppleCoreAPI.dispatcher.announcePlantGrowth(this, world, x, y, z, previousMetadata);
	}

	/**
	 * A custom updateTick implementation (that doesn't call super.updateTick)
	 * will not fire the AppleCore events that get inserted into BlockCrops.updateTick,
	 * which means that the custom implementation will need to fire and handle them
	 */
	@Override
	public void updateTick(World world, int x, int y, int z, Random random)
	{
		// get the result of the event
		Event.Result allowGrowthResult = Event.Result.DEFAULT;
		if (Loader.isModLoaded("AppleCore"))
		{
			allowGrowthResult = validateAppleCoreGrowthTick(world, x, y, z, random);
		}

		// note: you could return early here if plant growth is the only thing
		// that this method does. This would allow for skipping the allowGrowthResult == Event.Result.DEFAULT
		// check in the template below, changing it to: allowGrowthResult == Result.ALLOW || (DEFAULT CONDITIONS)
		if (allowGrowthResult == Event.Result.DENY)
			return;

		// for each growth requirement/conditional in updateTick, the following  template should be applied:
		// allowGrowthResult == Result.ALLOW || (allowGrowthResult == Result.DEFAULT && (DEFAULT CONDITIONS))
		// this follows the PlantGrowthEvent.AllowGrowthTick specifications:
		//  - Event.Result.ALLOW means to always allow the growth tick without condition
		//  - Event.Result.DEFAULT means to continue with the conditionals as normal
		//  - Event.Result.DENY means to always disallow the growth tick
		if (allowGrowthResult == Event.Result.ALLOW || (allowGrowthResult == Event.Result.DEFAULT && world.getBlockLightValue(x, y + 1, z) >= 9))
		{
			int meta = world.getBlockMetadata(x, y, z);
			int previousMetadata = meta;

			if (meta < 7)
			{
				// same template as above is applied here, but the default conditional is
				// moved into a variable for clarity
				boolean defaultGrowthCondition = random.nextInt(50) == 0;
				if (allowGrowthResult == Result.ALLOW || (allowGrowthResult == Result.DEFAULT && defaultGrowthCondition))
				{
					++meta;
					world.setBlockMetadataWithNotify(x, y, z, meta, 2);

					// *After* the actual growth occurs, we simply let everyone know that it happened
					if (Loader.isModLoaded("AppleCore"))
					{
						announceAppleCoreGrowthTick(world, x, y, z, previousMetadata);
					}
				}
			}
		}
	}
}
