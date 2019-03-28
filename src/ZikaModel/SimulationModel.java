package ZikaModel;

public interface SimulationModel {
    /**
     * Corre el modelo de simulación
     *
     * @return Retorna el estado del sistema
     */
    SimulationState run(SimulationState simulationState);
}
