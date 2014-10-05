package squeek.applecore.asm.reference;

import squeek.applecore.api.hunger.HealthRegenEvent;
import squeek.applecore.asm.Hooks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.FoodStats;
import net.minecraft.world.EnumDifficulty;
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

	// changed this.itemInUse.getMaxItemUseDuration() to Hooks.getItemInUseMaxDuration()
	@Override
	@SideOnly(Side.CLIENT)
	public int getItemInUseDuration()
	{
		return this.isUsingItem() ? Hooks.getItemInUseMaxDuration(this, itemInUseMaxDuration) - this.itemInUseCount : 0;
	}

	// add hook for peaceful health regen
	@Override
	public void onLivingUpdate()
	{
		if (this.flyToggleTimer > 0)
		{
			--this.flyToggleTimer;
		}

		// modified
		if (this.worldObj.difficultySetting == EnumDifficulty.PEACEFUL && this.getHealth() < this.getMaxHealth() && this.worldObj.getGameRules().getGameRuleBooleanValue("naturalRegeneration") && this.ticksExisted % 20 * 12 == 0)
		{
			// added event and if statement
			HealthRegenEvent.PeacefulRegen peacefulRegenEvent = Hooks.firePeacefulRegenEvent(this);
			if (!peacefulRegenEvent.isCanceled())
			{
				// modified from this.heal(1.0F);
				this.heal(peacefulRegenEvent.deltaHealth);
			}
		}

		// ...
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
