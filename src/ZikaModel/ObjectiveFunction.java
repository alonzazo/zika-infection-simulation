package ZikaModel;

public interface ObjectiveFunction {
    /**
     * Calcula la energía del sistema, también conocida como la función objetivo
     *
     * @param params
     * @return
     */
    Double calculate(SimulationState params);
}
