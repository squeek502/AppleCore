package squeek.applecore.asm.module;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.*;
import squeek.applecore.asm.ASMConstants;
import squeek.applecore.asm.IClassTransformerModule;
import squeek.asmhelper.applecore.ASMHelper;
import squeek.asmhelper.applecore.InsnComparator;
import squeek.asmhelper.applecore.ObfHelper;

import static org.objectweb.asm.Opcodes.*;

public class ModuleBlockFood implements IClassTransformerModule
{
	private static String isEdibleAtMaxHungerField = "AppleCore_isEdibleAtMaxHunger";

	@Override
	public String[] getClassesToTransform()
	{
		return new String[]{ASMConstants.CAKE};
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		ClassNode classNode = ASMHelper.readClassFromBytes(basicClass);

		implementIEdibleBlock(classNode);

		MethodNode methodNode = ASMHelper.findMethodNodeOfClass(classNode, "func_180682_b", "eatCake", ASMHelper.toMethodDescriptor("V", ASMConstants.WORLD, ASMConstants.BLOCK_POS, ASMConstants.IBLOCKSTATE, ASMConstants.PLAYER));
		if (methodNode != null)
		{
			addOnBlockFoodEatenHook(classNode, methodNode);
			addAlwaysEdibleCheck(classNode, methodNode);
			return ASMHelper.writeClassToBytes(classNode);
		}
		else
			throw new RuntimeException("BlockCake: eatCake (func_180682_b) method not found");
	}

	private void implementIEdibleBlock(ClassNode classNode)
	{
		classNode.interfaces.add(ASMHelper.toInternalClassName(ASMConstants.IEDIBLE));
		classNode.interfaces.add(ASMHelper.toInternalClassName(ASMConstants.IEDIBLEBLOCK));

		MethodVisitor mv = classNode.visitMethod(ACC_PUBLIC, "getFoodValues", "(Lnet/minecraft/item/ItemStack;)Lsqueek/applecore/api/food/FoodValues;", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitTypeInsn(NEW, "squeek/applecore/api/food/FoodValues");
		mv.visitInsn(DUP);
		mv.visitInsn(ICONST_2);
		mv.visitLdcInsn(new Float("0.1"));
		mv.visitMethodInsn(INVOKESPECIAL, "squeek/applecore/api/food/FoodValues", "<init>", "(IF)V", false);
		mv.visitInsn(ARETURN);
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitLocalVariable("this", ASMHelper.toDescriptor(classNode.name), null, l0, l1, 0);
		mv.visitLocalVariable("itemStack", "Lnet/minecraft/item/ItemStack;", null, l0, l1, 1);
		mv.visitMaxs(4, 2);
		mv.visitEnd();

		classNode.fields.add(new FieldNode(ACC_PUBLIC, isEdibleAtMaxHungerField, "Z", null, null));

		mv = classNode.visitMethod(ACC_PUBLIC, "setEdibleAtMaxHunger", ASMHelper.toMethodDescriptor("V", "Z"), null, null);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ILOAD, 1);
		mv.visitFieldInsn(PUTFIELD, ASMHelper.toInternalClassName(classNode.name), isEdibleAtMaxHungerField, "Z");
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
	}

	private void addOnBlockFoodEatenHook(ClassNode classNode, MethodNode method)
	{
		// default player.getFoodStats().addStats call replaced with:
		/*
		Hooks.onBlockFoodEaten(this, world, player);
		*/
		InsnList needle = new InsnList();
		needle.add(new VarInsnNode(ALOAD, 4));
		needle.add(new MethodInsnNode(INVOKEVIRTUAL, ObfHelper.getInternalClassName("net.minecraft.entity.player.EntityPlayer"), InsnComparator.WILDCARD, InsnComparator.WILDCARD, false));
		needle.add(new InsnNode(ICONST_2));
		needle.add(new LdcInsnNode(0.1f));
		needle.add(new MethodInsnNode(INVOKEVIRTUAL, ObfHelper.getInternalClassName("net.minecraft.util.FoodStats"), InsnComparator.WILDCARD, "(IF)V", false));

		InsnList replacement = new InsnList();
		replacement.add(new VarInsnNode(ALOAD, 0));
		replacement.add(new VarInsnNode(ALOAD, 1));
		replacement.add(new VarInsnNode(ALOAD, 4));
		replacement.add(new MethodInsnNode(INVOKESTATIC, ASMConstants.HOOKS_INTERNAL_CLASS, "onBlockFoodEaten", "(Lnet/minecraft/block/Block;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;)V", false));

		if (ASMHelper.findAndReplace(method.instructions, needle, replacement) == null)
			throw new RuntimeException("Could not replace FoodStats.addStats call in " + classNode.name + "." + method.name + "\n" + ASMHelper.getMethodAsString(method));
	}

	private void addAlwaysEdibleCheck(ClassNode classNode, MethodNode method)
	{
		AbstractInsnNode pushFalse = ASMHelper.findFirstInstructionWithOpcode(method, ICONST_0);

		if (pushFalse == null)
			throw new RuntimeException("ICONST_0 instruction not found in " + classNode.name + "." + method.name);

		InsnList pushField = new InsnList();
		pushField.add(new VarInsnNode(ALOAD, 0));
		pushField.add(new FieldInsnNode(GETFIELD, ASMHelper.toInternalClassName(classNode.name), isEdibleAtMaxHungerField, "Z"));
		method.instructions.insert(pushFalse, pushField);
		method.instructions.remove(pushFalse);
	}
}