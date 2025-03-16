package coppercore.parameter_tools.json;

public interface JSONPrimativeErrorAlert extends Runnable {

    @Override
    public default void run() {
        while (true) {
            System.out.println("You used primitive");
            try {
                Thread t =
                        new Thread(
                                () -> {
                                    throw new RuntimeException("You used primitive: 1");
                                },
                                "JSONPrimativeErrorAlertThread");
                t.run();
            } finally {
            }
        }
    }
}
