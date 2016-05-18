package squeek.applecore.asm.module;

import org.objectweb.asm.tree.*;
import squeek.applecore.asm.ASMConstants;
import squeek.applecore.asm.IClassTransformerModule;
import squeek.asmhelper.applecore.ASMHelper;

import static org.objectweb.asm.Opcodes.*;

public class ModulePlantGrowth implements IClassTransformerModule
{
	@Override
	public String[] getClassesToTransform()
	{
		return new String[]{
		ASMConstants.CROPS,
		ASMConstants.REED,
		ASMConstants.CACTUS,
		ASMConstants.COCOA,
		ASMConstants.MUSHROOM,
		ASMConstants.NETHER_WART,
		ASMConstants.SAPLING,
		ASMConstants.STEM
		};
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		ClassNode classNode = ASMHelper.readClassFromBytes(basicClass);

		MethodNode methodNode = ASMHelper.findMethodNodeOfClass(classNode, "func_180650_b", "updateTick", ASMHelper.toMethodDescriptor("V", ASMConstants.WORLD, ASMConstants.BLOCK_POS, ASMConstants.IBLOCKSTATE, ASMConstants.RANDOM));

		if (methodNode == null)
			methodNode = ASMHelper.findMethodNodeOfClass(classNode, "func_180650_b", "updateTick", ASMHelper.toMethodDescriptor("V", ASMConstants.WORLD, ASMConstants.BLOCK_POS, ASMConstants.IBLOCKSTATE, ASMConstants.RANDOM));

		if (methodNode == null)
			throw new RuntimeException(classNode.name + ": updateTick method not found");

		if (transformedName.equals(ASMConstants.CROPS))
			hookBlockCrops(methodNode);
		else if (transformedName.equals(ASMConstants.REED))
			hookBlockReed(methodNode);
		else if (transformedName.equals(ASMConstants.CACTUS))
			hookBlockCactus(methodNode);
		else if (transformedName.equals(ASMConstants.COCOA))
			hookBlockCocoa(methodNode);
		else if (transformedName.equals(ASMConstants.MUSHROOM))
			hookBlockMushroom(methodNode);
		else if (transformedName.equals(ASMConstants.NETHER_WART))
			hookBlockNetherWart(methodNode);
		else if (transformedName.equals(ASMConstants.SAPLING))
			hookBlockSapling(methodNode);
		else if (transformedName.equals(ASMConstants.STEM))
			hookBlockStem(methodNode);
		else
			throw new RuntimeException("Unexpected class passed to transformer : " + transformedName);

		return ASMHelper.writeClassToBytes(classNode);
	}

	private void hookBlockCrops(MethodNode method)
	{
		JumpInsnNode ifJumpInsn = (JumpInsnNode) ASMHelper.findFirstInstructionWithOpcode(method, IF_ICMPLT);
		AbstractInsnNode ifStartPoint = ASMHelper.findPreviousLabelOrLineNumber(ifJumpInsn).getNext();

		LabelNode endLabel = ASMHelper.findEndLabel(method);
		LabelNode ifFailedLabel = ifJumpInsn.label;
		LabelNode ifAllowedLabel = new LabelNode();
		method.instructions.insert(ifJumpInsn, ifAllowedLabel);

		int resultIndex = fireAllowGrowthEventAndStoreResultBefore(method, ifStartPoint, endLabel);
		injectAllowedOrDefaultCheckBefore(method, ifStartPoint, resultIndex, ifAllowedLabel, ifFailedLabel);

		ifStartPoint = ASMHelper.findPreviousLabelOrLineNumber((ASMHelper.findNextInstructionWithOpcode(ifJumpInsn, LDC))).getNext();
		ifJumpInsn = (JumpInsnNode) ASMHelper.findNextInstructionWithOpcode(ifStartPoint, IFNE);

		ifFailedLabel = ifJumpInsn.label;
		ifAllowedLabel = new LabelNode();
		method.instructions.insert(ifJumpInsn, ifAllowedLabel);

		injectAllowedOrDefaultCheckBefore(method, ifStartPoint, resultIndex, ifAllowedLabel, ifFailedLabel);

		injectOnGrowthEventBefore(method, ifFailedLabel);
	}

