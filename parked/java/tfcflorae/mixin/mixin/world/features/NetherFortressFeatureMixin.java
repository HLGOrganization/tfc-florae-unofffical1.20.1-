package tfcflorae.mixin.world.features;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.QuartPos;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraft.world.level.levelgen.feature.NetherFortressFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.NetherBridgePieces.StartPiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier.Context;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tfcflorae.Config;

@Mixin({NetherFortressFeature.class})
public class NetherFortressFeatureMixin extends StructureFeature<NoneFeatureConfiguration> {
   @Shadow
   public static final WeightedRandomList<SpawnerData> FORTRESS_ENEMIES = WeightedRandomList.create(
      new SpawnerData[]{
         new SpawnerData(EntityType.BLAZE, 10, 2, 3),
         new SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 5, 4, 4),
         new SpawnerData(EntityType.WITHER_SKELETON, 8, 5, 5),
         new SpawnerData(EntityType.SKELETON, 2, 5, 5),
         new SpawnerData(EntityType.MAGMA_CUBE, 3, 4, 4)
      }
   );

   public NetherFortressFeatureMixin(Codec<NoneFeatureConfiguration> codec) {
      super(codec, PieceGeneratorSupplier.simple(NetherFortressFeatureMixin::checkLocation, NetherFortressFeatureMixin::generatePieces));
   }

   @Shadow
   private static boolean checkLocation(Context<NoneFeatureConfiguration> context) {
      return context.validBiome()
         .test(
            context.chunkGenerator()
               .getNoiseBiome(QuartPos.fromBlock(context.chunkPos().getMiddleBlockX()), QuartPos.fromBlock(64), QuartPos.fromBlock(context.chunkPos().getMiddleBlockZ()))
         );
   }

   @Overwrite(
      remap = true
   )
   private static void generatePieces(
      StructurePiecesBuilder builder, net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator.Context<NoneFeatureConfiguration> context
   ) {
      StartPiece netherbridgepieces$startpiece = new StartPiece(context.random(), context.chunkPos().getBlockX(2), context.chunkPos().getBlockZ(2));
      builder.addPiece(netherbridgepieces$startpiece);
      netherbridgepieces$startpiece.addChildren(netherbridgepieces$startpiece, builder, context.random());
      List<StructurePiece> list = netherbridgepieces$startpiece.pendingChildren;

      while (!list.isEmpty()) {
         int i = context.random().nextInt(list.size());
         StructurePiece structurepiece = list.remove(i);
         structurepiece.addChildren(netherbridgepieces$startpiece, builder, context.random());
      }

      if (context.chunkGenerator().getMinY() < -64) {
         builder.moveInsideHeights(context.random(), (Integer)Config.COMMON.minFortressHeight.get(), (Integer)Config.COMMON.maxFortressHeight.get());
      } else {
         builder.moveInsideHeights(context.random(), 48, 70);
      }
   }
}
