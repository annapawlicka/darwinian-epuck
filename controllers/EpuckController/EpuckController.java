import com.cyberbotics.webots.controller.*;
import util.Util;

/**
 * Created with IntelliJ IDEA.
 * User: annapawlicka
 * Date: 01/03/2013
 * Time: 19:50
 * E-puck controller that works with NN algorithm.
 */
public class EpuckController extends Robot {

    // Global variables
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
    private double fitnessConstants[];
    private float currentFitness;

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
    private Emitter gamesEmitter;
    private Receiver receiver;
    private Receiver gamesReceiver;

    private int step;
    private boolean ifAllGamesPlayed;

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

            //if(ifAllGamesPlayed){
                int n = gamesReceiver.getQueueLength();
                // Wait for new genome
                if (n > 0) {
                    byte[] genes = gamesReceiver.getData();
                    System.out.println("Received games.");
                    // Set neural network weights
                    for (i = 0; i < NB_CONSTANTS; i++) fitnessConstants[i] = genes[i];
                    gamesReceiver.nextPacket();
                }
            //}
            // If we're testing a new genome, receive weights and initialize trial
            if (step == 0) {
                n = receiver.getQueueLength();
                // Wait for new genome
                if (n > 0) {
                    byte[] genes = receiver.getData();
                    // Set neural network weights
                    for (i = 0; i < NB_WEIGHTS; i++) weights[i] = genes[i];
                    receiver.nextPacket();
                }
                currentFitness = 0;
            }

            step++;

            if (step < TRIAL_DURATION / TIME_STEP) {
                // Drive robot
                currentFitness += runTrial();                // Send message with current fitness
                float msg[] = {currentFitness, 0.0f};
                byte[] msgInBytes = Util.float2Byte(msg);
                emitter.send(msgInBytes);
            } else {
                // Send message to indicate end of trial
                float msg[] = {currentFitness, 1};
                byte[] msgInBytes = Util.float2Byte(msg);
                emitter.send(msgInBytes);
                // Reinitialize counter
                step = 0;
                //ifAllGamesPlayed = true;    // Indicate that all games have been played
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
     * @param speed
     * @param position
     * @param maxIRActivation
     * @return
     */
    public float computeFitness(double [] speed, double [] position, double maxIRActivation, double distanceTravelled) {

        float fitness = 0.0f;

        try {
            fitness = (float) ((fitnessConstants[0] * util.Util.mean(speed)) * (fitnessConstants[1] - Math.sqrt(Math.abs(speed[LEFT] - speed[RIGHT])) *
                    (fitnessConstants[2] - util.Util.normalize(0, 4000, maxIRActivation))) + (fitnessConstants[3] * distanceTravelled));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


        return fitness;
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

        int i;
        mode = 1;
        step = 0;

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
        gamesEmitter = getEmitter("gamesemitterepuck");
        //gamesEmitter.setChannel(1);
        gamesReceiver = getReceiver("gamesreceiverepuck");
        gamesReceiver.enable(TIME_STEP);
        //gamesReceiver.setChannel(1);

        weights = new float[NB_WEIGHTS];
        fitnessConstants = new double[NB_CONSTANTS];

        System.out.println("e-puck has been initialised.");
    }


    public static void main(String[] args) {
        EpuckController controller = new EpuckController();
        controller.reset();
        controller.run();
    }
}

