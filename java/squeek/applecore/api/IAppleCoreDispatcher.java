package squeek.applecore.api;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Random;

public interface IAppleCoreDispatcher {
	/**
	 * Deprecated in favor of {@link net.minecraftforge.event.world.BlockEvent.CropGrowEvent.Pre}
	 * <p/>
	 * Fires a {@link squeek.applecore.api.plants.PlantGrowthEvent.AllowGrowthTick} event and returns the result.
	 * <p/>
	 * See {@link squeek.applecore.api.plants.PlantGrowthEvent.AllowGrowthTick} for how to use the result.
	 */
	@Deprecated
	public Event.Result validatePlantGrowth(Block block, World world, BlockPos pos, IBlockState state, Random random);

	/**
	 * Deprecated in favor of {@link net.minecraftforge.event.world.BlockEvent.CropGrowEvent.Post}
	 * <p/>
	 * Fires a {@link squeek.applecore.api.plants.PlantGrowthEvent.GrowthTick} event.
	 */
	@Deprecated
	public void announcePlantGrowth(Block block, World world, BlockPos pos, IBlockState currentState, IBlockState previousState);

	/**
	 * Deprecated in favor of {@link net.minecraftforge.event.world.BlockEvent.CropGrowEvent.Post}
	 * <p/>
	 * Fires a {@link squeek.applecore.api.plants.PlantGrowthEvent.GrowthTick} event.
	 * Use to automatically retrieve the current state.
	 */
	@Deprecated
	public void announcePlantGrowth(Block block, World world, BlockPos pos, IBlockState previousState);
}