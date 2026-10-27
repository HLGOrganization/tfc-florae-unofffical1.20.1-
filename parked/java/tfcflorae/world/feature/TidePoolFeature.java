package tfcflorae.world.feature;
import net.minecraft.core.registries.Registries;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import net.minecraft.util.RandomSource;
import java.util.function.Predicate;
import net.dries007.tfc.common.blocks.GroundcoverBlockType;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.settings.RockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tfcflorae.common.TFCFTags;
import tfcflorae.common.blocks.soil.SandLayerBlock;

public class TidePoolFeature extends Feature<NoneFeatureConfiguration> {
   public TidePoolFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
      boolean placedAny = false;
      RandomSource random = context.random();
      WorldGenLevel level = context.level();
      BlockPos origin = context.origin();
      if (level.getBlockState(origin).getBlock() instanceof SandLayerBlock) {
         origin = origin.below();
      }

      if (origin.getY() <= 67 && origin.getY() >= 62) {
         boolean hasRim = random.nextDouble() < 0.9;
         int rimOffsetX = hasRim ? Mth.nextInt(random, 0, 2) : 0;
         int rimOffsetZ = hasRim ? Mth.nextInt(random, 0, 2) : 0;
         boolean willPlaceRim = hasRim && rimOffsetX != 0 && rimOffsetZ != 0;
         int xSize = Mth.nextInt(random, 3, 7);
         int zSize = Mth.nextInt(random, 3, 7);
         int maxLength = Math.max(xSize, zSize);
         ChunkDataProvider provider = ChunkDataProvider.get(context.chunkGenerator());
         RockSettings rock = provider.get(context.level(), origin).getRockData().getRock(origin);
         BlockState cobble = rock.cobble().defaultBlockState();
         BlockState raw = rock.raw().defaultBlockState();
         BlockState water = ((LiquidBlock)TFCBlocks.SALT_WATER.get()).defaultBlockState();
         Predicate<BlockState> test = state -> state.getBlock() == cobble.getBlock() || !state.getFluidState().isEmpty();

         for (BlockPos pos : BlockPos.withinManhattan(origin, xSize, 0, zSize)) {
            if (pos.distManhattan(origin) > maxLength) {
               break;
            }

            if (isClear(level, pos, test)) {
               if (willPlaceRim || hasSandLayerAround(level, pos)) {
                  placedAny = true;
                  this.setBlock(level, pos, raw);
                  if (random.nextFloat() < 0.01F && level.isStateAtPosition(pos.above(), net.minecraft.world.level.block.state.BlockState::isAir)) {
                     this.setBlock(level, pos.above(), ((Block)((RegistryObject)TFCBlocks.GROUNDCOVER.get(GroundcoverBlockType.GUANO)).get()).defaultBlockState());
                  }
               }

               BlockPos offsetPos = pos.offset(rimOffsetX, 0, rimOffsetZ);
               if (isClear(level, offsetPos, test)) {
                  placedAny = true;
                  BlockState toPlace = cobble;
                  if (random.nextBoolean()) {
                     toPlace = water;
                     if (random.nextFloat() < 0.15F) {
                        BlockState groundcover = Helpers.getRandomElement(ForgeRegistries.BLOCKS, TFCFTags.Blocks.TIDE_POOL_BLOCKS, random)
                           .<BlockState>map(Block::defaultBlockState)
                           .orElse(water);
                        groundcover = FluidHelpers.fillWithFluid(groundcover, TFCFluids.SALT_WATER.getSource());
                        if (groundcover != null) {
                           toPlace = groundcover;
                        }
                     }
                  }

                  this.setBlock(level, offsetPos, toPlace);

                  for (Direction direction : Plane.HORIZONTAL) {
                     if (level.getBlockState(offsetPos.relative(direction)).getBlock() instanceof SandLayerBlock
                        && pos.distManhattan(origin) >= maxLength - random.nextInt(2)) {
                        this.setBlock(level, offsetPos.relative(direction), cobble);
                     }
                  }
               }
            }
         }

         return placedAny;
      } else {
         return false;
      }
   }

   public static boolean isClear(LevelAccessor level, BlockPos pos, Predicate<BlockState> contentsTest) {
      if (contentsTest.test(level.getBlockState(pos))) {
         return false;
      } else {
         for (Direction direction : Helpers.DIRECTIONS) {
            boolean airAbove = level.isStateAtPosition(pos.relative(direction), net.minecraft.world.level.block.state.BlockState::isAir);
            if (airAbove && direction != Direction.UP || !airAbove && direction == Direction.UP) {
               return false;
            }
         }

         return true;
      }
   }

   public static boolean hasSandLayerAround(LevelAccessor level, BlockPos pos) {
      Iterator var2 = Plane.HORIZONTAL.iterator();
      if (var2.hasNext()) {
         Direction direction = (Direction)var2.next();
         return level.getBlockState(pos.relative(direction)).getBlock() instanceof SandLayerBlock;
      } else {
         return false;
      }
   }
}
