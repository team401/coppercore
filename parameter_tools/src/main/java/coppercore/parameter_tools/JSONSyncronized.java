package coppercore.parameter_tools;

public interface JSONSyncronized<clazz> {
    JSONSync<clazz> getJSONSync();

    clazz getObject();
}
