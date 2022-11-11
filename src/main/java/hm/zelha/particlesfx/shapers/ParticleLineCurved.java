package hm.zelha.particlesfx.shapers;

import hm.zelha.particlesfx.particles.parents.Particle;
import hm.zelha.particlesfx.util.CurveInfo;
import hm.zelha.particlesfx.util.LVMath;
import hm.zelha.particlesfx.util.LocationSafe;
import hm.zelha.particlesfx.util.Rotation;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class ParticleLineCurved extends ParticleLine {

    //TODO: make curve rotation stuff less jank

    private final List<CurveInfo> curves = new ArrayList<>();
    private final Rotation rot3 = new Rotation();
    private final Vector vectorHelper2 = new Vector(0, 0, 0);
    private final Vector vectorHelper3 = new Vector(0, 0, 0);

    public ParticleLineCurved(Particle particle, double frequency, LocationSafe... locations) {
        super(particle, frequency, locations);
    }

    public ParticleLineCurved(Particle particle, LocationSafe... locations) {
        this(particle, 100, locations);
    }

    //TODO: make this thread-safe (and improve?)
    @Override
    public void display() {
        int curveIndex = 0;
        CurveInfo curve = (curves.isEmpty()) ? null : curves.get(0);
        int current = 0;
        int estimatedOverallCount = 0;
        boolean hasRan = false;
        boolean trackCount = particlesPerDisplay > 0;
        double curveApex = 0, curveEnd = 0, curveCurrent = 0;

        if (curve != null) {
            curveApex = curve.getApexPosition();
            curveEnd = curve.getLength();
        }

        main:
        for (int i = 0; i < locations.size() - 1; i++) {
            Location start = locations.get(i);
            Location end = locations.get(i + 1);
            double distance = start.distance(end);
            double control = (distance / particleFrequency) * locations.size();

            if (trackCount && overallCount >= estimatedOverallCount + (distance / control)) {
                estimatedOverallCount += distance / control;
                curveCurrent += distance;

                while (curveCurrent >= curveEnd) {
                    curveIndex++;
                    curve = (curveIndex >= curves.size()) ? null : curves.get(curveIndex);

                    if (curve == null) break;

                    curveCurrent -= curveEnd;
                    curveApex = curve.getApexPosition();
                    curveEnd = curve.getLength();
                }

                continue;
            }

            locationHelper.zero().add(start);
            LVMath.subtractToVector(vectorHelper3, end, start).normalize().multiply(control);

            if (trackCount) {
                current = overallCount - estimatedOverallCount;
                curveCurrent += control * current;

                locationHelper.add(vectorHelper3.getX() * current, vectorHelper3.getY() * current, vectorHelper3.getZ() * current);

                while (curveCurrent >= curveEnd) {
                    curveIndex++;
                    curve = (curveIndex >= curves.size()) ? null : curves.get(curveIndex);

                    if (curve == null) break;

                    curveCurrent -= curveEnd;
                    curveApex = curve.getApexPosition();
                    curveEnd = curve.getLength();
                }
            }

            for (double length = control * current; length <= distance; length += control) {
                if (mechanic != null) {
                    mechanic.apply(particle, locationHelper, vectorHelper3);
                }

                vectorHelper2.zero();
                locationHelper.add(vectorHelper3);

                curveCurrent += control;

                while (curveCurrent >= curveEnd) {
                    curveIndex++;
                    curve = (curveIndex >= curves.size()) ? null : curves.get(curveIndex);

                    if (curve == null) break;

                    curveCurrent -= curveEnd;
                    curveApex = curve.getApexPosition();
                    curveEnd = curve.getLength();
                }

                if (curve != null && curve.getHeight() != 0) {
                    double height = curve.getHeight();

                    if (curveCurrent <= curveApex) {
                        vectorHelper2.setY(height * Math.sin(Math.PI / 2 * curveCurrent / curveApex));
                    } else {
                        //idk what to call this variable it basically determines the decrement of the curve
                        double v = (curveCurrent - curveApex) / (curveEnd - curveApex);

                        vectorHelper2.setY(height - height * v * Math.sin(Math.PI / 2 * v));
                    }

                    rot3.set(curve.getPitch(), curve.getYaw(), curve.getRoll());
                    rot3.apply(vectorHelper2);
                    locationHelper.add(vectorHelper2);
                }

                getCurrentParticle().display(locationHelper);
                locationHelper.subtract(vectorHelper2);

                overallCount++;

                if (trackCount) {
                    currentCount++;
                    hasRan = true;

                    if (currentCount >= particlesPerDisplay) {
                        currentCount = 0;
                        break main;
                    }
                }
            }
        }

        if (!trackCount || !hasRan) {
            overallCount = 0;
        }
    }

    public void addCurve(CurveInfo curve) {
        curves.add(curve);
    }

    public CurveInfo getCurve(int index) {
        return curves.get(index);
    }

    public void removeCurve(int index) {
        curves.remove(index);
    }
}