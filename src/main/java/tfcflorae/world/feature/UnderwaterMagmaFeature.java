package tfcflorae.world.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.util.RandomSource;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.settings.RockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.Direction.Plane;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.phys.AABB;

public class UnderwaterMagmaFeature extends Feature<UnderwaterMagmaConfig> {
   public UnderwaterMagmaFeature(Codec<UnderwaterMagmaConfig> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<UnderwaterMagmaConfig> context) {
      WorldGenLevel level = context.level();
      BlockPos pos = context.origin();
      RandomSource random = context.random();
      UnderwaterMagmaConfig config = (UnderwaterMagmaConfig)context.config();
      ChunkDataProvider provider = ChunkDataProvider.get(context.chunkGenerator());
      ChunkData data = provider.get(context.level(), pos);
      RockSettings rock = data.getRockData().getRock(pos);
      List<BlockState> states = config.getStates(rock.raw());
      OptionalInt optionalInt = getFloorY(level, pos, context);
      if (states != null) {
         Supplier<BlockState> state;
         if (states.size() == 1) {
            BlockState onlyState = states.get(0);
            state = () -> onlyState;
         } else {
            state = () -> states.get(random.nextInt(states.size()));
         }

         if (!optionalInt.isPresent()) {
            return false;
         } else {
            BlockPos pos1 = pos.atY(optionalInt.getAsInt());
            Vec3i vec3i = new Vec3i(config.placementRadiusAroundFloor(), config.placementRadiusAroundFloor(), config.placementRadiusAroundFloor());
            AABB aabb = new AABB(pos1.subtract(vec3i), pos1.offset(vec3i));
            return BlockPos.betweenClosedStream(aabb)
                  .filter(p_160573_ -> random.nextFloat() < config.placementProbabilityPerValidPosition())
                  .filter(p_160584_ -> this.isValidPlacement(level, p_160584_))
                  .mapToInt(p_160579_ -> {
                     level.setBlock(p_160579_, state.get(), 2);
                     return 1;
                  })
                  .sum()
               > 0;
         }
      } else {
         return false;
      }
   }

   private static OptionalInt getFloorY(WorldGenLevel level, BlockPos pos, FeaturePlaceContext<UnderwaterMagmaConfig> context) {
      Predicate<BlockState> predicate = state -> state.is(Blocks.WATER);
      Predicate<BlockState> predicate1 = state -> !state.is(Blocks.WATER);
      Optional<Column> optional = Column.scan(level, pos, ((UnderwaterMagmaConfig)context.config()).floorSearchRange(), predicate, predicate1);
      return optional.<OptionalInt>map(Column::getFloor).orElseGet(OptionalInt::empty);
   }

   private boolean isValidPlacement(WorldGenLevel level, BlockPos pos) {
      if (!this.isWaterOrAir(level, pos) && !this.isWaterOrAir(level, pos.below())) {
         for (Direction direction : Plane.HORIZONTAL) {
            if (this.isWaterOrAir(level, pos.relative(direction))) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean isWaterOrAir(LevelAccessor level, BlockPos pos) {
      BlockState blockstate = level.getBlockState(pos);
      return blockstate.is(Blocks.WATER) || blockstate.isAir();
   }
}
