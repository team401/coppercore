import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import coppercore.wpilib_interface.subsystems.configs.CANDeviceID;
import coppercore.wpilib_interface.subsystems.configs.MechanismConfig;
import coppercore.wpilib_interface.subsystems.configs.MechanismConfig.GravityFeedforwardType;
import org.junit.Test;

/**
 * Tests for the MechanismConfig to ensure that its builder correctly validates and stores
 * information.
 */
public class MechanismConfigTests {
    @Test
    public void nameMustBeNonNull() {
        assertThrows(
                NullPointerException.class,
                () -> {
                    MechanismConfig.builder().withName(null);
                });
    }

    @Test
    public void leadMotorIDMustBeNonNull() {
        assertThrows(
                NullPointerException.class,
                () -> {
                    MechanismConfig.builder().withLeadMotorId(null);
                });
    }

    @Test
    public void leadMotorIDBusMustBeNonNull() {
        assertThrows(
                NullPointerException.class,
                () -> {
                    MechanismConfig.builder().withLeadMotorId(new CANDeviceID(null, 1));
                });
    }

    @Test
    public void followerMotorIDMustBeNonNull() {
        assertThrows(
                NullPointerException.class,
                () -> {
                    MechanismConfig.builder().addFollower(null, false);
                });
    }

    @Test
    public void followerMotorIDBusMustBeNonNull() {
        assertThrows(
                NullPointerException.class,
                () -> {
                    MechanismConfig.builder().addFollower(new CANDeviceID(null, 1), false);
                });
    }

    @Test
    public void TestFeedforwardTypeMustBeNonNull() {
        assertThrows(
                NullPointerException.class,
                () -> {
                    MechanismConfig.builder().withGravityFeedforwardType(null);
                });
    }

    @Test
    public void nameMustBeConfigured() {
        assertThrows(
                NullPointerException.class,
                () -> {
                    MechanismConfig.builder()
                            .withLeadMotorId(new CANDeviceID("rio", 1))
                            .withGravityFeedforwardType(GravityFeedforwardType.STATIC_ELEVATOR)
                            .build();
                });
    }

    @Test
    public void leadMotorIDMustBeConfigured() {
        assertThrows(
                NullPointerException.class,
                () -> {
                    MechanismConfig.builder()
                            .withName("TestMechanism")
                            .withGravityFeedforwardType(GravityFeedforwardType.STATIC_ELEVATOR)
                            .build();
                });
    }

    @Test
    public void feedforwardTypeMustBeConfigured() {
        assertThrows(
                NullPointerException.class,
                () -> {
                    MechanismConfig.builder()
                            .withName("TestMechanism")
                            .withLeadMotorId(new CANDeviceID("rio", 1))
                            .build();
                });
    }

    @Test
    public void dataIsCorrect() {
        MechanismConfig config =
                MechanismConfig.builder()
                        .withName("TestMechanism")
                        .withLeadMotorId(new CANDeviceID("rio", 11))
                        .withGravityFeedforwardType(GravityFeedforwardType.STATIC_ELEVATOR)
                        .addFollower(new CANDeviceID("rio", 12), false)
                        .addFollower(new CANDeviceID("rio", 13), true)
                        .build();

        assertEquals(config.name, "TestMechanism");
        assertEquals(config.leadMotorId, new CANDeviceID("rio", 11));
        assertEquals(config.gravityFeedforwardType, GravityFeedforwardType.STATIC_ELEVATOR);
        assertEquals(config.followerMotorConfigs.length, 2);
        assertEquals(config.followerMotorConfigs[0].id(), new CANDeviceID("rio", 12));
        assertEquals(config.followerMotorConfigs[1].id(), new CANDeviceID("rio", 13));
    }
}
