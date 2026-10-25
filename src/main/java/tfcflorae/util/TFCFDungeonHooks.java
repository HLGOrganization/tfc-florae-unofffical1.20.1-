package tfcflorae.util;

import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.util.random.WeightedEntry.IntrusiveBase;
import net.minecraft.world.entity.EntityType;

public class TFCFDungeonHooks {
   private static ArrayList<TFCFDungeonHooks.DungeonMob> dungeonMobs = new ArrayList<>();

   public TFCFDungeonHooks() {
   }

   public static float addDungeonMob(EntityType<?> type, int rarity) {
      if (rarity <= 0) {
         throw new IllegalArgumentException("Rarity must be greater then zero");
      } else {
         Iterator<TFCFDungeonHooks.DungeonMob> itr = dungeonMobs.iterator();

         while (itr.hasNext()) {
            TFCFDungeonHooks.DungeonMob mob = itr.next();
            if (type == mob.type) {
               itr.remove();
               rarity += mob.getWeight().asInt();
               break;
            }
         }

         dungeonMobs.add(new TFCFDungeonHooks.DungeonMob(rarity, type));
         return (float)rarity;
      }
   }

   public static int removeDungeonMob(EntityType<?> name) {
      for (TFCFDungeonHooks.DungeonMob mob : dungeonMobs) {
         if (name == mob.type) {
            dungeonMobs.remove(mob);
            return mob.getWeight().asInt();
         }
      }

      return 0;
   }

   public static EntityType<?> getRandomDungeonMob(RandomSource rand) {
      TFCFDungeonHooks.DungeonMob mob = (TFCFDungeonHooks.DungeonMob)WeightedRandom.getRandomItem(rand, dungeonMobs).orElseThrow();
      return mob.type;
   }

   static {
      addDungeonMob(EntityType.SKELETON, 100);
      addDungeonMob(EntityType.ZOMBIE, 300);
      addDungeonMob(EntityType.SPIDER, 100);
      addDungeonMob(EntityType.CAVE_SPIDER, 200);
      addDungeonMob(EntityType.WITHER_SKELETON, 40);
      addDungeonMob(EntityType.STRIDER, 20);
      addDungeonMob(EntityType.HUSK, 200);
      addDungeonMob(EntityType.CREEPER, 30);
   }

   public static class DungeonMob extends IntrusiveBase {
      public final EntityType<?> type;

      public DungeonMob(int weight, EntityType<?> type) {
         super(weight);
         this.type = type;
      }

      public boolean equals(Object target) {
         return target instanceof TFCFDungeonHooks.DungeonMob && this.type.equals(((TFCFDungeonHooks.DungeonMob)target).type);
      }
   }
}
