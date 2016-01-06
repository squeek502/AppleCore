package squeek.applecore.asm.reference;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.FoodStats;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import squeek.applecore.api.hunger.HealthRegenEvent;
import squeek.applecore.asm.Hooks;

public abstract class EntityPlayerModifications extends EntityPlayer
{
	// modified initialization of foodStats field to use the added constructor
	protected FoodStats foodStats = new FoodStatsModifications(this);

	// added field
	public int itemInUseMaxDuration;

	// a single line added
	@Override
	public void setItemInUse(ItemStack stack, int duration)
	{
		if (stack != this.itemInUse)
		{
			duration = ForgeEventFactory.onItemUseStart(this, stack, duration);
			if (duration <= 0)
				return;
			this.itemInUse = stack;
			this.itemInUseCount = duration;
			// added:
			this.itemInUseMaxDuration = duration;

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
		if (this.worldObj.getDifficulty() == EnumDifficulty.PEACEFUL && this.getHealth() < this.getMaxHealth() && this.worldObj.getGameRules().getBoolean("naturalRegeneration") && this.ticksExisted % 20 * 12 == 0)
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

	public EntityPlayerModifications(World world, GameProfile gameProfile)
	{
		super(world, gameProfile);
	}
}