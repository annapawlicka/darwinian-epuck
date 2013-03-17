import com.cyberbotics.webots.controller.*;
import games.Game;
import util.Util;

import java.io.BufferedWriter;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: annapawlicka
 * Date: 01/03/2013
 * Time: 19:50
 * E-puck controller that works with NN algorithm.
 */
public class EpuckController extends Robot {

    // Global variables
    private int GAME_POP_SIZE = 10;
    private final int LEFT = 0;
    private final int RIGHT = 1;
    private final int TIME_STEP = 128;              // [ms]
    private final int PS_RANGE = 3800;
    private final int SPEED_RANGE = 500;
    private final int NB_DIST_SENS = 8;             // Number of IR proximity sensors
    private final double OBSTACLE_THRESHOLD = 3000;
    private final int TRIAL_DURATION = 60000;       // Evaluation duration of one individual [ms]
    private final int NB_INPUTS = 7;
    private final int NB_OUTPUTS = 2;
    private int NB_WEIGHTS = NB_INPUTS * NB_OUTPUTS + NB_OUTPUTS;   // No hidden layer
    private int NB_CONSTANTS = 4;
    private float weights[];
    private float currentFitness;

    // Evolution of games
    private Game[] populationOfGames;
    private double[] fitnessGames;
    private double[][] sortedfitnessGames;                  // Population of games sorted byte fitness
    private double ELITISM_RATIO = 0.1;
    private double REPRODUCTION_RATIO = 0.4;                // If not using roulette wheel (truncation selection), we need reproduction ratio
    private double CROSSOVER_PROBABILITY = 0.5;             // Probability of having a crossover
    private double MUTATION_PROBABILITY = 0.1;              // Probability of mutating each weight-value in a genome    private int GENE_MIN = -1;                              // Range of genes: minimum value
    private int GENE_MIN = -1;
    private int GENE_MAX = 1;                               // Range of genes: maximum value
    private double MUTATION_SIGMA = 0.2;                    // Mutations follow a Box-Muller distribution from the gene with this sigma
    private int evaluatedGame = 0;                          // Evaluated individuals
    private int generation = 0;                             // Generation counter
    //If 1, evolution takes place. If 0, then the best individual obtained during the previous evolution is tested for an undetermined amount of time.
    private int EVOLVING = 1;
    private int currentGame;

    //Log variables
    private double minFitGame = 0.0, avgFitGame = 0.0, bestFitGame = 0.0, absBestFitGame = 0.0;
    private int bestGame = -1, absBestGame = -1;

    // Mode of robot
    private static int mode;
    private final int SIMULATION = 0;               // for robot.get_mode() function
    private final int REALITY = 2;                  // for robot.get_mode() function

    // 8 IR proximity sensors
    private int proximitySensorsNo = 8;
    private DistanceSensor[] ps;
    private float[] ps_offset;
    private int[] PS_OFFSET_SIMULATION = new int[]{300, 300, 300, 300, 300, 300, 300, 300};
    private int[] PS_OFFSET_REALITY = new int[]{480, 170, 320, 500, 600, 680, 210, 640};

    // 3 IR floor color sensors
    private int floorSensorsNo = 3;
    private DistanceSensor[] fs;
    private double[] fs_value = new double[]{0, 0, 0};
    private double maxIRActivation;

    // LEDs
    private int ledsNo = 8;
    private LED[] led = new LED[ledsNo];


    // Differential Wheels
    private DifferentialWheels robot = new DifferentialWheels();
    private double[] speed;

    // GPS
    private GPS gps;
    private double[] position;
    private double[] states = new double[11];               // The sensor values  8+3
    private double distanceTravelled;

    // Emitter and Receiver
    private Emitter emitter;
    private Receiver receiver;
    private Emitter gameEmitter;
    private Receiver gameReceiver;

    // Logging
    private BufferedWriter out1, out2, out3;

    private int step;
    private Random random = new Random();


