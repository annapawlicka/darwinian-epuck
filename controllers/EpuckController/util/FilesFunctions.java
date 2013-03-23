package util;

import games.Game;

import java.io.BufferedWriter;
import java.io.IOException;

public class FilesFunctions {

    /**
     *
     * @param out3
     * @param generation
     * @param NB_GAMES
     * @param absBestInd
     * @param population
     */
    public static void logBest(BufferedWriter out3, int generation, int NB_GAMES, int absBestInd, Game[] population) {

        //best game
        try {
            out3.write("generation, absolute best ind index, weights");
            out3.write("\n");
            out3.write("" + generation + " " + absBestInd);
            out3.write("\n");
            for (int j = 0; j < NB_GAMES; j++){
                out3.write("" + population[absBestInd].getConstants()[j]);
                out3.write("\n");
            }
            out3.write("\n");
            out3.flush();
        } catch (IOException e) {
            System.err.println("Buffer Error: " + e.getMessage());
        }

    }

     /**
     * Write average, worst and best fitnesses of each generation.
     * @param out1
     * @param avgFit
     * @param generation
     * @param bestFit
     * @param minFit
     **/
    public static void logFitnessCases(BufferedWriter out1, double avgFit, int generation, double bestFit, double minFit) {

        //fitness
        try {
            out1.write("" + generation + ", " + avgFit + ", " +minFit +", "+ bestFit);
            out1.write("\n");
            out1.flush();
        } catch (IOException e) {
            System.err.println("Buffer Error: " + e.getMessage());
        }
    }

    public static void logAllFitnesses(BufferedWriter out2, int generation, float[] gameFitness) {
        try{
            out2.write(""+generation+", ");
            for(int i=0; i<gameFitness.length; i++) out2.write(""+gameFitness[i]+", ");
            out2.write("\n");
            out2.flush();
        } catch (IOException ioe){
            System.err.println("Buffer error: "+ioe.getMessage());
        }
    }
}

