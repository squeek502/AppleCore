package squeek.applecore.api_impl;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import squeek.applecore.api.AppleCoreAPI;
import squeek.applecore.api.IAppleCoreRegistry;

import java.util.HashMap;
import java.util.Map;

public enum AppleCoreRegistryImpl implements IAppleCoreRegistry
{
	INSTANCE;

	private Map<Block, Item> edibleBlockToItem = new HashMap<Block, Item>();
	private Map<Item, Block> edibleItemToBlock = new HashMap<Item, Block>();

	private AppleCoreRegistryImpl()
	{
		AppleCoreAPI.registry = this;
		registerEdibleBlock(Blocks.cake, Items.cake);
	}

	@Override
	public void registerEdibleBlock(Block block, Item item)
	{
		edibleBlockToItem.put(block, item);
		edibleItemToBlock.put(item, block);
	}

	@Override
	public Item getItemFromEdibleBlock(Block block)
	{
		Item item = edibleBlockToItem.get(block);
		if (item == null) item = Item.getItemFromBlock(block);
		return item;
	}

	@Override
	public Block getEdibleBlockFromItem(Item item)
	{
		Block block = edibleItemToBlock.get(item);
		if (block == null) block = Block.getBlockFromItem(item);
		return block != Blocks.air ? block : null;
	}
}
