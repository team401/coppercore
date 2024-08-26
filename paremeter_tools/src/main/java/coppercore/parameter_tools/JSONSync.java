package coppercore.parameter_tools;

import com.google.gson.*;
import java.io.FileReader;
import java.io.FileNotFoundException;

public class JSONSync<T> {

    private final Gson gson;

    private T instance;

    private String file;

    private final JSONSyncConfig config;

    public T getObject(){
        return instance;
    }

    private FileReader getFileReader(String path){
        try {
            return new FileReader(path);
        }catch (FileNotFoundException e){
            throw new RuntimeException("File not found "+path, e);
        }
    }
    
    @SuppressWarnings("unchecked")
    public void loadData(){
        instance = gson.fromJson(getFileReader(file), (Class<T>) instance.getClass());
    }

    public void setFile(String newFilePath){
        file = newFilePath;
    }

    private Gson generateGson(){
        return new GsonBuilder().serializeNulls().create();
    }

    public JSONSync(T instance, String file, JSONSyncConfig config){
        this.instance = instance;
        this.gson = generateGson();
        this.config = (config == null)? new JSONSyncConfigBuilder().build() : config;
        this.file = file;
    }

    private static record JSONSyncConfig(boolean serializeNulls, boolean prettyPrinting, boolean excludeFieldsWithoutExposeAnnotation, FieldNamingPolicy namingPolicy,
    ToNumberPolicy numberToNumberPolicy, ToNumberPolicy objectToNumberPolicy, LongSerializationPolicy longSerializationPolicy, boolean autoReload) {       
        public JSONSyncConfig(JSONSyncConfigBuilder builder){
            this(builder.serializeNulls, builder.prettyPrinting, builder.excludeFieldsWithoutExposeAnnotation, builder.namingPolicy, builder.numberToNumberPolicy,
                builder.objectToNumberPolicy, builder.longSerializationPolicy, builder.autoReload);
        }
    }

    
    public static class JSONSyncConfigBuilder {
        public boolean serializeNulls = false;
        public boolean prettyPrinting = false;
        public boolean excludeFieldsWithoutExposeAnnotation = false;
        public FieldNamingPolicy namingPolicy = FieldNamingPolicy.IDENTITY;
        public ToNumberPolicy numberToNumberPolicy = ToNumberPolicy.DOUBLE;
        public ToNumberPolicy objectToNumberPolicy = ToNumberPolicy.LAZILY_PARSED_NUMBER;
        public LongSerializationPolicy longSerializationPolicy = LongSerializationPolicy.DEFAULT;
        public boolean keepOldValuesWhenNotPresent = false;
        public boolean autoReload = false;

        //public JSONSyncConfigBuilder newInstance(){
        //    return new JSONSyncConfigBuilder();
        //}
        //public JSONSyncConfigBuilder(){
        //
        //}
        public JSONSyncConfigBuilder setSerializeNulls(boolean serializeNulls) {
            this.serializeNulls = serializeNulls;
            return this;
        }
        public JSONSyncConfigBuilder setPrettyPrinting(boolean prettyPrinting) {
            this.prettyPrinting = prettyPrinting;
            return this;
        }
        public JSONSyncConfigBuilder setExcludeFieldsWithoutExposeAnnotation(boolean excludeFieldsWithoutExposeAnnotation) {
            this.excludeFieldsWithoutExposeAnnotation = excludeFieldsWithoutExposeAnnotation;
            return this;
        }
        public JSONSyncConfigBuilder setNamingPolicy(FieldNamingPolicy namingPolicy) {
            this.namingPolicy = namingPolicy;
            return this;
        }
        public JSONSyncConfigBuilder setNumberToNumberPolicy(ToNumberPolicy numberToNumberPolicy) {
            this.numberToNumberPolicy = numberToNumberPolicy;
            return this;
        }
        public JSONSyncConfigBuilder setObjectToNumberPolicy(ToNumberPolicy objectToNumberPolicy) {
            this.objectToNumberPolicy = objectToNumberPolicy;
            return this;
        }
        public JSONSyncConfigBuilder setLongSerializationPolicy(LongSerializationPolicy longSerializationPolicy) {
            this.longSerializationPolicy = longSerializationPolicy;
            return this;
        }
        public JSONSyncConfigBuilder setKeepOldValuesWhenNotPresent(boolean keepOldValuesWhenNotPresent) {
            this.keepOldValuesWhenNotPresent = keepOldValuesWhenNotPresent;
            return this;
        }
        public JSONSyncConfigBuilder setAutoReload(boolean autoReload) {
            this.autoReload = autoReload;
            return this;
        }
        public JSONSyncConfig build(){
            return new JSONSyncConfig(this);
        }
    }
}