    public void run() {

        while (step(TIME_STEP) != -1) {

            int i;
            if (mode != getMode()) {
                mode = getMode();
                if (mode == SIMULATION) {
                    for (i = 0; i < NB_DIST_SENS; i++) ps_offset[i] = PS_OFFSET_SIMULATION[i];
                    System.out.println("Switching to SIMULATION.\n\n");
                } else if (mode == REALITY) {
                    for (i = 0; i < NB_DIST_SENS; i++) ps_offset[i] = PS_OFFSET_REALITY[i];
                    System.out.println("\nSwitching to REALITY.\n\n");
                }
            }

            int m = gameReceiver.getQueueLength();
            if(m > 0){
                byte[] flag = gameReceiver.getData();

                if(flag[0]==1){
                    // Start evolution of games
                    // Sort populationOfNN by fitness
                    sortPopulation(sortedfitnessGames, fitnessGames);
                    // Find and log current and absolute best individual
                    bestFitGame = sortedfitnessGames[0][0];
                    minFitGame = sortedfitnessGames[GAME_POP_SIZE - 1][0];
                    bestGame = (int) sortedfitnessGames[0][1];
                    avgFitGame = util.Util.mean(fitnessGames);
                    if (bestFitGame > absBestFitGame) {
                        absBestFitGame = bestFitGame;
                        absBestGame = bestGame;
                       // FilesFunctions.logBest(out3, generation, NB_CONSTANTS, absBestGame, populationOfGames);
                    }
                    System.out.println("Best game fitness score: \n" + bestFitGame);
                    System.out.println("Average game fitness score: \n" + avgFitGame);
                    System.out.println("Worst game fitness score: \n" + minFitGame);

                    // Write data to files
                    //FilesFunctions.logPopulation(out1, out2, GAME_POP_SIZE, avgFitGame, generation, fitnessGames,
                    //        bestFitGame, minFitGame, NB_CONSTANTS, populationOfGames, bestGame);

                    // Rank populationOfNN, select best individuals and create new generation
                    createNewPopulation();

                    generation++;
                    System.out.println("\nGAME GENERATION \n" + generation);
                    evaluatedGame = 0;
                    avgFitGame = 0.0;
                    bestFitGame = 0;
                    bestGame = 0;
                    minFitGame = 0;
                }
                gameReceiver.nextPacket();
            }

            if (step == 0 && currentGame == 0) {
                int n = receiver.getQueueLength();
                // Wait for new genome
                if (n > 0) {
                    byte[] genes = receiver.getData();
                    // Set neural network weights
                    for (i = 0; i < NB_WEIGHTS; i++) weights[i] = genes[i];
                    receiver.nextPacket();
                }
                currentFitness = 0;
                System.out.println("Current game: "+currentGame);
            }

            step++;

            if (step < TRIAL_DURATION / TIME_STEP && currentGame < GAME_POP_SIZE) {
                // Drive robot
                currentFitness += runTrial();                // Send message with current fitness
                float msg[] = {currentFitness, 0.0f};
                byte[] msgInBytes = Util.float2Byte(msg);
                emitter.send(msgInBytes);
            }
            else if(step >= TRIAL_DURATION / TIME_STEP && currentGame < GAME_POP_SIZE){
                // Play the next game
                currentGame++;
                step=0;
                currentFitness += runTrial();                // Send message with current fitness
                float msg[] = {currentFitness, 0.0f};
                byte[] msgInBytes = Util.float2Byte(msg);
                emitter.send(msgInBytes);
                System.out.println("Current game: "+currentGame);
            } else {
                // Send message to indicate end of trial - next actor will be called
                float msg[] = {currentFitness, 1};
                byte[] msgInBytes = Util.float2Byte(msg);
                emitter.send(msgInBytes);
                // Reinitialize counter
                step = 0;
                currentGame = 0;
            }

        }
    }


    /**
     * A single trial during which one action is performed.
     *
     * @return Returns current fitness score
     */
    private float runTrial() {

        double[] outputs = new double[NB_OUTPUTS];

        updateSenorReadings();
        run_neural_network(states, outputs);

        speed[LEFT] = SPEED_RANGE * outputs[0];
        speed[RIGHT] = SPEED_RANGE * outputs[1];

        // Set wheel speeds to output values
        robot.setSpeed(speed[LEFT], speed[RIGHT]);

        // Stop the robot if it is against an obstacle
        for (int i = 0; i < NB_DIST_SENS; i++) {
            double tmpps = (((ps[i].getValue()) - ps_offset[i]) < 0) ? 0 : ((ps[i].getValue()) - ps_offset[i]);

            if (OBSTACLE_THRESHOLD < tmpps) {// proximity sensors
                speed[LEFT] = 0;
                speed[RIGHT] = 0;
                break;
            }
        }

        updateSenorReadings();

        return computeFitness(speed, position, maxIRActivation, distanceTravelled);
    }


