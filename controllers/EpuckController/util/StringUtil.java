package util;

import java.io.BufferedWriter;
import java.io.FileWriter;


public class StringUtil {

    /**
     * Method to convert an array of doubles to single String
     *
     * @param a
     * @return
     */
    public static String arrayToString(double[] a) {
        StringBuilder b = new StringBuilder();
        b.append("[ ");
        for (int i = 0; i < a.length; i++) {
            b.append(a[i] + " ");
        }
        b.append("]");
        return b.toString();
    }

    /**
     * Method to convert 2D array of doubles to single String
     *
     * @param a
     * @return
     */
    public static String arrayToString(double[][] a) {
        StringBuilder b = new StringBuilder();
        b.append("[ ");
        for (int i = 0; i < a.length; i++) {
            b.append("[ ");
            for (int j = 0; j < a[i].length; j++) {
                b.append(a[i][j] + " ");
            }
            if (i != a.length - 1) b.append("]\n");
        }
        b.append("]");
        return b.toString();
    }

    /**
     * Method to convert an array to single String
     *
     * @param a
     * @return
     */
    public static String arrayToString(int[] a) {
        StringBuilder b = new StringBuilder();
        b.append("[ ");
        for (int i = 0; i < a.length; i++) {
            b.append(a[i] + " ");
        }
        b.append("]");
        return b.toString();
    }

    /**
     * Method to convert 2D array of integers to single String
     *
     * @param a
     * @return
     */
    public static String arrayToString(int[][] a) {
        StringBuilder b = new StringBuilder();
        b.append("[ ");
        for (int i = 0; i < a.length; i++) {
            b.append("[ ");
            for (int j = 0; j < a[i].length; j++) {
                b.append(a[i][j] + " ");
            }
            if (i != a.length - 1) b.append("]\n");
        }
        b.append("]");
        return b.toString();
    }

    /**
     * Method to write an array to file
     *
     * @param f
     * @param a
     * @param epoch
     * @param ts
     */
    public static void arrayToFile(FileWriter f, double[] a, int epoch, int ts) {
        try {
            BufferedWriter out = new BufferedWriter(f);
            out.write("Epoch " + epoch + ", time step " + ts + ".\n");
            out.write("[ ");
            for (int i = 0; i < a.length; i++) {
                out.write(a[i] + " ");
            }
            out.write("]\n");
            out.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * Method to write a 2D array to file.
     *
     * @param f     FileWriter object
     * @param a     Array that will be written to file
     * @param epoch Epoch - represents a period of generating a set number of generations
     * @param ts    TIMESTEP [ms]
     */
    public static void arrayToFile(FileWriter f, double[][] a, int epoch, int ts) {
        try {
            // Create file
            BufferedWriter out = new BufferedWriter(f);
            out.write("Epoch " + epoch + ", time step " + ts + ".\n");
            for (int i = 0; i < a.length; i++) {
                out.write("[ ");
                for (int j = 0; j < a[i].length; j++) {
                    out.write(a[i][j] + " ");
                }
                out.write("]");
            }
            out.write("]");
            out.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }
}