	private void hookBlockReed(MethodNode method)
	{
		JumpInsnNode ifJumpInsn = (JumpInsnNode) ASMHelper.findFirstInstructionWithOpcode(method, IF_ICMPGE);
		LabelNode ifDeniedLabel = ifJumpInsn.label;

		injectNotDeniedCheckBefore(method, ifJumpInsn.getNext(), ifDeniedLabel);

		// need to change the GOTO so that it doesn't skip our added fireEvent call
		LabelNode newGotoLabel = new LabelNode();
		JumpInsnNode gotoInsn = (JumpInsnNode) ASMHelper.findNextInstructionWithOpcode(ifJumpInsn, GOTO);
		gotoInsn.label = newGotoLabel;

		method.instructions.insertBefore(ifDeniedLabel, newGotoLabel);
		injectOnGrowthEventBefore(method, ifDeniedLabel);
	}

	// TODO: same as hookBlockReed, should they use a shared method?
	private void hookBlockCactus(MethodNode method)
	{
		JumpInsnNode ifJumpInsn = (JumpInsnNode) ASMHelper.findFirstInstructionWithOpcode(method, IF_ICMPGE);
		LabelNode ifDeniedLabel = ifJumpInsn.label;

		injectNotDeniedCheckBefore(method, ifJumpInsn.getNext(), ifDeniedLabel);

		// need to change the GOTO so that it doesn't skip our added fireEvent call
		LabelNode newGotoLabel = new LabelNode();
		JumpInsnNode gotoInsn = (JumpInsnNode) ASMHelper.findNextInstructionWithOpcode(ifJumpInsn, GOTO);
		gotoInsn.label = newGotoLabel;

		method.instructions.insertBefore(ifDeniedLabel, newGotoLabel);
		injectOnGrowthEventBefore(method, ifDeniedLabel);
	}

	private void hookBlockCocoa(MethodNode method)
	{
		int resultIndex = fireAllowGrowthEventAndStoreResultBefore(method, ASMHelper.findFirstInstruction(method), ASMHelper.findEndLabel(method));

		// get second IFNE
		JumpInsnNode ifJumpInsn = (JumpInsnNode) ASMHelper.findFirstInstructionWithOpcode(method, IFNE);
		ifJumpInsn = (JumpInsnNode) ASMHelper.findNextInstructionWithOpcode(ifJumpInsn, IFNE);
		AbstractInsnNode ifStartPoint = ASMHelper.findPreviousLabelOrLineNumber(ifJumpInsn).getNext();

		LabelNode ifFailedLabel = ifJumpInsn.label;
		LabelNode ifAllowedLabel = new LabelNode();
		method.instructions.insert(ifJumpInsn, ifAllowedLabel);

		injectAllowedOrDefaultCheckBefore(method, ifStartPoint, resultIndex, ifAllowedLabel, ifFailedLabel);

		injectOnGrowthEventBefore(method, ifFailedLabel);
	}

	private void hookBlockMushroom(MethodNode method)
	{
		int resultIndex = fireAllowGrowthEventAndStoreResultBefore(method, ASMHelper.findFirstInstruction(method), ASMHelper.findEndLabel(method));

		JumpInsnNode ifJumpInsn = (JumpInsnNode) ASMHelper.findFirstInstructionWithOpcode(method, IFNE);
		AbstractInsnNode ifStartPoint = ASMHelper.findPreviousLabelOrLineNumber(ifJumpInsn).getNext();

		LabelNode ifFailedLabel = ifJumpInsn.label;
		LabelNode ifAllowedLabel = new LabelNode();
		method.instructions.insert(ifJumpInsn, ifAllowedLabel);

		injectAllowedOrDefaultCheckBefore(method, ifStartPoint, resultIndex, ifAllowedLabel, ifFailedLabel);

		fixPrecedingIfsToNotSkipInjectedInstructions(method, ifFailedLabel);
		injectOnGrowthEventBefore(method, ifFailedLabel);
	}

	private void hookBlockNetherWart(MethodNode method)
	{
		int previousStateIndex = storeBlockStateInNewVariable(method);
		int resultIndex = fireAllowGrowthEventAndStoreResultBefore(method, ASMHelper.findFirstInstruction(method), ASMHelper.findEndLabel(method));

		JumpInsnNode ifJumpInsn = (JumpInsnNode) ASMHelper.findFirstInstructionWithOpcode(method, IFNE);
		AbstractInsnNode ifStartPoint = ASMHelper.findPreviousInstructionWithOpcode(ifJumpInsn, IF_ICMPGE).getNext();

		LabelNode ifFailedLabel = ifJumpInsn.label;
		LabelNode ifAllowedLabel = new LabelNode();
		method.instructions.insert(ifJumpInsn, ifAllowedLabel);

		injectAllowedOrDefaultCheckBefore(method, ifStartPoint, resultIndex, ifAllowedLabel, ifFailedLabel);

		injectOnGrowthEventBefore(method, ifFailedLabel, previousStateIndex);
	}

