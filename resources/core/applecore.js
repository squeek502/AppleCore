var Opcodes = Java.type("org.objectweb.asm.Opcodes");
var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
var LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
var JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
var Type = Java.type("org.objectweb.asm.Type");
var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");

var FOODSTATS_PLAYER_FIELD = "entityplayer";
var FOODSTATS_STARVE_TIMER_FIELD = "starveTimer";

var ASMConstants = {
	PLAYER: "net.minecraft.entity.player.PlayerEntity",
	IAPPLECOREFOODSTATS: "squeek.applecore.asm.util.IAppleCoreFoodStats"
}

var ASMHelper = {
	/**
	 * Converts a class name to an internal class name.
	 * @return internal/class/name
	 */
	toInternalClassName: function(className) {
		return className.replace('.', '/');
	},
	/**
	 * @return true if the String is a valid descriptor;
	 */
	isDescriptor: function(descriptor) {
		return descriptor.length == 1 || (descriptor.startsWith("L") && descriptor.endsWith(";"));
	},
	/**
	 * Converts a class name to a descriptor.
	 * @return Linternal/class/name;
	 */
	toDescriptor: function(className) {
		return this.isDescriptor(className) ? className : "L" + this.toInternalClassName(className) + ";";
	},
	/**
	 * Turns the given return and parameter values into a method descriptor
	 * Converts the types into descriptors as needed
	 * @return (LparamType;)LreturnType;
	 */
	toMethodDescriptor: function(returnType) {
		// gather the rest of the arguments as an array, but exclude the first argument
		var paramTypes = Array.from(arguments);
		paramTypes.shift();

		var paramDescriptors = paramTypes.map(this.toDescriptor).join("");
		return "(" + paramDescriptors + ")" + this.toDescriptor(returnType);
	},
	/**
	 * @return The method of the class that has both a matching {@code methodName} and {@code methodDesc}.
	 * If no matching method is found, returns {@code null}.
	 */
	findMethodNodeOfClass: function(classNode, methodName, methodDesc) {
		return classNode.methods.find(function(method) {
			return method.name == methodName && (!methodDesc || method.desc == methodDesc)
		});
	}
}

function tryAddFieldGetter(classNode, methodName, fieldName, fieldDescriptor) {
	var methodDescriptor = ASMHelper.toMethodDescriptor(fieldDescriptor);
	if (ASMHelper.findMethodNodeOfClass(classNode, methodName, methodDescriptor) !== undefined)
		return false;

	var mv = classNode.visitMethod(Opcodes.ACC_PUBLIC, methodName, methodDescriptor, null, null);
	mv.visitVarInsn(Opcodes.ALOAD, 0);
	mv.visitFieldInsn(Opcodes.GETFIELD, ASMHelper.toInternalClassName(classNode.name), fieldName, fieldDescriptor);
	mv.visitInsn(Type.getType(fieldDescriptor).getOpcode(Opcodes.IRETURN));
	mv.visitMaxs(0, 0);
	return true;
}

function tryAddFieldSetter(classNode, methodName, fieldName, fieldDescriptor) {
	var methodDescriptor = ASMHelper.toMethodDescriptor("V", fieldDescriptor);
	if (ASMHelper.findMethodNodeOfClass(classNode, methodName, methodDescriptor) !== undefined)
		return false;

	var mv = classNode.visitMethod(Opcodes.ACC_PUBLIC, methodName, methodDescriptor, null, null);
	mv.visitVarInsn(Opcodes.ALOAD, 0);
	mv.visitVarInsn(Type.getType(fieldDescriptor).getOpcode(Opcodes.ILOAD), 1);
	mv.visitFieldInsn(Opcodes.PUTFIELD, ASMHelper.toInternalClassName(classNode.name), fieldName, fieldDescriptor);
	mv.visitInsn(Opcodes.RETURN);
	mv.visitMaxs(0, 0);
	return true;
}

function transformFoodStats(classNode) {
	classNode.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, FOODSTATS_PLAYER_FIELD, ASMHelper.toDescriptor(ASMConstants.PLAYER), null, null));
	classNode.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, FOODSTATS_STARVE_TIMER_FIELD, "I", null, null));

	// TODO: patch FoodStats constructor to take a player parameter

	// IAppleCoreFoodStats implementation
	classNode.interfaces.add(ASMHelper.toInternalClassName(ASMConstants.IAPPLECOREFOODSTATS));
	tryAddFieldGetter(classNode, "getFoodTimer", "field_75123_d", "I");
	tryAddFieldSetter(classNode, "setFoodTimer", "field_75123_d", "I");
	tryAddFieldGetter(classNode, "getStarveTimer", FOODSTATS_STARVE_TIMER_FIELD, "I");
	tryAddFieldSetter(classNode, "setStarveTimer", FOODSTATS_STARVE_TIMER_FIELD, "I");
	tryAddFieldGetter(classNode, "getPlayer", FOODSTATS_PLAYER_FIELD, ASMHelper.toDescriptor(ASMConstants.PLAYER));
	tryAddFieldSetter(classNode, "setPlayer", FOODSTATS_PLAYER_FIELD, ASMHelper.toDescriptor(ASMConstants.PLAYER));
	tryAddFieldSetter(classNode, "setPrevFoodLevel", "field_75124_e", "I");
	tryAddFieldGetter(classNode, "getExhaustion", "field_75126_c", "F");
	tryAddFieldSetter(classNode, "setExhaustion", "field_75126_c", "F");
	tryAddFieldSetter(classNode, "setSaturation", "field_75125_b", "F");

	return classNode;
}

function initializeCoreMod() {
	return {
		"FoodStatsTransformer": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.util.FoodStats"
			},
			"transformer": transformFoodStats
		}
	}
}