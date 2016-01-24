package squeek.applecore.asm.module;

import org.objectweb.asm.tree.*;
import squeek.applecore.asm.ASMConstants;
import squeek.applecore.asm.IClassTransformerModule;
import squeek.asmhelper.applecore.ASMHelper;
import squeek.asmhelper.applecore.InsnComparator;
import squeek.asmhelper.applecore.ObfHelper;

import static org.objectweb.asm.Opcodes.*;

public class ModuleFoodEatingSpeed implements IClassTransformerModule
{
	@Override
	public String[] getClassesToTransform()
	{
		return new String[]{
		ASMConstants.Player,
		ASMConstants.ItemRenderer
		};
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		ClassNode classNode = ASMHelper.readClassFromBytes(basicClass);

		if (transformedName.equals(ASMConstants.Player))
		{
			addItemInUseMaxDurationField(classNode);

			MethodNode methodNode = ASMHelper.findMethodNodeOfClass(classNode, "func_71008_a", "setItemInUse", ASMHelper.toMethodDescriptor("V", ASMConstants.Stack, "I"));
			if (methodNode != null)
			{
				patchSetItemInUse(classNode, methodNode);
			}
			else
				throw new RuntimeException(classNode.name + ": setItemInUse method not found");

			// this is client-only, so don't throw an error if it's not found
			methodNode = ASMHelper.findMethodNodeOfClass(classNode, "func_71057_bx", "getItemInUseDuration", ASMHelper.toMethodDescriptor("I"));
			if (methodNode != null)
			{
				patchGetItemInUseDuration(classNode, methodNode);
			}
		}
		else if (transformedName.equals(ASMConstants.ItemRenderer))
		{
			MethodNode methodNode = ASMHelper.findMethodNodeOfClass(classNode, "func_178104_a", ASMHelper.toMethodDescriptor("V", ASMConstants.AbstractClientPlayer, "F"));
			if (methodNode != null)
			{
				patchRenderItemInFirstPerson(classNode, methodNode);
			}
			else
				throw new RuntimeException(classNode.name + ": setItemInUse method not found");
		}

		return ASMHelper.writeClassToBytes(classNode);
	}

	private void patchRenderItemInFirstPerson(ClassNode classNode, MethodNode method)
	{
		InsnList needle = new InsnList();
		needle.add(new VarInsnNode(ALOAD, 0));
		needle.add(new FieldInsnNode(GETFIELD, ASMHelper.toInternalClassName(ASMConstants.ItemRenderer), InsnComparator.WILDCARD, ASMHelper.toDescriptor(ASMConstants.Stack)));
		needle.add(new MethodInsnNode(INVOKEVIRTUAL, ASMHelper.toInternalClassName(ASMConstants.Stack), ObfHelper.isObfuscated() ? "func_77988_m" : "getMaxItemUseDuration", ASMHelper.toMethodDescriptor("I"), false));

		int playerParamIndex = 1;
		String playerInternalName = ASMHelper.toInternalClassName(ASMConstants.Player);

		InsnList replacement = new InsnList();
		replacement.add(new VarInsnNode(ALOAD, playerParamIndex));
		replacement.add(new FieldInsnNode(GETFIELD, playerInternalName, "itemInUseMaxDuration", "I"));

		boolean replaced = ASMHelper.findAndReplace(method.instructions, needle, replacement) != null;
		if (!replaced)
			throw new RuntimeException("ItemRenderer.func_178104_a: no replacements made");
	}

	private void patchGetItemInUseDuration(ClassNode classNode, MethodNode method)
	{
		InsnList needle = new InsnList();
		needle.add(new VarInsnNode(ALOAD, 0));
		needle.add(new FieldInsnNode(GETFIELD, ObfHelper.getInternalClassName(ASMConstants.Player), ObfHelper.isObfuscated() ? "field_71074_e" : "itemInUse", ASMHelper.toDescriptor(ASMConstants.Stack)));
		needle.add(new MethodInsnNode(INVOKEVIRTUAL, ObfHelper.getInternalClassName(ASMConstants.Stack), ObfHelper.isObfuscated() ? "func_77988_m" : "getMaxItemUseDuration", ASMHelper.toMethodDescriptor("I"), false));

		InsnList replacement = new InsnList();
		replacement.add(new VarInsnNode(ALOAD, 0));
		replacement.add(new VarInsnNode(ALOAD, 0));
		replacement.add(new FieldInsnNode(GETFIELD, ASMHelper.toInternalClassName(classNode.name), "itemInUseMaxDuration", "I"));
		replacement.add(new MethodInsnNode(INVOKESTATIC, ASMHelper.toInternalClassName(ASMConstants.Hooks), "getItemInUseMaxDuration", ASMHelper.toMethodDescriptor("I", ASMConstants.Player, "I"), false));

		int numReplacementsMade = ASMHelper.findAndReplaceAll(method.instructions, needle, replacement);
		if (numReplacementsMade == 0)
			throw new RuntimeException("EntityPlayer.getItemInUseDuration: no replacements made");
	}

	private void patchSetItemInUse(ClassNode classNode, MethodNode method)
	{
		AbstractInsnNode targetNode = ASMHelper.findFirstInstructionWithOpcode(method, PUTFIELD);
		while (targetNode != null && !((FieldInsnNode) targetNode).name.equals(ObfHelper.isObfuscated() ? "field_71072_f" : "itemInUseCount"))
		{
			targetNode = ASMHelper.findNextInstructionWithOpcode(targetNode, PUTFIELD);
		}

		if (targetNode == null)
			throw new RuntimeException("EntityPlayer.setItemInUse: PUTFIELD itemInUseCount instruction not found");

		InsnList toInject = new InsnList();

		// this.itemInUseMaxDuration = p_71008_2_;
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new VarInsnNode(ILOAD, 2));
		toInject.add(new FieldInsnNode(PUTFIELD, ASMHelper.toInternalClassName(classNode.name), "itemInUseMaxDuration", "I"));

		method.instructions.insert(targetNode, toInject);
	}

	private void addItemInUseMaxDurationField(ClassNode classNode)
	{
		classNode.fields.add(new FieldNode(ACC_PUBLIC, "itemInUseMaxDuration", "I", null, null));
	}
}