import com.cyberbotics.webots.controller.*;
import nn.NeuralNetwork;
import utils.FilesFunctions;
import utils.Util;

import java.io.*;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: annapawlicka
 * Date: 09/03/2013
 * Time: 14:27
 * Supervisor controller. Controls the evolution of e-puck. Supervisor can reset position of the epuck.
 * Communication with e-puck is done via emitters/receivers. Separate devices for game and neural communication.
 * Evolution is done by elitism, crossover and mutation.
 * Fitness function of agents is how well they learn the games.
 */
public class SupervisorController extends Supervisor {

    // Devices
    private Emitter emitter;
    private Emitter gameEmitter;
    private Receiver receiver;
    private Receiver gameReceiver;
    private Node epuck;
    private Field fldTranslation;
    private double[] initTranslation;

    private final int TIME_STEP = 128;                      // [ms]

    // Evolution
    private int NN_POP_SIZE;
    private int GAME_POP_SIZE;
    private int NB_INPUTS;
    private int NB_OUTPUTS;
    private int NB_GENES;
    private double[][] locations;                           // Last 5 GPS coordinates
    private NeuralNetwork[] populationOfNN;
    private double[] fitnessNN;
    private double[][] sortedfitnessNN;                     // Population sorted by fitness
    private double ELITISM_RATIO = 0.1;
    private double REPRODUCTION_RATIO = 0.4;                // If not using roulette wheel (truncation selection), we need reproduction ratio
    private double CROSSOVER_PROBABILITY = 0.5;             // Probability of having a crossover
    private double MUTATION_PROBABILITY = 0.1;              // Probability of mutating each weight-value in a genome
    private int GENE_MIN = -1;                              // Range of genes: minimum value
    private int GENE_MAX = 1;                               // Range of genes: maximum value
    private double MUTATION_SIGMA = 0.2;                    // Mutations follow a Box-Muller distribution from the gene with this sigma
    private int evaluatedNN = 0;                            // Evaluated individuals
    private int generation = 0;                             // Generation counter
    //If 1, evolution takes place. If 0, then the best individual obtained during the previous evolution is tested for an undetermined amount of time.
    private int EVOLVING = 1;
    private int TESTING = 0;
    private int ROULETTE_WHEEL = 1;

    //Log variables
    private double minFitNN = 0.0, avgFitNN = 0.0, bestFitNN = 0.0, absBestFitNN = -10000;
    private int bestNN = -1, absBestNN = -1;
    private BufferedWriter out1;
    private BufferedWriter out3;
    private BufferedReader in1, in2, in3;
    private FileWriter file1, file3;
    private BufferedReader reader3;

    private Random random = new Random();

    public SupervisorController() {
        super();
    }

