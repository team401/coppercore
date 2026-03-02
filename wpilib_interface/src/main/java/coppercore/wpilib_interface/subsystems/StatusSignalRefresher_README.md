# StatusSignalRefresher Credit/Attribution

Team 422 was kind enough to provide us access to their CtreBaseRefreshsManager class:

```java
package frc.robot.util;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.CANBus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.littletonrobotics.junction.Logger;

public class CtreBaseRefreshManager {
  // this is a static class and may not be instantiated
  private CtreBaseRefreshManager() {}

  private static Map<CANBus, List<BaseStatusSignal>> m_signalMap = new HashMap<>();

  /**
   * Updates all of the signals in the manager. This must be called BEFORE {@code
   * CommandScheduler.getInstance().run()} in the robot periodic to work properly.
   */
  public static void updateAll() {
    if (m_signalMap.isEmpty()) {
      return;
    }

    for (CANBus bus : m_signalMap.keySet()) {
      // Refresh all signals on this bus and log the resulting status code
      var status = BaseStatusSignal.refreshAll(m_signalMap.get(bus));
      Logger.recordOutput("CtreBaseRefreshManager/StatusCode/" + bus, status);
    }
  }

  // Add a list of signals to be refreshed on a given bus
  public static void addSignals(CANBus bus, List<BaseStatusSignal> signals) {
    if (!m_signalMap.containsKey(bus)) {
      m_signalMap.put(bus, new ArrayList<>());
    }
    m_signalMap.get(bus).addAll(signals);
  }

  // Add a single signal to be refreshed on a given bus
  public static void addSignal(CANBus bus, BaseStatusSignal signal) {
    if (!m_signalMap.containsKey(bus)) {
      m_signalMap.put(bus, new ArrayList<>());
    }
    m_signalMap.get(bus).add(signal);
  }
}
```

We received permission to publish this code with attribution. The StatusSignalRefresher class in this directory is based on their CtreBaseRefreshManager, with some fields renamed to match the rest of 401's code style standards. All changes that make the file differ from the code block above were made by Team 401.