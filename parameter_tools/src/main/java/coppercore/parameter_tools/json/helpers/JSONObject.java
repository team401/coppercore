package coppercore.parameter_tools.json.helpers;

/**
 * A base class for JSON objects that can be converted to a Java object of type T.
 *
 * @param <T> the type of the Java object that this JSON object represents
 */
public abstract class JSONObject<T> {
    protected JSONObject(T object) {}

    public abstract T toJava();
}
