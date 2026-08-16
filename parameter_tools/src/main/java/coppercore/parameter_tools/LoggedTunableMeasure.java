package coppercore.parameter_tools;

import org.wpilib.units.AngleUnit;
import org.wpilib.units.AngularAccelerationUnit;
import org.wpilib.units.AngularMomentumUnit;
import org.wpilib.units.AngularVelocityUnit;
import org.wpilib.units.CurrentUnit;
import org.wpilib.units.DimensionlessUnit;
import org.wpilib.units.DistanceUnit;
import org.wpilib.units.EnergyUnit;
import org.wpilib.units.ForceUnit;
import org.wpilib.units.FrequencyUnit;
import org.wpilib.units.LinearAccelerationUnit;
import org.wpilib.units.LinearMomentumUnit;
import org.wpilib.units.LinearVelocityUnit;
import org.wpilib.units.MassUnit;
import org.wpilib.units.Measure;
import org.wpilib.units.MomentOfInertiaUnit;
import org.wpilib.units.MutableMeasure;
import org.wpilib.units.PerUnit;
import org.wpilib.units.PowerUnit;
import org.wpilib.units.ResistanceUnit;
import org.wpilib.units.TemperatureUnit;
import org.wpilib.units.TimeUnit;
import org.wpilib.units.TorqueUnit;
import org.wpilib.units.Unit;
import org.wpilib.units.VelocityUnit;
import org.wpilib.units.VoltageUnit;
import org.wpilib.units.measure.Angle;
import org.wpilib.units.measure.AngularAcceleration;
import org.wpilib.units.measure.AngularMomentum;
import org.wpilib.units.measure.AngularVelocity;
import org.wpilib.units.measure.Current;
import org.wpilib.units.measure.Dimensionless;
import org.wpilib.units.measure.Distance;
import org.wpilib.units.measure.Energy;
import org.wpilib.units.measure.Force;
import org.wpilib.units.measure.Frequency;
import org.wpilib.units.measure.LinearAcceleration;
import org.wpilib.units.measure.LinearMomentum;
import org.wpilib.units.measure.LinearVelocity;
import org.wpilib.units.measure.Mass;
import org.wpilib.units.measure.MomentOfInertia;
import org.wpilib.units.measure.MutAngle;
import org.wpilib.units.measure.MutAngularAcceleration;
import org.wpilib.units.measure.MutAngularMomentum;
import org.wpilib.units.measure.MutAngularVelocity;
import org.wpilib.units.measure.MutCurrent;
import org.wpilib.units.measure.MutDimensionless;
import org.wpilib.units.measure.MutDistance;
import org.wpilib.units.measure.MutEnergy;
import org.wpilib.units.measure.MutForce;
import org.wpilib.units.measure.MutFrequency;
import org.wpilib.units.measure.MutLinearAcceleration;
import org.wpilib.units.measure.MutLinearMomentum;
import org.wpilib.units.measure.MutLinearVelocity;
import org.wpilib.units.measure.MutMass;
import org.wpilib.units.measure.MutMomentOfInertia;
import org.wpilib.units.measure.MutPer;
import org.wpilib.units.measure.MutPower;
import org.wpilib.units.measure.MutResistance;
import org.wpilib.units.measure.MutTemperature;
import org.wpilib.units.measure.MutTime;
import org.wpilib.units.measure.MutTorque;
import org.wpilib.units.measure.MutVelocity;
import org.wpilib.units.measure.MutVoltage;
import org.wpilib.units.measure.Per;
import org.wpilib.units.measure.Power;
import org.wpilib.units.measure.Resistance;
import org.wpilib.units.measure.Temperature;
import org.wpilib.units.measure.Time;
import org.wpilib.units.measure.Torque;
import org.wpilib.units.measure.Velocity;
import org.wpilib.units.measure.Voltage;

