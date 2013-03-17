import com.cyberbotics.webots.controller.*;
import nn.NeuralNetwork;
import utils.Util;
import utils.FilesFunctions;

import java.io.*;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: annapawlicka
 * Date: 09/03/2013
 * Time: 14:27
 * Supervisor epuck. Controls the evolution.
 * A Supervisor can reset position of the epuck.
 */
public class SupervisorController extends Supervisor {

    // Devices
    private Emitter emitter;
    private Receiver receiver;
    private Node epuck;
    private Field fldTranslation;
    private double[] initTranslation;

    private final int TIME_STEP = 128;                      // [ms]

    // Evolution
    private int POP_SIZE;
    private int NB_INPUTS;
    private int NB_OUTPUTS;
    private int NB_GENES;
    private double[][] locations;                           // Last 5 GPS coordinates
    private NeuralNetwork[] population;
    private double[] fitness;
    private double[][] sortedfitness;                       // Population sorted by fitness
    private double ELITISM_RATIO = 0.1;
    private double REPRODUCTION_RATIO = 0.4;                //If not using roulette wheel (truncation selection), we need reproduction ratio
    private double CROSSOVER_PROBABILITY = 0.5;             //Probability of having a crossover
    private double MUTATION_PROBABILITY = 0.1;              //Probability of mutating each weight-value in a genome
    private int GENE_MIN = -1;                              //Range of genes: minimum value
    private int GENE_MAX = 1;                               //Range of genes: maximum value
    private double MUTATION_SIGMA = 0.2;                    //Mutations follow a Box-Muller distribution from the gene with this sigma
    private int evaluated_inds = 0, generation = 0;         //evaluated individual and generation counter
    //If 1, evolution takes place. If 0, then the best individual obtained during the previous evolution is tested for an undetermined amount of time.
    private int EVOLVING = 1;

    //Log variables
    private double minFit = 0.0, avgFit = 0.0, bestFit = 0.0, absBestFit = 0.0;
    private int bestInd = -1, absBestInd = -1;
    private BufferedWriter out1, out2, out3;
    private BufferedReader in1, in2, in3;
    private FileWriter file1, file2, file3;
    private FileReader reader1, reader2, reader3;

    private Random random = new Random();

    public SupervisorController() {
        super();
    }

