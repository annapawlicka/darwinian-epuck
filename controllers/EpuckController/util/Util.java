package util;

import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: annapawlicka
 * Date: 20/12/2012
 * Time: 23:11
 * Utilities is a collection of methods that implement statistical/mathematical functions.
 */

public class Util {

    /**
     * Copy one array into another
     * @param a     Array of floats
     * @return      New array with contents of passed array
     */
    public static float[] copy(float[] a) {
        float[] b = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            b[i] = a[i];
        }
        return b;
    }

    /**
     * Computes the arithmetic mean of a set of values. Uses the definitional formula: mean = sum(x_i) / n
     * @param values    An array of doubles
     * @return          Arithmetic mean (double)
     */
    public static double mean(double[] values) {
        double sum = 0.0;
        for (int i = 0; i < values.length; i++) {
            sum += values[i];
        }
        return (sum) / values.length;
    }

    /**
     * Computes the arithmetic mean of a set of values. Uses the definitional formula: mean = sum(x_i) / n
     * @param values    An array of floats
     * @return          Returns arithmetic mean (float)
     */
    public static float mean(float[] values) {
        float sum = 0.0f;
        for (int i = 0; i < values.length; i++) {
            sum += values[i];
        }
        return (sum) / values.length;
    }

    /**
     * Method to calculate variance
     * @param values    An array of doubles
     * @return          Returns variance - double
     */
    public static double variance(double[] values) {
        double mean = mean(values);
        double temp = 0;
        for (double a : values)
            temp += (mean - a) * (mean - a);
        return temp / values.length;
    }

    /**
     * Makes a three's rule to normalize a value in a 0..1 interval
     * @param min   lowest saturation point. Values below this will return 0
     * @param max   highest saturation point. Values above this will return 1
     * @param value Number to be normalised
     * @return      Returns a double value between 0 and 1
     * @throws Exception
     */
    public static double normalize(double min, double max, double value) throws Exception {
        return normalize(min, max, value, 0, 1);
    }

    /**
     * Makes a three's rule to normalize a value in a 0..1 interval
     * @param min   lowest saturation point. Values below this will return 0
     * @param max   highest saturation point. Values above this will return 1
     * @param value Value to be normalised
     * @param start beginning of the normalization interval
     * @param end   ending of the normalization interval
     * @return a double value between start and end
     * @throws Exception
     */
    public static double normalize(double min, double max, double value,
                                   double start, double end) throws Exception {
        if (max <= min) throw new Exception("Min cannot be higher than max. Values entered are not valid.");
        if (end <= start) throw new Exception("End cannot be higher than start. Values entered are not valid.");
        if (value >= max) return end;
        if (value <= min) return start;

        double i1 = max - min;
        double i2 = end - start;
        double y = (value - min) * i2 / i1;
        return y + start;
    }

    /**
     * Converts an array of bytes to an array of floats
     * @param values    array of bytes
     * @return          Returns array of floats
     */
    public static float[] bytes2FloatArray(byte[] values){
        int p=0, r=0;
        float[] floats = new float[values.length/4];
         byte[] weight = new byte[4];
        for(int i=0; i< values.length; i++){
            if(p<4){
                weight[p] = values[i];
                p++;
            }
            else {
                p=0;
                float fl = ByteBuffer.wrap(weight).getFloat();
                floats[r] = fl;
                weight[p] = values[i];
                p++;
                r++;
            }
        }
        return floats;
    }

    /**
     * Converts an array of float to an array of bytes
     * @param values    array of floats
     * @return          Returns array of bytes
     */
    public static byte [] float2ByteArray (float[] values)
    {
        byte[][] b = new byte[values.length][4];

        for(int i=0; i<values.length; i++){
            b[i] = ByteBuffer.allocate(4).putFloat(values[i]).array();
        }
        byte[] result = new byte[b.length*4];
        int counter = 0;
        for(int i=0; i<b.length; i++){
            for(int j=0; j<b[i].length; j++){
                result[counter] = b[i][j];
                counter++;
            }
        }
        return result;
    }
}