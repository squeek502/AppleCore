package squeek.applecore.api.plants;

import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Random;

public class FertilizationEvent extends Event
{
	/**
	 * Fired when a block is about to be fertilized.
	 * 
	 * This event is fired in all {@link IGrowable#grow} implementations.<br>
	 * <br>
	 * This event is not {@link Cancelable}.<br>
	 * <br>
	 * This event uses the {@link Result}. {@link HasResult}<br>
	 * {@link Result#DEFAULT} will use the default fertilization implementation.
	 * {@link Result#ALLOW} will mark the fertilization as handled and skip the default implementation.
	 * {@link Result#DENY} will cancel the fertilization.
	 */
	@HasResult
	public static class Fertilize extends FertilizationEvent
	{
		public final Block block;
		public final World world;
		public final BlockPos pos;
		public final IBlockState state;
		public final Random random;

		public Fertilize(Block block, World world, BlockPos pos, IBlockState state, Random random)
		{
			this.block = block;
			this.world = world;
			this.pos = pos;
			this.state = state;
			this.random = random;
		}
	}

	/**
	 * Fired after a block is fertilized.
	 * 
	 * This event is fired in all {@link IGrowable#grow} implementations.<br>
	 * <br>
	 * This event is not {@link Cancelable}.<br>
	 * <br>
	 * This event does not have a result. {@link HasResult}<br>
	 */
	public static class Fertilized extends FertilizationEvent
	{
		public final Block block;
		public final World world;
		public final BlockPos pos;
		public final IBlockState currentState;
		public final IBlockState previousState;

		public Fertilized(Block block, World world, BlockPos pos, IBlockState currentState, IBlockState previousState)
		{
			this.block = block;
			this.world = world;
			this.pos = pos;
			this.currentState = currentState;
			this.previousState = previousState;
		}

		public Fertilized(Block block, World world, BlockPos pos, IBlockState previousState)
		{
			this(block, world, pos, world.getBlockState(pos), previousState);
		}
	}
}