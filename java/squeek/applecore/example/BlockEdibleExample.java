package squeek.applecore.example;

// TODO
//@Optional.Interface(iface = "squeek.applecore.api.food.IEdibleBlock", modid = "applecore")
public class BlockEdibleExample //extends BlockCake implements IEdibleBlock
{
	//	private boolean isEdibleAtMaxHunger = false;
	//
	//	@Optional.Method(modid = "applecore")
	//	@Override
	//	public void setEdibleAtMaxHunger(boolean value)
	//	{
	//		isEdibleAtMaxHunger = value;
	//	}
	//
	//	@Optional.Method(modid = "applecore")
	//	@Override
	//	public FoodValues getFoodValues(@Nonnull ItemStack itemStack)
	//	{
	//		return new FoodValues(2, 0.1f);
	//	}
	//
	//	// This needs to be abstracted into an Optional method,
	//	// otherwise the ItemFoodProxy reference will cause problems
	//	@Optional.Method(modid = "applecore")
	//	public void onEatenCompatibility(ItemStack itemStack, PlayerEntity player)
	//	{
	//		// one possible compatible method
	//		player.getFoodStats().addStats(new ItemFoodProxy(this), itemStack);
	//
	//		// another possible compatible method:
	//		// new ItemFoodProxy(this).onEaten(itemStack, player);
	//	}
	//
	//	@Override
	//	public boolean onBlockActivated(@Nullable World world, @Nullable BlockPos pos, @Nullable IBlockState state, PlayerEntity player, @Nullable EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	//	{
	//		if (!world.isRemote)
	//		{
	//			return this.eat(world, pos, state, player);
	//		}
	//		else
	//		{
	//			ItemStack itemstack = player.getHeldItem(hand);
	//			return this.eat(world, pos, state, player) || itemstack.isEmpty();
	//		}
	//	}
	//
	//	private boolean eat(World world, BlockPos pos, IBlockState state, PlayerEntity player)
	//	{
	//		if (!player.canEat(isEdibleAtMaxHunger))
	//		{
	//			return false;
	//		}
	//		else
	//		{
	//			if (Loader.isModLoaded("applecore"))
	//			{
	//				onEatenCompatibility(new ItemStack(this), player);
	//			}
	//			else
	//			{
	//				// this method is not compatible with AppleCore
	//				player.getFoodStats().addStats(2, 0.1f);
	//			}
	//
	//			int bites = state.getValue(BITES);
	//			if (bites < 6)
	//			{
	//				world.setBlockState(pos, state.withProperty(BITES, bites + 1), 3);
	//			}
	//			else
	//			{
	//				world.setBlockToAir(pos);
	//			}
	//
	//			return true;
	//		}
	//	}
}