    /**
     * This method exits only when simulation is closed/reversed.
     */
    public void run() {

        while (step(TIME_STEP) != -1) {
            byte[] fit;
            float finished = 0;

            // As long as individual is being evaluated, print current fitness and return
            int n = receiver.getQueueLength();
            if (n > 0) {
                fit = receiver.getData();
                // Convert bytes into floats
                if (fit.length == 8) {
                    byte[] currFitness = new byte[4];
                    for (int i = 0; i < 4; i++) {
                        currFitness[i] = fit[i];
                    }
                    byte[] flag = new byte[4];
                    int m = 0;
                    for (int j = 4; j < 8; j++) {
                        flag[m] = fit[j];
                        m++;
                    }
                    fitness[evaluated_inds] = Util.bytearray2float(currFitness);
                    finished = Util.bytearray2float(flag);
                    receiver.nextPacket();
                }
            }

            // When evaluation is done, an extra flag is returned in the message
            if (finished == 1) {

                if (EVOLVING == 1) {
                    // If whole population has been evaluated
                    if ((evaluated_inds + 1) == POP_SIZE) {
                        // Sort population by fitness
                        sortPopulation();
                        // Find and log current and absolute best individual
                        bestFit = sortedfitness[0][0];
                        minFit = sortedfitness[POP_SIZE-1][0];
                        bestInd = (int) sortedfitness[0][1];
                        avgFit = Util.mean(fitness);
                        if (bestFit > absBestFit) {
                            absBestFit = bestFit;
                            absBestInd = bestInd;
                            FilesFunctions.logBest(out3, generation, NB_GENES, absBestInd, population);
                        }
                        System.out.println("Best fitness score: \n" + bestFit);
                        System.out.println("Average fitness score: \n" + avgFit);
                        System.out.println("Worst fitness score: \n" + minFit);

                        // Write data to files
                        FilesFunctions.logPopulation(out1, out2, POP_SIZE, avgFit, generation, fitness,
                                bestFit, minFit, NB_GENES, population, bestInd);

                        // Rank population, select best individuals and create new generation
                        createNewPopulation();

                        generation++;
                        System.out.println("\nGENERATION \n" + generation);
                        evaluated_inds = 0;
                        avgFit = 0.0;
                        bestFit = 0;
                        bestInd = 0;
                        minFit = 0;

                        resetRobotPosition();
                        byte[] msgInBytes = Util.float2Byte(population[evaluated_inds].getWeights());
                        emitter.send(msgInBytes);
                    } else {
                        // Assign received fitness to individual
                        System.out.println("Evaluated individual " + evaluated_inds + ". Fitness: " + fitness[evaluated_inds]);
                        evaluated_inds++;

                        // Send next genome to experiment
                        resetRobotPosition();
                        byte[] msgInBytes = Util.float2Byte(population[evaluated_inds].getWeights());
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
        for (i = 0; i < POP_SIZE; i++) fitness[i] = -1;

        if (EVOLVING == 1) {
            //Open log files

            // Initialise weights randomly
            initializePopulation();
            System.out.println("NEW EVOLUTION\n");
            System.out.println("GENERATION 0\n");
            // Send genomes to experiment
            resetRobotPosition();

            byte[] msgInBytes = Util.float2Byte(population[evaluated_inds].getWeights());
            emitter.send(msgInBytes);
        } else { // Testing best individual

            //TODO  Read best genome from bestgenome.txt and initialize weights.

            System.out.println("TESTING INDIVIDUAL, GENERATION \n" + evaluated_inds + ", " + generation);

            // Send genomes to experiment
            resetRobotPosition();

            byte[] msgInBytes = Util.float2Byte(population[0].getWeights());
            emitter.send(msgInBytes);
        }
    }

    /**
     * Initiaite genes of all individuals randomly
     */
    private void initializePopulation() {
        int i, j;
        for (i = 0; i < POP_SIZE; i++) {
            for (j = 0; j < NB_GENES; j++) {
                // all genes must be in the range of [-1, 1]
                population[i].setWeights(j, (float) ((GENE_MAX - GENE_MIN) * random.nextFloat() - (GENE_MAX - GENE_MIN) / 2.0));
            }
        }
    }

    /**
     * Based on the fitness of the last generation, generate a new population of genomes for the next generation.
     */
    private void createNewPopulation() {

        NeuralNetwork[] newpop = new NeuralNetwork[POP_SIZE];
        for (int i = 0; i < newpop.length; i++) {
            newpop[i] = new NeuralNetwork(4, 2);
        }
        double elitism_counter = POP_SIZE * ELITISM_RATIO;
        int i, j;

        // Create new population
        for (i = 0; i < POP_SIZE; i++) {

            // The elitism_counter best individuals are simply copied to the new population
            if (i < elitism_counter) {
                for (j = 0; j < NB_GENES; j++)
                    newpop[i].setWeights(j, population[(int) sortedfitness[i][1]].getWeights()[j]);
            }
            // The other individuals are generated through the crossover of two parents
            else {

                // Select non-elitist individual
                int ind1;
                ind1 = (int) (elitism_counter + random.nextFloat() * (POP_SIZE * REPRODUCTION_RATIO - elitism_counter));

                // If we will do crossover, select a second individual
                if (random.nextFloat() < CROSSOVER_PROBABILITY) {
                    int ind2;
                    do {
                         ind2 = (int) (elitism_counter + random.nextFloat() * (POP_SIZE * REPRODUCTION_RATIO - elitism_counter));
                    } while (ind1 == ind2);
                    ind1 = (int) sortedfitness[ind1][1];
                    ind2 = (int) sortedfitness[ind2][1];
                    newpop[i].crossover(ind1, ind2, newpop[i], NB_GENES, population);
                } else { //if no crossover was done, just copy selected individual directly
                    for (j = 0; j < NB_GENES; j++)
                        newpop[i].setWeights(j, population[(int) sortedfitness[ind1][1]].getWeights()[j]);
                }
            }
        }

        // Mutate new population and copy back to pop
        for (i = 0; i < POP_SIZE; i++) {
            if (i < elitism_counter) { //no mutation for elitists
                for (j = 0; j < NB_GENES; j++) {
                    population[i].copy(newpop[i]);
                }
            } else { // Mutate others with probability per gene
                for (j = 0; j < NB_GENES; j++)
                    if (random.nextFloat() < MUTATION_PROBABILITY)
                        population[i].setWeights(j, population[i].mutate(GENE_MIN, GENE_MAX, newpop[i].getWeights()[j], GENE_MIN, GENE_MAX, MUTATION_SIGMA));
                    else
                        population[i].copy(newpop[i]);
            }

            // Reset fitness
            fitness[i] = 0;
        }
        return;
    }


    /**
     * Sort whole population according to fitness score of each individual. Uses quickSort.
     */
    private void sortPopulation() {
        int i;
        //sort population by fitness
        for (i = 0; i < POP_SIZE; i++) {
            sortedfitness[i][0] = fitness[i];
            sortedfitness[i][1] = (float) i; //keep index
        }
        quickSort(sortedfitness, 0, POP_SIZE - 1);
    }

    /**
     * Standard fast algorithm to sort population by fitness
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

        // Population/Evolution parameters
        POP_SIZE = 50;
        NB_INPUTS = 7;
        NB_OUTPUTS = 2;
        NB_GENES = 10;

        receiver = getReceiver("receiver");
        receiver.enable(TIME_STEP);
        emitter = getEmitter("emitter");
        epuck = getFromDef("EPUCK");
        double[] d = epuck.getPosition();
        for(double number : d) System.out.println(number);

        fldTranslation = epuck.getField("translation");

        //initTranslation = epuck.getPosition();
        population = new NeuralNetwork[POP_SIZE];
        for (i = 0; i < POP_SIZE; i++) {
            population[i] = new NeuralNetwork(NB_INPUTS, NB_OUTPUTS);
        }

        // Initialise gps coordinates array
        locations = new double[5][2];
        for (i = 0; i < 5; i++) {
            for (j = 0; j < 2; j++) {
                locations[i][j] = 0;
            }
        }

        fitness = new double[POP_SIZE];
        for (i = 0; i < POP_SIZE; i++) {
            fitness[i] = 0.0;
        }
        sortedfitness = new double[POP_SIZE][2];
        for (i = 0; i < sortedfitness.length; i++) {
            for (j = 0; j < 2; j++) {
                sortedfitness[i][j] = 0.0;
            }
        }
        initTranslation = new double[3];
        for (i = 0; i < initTranslation.length; i++) {
            initTranslation[i] = 0;
        }
        initTranslation = fldTranslation.getSFVec3f();


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
            file2 = new FileWriter("results:genomes.txt");
        } catch (IOException e) {
            System.out.println("Cannot open genomes.txt file.");
        }

        out2 = new BufferedWriter(file2);


        try {
            file3 = new FileWriter("results:bestgenome.txt");
        } catch (IOException e) {
            System.out.println("Cannot open bestgenome.txt file.");
        }

        out3 = new BufferedWriter(file3);

        System.out.println("Supervisor has been initialised.");
    }

    public static void main(String[] args) {
        SupervisorController supervisorController = new SupervisorController();
        supervisorController.initialise();
        supervisorController.reset();
        supervisorController.run();
    }

}
