package squeek.applecore.asm.reference;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.FoodStats;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import com.mojang.authlib.GameProfile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class EntityPlayerModifications extends EntityPlayer
{
	// modified initialization of foodStats field to use the added constructor
	protected FoodStats foodStats = new FoodStatsModifications(this);

	// added field
	public int itemInUseMaxDuration;

	// a single line added
	@Override
	public void setItemInUse(ItemStack p_71008_1_, int p_71008_2_)
	{
		if (p_71008_1_ != this.itemInUse)
		{
			p_71008_2_ = ForgeEventFactory.onItemUseStart(this, p_71008_1_, p_71008_2_);
			if (p_71008_2_ <= 0)
				return;
			this.itemInUse = p_71008_1_;
			this.itemInUseCount = p_71008_2_;
			// added:
			this.itemInUseMaxDuration = p_71008_2_;
			
			// ...
		}
	}

	// changed this.itemInUse.getMaxItemUseDuration() to this.itemInUseMaxDuration
	@Override
	@SideOnly(Side.CLIENT)
	public int getItemInUseDuration()
	{
		return this.isUsingItem() ? this.itemInUseMaxDuration - this.itemInUseCount : 0;
	}

	/*
	 * everything below is unmodified
	 * it is only required to avoid compilation errors
	 */
	public ItemStack itemInUse;
	public int itemInUseCount;

	public EntityPlayerModifications(World p_i45324_1_, GameProfile p_i45324_2_)
	{
		super(p_i45324_1_, p_i45324_2_);
	}
}
