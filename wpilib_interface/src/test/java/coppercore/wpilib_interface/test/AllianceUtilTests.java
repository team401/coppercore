package coppercore.wpilib_interface.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import coppercore.wpilib_interface.alliance_util.AllianceUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.wpilib.hal.AllianceStationID;
import org.wpilib.hal.HAL;
import org.wpilib.wpilibj.DriverStation.Alliance;
import org.wpilib.wpilibj.simulation.DriverStationSim;

// This was made with Codex 5.5 Medium.
public class AllianceUtilTests {

    @BeforeAll
    public static void initializeHal() {
        HAL.initialize(500, 0);
    }

    @BeforeEach
    public void resetDriverStationSim() {
        setAllianceStation(AllianceStationID.Unknown);
    }

    private void setAllianceStation(AllianceStationID allianceStationId) {
        DriverStationSim.setAllianceStationId(allianceStationId);
        DriverStationSim.notifyNewData();
    }

    @Test
    public void getAllianceDefaultsToRedWhenUnknown() {
        setAllianceStation(AllianceStationID.Unknown);

        assertEquals(Alliance.Red, AllianceUtil.getAlliance());
        assertTrue(AllianceUtil.isRed());
        assertEquals(Alliance.Blue, AllianceUtil.getOppAlliance());
    }

    @Test
    public void getAllianceReturnsRedFromDriverStation() {
        setAllianceStation(AllianceStationID.Red2);

        assertEquals(Alliance.Red, AllianceUtil.getAlliance());
        assertTrue(AllianceUtil.isRed());
        assertEquals(Alliance.Blue, AllianceUtil.getOppAlliance());
    }

    @Test
    public void getAllianceReturnsBlueFromDriverStation() {
        setAllianceStation(AllianceStationID.Blue3);

        assertEquals(Alliance.Blue, AllianceUtil.getAlliance());
        assertFalse(AllianceUtil.isRed());
        assertEquals(Alliance.Red, AllianceUtil.getOppAlliance());
    }
}
