package coppercore.parameter_tools.json.helpers;

public abstract class JSONObject<T> {
    protected JSONObject(T object) {}

    public abstract T toJava();
}
