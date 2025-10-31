package coppercore.parameter_tools.json.helpers;

import java.lang.reflect.Constructor;

/**
 * A base class for JSON objects that can be converted to a Java object of type T.
 *
 * @param <T> the type of the Java object that this JSON object represents
 */
public abstract class JSONObject<T> {
    /**
     * Constructor for creating a JSONObject from a Java object of type T.
     *
     * @param object the Java object to convert to a JSON object
     */
    protected JSONObject(T object) {}

    /**
     * Converts this JSON object to a Java object of type T.
     *
     * @return the Java object represented by this JSON object
     */
    public abstract T toJava();

    /**
     * Gets the constructor of the json wrapper Must be reimplemented
     *
     * @return the json wrapper constructor
     */
    public static Constructor<? extends JSONObject<?>> getConstructor()
            throws NoSuchMethodException {
        throw new RuntimeException("getConstructor method not implemented");
    }
    ;
}
