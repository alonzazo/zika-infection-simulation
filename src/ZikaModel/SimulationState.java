package ZikaModel;

public interface SimulationState {
    SimulationState anneal(double variationPound);

    Double getTemperature();

    void setTemperature(Double temperature);

    Double cool(Double decreasing);
}
