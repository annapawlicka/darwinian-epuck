package utils;

import nn.NeuralNetwork;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class FilesFunctions {

    /**
     * Writes weights of absolute best individual to file.
     * @param out3          BufferedWriter initialised with a file name it will be writing to
     * @param generation    Number of current generation
     * @param NB_GENES      Number of genes (weights)
     * @param absBestInd    Index of absolute best individual
     * @param population    Population of neural networks
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
     * Writes weights of current best individual to file - overwrites previous entry.
     * @param population    Population of neural networks
     * @param index         Index of current best individual
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
     * Writes weights of every individual in current generation - overwrites previous entry.
     * @param population    Population of neural networks
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
     * @param out1          BufferedWriter initialised with a file name
     * @param generation    Number of current generation
     * @param stats         Array with current statistics about worst, average and best individual for each game
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
     * Writes fitness scores of every individual into a file. Appends data.
     * @param out           BufferedWriter initialised with a file name
     * @param generation    Number of current generation
     * @param fitnessScores Array of fitness scores
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