package squeek.applecore.asm.module;

import static org.objectweb.asm.Opcodes.*;
import org.objectweb.asm.tree.*;
import squeek.applecore.asm.ASMConstants;
import squeek.applecore.asm.IClassTransformerModule;
import squeek.asmhelper.applecore.ASMHelper;

public class ModulePeacefulRegen implements IClassTransformerModule
{
	@Override
	public String[] getClassesToTransform()
	{
		return new String[]{"net.minecraft.entity.player.EntityPlayer"};
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		if (transformedName.equals("net.minecraft.entity.player.EntityPlayer"))
		{
			ClassNode classNode = ASMHelper.readClassFromBytes(basicClass);

			MethodNode methodNode = ASMHelper.findMethodNodeOfClass(classNode, "func_70636_d", "onLivingUpdate", "()V");
			if (methodNode != null)
			{
				addPeacefulRegenHook(classNode, methodNode);
				return ASMHelper.writeClassToBytes(classNode);
			}
			else
				throw new RuntimeException("EntityPlayer: onLivingUpdate method not found");
		}
		return basicClass;
	}

	public void addPeacefulRegenHook(ClassNode classNode, MethodNode method)
	{
		AbstractInsnNode relevantConditional = ASMHelper.find(method.instructions, new LdcInsnNode("naturalRegeneration"));
		JumpInsnNode ifNode = (JumpInsnNode) ASMHelper.find(relevantConditional, new JumpInsnNode(IFEQ, new LabelNode()));
		LabelNode ifBlockEndLabel = ifNode.label;
		AbstractInsnNode targetNode = ASMHelper.find(ifNode, new InsnNode(FCONST_1)).getPrevious();

		int peacefulRegenEventIndex = firePeacefulRegenEventAndStoreEventBefore(method, targetNode, ifBlockEndLabel);

		InsnList healAmountNeedle = new InsnList();
		healAmountNeedle.add(new InsnNode(FCONST_1));

		InsnList healAmountReplacement = new InsnList();
		healAmountReplacement.add(new VarInsnNode(ALOAD, peacefulRegenEventIndex));
		healAmountReplacement.add(new FieldInsnNode(GETFIELD, ASMHelper.toInternalClassName(ASMConstants.HealthRegenEvent.PeacefulRegen), "deltaHealth", "F"));

		ASMHelper.findAndReplace(method.instructions, healAmountNeedle, healAmountReplacement, targetNode);

		InsnList ifNotCanceledBlock = new InsnList();
		LabelNode ifNotCanceled = new LabelNode();

		ifNotCanceledBlock.add(new VarInsnNode(ALOAD, peacefulRegenEventIndex));
		ifNotCanceledBlock.add(new MethodInsnNode(INVOKEVIRTUAL, ASMHelper.toInternalClassName(ASMConstants.HealthRegenEvent.PeacefulRegen), "isCanceled", "()Z", false));
		ifNotCanceledBlock.add(new JumpInsnNode(IFNE, ifNotCanceled));
		method.instructions.insertBefore(targetNode, ifNotCanceledBlock);

		method.instructions.insertBefore(ifBlockEndLabel, ifNotCanceled);
	}

	private int firePeacefulRegenEventAndStoreEventBefore(MethodNode method, AbstractInsnNode injectPoint, LabelNode endLabel)
	{
		// create  variable
		LabelNode peacefulRegenEventStart = new LabelNode();
		LocalVariableNode peacefulRegenEvent = new LocalVariableNode("peacefulRegenEvent", ASMHelper.toDescriptor(ASMConstants.HealthRegenEvent.PeacefulRegen), null, peacefulRegenEventStart, endLabel, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(peacefulRegenEvent);

		InsnList toInject = new InsnList();

		// HealthRegenEvent.PeacefulRegen peacefulRegenEvent = Hooks.firePeacefulRegenEvent(this);
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new MethodInsnNode(INVOKESTATIC, ASMHelper.toInternalClassName(ASMConstants.Hooks), "firePeacefulRegenEvent", "(Lnet/minecraft/entity/player/EntityPlayer;)Lsqueek/applecore/api/hunger/HealthRegenEvent$PeacefulRegen;", false));
		toInject.add(new VarInsnNode(ASTORE, peacefulRegenEvent.index));
		toInject.add(peacefulRegenEventStart);

		method.instructions.insertBefore(injectPoint, toInject);

		return peacefulRegenEvent.index;
	}
}