    /**
     * Method to calculate fitness score
     *
     * @param speed
     * @param position
     * @param maxIRActivation
     * @return
     */
    public float computeFitness(double[] speed, double[] position, double maxIRActivation, double distanceTravelled) {

        float fitness = 0.0f;

        try {
            fitness = (float) ((populationOfGames[currentGame].getConstants()[0] * util.Util.mean(speed)) * (populationOfGames[currentGame].getConstants()[1] - Math.sqrt(Math.abs(speed[LEFT] - speed[RIGHT])) *
                    (populationOfGames[currentGame].getConstants()[2] - util.Util.normalize(0, 4000, maxIRActivation))) + (populationOfGames[currentGame].getConstants()[3] * distanceTravelled));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return fitness;
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
     * Based on the fitness of the last generation, generate a new populationOfNN of genomes for the next generation.
     */
    private void createNewPopulation() {

        Game[] newpop = new Game[GAME_POP_SIZE];
        for (int i = 0; i < newpop.length; i++) {
            newpop[i] = new Game(true);
        }
        double elitism_counter = GAME_POP_SIZE * ELITISM_RATIO;
        int i, j;

        // Create new populationOfNN
        for (i = 0; i < GAME_POP_SIZE; i++) {

            // The elitism_counter best individuals are simply copied to the new populationOfNN
            if (i < elitism_counter) {
                for (j = 0; j < NB_CONSTANTS; j++)
                    newpop[i].setConstants(j, populationOfGames[(int) sortedfitnessGames[i][1]].getConstants()[j]);
            }
            // The other individuals are generated through the crossover of two parents
            else {

                // Select non-elitist individual
                int ind1;
                ind1 = (int) (elitism_counter + random.nextFloat() * (GAME_POP_SIZE * REPRODUCTION_RATIO - elitism_counter));

                // If we will do crossover, select a second individual
                if (random.nextFloat() < CROSSOVER_PROBABILITY) {
                    int ind2;
                    do {
                        ind2 = (int) (elitism_counter + random.nextFloat() * (GAME_POP_SIZE * REPRODUCTION_RATIO - elitism_counter));
                    } while (ind1 == ind2);
                    ind1 = (int) sortedfitnessGames[ind1][1];
                    ind2 = (int) sortedfitnessGames[ind2][1];
                    newpop[i].crossover(ind1, ind2, newpop[i], NB_CONSTANTS, populationOfGames);
                } else { //if no crossover was done, just copy selected individual directly
                    for (j = 0; j < NB_CONSTANTS; j++)
                        newpop[i].setConstants(j, populationOfGames[(int) sortedfitnessGames[ind1][1]].getConstants()[j]);
                }
            }
        }

        // Mutate new populationOfNN and copy back to pop
        for (i = 0; i < GAME_POP_SIZE; i++) {
            if (i < elitism_counter) { //no mutation for elitists
                for (j = 0; j < NB_CONSTANTS; j++) {
                    populationOfGames[i].copy(newpop[i]);
                }
            } else { // Mutate others with probability per gene
                for (j = 0; j < NB_CONSTANTS; j++)
                    if (random.nextFloat() < MUTATION_PROBABILITY)
                        populationOfGames[i].setConstants(j, populationOfGames[i].mutate(GENE_MIN, GENE_MAX, newpop[i].getConstants()[j], MUTATION_SIGMA));
                    else
                        populationOfGames[i].copy(newpop[i]);
            }

            // Reset fitness
            fitnessGames[i] = 0;
        }
        return;
    }


    /**
     * Read values from sensors into arrays.
     */
    private void updateSenorReadings() {

        maxIRActivation = 0;
        for (int j = 0; j < NB_INPUTS; j++) {
            states[j] = ps[j].getValue() - ps_offset[j] < 0 ? 0 : (ps[j].getValue() - (ps_offset[j]) / PS_RANGE);
            //get max IR activation
            if (states[j] > maxIRActivation) maxIRActivation = states[j];
        }

        for (int i = 0; i < floorSensorsNo; i++) {
            fs_value[i] = fs[i].getValue();
        }

        states[8] = fs_value[0];
        states[9] = fs_value[1];
        states[10] = fs_value[2];

        //Get position of the e-puck
        position = gps.getValues();

    }

    private void run_neural_network(double[] inputs, double[] outputs) {
        int i, j;
        int weight_counter = 0;

        for (i = 0; i < NB_OUTPUTS; i++) {
            double sum = 0.0;
            for (j = 0; j < NB_INPUTS; j++) {
                sum += inputs[j] * weights[weight_counter];
                weight_counter++;
            }
            outputs[i] = Math.tanh(sum + weights[weight_counter]);
            weight_counter++;
        }
    }


    /**
     * Method to initialise e-puck's sensors and data structures/variables.
     */
    public void reset() {

        int i, j;
        mode = 1;
        step = 0;
        currentGame = 0;

        // Games
        populationOfGames = new Game[GAME_POP_SIZE];
        for (i = 0; i < GAME_POP_SIZE; i++) populationOfGames[i] = new Game(true);
        fitnessGames = new double[GAME_POP_SIZE];
        for (i = 0; i < GAME_POP_SIZE; i++) fitnessGames[i] = 0.0;
        sortedfitnessGames = new double[GAME_POP_SIZE][2];
        for (i = 0; i < GAME_POP_SIZE; i++) {
            for (j = 0; j < 2; j++) {
                sortedfitnessGames[i][j] = 0.0;
            }
        }

        /* Initialise IR proximity sensors */
        ps = new DistanceSensor[proximitySensorsNo];
        ps[0] = getDistanceSensor("ps0");
        ps[0].enable(TIME_STEP);
        ps[1] = getDistanceSensor("ps1");
        ps[1].enable(TIME_STEP);
        ps[2] = getDistanceSensor("ps2");
        ps[2].enable(TIME_STEP);
        ps[3] = getDistanceSensor("ps3");
        ps[3].enable(TIME_STEP);
        ps[4] = getDistanceSensor("ps4");
        ps[4].enable(TIME_STEP);
        ps[5] = getDistanceSensor("ps5");
        ps[5].enable(TIME_STEP);
        ps[6] = getDistanceSensor("ps6");
        ps[6].enable(TIME_STEP);
        ps[7] = getDistanceSensor("ps7");
        ps[7].enable(TIME_STEP);

        ps_offset = new float[NB_DIST_SENS];
        for (i = 0; i < ps_offset.length; i++) {
            ps_offset[i] = PS_OFFSET_SIMULATION[i];
        }

        maxIRActivation = 0;

        /* Enable GPS sensor to determine position */
        gps = new GPS("gps");
        gps.enable(TIME_STEP);
        position = new double[3];
        for (i = 0; i < position.length; i++) {
            position[i] = 0.0f;
        }

        /* Initialise LED lights */
        for (i = 0; i < ledsNo; i++) {
            led[i] = getLED("led" + i);
        }

        /* Initialise IR floor sensors */
        fs = new DistanceSensor[floorSensorsNo];
        for (i = 0; i < fs.length; i++) {
            fs[i] = getDistanceSensor("fs" + i);
            fs[i].enable(TIME_STEP);
        }

        /* Initialise states array */
        for (i = 0; i < states.length; i++) {
            states[i] = 0.0;
        }
        speed = new double[2];
        // Speed initialization
        speed[LEFT] = 0;
        speed[RIGHT] = 0;

        emitter = getEmitter("emitterepuck");
        receiver = getReceiver("receiver");
        receiver.enable(TIME_STEP);

        gameEmitter = getEmitter("gamesemitterepuck");
        gameEmitter.setChannel(1);
        gameReceiver = getReceiver("gamesreceiverepuck");
        gameReceiver.setChannel(1);
        gameReceiver.enable(TIME_STEP);

        weights = new float[NB_WEIGHTS];

        System.out.println("e-puck has been initialised.");
    }


    public static void main(String[] args) {
        EpuckController controller = new EpuckController();
        controller.reset();
        controller.run();
    }
}

