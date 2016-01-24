package squeek.applecore.api;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Random;

public interface IAppleCoreDispatcher {
	/**
	 * Fires a {@link squeek.applecore.api.plants.PlantGrowthEvent.AllowGrowthTick} event and returns the result.
	 * <p/>
	 * See {@link squeek.applecore.api.plants.PlantGrowthEvent.AllowGrowthTick} for how to use the result.
	 */
	public Event.Result validatePlantGrowth(Block block, World world, BlockPos pos, IBlockState state, Random random);

	/**
	 * Fires a {@link squeek.applecore.api.plants.PlantGrowthEvent.GrowthTick} event.
	 */
	public void announcePlantGrowth(Block block, World world, BlockPos pos, IBlockState state, int previousMetadata);

	/**
	 * Fires a {@link squeek.applecore.api.plants.PlantGrowthEvent.GrowthTick} event.
	 * Use only when the growth did not cause a metadata change.
	 */
	public void announcePlantGrowthWithoutMetadataChange(Block block, World world, BlockPos pos, IBlockState state);
}