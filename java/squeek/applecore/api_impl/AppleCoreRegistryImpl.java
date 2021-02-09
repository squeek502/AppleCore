package squeek.applecore.api_impl;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraftforge.registries.ObjectHolder;
import squeek.applecore.api.AppleCoreAPI;
import squeek.applecore.api.IAppleCoreRegistry;

import java.util.HashMap;
import java.util.Map;

public enum AppleCoreRegistryImpl implements IAppleCoreRegistry
{
	INSTANCE;

	private Map<Block, Item> edibleBlockToItem = new HashMap<>();
	private Map<Item, Block> edibleItemToBlock = new HashMap<>();

	private AppleCoreRegistryImpl()
	{
		AppleCoreAPI.registry = this;
	}

	@ObjectHolder("minecraft:cake")
	public static final Block CAKE_BLOCK = null;

	@ObjectHolder("minecraft:cake")
	public static final Item CAKE_ITEM = null;

	public void init()
	{
		registerEdibleBlock(CAKE_BLOCK, CAKE_ITEM);
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
		if (item == null) item = block.asItem();
		return item;
	}

	@Override
	public Block getEdibleBlockFromItem(Item item)
	{
		Block block = edibleItemToBlock.get(item);
		if (block == null) block = Block.getBlockFromItem(item);
		return block != Blocks.AIR ? block : null;
	}
}
