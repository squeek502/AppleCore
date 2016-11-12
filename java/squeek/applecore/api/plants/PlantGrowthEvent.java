package squeek.applecore.api.plants;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Random;

/**
 * Deprecated in favor of {@link net.minecraftforge.event.world.BlockEvent.CropGrowEvent}
 */
@Deprecated
public class PlantGrowthEvent extends Event
{
	/**
	 * Deprecated in favor of {@link net.minecraftforge.event.world.BlockEvent.CropGrowEvent.Pre}
	 * <p/>
	 * Fired each plant update tick to determine whether or not growth is allowed for the {@link #block}.
	 * 
	 * This event is fired in various {@link Block#updateTick} overrides.<br>
	 * <br>
	 * This event is not {@link Cancelable}.<br>
	 * <br>
	 * This event uses the {@link Result}. {@link HasResult}<br>
	 * {@link Result#DEFAULT} will use the vanilla conditionals.
	 * {@link Result#ALLOW} will allow the growth tick without condition.
	 * {@link Result#DENY} will deny the growth tick without condition.
	 */
	@Deprecated
	@HasResult
	public static class AllowGrowthTick extends PlantGrowthEvent
	{
		public final Block block;
		public final World world;
		public final BlockPos pos;
		public final IBlockState state;
		public final Random random;

		public AllowGrowthTick(Block block, World world, BlockPos pos, IBlockState state, Random random)
		{
			this.block = block;
			this.world = world;
			this.pos = pos;
			this.state = state;
			this.random = random;
		}
	}

	/**
	 * Deprecated in favor of {@link net.minecraftforge.event.world.BlockEvent.CropGrowEvent.Post}
	 * <p/>
	 * Fired after a plant grows from a growth tick.<br>
	 * <br>
	 * Note: {@code currentState.getBlock()} can differ from {@code block}
	 * (e.g. when sapling grows into a tree, {@code currentState.getBlock()} will
	 * return BlockLog while {@code block} will store the sapling that grew.<br>
	 * <br>
	 * This event is fired in various {@link Block#updateTick} overrides.<br>
	 * <br>
	 * This event is not {@link Cancelable}.<br>
	 * <br>
	 * This event does not have a result. {@link HasResult}<br>
	 */
	@Deprecated
	public static class GrowthTick extends PlantGrowthEvent
	{
		public final Block block;
		public final World world;
		public final BlockPos pos;
		public final IBlockState currentState;
		public final IBlockState previousState;

		public GrowthTick(Block block, World world, BlockPos pos, IBlockState currentState, IBlockState previousState)
		{
			this.block = block;
			this.world = world;
			this.pos = pos;
			this.currentState = currentState;
			this.previousState = previousState;
		}

		public GrowthTick(Block block, World world, BlockPos pos, IBlockState previousState)
		{
			this(block, world, pos, world.getBlockState(pos), previousState);
		}
	}
}