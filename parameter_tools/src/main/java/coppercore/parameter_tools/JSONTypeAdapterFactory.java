package coppercore.parameter_tools;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class JSONTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(Gson gson, com.google.gson.reflect.TypeToken<T> type) {
        @SuppressWarnings("unchecked")
        Class<T> rawType = (Class<T>) type.getRawType();
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
                    gson.toJson(
                            JSONConverter.convert(rawType)
                                    .getConstructor(rawType)
                                    .newInstance(value),
                            JSONConverter.convert(rawType),
                            out);
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
