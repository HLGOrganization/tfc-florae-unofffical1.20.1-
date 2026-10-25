package tfcflorae.client.particle;

import net.dries007.tfc.common.blocks.RiverWaterBlock;
import net.dries007.tfc.world.river.Flow;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

public class WaterFlowParticle extends TextureSheetParticle {
   private WaterFlowParticle(ClientLevel level, double x, double y, double z) {
      super(level, x, y, z);
      this.setSize(0.02F, 0.02F);
      this.quadSize = this.quadSize * (this.random.nextFloat() * 0.6F + 0.2F);
      BlockPos pos = BlockPos.containing(x, y, z);
      FluidState state = level.getFluidState(pos);
      this.setDirectionFromFlow(state);
      this.lifetime = 100;
   }

   private void setDirectionFromFlow(FluidState state) {
      if (state.hasProperty(RiverWaterBlock.FLOW)) {
         Flow flow = (Flow)state.getValue(RiverWaterBlock.FLOW);
         Vec3 vec = flow.getVector();
         this.xd = vec.x * 0.18 + this.xd * 0.02;
         this.zd = vec.z * 0.18 + this.zd * 0.02;
      } else {
         this.xd *= 0.98;
         this.zd *= 0.98;
      }
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      BlockPos pos = BlockPos.containing(this.x, this.y, this.z);
      FluidState state = this.level.getFluidState(pos);
      this.y = (double)state.getHeight(this.level, pos);
      this.setDirectionFromFlow(state);
      if (!state.hasProperty(RiverWaterBlock.FLOW) || !this.level.getBlockState(pos.above()).isAir()) {
         this.lifetime = Math.min(this.lifetime, 10);
      }

      if (this.lifetime-- <= 0) {
         this.remove();
      } else {
         this.move(this.xd, this.yd, this.zd);
      }
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public static record Provider(SpriteSet sprite) implements ParticleProvider<SimpleParticleType> {
      public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
         WaterFlowParticle particle = new WaterFlowParticle(level, x, y, z);
         particle.pickSprite(this.sprite);
         return particle;
      }
   }
}
