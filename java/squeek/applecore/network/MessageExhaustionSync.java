package squeek.applecore.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import squeek.applecore.api.AppleCoreAPI;

public class MessageExhaustionSync implements IMessage, IMessageHandler<MessageExhaustionSync, IMessage>
{
	float exhaustionLevel;

	public MessageExhaustionSync()
	{
	}

	public MessageExhaustionSync(float exhaustionLevel)
	{
		this.exhaustionLevel = exhaustionLevel;
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeFloat(exhaustionLevel);
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		exhaustionLevel = buf.readFloat();
	}

	@Override
	public IMessage onMessage(final MessageExhaustionSync message, final MessageContext ctx)
	{
		Minecraft.getMinecraft().addScheduledTask(new Runnable() {
			@Override
			public void run() {
				AppleCoreAPI.mutator.setExhaustion(NetworkHelper.getSidedPlayer(ctx), message.exhaustionLevel);
			}
		});
		return null;
	}
}