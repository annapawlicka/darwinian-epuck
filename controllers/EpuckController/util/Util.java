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

    static float TOO_SMALL = -1.0E19f;
    static float TOO_BIG = 1.0E19f;

    /**
     * Normal probability density function
     * Y = normpdf(X,mu,sigma) computes the pdf at each of the values in X using
     * the normal distribution with mean mu and standard deviation sigma.
     * X, mu, and sigma can be vectors, matrices, or multidimensional arrays that all have the same size.
     *
     * @param x
     * @param mean
     * @param sd
     * @return
     */
    public static double normPdf(double x, double mean, double sd) {
        double y = (1 / (sd * Math.sqrt(2 * Math.PI))) * Math.exp(-Math.pow(x - mean, 2) / (2 * sd * sd));
        //System.out.println("normPDF: "+y);
        return y;
    }

    /**
     * Product of array elements
     *
     * @param a
     * @return
     */
    public static double prod(double[] a) {
        double p = 1;
        for (int i = 0; i < a.length; i++)
            if (a[i] != 0)
                p *= a[i];

        return p;
    }

    /**
     * Copy one array into another
     *
     * @param a
     * @return
     */
    public static double[] copy(double[] a) {
        double[] b = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            b[i] = a[i];
        }
        return b;
    }

    /**
     * Copy one array into another
     *
     * @param a
     * @return
     */
    public static float[] copy(float[] a) {
        float[] b = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            b[i] = a[i];
        }
        return b;
    }

    /**
     * Returns the maximum vlaue in an array
     *
     * @param array
     * @return
     */
    public static double max(double[] array) {
        // Validates input
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        } else if (array.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty.");
        }

        // Finds and returns max
        double max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }

        }
        return max;
    }

    /**
     * Returns the minimum value in an array
     *
     * @param array
     * @return
     */
    public static double min(double[] array) {
        // Validates input
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        } else if (array.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty.");
        }

        // Finds and returns min
        double min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }

        }
        return min;
    }

    /**
     * Sum of array elements
     *
     * @param in Array of doubles
     * @return Sum (double)
     */
    public static double sum(double in[]) {
        double sum = 0;
        for (int i = 0; i < in.length; i++)
            sum += in[i];

        return sum;
    }

    /**
     * Modulus after division
     * M = mod(X,Y) if Y ~= 0, returns X - n.*Y where n = floor(X./Y).
     * If Y is not an integer and the quotient X./Y is within roundoff error of an integer, then n is that integer.
     *
     * @param x
     * @param y
     * @return
     */
    public static double mod(double x, double y) {
        double result = x % y;
        if (result < 0) {
            result += y;
        }
        return result;
    }

    /**
     * Create an empty binary string of given length
     *
     * @param x
     * @return
     */
    public static String getEmptyBinaryString(int x) {

        String str = "0";
        for (int i = 0; i < x - 1; i++) {
            str += "0";
        }
        return str;
    }

    /**
     * Computes the arithmetic mean of a set of values. Uses the definitional formula: mean = sum(x_i) / n
     *
     * @param values An array of doubles
     * @return Arithmetic mean (double)
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
     *
     * @param values An array of floats
     * @return Arithmetic mean (float)
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
     *
     * @param values An array of doubles
     * @return Variance - double
     */
    public static double variance(double[] values) {
        double mean = mean(values);
        double temp = 0;
        for (double a : values)
            temp += (mean - a) * (mean - a);
        return temp / values.length;
    }

    /**
     * Method to calculate standard deviation
     *
     * @param values An array of doubles
     * @return Standard deviation - double
     */
    public static double stdDev(double[] values) {
        return Math.sqrt(variance(values));
    }

    /**
     * Makes a three's rule to normalize a value in a 0..1 interval
     *
     * @param min   lowest saturation point. Values below this will return 0
     * @param max   highest saturation point. Values above this will return 1
     * @param value
     * @return a double value between 0 and 1
     * @throws Exception
     */
    public static double normalize(double min, double max, double value) throws Exception {
        return normalize(min, max, value, 0, 1);
    }

    /**
     * Makes a three's rule to normalize a value in a 0..1 interval
     *
     * @param min   lowest saturation point. Values below this will return 0
     * @param max   highest saturation point. Values above this will return 1
     * @param value
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

    public static float normalizeFloat(float min, float max, float value,
                                       float start, float end) throws Exception {
        if (max <= min) throw new Exception("Min cannot be higher than max. Values entered are not valid.");
        if (end <= start) throw new Exception("End cannot be higher than start. Values entered are not valid.");
        if (value >= max) return end;
        if (value <= min) return start;

        float i1 = max - min;
        float i2 = end - start;
        float y = (value - min) * i2 / i1;
        return y + start;

    }



    public static float bound(float d) {

        if (d < TOO_SMALL) {
            return TOO_SMALL;
        } else if (d > TOO_BIG) {
            return TOO_BIG;
        } else {
            return d;
        }
    }

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

    public static void main(String[] args){

        //byte[] b = {127,-64,-65,94};
        //float f = bytearray2float(b);
        //System.out.println(f);

        //float[] floats= {0.20630264f,-0.062446f,-0.6959696f,0.3756733f,0.30890977f,0.3592105f,0.5308776f,0.5443175f,
        //        -0.35577512f,0.72968745f,0.43952227f,-0.81068146f,-0.38986576f,-0.69835114f,0.5708624f,-0.5889871f};
        /*float[] floats={1.0f};
        byte[] bytes = float2ByteArray(floats);
        System.out.println("FLOATS:");
        for(int i=0; i< floats.length; i++) System.out.print(floats[i] + ",");
        System.out.println();
        System.out.println("BYTES: ");
        for(int i=0; i< bytes.length; i++) System.out.print(bytes[i] + ",");

        float[] f = bytes2FloatArray(bytes);
        System.out.println();
        System.out.println("Floats:");
        for(int l=0; l<f.length; l++) System.out.println(f[l]);*/

        byte b = 1;
        float temp = b;
        System.out.println(temp);

    }

}
