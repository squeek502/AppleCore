package squeek.applecore.api;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public interface IAppleCoreRegistry
{
	/**
	 * Registers a Block <-> Item relationship for
	 * block-based foods. This registration is only necessary
	 * when {@link Block#getBlockFromItem} returns something other
	 * than the 'canonical' block for the Item (or vice versa
	 * with regards to {@link Item#getItemFromBlock}.<br>
	 * <br>
	 * For example, Blocks.cake <-> Items.cake requires their
	 * relationship to be registered.
	 */
	void registerEdibleBlock(Block block, Item item);

	/**
	 * Note: Falls back to {@link Item#getItemFromBlock} when no
	 * registry data is found for the block
	 *
	 * @return The Item, or null if no item was found
	 */
	Item getItemFromEdibleBlock(Block block);

	/**
	 * Note: Falls back to {@link Block#getBlockFromItem} when no
	 * registry data is found for the item
	 *
	 * @return The Block, or null if no block was found
	 */
	Block getEdibleBlockFromItem(Item item);
}
