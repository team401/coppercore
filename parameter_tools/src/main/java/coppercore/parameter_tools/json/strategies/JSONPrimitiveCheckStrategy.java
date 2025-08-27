package coppercore.parameter_tools.json.strategies;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;
import coppercore.parameter_tools.json.JSONSyncConfig;
import coppercore.parameter_tools.json.JSONSyncConfigBuilder;
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

        messageBuilder.append("Declaring Class Super Class: ");
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
            System.err.println(message);
        }
        if (config.primitiveCheckCrash()) {
            throw new RuntimeException(message);
        }
    }
}