public class LoggedTunableMeasure<
        MutMeasureType extends MutableMeasure<BaseUnitType, BaseMeasureType, MutMeasureType>,
        BaseMeasureType extends Measure<BaseUnitType>,
        BaseUnitType extends Unit> {

    final MutMeasureType value;
    final LoggedTunableNumber tunableNumber;
    final BaseUnitType displayedUnit;

    /**
     * Creates a logged tunable value for a WPILib measure.
     *
     * @param name logged tunable path
     * @param defaultValue default measure value
     * @param displayedUnit unit used for logging and tuning
     * @param addUnitSuffix whether to append the unit name to the path
     */
    public LoggedTunableMeasure(
            String name,
            MutMeasureType defaultValue,
            BaseUnitType displayedUnit,
            boolean addUnitSuffix) {
        this.value = defaultValue;
        String suffix = (addUnitSuffix) ? "_" + displayedUnit.name() : "";
        this.tunableNumber = new LoggedTunableNumber(name + suffix, defaultValue.in(displayedUnit));
        this.displayedUnit = displayedUnit;
    }

    private void updateValue(double newValue) {
        value.mut_replace(newValue, displayedUnit);
    }

    /** Forces the cached measure to match the current logged tunable number. */
    public void forceUpdate() {
        updateValue(tunableNumber.getAsDouble());
    }

    /**
     * Runs a callback when this value changes for a caller id.
     *
     * @param id caller id used to track changes independently
     * @param callback callback receiving the updated mutable measure
     */
    public void ifChanged(
            int id, MeasureConsumer<MutMeasureType, BaseMeasureType, BaseUnitType> callback) {
        LoggedTunableNumber.ifChanged(
                id,
                newValue -> {
                    updateValue(newValue[0]);
                    callback.accept(value);
                },
                tunableNumber);
    }

    /**
     * Runs a callback when this value changes for this object.
     *
     * @param callback callback receiving the updated mutable measure
     */
    public void ifChanged(MeasureConsumer<MutMeasureType, BaseMeasureType, BaseUnitType> callback) {
        ifChanged(hashCode(), callback);
    }

    protected void checkForUpdate() {
        LoggedTunableNumber.ifChanged(
                hashCode(), newValue -> updateValue(newValue[0]), tunableNumber);
    }

    /**
     * Checks whether this value changed for a caller id.
     *
     * @param id caller id used to track changes independently
     * @return true if the logged value changed since this id last checked
     */
    public boolean hasChanged(int id) {
        return tunableNumber.hasChanged(id);
    }

    /**
     * Checks whether this value changed for this object.
     *
     * @return true if the logged value changed since this object last checked
     */
    public boolean hasChanged() {
        return hasChanged(hashCode());
    }

    /**
     * Gets the latest tuned measure value.
     *
     * @return immutable copy of the current value
     */
    public BaseMeasureType get() {
        checkForUpdate();
        return value.copy();
    }

    /**
     * Sets the logged tunable value from a measure.
     *
     * @param newValue new measure value
     */
    public void set(BaseMeasureType newValue) {
        value.mut_replace(newValue);
        tunableNumber.setValue((value.in(displayedUnit)));
    }

    @FunctionalInterface
    public interface MeasureConsumer<
            M extends MutableMeasure<U, B, M>, B extends Measure<U>, U extends Unit> {
        /**
         * Accepts an updated measure value.
         *
         * @param newValue updated mutable measure
         */
        void accept(M newValue);

        /**
         * Chains another measure consumer after this one.
         *
         * @param after consumer to run after this one
         * @return combined consumer
         */
        default MeasureConsumer<M, B, U> chain(MeasureConsumer<M, B, U> after) {
            return (M newValue) -> {
                this.accept(newValue);
                after.accept(newValue);
            };
        }
    }

    public static class LoggedTunableMeasureFactory<
            M extends MutableMeasure<U, B, M>,
            B extends Measure<U>,
            U extends Unit,
            S extends LoggedTunableMeasure<M, B, U>> {

        LoggedTunableMeasureFactoryFunction<M, B, U, S> factoryFunction;

        /**
         * Creates a factory for a specific logged measure type.
         *
         * @param factoryFunction constructor-like function for the logged measure type
         */
        public LoggedTunableMeasureFactory(
                LoggedTunableMeasureFactoryFunction<M, B, U, S> factoryFunction) {
            this.factoryFunction = factoryFunction;
        }

        /**
         * Creates a logged measure with explicit display-unit path behavior.
         *
         * @param name logged tunable path
         * @param defaultValue default mutable measure
         * @param displayedUnit unit used for logging and tuning
         * @param addUnitSuffix whether to append the unit name to the path
         * @return logged tunable measure
         */
        public S of(String name, M defaultValue, U displayedUnit, boolean addUnitSuffix) {
            return factoryFunction.apply(name, defaultValue, displayedUnit, addUnitSuffix);
        }

        /**
         * Creates a logged measure with an explicit display unit.
         *
         * @param name logged tunable path
         * @param defaultValue default mutable measure
         * @param displayedUnit unit used for logging and tuning
         * @return logged tunable measure
         */
        public S of(String name, M defaultValue, U displayedUnit) {
            return of(name, defaultValue, displayedUnit, false);
        }

        /**
         * Creates a logged measure using the default value's unit.
         *
         * @param name logged tunable path
         * @param defaultValue default mutable measure
         * @return logged tunable measure
         */
        public S of(String name, M defaultValue) {
            return of(name, defaultValue, defaultValue.unit());
        }

        /**
         * Creates a logged measure from an immutable default value.
         *
         * @param name logged tunable path
         * @param defaultValue default measure
         * @return logged tunable measure
         */
        public S of(String name, B defaultValue) {
            return of(name, defaultValue, defaultValue.unit());
        }

        /**
         * Creates a logged measure from an immutable default value and display unit.
         *
         * @param name logged tunable path
         * @param defaultValue default measure
         * @param displayedUnit unit used for logging and tuning
         * @return logged tunable measure
         */
        public S of(String name, B defaultValue, U displayedUnit) {
            return of(name, defaultValue, displayedUnit, false);
        }

        /**
         * Creates a logged measure from an immutable default value with path suffix control.
         *
         * @param name logged tunable path
         * @param defaultValue default measure
         * @param displayedUnit unit used for logging and tuning
         * @param addUnitSuffix whether to append the unit name to the path
         * @return logged tunable measure
         */
        @SuppressWarnings("unchecked")
        public S of(String name, B defaultValue, U displayedUnit, boolean addUnitSuffix) {
            return of(name, (M) defaultValue.mutableCopy(), displayedUnit, addUnitSuffix);
        }

        /**
         * Creates a logged measure from a raw value in the displayed unit.
         *
         * @param name logged tunable path
         * @param defaultValue default raw value
         * @param displayedUnit unit used for the raw value
         * @return logged tunable measure
         */
        @SuppressWarnings("unchecked")
        public S of(String name, double defaultValue, U displayedUnit) {
            return of(name, (M) displayedUnit.mutable(defaultValue), displayedUnit);
        }

        @FunctionalInterface
        public interface LoggedTunableMeasureFactoryFunction<
                M extends MutableMeasure<U, B, M>,
                B extends Measure<U>,
                U extends Unit,
                S extends LoggedTunableMeasure<M, B, U>> {
            /**
             * Creates the typed logged measure.
             *
             * @param name logged tunable path
             * @param defaultValue default mutable measure
             * @param displayedUnit unit used for logging and tuning
             * @param addUnitSuffix whether to append the unit name to the path
             * @return logged tunable measure
             */
            S apply(String name, M defaultValue, U displayedUnit, boolean addUnitSuffix);
        }
    }

    public static class LoggedAngle extends LoggedTunableMeasure<MutAngle, Angle, AngleUnit> {
        public LoggedAngle(
                String name,
                MutAngle defaultValue,
                AngleUnit displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedAngularAcceleration
            extends LoggedTunableMeasure<
                    MutAngularAcceleration, AngularAcceleration, AngularAccelerationUnit> {
        public LoggedAngularAcceleration(
                String name,
                MutAngularAcceleration defaultValue,
                AngularAccelerationUnit displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedAngularMomentum
            extends LoggedTunableMeasure<MutAngularMomentum, AngularMomentum, AngularMomentumUnit> {
        public LoggedAngularMomentum(
                String name,
                MutAngularMomentum defaultValue,
                AngularMomentumUnit displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedAngularVelocity
            extends LoggedTunableMeasure<MutAngularVelocity, AngularVelocity, AngularVelocityUnit> {
        public LoggedAngularVelocity(
                String name,
                MutAngularVelocity defaultValue,
                AngularVelocityUnit displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedCurrent
            extends LoggedTunableMeasure<MutCurrent, Current, CurrentUnit> {
        public LoggedCurrent(
                String name,
                MutCurrent defaultValue,
                CurrentUnit displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedDimensionless
            extends LoggedTunableMeasure<MutDimensionless, Dimensionless, DimensionlessUnit> {
        public LoggedDimensionless(
                String name,
                MutDimensionless defaultValue,
                DimensionlessUnit displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedDistance
            extends LoggedTunableMeasure<MutDistance, Distance, DistanceUnit> {
        public LoggedDistance(
                String name,
                MutDistance defaultValue,
                DistanceUnit displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedEnergy extends LoggedTunableMeasure<MutEnergy, Energy, EnergyUnit> {
        public LoggedEnergy(
                String name,
                MutEnergy defaultValue,
                EnergyUnit displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedForce extends LoggedTunableMeasure<MutForce, Force, ForceUnit> {
        public LoggedForce(
                String name,
                MutForce defaultValue,
                ForceUnit displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedFrequency
            extends LoggedTunableMeasure<MutFrequency, Frequency, FrequencyUnit> {
        public LoggedFrequency(
                String name,
                MutFrequency defaultValue,
                FrequencyUnit displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedLinearAcceleration
            extends LoggedTunableMeasure<
                    MutLinearAcceleration, LinearAcceleration, LinearAccelerationUnit> {
        public LoggedLinearAcceleration(
                String name,
                MutLinearAcceleration defaultValue,
                LinearAccelerationUnit displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedLinearMomentum
            extends LoggedTunableMeasure<MutLinearMomentum, LinearMomentum, LinearMomentumUnit> {
        public LoggedLinearMomentum(
                String name,
                MutLinearMomentum defaultValue,
                LinearMomentumUnit displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedLinearVelocity
            extends LoggedTunableMeasure<MutLinearVelocity, LinearVelocity, LinearVelocityUnit> {
        public LoggedLinearVelocity(
                String name,
                MutLinearVelocity defaultValue,
                LinearVelocityUnit displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedMass extends LoggedTunableMeasure<MutMass, Mass, MassUnit> {
        public LoggedMass(
                String name, MutMass defaultValue, MassUnit displayedUnit, boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedMomentOfInertia
            extends LoggedTunableMeasure<MutMomentOfInertia, MomentOfInertia, MomentOfInertiaUnit> {
        public LoggedMomentOfInertia(
                String name,
                MutMomentOfInertia defaultValue,
                MomentOfInertiaUnit displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedPower extends LoggedTunableMeasure<MutPower, Power, PowerUnit> {
        public LoggedPower(
                String name,
                MutPower defaultValue,
                PowerUnit displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedResistance
            extends LoggedTunableMeasure<MutResistance, Resistance, ResistanceUnit> {
        public LoggedResistance(
                String name,
                MutResistance defaultValue,
                ResistanceUnit displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedTemperature
            extends LoggedTunableMeasure<MutTemperature, Temperature, TemperatureUnit> {
        public LoggedTemperature(
                String name,
                MutTemperature defaultValue,
                TemperatureUnit displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedTime extends LoggedTunableMeasure<MutTime, Time, TimeUnit> {
        public LoggedTime(
                String name, MutTime defaultValue, TimeUnit displayedUnit, boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedTorque extends LoggedTunableMeasure<MutTorque, Torque, TorqueUnit> {
        public LoggedTorque(
                String name,
                MutTorque defaultValue,
                TorqueUnit displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedVoltage
            extends LoggedTunableMeasure<MutVoltage, Voltage, VoltageUnit> {
        public LoggedVoltage(
                String name,
                MutVoltage defaultValue,
                VoltageUnit displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static LoggedTunableMeasureFactory<MutAngle, Angle, AngleUnit, LoggedAngle> ANGLE =
            new LoggedTunableMeasureFactory<>(LoggedAngle::new);
    public static LoggedTunableMeasureFactory<
                    MutAngularAcceleration,
                    AngularAcceleration,
                    AngularAccelerationUnit,
                    LoggedAngularAcceleration>
            ANGULAR_ACCELERATION =
                    new LoggedTunableMeasureFactory<>(LoggedAngularAcceleration::new);
    public static LoggedTunableMeasureFactory<
                    MutAngularMomentum, AngularMomentum, AngularMomentumUnit, LoggedAngularMomentum>
            ANGULAR_MOMENTUM = new LoggedTunableMeasureFactory<>(LoggedAngularMomentum::new);
    public static LoggedTunableMeasureFactory<
                    MutAngularVelocity, AngularVelocity, AngularVelocityUnit, LoggedAngularVelocity>
            ANGULAR_VELOCITY = new LoggedTunableMeasureFactory<>(LoggedAngularVelocity::new);
    public static LoggedTunableMeasureFactory<MutCurrent, Current, CurrentUnit, LoggedCurrent>
            CURRENT = new LoggedTunableMeasureFactory<>(LoggedCurrent::new);
    public static LoggedTunableMeasureFactory<
                    MutDimensionless, Dimensionless, DimensionlessUnit, LoggedDimensionless>
            DIMENSIONLESS = new LoggedTunableMeasureFactory<>(LoggedDimensionless::new);
    public static LoggedTunableMeasureFactory<MutDistance, Distance, DistanceUnit, LoggedDistance>
            DISTANCE = new LoggedTunableMeasureFactory<>(LoggedDistance::new);
    public static LoggedTunableMeasureFactory<MutEnergy, Energy, EnergyUnit, LoggedEnergy> ENERGY =
            new LoggedTunableMeasureFactory<>(LoggedEnergy::new);
    public static LoggedTunableMeasureFactory<MutForce, Force, ForceUnit, LoggedForce> FORCE =
            new LoggedTunableMeasureFactory<>(LoggedForce::new);
    public static LoggedTunableMeasureFactory<
                    MutFrequency, Frequency, FrequencyUnit, LoggedFrequency>
            FREQUENCY = new LoggedTunableMeasureFactory<>(LoggedFrequency::new);
    public static LoggedTunableMeasureFactory<
                    MutLinearAcceleration,
                    LinearAcceleration,
                    LinearAccelerationUnit,
                    LoggedLinearAcceleration>
            LINEAR_ACCELERATION = new LoggedTunableMeasureFactory<>(LoggedLinearAcceleration::new);
    public static LoggedTunableMeasureFactory<
                    MutLinearMomentum, LinearMomentum, LinearMomentumUnit, LoggedLinearMomentum>
            LINEAR_MOMENTUM = new LoggedTunableMeasureFactory<>(LoggedLinearMomentum::new);
    public static LoggedTunableMeasureFactory<
                    MutLinearVelocity, LinearVelocity, LinearVelocityUnit, LoggedLinearVelocity>
            LINEAR_VELOCITY = new LoggedTunableMeasureFactory<>(LoggedLinearVelocity::new);
    public static LoggedTunableMeasureFactory<MutMass, Mass, MassUnit, LoggedMass> MASS =
            new LoggedTunableMeasureFactory<>(LoggedMass::new);
    public static LoggedTunableMeasureFactory<
                    MutMomentOfInertia, MomentOfInertia, MomentOfInertiaUnit, LoggedMomentOfInertia>
            MOMENT_OF_INERTIA = new LoggedTunableMeasureFactory<>(LoggedMomentOfInertia::new);
    public static LoggedTunableMeasureFactory<MutPower, Power, PowerUnit, LoggedPower> POWER =
            new LoggedTunableMeasureFactory<>(LoggedPower::new);
    public static LoggedTunableMeasureFactory<
                    MutResistance, Resistance, ResistanceUnit, LoggedResistance>
            RESISTANCE = new LoggedTunableMeasureFactory<>(LoggedResistance::new);
    public static LoggedTunableMeasureFactory<
                    MutTemperature, Temperature, TemperatureUnit, LoggedTemperature>
            TEMPERATURE = new LoggedTunableMeasureFactory<>(LoggedTemperature::new);
    public static LoggedTunableMeasureFactory<MutTime, Time, TimeUnit, LoggedTime> TIME =
            new LoggedTunableMeasureFactory<>(LoggedTime::new);
    public static LoggedTunableMeasureFactory<MutTorque, Torque, TorqueUnit, LoggedTorque> TORQUE =
            new LoggedTunableMeasureFactory<>(LoggedTorque::new);
    public static LoggedTunableMeasureFactory<MutVoltage, Voltage, VoltageUnit, LoggedVoltage>
            VOLTAGE = new LoggedTunableMeasureFactory<>(LoggedVoltage::new);

    public static class LoggedAngularJerk
            extends LoggedTunableMeasure<
                    MutVelocity<AngularAccelerationUnit>,
                    Velocity<AngularAccelerationUnit>,
                    VelocityUnit<AngularAccelerationUnit>> {
        public LoggedAngularJerk(
                String name,
                MutVelocity<AngularAccelerationUnit> defaultValue,
                VelocityUnit<AngularAccelerationUnit> displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedVoltagePerAngularVelocity
            extends LoggedTunableMeasure<
                    MutPer<VoltageUnit, AngularVelocityUnit>,
                    Per<VoltageUnit, AngularVelocityUnit>,
                    PerUnit<VoltageUnit, AngularVelocityUnit>> {
        public LoggedVoltagePerAngularVelocity(
                String name,
                MutPer<VoltageUnit, AngularVelocityUnit> defaultValue,
                PerUnit<VoltageUnit, AngularVelocityUnit> displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedVoltagePerAngularAcceleration
            extends LoggedTunableMeasure<
                    MutPer<VoltageUnit, AngularAccelerationUnit>,
                    Per<VoltageUnit, AngularAccelerationUnit>,
                    PerUnit<VoltageUnit, AngularAccelerationUnit>> {
        public LoggedVoltagePerAngularAcceleration(
                String name,
                MutPer<VoltageUnit, AngularAccelerationUnit> defaultValue,
                PerUnit<VoltageUnit, AngularAccelerationUnit> displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static LoggedTunableMeasureFactory<
                    MutVelocity<AngularAccelerationUnit>,
                    Velocity<AngularAccelerationUnit>,
                    VelocityUnit<AngularAccelerationUnit>,
                    LoggedAngularJerk>
            ANGULAR_JERK = new LoggedTunableMeasureFactory<>(LoggedAngularJerk::new);
    public static LoggedTunableMeasureFactory<
                    MutPer<VoltageUnit, AngularVelocityUnit>,
                    Per<VoltageUnit, AngularVelocityUnit>,
                    PerUnit<VoltageUnit, AngularVelocityUnit>,
                    LoggedVoltagePerAngularVelocity>
            VOLTAGE_PER_ANGULAR_VELOCITY =
                    new LoggedTunableMeasureFactory<>(LoggedVoltagePerAngularVelocity::new);
    public static LoggedTunableMeasureFactory<
                    MutPer<VoltageUnit, AngularAccelerationUnit>,
                    Per<VoltageUnit, AngularAccelerationUnit>,
                    PerUnit<VoltageUnit, AngularAccelerationUnit>,
                    LoggedVoltagePerAngularAcceleration>
            VOLTAGE_PER_ANGULAR_ACCELERATION =
                    new LoggedTunableMeasureFactory<>(LoggedVoltagePerAngularAcceleration::new);
}
