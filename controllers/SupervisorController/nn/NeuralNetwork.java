package nn;

import utils.Util;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: annapawlicka
 * Date: 02/03/2013
 * Time: 20:25
 * Neural network which transforms the sensory inputs received from the sensors of the e-puck into motor commands.
 * Inputs are scaled to fit the range [0,1] and the neuronal transfer function is chosen to be a hyperbolic tangent
 * (tanh).
 */
public class NeuralNetwork {

    private int weightsNo;
    private int inputsNo;
    private int outputsNo;
    private float[] weights;
    private int weightMin = -1;
    private int weightMax = 1;

    private float[] outputs;

    private Random random = new Random();

    public NeuralNetwork(int inputsNo, int outputsNo) {

        this.inputsNo = inputsNo;
        this.outputsNo = outputsNo;
        this.weightsNo = this.inputsNo * this.outputsNo + this.outputsNo;
        this.weights = new float[weightsNo];
        this.outputs = new float[this.outputsNo];
        initialise();
    }

    public void initialise(){

        // Initialise weights
        for(int i=0; i< weightsNo; i++){
            weights[i] = (float)((weightMax-weightMin)*random.nextFloat()-(weightMax-weightMin)/2.0);
        }

        // Initialise outputs
        for(int j=0; j< outputsNo; j++){
            outputs[j] = 0.0f;
        }
    }

    /**
     *  Method to run neural network.
     */
    public void runNeuralNetwork(double[] inputs) {

        int weight_counter = 0;
        for (int i = 0; i < outputsNo; i++) {
            float sum = 0.0f;
            for (int j = 0; j < inputsNo; j++) {
                sum += inputs[j] * weights[weight_counter];
                weight_counter++;
            }
            outputs[i] = (float)(Math.tanh((sum + weights[weight_counter])));
            weight_counter++;
        }
    }

    /**
     * Method to create an offspring
     *
     * @param parent - parent whose information is being copied over to offspring
     */
    public void copy(NeuralNetwork parent) {

        this.inputsNo = parent.inputsNo;
        this.outputsNo = parent.outputsNo;
        this.outputs = Util.copy(parent.outputs);

    	/* Transformation */
        this.weights = Util.copy(parent.weights);

    }


    /**
     * Mehod to mutate weights of the neural network
     *
     * @param min
     * @param max
     * @param gene Weights
     * @return
     */
    public float mutate(float min, float max, float gene, int GENE_MIN, int GENE_MAX, double MUTATION_SIGMA) {
        float x1, x2, w, y1;

        do {
            x1 = (float) (GENE_MAX - GENE_MIN * random.nextFloat() - (GENE_MAX - GENE_MIN) / 2.0);
            x2 = (float) (GENE_MAX - GENE_MIN * random.nextFloat() - (GENE_MAX - GENE_MIN) / 2.0);
            w = x1 * x1 + x2 * x2;
        } while (w > 1.0 || w == 0);

        y1 = (float) (gene + MUTATION_SIGMA * x1 * Math.sqrt((-2.0 * Math.log(w)) / w));

        if (y1 > max) return max;
        if (y1 < min) return min;

        return y1;
    }

    /**
     * Method to crossover two individuals
     *
     * @param ind1    Individual 1
     * @param ind2    Individual 2
     * @param new_ind New individual that will be updated with crossovered details.
     */
    public void crossover(int ind1, int ind2, NeuralNetwork new_ind, float NB_GENES, NeuralNetwork[] population) {

        int crossover_point = 0;
        float nb_genes = NB_GENES;

        crossover_point = (int) Math.floor(nb_genes * random.nextFloat());

        for (int i = 0; i < NB_GENES; i++) {
            if (i <= crossover_point) new_ind.setWeights(i, population[ind1].getWeights()[i]);
            else new_ind.setWeights(i, population[ind2].getWeights()[i]);
        }
    }


    /* Getters and setters */

    public float[] getOutputs(){
        return outputs;
    }

    public void setOutputsNo(int n){
        outputsNo = n;
    }

    public void setInputsNo(int n){
        inputsNo = n;
    }

    public void setWeightsNo(int n){
        weightsNo = n;
    }

    public int getOutputsNo(){
        return outputsNo;
    }

    public int getInputsNo(){
        return inputsNo;
    }

    public int getWeightsNo(){
        return weightsNo;
    }

    public float[] getWeights(){
        return weights;
    }

    public void setWeights(int index, float weight){
        weights[index] = weight;
    }

}
