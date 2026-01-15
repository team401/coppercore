package coppercore.parameter_tools.json;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import coppercore.parameter_tools.json.helpers.JSONConverter;
import coppercore.parameter_tools.json.helpers.JSONObject;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A factory for creating TypeAdapters that convert between Java objects and their JSON
 * representations
 */
public class JSONTypeAdapterFactory implements TypeAdapterFactory {

    private JSONSyncConfig config;

    public JSONTypeAdapterFactory(JSONSyncConfig config) {
        this.config = config;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, com.google.gson.reflect.TypeToken<T> type) {
        Class<T> rawType = (Class<T>) type.getRawType();
        Class<? extends JSONObject<T>> jsonObject;
        try {
            jsonObject = JSONConverter.convert(rawType);
        } catch (JSONConverter.ConversionException e) {
            return null;
        }
        if (jsonObject == null) {
            return null;
        }

        Constructor constructor;
        try {
            Method getConstructor = jsonObject.getMethod("getConstructor");
            constructor = (Constructor<? extends JSONObject<T>>) getConstructor.invoke(null);
        } catch (NoSuchMethodException
                | IllegalAccessException
                | IllegalArgumentException
                | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }

        return new TypeAdapter<T>() {

            @SuppressWarnings("unchecked")
            @Override
            public T read(JsonReader reader) throws IOException {
                try {
                    return ((JSONObject<T>) gson.fromJson(reader, jsonObject)).toJava();
                } catch (ClassCastException e) {
                    System.err.println("Could not find JavaObject for " + rawType.getName());
                }
                return null;
            }

            @SuppressWarnings("unchecked")
            @Override
            public void write(JsonWriter out, T value) throws IOException {
                try {
                    gson.toJson(constructor.newInstance(value), jsonObject, out);
                } catch (IllegalAccessException
                        | IllegalArgumentException
                        | InstantiationException
                        | SecurityException
                        | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
