package tfcflorae.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import net.dries007.tfc.world.Codecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class GeyserPointedConfig implements FeatureConfiguration {
   public static final Codec<GeyserPointedConfig> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
               Codecs.mapListCodec(Codecs.recordPairCodec(Codecs.BLOCK, "rock", Codecs.BLOCK_STATE.listOf(), "blocks"))
                  .fieldOf("states")
                  .forGetter(c -> c.states),
               Codecs.mapListCodec(Codecs.recordPairCodec(Codecs.BLOCK, "rock", Codecs.BLOCK_STATE.listOf(), "blocks"))
                  .fieldOf("surface_states")
                  .forGetter(c -> c.surfaceStates),
               Codec.BOOL.optionalFieldOf("has_surface", false).forGetter(c -> c.hasSurface),
               Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_taller_dripstone").orElse(0.2F).forGetter(p_191294_ -> p_191294_.chanceOfTallerDripstone),
               Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_directional_spread").orElse(0.7F).forGetter(p_191292_ -> p_191292_.chanceOfDirectionalSpread),
               Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_spread_radius2").orElse(0.5F).forGetter(p_191290_ -> p_191290_.chanceOfSpreadRadius2),
               Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_spread_radius3").orElse(0.5F).forGetter(p_191288_ -> p_191288_.chanceOfSpreadRadius3)
            )
            .apply(instance, GeyserPointedConfig::new)
   );
   public final Map<Block, List<BlockState>> states;
   public final Map<Block, List<BlockState>> surfaceStates;
   public final Boolean hasSurface;
   public final float chanceOfTallerDripstone;
   public final float chanceOfDirectionalSpread;
   public final float chanceOfSpreadRadius2;
   public final float chanceOfSpreadRadius3;

   public GeyserPointedConfig(
      Map<Block, List<BlockState>> states,
      Map<Block, List<BlockState>> surfaceStates,
      Boolean hasSurface,
      float p_191281_,
      float p_191282_,
      float p_191283_,
      float p_191284_
   ) {
      this.states = states;
      this.surfaceStates = surfaceStates;
      this.hasSurface = hasSurface;
      this.chanceOfTallerDripstone = p_191281_;
      this.chanceOfDirectionalSpread = p_191282_;
      this.chanceOfSpreadRadius2 = p_191283_;
      this.chanceOfSpreadRadius3 = p_191284_;
   }
}
