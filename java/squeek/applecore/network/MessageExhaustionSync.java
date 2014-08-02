package squeek.applecore.network;

import io.netty.buffer.ByteBuf;
import squeek.applecore.accessor.AppleCoreAccessorImpl;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

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
	public IMessage onMessage(MessageExhaustionSync message, MessageContext ctx)
	{
		AppleCoreAccessorImpl.setExhaustion(NetworkHelper.getSidedPlayer(ctx), message.exhaustionLevel);
		return null;
	}
}