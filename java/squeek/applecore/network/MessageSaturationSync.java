package squeek.applecore.network;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class MessageSaturationSync implements IMessage, IMessageHandler<MessageSaturationSync, IMessage>
{
	float saturationLevel;

	public MessageSaturationSync()
	{
	}

	public MessageSaturationSync(float saturationLevel)
	{
		this.saturationLevel = saturationLevel;
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeFloat(saturationLevel);
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		saturationLevel = buf.readFloat();
	}

	@Override
	public IMessage onMessage(MessageSaturationSync message, MessageContext ctx)
	{
		NetworkHelper.getSidedPlayer(ctx).getFoodStats().setFoodSaturationLevel(message.saturationLevel);
		return null;
	}
}
