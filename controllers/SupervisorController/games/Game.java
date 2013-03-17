package games;

import util.Util;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: annapawlicka
 * Date: 06/03/2013
 * Time: 22:06
 * Game - representation of a fitness function that is genetically programmed.
 */
public class Game {

    private final int LEFT = 0;
    private final int RIGHT = 1;

    private double [] constants;    // Constants used in evolution


    Random rand = new Random();

    public Game(boolean init) {
        if(init) initialise();
    }

    public void initialise(){
        for(int i=0; i< constants.length; i++) constants[i] = rand.nextDouble();
    }


    /**
     * Method to calculate fitness score
     * @param speed
     * @param position
     * @param maxIRActivation
     * @return
     */
    public double computeFitness(double [] speed, double [] position, double maxIRActivation, double distanceTravelled) {

        double fitness = 0.0;

        try {
            fitness = (float) ((constants[0] * Util.mean(speed)) * (constants[1] - Math.sqrt(Math.abs(speed[LEFT] - speed[RIGHT])) *
                    (constants[2] - Util.normalize(0, 4000, maxIRActivation))) + (constants[3] * distanceTravelled));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


        return fitness;
    }

    /**
     * Method to mutate game
     * @param GENE_MIN
     * @param GENE_MAX
     * @param gene
     * @param MUTATION_SIGMA
     * @return
     */
    public double mutate(int GENE_MIN, int GENE_MAX, double gene, double MUTATION_SIGMA){

        double x1, x2, w, y1;
        do {
            x1 = (float) (GENE_MAX - GENE_MIN * rand.nextDouble() - (GENE_MAX - GENE_MIN) / 2.0);
            x2 = (float) (GENE_MAX - GENE_MIN * rand.nextDouble() - (GENE_MAX - GENE_MIN) / 2.0);
            w = x1 * x1 + x2 * x2;
        } while (w > 1.0 || w == 0);

        y1 = (double) (gene + MUTATION_SIGMA * x1 * Math.sqrt((-2.0 * Math.log(w)) / w));

        if (y1 > GENE_MAX) return GENE_MAX;
        if (y1 < GENE_MIN) return GENE_MIN;

        return y1;
    }

    /**
     * Method to crossover two games
     * @param ind1
     * @param ind2
     * @param new_ind
     * @param NB_GENES
     * @param population
     */
    public void crossover(int ind1, int ind2, Game new_ind, float NB_GENES, Game[] population) {

        int crossover_point = 0;
        float nb_genes = NB_GENES;

        crossover_point = (int) Math.floor(nb_genes * rand.nextFloat());

        for (int i = 0; i < NB_GENES; i++) {
            if (i <= crossover_point) new_ind.setConstants(i, population[ind1].getConstants()[i]);
            else new_ind.setConstants(i, population[ind2].getConstants()[i]);
        }
    }

    public double[] getConstants(){
        return constants;
    }

    public void setConstants(int i, double constant){
        constants[i] = constant;

    }


}
