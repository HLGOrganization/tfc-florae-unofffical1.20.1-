package tfcflorae.common.blocks.soil;

import java.awt.Color;
import java.util.Locale;
import net.dries007.tfc.common.blocks.soil.SandBlockType;
import net.dries007.tfc.common.blocks.soil.TFCSandBlock;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;

public enum Colors implements StringRepresentable {
   BLACK(new Color(56, 56, 56).getRGB(), MapColor.TERRACOTTA_BLACK, true, false, true),
   BROWN(new Color(112, 113, 89).getRGB(), MapColor.TERRACOTTA_BROWN, true, false, true),
   GREEN(new Color(106, 116, 81).getRGB(), MapColor.TERRACOTTA_GREEN, true, false, true),
   PINK(new Color(150, 101, 97).getRGB(), MapColor.TERRACOTTA_PINK, true, false, true),
   RED(new Color(125, 99, 84).getRGB(), MapColor.TERRACOTTA_RED, true, false, true),
   WHITE(new Color(202, 202, 201).getRGB(), MapColor.TERRACOTTA_WHITE, true, false, true),
   YELLOW(new Color(215, 196, 140).getRGB(), MapColor.TERRACOTTA_YELLOW, true, false, true);

   public static final Colors[] VALUES = values();
   public final int dustColor;
   public final MapColor materialColor;
   public final boolean hasSand;
   public final boolean hasSandNew;
   public final boolean hasLayered;
   private final String serializedName;

   public static Colors valueOf(int i) {
      return i >= 0 && i < VALUES.length ? VALUES[i] : BLACK;
   }

   private Colors(int dustColor, MapColor materialColor, boolean hasSand, boolean hasSandNew, boolean hasLayered) {
      this.dustColor = dustColor;
      this.materialColor = materialColor;
      this.hasSand = hasSand;
      this.hasSandNew = hasSandNew;
      this.hasLayered = hasLayered;
      this.serializedName = this.name().toLowerCase(Locale.ROOT);
   }

   @Override
   public String toString() {
      return this.serializedName;
   }

   public String getSerializedName() {
      return this.serializedName;
   }

   public int getDustColor() {
      return this.dustColor;
   }

   public MapColor getMaterialColor() {
      return this.materialColor;
   }

   public Block create() {
      return new TFCSandBlock(this.getDustColor(), Properties.of().mapColor(this.getMaterialColor()).strength(1.0F).sound(SoundType.ANCIENT_DEBRIS));
   }

   public boolean hasSandTFC() {
      return this.hasSand;
   }

   public boolean hasSandNew() {
      return this.hasSandNew;
   }

   public boolean hasLayered() {
      return this.hasLayered;
   }

