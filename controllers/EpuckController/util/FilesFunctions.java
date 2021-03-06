package util;

import games.Game;

import java.io.BufferedWriter;
import java.io.FileWriter;
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

    public static void logAllCompFit(BufferedWriter out, double [][] compFit, int generation){

        try{
            out.write(""+generation);
            out.newLine();
            for(int i=0; i<compFit.length; i++){
                for(int j=0; j< compFit[i].length; j++){
                    out.write(""+compFit[i][j]+", ");
                }
                out.newLine();
            }

            out.flush();
        }   catch (IOException e){
            System.err.println("Buffer error: "+e.getMessage());
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

    public static void logCompFitnesses(BufferedWriter out2, int generation, double[] gameFitness) {
        try{
            out2.write(""+generation+", ");
            for(int i=0; i<gameFitness.length; i++) out2.write(""+gameFitness[i]+", ");
            out2.write("\n");
            out2.flush();
        } catch (IOException ioe){
            System.err.println("Buffer error: "+ioe.getMessage());
        }
    }

    public static void logLastGeneration(Game[] population) throws IOException {

        FileWriter file = new FileWriter("out/results:games_genomes.txt");

        BufferedWriter out = new BufferedWriter(file);

        try{
            for(int i=0; i< population.length; i++){
                for (int j=0; j<population[i].getConstants().length; j++){
                    out.write(""+population[i].getConstants()[j]+",");
                }
                out.write("\n");
            }
            out.write("\n");
            out.flush();
        }   catch(IOException e){
            System.err.println("Buffer error: "+e.getMessage());
        }
    }

    public static void logAllGameGenomes(BufferedWriter out, int generation, Game[] population) throws IOException {
        out.write("Generation: "+generation+"\n");
        try{
            for(int i=0; i< population.length; i++){
                for (int j=0; j<population[i].getConstants().length; j++){
                    out.write(","+population[i].getConstants()[j]);
                }
                out.write("\n");
            }
            out.write("\n");
            out.flush();
        }   catch(IOException e){
            System.err.println("Buffer error: "+e.getMessage());
        }
    }
}

