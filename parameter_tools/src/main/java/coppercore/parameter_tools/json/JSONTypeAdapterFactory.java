package coppercore.parameter_tools.json;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import coppercore.parameter_tools.json.adapters.measure.JSONMeasure;
import edu.wpi.first.units.Measure;

public class JSONTypeAdapterFactory implements TypeAdapterFactory {

    private JSONSyncConfig config = new JSONSyncConfigBuilder().build();

    public JSONTypeAdapterFactory(JSONSyncConfig config) {
        this.config = config;
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, com.google.gson.reflect.TypeToken<T> type) {
        @SuppressWarnings("unchecked")
        Class<T> rawType = (Class<T>) type.getRawType();
        if (config.primitiveChecking()) {
            if (type.getRawType() == int.class
                    || type.getRawType() == double.class
                    || type.getRawType() == float.class
                    || type.getRawType() == long.class
                    || type.getRawType() == short.class
                    || type.getRawType() == char.class
                    || type.getRawType() == byte.class
                    || type.getRawType() == boolean.class) {
                if (config.primitiveCheckPrintAlert()) {
                    Thread thread = new Thread(new JSONPrimativeErrorAlert() {});
                    thread.start();
                }
                if (config.primitiveCheckCrash()) {
                    throw new RuntimeException("You used primitive");
                }
            }
        }
        if (!JSONConverter.has(rawType)) {
            return null;
        }

        return new TypeAdapter<T>() {

            @SuppressWarnings("unchecked")
            @Override
            public T read(JsonReader reader) throws IOException {
                try {
                    return ((JSONObject<T>) gson.fromJson(reader, JSONConverter.convert(rawType)))
                            .toJava();
                } catch (ClassCastException e) {
                    System.err.println("Could not find JavaObject for " + rawType.getName());
                }
                return null;
            }

            @Override
            public void write(JsonWriter out, T value) throws IOException {
                try {
                    Class jsonObject = JSONConverter.convert(rawType);
                    // This only exists because of Units.
                    if (jsonObject != JSONMeasure.class) {
                        gson.toJson(
                                jsonObject.getConstructor(rawType).newInstance(value),
                                jsonObject,
                                out);
                    } else {
                        gson.toJson(
                                jsonObject.getConstructor(Measure.class).newInstance(value),
                                jsonObject,
                                out);
                    }
                } catch (IllegalAccessException
                        | IllegalArgumentException
                        | InstantiationException
                        | NoSuchMethodException
                        | SecurityException
                        | InvocationTargetException e) {
                }
            }
        };
    }
}
