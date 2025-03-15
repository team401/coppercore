/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package coppercore.parameter_tools.json;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.LongSerializationPolicy;

/** Configuration class for customizing Gson behavior. */
public record JSONSyncConfig(
        boolean serializeNulls,
        boolean prettyPrinting,
        boolean excludeFieldsWithoutExposeAnnotation,
        FieldNamingPolicy namingPolicy,
        LongSerializationPolicy longSerializationPolicy,
        boolean primitiveChecking,
        boolean primitiveCheckPrintAlert,
        boolean primitiveCheckCrash) {
    public JSONSyncConfig(JSONSyncConfigBuilder builder) {
        this(
                builder.serializeNulls,
                builder.prettyPrinting,
                builder.excludeFieldsWithoutExposeAnnotation,
                builder.namingPolicy,
                builder.longSerializationPolicy,
                builder.primitiveChecking,
                builder.primitiveCheckPrintAlert,
                builder.primitiveCheckCrash);
    }
}
