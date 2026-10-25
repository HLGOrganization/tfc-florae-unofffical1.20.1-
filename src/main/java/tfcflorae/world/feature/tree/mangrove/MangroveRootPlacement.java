package tfcflorae.world.feature.tree.mangrove;
import net.minecraft.core.registries.Registries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record MangroveRootPlacement(
   HolderSet<Block> canGrowThrough,
   HolderSet<Block> muddyRootsIn,
   BlockStateProvider muddyRootsProvider,
   int maxRootWidth,
   int maxRootLength,
   float randomSkewChance
) {
   public static final Codec<MangroveRootPlacement> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
               RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("can_grow_through").forGetter(placer -> placer.canGrowThrough),
               RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("muddy_roots_in").forGetter(placer -> placer.muddyRootsIn),
               BlockStateProvider.CODEC.fieldOf("muddy_roots_provider").forGetter(placer -> placer.muddyRootsProvider),
               Codec.intRange(1, 12).fieldOf("max_root_width").forGetter(placer -> placer.maxRootWidth),
               Codec.intRange(1, 64).fieldOf("max_root_length").forGetter(placer -> placer.maxRootLength),
               Codec.floatRange(0.0F, 1.0F).fieldOf("random_skew_chance").forGetter(placer -> placer.randomSkewChance)
            )
            .apply(instance, MangroveRootPlacement::new)
   );
}
