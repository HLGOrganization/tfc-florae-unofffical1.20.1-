package tfcflorae.mixin;

import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import net.dries007.tfc.common.TFCTags.Blocks;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.soil.SoilBlockType.Variant;
import net.dries007.tfc.common.blocks.wood.ILeavesBlock;
import net.dries007.tfc.util.EnvironmentHelpers;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.chunkdata.ForestType;
import net.dries007.tfc.world.feature.tree.ForestConfig;
import net.dries007.tfc.world.feature.tree.ForestFeature;
import net.dries007.tfc.world.feature.tree.ForestConfig.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Plane;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tfcflorae.common.TFCFTags;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.blocks.plant.TFCFPlant;
import tfcflorae.common.blocks.soil.TFCFSoil;

@Mixin({ForestFeature.class})
public abstract class ForestFeatureMixin extends Feature<ForestConfig> {
   public ForestFeatureMixin(Codec<ForestConfig> codec) {
      super(codec);
   }

   @Overwrite(
      remap = false
   )
   private void placeFallenTree(WorldGenLevel level, RandomSource random, BlockPos chunkBlockPos, ForestConfig config, ChunkData data, MutableBlockPos mutablePos) {
      int chunkX = chunkBlockPos.getX();
      int chunkZ = chunkBlockPos.getZ();
      mutablePos.set(chunkX + random.nextInt(16), 0, chunkZ + random.nextInt(16));
      mutablePos.setY(level.getHeight(Types.OCEAN_FLOOR, mutablePos.getX(), mutablePos.getZ()));
      mutablePos.move(Direction.DOWN);
      BlockState downState = level.getBlockState(mutablePos);
      mutablePos.move(Direction.UP);
      if (Helpers.isBlock(downState, Blocks.BUSH_PLANTABLE_ON) || Helpers.isBlock(downState, Blocks.SEA_BUSH_PLANTABLE_ON)) {
         Entry entry = this.getTree(data, random, config, mutablePos);
         if (entry != null) {
            int fallChance = entry.fallenChance();
            if (fallChance > 0 && level.getRandom().nextInt(fallChance) == 0) {
               BlockState log = (BlockState)entry.fallenLog().orElse(null);
               if (log != null) {
                  Direction axis = Plane.HORIZONTAL.getRandomDirection(random);
                  log = Helpers.setProperty(log, TFCBlockStateProperties.NATURAL, false);
                  log = Helpers.setProperty(log, BlockStateProperties.AXIS, axis.getAxis());
                  int length = 4 + random.nextInt(10);
                  BlockPos start = mutablePos.immutable();
                  boolean[] moment = new boolean[length];
                  mutablePos.set(start);

                  int valid;
                  for (valid = 0; valid < length; valid++) {
                     BlockState replaceState = level.getBlockState(mutablePos);
                     if (!EnvironmentHelpers.isWorldgenReplaceable(replaceState) && !(replaceState.getBlock() instanceof ILeavesBlock)) {
                        break;
                     }

                     mutablePos.move(Direction.DOWN);
                     moment[valid] = level.getBlockState(mutablePos).isFaceSturdy(level, mutablePos, Direction.UP);
                     mutablePos.move(Direction.UP);
                     mutablePos.move(axis);
                  }

                  int left = 0;
                  int right = valid - 1;

                  while (left < moment.length && !moment[left]) {
                     left++;
                  }

                  while (right >= 0 && !moment[right]) {
                     right--;
                  }

                  if (left <= valid / 2 && right >= valid / 2 && valid >= 3) {
                     mutablePos.set(start);

                     for (int i = 0; i < length; i++) {
                        level.setBlock(mutablePos, log, 3);
                        MutableBlockPos mutablePosMycelium = new MutableBlockPos(mutablePos.getX(), mutablePos.getY(), mutablePos.getZ());
                        ChunkDataProvider provider = ChunkDataProvider.get(level);
                        ChunkData dataNew = provider.get(level, mutablePosMycelium);
                        float rainfall = dataNew.getRainfall(mutablePosMycelium);
                        float rainfallInverted = (500.0F - rainfall + 10.0F) * 0.1F;
                        float actualForestDensity = dataNew.getForestDensity();
                        float forestDensity = actualForestDensity == 0.0F ? 0.001F : actualForestDensity;
                        ForestType forestType = data.getForestType();
                        BlockState fungiState = Helpers.getRandomElement(ForgeRegistries.BLOCKS, TFCFTags.Blocks.FUNGI_ON_FALLEN_LOGS, random)
                           .<BlockState>map(Block::defaultBlockState)
                           .orElse(((Block)TFCFBlocks.PLANTS.get(TFCFPlant.PORCINI).get()).defaultBlockState());
                        if (random.nextFloat(1.0F - actualForestDensity) <= (float)forestType.ordinal()
                           && fungiState != null
                           && fungiState.canSurvive(level, mutablePosMycelium.above())) {
                           level.setBlock(mutablePosMycelium.above(), fungiState, 3);
                        }

                        int radius = Math.round(forestDensity * 1.5F * (float)forestType.ordinal());
                        if (radius > 0
                           && random.nextInt(Math.round(rainfallInverted * 1.2F / forestDensity * (float)(5 - forestType.ordinal()) * 0.75F))
                              <= forestType.ordinal()) {
                           int radiusSquared = radius * radius;
                           int posX = mutablePosMycelium.getX();
                           int posZ = mutablePosMycelium.getZ();
                           int posY = mutablePosMycelium.getY();

                           for (int x = posX - radius; x <= posX + radius; x++) {
                              for (int z = posZ - radius; z <= posZ + radius; z++) {
                                 int relX = x - posX;
                                 int relZ = z - posZ;
                                 if (relX * relX + relZ * relZ <= radiusSquared) {
                                    for (int y = posY - radius; y <= posY; y++) {
                                       mutablePosMycelium.set(x, y, z);
                                       if (level.getBlockState(mutablePosMycelium.above()).isAir()) {
                                          BlockState stateAt = level.getBlockState(mutablePosMycelium);
                                          if (Helpers.isBlock(stateAt, TFCFTags.Blocks.IS_BOG_IRON)) {
                                             level.setBlock(mutablePosMycelium, ((Block)TFCFBlocks.MYCELIUM_BOG_IRON.get()).defaultBlockState(), 3);
                                          } else if (Helpers.isBlock(stateAt, TFCFTags.Blocks.IS_LOAM)) {
                                             level.setBlock(
                                                mutablePosMycelium,
                                                ((Block)TFCFBlocks.TFCSOIL.get(TFCFSoil.MYCELIUM_DIRT).get(Variant.LOAM).get()).defaultBlockState(),
                                                3
                                             );
                                          } else if (Helpers.isBlock(stateAt, TFCFTags.Blocks.IS_SANDY_LOAM)) {
                                             level.setBlock(
                                                mutablePosMycelium,
                                                ((Block)TFCFBlocks.TFCSOIL.get(TFCFSoil.MYCELIUM_DIRT).get(Variant.SANDY_LOAM).get()).defaultBlockState(),
                                                3
                                             );
                                          } else if (Helpers.isBlock(stateAt, TFCFTags.Blocks.IS_SILT)) {
                                             level.setBlock(
                                                mutablePosMycelium,
                                                ((Block)TFCFBlocks.TFCSOIL.get(TFCFSoil.MYCELIUM_DIRT).get(Variant.SILT).get()).defaultBlockState(),
                                                3
                                             );
                                          } else if (Helpers.isBlock(stateAt, TFCFTags.Blocks.IS_SILTY_LOAM)) {
                                             level.setBlock(
                                                mutablePosMycelium,
                                                ((Block)TFCFBlocks.TFCSOIL.get(TFCFSoil.MYCELIUM_DIRT).get(Variant.SILTY_LOAM).get()).defaultBlockState(),
                                                3
                                             );
                                          } else if (Helpers.isBlock(stateAt, TFCFTags.Blocks.IS_HUMUS)) {
                                             level.setBlock(
                                                mutablePosMycelium,
                                                ((Block)TFCFBlocks.TFCFSOIL.get(TFCFSoil.MYCELIUM_DIRT).get(TFCFSoil.TFCFVariant.HUMUS).get()).defaultBlockState(),
                                                3
                                             );
                                          }

                                          if (random.nextFloat(1.0F - actualForestDensity) <= (float)forestType.ordinal()
                                             && fungiState != null
                                             && fungiState.canSurvive(level, mutablePosMycelium.above())) {
                                             level.setBlock(mutablePosMycelium.above(), fungiState, 3);
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }

                        mutablePos.move(axis);
                     }
                  }
               }
            }
         }
      }
   }

   @Shadow
   @Nullable
   private Entry getTree(ChunkData chunkData, RandomSource random, ForestConfig config, BlockPos pos) {
      return null;
   }
}
