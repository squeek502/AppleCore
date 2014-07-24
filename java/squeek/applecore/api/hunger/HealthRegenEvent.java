package squeek.applecore.api.hunger;

import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;

public abstract class HealthRegenEvent extends Event
{
	public EntityPlayer player;

	public HealthRegenEvent(EntityPlayer player)
	{
		this.player = player;
	}

	public static class AllowRegen extends HealthRegenEvent
	{
		public AllowRegen(EntityPlayer player)
		{
			super(player);
		}
	}

	@Cancelable
	public static class Tick extends HealthRegenEvent
	{
		public int regenTickPeriod = 80;

		public Tick(EntityPlayer player)
		{
			super(player);
		}
	}

	@Cancelable
	public static class Regen extends HealthRegenEvent
	{
		public float deltaHealth = 1f;
		public float deltaExhaustion = -3f;

		public Regen(EntityPlayer player)
		{
			super(player);
		}
	}
}