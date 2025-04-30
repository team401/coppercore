/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package coppercore.parameter_tools.json;

import java.util.List;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.LongSerializationPolicy;
import com.google.gson.TypeAdapterFactory;

import edu.wpi.first.math.Pair;

/** Configuration class for customizing Gson behavior. */
public record JSONSyncConfig(
        boolean serializeNulls,
        boolean prettyPrinting,
        boolean excludeFieldsWithoutExposeAnnotation,
        FieldNamingPolicy namingPolicy,
        LongSerializationPolicy longSerializationPolicy,
        List<Pair<Class, Object>> typeAdapters,
        List<TypeAdapterFactory> typeAdapterFactories) {}
