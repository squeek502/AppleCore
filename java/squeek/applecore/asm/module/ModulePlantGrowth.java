package squeek.applecore.asm.module;

import static org.objectweb.asm.Opcodes.*;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import squeek.applecore.asm.ASMHelper;
import squeek.applecore.asm.Hooks;
import squeek.applecore.asm.IClassTransformerModule;
import cpw.mods.fml.common.eventhandler.Event;

public class ModulePlantGrowth implements IClassTransformerModule
{
	@Override
	public String[] getClassesToTransform()
	{
		return new String[]{
		"net.minecraft.block.BlockCrops",
		"net.minecraft.block.BlockReed",
		"net.minecraft.block.BlockCactus",
		"net.minecraft.block.BlockCocoa",
		"net.minecraft.block.BlockMushroom",
		"net.minecraft.block.BlockNetherWart",
		"net.minecraft.block.BlockSapling",
		"net.minecraft.block.BlockStem"
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

		if (methodNode == null)
			throw new RuntimeException(classNode.name + ": updateTick method not found");

		if (transformedName.equals("net.minecraft.block.BlockCrops"))
			hookBlockCrops(classNode, methodNode, isObfuscated);
		else if (transformedName.equals("net.minecraft.block.BlockReed"))
			hookBlockReed(classNode, methodNode, isObfuscated);
		else if (transformedName.equals("net.minecraft.block.BlockCactus"))
			hookBlockCactus(classNode, methodNode, isObfuscated);
		else if (transformedName.equals("net.minecraft.block.BlockCocoa"))
			hookBlockCocoa(classNode, methodNode, isObfuscated);
		else if (transformedName.equals("net.minecraft.block.BlockMushroom"))
			hookBlockMushroom(classNode, methodNode, isObfuscated);
		else if (transformedName.equals("net.minecraft.block.BlockNetherWart"))
			hookBlockNetherWart(classNode, methodNode, isObfuscated);
		else if (transformedName.equals("net.minecraft.block.BlockSapling"))
			hookBlockSapling(classNode, methodNode, isObfuscated);
		else if (transformedName.equals("net.minecraft.block.BlockStem"))
			hookBlockStem(classNode, methodNode, isObfuscated);
		else
			return basicClass;

		return ASMHelper.writeClassToBytes(classNode);
	}

	private void hookBlockCrops(ClassNode classNode, MethodNode method, boolean isObfuscated)
	{
		JumpInsnNode ifJumpInsn = (JumpInsnNode) ASMHelper.findFirstInstructionOfType(method, IF_ICMPLT);
		AbstractInsnNode ifStartPoint = ASMHelper.findPreviousInstructionOfType(ifJumpInsn, ALOAD);

		LabelNode endLabel = ASMHelper.findEndLabel(method);
		LabelNode ifFailedLabel = ifJumpInsn.label;
		LabelNode ifAllowedLabel = new LabelNode();
		method.instructions.insert(ifJumpInsn, ifAllowedLabel);

		int resultIndex = fireAllowGrowthEventAndStoreResultBefore(method, ifStartPoint, endLabel);
		injectAllowedOrDefaultCheckBefore(method, ifStartPoint, resultIndex, ifAllowedLabel, ifFailedLabel);

		ifStartPoint = ASMHelper.findNextInstructionOfType(ifJumpInsn, LDC).getPrevious();
		ifJumpInsn = (JumpInsnNode) ASMHelper.findNextInstructionOfType(ifStartPoint, IFNE);

		ifFailedLabel = ifJumpInsn.label;
		ifAllowedLabel = new LabelNode();
		method.instructions.insert(ifJumpInsn, ifAllowedLabel);

		injectAllowedOrDefaultCheckBefore(method, ifStartPoint, resultIndex, ifAllowedLabel, ifFailedLabel);

		injectOnGrowthEventBefore(method, ifFailedLabel);
	}

	private void hookBlockReed(ClassNode classNode, MethodNode method, boolean isObfuscated)
	{
		JumpInsnNode ifJumpInsn = (JumpInsnNode) ASMHelper.findFirstInstructionOfType(method, IF_ICMPGE);
		LabelNode ifDeniedLabel = ifJumpInsn.label;

		injectNotDeniedCheckBefore(method, ifJumpInsn.getNext(), ifDeniedLabel);

		// need to change the GOTO so that it doesn't skip our added fireEvent call
		LabelNode newGotoLabel = new LabelNode();
		JumpInsnNode gotoInsn = (JumpInsnNode) ASMHelper.findNextInstructionOfType(ifJumpInsn, GOTO);
		gotoInsn.label = newGotoLabel;

		method.instructions.insertBefore(ifDeniedLabel, newGotoLabel);
		injectOnGrowthEventBefore(method, ifDeniedLabel);
	}

	// TODO: same as hookBlockReed, should they use a shared method?
	private void hookBlockCactus(ClassNode classNode, MethodNode method, boolean isObfuscated)
	{
		JumpInsnNode ifJumpInsn = (JumpInsnNode) ASMHelper.findFirstInstructionOfType(method, IF_ICMPGE);
		LabelNode ifDeniedLabel = ifJumpInsn.label;

		injectNotDeniedCheckBefore(method, ifJumpInsn.getNext(), ifDeniedLabel);

		// need to change the GOTO so that it doesn't skip our added fireEvent call
		LabelNode newGotoLabel = new LabelNode();
		JumpInsnNode gotoInsn = (JumpInsnNode) ASMHelper.findNextInstructionOfType(ifJumpInsn, GOTO);
		gotoInsn.label = newGotoLabel;

		method.instructions.insertBefore(ifDeniedLabel, newGotoLabel);
		injectOnGrowthEventBefore(method, ifDeniedLabel);
	}

	private void hookBlockCocoa(ClassNode classNode, MethodNode method, boolean isObfuscated)
	{
		int resultIndex = fireAllowGrowthEventAndStoreResultBefore(method, ASMHelper.findFirstInstruction(method), ASMHelper.findEndLabel(method));

		// get second IFNE
		JumpInsnNode ifJumpInsn = (JumpInsnNode) ASMHelper.findFirstInstructionOfType(method, IFNE);
		ifJumpInsn = (JumpInsnNode) ASMHelper.findNextInstructionOfType(ifJumpInsn, IFNE);
		AbstractInsnNode ifStartPoint = ASMHelper.findPreviousInstructionOfType(ifJumpInsn, ALOAD);

		LabelNode ifFailedLabel = ifJumpInsn.label;
		LabelNode ifAllowedLabel = new LabelNode();
		method.instructions.insert(ifJumpInsn, ifAllowedLabel);

		injectAllowedOrDefaultCheckBefore(method, ifStartPoint, resultIndex, ifAllowedLabel, ifFailedLabel);
		injectOnGrowthEventBefore(method, ifFailedLabel);
	}

	private void hookBlockMushroom(ClassNode classNode, MethodNode method, boolean isObfuscated)
	{
		int resultIndex = fireAllowGrowthEventAndStoreResultBefore(method, ASMHelper.findFirstInstruction(method), ASMHelper.findEndLabel(method));

		JumpInsnNode ifJumpInsn = (JumpInsnNode) ASMHelper.findFirstInstructionOfType(method, IFNE);
		AbstractInsnNode ifStartPoint = ASMHelper.findPreviousInstructionOfType(ifJumpInsn, ALOAD);

		LabelNode ifFailedLabel = ifJumpInsn.label;
		LabelNode ifAllowedLabel = new LabelNode();
		method.instructions.insert(ifJumpInsn, ifAllowedLabel);

		injectAllowedOrDefaultCheckBefore(method, ifStartPoint, resultIndex, ifAllowedLabel, ifFailedLabel);
	}

	private void hookBlockNetherWart(ClassNode classNode, MethodNode method, boolean isObfuscated)
	{
		int resultIndex = fireAllowGrowthEventAndStoreResultBefore(method, ASMHelper.findFirstInstruction(method), ASMHelper.findEndLabel(method));

		JumpInsnNode ifJumpInsn = (JumpInsnNode) ASMHelper.findFirstInstructionOfType(method, IFNE);
		AbstractInsnNode ifStartPoint = ASMHelper.findPreviousInstructionOfType(ifJumpInsn, ALOAD);

		LabelNode ifFailedLabel = ifJumpInsn.label;
		LabelNode ifAllowedLabel = new LabelNode();
		method.instructions.insert(ifJumpInsn, ifAllowedLabel);

		injectAllowedOrDefaultCheckBefore(method, ifStartPoint, resultIndex, ifAllowedLabel, ifFailedLabel);
		injectOnGrowthEventBefore(method, ifFailedLabel);
	}

	private void hookBlockSapling(ClassNode classNode, MethodNode method, boolean isObfuscated)
	{
		JumpInsnNode lightValueIf = (JumpInsnNode) ASMHelper.findFirstInstructionOfType(method, IF_ICMPLT);
		JumpInsnNode randomIf = (JumpInsnNode) ASMHelper.findNextInstructionOfType(lightValueIf, IFNE);
		AbstractInsnNode ifStartPoint = ASMHelper.findPreviousInstructionOfType(lightValueIf, ALOAD);

		LabelNode ifFailedLabel = lightValueIf.label;
		LabelNode ifAllowedLabel = new LabelNode();
		method.instructions.insert(randomIf, ifAllowedLabel);

		int resultIndex = fireAllowGrowthEventAndStoreResultBefore(method, ifStartPoint, ifFailedLabel);
		injectAllowedOrDefaultCheckBefore(method, ifStartPoint, resultIndex, ifAllowedLabel, ifFailedLabel);
	}

	private void hookBlockStem(ClassNode classNode, MethodNode method, boolean isObfuscated)
	{
		JumpInsnNode lightValueIf = (JumpInsnNode) ASMHelper.findFirstInstructionOfType(method, IF_ICMPLT);
		AbstractInsnNode ifStartPoint = ASMHelper.findPreviousInstructionOfType(lightValueIf, ALOAD);

		LabelNode ifFailedLabel = lightValueIf.label;
		LabelNode ifAllowedLabel = new LabelNode();
		method.instructions.insert(lightValueIf, ifAllowedLabel);

		int resultIndex = fireAllowGrowthEventAndStoreResultBefore(method, ifStartPoint, ASMHelper.findEndLabel(method));
		injectAllowedOrDefaultCheckBefore(method, ifStartPoint, resultIndex, ifAllowedLabel, ifFailedLabel);

		JumpInsnNode randomIf = (JumpInsnNode) ASMHelper.findNextInstructionOfType(lightValueIf, IFNE);
		ifStartPoint = ASMHelper.findPreviousInstructionOfType(randomIf, ALOAD);

		ifFailedLabel = randomIf.label;
		ifAllowedLabel = new LabelNode();
		method.instructions.insert(randomIf, ifAllowedLabel);

		injectAllowedOrDefaultCheckBefore(method, ifStartPoint, resultIndex, ifAllowedLabel, ifFailedLabel);
	}

	private int fireAllowGrowthEventAndStoreResultBefore(MethodNode method, AbstractInsnNode injectPoint, LabelNode endLabel)
	{
		// create allowGrowthResult variable
		LabelNode allowGrowthResultStart = new LabelNode();
		LocalVariableNode allowGrowthResult = new LocalVariableNode("allowGrowthResult", Type.getDescriptor(Event.Result.class), null, allowGrowthResultStart, endLabel, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(allowGrowthResult);

		InsnList toInject = new InsnList();

		// Result allowGrowthResult = Hooks.fireAllowPlantGrowthEvent(this, p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_, p_149674_5_);
		addFireGrowthEventInsnsToList(toInject);
		toInject.add(new VarInsnNode(ASTORE, allowGrowthResult.index));
		toInject.add(allowGrowthResultStart);

		method.instructions.insertBefore(injectPoint, toInject);

		return allowGrowthResult.index;
	}

	private void addFireGrowthEventInsnsToList(InsnList insnList)
	{
		insnList.add(new VarInsnNode(ALOAD, 0));
		insnList.add(new VarInsnNode(ALOAD, 1));
		insnList.add(new VarInsnNode(ILOAD, 2));
		insnList.add(new VarInsnNode(ILOAD, 3));
		insnList.add(new VarInsnNode(ILOAD, 4));
		insnList.add(new VarInsnNode(ALOAD, 5));
		insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "fireAllowPlantGrowthEvent", "(Lnet/minecraft/block/Block;Lnet/minecraft/world/World;IIILjava/util/Random;)Lcpw/mods/fml/common/eventhandler/Event$Result;"));
	}