	private void hookBlockSapling(MethodNode method)
	{
		JumpInsnNode lightValueIf = (JumpInsnNode) ASMHelper.findFirstInstructionWithOpcode(method, IF_ICMPLT);
		JumpInsnNode randomIf = (JumpInsnNode) ASMHelper.findNextInstructionWithOpcode(lightValueIf, IFNE);
		AbstractInsnNode ifStartPoint = ASMHelper.findPreviousLabelOrLineNumber(lightValueIf).getNext();

		LabelNode ifFailedLabel = lightValueIf.label;
		LabelNode ifAllowedLabel = new LabelNode();
		method.instructions.insert(randomIf, ifAllowedLabel);

		int resultIndex = fireAllowGrowthEventAndStoreResultBefore(method, ifStartPoint, ifFailedLabel);
		injectAllowedOrDefaultCheckBefore(method, ifStartPoint, resultIndex, ifAllowedLabel, ifFailedLabel);

		injectOnGrowthEventBefore(method, ifFailedLabel);
	}

	private void hookBlockStem(MethodNode method)
	{
		int previousStateIndex = storeBlockStateInNewVariable(method);

		JumpInsnNode lightValueIf = (JumpInsnNode) ASMHelper.findFirstInstructionWithOpcode(method, IF_ICMPLT);
		AbstractInsnNode ifStartPoint = ASMHelper.getOrFindInstructionOfType(lightValueIf, AbstractInsnNode.LINE, true).getNext();

		LabelNode ifFailedLabel = lightValueIf.label;
		LabelNode ifAllowedLabel = new LabelNode();
		method.instructions.insert(lightValueIf, ifAllowedLabel);

		int resultIndex = fireAllowGrowthEventAndStoreResultBefore(method, ifStartPoint, ASMHelper.findEndLabel(method));
		injectAllowedOrDefaultCheckBefore(method, ifStartPoint, resultIndex, ifAllowedLabel, ifFailedLabel);

		JumpInsnNode randomIf = (JumpInsnNode) ASMHelper.findNextInstructionWithOpcode(lightValueIf, IFNE);
		ifStartPoint = ASMHelper.getOrFindInstructionOfType(randomIf, AbstractInsnNode.LINE, true).getNext();

		ifFailedLabel = randomIf.label;
		ifAllowedLabel = new LabelNode();
		method.instructions.insert(randomIf, ifAllowedLabel);

		injectAllowedOrDefaultCheckBefore(method, ifStartPoint, resultIndex, ifAllowedLabel, ifFailedLabel);

		// need to change the GOTO so that it doesn't skip our added fireEvent call
		LabelNode newGotoLabel = new LabelNode();
		JumpInsnNode gotoInsn = (JumpInsnNode) ASMHelper.findNextInstructionWithOpcode(randomIf, GOTO);
		gotoInsn.label = newGotoLabel;

		method.instructions.insertBefore(ifFailedLabel, newGotoLabel);
		fixPrecedingIfsToNotSkipInjectedInstructions(method, ifFailedLabel);
		injectOnGrowthEventBefore(method, ifFailedLabel, previousStateIndex);
	}

