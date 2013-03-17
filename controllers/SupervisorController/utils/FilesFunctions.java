package utils;

import nn.NeuralNetwork;

import java.io.BufferedWriter;
import java.io.IOException;

public class FilesFunctions {

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

    /**
     * Write all genomes and fitnesses to file
     */
    public static void logPopulation(BufferedWriter out1, BufferedWriter out2, int populationSize, double avgFit,
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

        //all genomes
        /*try {
            out2.write("generation: "+ generation + "\n");
            out2.write("individual , fitness, weights");
            out2.write("\n");
            for (int i = 0; i < populationSize; i++) {
                out2.write("" + i + ", " + fitness[i] + ", ");
                for (int j = 0; j < NB_GENES; j++)
                    out2.write("" + population[i].getWeights()[j]+", ");
                out2.write("\n");
            }
            out2.write("best ind: "+ bestInd + "\n" + "best fitness: " + bestFit);
            out2.flush();

        } catch (IOException e) {
            System.err.println("Buffer Error: " + e.getMessage());
        } */
    }
}

