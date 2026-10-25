package tfcflorae.common.entities;

import java.util.Map;
import java.util.function.Supplier;
import net.dries007.tfc.common.entities.EntityHelpers;
import net.dries007.tfc.common.entities.Fauna;
import net.dries007.tfc.common.entities.aquatic.AquaticMob;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.SpawnPlacements.Type;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraftforge.registries.RegistryObject;

public class TFCFFaunas {
   public static final TFCFFaunas.FaunaType<Silkmoth> SILK_MOTH = registerAnimal(TFCFEntities.SILKMOTH);
   // 青蛙 / 蝌蚪 / 鹦鹉已移除，对应的 fauna 与生成放置一并去掉

   public TFCFFaunas() {
   }

   public static void registerSpawnPlacements() {
      registerSpawnPlacement(SILK_MOTH);
   }

   private static <E extends Mob> TFCFFaunas.FaunaType<E> registerAnimal(RegistryObject<EntityType<E>> entity) {
      return register(entity, Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES);
   }

   private static <E extends Mob> TFCFFaunas.FaunaType<E> registerFish(RegistryObject<EntityType<E>> entity) {
      return register(entity, Type.IN_WATER, Types.MOTION_BLOCKING_NO_LEAVES);
   }

   private static <E extends Mob> TFCFFaunas.FaunaType<E> register(RegistryObject<EntityType<E>> entity, Type spawnPlacement, Types heightmapType) {
      Supplier<Fauna> fauna = Fauna.MANAGER.register(entity.getId());
      return new TFCFFaunas.FaunaType<>(entity, fauna, spawnPlacement, heightmapType);
   }

   private static <E extends Mob> void registerSpawnPlacement(TFCFFaunas.FaunaType<E> type) {
      SpawnPlacements.register(
         type.entity().get(),
         type.spawnPlacementType(),
         type.heightmapType(),
         (mob, level, heightmap, pos, rand) -> {
            Fauna fauna = type.fauna().get();
            ChunkGenerator generator = level.getLevel().getChunkSource().getGenerator();
            if (rand.nextInt(fauna.getChance()) != 0) {
               return false;
            } else {
               if (mob instanceof AquaticMob aquaticMob && !aquaticMob.canSpawnIn(level.getFluidState(pos).getType())) {
                  return false;
               }

               int seaLevel = generator.getSeaLevel();
               if (fauna.getDistanceBelowSeaLevel() != -1 && pos.getY() > seaLevel - fauna.getDistanceBelowSeaLevel()) {
                  return false;
               } else {
                  ChunkData data = EntityHelpers.getChunkDataForSpawning(level, pos);
                  if (!fauna.getClimate().isValid(data, pos, rand)) {
                     return false;
                  } else {
                     BlockPos below = pos.below();
                     return fauna.isSolidGround() && !Helpers.isBlock(level.getBlockState(below), BlockTags.VALID_SPAWN)
                        ? false
                        : fauna.getMaxBrightness() == -1 || level.getRawBrightness(pos, 0) <= fauna.getMaxBrightness();
                  }
               }
            }
         }
      );
   }

   static record FaunaType<E extends Mob>(Supplier<EntityType<E>> entity, Supplier<Fauna> fauna, Type spawnPlacementType, Types heightmapType) {
   }
}
