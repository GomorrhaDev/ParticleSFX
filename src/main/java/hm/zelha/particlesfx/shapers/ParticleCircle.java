package hm.zelha.particlesfx.shapers;

import hm.zelha.particlesfx.Main;
import hm.zelha.particlesfx.particles.parents.Particle;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class ParticleCircle {

    private BukkitTask animator = null;
    private Particle particle;
    private Location center;
    private double xRadius;
    private double zRadius;
    private double pitch;
    private double yaw;
    private double roll;
    private double frequency;
    private boolean halfCircle;

    public ParticleCircle(Particle particle, Location center, double xRadius, double zRadius, double pitch, double yaw, double roll, double frequency, boolean halfCircle) {
        Validate.notNull(particle, "Particle cannot be null!");
        Validate.notNull(center, "Location cannot be null!");
        Validate.notNull(center.getWorld(), "Location's world cannot be null!");
        Validate.isTrue(frequency > 2.0D, "Frequency cannot be 2 or less! if you only want one particle, use Particle.display().");

        this.particle = particle;
        this.center = center;
        this.xRadius = xRadius;
        this.zRadius = zRadius;
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
        this.frequency = (Math.PI * 2) / frequency;
        this.halfCircle = halfCircle;

        start();
    }

    public ParticleCircle(Particle particle, Location center, double xRadius, double zRadius, double pitch, double yaw, double roll, double frequency) {
        this(particle, center, xRadius, zRadius, pitch, yaw, roll, frequency, false);
    }

    public ParticleCircle(Particle particle, Location center, double xRadius, double zRadius, double pitch, double yaw, double roll, boolean halfCircle) {
        this(particle, center, xRadius, zRadius, pitch, yaw, roll, Math.PI / 50, halfCircle);
    }

    public ParticleCircle(Particle particle, Location center, double xRadius, double zRadius, double pitch, double yaw, double roll) {
        this(particle, center, xRadius, zRadius, pitch, yaw, roll, Math.PI / 50, false);
    }

    public ParticleCircle(Particle particle, Location center, double xRadius, double zRadius, double frequency) {
        this(particle, center, xRadius, zRadius, 0, 0, 0, frequency, false);
    }

    public ParticleCircle(Particle particle, Location center, double xRadius, double zRadius, boolean halfCircle) {
        this(particle, center, xRadius, zRadius, 0, 0, 0, Math.PI / 50, halfCircle);
    }

    public ParticleCircle(Particle particle, Location center, double xRadius, double zRadius) {
        this(particle, center, xRadius, zRadius, 0, 0, 0, Math.PI / 50, false);
    }

    public void start() {
        if (animator != null) return;

        animator = new BukkitRunnable() {
            @Override
            public void run() {
                for (double radian = 0; radian < Math.PI * ((halfCircle) ? 1 : 2); radian += frequency) {
                    Vector addition = new Vector(xRadius * Math.cos(radian), 0, zRadius * Math.sin(radian));

                    applyPitch(addition);
                    applyYaw(addition);
                    applyRoll(addition);

                    particle.display(center.add(addition));
                    center.subtract(addition);
                }
            }
        }.runTaskTimer(Main.getPlugin(), 0, 1);
    }

    public void stop() {
        if (animator == null) return;

        animator.cancel();
        animator = null;
    }

    private void applyPitch(Vector v) {
        if (pitch == 0) return;

        double y, z, cos, sin, angle;
        angle = Math.toRadians(pitch);
        cos = Math.cos(angle);
        sin = Math.sin(angle);
        y = v.getY() * cos - v.getZ() * sin;
        z = v.getY() * sin + v.getZ() * cos;

        v.setY(y).setZ(z);
    }

    private void applyYaw(Vector v) {
        if (yaw == 0) return;

        double x, z, cos, sin, angle;
        angle = -yaw;
        angle = Math.toRadians(angle);
        cos = Math.cos(angle);
        sin = Math.sin(angle);
        x = v.getX() * cos + v.getZ() * sin;
        z = v.getX() * -sin + v.getZ() * cos;

        v.setX(x).setZ(z);
    }

    private void applyRoll(Vector v) {
        if (roll == 0) return;

        double x, y, cos, sin, angle;
        angle = Math.toRadians(roll);
        cos = Math.cos(angle);
        sin = Math.sin(angle);
        x = v.getX() * cos - v.getY() * sin;
        y = v.getX() * sin + v.getY() * cos;

        v.setX(x).setY(y);
    }

    public void setParticle(Particle particle) {
        Validate.notNull(particle, "Particle cannot be null!");

        this.particle = particle;
    }

    public void setCenter(Location center) {
        this.center = center;
    }

    public void setxRadius(double xRadius) {
        this.xRadius = xRadius;
    }

    public void setzRadius(double zRadius) {
        this.zRadius = zRadius;
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
    }

    public void setYaw(double yaw) {
        this.yaw = yaw;
    }

    public void setRoll(double roll) {
        this.roll = roll;
    }

    /**
     *
     * @param frequency amount of particles to display per full animation
     */
    public void setFrequency(double frequency) {
        Validate.isTrue(frequency > 2.0D, "Frequency cannot be 2 or less! if you only want one particle, use Particle.display()");

        this.frequency = (Math.PI * 2) / frequency;
    }

    public void setHalfCircle(boolean halfCircle) {
        this.halfCircle = halfCircle;
    }

    public Particle getParticle() {
        return particle;
    }

    public Location getCenter() {
        return center;
    }

    public double getxRadius() {
        return xRadius;
    }

    public double getzRadius() {
        return zRadius;
    }

    public double getPitch() {
        return pitch;
    }

    public double getYaw() {
        return yaw;
    }

    public double getRoll() {
        return roll;
    }

    public double getFrequency() {
        return frequency;
    }

    public boolean isHalfCircle() {
        return halfCircle;
    }
}
