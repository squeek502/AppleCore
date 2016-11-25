package squeek.applecore.asm.reference;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.FoodStats;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import squeek.applecore.api.hunger.HealthRegenEvent;
import squeek.applecore.api.hunger.HungerRegenEvent;
import squeek.applecore.asm.Hooks;

public abstract class EntityPlayerModifications extends EntityPlayer
{
	// modified initialization of foodStats field to use the added constructor
	protected FoodStats foodStats = new FoodStatsModifications(this);

	// added field
	public int itemInUseMaxDuration;

	// a single line added
	@Override
	public void setActiveHand(EnumHand hand)
	{
		ItemStack stack = this.getHeldItem(hand);

		if (!stack.isEmpty() && !this.isHandActive())
		{
			int duration = ForgeEventFactory.onItemUseStart(this, stack, stack.getMaxItemUseDuration());
			if (duration <= 0) return;
			this.activeItemStack = stack;
			this.activeItemStackUseCount = duration;

			// added:
			this.itemInUseMaxDuration = duration;

			if (!this.world.isRemote)
			{
				int i = 1;

				if (hand == EnumHand.OFF_HAND)
				{
					i |= 2;
				}

				this.dataManager.set(HAND_STATES, (byte) i);
			}
		}
	}

	// changed this.activeItemStack.getMaxItemUseDuration() to Hooks.getItemInUseMaxDuration()
	@Override
	public int getItemInUseMaxCount()
	{
		return this.isHandActive() ? Hooks.getItemInUseMaxCount(this, itemInUseMaxDuration) - this.itemInUseCount : 0;
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
		if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL && this.world.getGameRules().getBoolean("naturalRegeneration"))
		{
			if (this.getHealth() < this.getMaxHealth() && this.ticksExisted % 20 == 0)
			{
				// added event and if statement
				HealthRegenEvent.PeacefulRegen peacefulRegenEvent = Hooks.firePeacefulRegenEvent(this);
				if (!peacefulRegenEvent.isCanceled())
				{
					// modified from this.heal(1.0F);
					this.heal(peacefulRegenEvent.deltaHealth);
				}
			}

			if (this.foodStats.needFood() && this.ticksExisted % 10 == 0)
			{
				HungerRegenEvent.PeacefulRegen peacefulHungerRegenEvent = Hooks.firePeacefulHungerRegenEvent(this);
				if (!peacefulHungerRegenEvent.isCanceled())
				{
					this.foodStats.setFoodLevel(this.foodStats.getFoodLevel() + peacefulHungerRegenEvent.deltaHunger);
				}
			}

			// ...
		}
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