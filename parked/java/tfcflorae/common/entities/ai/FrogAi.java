package tfcflorae.common.entities.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.RandomSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.ai.behavior.AnimalPanic;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.FollowTemptation;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.behavior.LongJumpMidJump;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.TriggerGate;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.GateBehavior.OrderPolicy;
import net.minecraft.world.entity.ai.behavior.GateBehavior.RunningPolicy;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import tfcflorae.client.TFCFSounds;
import tfcflorae.common.TFCFTags;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.entities.Frog;
import tfcflorae.common.entities.TFCFEntities;
import tfcflorae.common.entities.ai.frog.BiasedLongJumpTask;
import tfcflorae.common.entities.ai.frog.Croak;
import tfcflorae.common.entities.ai.frog.FrogEat;
import tfcflorae.common.entities.ai.frog.LayFrogSpawn;
import tfcflorae.common.entities.ai.frog.WalkTowardsLand;
import tfcflorae.common.entities.ai.frog.WalkTowardsWater;

public class FrogAi {
   private static final UniformInt LONG_JUMP_COOLDOWN_RANGE = UniformInt.of(100, 140);

   public FrogAi() {
   }

   public static void coolDownLongJump(Frog frog, RandomSource random) {
      frog.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, LONG_JUMP_COOLDOWN_RANGE.sample(random));
   }

   public static Brain<?> create(Brain<Frog> brain) {
      addCoreActivities(brain);
      addIdleActivities(brain);
      addSwimActivities(brain);
      addLaySpawnActivities(brain);
      addTongueActivities(brain);
      addLongJumpActivities(brain);
      brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
      brain.setDefaultActivity(Activity.IDLE);
      brain.useDefaultActivity();
      return brain;
   }

   private static void addCoreActivities(Brain<Frog> brain) {
      brain.addActivity(
         Activity.CORE,
         0,
         ImmutableList.of(
            new AnimalPanic(2.0F),
            new LookAtTargetSink(45, 90),
            new MoveToTargetSink(),
            new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS),
            new CountDownCooldownTicks(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS)
         )
      );
   }

   private static void addIdleActivities(Brain<Frog> brain) {
      brain.addActivityWithConditions(
         Activity.IDLE,
         ImmutableList.of(
            Pair.of(0, SetEntityLookTarget.create(EntityType.PLAYER, 6.0F)),
            Pair.of(0, new AnimalMakeLove((EntityType)TFCFEntities.FROG.get(), 1.0F)),
            Pair.of(1, new FollowTemptation(entity -> 1.25F)),
            Pair.of(2, StartAttacking.create(FrogAi::isNotBreeding, frog -> frog.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE))),
            Pair.of(3, new WalkTowardsLand(6, 1.0F)),
            Pair.of(
               4,
               new RunOne(
                  ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
                  ImmutableList.of(
                     Pair.of(RandomStroll.stroll(1.0F), 1),
                     Pair.of(SetWalkTargetFromLookTarget.create(1.0F, 3), 1),
                     Pair.of(new Croak(), 3),
                     Pair.of(new DoNothing(5, 20), 2)
                  )
               )
            )
         ),
         ImmutableSet.of(
            Pair.of(MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_ABSENT),
            Pair.of((MemoryModuleType)TFCFBrain.IS_IN_WATER_MEMORY.get(), MemoryStatus.VALUE_ABSENT)
         )
      );
   }

   private static void addSwimActivities(Brain<Frog> brain) {
      brain.addActivityWithConditions(
         (Activity)TFCFBrain.SWIM.get(),
         ImmutableList.of(
            Pair.of(0, SetEntityLookTarget.create(EntityType.PLAYER, 6.0F)),
            Pair.of(1, new FollowTemptation(entity -> 1.25F)),
            Pair.of(2, StartAttacking.create(FrogAi::isNotBreeding, frog -> frog.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE))),
            Pair.of(3, new WalkTowardsLand(8, 1.5F)),
            Pair.of(
               5,
               new GateBehavior(
                  ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
                  ImmutableSet.of(),
                  OrderPolicy.ORDERED,
                  RunningPolicy.TRY_ALL,
                  ImmutableList.of(
                     Pair.of(RandomStroll.stroll(0.75F), 1),
                     Pair.of(RandomStroll.stroll(1.0F, true), 1),
                     Pair.of(SetWalkTargetFromLookTarget.create(1.0F, 3), 1),
                     Pair.of(new DoNothing(30, 60), 5)
                  )
               )
            )
         ),
         ImmutableSet.of(
            Pair.of(MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_ABSENT),
            Pair.of((MemoryModuleType)TFCFBrain.IS_IN_WATER_MEMORY.get(), MemoryStatus.VALUE_PRESENT)
         )
      );
   }

   private static void addLaySpawnActivities(Brain<Frog> brain) {
      brain.addActivityWithConditions(
         (Activity)TFCFBrain.LAY_SPAWN.get(),
         ImmutableList.of(
            Pair.of(0, SetEntityLookTarget.create(EntityType.PLAYER, 6.0F)),
            Pair.of(1, StartAttacking.create(FrogAi::isNotBreeding, frog -> frog.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE))),
            Pair.of(2, new WalkTowardsWater(8, 1.0F)),
            Pair.of(3, new LayFrogSpawn((Block)TFCFBlocks.FROGSPAWN.get(), (MemoryModuleType<?>)TFCFBrain.IS_PREGNANT.get())),
            Pair.of(
               4,
               new RunOne(
                  ImmutableList.of(
                     Pair.of(RandomStroll.stroll(1.0F), 2),
                     Pair.of(SetWalkTargetFromLookTarget.create(1.0F, 3), 1),
                     Pair.of(new Croak(), 2),
                     Pair.of(new DoNothing(5, 20), 1)
                  )
               )
            )
         ),
         ImmutableSet.of(
            Pair.of(MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_ABSENT), Pair.of((MemoryModuleType)TFCFBrain.IS_PREGNANT.get(), MemoryStatus.VALUE_PRESENT)
         )
      );
   }

   private static void addLongJumpActivities(Brain<Frog> brain) {
      brain.addActivityWithConditions(
         Activity.LONG_JUMP,
         ImmutableList.of(
            Pair.of(0, new LongJumpMidJump(LONG_JUMP_COOLDOWN_RANGE, TFCFSounds.FROG.step().get())),
            Pair.of(
               1,
               new BiasedLongJumpTask(
                  LONG_JUMP_COOLDOWN_RANGE,
                  2,
                  4,
                  1.5F,
                  frog -> (SoundEvent)TFCFSounds.FROG_LONG_JUMP.get(),
                  TFCFTags.Blocks.FROG_PREFER_JUMP_TO,
                  0.5F,
                  pos -> false
               )
            )
         ),
         ImmutableSet.of(
            Pair.of(MemoryModuleType.TEMPTING_PLAYER, MemoryStatus.VALUE_ABSENT),
            Pair.of(MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT),
            Pair.of(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT),
            Pair.of((MemoryModuleType)TFCFBrain.IS_IN_WATER_MEMORY.get(), MemoryStatus.VALUE_ABSENT)
         )
      );
   }

   private static void addTongueActivities(Brain<Frog> brain) {
      brain.addActivityAndRemoveMemoryWhenStopped(
         (Activity) TFCFBrain.TONGUE.get(),
         0,
         (ImmutableList) ImmutableList.of(
            new StopAttackingIfTargetInvalid(),
            new FrogEat((SoundEvent) TFCFSounds.FROG_TONGUE.get(), (SoundEvent) TFCFSounds.FROG_EAT.get())
         ),
         MemoryModuleType.ATTACK_TARGET
      );
   }

   private static boolean isNotBreeding(Frog frog) {
      return !frog.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET);
   }

   public static void updateActivities(Frog frog) {
      frog.getBrain()
         .setActiveActivityToFirstValid(
            ImmutableList.of(
               (Activity)TFCFBrain.TONGUE.get(), (Activity)TFCFBrain.LAY_SPAWN.get(), Activity.LONG_JUMP, (Activity)TFCFBrain.SWIM.get(), Activity.IDLE
            )
         );
   }

   public static Ingredient getTemptItems() {
      return Frog.FOOD;
   }
}
