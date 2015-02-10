package squeek.applecore.asm.module;

import static org.objectweb.asm.Opcodes.*;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import squeek.applecore.asm.Hooks;
import squeek.applecore.asm.IClassTransformerModule;
import squeek.applecore.asm.TransformerModuleHandler;
import squeek.asmhelper.applecore.ASMHelper;
import squeek.asmhelper.applecore.ObfHelper;
import cpw.mods.fml.common.eventhandler.Event;

public class ModulePlantFertilization implements IClassTransformerModule
{
	private static final boolean isObfuscated = ObfHelper.isObfuscated();

	@Override
	public String[] getClassesToTransform()
	{
		return TransformerModuleHandler.ALL_CLASSES;
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes)
	{
		ClassNode classNode = ASMHelper.readClassFromBytes(bytes);
		if (classNode.interfaces.contains(ObfHelper.getInternalClassName("net.minecraft.block.IGrowable")))
		{
			boolean didTransform = false;

			MethodNode method = ASMHelper.findMethodNodeOfClass(classNode, isObfuscated ? "b" : "func_149853_b", ObfHelper.desc("(Lnet/minecraft/world/World;Ljava/util/Random;III)V"));
			if (method != null)
			{
				wrapFertilizeMethod(method);
				didTransform = true;
			}

			method = ASMHelper.findMethodNodeOfClass(classNode, isObfuscated ? "a" : "func_149852_a", ObfHelper.desc("(Lnet/minecraft/world/World;Ljava/util/Random;III)Z"));
			if (method != null)
			{
				hookCanFertilizeMethod(method);
				didTransform = true;
			}

			if (didTransform)
				return ASMHelper.writeClassToBytes(classNode);
		}
		return bytes;
	}

