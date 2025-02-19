package coppercore.math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * This class makes a hashmap of point and connects it to a bunch of keys. Then it sorts the keys,
 * gets the extrema and returns the interpolate double of the given keys. If the key is not in the
 * map, then it finds the interpolate double with the closet key. It then checks for edge cases.
 */
public class InterpolateDouble {
    private HashMap<Double, Double> map;
    private ArrayList<Double> sortedKeys;

    private final double minValue;
    private final double maxValue;

    private final double minKey;
    private final double maxKey;

    /**
     * This makes a HashMap called map, and it adds the minimum possible value and a maximum
     * possible value
     *
     * @param map the HashMap that we are using
     */
    public InterpolateDouble(HashMap<Double, Double> map) {
        this(map, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    /**
     * This defines the variables in a way that they are able to be used and then adds all of the
     * values to the HashMap and sorts them
     *
     * @param map the HashMap that we are using
     * @param minValue the minimum value
     * @param maxValue the maximum value
     */
    public InterpolateDouble(HashMap<Double, Double> map, double minValue, double maxValue) {
        this.map = map;
        this.minValue = minValue;
        this.maxValue = maxValue;

        sortedKeys = new ArrayList<Double>();
        for (Double k : map.keySet()) {
            sortedKeys.add(k);
        }
        Collections.sort(sortedKeys);

        // Get lowest and highest keys of the HashMap
        if (sortedKeys.size() > 0) {
            minKey = sortedKeys.get(0);
            maxKey = sortedKeys.get(sortedKeys.size() - 1);
        } else {
            throw new RuntimeException("Empty HashMap passed to InterpolateDouble");
        }
    }

    /**
     * Returns the interpolated value for the given key. If the key is not in the map, it will
     * return the value for the closest key.
     *
     * @param key The key to interpolate
     * @return The interpolated value
     */
    public double getValue(double key) {
        if (map.containsKey(key)) {
            return map.get(key);
        }

        // Ensure that key is within the bounds of the HashMap
        if (key < minKey) {
            return map.get(minKey);
        } else if (key > maxKey) {
            return map.get(maxKey);
        }

        double lowerKey = 0;
        double upperKey = 0;
        for (double k : sortedKeys) {
            if (k < key) {
                lowerKey = k;
            } else {
                upperKey = k;
                break;
            }
        }

        double lowerValue = map.get(lowerKey);
        double upperValue = map.get(upperKey);

        // Edge case if keys equal each other
        if (upperKey == lowerKey) {
            upperKey += 0.01;
        }

        double t = (key - lowerKey) / (upperKey - lowerKey);
        double result = lowerValue * (1.0 - t) + t * upperValue;
        if (result < minValue) {
            return minValue;
        } else if (result > maxValue) {
            return maxValue;
        } else {
            return result;
        }
    }
}
