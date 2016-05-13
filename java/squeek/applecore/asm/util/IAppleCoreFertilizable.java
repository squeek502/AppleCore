package squeek.applecore.asm.util;

import net.minecraft.world.World;

import java.util.Random;

/**
 * Dummy interface to allow calling the copied fertilize method without using reflection.
 * This avoidance of reflection in turn avoids @SideOnly issues on dedicated servers
 */
public interface IAppleCoreFertilizable
{
	void AppleCore_fertilize(World world, Random random, int x, int y, int z);

	// HarvestCraft specific
	void AppleCore_fertilize(World world, int x, int y, int z);
	void AppleCore_fertilize(World world, int x, int y, int z, Random random);
}
