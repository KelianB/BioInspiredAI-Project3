package ga;

/**
 * Interface used to represent a class that reads problem instances.
 * @author Kelian Baert & Caroline de Pourtales
 */
public interface IProblemReader {
	/**
	 * Reads a problem instance from the given problem name.
	 * @param problemName - The name of the problem instance (usually the file name)
	 * @return a problem instance
	 * @throws ProblemReadingException
	 */
	public IProblemInstance readProblem(String problemName);
}