   public static Colors fromMaterialColour(MapColor materialColor) {
      if (materialColor == MapColor.GRASS
         || materialColor == MapColor.PLANT
         || materialColor == MapColor.COLOR_GREEN
         || materialColor == MapColor.EMERALD
         || materialColor == MapColor.TERRACOTTA_GREEN) {
         return GREEN;
      } else if (materialColor == MapColor.SAND
         || materialColor == MapColor.COLOR_YELLOW
         || materialColor == MapColor.GOLD
         || materialColor == MapColor.TERRACOTTA_YELLOW
         || materialColor == MapColor.RAW_IRON) {
         return YELLOW;
      } else if (false
         || materialColor == MapColor.WOOL
         || materialColor == MapColor.SNOW
         || materialColor == MapColor.QUARTZ
         || materialColor == MapColor.TERRACOTTA_WHITE) {
         return WHITE;
      } else if (materialColor == MapColor.FIRE || materialColor == MapColor.COLOR_ORANGE || materialColor == MapColor.TERRACOTTA_ORANGE) {
         return YELLOW;
      } else if (materialColor == MapColor.ICE
         || materialColor == MapColor.CLAY
         || materialColor == MapColor.COLOR_LIGHT_BLUE
         || materialColor == MapColor.TERRACOTTA_LIGHT_BLUE) {
         return WHITE;
      } else if (materialColor == MapColor.METAL || materialColor == MapColor.COLOR_LIGHT_GRAY || materialColor == MapColor.TERRACOTTA_LIGHT_GRAY) {
         return WHITE;
      } else if (materialColor == MapColor.DIRT
         || materialColor == MapColor.WOOD
         || materialColor == MapColor.COLOR_BROWN
         || materialColor == MapColor.TERRACOTTA_BROWN
         || materialColor == MapColor.PODZOL) {
         return BROWN;
      } else if (false
         || materialColor == MapColor.COLOR_GRAY
         || materialColor == MapColor.TERRACOTTA_GRAY
         || materialColor == MapColor.GLOW_LICHEN) {
         return WHITE;
      } else if (materialColor == MapColor.WATER
         || materialColor == MapColor.COLOR_BLUE
         || materialColor == MapColor.TERRACOTTA_BLUE
         || materialColor == MapColor.LAPIS) {
         return WHITE;
      } else if (materialColor == MapColor.COLOR_MAGENTA
         || materialColor == MapColor.TERRACOTTA_MAGENTA
         || materialColor == MapColor.WARPED_STEM
         || materialColor == MapColor.WARPED_HYPHAE) {
         return PINK;
      } else if (materialColor == MapColor.COLOR_LIGHT_GREEN || materialColor == MapColor.TERRACOTTA_LIGHT_GREEN) {
         return GREEN;
      } else if (materialColor == MapColor.COLOR_PINK || materialColor == MapColor.TERRACOTTA_PINK) {
         return PINK;
      } else if (materialColor == MapColor.COLOR_CYAN
         || materialColor == MapColor.TERRACOTTA_CYAN
         || materialColor == MapColor.DIAMOND
         || materialColor == MapColor.WARPED_NYLIUM
         || materialColor == MapColor.CRIMSON_STEM
         || materialColor == MapColor.CRIMSON_HYPHAE) {
         return GREEN;
      } else if (materialColor == MapColor.COLOR_PURPLE || materialColor == MapColor.TERRACOTTA_PURPLE) {
         return PINK;
      } else if (materialColor == MapColor.COLOR_RED
         || materialColor == MapColor.TERRACOTTA_RED
         || materialColor == MapColor.NETHER
         || materialColor == MapColor.CRIMSON_NYLIUM
         || materialColor == MapColor.WARPED_WART_BLOCK) {
         return RED;
      } else {
         return materialColor != MapColor.COLOR_BLACK && materialColor != MapColor.TERRACOTTA_BLACK && materialColor != MapColor.DEEPSLATE ? YELLOW : BLACK;
      }
   }

   public static Colors fromTFC(SandBlockType sandColor) {
      switch (sandColor) {
         case BROWN:
            return BROWN;
         case WHITE:
            return WHITE;
         case BLACK:
            return BLACK;
         case RED:
            return RED;
         case YELLOW:
            return YELLOW;
         case GREEN:
            return GREEN;
         case PINK:
            return PINK;
         default:
            return YELLOW;
      }
   }

   public static Colors nonTFC(Colors sandColor) {
      switch (sandColor) {
         case BROWN:
         case WHITE:
         case BLACK:
         case RED:
         case YELLOW:
         case GREEN:
         default:
            return sandColor;
      }
   }

   public Colors nonTFC() {
      switch (this) {
         case BROWN:
         case WHITE:
         case BLACK:
         case RED:
         case YELLOW:
         case GREEN:
         default:
            return this;
      }
   }

   public static SandBlockType toSandTFC(Colors sandColor, boolean withDefault) {
      switch (sandColor) {
         case BROWN:
            return SandBlockType.BROWN;
         case WHITE:
            return SandBlockType.WHITE;
         case BLACK:
            return SandBlockType.BLACK;
         case RED:
            return SandBlockType.RED;
         case YELLOW:
            return SandBlockType.YELLOW;
         case GREEN:
            return SandBlockType.GREEN;
         case PINK:
            return SandBlockType.PINK;
         default:
            return withDefault ? SandBlockType.YELLOW : null;
      }
   }

   public SandBlockType toSandTFC(boolean defaults) {
      switch (this) {
         case BROWN:
            return SandBlockType.BROWN;
         case WHITE:
            return SandBlockType.WHITE;
         case BLACK:
            return SandBlockType.BLACK;
         case RED:
            return SandBlockType.RED;
         case YELLOW:
            return SandBlockType.YELLOW;
         case GREEN:
            return SandBlockType.GREEN;
         case PINK:
            return SandBlockType.PINK;
         default:
            return defaults ? SandBlockType.YELLOW : null;
      }
   }

   public static Colors fromDyeColor(DyeColor dyeColor, boolean withDefault) {
      switch (dyeColor) {
         case WHITE:
            return WHITE;
         case YELLOW:
            return YELLOW;
         case PINK:
            return PINK;
         case BROWN:
            return BROWN;
         case GREEN:
            return GREEN;
         case RED:
            return RED;
         case BLACK:
            return BLACK;
         default:
            return withDefault ? YELLOW : null;
      }
   }
}
