package coppercore.wpilib_interface.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import coppercore.wpilib_interface.alliance_util.AllianceUtil;
import edu.wpi.first.hal.AllianceStationID;
import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