    /**
     * This method exits only when simulation is closed/reversed.
     */
    public void run() {

        while (step(TIME_STEP) != -1) {
            byte[] nnFit;
            float finished = 0;

            // As long as individual is being evaluated, print current fitness and return
            int n = receiver.getQueueLength();
            if (n > 0) {
                nnFit = receiver.getData();
                // Convert bytes into floats
                if (nnFit.length == 8) {
                    byte[] currFitness = new byte[4];
                    for (int i = 0; i < 4; i++) {
                        currFitness[i] = nnFit[i];
                    }
                    byte[] flag = new byte[4];
                    int l = 0;
                    for (int j = 4; j < 8; j++) {
                        flag[l] = nnFit[j];
                        l++;
                    }
                    fitnessNN[evaluatedNN] = Util.bytearray2float(currFitness);
                    finished = Util.bytearray2float(flag);
                    receiver.nextPacket();
                }
            }

            // When evaluation is done, an extra flag is returned in the message
            if (finished == 1) {

                if (EVOLVING == 1) {
                    // If whole populationOfNN has been evaluated
                    if ((evaluatedNN + 1) == NN_POP_SIZE) {
                        // Sort populationOfNN by fitness
                        sortPopulation(sortedfitnessNN, fitnessNN);
                        // Find and log current and absolute best individual
                        bestFitNN = sortedfitnessNN[0][0];
                        minFitNN = sortedfitnessNN[NN_POP_SIZE - 1][0];
                        bestNN = (int) sortedfitnessNN[0][1];
                        avgFitNN = Util.mean(fitnessNN);
                        if (bestFitNN > absBestFitNN) {
                            absBestFitNN = bestFitNN;
                            absBestNN = bestNN;
                            FilesFunctions.logBest(out3, generation, NB_GENES, absBestNN, populationOfNN);
                        }
                        System.out.println("Best fitness score: \n" + bestFitNN);
                        System.out.println("Average fitness score: \n" + avgFitNN);
                        System.out.println("Worst fitness score: \n" + minFitNN);

                        // Write data to files
                        FilesFunctions.logPopulation(out1, avgFitNN, generation, fitnessNN,
                                bestFitNN, minFitNN, NB_GENES, populationOfNN, bestNN);
                        // Log the generation data  - stores weights
                        try {
                            FilesFunctions.logLastGeneration(populationOfNN);
                        } catch (IOException e) {
                            e.getMessage();
                        }
                        // Rank populationOfNN, select best individuals and create new generation
                        createNewPopulation();

                        generation++;
                        System.out.println("\nGENERATION \n" + generation);
                        evaluatedNN = 0;
                        avgFitNN = 0.0;
                        bestFitNN = 0;
                        bestNN = 0;
                        minFitNN = 0;

                        resetRobotPosition();
                        // Evolve games every 5 NN generations (gives them time to learn)
                        if (generation % 1 == 0) {
                            // Send flag to start evolution of games
                            byte[] flag = {1};
                            gameEmitter.send(flag);
                        }

                        // Send new weights
                        byte[] msgInBytes = Util.float2Byte(populationOfNN[evaluatedNN].getWeights());
                        emitter.send(msgInBytes);

                    } else {
                        // Assign received fitness to individual
                        System.out.println("Evaluated individual " + evaluatedNN + ". Fitness: " + fitnessNN[evaluatedNN]);
                        evaluatedNN++;
                        // Send next genome to experiment
                        resetRobotPosition();
                        byte[] msgInBytes = Util.float2Byte(populationOfNN[evaluatedNN].getWeights());
                        emitter.send(msgInBytes);
                    }
                }
            }

        }

    }

    /**
     * The reset function is called at the beginning of an evolution.
     */
    public void reset() {

        int i;
        for (i = 0; i < NN_POP_SIZE; i++) fitnessNN[i] = -1;

        if (EVOLVING == 1) {
            // Initialise weights randomly
            initializePopulation();
            System.out.println("NEW EVOLUTION\n");
            System.out.println("GENERATION 0\n");
            resetRobotPosition();

            // Then, send weights of NNs to experiment
            byte[] msgInBytes = Util.float2Byte(populationOfNN[evaluatedNN].getWeights());
            emitter.send(msgInBytes);

        }

        if (TESTING == 1) {
            int counter = 0;
            String strLine;
            try {
                while ((strLine = reader3.readLine()) != null && counter < 50) {
                    String[] weightsStr = strLine.split(",");
                    for (i = 0; i < populationOfNN[counter].getWeightsNo(); i++) {
                        populationOfNN[counter].setWeights(i, Float.parseFloat(weightsStr[i]));
                    }
                    counter++;
                }
            } catch (IOException e) {
                e.getMessage();
            }


            System.out.println("TESTING LAST GENERATION \n");
        }

    }

    /**
     * Initiate genes of all individuals randomly
     */
    private void initializePopulation() {
        int i, j;
        for (i = 0; i < NN_POP_SIZE; i++) {
            for (j = 0; j < NB_GENES; j++) {
                // all genes must be in the range of [-1, 1]
                populationOfNN[i].setWeights(j, (float) ((GENE_MAX - GENE_MIN) * random.nextFloat() - (GENE_MAX - GENE_MIN) / 2.0));
            }
        }
    }

