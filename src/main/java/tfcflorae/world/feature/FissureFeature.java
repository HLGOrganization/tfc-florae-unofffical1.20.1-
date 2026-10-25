package tfcflorae.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.feature.FissureConfig;
import net.dries007.tfc.world.feature.FissureConfig.Decoration;
import net.dries007.tfc.world.settings.RockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Plane;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import org.jetbrains.annotations.Nullable;

public class FissureFeature extends Feature<FissureConfig> {
   public static void placeFissure(
      WorldGenLevel level,
      BlockPos startPos,
      BlockPos centerPos,
      MutableBlockPos mutablePos,
      RandomSource random,
      BlockState insideState,
      BlockState wallState,
      int minPieces,
      int maxPieces,
      int maxPieceLength,
      int minDepth,
      int radius,
      @Nullable Decoration decoration
   ) {
      int pieces = minPieces + random.nextInt(maxPieces - minPieces);
      BlockPos topPos = startPos.immutable();

      for (int i = 0; i < pieces; i++) {
         int pieceDepth = 1 + random.nextInt(maxPieceLength);

         for (int dy = 1; dy <= pieceDepth; dy++) {
            level.setBlock(mutablePos.setWithOffset(topPos, 0, -dy, 0), insideState, 2);
            level.setBlock(mutablePos.setWithOffset(topPos, -1, -dy, 0), wallState, 2);
            level.setBlock(mutablePos.setWithOffset(topPos, 1, -dy, 0), wallState, 2);
            level.setBlock(mutablePos.setWithOffset(topPos, 0, -dy, -1), wallState, 2);
            level.setBlock(mutablePos.setWithOffset(topPos, 0, -dy, 1), wallState, 2);
            if (decoration != null) {
               for (int j = 0; j < decoration.count(); j++) {
                  if (random.nextInt(decoration.rarity()) == 0) {
                     mutablePos.setWithOffset(
                        topPos,
                        random.nextInt(decoration.radius()) - random.nextInt(decoration.radius()),
                        random.nextInt(3) - random.nextInt(3) - dy,
                        random.nextInt(decoration.radius()) - random.nextInt(decoration.radius())
                     );
                     BlockState stoneState = level.getBlockState(mutablePos);
                     BlockState decorationState = decoration.getState(stoneState, random);
                     if (decorationState != null) {
                        level.setBlock(mutablePos, decorationState, 2);
                     }
                  }
               }
            }
         }

         Direction branchDirection = randomBoundedDirection(random, centerPos, topPos, radius);
         topPos = mutablePos.setWithOffset(topPos, 0, -pieceDepth, 0).move(branchDirection).immutable();
         level.setBlock(mutablePos.set(topPos), insideState, 2);
         level.setBlock(mutablePos.setWithOffset(topPos, 0, 1, 0), wallState, 2);

         for (Direction direction : Plane.HORIZONTAL) {
            if (direction != branchDirection.getOpposite()) {
               level.setBlock(mutablePos.setWithOffset(topPos, direction), wallState, 2);
            }
         }

         if (topPos.getY() < minDepth) {
            break;
         }
      }
   }

   private static Direction randomBoundedDirection(RandomSource random, BlockPos center, BlockPos target, int radius) {
      Direction direction = Plane.HORIZONTAL.getRandomDirection(random);
      int distX = target.getX() - center.getX();
      int distZ = target.getZ() - center.getZ();
      switch (direction) {
         case EAST:
            if (distX > radius) {
               return Direction.WEST;
            }

            return direction;
         case WEST:
            if (distX < -radius) {
               return Direction.EAST;
            }

            return direction;
         case NORTH:
            if (distZ < -radius) {
               return Direction.SOUTH;
            }

            return direction;
         case SOUTH:
            if (distZ > radius) {
               return Direction.NORTH;
            }

            return direction;
         default:
            return direction;
      }
   }

   public FissureFeature(Codec<FissureConfig> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<FissureConfig> context) {
      WorldGenLevel level = context.level();
      BlockPos pos = context.origin();
      RandomSource rand = context.random();
      FissureConfig config = (FissureConfig)context.config();
      MutableBlockPos mutablePos = new MutableBlockPos();
      int placeCount = 1 + rand.nextInt(config.count());
      int minDepth = config.minDepth().resolveY(new WorldGenerationContext(context.chunkGenerator(), level));
      BlockState insideState = config.wallState().orElseGet(() -> {
         ChunkDataProvider provider = ChunkDataProvider.get(context.chunkGenerator());
         ChunkData data = provider.get(context.level(), pos);
         RockSettings rock = data.getRockData().getRock(pos.getX(), context.chunkGenerator().getMinY() + 1, pos.getZ());
         return rock.raw().defaultBlockState();
      });

      for (int i = 0; i < placeCount; i++) {
         mutablePos.setWithOffset(
            pos, rand.nextInt(config.radius()) - rand.nextInt(config.radius()), 0, rand.nextInt(config.radius()) - rand.nextInt(config.radius())
         );
         mutablePos.setY(level.getHeight(Types.OCEAN_FLOOR_WG, mutablePos.getX(), mutablePos.getZ()));
         placeFissure(
            level,
            pos,
            mutablePos.immutable(),
            mutablePos,
            rand,
            config.fluidState(),
            insideState,
            config.minPieces(),
            config.maxPieces(),
            config.maxPieceLength(),
            minDepth,
            config.radius(),
            (Decoration)config.decoration().orElse(null)
         );
      }

      return true;
   }
}
