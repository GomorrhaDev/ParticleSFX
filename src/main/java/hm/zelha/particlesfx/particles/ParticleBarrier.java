package hm.zelha.particlesfx.particles;

import hm.zelha.particlesfx.particles.parents.Particle;
import net.minecraft.server.v1_13_R2.IRegistry;
import net.minecraft.server.v1_13_R2.MinecraftKey;
import net.minecraft.server.v1_13_R2.ParticleType;

public class ParticleBarrier extends Particle {
    public ParticleBarrier(double offsetX, double offsetY, double offsetZ, int count) {
        super((ParticleType) IRegistry.PARTICLE_TYPE.get(new MinecraftKey("barrier")), offsetX, offsetY, offsetZ, 0, count, 0);
    }

    public ParticleBarrier(double offsetX, double offsetY, double offsetZ) {
        this(offsetX, offsetY, offsetZ, 1);
    }

    public ParticleBarrier(int count) {
        this(0, 0, 0, count);
    }

    public ParticleBarrier() {
        this(0, 0, 0, 1);
    }

    @Override
    public ParticleBarrier inherit(Particle particle) {
        super.inherit(particle);

        return this;
    }

    @Override
    public ParticleBarrier clone() {
        return new ParticleBarrier().inherit(this);
    }
}