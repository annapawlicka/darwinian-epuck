package games;

/**
 * Created with IntelliJ IDEA.
 * User: annapawlicka
 * Date: 06/03/2013
 * Time: 22:06
 * Interface to implementations of fitness functions.
 */
public interface FitnessFunction {

    /**
     * A method to compute fitness score, using var args and generics, as each fitness function implementation
     * may have different number and type of arguments.
     * @param arr Arguments depend on the implementation
     * @param <T>
     * @return   Returns computed fitness score (double)
     */
    public <T> double computeFitness(T arr);
}
