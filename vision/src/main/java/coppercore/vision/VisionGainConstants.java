package coppercore.vision;

/** Vision constants for coppercore's pose rejection and standard deviation calculations. */
public final class VisionGainConstants {
    /**
     * Maximum distance for a pose estimate to be accepted.
     *
     * <p>The default value of 10.0 is just a guess and will need to be tuned depending on your
     * specific camera resolution and FOV.
     */
    public double maxAcceptedDistanceMeters = 10.0;

    /**
     * Baseline position standard deviation, for 1 tag from 1 meter distance, measured in meters.
     *
     * <p>This value will be adjusted for distance and tag count.
     *
     * <p>The default value of 0.02 comes from the AdvantageKit vision template and will work
     * decently in many situations but MUST be tuned per-robot for optimal results.
     */
    public double linearStdDevFactor = 0.02;

    /**
     * Baseline angular standard deviation, for 1 tag from 1 meter distance, measured in radians.
     *
     * <p>This value will be adjusted for distance and tag count.
     *
     * <p>The default value of 0.06 comes from the AdvantageKit vision template and will work
     * decently in many situations but MUST be tuned per-robot for optimal results.
     */
    public double angularStdDevFactor = 0.06;

    /**
     * Maximum height in meters that a pose estimate can report before being rejected.
     *
     * <p>The thinking here is that if a pose estimate thinks the robot is super high up off the
     * ground, it must be a bad pose.
     */
    public double maxZCutoff = 0.75;

    /**
     * The maximum pose ambiguity a single tag estimate may report before being rejected.
     *
     * <p>Read more about pose ambiguity here:
     * https://docs.wpilib.org/en/stable/docs/software/vision-processing/apriltag/apriltag-intro.html#d-to-3d-ambiguity
     *
     * <p>The calculations for this value are described in the photonvision docs here:
     * https://docs.photonvision.org/en/latest/docs/apriltag-pipelines/3D-tracking.html#ambiguity.
     */
    public double maxSingleTagAmbiguity = 0.3;

    /**
     * The maximum pose ambiguity a multi- or single-tag estimate may report before being rejected.
     *
     * <p>Read more about pose ambiguity here:
     * https://docs.wpilib.org/en/stable/docs/software/vision-processing/apriltag/apriltag-intro.html#d-to-3d-ambiguity
     *
     * <p>The calculations for this value are described in the photonvision docs here:
     * https://docs.photonvision.org/en/latest/docs/apriltag-pipelines/3D-tracking.html#ambiguity.
     */
    public double maxAmbiguity = 0.3;

    /**
     * Create a new VisionGainConstants object with default gains.
     *
     * <p>These default gains are taken from the AdvantageKit vision template. They are intended to
     * be a sensible BASELINE for your vision system. They will not give amazing results for your
     * specific system, and they need to be tuned for each specific coprocessor and camera to ensure
     * that they are as accurate as possible.
     */
    public VisionGainConstants() {}

    /**
     * Create a new vision gain constants with specific values for each gain.
     *
     * @param maxAcceptedDistanceMeters The maximum distance in meters to accept measurements at;
     *     all values with a distance greater than this value will be rejected.
     * @param linearStdDevFactor Baseline standard deviation in meters for 1 meter away from 1 tag.
     * @param angularStdDevFactor Baseline standard deviation in radians for 1 meter away from 1
     *     tag.
     * @param maxZCutoff Maximum acceptable height for a pose estimate to be accepted.
     * @param maxSingleTagAmbiguity Maximum pose ambiguity for a pose estimate to be accepted.
     */
    public VisionGainConstants(
            double maxAcceptedDistanceMeters,
            double linearStdDevFactor,
            double angularStdDevFactor,
            double distanceFactor,
            double maxZCutoff,
            double maxSingleTagAmbiguity) {
        this.maxAcceptedDistanceMeters = maxAcceptedDistanceMeters;
        this.linearStdDevFactor = linearStdDevFactor;
        this.angularStdDevFactor = angularStdDevFactor;
        this.maxZCutoff = maxZCutoff;
        this.maxSingleTagAmbiguity = maxSingleTagAmbiguity;
    }

    /**
     * Update this object's maximum distance to accept measurements.
     *
     * @param newMaxAcceptedDistanceMeters The new maximum distance, in meters.
     * @return This gains object, for easy method chaining.
     */
    public VisionGainConstants withMaxAcceptedDistanceMeters(double newMaxAcceptedDistanceMeters) {
        this.maxAcceptedDistanceMeters = newMaxAcceptedDistanceMeters;
        return this;
    }

    /**
     * Update this object's linear standard deviation factor. This is the baseline standard
     * deviation for 1 meter from 1 tag, measured in meters.
     *
     * @param linearStdDevFactorMeters Baseline 1 meter/1 tag linear standard deviation in meters.
     * @return This gains object, for easy method chaining.
     */
    public VisionGainConstants withLinearStdDevFactor(double linearStdDevFactorMeters) {
        this.linearStdDevFactor = linearStdDevFactorMeters;
        return this;
    }

    /**
     * Update this object's angular standard deviation factor. This is the baseline standard
     * deviation in angle estimate for 1 meter from 1 tag, measured in radians.
     *
     * @param angularStdDevFactorRadians Baseline 1 meter/1 tag angular standard deviation in
     *     meters.
     * @return This gains object, for easy method chaining.
     */
    public VisionGainConstants withAngularStdDevFactor(double angularStdDevFactorRadians) {
        this.angularStdDevFactor = angularStdDevFactorRadians;
        return this;
    }

    /**
     * Update this object's maximum height in meters that a pose estimate can report before being
     * rejected.
     *
     * @param maxZCutoff Maximum height in meters that a pose estimate can report before being
     *     rejected.
     * @return This gains object, for easy method chaining.
     */
    public VisionGainConstants withMaxZCutoff(double maxZCutoff) {
        this.maxZCutoff = maxZCutoff;
        return this;
    }

    /**
     * Update this object's maximum pose ambiguity a single tag estimate may report before being
     * rejected.
     *
     * @param maxSingleTagAmbiguity Maximum pose ambiguity a single tag estimate may report before
     *     being rejected.
     * @return This gains object, for easy method chaining.
     */
    public VisionGainConstants withMaxSingleTagAmbiguity(double maxSingleTagAmbiguity) {
        this.maxSingleTagAmbiguity = maxSingleTagAmbiguity;
        return this;
    }

    /**
     * Update this object's maximum pose ambiguity any estimate may report before being rejected.
     *
     * <p>While this value should almost always be greater than maxSingleTagAmbiguity, it will still
     * serve as a maximum for single-tag measurements as well in the case that it's lower than the
     * maxSingleTagAmbiguity.
     *
     * @param maxAmbiguity Maximum pose ambiguity a multi-tag estimate may report before being
     *     rejected.
     * @return This gains object, for easy method chaining.
     */
    public VisionGainConstants withMaxAmbiguity(double maxAmbiguity) {
        this.maxAmbiguity = maxAmbiguity;
        return this;
    }
}
