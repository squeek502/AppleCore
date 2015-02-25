package squeek.applecore.api;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import cpw.mods.fml.common.eventhandler.Event;

public interface IAppleCoreDispatcher
{
	/**
	 * Fires a {@link squeek.applecore.api.plants.PlantGrowthEvent.AllowGrowthTick} event and returns the result.
	 * 
	 * See {@link squeek.applecore.api.plants.PlantGrowthEvent.AllowGrowthTick} for how to use the result.
	 */
	public Event.Result validatePlantGrowth(Block block, World world, int x, int y, int z, Random random);

	/**
	 * Fires a {@link squeek.applecore.api.plants.PlantGrowthEvent.GrowthTick} event.
	 */
	public void announcePlantGrowth(Block block, World world, int x, int y, int z, int previousMetadata);

	/**
	 * Fires a {@link squeek.applecore.api.plants.PlantGrowthEvent.GrowthTick} event.
	 * Use only when the growth did not cause a metadata change.
	 */
	public void announcePlantGrowthWithoutMetadataChange(Block block, World world, int x, int y, int z);

	/**
	 * Deprecated in favor of {@link #announcePlantGrowth(Block, World, int, int, int, int)}
	 * and {@link #announcePlantGrowthWithoutMetadataChange(Block, World, int, int, int)}
	 */
	@Deprecated
	public void announcePlantGrowth(Block block, World world, int x, int y, int z);
}
