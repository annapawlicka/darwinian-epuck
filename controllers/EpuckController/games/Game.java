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

    private float [] constants;    // Constants used in evolution


    Random rand = new Random();

    public Game(boolean init) {
        constants = new float[4];
        if(init) initialise();
    }

    public void initialise(){
        for(int i=0; i< constants.length; i++) constants[i] = rand.nextFloat();
    }



    /**
     * Method to mutate game
     * @param GENE_MIN
     * @param GENE_MAX
     * @param gene
     * @param MUTATION_SIGMA
     * @return
     */
    public float mutate(int GENE_MIN, int GENE_MAX, double gene, double MUTATION_SIGMA){

        float x1, x2, w, y1;
        do {
            x1 = (float) (GENE_MAX - GENE_MIN * rand.nextDouble() - (GENE_MAX - GENE_MIN) / 2.0);
            x2 = (float) (GENE_MAX - GENE_MIN * rand.nextDouble() - (GENE_MAX - GENE_MIN) / 2.0);
            w = x1 * x1 + x2 * x2;
        } while (w > 1.0 || w == 0);

        y1 = (float)(gene + MUTATION_SIGMA * x1 * Math.sqrt((-2.0 * Math.log(w)) / w));

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

    /**
     * Method to create an offspring
     *
     * @param parent - parent whose information is being copied over to offspring
     */
    public void copy(Game parent) {

    	/* Transformation */
        this.constants = Util.copy(parent.constants);

    }

    /* Getters and setters */

    public float[] getConstants(){
        return constants;
    }

    public void setConstants(int i, float constant){
        constants[i] = constant;

    }

    public String toString(){
        String s = "";
        for(int i=0; i< constants.length; i++) s+=" "+constants[i];

        return s;
    }


}
