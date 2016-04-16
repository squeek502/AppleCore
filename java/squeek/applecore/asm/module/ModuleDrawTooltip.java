package squeek.applecore.asm.module;

import org.objectweb.asm.tree.*;
import squeek.applecore.AppleCore;
import squeek.applecore.asm.ASMConstants;
import squeek.applecore.asm.IClassTransformerModule;
import squeek.asmhelper.applecore.ASMHelper;

import static org.objectweb.asm.Opcodes.*;

public class ModuleDrawTooltip implements IClassTransformerModule
{
	@Override
	public String[] getClassesToTransform()
	{
		return new String[]{
		ASMConstants.GuiUtils,
		"codechicken.lib.gui.GuiDraw"
		};
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes)
	{
		ClassNode classNode = ASMHelper.readClassFromBytes(bytes);

		if (transformedName.equals(ASMConstants.GuiUtils))
		{
			MethodNode methodNode = ASMHelper.findMethodNodeOfClass(classNode, "drawHoveringText", ASMHelper.toMethodDescriptor("V", ASMConstants.List, "I", "I", "I", "I", "I", ASMConstants.FontRenderer));

			if (methodNode != null)
			{
				addDrawHoveringTextHook(methodNode, "onDrawHoveringText", ASMHelper.toMethodDescriptor("V", "I", "I", "I", "I"));
			}
			else
				throw new RuntimeException("GuiUtils.drawHoveringText not found");
		}
		else if (name.equals("codechicken.lib.gui.GuiDraw"))
		{
			MethodNode methodNode = ASMHelper.findMethodNodeOfClass(classNode, "drawTooltipBox", ASMHelper.toMethodDescriptor("V", "I", "I", "I", "I"));

			if (methodNode != null)
			{
				addCodeChickenDrawHoveringTextHook(methodNode, "onDrawHoveringText", ASMHelper.toMethodDescriptor("V", "I", "I", "I", "I"));
			}
			else
				AppleCore.Log.error("drawTooltipBox method in codechicken.lib.gui.GuiDraw not found");
		}

		return ASMHelper.writeClassToBytes(classNode);
	}

	private void addDrawHoveringTextHook(MethodNode method, String hookMethod, String hookDesc)
	{
		AbstractInsnNode targetNode = null;

		// get last drawGradientRect call
		for (AbstractInsnNode instruction : method.instructions.toArray())
		{
			if (instruction.getOpcode() == INVOKESTATIC)
			{
				MethodInsnNode methodInsn = (MethodInsnNode) instruction;

				if (methodInsn.desc.equals(ASMHelper.toMethodDescriptor("V", "I", "I", "I", "I", "I", "I", "I")))
					targetNode = instruction;
			}
		}
		if (targetNode == null)
			throw new RuntimeException("Unexpected instruction pattern encountered in " + method.name);

		LocalVariableNode x = ASMHelper.findLocalVariableOfMethod(method, "tooltipX", "I");
		LocalVariableNode y = ASMHelper.findLocalVariableOfMethod(method, "tooltipY", "I");
		LocalVariableNode w = ASMHelper.findLocalVariableOfMethod(method, "tooltipTextWidth", "I");
		LocalVariableNode h = ASMHelper.findLocalVariableOfMethod(method, "tooltipHeight", "I");

		if (x == null || y == null || w == null || h == null)
		{
			AppleCore.Log.warn("Could not patch " + method.name + "; local variables not found");
			return;
		}

		InsnList toInject = new InsnList();

		/*
		// equivalent to:
		Hooks.onDrawHoveringText(0, 0, 0, 0);
		*/

		toInject.add(new VarInsnNode(ILOAD, x.index));
		toInject.add(new VarInsnNode(ILOAD, y.index));
		toInject.add(new VarInsnNode(ILOAD, w.index));
		toInject.add(new VarInsnNode(ILOAD, h.index));
		toInject.add(new MethodInsnNode(INVOKESTATIC, ASMConstants.HooksInternalClass, hookMethod, hookDesc, false));

		method.instructions.insert(targetNode, toInject);
	}

	public void addCodeChickenDrawHoveringTextHook(MethodNode method, String hookMethod, String hookDesc)
	{
		AbstractInsnNode targetNode = ASMHelper.findFirstInstruction(method);

		InsnList toInject = new InsnList();

		/*
		// equivalent to:
		Hooks.onDrawHoveringText(0, 0, 0, 0);
		*/

		toInject.add(new VarInsnNode(ILOAD, 0));	// x
		toInject.add(new VarInsnNode(ILOAD, 1));	// y
		toInject.add(new VarInsnNode(ILOAD, 2));	// w
		toInject.add(new VarInsnNode(ILOAD, 3));	// h
		toInject.add(new MethodInsnNode(INVOKESTATIC, ASMConstants.HooksInternalClass, hookMethod, hookDesc, false));

		method.instructions.insertBefore(targetNode, toInject);
	}
}