	private void wrapFertilizeMethod(MethodNode method)
	{
		LabelNode endLabel = ASMHelper.findEndLabel(method);

		LabelNode previousMetadataStart = new LabelNode();
		LocalVariableNode previousMetadata = new LocalVariableNode("previousMetadata", "I", null, previousMetadataStart, endLabel, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(previousMetadata);

		LabelNode fertilizeResultStart = new LabelNode();
		LocalVariableNode fertilizeResult = new LocalVariableNode("fertilizeResult", Type.getDescriptor(Event.Result.class), null, fertilizeResultStart, endLabel, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(fertilizeResult);

		InsnList toInjectAtStart = new InsnList();
		// get previous meta
		toInjectAtStart.add(new VarInsnNode(ALOAD, 1));
		toInjectAtStart.add(new VarInsnNode(ILOAD, 3));
		toInjectAtStart.add(new VarInsnNode(ILOAD, 4));
		toInjectAtStart.add(new VarInsnNode(ILOAD, 5));
		toInjectAtStart.add(new MethodInsnNode(INVOKEVIRTUAL, ObfHelper.getInternalClassName("net.minecraft.world.World"), isObfuscated ? "e" : "getBlockMetadata", "(III)I", false));
		toInjectAtStart.add(new VarInsnNode(ISTORE, previousMetadata.index));
		toInjectAtStart.add(previousMetadataStart);
		// fire event and get result
		toInjectAtStart.add(new VarInsnNode(ALOAD, 0));
		toInjectAtStart.add(new VarInsnNode(ALOAD, 1));
		toInjectAtStart.add(new VarInsnNode(ILOAD, 3));
		toInjectAtStart.add(new VarInsnNode(ILOAD, 4));
		toInjectAtStart.add(new VarInsnNode(ILOAD, 5));
		toInjectAtStart.add(new VarInsnNode(ALOAD, 2));
		toInjectAtStart.add(new VarInsnNode(ILOAD, previousMetadata.index));
		toInjectAtStart.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "fireFertilizeEvent", "(Lnet/minecraft/block/IGrowable;Lnet/minecraft/world/World;IIILjava/util/Random;I)Lcpw/mods/fml/common/eventhandler/Event$Result;", false));
		toInjectAtStart.add(new VarInsnNode(ASTORE, fertilizeResult.index));
		toInjectAtStart.add(fertilizeResultStart);
		// check if its denied
		toInjectAtStart.add(new VarInsnNode(ALOAD, fertilizeResult.index));
		toInjectAtStart.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Event.Result.class), "DENY", Type.getDescriptor(Event.Result.class)));
		LabelNode ifNotDenied = new LabelNode();
		toInjectAtStart.add(new JumpInsnNode(IF_ACMPNE, ifNotDenied));
		toInjectAtStart.add(new LabelNode());
		toInjectAtStart.add(new InsnNode(RETURN));
		toInjectAtStart.add(ifNotDenied);
		// check if its default
		toInjectAtStart.add(new VarInsnNode(ALOAD, fertilizeResult.index));
		toInjectAtStart.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Event.Result.class), "DEFAULT", Type.getDescriptor(Event.Result.class)));
		LabelNode ifNotDefault = new LabelNode();
		toInjectAtStart.add(new JumpInsnNode(IF_ACMPNE, ifNotDefault));
		toInjectAtStart.add(new LabelNode());
		method.instructions.insertBefore(ASMHelper.findFirstInstruction(method), toInjectAtStart);

		InsnList toInjectAtEnd = new InsnList();
		toInjectAtEnd.add(ifNotDefault);
		toInjectAtEnd.add(new VarInsnNode(ALOAD, 0));
		toInjectAtEnd.add(new VarInsnNode(ALOAD, 1));
		toInjectAtEnd.add(new VarInsnNode(ILOAD, 3));
		toInjectAtEnd.add(new VarInsnNode(ILOAD, 4));
		toInjectAtEnd.add(new VarInsnNode(ILOAD, 5));
		toInjectAtEnd.add(new VarInsnNode(ILOAD, previousMetadata.index));
		toInjectAtEnd.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "fireFertilizedEvent", "(Lnet/minecraft/block/IGrowable;Lnet/minecraft/world/World;IIII)V", false));
		method.instructions.insertBefore(ASMHelper.findPreviousInstruction(endLabel), toInjectAtEnd);
	}

	private void hookCanFertilizeMethod(MethodNode method)
	{
		LabelNode endLabel = ASMHelper.findEndLabel(method);

		LabelNode canFertilizeResultStart = new LabelNode();
		LocalVariableNode canFertilizeResult = new LocalVariableNode("canFertilizeResult", Type.getDescriptor(Event.Result.class), null, canFertilizeResultStart, endLabel, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(canFertilizeResult);

		InsnList toInject = new InsnList();
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new VarInsnNode(ILOAD, 3));
		toInject.add(new VarInsnNode(ILOAD, 4));
		toInject.add(new VarInsnNode(ILOAD, 5));
		toInject.add(new VarInsnNode(ALOAD, 2));
		toInject.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "fireCanFertilizeEvent", "(Lnet/minecraft/block/IGrowable;Lnet/minecraft/world/World;IIILjava/util/Random;)Lcpw/mods/fml/common/eventhandler/Event$Result;", false));
		toInject.add(new VarInsnNode(ASTORE, canFertilizeResult.index));
		toInject.add(canFertilizeResultStart);
		toInject.add(new VarInsnNode(ALOAD, canFertilizeResult.index));
		toInject.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Event.Result.class), "DENY", Type.getDescriptor(Event.Result.class)));
		LabelNode ifNotDenied = new LabelNode();
		toInject.add(new JumpInsnNode(IF_ACMPNE, ifNotDenied));
		toInject.add(new LabelNode());
		toInject.add(new InsnNode(ICONST_0));
		toInject.add(new InsnNode(IRETURN));
		toInject.add(ifNotDenied);
		toInject.add(new VarInsnNode(ALOAD, canFertilizeResult.index));
		toInject.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Event.Result.class), "ALLOW", Type.getDescriptor(Event.Result.class)));
		LabelNode ifNotAllowed = new LabelNode();
		toInject.add(new JumpInsnNode(IF_ACMPNE, ifNotAllowed));
		toInject.add(new LabelNode());
		toInject.add(new InsnNode(ICONST_1));
		toInject.add(new InsnNode(IRETURN));
		toInject.add(ifNotAllowed);
		method.instructions.insertBefore(ASMHelper.findFirstInstruction(method), toInject);
	}
}
