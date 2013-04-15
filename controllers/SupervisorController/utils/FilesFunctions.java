package utils;

import nn.NeuralNetwork;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class FilesFunctions {

    /**
     * Writes absolute best individual to file.
     * @param out3
     * @param generation
     * @param NB_GENES
     * @param absBestInd
     * @param population
     */
    public static void logBest(BufferedWriter out3, int generation, int NB_GENES, int absBestInd, NeuralNetwork[] population)
            throws IOException {
        try {
            out3.write("generation, absolute best ind index, weights");
            out3.write("\n");
            out3.write("" + generation + " " + absBestInd);
            out3.write("\n");
            for (int j = 0; j < NB_GENES; j++){
                out3.write("" + population[absBestInd].getWeights()[j]);
                out3.write("\n");
            }
            out3.write("\n");
            out3.flush();
        } catch (IOException e) {
            System.out.println("Buffer Error: " + e.getMessage());
        }
    }

    /**
     *
     * @param population
     * @param index
     * @throws IOException
     */
    public static void logBestIndiv(NeuralNetwork[] population, int index) throws  IOException{
        FileWriter file = new FileWriter("out/best_actor.txt");
        BufferedWriter out = new BufferedWriter(file);
        for(int i=0; i<population[index].getWeightsNo(); i++){
            out.write(""+population[index].getWeights()[i]+",");
        }
        out.write("\n");
        out.flush();
    }

    /**
     *
     * @param population
     * @throws IOException
     */
    public static void logLastGeneration(NeuralNetwork[] population) throws IOException {
        FileWriter file = new FileWriter("out/results:genomes.txt");
        BufferedWriter out = new BufferedWriter(file);
        try{
            for(int i=0; i< population.length; i++){
                for (int j=0; j<population[i].getWeightsNo(); j++){
                    out.write(""+population[i].getWeights()[j]+",");
                }
                out.write("\n");
            }
            out.write("\n");
            out.flush();
        }   catch(IOException e){
            System.err.println("Buffer error: "+e.getMessage());
        }
    }

    /**
     * Write all genomes and fitness scores to file
     * @param out1
     * @param generation
     * @param stats
     */
    public static void logPopulation(BufferedWriter out1, int generation, double[][] stats) throws IOException {
        try {
            out1.write("" + generation);
            for(int i=0; i<stats.length; i++){
                for(int j=0; j<stats[i].length; j++){
                out1.write(","+stats[i][j]);
                }
            }
            out1.write("\n");
            out1.flush();
        } catch (IOException e) {
            System.err.println("Buffer Error: " + e.getMessage());
        }
    }

    /**
     * Write worst, average and best fitness score of current generation
     * @param out           BufferedWriter that is used to write into earlier initialised file
     * @param generation    Number of generation
     * @param worst         Worst fitness recorded
     * @param avg           Average fitness recorded
     * @param best          Best fitness recorded
     */
    public static void logFitnessScores(BufferedWriter out, int generation, double worst, double avg, double best)
            throws IOException{
        try {
            out.write("" + generation+","+worst+","+avg+","+best);
            out.write("\n");
            out.flush();
        } catch (IOException e) {
            System.err.println("Buffer Error: " + e.getMessage());
        }
    }

    /**
     *
     * @param out
     * @param generation
     * @param fitnessScores
     */
    public static void logAllActorFitnesses(BufferedWriter out, int generation, double[] fitnessScores) {
        try{
            out.write(""+generation);
            for(int i=0; i<fitnessScores.length; i++) out.write(","+fitnessScores[i]);
            out.write("\n");
            out.flush();
        } catch (IOException ioe){
            System.err.println("Buffer error: "+ioe.getMessage());
        }
    }
}