package tfcflorae.util.registry;

import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.rock.RockCategory;
import net.dries007.tfc.util.registry.RegistryRock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.material.MapColor;
import tfcflorae.common.blocks.rock.TFCFRock;

public interface TFCFRegistryRock extends RegistryRock {
   RockCategory category();

   default MapColor color() {
      return MapColor.STONE;
   }

   Supplier<? extends Block> getBlockTFC(RegistryRock var1, TFCFRock.TFCFBlockType var2);

   Supplier<? extends SlabBlock> getSlabTFC(RegistryRock var1, TFCFRock.TFCFBlockType var2);

   Supplier<? extends StairBlock> getStairTFC(RegistryRock var1, TFCFRock.TFCFBlockType var2);

   Supplier<? extends WallBlock> getWallTFC(RegistryRock var1, TFCFRock.TFCFBlockType var2);

   Supplier<? extends Block> getBlockTFCF(TFCFRock.TFCFBlockType var1);

   Supplier<? extends SlabBlock> getSlabTFCF(TFCFRock.TFCFBlockType var1);

   Supplier<? extends StairBlock> getStairTFCF(TFCFRock.TFCFBlockType var1);

   Supplier<? extends WallBlock> getWallTFCF(TFCFRock.TFCFBlockType var1);
}
