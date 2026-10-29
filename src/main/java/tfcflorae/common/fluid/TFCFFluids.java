package tfcflorae.common.fluid;
import net.minecraft.core.registries.Registries;

import java.awt.Color;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import net.dries007.tfc.common.fluids.ExtendedFluidType;
import net.dries007.tfc.common.fluids.FluidRegistryObject;
import net.dries007.tfc.common.fluids.FluidTypeClientProperties;
import net.dries007.tfc.common.fluids.MoltenFluid;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.registry.RegistrationHelpers;

import tfcflorae.common.blocks.TFCFBlocks;

/**
 * Fluids for TFC Florae.
 * <p>
 * Forge 1.19 split {@code FluidAttributes} into {@link FluidType} (server data) plus
 * {@code IClientFluidTypeExtensions} (client data); TFC 3.x wraps this in
 * {@link ExtendedFluidType} + {@link FluidTypeClientProperties}. Registration therefore needs a
 * second {@link DeferredRegister} for the types and passes the factories explicitly.
 */
public class TFCFFluids
{
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, "tfcflorae");
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, "tfcflorae");

    public static final ResourceLocation WATER_STILL = new ResourceLocation("block/water_still");
    public static final ResourceLocation WATER_FLOW = new ResourceLocation("block/water_flow");
    public static final ResourceLocation WATER_OVERLAY = new ResourceLocation("block/water_overlay");
    public static final ResourceLocation MOLTEN_STILL = Helpers.identifier("block/molten_still");
    public static final ResourceLocation MOLTEN_FLOW = Helpers.identifier("block/molten_flow");
    public static final int ALPHA_MASK = 0xFF000000;

    public static final Map<SimpleFluids, FluidRegistryObject<ForgeFlowingFluid>> SIMPLE_FLUIDS = new java.util.HashMap<>();

    public static int dyeColorToInt(DyeColor dye)
    {
        final float[] colors = dye.getTextureDiffuseColors();
        return new Color(colors[0], colors[1], colors[2]).getRGB();
    }

    // Registration

    private static FluidRegistryObject<ForgeFlowingFluid> register(
        String name,
        Consumer<ForgeFlowingFluid.Properties> builder,
        FluidType.Properties typeProperties,
        FluidTypeClientProperties clientProperties
    )
    {
        return register(name, builder, typeProperties, clientProperties,
            ForgeFlowingFluid.Source::new, ForgeFlowingFluid.Flowing::new);
    }

    private static <F extends FlowingFluid> FluidRegistryObject<F> register(
        String name,
        Consumer<ForgeFlowingFluid.Properties> builder,
        FluidType.Properties typeProperties,
        FluidTypeClientProperties clientProperties,
        Function<ForgeFlowingFluid.Properties, F> sourceFactory,
        Function<ForgeFlowingFluid.Properties, F> flowingFactory
    )
    {
        final String flowingName = "flowing_" + name;
        return RegistrationHelpers.registerFluid(
            FLUID_TYPES, FLUIDS,
            name, name, flowingName,
            builder,
            () -> new ExtendedFluidType(typeProperties, clientProperties),
            sourceFactory, flowingFactory
        );
    }

    private static <F extends Fluid> RegistryObject<F> register(String name, Supplier<F> factory)
    {
        return FLUIDS.register(name, factory);
    }
}