	private void injectAllowedOrDefaultCheckBefore(MethodNode method, AbstractInsnNode injectPoint, int resultIndex, LabelNode ifAllowedLabel, LabelNode ifFailedLabel)
	{
		InsnList toInject = new InsnList();

		toInject.add(new VarInsnNode(ALOAD, resultIndex));
		toInject.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Event.Result.class), "ALLOW", Type.getDescriptor(Event.Result.class)));
		toInject.add(new JumpInsnNode(IF_ACMPEQ, ifAllowedLabel));
		toInject.add(new VarInsnNode(ALOAD, resultIndex));
		toInject.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Event.Result.class), "DEFAULT", Type.getDescriptor(Event.Result.class)));
		toInject.add(new JumpInsnNode(IF_ACMPNE, ifAllowedLabel));

		method.instructions.insertBefore(injectPoint, toInject);
	}

	private void injectNotDeniedCheckBefore(MethodNode method, AbstractInsnNode injectPoint, LabelNode ifDeniedLabel)
	{
		InsnList toInject = new InsnList();

		addFireGrowthEventInsnsToList(toInject);
		toInject.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Event.Result.class), "DENY", Type.getDescriptor(Event.Result.class)));
		toInject.add(new JumpInsnNode(IF_ACMPEQ, ifDeniedLabel));

		method.instructions.insertBefore(injectPoint, toInject);
	}

	private void injectOnGrowthEventBefore(MethodNode method, AbstractInsnNode injectPoint)
	{
		InsnList toInject = new InsnList();

		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new VarInsnNode(ILOAD, 2));
		toInject.add(new VarInsnNode(ILOAD, 3));
		toInject.add(new VarInsnNode(ILOAD, 4));
		toInject.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "fireOnGrowthEvent", "(Lnet/minecraft/block/Block;Lnet/minecraft/world/World;III)V"));

		method.instructions.insertBefore(injectPoint, toInject);
	}
}
