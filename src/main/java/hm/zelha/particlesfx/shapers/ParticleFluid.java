package hm.zelha.particlesfx.shapers;

import hm.zelha.particlesfx.Main;
import hm.zelha.particlesfx.particles.parents.Particle;
import hm.zelha.particlesfx.shapers.parents.ParticleShaper;
import hm.zelha.particlesfx.util.LVMath;
import hm.zelha.particlesfx.util.LocationSafe;
import hm.zelha.particlesfx.util.ShapeDisplayMechanic;
import net.minecraft.server.v1_8_R3.Entity;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.bukkit.Material.AIR;

public class ParticleFluid extends ParticleShaper {

    private final ThreadLocalRandom rng = ThreadLocalRandom.current();
    private final Location locationHelper2;
    private final Vector vectorHelper2 = new Vector(0, 0, 0);
    private LocationSafe spawnLocation;
    private double gravity;
    private double repulsion;

    public ParticleFluid(Particle particle, LocationSafe spawnLocation, double gravity, double repulsion, int particleFrequency) {
        super(particle, particleFrequency);

        locationHelper2 = spawnLocation.clone();

        setSpawnLocation(spawnLocation);
        setWorld(spawnLocation.getWorld());
        setParticleFrequency(particleFrequency);
        setGravity(gravity);
        setRepulsion(repulsion);
        start();
    }

    @Override
    public void start() {
        if (animator != null) return;

        animator = new BukkitRunnable() {
            @Override
            public void run() {
                //literally cant do anything about this error so
                try {
                    display();
                } catch (IllegalStateException ignored) {}
            }
        }.runTaskTimerAsynchronously(Main.getPlugin(), 1, 1);
    }

    @Override
    public void display() {
        double repulsion = Math.abs(this.repulsion);
        boolean hasRan = false;
        boolean trackCount = particlesPerDisplay > 0;

        for (int i = 0; i < locations.size(); i++) {
            Location l = locations.get(i);
            Particle particle = getCurrentParticle();
            List<Entity> entityList = ((CraftWorld) l.getWorld()).getHandle().entityList;
            int nearby = 0;

            //stuck prevention
            //for some reason some particles still get lodged inside of blocks
            //and i cant figure out why or how to fix it for the life of me
            //i seem to have at least mostly fixed it though
            while (l.getBlock().getType() != AIR) {
                l.setY((int) (l.getY() + 1));
            }

            locationHelper.zero().add(l);
            locationHelper2.zero();

            //attraction
            //TODO: make block collision work with this
            if (this.repulsion < 0) {
                locationHelper2.zero();

                for (int z = 0; z < locations.size(); z++) {
                    locationHelper2.add(locations.get(z));
                }

                locationHelper2.multiply(1D / locations.size());
                LVMath.subtractToVector(vectorHelper, locationHelper2, locationHelper);
                LVMath.divide(vectorHelper, LVMath.getAbsoluteSum(vectorHelper) / repulsion);
                locationHelper.add(vectorHelper);
            }

            //repulsion
            for (int z = 0; z < locations.size(); z++) {
                Location other = locations.get(z);

                if (l == other) continue;
                if (locationHelper.distanceSquared(other) > Math.pow(repulsion, 2)) continue;

                LVMath.subtractToVector(vectorHelper, locationHelper, other);

                if (LVMath.getAbsoluteSum(vectorHelper) == 0) {
                    vectorHelper.setX(rng.nextDouble(repulsion * 2) - repulsion);
                    vectorHelper.setY(rng.nextDouble(repulsion * 2) - repulsion);
                    vectorHelper.setZ(rng.nextDouble(repulsion * 2) - repulsion);
                } else {
                    LVMath.divide(vectorHelper, LVMath.getAbsoluteSum(vectorHelper) / repulsion);
                }

                locationHelper.add(vectorHelper);

                nearby++;
            }

            //go up if theres more than 2 particles nearby
            if (nearby > 1) {
                locationHelper.setY(locationHelper.getY() + (repulsion * (1 - (1D / nearby))));
            }

            //gravity
            vectorHelper.zero();
            vectorHelper.setY(-gravity);
            locationHelper.add(vectorHelper);

            //entity collision
            for (int z = 0; z < entityList.size(); z++) {
                Entity e = entityList.get(z);

                //checking if locationHelper is within range of entity
                if (locationHelper.getX() < e.locX - (e.width / 2) - repulsion) continue;
                if (locationHelper.getY() < e.locY - repulsion) continue;
                if (locationHelper.getZ() < e.locZ - (e.width / 2) - repulsion) continue;
                if (locationHelper.getX() >= e.locX + (e.width / 2) + repulsion) continue;
                if (locationHelper.getY() >= e.locY + e.length + repulsion) continue;
                if (locationHelper.getZ() >= e.locZ + (e.width / 2) + repulsion) continue;

                locationHelper2.zero().add(e.locX, e.locY + (e.length / 2), e.locZ);
                LVMath.subtractToVector(vectorHelper, locationHelper, locationHelper2);
                vectorHelper2.zero().add(vectorHelper);

                double bound = (e.width / 2) + repulsion;

                if (Math.abs(vectorHelper2.getX()) > Math.abs(vectorHelper2.getZ())) {
                    if (vectorHelper2.getX() < 0) bound = -bound;

                    vectorHelper2.setX(bound);
                } else {
                    if (vectorHelper2.getZ() < 0) bound = -bound;

                    vectorHelper2.setZ(bound);
                }

                vectorHelper2.subtract(vectorHelper);
                locationHelper.add(vectorHelper2);
            }

            //block collision
            LVMath.subtractToVector(vectorHelper, locationHelper, l);
            applyMechanics(ShapeDisplayMechanic.Phase.BEFORE_ROTATION, particle, l, vectorHelper);

            double increase = 0.1;
            double absoluteSum = LVMath.getAbsoluteSum(vectorHelper);

            if (absoluteSum < increase) {
                increase = absoluteSum;
            }

            LVMath.divide(vectorHelper, absoluteSum / increase);

            for (double z = 0; z < absoluteSum; z += increase) {
                locationHelper2.zero().add(l);

                if (locationHelper2.add(increase, 0, 0).getBlock().getType() != AIR && vectorHelper.getX() > 0) {
                    vectorHelper.setX(0);
                }

                if (locationHelper2.subtract(increase * 2, 0, 0).getBlock().getType() != AIR && vectorHelper.getX() < 0) {
                    vectorHelper.setX(0);
                }

                if (locationHelper2.add(increase, increase, 0).getBlock().getType() != AIR && vectorHelper.getY() > 0) {
                    vectorHelper.setY(0);
                }

                if (locationHelper2.subtract(0, increase * 2, 0).getBlock().getType() != AIR && vectorHelper.getY() < 0) {
                    vectorHelper.setY(0);
                }

                if (locationHelper2.add(0, increase, increase).getBlock().getType() != AIR && vectorHelper.getZ() > 0) {
                    vectorHelper.setZ(0);
                }

                if (locationHelper2.subtract(0, 0, increase * 2).getBlock().getType() != AIR && vectorHelper.getZ() < 0) {
                    vectorHelper.setZ(0);
                }

                LVMath.divide(vectorHelper, LVMath.getAbsoluteSum(vectorHelper) / increase);
                l.add(vectorHelper);
            }

            if (!players.isEmpty()) {
                particle.displayForPlayers(l, players);
            } else {
                particle.display(l);
            }

            overallCount++;

            applyMechanics(ShapeDisplayMechanic.Phase.AFTER_DISPLAY, particle, locationHelper, vectorHelper);

            if (trackCount) {
                currentCount++;
                hasRan = true;

                if (currentCount >= particlesPerDisplay) {
                    currentCount = 0;
                    break;
                }
            }
        }

        if (!trackCount || !hasRan) {
            overallCount = 0;
        }
    }

