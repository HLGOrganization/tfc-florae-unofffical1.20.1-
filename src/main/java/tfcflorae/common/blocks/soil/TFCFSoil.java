package tfcflorae.common.blocks.soil;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.soil.ConnectedGrassBlock;
import net.dries007.tfc.common.blocks.soil.DirtBlock;
import net.dries007.tfc.common.blocks.soil.PathBlock;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.common.blocks.soil.TFCRootedDirtBlock;
import net.dries007.tfc.common.blocks.soil.SoilBlockType.Variant;
import net.dries007.tfc.util.registry.RegistrySoilVariant;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import tfcflorae.client.TFCFSounds;
import tfcflorae.common.blockentities.TFCFBlockEntities;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.blocks.devices.TFCFDryingBricksBlock;
import tfcflorae.common.items.TFCFItems;

public enum TFCFSoil {
   GRASS_PATH(
      (self, variant) -> new PathBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(0.65F).sound(SoundType.GRASS),
            (Supplier)TFCFBlocks.TFCFSOIL.get(self.transform()).get(variant)
         )
   ),
   FARMLAND(
      (self, variant) -> new net.dries007.tfc.common.blocks.soil.FarmlandBlock(
            ExtendedProperties.of(
                  Properties.of().mapColor(MapColor.DIRT).strength(0.6F).sound(SoundType.GRAVEL).isViewBlocking(TFCBlocks::always).isSuffocating(TFCBlocks::always)
               )
               .blockEntity(net.dries007.tfc.common.blockentities.TFCBlockEntities.FARMLAND),
            (Supplier<? extends Block>)TFCFBlocks.TFCFSOIL.get(self.transform()).get(variant)
         )
   ),
   ROOTED_DIRT(
      (self, variant) -> new TFCRootedDirtBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(0.5F).sound(SoundType.ROOTED_DIRT),
            (Supplier)TFCFBlocks.TFCFSOIL.get(self.transform()).get(variant),
            null
         )
   ),
   DIRT(
      (self, variant) -> new DirtBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(0.5F).sound(SoundType.GRAVEL),
            (Supplier)TFCFBlocks.TFCFSOIL.get(self.transform()).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(GRASS_PATH).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(FARMLAND).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(ROOTED_DIRT).get(variant),
            null
         )
   ),
   GRASS(
      (self, variant) -> new ConnectedGrassBlock(
            Properties.of().mapColor(MapColor.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS),
            (Supplier)TFCFBlocks.TFCFSOIL.get(self.transform()).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(GRASS_PATH).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(FARMLAND).get(variant)
         )
   ),
   CLAY(
      (self, variant) -> new DirtBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(0.5F).sound(SoundType.GRAVEL),
            (Supplier)TFCFBlocks.TFCFSOIL.get(self.transform()).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(GRASS_PATH).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(self.transform()).get(variant),
            null,
            null
         )
   ),
   CLAY_GRASS(
      (self, variant) -> new ConnectedGrassBlock(
            Properties.of().mapColor(MapColor.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS),
            (Supplier)TFCFBlocks.TFCFSOIL.get(self.transform()).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(GRASS_PATH).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(FARMLAND).get(variant)
         )
   ),
   PACKED_MUD(
      (self, variant) -> new PackedMudBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(1.4F, 3.0F).sound(TFCFSounds.PACKED_MUD),
            (Supplier<? extends Block>)TFCFBlocks.TFCFSOIL.get(self.transform()).get(variant)
         ),
      (self, variant) -> new PackedMudBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(1.4F, 3.0F).sound(TFCFSounds.PACKED_MUD),
            (Supplier<? extends Block>)((Map)TFCBlocks.SOIL.get(SoilBlockType.MUD)).get(variant)
         )
   ),
   MOSSY_PACKED_MUD(
      (self, variant) -> new PackedMudBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(1.4F, 3.0F).sound(TFCFSounds.PACKED_MUD),
            (Supplier<? extends Block>)TFCFBlocks.TFCFSOIL.get(self.transform()).get(variant)
         ),
      (self, variant) -> new PackedMudBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(1.4F, 3.0F).sound(TFCFSounds.PACKED_MUD),
            (Supplier<? extends Block>)((Map)TFCBlocks.SOIL.get(SoilBlockType.MUD)).get(variant)
         )
   ),
   LOOSE_MUD(
      (self, variant) -> new LooseMudBlock(Properties.of().mapColor(MapColor.DIRT).strength(0.3F, 0.3F).sound(TFCFSounds.MUD)),
      (self, variant) -> new LooseMudBlock(Properties.of().mapColor(MapColor.DIRT).strength(0.3F, 0.3F).sound(TFCFSounds.MUD))
   ),
   MYCELIUM_DIRT(
      (self, variant) -> new TFCRootedDirtBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(0.5F).sound(SoundType.ROOTED_DIRT),
            (Supplier)TFCFBlocks.TFCFSOIL.get(self.transform()).get(variant),
            null
         ),
      (self, variant) -> new TFCRootedDirtBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(0.5F).sound(SoundType.ROOTED_DIRT),
            (Supplier)((Map)TFCBlocks.SOIL.get(SoilBlockType.DIRT)).get(variant),
            null
         )
   ),
   MUD_BRICKS((self, variant) -> new Block(mudBrickProperties())),
   DRYING_BRICKS(
      (self, variant) -> new net.dries007.tfc.common.blocks.devices.DryingBricksBlock(
            ExtendedProperties.of(Properties.of().mapColor(MapColor.PLANT).noCollission().noOcclusion().instabreak().sound(TFCFSounds.MUD_BRICKS).randomTicks())
               .blockEntity(net.dries007.tfc.common.blockentities.TFCBlockEntities.TICK_COUNTER),
            variant.getDriedMudBrick()
         )
   ),
   COARSE_DIRT(
      (self, variant) -> new CompactDirtBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(0.5F).sound(SoundType.GRAVEL),
            (Supplier<? extends Block>)TFCFBlocks.TFCFSOIL.get(DIRT).get(variant)
         ),
      (self, variant) -> new CompactDirtBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(0.5F).sound(SoundType.GRAVEL),
            (Supplier<? extends Block>)((Map)TFCBlocks.SOIL.get(SoilBlockType.DIRT)).get(variant)
         )
   ),
   PODZOL(
      (self, variant) -> new PodzolBlock(
            Properties.of().mapColor(MapColor.PODZOL).randomTicks().strength(0.6F).sound(SoundType.GRAVEL),
            (Supplier<? extends Block>)TFCFBlocks.TFCFSOIL.get(self.transform()).get(variant),
            (Supplier<? extends Block>)TFCFBlocks.TFCFSOIL.get(GRASS_PATH).get(variant),
            (Supplier<? extends Block>)TFCFBlocks.TFCFSOIL.get(FARMLAND).get(variant)
         ),
      (self, variant) -> new PodzolBlock(
            Properties.of().mapColor(MapColor.PODZOL).randomTicks().strength(0.6F).sound(SoundType.GRAVEL),
            self.findSoil(self.transform(), SoilBlockType.DIRT, variant),
            (Supplier<? extends Block>)((Map)TFCBlocks.SOIL.get(SoilBlockType.GRASS_PATH)).get(variant),
            (Supplier<? extends Block>)((Map)TFCBlocks.SOIL.get(SoilBlockType.FARMLAND)).get(variant)
         )
   ),
   DRY_GRASS_PATH(
      (self, variant) -> new PathBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(0.65F).sound(SoundType.GRASS),
            (Supplier)TFCFBlocks.TFCFSOIL.get(self.transform()).get(variant)
         ),
      (self, variant) -> new PathBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(0.65F).sound(SoundType.GRASS),
            (Supplier)((Map)TFCBlocks.SOIL.get(SoilBlockType.DIRT)).get(variant)
         )
   ),
   DRY_GRASS(
      (self, variant) -> new ConnectedGrassBlock(
            Properties.of().mapColor(MapColor.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS),
            (Supplier)TFCFBlocks.TFCFSOIL.get(self.transform()).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(DRY_GRASS_PATH).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(FARMLAND).get(variant)
         ),
      (self, variant) -> new ConnectedGrassBlock(
            Properties.of().mapColor(MapColor.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS),
            (Supplier)((Map)TFCBlocks.SOIL.get(SoilBlockType.DIRT)).get(variant),
            (Supplier)TFCFBlocks.TFCSOIL.get(DRY_GRASS_PATH).get(variant),
            (Supplier)((Map)TFCBlocks.SOIL.get(SoilBlockType.FARMLAND)).get(variant)
         )
   ),
   DENSE_GRASS_PATH(
      (self, variant) -> new PathBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(0.65F).sound(SoundType.GRASS),
            (Supplier)TFCFBlocks.TFCFSOIL.get(self.transform()).get(variant)
         ),
      (self, variant) -> new PathBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(0.65F).sound(SoundType.GRASS),
            (Supplier)((Map)TFCBlocks.SOIL.get(SoilBlockType.DIRT)).get(variant)
         )
   ),
   DENSE_GRASS(
      (self, variant) -> new ConnectedGrassBlock(
            Properties.of().mapColor(MapColor.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS),
            (Supplier)TFCFBlocks.TFCFSOIL.get(self.transform()).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(DENSE_GRASS_PATH).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(FARMLAND).get(variant)
         ),
      (self, variant) -> new ConnectedGrassBlock(
            Properties.of().mapColor(MapColor.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS),
            (Supplier)((Map)TFCBlocks.SOIL.get(SoilBlockType.DIRT)).get(variant),
            (Supplier)TFCFBlocks.TFCSOIL.get(DENSE_GRASS_PATH).get(variant),
            (Supplier)((Map)TFCBlocks.SOIL.get(SoilBlockType.FARMLAND)).get(variant)
         )
   ),
   SPARSE_GRASS_PATH(
      (self, variant) -> new PathBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(0.65F).sound(SoundType.GRASS),
            (Supplier)TFCFBlocks.TFCFSOIL.get(self.transform()).get(variant)
         ),
      (self, variant) -> new PathBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(0.65F).sound(SoundType.GRASS),
            (Supplier)((Map)TFCBlocks.SOIL.get(SoilBlockType.DIRT)).get(variant)
         )
   ),
   SPARSE_GRASS(
      (self, variant) -> new ConnectedGrassBlock(
            Properties.of().mapColor(MapColor.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS),
            (Supplier)TFCFBlocks.TFCFSOIL.get(self.transform()).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(SPARSE_GRASS_PATH).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(FARMLAND).get(variant)
         ),
      (self, variant) -> new ConnectedGrassBlock(
            Properties.of().mapColor(MapColor.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS),
            (Supplier)((Map)TFCBlocks.SOIL.get(SoilBlockType.DIRT)).get(variant),
            (Supplier)TFCFBlocks.TFCSOIL.get(SPARSE_GRASS_PATH).get(variant),
            (Supplier)((Map)TFCBlocks.SOIL.get(SoilBlockType.FARMLAND)).get(variant)
         )
   ),
   DRY_CLAY_GRASS(
      (self, variant) -> new ConnectedGrassBlock(
            Properties.of().mapColor(MapColor.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS),
            (Supplier)TFCFBlocks.TFCFSOIL.get(self.transform()).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(DRY_GRASS_PATH).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(FARMLAND).get(variant)
         ),
      (self, variant) -> new ConnectedGrassBlock(
            Properties.of().mapColor(MapColor.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS),
            (Supplier)((Map)TFCBlocks.SOIL.get(SoilBlockType.CLAY)).get(variant),
            (Supplier)TFCFBlocks.TFCSOIL.get(DRY_GRASS_PATH).get(variant),
            (Supplier)((Map)TFCBlocks.SOIL.get(SoilBlockType.FARMLAND)).get(variant)
         )
   ),
   DENSE_CLAY_GRASS(
      (self, variant) -> new ConnectedGrassBlock(
            Properties.of().mapColor(MapColor.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS),
            (Supplier)TFCFBlocks.TFCFSOIL.get(self.transform()).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(DENSE_GRASS_PATH).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(FARMLAND).get(variant)
         ),
      (self, variant) -> new ConnectedGrassBlock(
            Properties.of().mapColor(MapColor.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS),
            (Supplier)((Map)TFCBlocks.SOIL.get(SoilBlockType.CLAY)).get(variant),
            (Supplier)TFCFBlocks.TFCSOIL.get(DENSE_GRASS_PATH).get(variant),
            (Supplier)((Map)TFCBlocks.SOIL.get(SoilBlockType.FARMLAND)).get(variant)
         )
   ),
   SPARSE_CLAY_GRASS(
      (self, variant) -> new ConnectedGrassBlock(
            Properties.of().mapColor(MapColor.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS),
            (Supplier)TFCFBlocks.TFCFSOIL.get(self.transform()).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(SPARSE_GRASS_PATH).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(FARMLAND).get(variant)
         ),
      (self, variant) -> new ConnectedGrassBlock(
            Properties.of().mapColor(MapColor.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS),
            (Supplier)((Map)TFCBlocks.SOIL.get(SoilBlockType.CLAY)).get(variant),
            (Supplier)TFCFBlocks.TFCSOIL.get(SPARSE_GRASS_PATH).get(variant),
            (Supplier)((Map)TFCBlocks.SOIL.get(SoilBlockType.FARMLAND)).get(variant)
         )
   ),
   EARTHENWARE_CLAY(
      (self, variant) -> new DirtBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(0.5F).sound(SoundType.GRAVEL),
            (Supplier)TFCFBlocks.TFCFSOIL.get(self.transform()).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(GRASS_PATH).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(FARMLAND).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(ROOTED_DIRT).get(variant),
            null
         ),
      (self, variant) -> new DirtBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(0.5F).sound(SoundType.GRAVEL),
            (Supplier)TFCFBlocks.TFCSOIL.get(self.transform()).get(variant),
            (Supplier)((Map)TFCBlocks.SOIL.get(SoilBlockType.GRASS_PATH)).get(variant),
            (Supplier)((Map)TFCBlocks.SOIL.get(SoilBlockType.FARMLAND)).get(variant),
            (Supplier)((Map)TFCBlocks.SOIL.get(SoilBlockType.ROOTED_DIRT)).get(variant),
            null
         )
   ),
   EARTHENWARE_CLAY_GRASS(
      (self, variant) -> new ConnectedGrassBlock(
            Properties.of().mapColor(MapColor.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS),
            (Supplier)TFCFBlocks.TFCFSOIL.get(self.transform()).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(GRASS_PATH).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(FARMLAND).get(variant)
         ),
      (self, variant) -> new ConnectedGrassBlock(
            Properties.of().mapColor(MapColor.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS),
            (Supplier)TFCFBlocks.TFCSOIL.get(self.transform()).get(variant),
            (Supplier)((Map)TFCBlocks.SOIL.get(SoilBlockType.GRASS_PATH)).get(variant),
            (Supplier)((Map)TFCBlocks.SOIL.get(SoilBlockType.FARMLAND)).get(variant)
         )
   ),
   DRY_EARTHENWARE_CLAY_GRASS(
      (self, variant) -> new ConnectedGrassBlock(
            Properties.of().mapColor(MapColor.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS),
            (Supplier)TFCFBlocks.TFCFSOIL.get(self.transform()).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(DRY_GRASS_PATH).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(FARMLAND).get(variant)
         ),
      (self, variant) -> new ConnectedGrassBlock(
            Properties.of().mapColor(MapColor.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS),
            (Supplier)TFCFBlocks.TFCSOIL.get(self.transform()).get(variant),
            (Supplier)TFCFBlocks.TFCSOIL.get(DRY_GRASS_PATH).get(variant),
            (Supplier)((Map)TFCBlocks.SOIL.get(SoilBlockType.FARMLAND)).get(variant)
         )
   ),
   DENSE_EARTHENWARE_CLAY_GRASS(
      (self, variant) -> new ConnectedGrassBlock(
            Properties.of().mapColor(MapColor.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS),
            (Supplier)TFCFBlocks.TFCFSOIL.get(self.transform()).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(DENSE_GRASS_PATH).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(FARMLAND).get(variant)
         ),
      (self, variant) -> new ConnectedGrassBlock(
            Properties.of().mapColor(MapColor.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS),
            (Supplier)TFCFBlocks.TFCSOIL.get(self.transform()).get(variant),
            (Supplier)TFCFBlocks.TFCSOIL.get(DENSE_GRASS_PATH).get(variant),
            (Supplier)((Map)TFCBlocks.SOIL.get(SoilBlockType.FARMLAND)).get(variant)
         )
   ),
   SPARSE_EARTHENWARE_CLAY_GRASS(
      (self, variant) -> new ConnectedGrassBlock(
            Properties.of().mapColor(MapColor.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS),
            (Supplier)TFCFBlocks.TFCFSOIL.get(self.transform()).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(SPARSE_GRASS_PATH).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(FARMLAND).get(variant)
         ),
      (self, variant) -> new ConnectedGrassBlock(
            Properties.of().mapColor(MapColor.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS),
            (Supplier)TFCFBlocks.TFCSOIL.get(self.transform()).get(variant),
            (Supplier)TFCFBlocks.TFCSOIL.get(SPARSE_GRASS_PATH).get(variant),
            (Supplier)((Map)TFCBlocks.SOIL.get(SoilBlockType.FARMLAND)).get(variant)
         )
   ),
   STONEWARE_CLAY(
      (self, variant) -> new DirtBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(0.5F).sound(SoundType.GRAVEL),
            (Supplier)TFCFBlocks.TFCFSOIL.get(self.transform()).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(GRASS_PATH).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(FARMLAND).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(ROOTED_DIRT).get(variant),
            null
         ),
      (self, variant) -> new DirtBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(0.5F).sound(SoundType.GRAVEL),
            (Supplier)TFCFBlocks.TFCSOIL.get(self.transform()).get(variant),
            (Supplier)((Map)TFCBlocks.SOIL.get(SoilBlockType.GRASS_PATH)).get(variant),
            (Supplier)((Map)TFCBlocks.SOIL.get(SoilBlockType.FARMLAND)).get(variant),
            (Supplier)((Map)TFCBlocks.SOIL.get(SoilBlockType.ROOTED_DIRT)).get(variant),
            null
         )
   ),
   STONEWARE_CLAY_GRASS(
      (self, variant) -> new ConnectedGrassBlock(
            Properties.of().mapColor(MapColor.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS),
            (Supplier)TFCFBlocks.TFCFSOIL.get(self.transform()).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(GRASS_PATH).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(FARMLAND).get(variant)
         ),
      (self, variant) -> new ConnectedGrassBlock(
            Properties.of().mapColor(MapColor.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS),
            (Supplier)TFCFBlocks.TFCSOIL.get(self.transform()).get(variant),
            (Supplier)((Map)TFCBlocks.SOIL.get(SoilBlockType.GRASS_PATH)).get(variant),
            (Supplier)((Map)TFCBlocks.SOIL.get(SoilBlockType.FARMLAND)).get(variant)
         )
   ),
   DRY_STONEWARE_CLAY_GRASS(
      (self, variant) -> new ConnectedGrassBlock(
            Properties.of().mapColor(MapColor.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS),
            (Supplier)TFCFBlocks.TFCFSOIL.get(self.transform()).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(DRY_GRASS_PATH).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(FARMLAND).get(variant)
         ),
      (self, variant) -> new ConnectedGrassBlock(
            Properties.of().mapColor(MapColor.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS),
            (Supplier)TFCFBlocks.TFCSOIL.get(self.transform()).get(variant),
            (Supplier)TFCFBlocks.TFCSOIL.get(DRY_GRASS_PATH).get(variant),
            (Supplier)((Map)TFCBlocks.SOIL.get(SoilBlockType.FARMLAND)).get(variant)
         )
   ),
   DENSE_STONEWARE_CLAY_GRASS(
      (self, variant) -> new ConnectedGrassBlock(
            Properties.of().mapColor(MapColor.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS),
            (Supplier)TFCFBlocks.TFCFSOIL.get(self.transform()).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(DENSE_GRASS_PATH).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(FARMLAND).get(variant)
         ),
      (self, variant) -> new ConnectedGrassBlock(
            Properties.of().mapColor(MapColor.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS),
            (Supplier)TFCFBlocks.TFCSOIL.get(self.transform()).get(variant),
            (Supplier)TFCFBlocks.TFCSOIL.get(DENSE_GRASS_PATH).get(variant),
            (Supplier)((Map)TFCBlocks.SOIL.get(SoilBlockType.FARMLAND)).get(variant)
         )
   ),
   SPARSE_STONEWARE_CLAY_GRASS(
      (self, variant) -> new ConnectedGrassBlock(
            Properties.of().mapColor(MapColor.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS),
            (Supplier)TFCFBlocks.TFCFSOIL.get(self.transform()).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(SPARSE_GRASS_PATH).get(variant),
            (Supplier)TFCFBlocks.TFCFSOIL.get(FARMLAND).get(variant)
         ),
      (self, variant) -> new ConnectedGrassBlock(
            Properties.of().mapColor(MapColor.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS),
            (Supplier)TFCFBlocks.TFCSOIL.get(self.transform()).get(variant),
            (Supplier)TFCFBlocks.TFCSOIL.get(SPARSE_GRASS_PATH).get(variant),
            (Supplier)((Map)TFCBlocks.SOIL.get(SoilBlockType.FARMLAND)).get(variant)
         )
   ),
   COMPACT_DIRT(
      (self, variant) -> new CompactDirtBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(0.75F).sound(TFCFSounds.MUD).requiresCorrectToolForDrops(),
            (Supplier<? extends Block>)TFCFBlocks.TFCFSOIL.get(self.transform()).get(variant)
         ),
      (self, variant) -> new CompactDirtBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(0.75F).sound(TFCFSounds.MUD).requiresCorrectToolForDrops(),
            (Supplier<? extends Block>)((Map)TFCBlocks.SOIL.get(SoilBlockType.DIRT)).get(variant)
         )
   );

   public static final TFCFSoil[] VALUES = values();
   private final BiFunction<TFCFSoil, TFCFSoil.TFCFVariant, Block> TFCFFactory;
   private final BiFunction<TFCFSoil, Variant, Block> TFCFactory;

   public static TFCFSoil valueOf(int i) {
      return i >= 0 && i < VALUES.length ? VALUES[i] : COARSE_DIRT;
   }

   public static Properties mudProperties() {
      return Properties.of().mapColor(MapColor.DIRT).strength(1.0F).sound(TFCFSounds.MUD);
   }

   public static Properties mudBrickProperties() {
      return Properties.of().mapColor(MapColor.DIRT).strength(1.0F).sound(TFCFSounds.MUD_BRICKS);
   }

   private TFCFSoil(BiFunction<TFCFSoil, TFCFSoil.TFCFVariant, Block> TFCFFactory, BiFunction<TFCFSoil, Variant, Block> TFCFactory) {
      this.TFCFFactory = TFCFFactory;
      this.TFCFactory = TFCFactory;
   }

   private TFCFSoil(BiFunction<TFCFSoil, TFCFSoil.TFCFVariant, Block> TFCFFactory) {
      this.TFCFFactory = TFCFFactory;
      this.TFCFactory = null;
   }

   public Block TFCFCreate(TFCFSoil.TFCFVariant variant) {
      return this.TFCFFactory.apply(this, variant);
   }

   public Block TFCCreate(Variant variant) {
      return this.TFCFactory.apply(this, variant);
   }

   private Supplier<Block> findSoil(TFCFSoil soil, SoilBlockType altSoil, Variant variant) {
      return TFCFBlocks.TFCSOIL.get(soil) != null
         ? (Supplier)TFCFBlocks.TFCSOIL.get(soil).get(variant)
         : (Supplier)((Map)TFCBlocks.SOIL.get(altSoil)).get(variant);
   }

   public BiFunction<TFCFSoil, Variant, Block> getTFCFactory() {
      return this.TFCFactory;
   }

   public TFCFSoil transform() {
      switch (this) {
         case PACKED_MUD:
            return DIRT;
         case DIRT:
            return GRASS;
         case FARMLAND:
         case ROOTED_DIRT:
         case MUD_BRICKS:
         case DRYING_BRICKS:
         case COMPACT_DIRT:
         case COARSE_DIRT:
         case MYCELIUM_DIRT:
         case PODZOL:
         case SPARSE_GRASS:
         case SPARSE_GRASS_PATH:
         case DRY_GRASS:
         case DRY_GRASS_PATH:
            return DIRT;
         case GRASS:
            return DENSE_GRASS;
         case GRASS_PATH:
            return DENSE_GRASS_PATH;
         case DENSE_GRASS:
            return SPARSE_GRASS;
         case DENSE_GRASS_PATH:
            return SPARSE_GRASS_PATH;
         case CLAY:
            return CLAY_GRASS;
         case CLAY_GRASS:
            return DENSE_CLAY_GRASS;
         case DENSE_CLAY_GRASS:
            return SPARSE_CLAY_GRASS;
         case DRY_CLAY_GRASS:
         case SPARSE_CLAY_GRASS:
            return CLAY;
         case EARTHENWARE_CLAY:
            return EARTHENWARE_CLAY_GRASS;
         case EARTHENWARE_CLAY_GRASS:
            return DENSE_EARTHENWARE_CLAY_GRASS;
         case DENSE_EARTHENWARE_CLAY_GRASS:
            return SPARSE_EARTHENWARE_CLAY_GRASS;
         case SPARSE_EARTHENWARE_CLAY_GRASS:
         case DRY_EARTHENWARE_CLAY_GRASS:
            return EARTHENWARE_CLAY;
         case STONEWARE_CLAY:
            return STONEWARE_CLAY_GRASS;
         case STONEWARE_CLAY_GRASS:
            return DENSE_STONEWARE_CLAY_GRASS;
         case DENSE_STONEWARE_CLAY_GRASS:
            return SPARSE_STONEWARE_CLAY_GRASS;
         case SPARSE_STONEWARE_CLAY_GRASS:
         case DRY_STONEWARE_CLAY_GRASS:
            return STONEWARE_CLAY;
         default:
            return this;
      }
   }

   public static enum TFCFVariant implements RegistrySoilVariant {
      // TFE 新增的土壤类型（原版 TFC 只有 silt/loam/sandy_loam/silty_loam）
      FLUVISOL,
      MOLLISOL,
      OXISOL,
      PODZOL
      ;

      /** TFE 新增、只有多石土一族补齐了资源，其余土壤类型不应注册这些变体。 */
      public boolean isTfeOnly()
      {
         return this == FLUVISOL || this == MOLLISOL || this == OXISOL || this == PODZOL;
      }

      public static final TFCFSoil.TFCFVariant[] VALUES = values();

      private TFCFVariant() {
      }

      public static TFCFSoil.TFCFVariant valueOf(int i) {
         return i >= 0 && i < VALUES.length ? VALUES[i] : null;
      }

      public Supplier<? extends Item> getDriedMudBrick() {
         return null;
      }

      public Supplier<? extends Block> getBlock(SoilBlockType type) {
         return null;
      }
   }
}
