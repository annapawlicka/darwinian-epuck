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
    public static void logBest(BufferedWriter out3, int generation, int NB_GENES, int absBestInd, NeuralNetwork[] population) {

        //best genome
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

    public static void logLastGeneration(NeuralNetwork[] population) throws IOException {

        FileWriter file = new FileWriter("results:genomes.txt");

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
     * Write all genomes and fitnesses to file
     */
    public static void logPopulation(BufferedWriter out1, double avgFit,
                                     int generation, double[] fitness,
                                     double bestFit, double minFit, int NB_GENES, NeuralNetwork[] population, int bestInd) {

        //fitness
        try {
            out1.write("" + generation + ", " + avgFit + ", " +minFit +", "+ bestFit);
            out1.write("\n");
            out1.flush();
        } catch (IOException e) {
            System.err.println("Buffer Error: " + e.getMessage());
        }
    }

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

