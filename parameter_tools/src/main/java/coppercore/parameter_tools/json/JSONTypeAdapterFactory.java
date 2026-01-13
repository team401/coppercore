package coppercore.parameter_tools.json;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import coppercore.parameter_tools.json.annotations.AfterJsonLoad;
import coppercore.parameter_tools.json.helpers.JSONConverter;
import coppercore.parameter_tools.json.helpers.JSONObject;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A factory for creating TypeAdapters that convert between Java objects and their JSON
 * representations
 */
public class JSONTypeAdapterFactory implements TypeAdapterFactory {

    private JSONSyncConfig config;

    private List<Class<?>> jsonAfterLoadClasses = new ArrayList<>();

    public JSONTypeAdapterFactory(JSONSyncConfig config) {
        this.config = config;
    }

    public boolean needsAfterLoadAdapter(Class<?> rawType) {

        if (jsonAfterLoadClasses.contains(rawType)) {
            return false;
        }

        Method[] methods = rawType.getMethods();
        var filteredMethods =
                Arrays.stream(methods)
                        .filter((Method method) -> method.isAnnotationPresent(AfterJsonLoad.class))
                        .toList();
        return filteredMethods.size() > 0;
    }

    public <T> TypeAdapter<T> wrapAdapterWithAfterLoad(TypeAdapter<T> adapter) {
        return new TypeAdapter<T>() {

            @Override
            public T read(JsonReader reader) throws IOException {
                T obj = adapter.read(reader);
                if (obj != null) {
                    Class<?> clazz = obj.getClass();
                    Method[] methods = clazz.getMethods();
                    var filteredMethods =
                            Arrays.stream(methods)
                                    .filter(
                                            (Method method) ->
                                                    method.isAnnotationPresent(AfterJsonLoad.class))
                                    .toList();
                    if (filteredMethods.size() > 1) {
                        throw new RuntimeException(
                                "Multiple AfterJsonLoad annotations on methods in one class");
                    }
                    filteredMethods.forEach(
                            (method) -> {
                                try {
                                    method.invoke(obj);
                                } catch (IllegalAccessException
                                        | IllegalArgumentException
                                        | InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            });
                }
                return obj;
            }

            @Override
            public void write(JsonWriter out, T value) throws IOException {
                adapter.write(out, value);
            }
        };
    }

    public <T> TypeAdapter<T> getAfterLoadTypeAdapter(
            Gson gson, Class<T> rawType, com.google.gson.reflect.TypeToken<T> typeToken) {
        if (jsonAfterLoadClasses.contains(rawType)) {
            // Already have AfterJsonLoad implementation
            return null;
        }

        // Implementation for AfterJsonLoad TypeAdapter if needed
        jsonAfterLoadClasses.add(rawType);

        // Get the correct TypeAdapter from the gson instance
        var innerAdapter = gson.getDelegateAdapter(this, typeToken);

        return wrapAdapterWithAfterLoad(innerAdapter);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, com.google.gson.reflect.TypeToken<T> type) {

        Class<T> rawType = (Class<T>) type.getRawType();

        Class<? extends JSONObject<T>> jsonObject;
        try {
            jsonObject = JSONConverter.convert(rawType);
        } catch (JSONConverter.ConversionException e) {
            if (needsAfterLoadAdapter(rawType)) {
                return getAfterLoadTypeAdapter(gson, rawType, type);
            }
            return null;
        }
        if (jsonObject == null) {
            if (needsAfterLoadAdapter(rawType)) {
                return getAfterLoadTypeAdapter(gson, rawType, type);
            }
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
            if (needsAfterLoadAdapter(rawType)) {
                return getAfterLoadTypeAdapter(gson, rawType, type);
            }
            return null;
        }

        TypeAdapter<T> newTypeAdapter =
                new TypeAdapter<T>() {

                    @SuppressWarnings("unchecked")
                    @Override
                    public T read(JsonReader reader) throws IOException {
                        try {
                            return ((JSONObject<T>) gson.fromJson(reader, jsonObject)).toJava();
                        } catch (ClassCastException e) {
                            System.err.println(
                                    "Could not find JavaObject for " + rawType.getName());
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
        if (needsAfterLoadAdapter(rawType)) {
            return wrapAdapterWithAfterLoad(newTypeAdapter);
        }
        return newTypeAdapter;
    }
}
