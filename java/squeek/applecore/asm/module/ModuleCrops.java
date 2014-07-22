package squeek.applecore.asm.module;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import squeek.applecore.Hooks;
import squeek.applecore.asm.ASMHelper;
import squeek.applecore.asm.IClassTransformerModule;

public class ModuleCrops implements IClassTransformerModule
{
	@Override
	public String[] getClassesToTransform()
	{
		return new String[]{
		"net.minecraft.block.BlockCrops",
		"net.minecraft.block.BlockReed",
		"mods.natura.blocks.crops.NetherBerryBush",
		"mods.natura.blocks.trees.SaguaroBlock",
		"mods.natura.blocks.crops.CropBlock",
		"mods.natura.blocks.crops.BerryBush"
		};
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		boolean isObfuscated = !name.equals(transformedName);

		ClassNode classNode = ASMHelper.readClassFromBytes(basicClass);

		MethodNode methodNode = ASMHelper.findMethodNodeOfClass(classNode, isObfuscated ? "a" : "updateTick", isObfuscated ? "(Lahb;IIILjava/util/Random;)V" : "(Lnet/minecraft/world/World;IIILjava/util/Random;)V");

		if (methodNode == null)
			methodNode = ASMHelper.findMethodNodeOfClass(classNode, "func_149674_a", isObfuscated ? "(Lahb;IIILjava/util/Random;)V" : "(Lnet/minecraft/world/World;IIILjava/util/Random;)V");

		if (methodNode != null)
		{
			addUpdateTickHook(methodNode, isObfuscated);
		}
		else
			throw new RuntimeException(classNode.name + ": updateTick method not found");

		return ASMHelper.writeClassToBytes(classNode);
	}

	private void addUpdateTickHook(MethodNode method, boolean isObfuscated)
	{
		// injected code
		/*
		if (!Hooks.shouldUpdateTick(this, p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_, p_149674_5_))
		    return;
		*/

		AbstractInsnNode targetNode = ASMHelper.findFirstInstruction(method);

		LabelNode ifShouldUpdate = new LabelNode();

		InsnList toInject = new InsnList();
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new VarInsnNode(ILOAD, 2));
		toInject.add(new VarInsnNode(ILOAD, 3));
		toInject.add(new VarInsnNode(ILOAD, 4));
		toInject.add(new VarInsnNode(ALOAD, 5));
		toInject.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "shouldUpdateTick", isObfuscated ? "(Laji;Lahb;IIILjava/util/Random;)Z" : "(Lnet/minecraft/block/Block;Lnet/minecraft/world/World;IIILjava/util/Random;)Z"));
		toInject.add(new JumpInsnNode(IFNE, ifShouldUpdate));
		toInject.add(new InsnNode(RETURN));
		toInject.add(ifShouldUpdate);

		method.instructions.insertBefore(targetNode, toInject);
	}
}
