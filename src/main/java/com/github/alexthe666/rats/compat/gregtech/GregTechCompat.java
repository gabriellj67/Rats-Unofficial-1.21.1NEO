package com.github.alexthe666.rats.compat.gregtech;

import net.minecraft.world.level.block.entity.BlockEntity;

import java.lang.reflect.Method;

public final class GregTechCompat {

	private static Class<?> recipeLogicMachineClass;
	private static Method getRecipeLogicMethod;
	private static Method getStatusMethod;
	private static Method getProgressMethod;
	private static Method getDurationMethod;
	private static Method setProgressDeltaMethod;
	private static boolean available = false;

	private GregTechCompat() {}

	public static void init() {
		try {
			recipeLogicMachineClass = Class.forName("com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine");
			getRecipeLogicMethod = recipeLogicMachineClass.getMethod("getRecipeLogic");
			Class<?> recipeLogicClass = Class.forName("com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic");
			getStatusMethod = recipeLogicClass.getMethod("getStatus");
			getProgressMethod = recipeLogicClass.getMethod("getProgress");
			getDurationMethod = recipeLogicClass.getMethod("getDuration");
			setProgressDeltaMethod = recipeLogicClass.getMethod("setProgressDelta", int.class);
			available = true;
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean accelerateMachine(BlockEntity entity, int amount) {
		if (!available || entity == null || !recipeLogicMachineClass.isInstance(entity)) {
			return false;
		}
		try {
			Object recipeLogic = getRecipeLogicMethod.invoke(entity);
			if (recipeLogic == null) {
				return false;
			}
			Object status = getStatusMethod.invoke(recipeLogic);
			if (status == null || !"WORKING".equals(status.toString())) {
				return false;
			}
			int progress = (int) getProgressMethod.invoke(recipeLogic);
			int duration = (int) getDurationMethod.invoke(recipeLogic);
			if (duration <= 0 || progress >= duration) {
				return false;
			}
			setProgressDeltaMethod.invoke(recipeLogic, Math.min(amount, duration - progress));
			return true;
		} catch (ReflectiveOperationException | ClassCastException e) {
			available = false;
			return false;
		}
	}
}
