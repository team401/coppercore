package coppercore.wpilib_interface.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import coppercore.wpilib_interface.alliance_util.AllianceUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.wpilib.hardware.hal.AllianceStationID;
import org.wpilib.hardware.hal.HAL;
import org.wpilib.driverstation.Alliance;
import org.wpilib.simulation.DriverStationSim;

// This was made with Codex 5.5 Medium.
public class AllianceUtilTests {

    @BeforeAll
    public static void initializeHal() {
        HAL.initialize(500, 0);
    }

    @BeforeEach
    public void resetDriverStationSim() {
        setAllianceStation(AllianceStationID.UNKNOWN);
    }

    private void setAllianceStation(AllianceStationID allianceStationId) {
        DriverStationSim.setAllianceStationId(allianceStationId);
        DriverStationSim.notifyNewData();
    }

    @Test
    public void getAllianceDefaultsToRedWhenUnknown() {
        setAllianceStation(AllianceStationID.UNKNOWN);

        assertEquals(Alliance.RED, AllianceUtil.getAlliance());
        assertTrue(AllianceUtil.isRed());
        assertEquals(Alliance.BLUE, AllianceUtil.getOppAlliance());
    }

    @Test
    public void getAllianceReturnsRedFromDriverStation() {
        setAllianceStation(AllianceStationID.RED_2);

        assertEquals(Alliance.RED, AllianceUtil.getAlliance());
        assertTrue(AllianceUtil.isRed());
        assertEquals(Alliance.BLUE, AllianceUtil.getOppAlliance());
    }

    @Test
    public void getAllianceReturnsBlueFromDriverStation() {
        setAllianceStation(AllianceStationID.BLUE_3);

        assertEquals(Alliance.BLUE, AllianceUtil.getAlliance());
        assertFalse(AllianceUtil.isRed());
        assertEquals(Alliance.RED, AllianceUtil.getOppAlliance());
    }
}