    /**
     * Based on the fitness of the last generation, generate a new population of genomes for the next generation.
     */
    private void createNewPopulation() {

        NeuralNetwork[] newpop = new NeuralNetwork[NN_POP_SIZE];
        for (int i = 0; i < newpop.length; i++) {
            newpop[i] = new NeuralNetwork(NB_INPUTS, NB_OUTPUTS);
        }
        double elitism_counter = NN_POP_SIZE * ELITISM_RATIO;
        double total_fitness = 0;

        // Find minimum fitness to subtract it from sum
        double min_fitness = sortedfitnessNN[NN_POP_SIZE - 1][0];
        if (min_fitness < 0) min_fitness = 0;
        int i, j;

        // Calculate total of fitness, used for roulette wheel selection
        for (i = 0; i < NN_POP_SIZE; i++) total_fitness += fitnessNN[i];
        total_fitness -= min_fitness * NN_POP_SIZE;

        // Create new population
        for (i = 0; i < NN_POP_SIZE; i++) {

            // The elitism_counter best individuals are simply copied to the new populationOfNN
            if (i < elitism_counter) {
                for (j = 0; j < NB_GENES; j++)
                    newpop[i].setWeights(j, populationOfNN[(int) sortedfitnessNN[i][1]].getWeights()[j]);
            }
            // The other individuals are generated through the crossover of two parents
            else {

                // Select non-elitist individual
                int ind1 = 0;
                if (ROULETTE_WHEEL == 1) {
                    float r = random.nextFloat();
                    double fitness_counter = (sortedfitnessNN[ind1][0] - min_fitness) / total_fitness;
                    while (r > fitness_counter) {
                        ind1++;
                        fitness_counter += (sortedfitnessNN[ind1][0] - min_fitness) / total_fitness;
                    }
                } else
                    ind1 = (int) (elitism_counter + random.nextFloat() * (NN_POP_SIZE * REPRODUCTION_RATIO - elitism_counter));

                // If we will do crossover, select a second individual
                if (random.nextFloat() < CROSSOVER_PROBABILITY) {
                    int ind2 = 0;
                    if (ROULETTE_WHEEL == 1)
                        do {
                            float r = random.nextFloat();
                            double fitness_counter = (sortedfitnessNN[ind2][0] - min_fitness) / total_fitness;
                            while (r > fitness_counter) {
                                ind2++;
                                fitness_counter += (sortedfitnessNN[ind2][0] - min_fitness) / total_fitness;
                            }
                        } while (ind1 == ind2);
                    else
                        do {
                            ind2 = (int) (elitism_counter + random.nextFloat() * (NN_POP_SIZE * REPRODUCTION_RATIO - elitism_counter));
                        } while (ind1 == ind2);
                    ind1 = (int) sortedfitnessNN[ind1][1];
                    ind2 = (int) sortedfitnessNN[ind2][1];
                    newpop[i].crossover(ind1, ind2, newpop[i], NB_GENES, populationOfNN);
                } else { //if no crossover was done, just copy selected individual directly
                    for (j = 0; j < NB_GENES; j++)
                        newpop[i].setWeights(j, populationOfNN[(int) sortedfitnessNN[ind1][1]].getWeights()[j]);
                }
            }
        }

        // Mutate new populationOfNN and copy back to pop
        for (i = 0; i < NN_POP_SIZE; i++) {
            if (i < elitism_counter) { //no mutation for elitists
                for (j = 0; j < NB_GENES; j++) {
                    populationOfNN[i].copy(newpop[i]);
                }
            } else { // Mutate others with probability per gene
                for (j = 0; j < NB_GENES; j++)
                    if (random.nextFloat() < MUTATION_PROBABILITY)
                        populationOfNN[i].setWeights(j, populationOfNN[i].mutate(GENE_MIN, GENE_MAX, newpop[i].getWeights()[j], MUTATION_SIGMA));
                    else
                        populationOfNN[i].copy(newpop[i]);
            }

            // Reset fitness
            fitnessNN[i] = 0;
        }
        return;
    }


    /**
     * Sort whole population according to fitness score of each individual. Uses quickSort.
     */
    private void sortPopulation(double[][] sortedfitness, double[] fitness) {
        int i;
        //sort populationOfNN by fitness
        for (i = 0; i < sortedfitness.length; i++) {
            sortedfitness[i][0] = fitness[i];
            sortedfitness[i][1] = (float) i; //keep index
        }
        quickSort(sortedfitness, 0, sortedfitness.length - 1);
    }

