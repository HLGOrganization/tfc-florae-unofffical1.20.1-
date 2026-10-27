package tfcflorae.common.blocks.ceramics;

import java.awt.Color;
import java.util.Locale;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import tfcflorae.util.registry.RegistryClay;

public enum Clay implements RegistryClay {
   EARTHENWARE(new Color(143, 70, 47).getRGB(), MapColor.TERRACOTTA_RED, false),
   STONEWARE(new Color(103, 99, 98).getRGB(), MapColor.TERRACOTTA_GRAY, false);

   public static final Clay[] VALUES = values();
   public final String serializedName = this.name().toLowerCase(Locale.ROOT);
   public final int dustColor;
   public final MapColor materialColor;
   private final boolean hasRock;

   public static Clay valueOf(int i) {
      return i >= 0 && i < VALUES.length ? VALUES[i] : EARTHENWARE;
   }

   private Clay(int dustColor, MapColor materialColor, boolean hasRock) {
      this.dustColor = dustColor;
      this.materialColor = materialColor;
      this.hasRock = hasRock;
   }

   public String getSerializedName() {
      return this.serializedName;
   }

   public MapColor getMaterialColor() {
      return this.materialColor;
   }

   public boolean hasRock() {
      return this.hasRock;
   }

   public Block create() {
      return new Block(Properties.of().mapColor(MapColor.STONE).sound(SoundType.STONE).strength(5.5F, 9.0F).noCollission().requiresCorrectToolForDrops());
   }
}
