package coppercore.parameter_tools;

public abstract class JSONObject<T> {
    protected JSONObject(T object) {}

    public abstract T toJava();
}