    /**
     * Standard fast algorithm to sort populationOfNN by fitness
     *
     * @param fitness Array that stores fitness and index of each individual.
     * @param left    Min index of the array
     * @param right   Max index of the array
     */
    private void quickSort(double fitness[][], int left, int right) {
        double[] pivot = new double[2];
        int l_hold, r_hold;

        l_hold = left;
        r_hold = right;
        pivot[0] = fitness[left][0];
        pivot[1] = fitness[left][1];
        while (left < right) {
            while ((fitness[right][0] <= pivot[0]) && (left < right))
                right--;
            if (left != right) {
                fitness[left][0] = fitness[right][0];
                fitness[left][1] = fitness[right][1];
                left++;
            }
            while ((fitness[left][0] >= pivot[0]) && (left < right))
                left++;
            if (left != right) {
                fitness[right][0] = fitness[left][0];
                fitness[right][1] = fitness[left][1];
                right--;
            }
        }
        fitness[left][0] = pivot[0];
        fitness[left][1] = pivot[1];
        pivot[0] = left;
        left = l_hold;
        right = r_hold;
        if (left < (int) pivot[0]) quickSort(fitness, left, (int) pivot[0] - 1);
        if (right > (int) pivot[0]) quickSort(fitness, (int) pivot[0] + 1, right);
    }


    /**
     * Resets the position of the epuck before each generation's trials
     */
    private void resetRobotPosition() {
        fldTranslation.setSFVec3f(initTranslation);
    }

    private void initialise() {

        int i, j;

        /* Population/Evolution parameters */
        NN_POP_SIZE = 50;
        GAME_POP_SIZE = 10;

        // Neural Networks
        NB_INPUTS = 7;
        NB_OUTPUTS = 2;
        NB_GENES = 10;
        populationOfNN = new NeuralNetwork[NN_POP_SIZE];
        for (i = 0; i < NN_POP_SIZE; i++) populationOfNN[i] = new NeuralNetwork(NB_INPUTS, NB_OUTPUTS);
        fitnessNN = new double[NN_POP_SIZE];
        for (i = 0; i < NN_POP_SIZE; i++) fitnessNN[i] = 0.0;
        sortedfitnessNN = new double[NN_POP_SIZE][2];
        for (i = 0; i < sortedfitnessNN.length; i++) {
            for (j = 0; j < 2; j++) {
                sortedfitnessNN[i][j] = 0.0;
            }
        }

        // Nodes
        receiver = getReceiver("receiver");
        receiver.enable(TIME_STEP);
        emitter = getEmitter("emitter");
        epuck = getFromDef("EPUCK");
        fldTranslation = epuck.getField("translation");

        gameEmitter = getEmitter("gamesemittersuper");
        gameEmitter.setChannel(1);
        gameReceiver = getReceiver("gamesreceiversuper");
        gameReceiver.setChannel(1);
        gameReceiver.enable(TIME_STEP);

        // Initialise gps coordinates arrays
        locations = new double[5][2];
        initTranslation = new double[3];
        initTranslation = fldTranslation.getSFVec3f();

        // Logging
        try {
            file1 = new FileWriter("results:fitness.txt");
        } catch (IOException e) {
            System.out.println("Cannot open fitness.txt file.");
        }

        out1 = new BufferedWriter(file1);
        try {
            out1.write("generation , average fitness, worst fitness, best fitness");
            out1.write("\n");

        } catch (IOException e) {
            System.out.println("" + e.getMessage());
        }

        try {
            file3 = new FileWriter("results:bestgenome.txt");
        } catch (IOException e) {
            System.out.println("Cannot open bestgenome.txt file.");
        }

        out3 = new BufferedWriter(file3);


        try {
            reader3 = new BufferedReader(new FileReader("results:genomes.txt"));
        } catch (FileNotFoundException e) {
            System.out.println("Cannot read from file: results:genomes.txt");
        }

        System.out.println("Supervisor has been initialised.");
    }


    public static void main(String[] args) {
        SupervisorController supervisorController = new SupervisorController();
        supervisorController.initialise();
        supervisorController.reset();
        supervisorController.run();
    }

}