    @Override
    public ParticleFluid clone() {
        ParticleFluid clone = new ParticleFluid(particle, spawnLocation.clone(), gravity, repulsion, 1);

        clone.particleFrequency = particleFrequency;

        clone.locations.clear();
        clone.origins.clear();

        for (LocationSafe l : locations) {
            clone.locations.add(l.clone());
            clone.origins.add(l.clone());
        }

        for (Pair<Particle, Integer> pair : secondaryParticles) {
            clone.addParticle(pair.getKey(), pair.getValue());
        }

        for (Pair<ShapeDisplayMechanic, ShapeDisplayMechanic.Phase> pair : mechanics) {
            clone.addMechanic(pair.getValue(), pair.getKey());
        }

        for (UUID id : players) {
            clone.addPlayer(id);
        }

        clone.setParticlesPerDisplay(particlesPerDisplay);

        return null;
    }

    @Override
    public void setParticleFrequency(int particleFrequency) {
        Validate.isTrue(particleFrequency > 0, "Cannot have a particle frequency of 0 or less!");

        super.particleFrequency = particleFrequency;

        if (spawnLocation == null) return;

        if (particleFrequency > locations.size()) {
            for (int i = locations.size(); i <= particleFrequency; i++) {
                locations.add(spawnLocation.clone());
                origins.add(spawnLocation.clone());
            }
        } else if (particleFrequency < locations.size()) {
            for (int i = particleFrequency; i <= locations.size(); i++) {
                int index = rng.nextInt(locations.size());

                locations.remove(index);
                origins.remove(index);
            }
        }

        locations.get(0).setChanged(true);
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        spawnLocation.setWorld(world);
        locationHelper2.setWorld(world);
    }

    public void setGravity(double gravity) {
        this.gravity = gravity;
    }

    public void setRepulsion(double repulsion) {
        this.repulsion = repulsion;
    }

    public void setSpawnLocation(LocationSafe spawnLocation) {
        Validate.notNull(spawnLocation, "Location cannot be null!");
        Validate.notNull(spawnLocation.getWorld(), "Location's world cannot be null!");

        this.spawnLocation = spawnLocation;
    }

    public double getGravity() {
        return gravity;
    }

    public double getRepulsion() {
        return repulsion;
    }

    public LocationSafe getSpawnLocation() {
        return spawnLocation;
    }
}
