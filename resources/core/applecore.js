var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var FOODSTATS_PLAYER_FIELD = "entityplayer";
var FOODSTATS_STARVE_TIMER_FIELD = "starveTimer";

function injectFoodStatsConstructor(classNode) {
	// get the default constructor, copy it, apply max hunger patches, set player field
	var defaultConstructor = ASMHelper.findMethodNodeOfClass(classNode, "<init>", ASMHelper.toMethodDescriptor("V"));

	if (defaultConstructor == null)
		throw "FoodStats.<init>() not found";

	var constructor = new MethodNode(ACC_PUBLIC, "<init>", ASMHelper.toMethodDescriptor("V", ASMConstants.PLAYER), null, null);
	constructor.instructions = ASMHelper.cloneInsnList(defaultConstructor.instructions);

	var foodLevelReplacement = new InsnList();
	foodLevelReplacement.add(new VarInsnNode(ALOAD, 0));
	foodLevelReplacement.add(new MethodInsnNode(INVOKESTATIC, ASMHelper.toInternalClassName(ASMConstants.HOOKS), "getMaxHunger", ASMHelper.toMethodDescriptor("I", ASMConstants.FOOD_STATS), false));

	// replace all instances of BIPUSH 20 with getMaxHunger calls
	var numReplacements = 0;
	for (var instruction = constructor.instructions.getFirst(); instruction != null; instruction = instruction.getNext()) {
		if (instruction.getOpcode() == BIPUSH && instruction.operand == 20) {
			var replacement = ASMHelper.cloneInsnList(foodLevelReplacement);
			var after = instruction.getNext();
			constructor.instructions.insertBefore(instruction, replacement);
			constructor.instructions.remove(instruction);
			instruction = after;
			numReplacements += 1;
		}
	}

	if (numReplacements < 2)
		throw "FoodStats.<init>() replaced " + numReplacements + " (BIPUSH 20) instructions, expected >= 2";

	var targetNode = ASMHelper.findFirstInstructionWithOpcode(constructor, INVOKESPECIAL);

	var toInject = new InsnList();
	toInject.add(new VarInsnNode(ALOAD, 0)); // this
	toInject.add(new VarInsnNode(ALOAD, 1)); // player param
	toInject.add(new FieldInsnNode(PUTFIELD, classNode.name, FOODSTATS_PLAYER_FIELD, ASMHelper.toDescriptor(ASMConstants.PLAYER)));

	constructor.instructions.insert(targetNode, toInject);

	classNode.methods.add(constructor);
}

function hookFoodStatsAddition(/*ClassNode*/ classNode, /*MethodNode*/ method)
{
	// injected code:
	/*
	if (!Hooks.fireFoodStatsAdditionEvent(player, new FoodValues(p_75122_1_, p_75122_2_)))
	{
		// default code
	}
	*/

	var targetNode = ASMHelper.findFirstInstruction(method);
	var ifCanceled = new LabelNode();

	var toInject = new InsnList();
	toInject.add(new VarInsnNode(ALOAD, 0));
	toInject.add(new FieldInsnNode(GETFIELD, ASMHelper.toInternalClassName(classNode.name), foodStatsPlayerField, ASMHelper.toDescriptor(ASMConstants.PLAYER)));
	toInject.add(new TypeInsnNode(NEW, ASMHelper.toInternalClassName(ASMConstants.FOOD_VALUES)));
	toInject.add(new InsnNode(DUP));
	toInject.add(new VarInsnNode(ILOAD, 1));
	toInject.add(new VarInsnNode(FLOAD, 2));
	toInject.add(new MethodInsnNode(INVOKESPECIAL, ASMHelper.toInternalClassName(ASMConstants.FOOD_VALUES), "<init>", ASMHelper.toMethodDescriptor("V", "I", "F"), false));
	toInject.add(new MethodInsnNode(INVOKESTATIC, ASMConstants.HOOKS_INTERNAL_CLASS, "fireFoodStatsAdditionEvent", ASMHelper.toMethodDescriptor("Z", ASMConstants.PLAYER, ASMConstants.FOOD_VALUES), false));
	toInject.add(new JumpInsnNode(IFNE, ifCanceled));

	method.instructions.insertBefore(targetNode, toInject);

	targetNode = ASMHelper.findLastInstructionWithOpcode(method, RETURN);

	method.instructions.insertBefore(targetNode, ifCanceled);

	// BIPUSH 20 replaced with GetMaxHunger lookup
	var needle = new InsnList();
	needle.add(new IntInsnNode(BIPUSH, 20));
	var replacement = new InsnList();
	replacement.add(new VarInsnNode(ALOAD, 0));
	replacement.add(new MethodInsnNode(INVOKESTATIC, ASMHelper.toInternalClassName(ASMConstants.HOOKS), "getMaxHunger", ASMHelper.toMethodDescriptor("I", ASMConstants.FOOD_STATS), false));

	ASMHelper.findAndReplaceAll(method.instructions, needle, replacement);
}

function transformFoodStats(classNode) {
	ASMAPI.log("INFO", ASMAPI.mapMethod("func_75122_a"));
	classNode.fields.add(new FieldNode(ACC_PUBLIC, FOODSTATS_PLAYER_FIELD, ASMHelper.toDescriptor(ASMConstants.PLAYER), null, null));
	classNode.fields.add(new FieldNode(ACC_PUBLIC, FOODSTATS_STARVE_TIMER_FIELD, "I", null, null));
	//injectFoodStatsConstructor(classNode);

/*
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

	var addStatsMethodNode = ASMHelper.findMethodNodeOfClass(classNode, ASMAPI.mapMethod("func_75122_a"), ASMHelper.toMethodDescriptor("V", "I", "F"));
	if (addStatsMethodNode == null)
		throw "FoodStats: addStats(IF)V method not found";
	hookFoodStatsAddition(classNode, addStatsMethodNode);
	*/
	return classNode;
}

function initializeCoreMod() {
	ASMAPI.loadFile('core/asmhelper.js');
	ASMAPI.loadFile('core/asmconstants.js');
	ASMAPI.loadFile('core/import_asm.js');
	ASMAPI.loadFile('core/import_asm_opcodes.js');
	ASMAPI.loadFile('core/utils.js');
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