	private int storeBlockStateInNewVariable(MethodNode method)
	{
		LabelNode previousStateStart = new LabelNode();
		LabelNode previousStateEnd = ASMHelper.findEndLabel(method);
		LocalVariableNode previousState = new LocalVariableNode("previousState", ASMHelper.toDescriptor(ASMConstants.IBLOCKSTATE), null, previousStateStart, previousStateEnd, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(previousState);

		InsnList toInject = new InsnList();
		toInject.add(new VarInsnNode(ALOAD, 3));
		toInject.add(new VarInsnNode(ASTORE, previousState.index));
		toInject.add(previousStateStart);
		method.instructions.insert(ASMHelper.findFirstInstruction(method), toInject);

		return previousState.index;
	}

	private int fireAllowGrowthEventAndStoreResultBefore(MethodNode method, AbstractInsnNode injectPoint, LabelNode endLabel)
	{
		// create allowGrowthResult variable
		LabelNode allowGrowthResultStart = new LabelNode();
		LocalVariableNode allowGrowthResult = new LocalVariableNode("allowGrowthResult", ASMHelper.toDescriptor(ASMConstants.EVENT_RESULT), null, allowGrowthResultStart, endLabel, method.maxLocals);
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
		insnList.add(new VarInsnNode(ALOAD, 2));
		insnList.add(new VarInsnNode(ALOAD, 3));
		insnList.add(new VarInsnNode(ALOAD, 4));
		insnList.add(new MethodInsnNode(INVOKESTATIC, ASMHelper.toInternalClassName(ASMConstants.HOOKS), "fireAllowPlantGrowthEvent", ASMHelper.toMethodDescriptor(ASMConstants.EVENT_RESULT, ASMConstants.BLOCK, ASMConstants.WORLD, ASMConstants.BLOCK_POS, ASMConstants.IBLOCKSTATE, ASMConstants.RANDOM), false));
	}

	private void injectAllowedOrDefaultCheckBefore(MethodNode method, AbstractInsnNode injectPoint, int resultIndex, LabelNode ifAllowedLabel, LabelNode ifFailedLabel)
	{
		InsnList toInject = new InsnList();

		toInject.add(new VarInsnNode(ALOAD, resultIndex));
		toInject.add(new FieldInsnNode(GETSTATIC, ASMHelper.toInternalClassName(ASMConstants.EVENT_RESULT), "ALLOW", ASMHelper.toDescriptor(ASMConstants.EVENT_RESULT)));
		toInject.add(new JumpInsnNode(IF_ACMPEQ, ifAllowedLabel));
		toInject.add(new VarInsnNode(ALOAD, resultIndex));
		toInject.add(new FieldInsnNode(GETSTATIC, ASMHelper.toInternalClassName(ASMConstants.EVENT_RESULT), "DEFAULT", ASMHelper.toDescriptor(ASMConstants.EVENT_RESULT)));
		toInject.add(new JumpInsnNode(IF_ACMPNE, ifFailedLabel));

		method.instructions.insertBefore(injectPoint, toInject);
	}

	private void injectNotDeniedCheckBefore(MethodNode method, AbstractInsnNode injectPoint, LabelNode ifDeniedLabel)
	{
		InsnList toInject = new InsnList();

		addFireGrowthEventInsnsToList(toInject);
		toInject.add(new FieldInsnNode(GETSTATIC, ASMHelper.toInternalClassName(ASMConstants.EVENT_RESULT), "DENY", ASMHelper.toDescriptor(ASMConstants.EVENT_RESULT)));
		toInject.add(new JumpInsnNode(IF_ACMPEQ, ifDeniedLabel));

		method.instructions.insertBefore(injectPoint, toInject);
	}

	private void injectOnGrowthEventBefore(MethodNode method, AbstractInsnNode injectPoint, int previousStateIndex)
	{
		InsnList toInject = new InsnList();

		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new VarInsnNode(ALOAD, 2));
		toInject.add(new VarInsnNode(ALOAD, previousStateIndex));
		toInject.add(new MethodInsnNode(INVOKESTATIC, ASMHelper.toInternalClassName(ASMConstants.HOOKS), "fireOnGrowthEvent", ASMHelper.toMethodDescriptor("V", ASMConstants.BLOCK, ASMConstants.WORLD, ASMConstants.BLOCK_POS, ASMConstants.IBLOCKSTATE), false));

		method.instructions.insertBefore(injectPoint, toInject);
	}

	private void injectOnGrowthEventBefore(MethodNode method, AbstractInsnNode injectPoint)
	{
		injectOnGrowthEventBefore(method, injectPoint, 3);
	}

	private void fixPrecedingIfsToNotSkipInjectedInstructions(MethodNode method, LabelNode labelJumpedTo)
	{
		LabelNode beforeOnGrowthEvent = new LabelNode();
		method.instructions.insertBefore(labelJumpedTo, beforeOnGrowthEvent);

		AbstractInsnNode curInsn = labelJumpedTo.getPrevious();
		while (curInsn != null)
		{
			boolean isJump = curInsn instanceof JumpInsnNode;
			if (isJump && ((JumpInsnNode) curInsn).label == labelJumpedTo)
				((JumpInsnNode) curInsn).label = beforeOnGrowthEvent;
			else if (isJump)
				break;
			curInsn = curInsn.getPrevious();
		}
	}
}