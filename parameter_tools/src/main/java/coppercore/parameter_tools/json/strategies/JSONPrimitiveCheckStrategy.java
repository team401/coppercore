package coppercore.parameter_tools.json.strategies;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;
import coppercore.parameter_tools.json.JSONSyncConfig;
import coppercore.parameter_tools.json.JSONSyncConfigBuilder;
import coppercore.parameter_tools.json.annotations.JSONName;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class JSONPrimitiveCheckStrategy {

    public static FieldNamingStrategy checkForPrimitives(
            FieldNamingStrategy strategy, JSONSyncConfig config) {
        return new JSONPrimitiveCheckStrategyHelper(strategy, config);
    }

    private static class JSONPrimitiveCheckStrategyHelper implements FieldNamingStrategy {

        protected FieldNamingStrategy strategy = FieldNamingPolicy.IDENTITY;
        protected JSONSyncConfig config = new JSONSyncConfigBuilder().build();

        public JSONPrimitiveCheckStrategyHelper() {}

        public JSONPrimitiveCheckStrategyHelper(
                FieldNamingStrategy strategy, JSONSyncConfig config) {
            this.strategy = strategy;
            this.config = config;
        }

        @Override
        public String translateName(Field f) {
            if (this.config.primitiveChecking()) {
                JSONPrimitiveCheckStrategy.handlePrimitive(f, this.config);
            }
            return this.strategy.translateName(f);
        }
    }

    protected static void handlePrimitive(Field field, JSONSyncConfig config) {
        JSONName annotation = field.getAnnotation(JSONName.class);
        Class<?> type = field.getType();
        int fieldModifiers = field.getModifiers();

        // Check if type is primitive
        if (!type.isPrimitive()) {
            return;
        }

        // Check if is record
        if (field.getDeclaringClass().isRecord()) {
            return;
        }

        // Check if field is final
        if (!Modifier.isFinal(fieldModifiers)) {
            return;
        }

        Class<?> declaringClass = field.getDeclaringClass();

        // Build warning/error message
        StringBuilder messageBuilder = new StringBuilder();

        messageBuilder.append("Primitive Used: ");
        messageBuilder.append('\n');

        messageBuilder.append("In Class: ");
        messageBuilder.append(declaringClass.getName());
        messageBuilder.append('\n');

        messageBuilder.append("Declaring Class Super Class");
        messageBuilder.append(declaringClass.getSuperclass().getName());
        messageBuilder.append('\n');

        messageBuilder.append("Field Name: ");
        messageBuilder.append(field.getName());
        messageBuilder.append('\n');

        messageBuilder.append("Field Generic Type: ");
        messageBuilder.append(field.getGenericType().getTypeName());
        messageBuilder.append('\n');

        String message = messageBuilder.toString();

        if (config.primitiveCheckPrintAlert()) {
            System.out.println(message);
        }
        if (config.primitiveCheckCrash()) {
            throw new RuntimeException(message);
        }
    }
}

/*
java.lang.RuntimeException: Primitive Used:
In Class: edu.wpi.first.units.measure.ImmutableAngle
Declaring Class Super Classjava.lang.Record
Field Name: magnitude
Field Generic Type: double

	at coppercore.parameter_tools.json.strategies.JSONPrimitiveCheckStrategy.handlePrimitive(JSONPrimitiveCheckStrategy.java:85)
	at coppercore.parameter_tools.json.strategies.JSONPrimitiveCheckStrategy$JSONPrimitiveCheckStrategyHelper.translateName(JSONPrimitiveCheckStrategy.java:34)
	at com.google.gson.internal.bind.ReflectiveTypeAdapterFactory.getFieldNames(ReflectiveTypeAdapterFactory.java:89)
	at com.google.gson.internal.bind.ReflectiveTypeAdapterFactory.getBoundFields(ReflectiveTypeAdapterFactory.java:392)
	at com.google.gson.internal.bind.ReflectiveTypeAdapterFactory.create(ReflectiveTypeAdapterFactory.java:155)
	at com.google.gson.Gson.getAdapter(Gson.java:628)
	at com.google.gson.internal.bind.TypeAdapterRuntimeTypeWrapper.write(TypeAdapterRuntimeTypeWrapper.java:57)
	at com.google.gson.internal.bind.ReflectiveTypeAdapterFactory$2.write(ReflectiveTypeAdapterFactory.java:247)
	at com.google.gson.internal.bind.ReflectiveTypeAdapterFactory$Adapter.write(ReflectiveTypeAdapterFactory.java:490)
	at com.google.gson.Gson.toJson(Gson.java:944)
	at com.google.gson.Gson.toJson(Gson.java:899)
	at com.google.gson.Gson.toJson(Gson.java:848)
	at com.google.gson.Gson.toJson(Gson.java:825)
	at coppercore.parameter_tools.json.JSONSync.saveData(JSONSync.java:64)
	at coppercore.parameter_tools.test.JSONSyncTests.JsonSyncSaveFileTest(JSONSyncTests.java:93)
	at java.base/java.lang.reflect.Method.invoke(Method.java:569)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1511)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1511)
*/
