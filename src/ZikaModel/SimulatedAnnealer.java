package ZikaModel;

import java.util.LinkedList;
import java.util.Properties;
import java.util.Random;

public class SimulatedAnnealer {

    private ObjectiveFunction objectiveFunction;

    /**
     * Tmax = 25000.0  # Max (starting) temperature
     * Tmin = 2.5      # Min (ending) temperature
     * steps = 50000   # Number of iterations
     * updates = 100   # Number of updates (by default an update prints to stdout)
     */
    private SimulationState simulationState;
    private LinkedList<SimulationState> history;
    private StopCondition stopCondition;
    private double maxTemperature;
    private double minTemperature;
    private double learningRate;
    private int steps;
    private int updates;

    public SimulatedAnnealer(ObjectiveFunction objectiveFunction, SimulationState initialState, Properties hyperParameters) {
        this.objectiveFunction = objectiveFunction;
        this.simulationState = initialState;
        stopCondition = () -> false;
        history = new LinkedList<>();

        maxTemperature = Double.valueOf(hyperParameters.getProperty("Tmax"));
        minTemperature = Double.valueOf(hyperParameters.getProperty("Tmin"));
        steps = Integer.valueOf(hyperParameters.getProperty("steps"));
        updates = Integer.valueOf(hyperParameters.getProperty("updates"));
        learningRate = Double.valueOf(hyperParameters.getProperty("learningRate"));

    }

    public SimulatedAnnealer(ObjectiveFunction objectiveFunction, SimulationState initialState, Properties hyperParameters, StopCondition stopCondition) {
        this.objectiveFunction = objectiveFunction;
        this.simulationState = initialState;
        this.stopCondition = stopCondition;
        history = new LinkedList<>();

        maxTemperature = Double.valueOf(hyperParameters.getProperty("Tmax"));
        minTemperature = Double.valueOf(hyperParameters.getProperty("Tmin"));
        steps = Integer.valueOf(hyperParameters.getProperty("steps"));
        updates = Integer.valueOf(hyperParameters.getProperty("updates"));
        learningRate = Integer.valueOf(hyperParameters.getProperty("learningRate"));
    }

    /**
     * Optimiza un modelo usando temple simulado con respecto a una función objetivo.
     *
     * @return El estado optimizado
     */
    public SimulationState oneStep() {
        if (!stopCondition.verify()) {
            // Calentamos el estado un momento
            Random random = new Random();

            // Coeficiente de variación
            double variationPound = learningRate * (simulationState.getTemperature() - minTemperature) / (maxTemperature - minTemperature);
            SimulationState annealedState = new ZikaInfectionState((ZikaInfectionState) simulationState);
            annealedState.anneal(variationPound);
            // Comparamos si la energía del estado alterado se reduce
            Double newLevelEnergy = objectiveFunction.calculate(annealedState);
            Double currentLevelEnergy = objectiveFunction.calculate(simulationState);
            if (newLevelEnergy < currentLevelEnergy) {
                simulationState = annealedState;
            } else {
                //Calculamos la probabilidad de aceptación
                /**
                 * Definir esta probabilidad de aceptación para que con la
                 * temperatura inicial se produzca una probabilidad de 0.8
                 */
                Double acceptationProbability = Math.exp(-(newLevelEnergy - currentLevelEnergy) / simulationState.getTemperature());
                // Generamos un número aleatorio
                // Si es menor que la probabilidad de aceptación nos lo dejamos
                if (random.nextDouble() < acceptationProbability)
                    simulationState = annealedState;
                // Si no conservamos el estado
            }
            // Decremento lineal de la temperatura
            // simulationState.cool((maxTemperature - minTemperature) / steps);

            //Decremento geométrico de la temperatura
            simulationState.cool(simulationState.getTemperature() - (simulationState.getTemperature() * 0.99));
            // Lo agregamos a la historia
            history.add(simulationState);
        }
        return simulationState;
    }

    /**
     * Optimiza el modelo usando temple simulado hasta llegar al final de los pasos
     *
     * @return
     */
    public SimulationState optimize() {
        simulationState.setTemperature(this.maxTemperature);
        for (int i = 0; i < steps; i++)
            if (!stopCondition.verify()) {
                oneStep();

                //Reportamos el estado
                if (i % updates == 0){
                    System.out.println("*************************************REPORTE #" + (i/updates));
                    System.out.println(simulationState.toString());
                    System.out.println("Nivel de energía: " + objectiveFunction.calculate(simulationState));
                    System.out.println("**************************************************************");
                }
            }
            else break;
        return simulationState;
    }
}
