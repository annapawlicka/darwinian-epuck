package utils;

import nn.NeuralNetwork;

import java.nio.ByteBuffer;
import java.util.Random;

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
     * @param x     Values of x
     * @param mean  Mean
     * @param sd    Standard deviation sigma
     * @return      Returns normal probability density
     */
    public static double normPdf(double x, double mean, double sd) {
        double y = (1 / (sd * Math.sqrt(2 * Math.PI))) * Math.exp(-Math.pow(x - mean, 2) / (2 * sd * sd));
        //System.out.println("normPDF: "+y);
        return y;
    }

    /**
     * Product of array elements
     * @param a Array of doubles that will be worked on
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
     * @param a  Array that needs to be copied over to a new array
     * @return   New array
     */
    public static float[] copy(float[] a) {
        float[] b = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            b[i] = a[i];
        }
        return b;
    }

    /**
     * Sums up elements of an array
     * @param in  Array that will have its elements summed up
     * @return    Sum of array elements
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
     * @param x  Value of x
     * @param y  Value of y
     * @return   Returns modulus after division
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
     * @param x   Length of a string to be created
     * @return    Returns string
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
     * Makes a three's rule to normalize a value in a 0..1 interval
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

    /**
     * Returns the maximum vlaue in an array
     * @param array   Array of doubles
     * @return        Maximum value in an array
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
        for (int j = 1; j < array.length; j++) {
            if (Double.isNaN(array[j])) {
                return Double.NaN;
            }
            if (array[j] > max) {
                max = array[j];
            }
        }

        return max;
    }

    /**
     * Returns the minimum value in an array
     * @param array  Array that will be worked on
     * @return       Minimum value in an array
     */
    public static double min(double[] array) {
        // Validates input
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        } else if (array.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty.");
        }

        // Finds and returns max
        double min = array[0];
        for (int j = 1; j < array.length; j++) {
            if (Double.isNaN(array[j])) {
                return Double.NaN;
            }
            if (array[j] < min) {
                min = array[j];
            }
        }

        return min;
    }

    /**
     * Method to calculate standard deviation
     * @param values An array of doubles
     * @return Standard deviation - double
     */
    public static double stdDev(double[] values) {
        return Math.sqrt(variance(values));
    }

    /**
     * Method to calculate variance
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
     * Converts an array of bytes into an array of floats
     * @param values    Array of bytes
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
     * Converts an array of floats into an array of bytes
     * @param values    Array of floats
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

    /**
     * Method to concatenate arrays
     * @param arrays   Array of Neural Network arrays
     * @return         Returns concatenated arrays into one array
     */
    public static NeuralNetwork[] concat(NeuralNetwork[][] arrays) {
        int lengh = 0;
        for (NeuralNetwork[] array : arrays) {
            lengh += array.length;
        }
        NeuralNetwork[] result = new NeuralNetwork[lengh];
        int pos = 0;
        for (NeuralNetwork[] array : arrays) {
            for (NeuralNetwork element : array) {
                result[pos] = element;
                pos++;
            }
        }
        return result;
    }

    /**
     * Shuffles elements of an array
     * @param a     Array of neural networks
     */
    public static void shuffleList(NeuralNetwork[] a) {
        int n = a.length;
        Random random = new Random();
        random.nextInt();
        for (int i = 0; i < n; i++) {
            int change = i + random.nextInt(n - i);
            swap(a, i, change);
        }
    }

    /**
     * Swaps two elements of an array
     * @param a         Array of NeuralNetwork elements
     * @param i         Index of one elements
     * @param change    Index of second element
     */
    private static void swap(NeuralNetwork[]a, int i, int change) {
        NeuralNetwork helper = a[i];
        a[i] =  a[change];
        a[change] = helper;
    }

    /**
     * Bounds a float value to prevent from producing NaNs
     * @param d     float value
     * @return      Returns bounded float value
     */
    public static float bound(float d) {

        if (d < TOO_SMALL) {
            return TOO_SMALL;
        } else if (d > TOO_BIG) {
            return TOO_BIG;
        } else {
            return d;
        }
    }

}
