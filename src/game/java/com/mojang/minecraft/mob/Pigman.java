package com.mojang.minecraft.mob;

import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.mob.HumanoidMob;
import com.mojang.minecraft.mob.ai.BasicAttackAI;

public class Pigman extends HumanoidMob {

   public static final long serialVersionUID = 0L;


   public Pigman(Level var1, float var2, float var3, float var4) {
      super(var1, var2, var3, var4);
      this.modelName = "pigman";
      this.textureName = "/mob/pigman.png";
      this.heightOffset = 1.62F;
      BasicAttackAI var5 = new BasicAttackAI();
      this.deathScore = 80;
      var5.defaultLookAngle = 30;
      var5.runSpeed = 1.0F;
      this.ai = var5;
   }
}
