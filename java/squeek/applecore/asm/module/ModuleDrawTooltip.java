package squeek.applecore.asm.module;

import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import squeek.applecore.AppleCore;
import squeek.applecore.asm.ASMConstants;
import squeek.applecore.asm.IClassTransformerModule;
import squeek.asmhelper.applecore.ASMHelper;

public class ModuleDrawTooltip implements IClassTransformerModule
{
	@Override
	public String[] getClassesToTransform()
	{
		return new String[]{
		"net.minecraft.client.gui.GuiScreen",
		"codechicken.lib.gui.GuiDraw"
		};
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes)
	{
		ClassNode classNode = ASMHelper.readClassFromBytes(bytes);

		if (transformedName.equals("net.minecraft.client.gui.GuiScreen"))
		{

			MethodNode methodNode = ASMHelper.findMethodNodeOfClass(classNode, "drawHoveringText", "(Ljava/util/List;IILnet/minecraft/client/gui/FontRenderer;)V");

			if (methodNode != null)
			{
				addDrawHoveringTextHook(methodNode, "onDrawHoveringText", "(IIII)V");
			}
			else
				throw new RuntimeException("GuiScreen.drawHoveringText not found");
		}
		else if (name.equals("codechicken.lib.gui.GuiDraw"))
		{
			MethodNode methodNode = ASMHelper.findMethodNodeOfClass(classNode, "drawTooltipBox", "(IIII)V");

			if (methodNode != null)
			{
				addCodeChickenDrawHoveringTextHook(methodNode, "onDrawHoveringText", "(IIII)V");
			}
			else
				AppleCore.Log.error("drawTooltipBox method in codechicken.lib.gui.GuiDraw not found");
		}

		return ASMHelper.writeClassToBytes(classNode);
	}

	public void addDrawHoveringTextHook(MethodNode method, String hookMethod, String hookDesc)
	{
		AbstractInsnNode targetNode = null;

		// get last drawGradientRect call
		for (AbstractInsnNode instruction : method.instructions.toArray())
		{
			if (instruction.getOpcode() == INVOKEVIRTUAL)
			{
				MethodInsnNode methodInsn = (MethodInsnNode) instruction;

				if (methodInsn.desc.equals("(IIIIII)V"))
					targetNode = instruction;
			}
		}
		if (targetNode == null)
			throw new RuntimeException("Unexpected instruction pattern encountered in " + method.name);

		LocalVariableNode x = ASMHelper.findLocalVariableOfMethod(method, "j2", "I");
		LocalVariableNode y = ASMHelper.findLocalVariableOfMethod(method, "k2", "I");
		LocalVariableNode w = ASMHelper.findLocalVariableOfMethod(method, "k", "I");
		LocalVariableNode h = ASMHelper.findLocalVariableOfMethod(method, "i1", "I");

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
