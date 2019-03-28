package ZikaModel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class ZikaInfectionState implements SimulationState {

    private double temperature;

    // Parametros de la simulación
    private double[] cellDensity;
    private double[] initialInfectedCellPercentage;
    private double[] viralReach;
    private double[] infectionRate;
    private double[] mNeptuneEffectiveness;
    private double[] initialProbabilityOfDeath;
    private double[] initialProbabilityOfChromatinCondensation;
    private double[] markerDetectionThreashold;
    private double totalCells;

    // Resultados de la simulación
    private ArrayList<Double> deadCells = new ArrayList<>();
    private ArrayList<Double> condensedCells = new ArrayList<>();
    private ArrayList<Double> aliveCells = new ArrayList<>();

    /* App.app().command("set cell-density 10.00");                                    // Min: 0 Max: 20 Inc: 2
            App.app().command("set initial-infected-cell-percentage 30");                   // Min: 0 Max: 100.0 Inc: 3
            App.app().command("set viral-reach 2.5");                                       // Min: 0 Max: 3 Inc: 1
            App.app().command("set infection-rate 10.00");                                  // Min: 0 Max: 15 Inc: 1
            App.app().command("set mNeptune-effectiveness 10.00");                          // Min: 0 Max: 400 Inc: 2
            App.app().command("set initial-probability-of-death 1.005");                    // Min: 0 Max: 2 Inc: 3
            App.app().command("set initial-probability-of-chromatin-condensation 2");       // Min: 0 Max: 2.5 Inc: 2 /5
            App.app().command("set marker-detection-threashold 0.05");                      // Min: 0 Max: 1 Inc: 2*/

    public ZikaInfectionState(double[] cellDensity, double[] initialInfectedCellPercentage, double[] viralReach, double[] infectionRate, double[] mNeptuneEffectiveness, double[] initialProbabilityOfDeath, double[] initialProbabilityOfChromatinCondensation, double[] markerDetectionThreashold, double totalCells) {
        this.cellDensity = cellDensity;
        this.initialInfectedCellPercentage = initialInfectedCellPercentage;
        this.viralReach = viralReach;
        this.infectionRate = infectionRate;
        this.mNeptuneEffectiveness = mNeptuneEffectiveness;
        this.initialProbabilityOfDeath = initialProbabilityOfDeath;
        this.initialProbabilityOfChromatinCondensation = initialProbabilityOfChromatinCondensation;
        this.markerDetectionThreashold = markerDetectionThreashold;
        this.totalCells = totalCells;
    }

    public ZikaInfectionState(double[] cellDensity, double[] initialInfectedCellPercentage, double[] viralReach, double[] infectionRate, double[] mNeptuneEffectiveness, double[] initialProbabilityOfDeath, double[] initialProbabilityOfChromatinCondensation, double[] markerDetectionThreashold) {
        this.cellDensity = cellDensity;
        this.initialInfectedCellPercentage = initialInfectedCellPercentage;
        this.viralReach = viralReach;
        this.infectionRate = infectionRate;
        this.mNeptuneEffectiveness = mNeptuneEffectiveness;
        this.initialProbabilityOfDeath = initialProbabilityOfDeath;
        this.initialProbabilityOfChromatinCondensation = initialProbabilityOfChromatinCondensation;
        this.markerDetectionThreashold = markerDetectionThreashold;
        this.totalCells = 0;
    }

    public ZikaInfectionState(ZikaInfectionState source) {
        this.cellDensity = source.getCellDensity().clone();
        this.initialInfectedCellPercentage = source.getInitialInfectedCellPercentage().clone();
        this.viralReach = source.getViralReach().clone();
        this.infectionRate = source.getInfectionRate();
        this.mNeptuneEffectiveness = source.getmNeptuneEffectiveness().clone();
        this.initialProbabilityOfDeath = source.getInitialProbabilityOfDeath().clone();
        this.initialProbabilityOfChromatinCondensation = source.getInitialProbabilityOfChromatinCondensation().clone();
        this.markerDetectionThreashold = source.getMarkerDetectionThreashold().clone();
        this.temperature = source.getTemperature();
        this.totalCells = source.getTotalCells();
    }

    public static ZikaInfectionState clone(ZikaInfectionState zikaInfectionState) {
        return new ZikaInfectionState(zikaInfectionState.getCellDensity().clone(),
                zikaInfectionState.getInitialInfectedCellPercentage().clone(),
                zikaInfectionState.getViralReach().clone(),
                zikaInfectionState.getInfectionRate().clone(),
                zikaInfectionState.getmNeptuneEffectiveness().clone(),
                zikaInfectionState.getInitialProbabilityOfDeath().clone(),
                zikaInfectionState.getInitialProbabilityOfChromatinCondensation().clone(),
                zikaInfectionState.getMarkerDetectionThreashold().clone(),
                zikaInfectionState.totalCells);
    }

    public ArrayList<Double> getDeadCells() {
        return deadCells;
    }

    public void setDeadCells(ArrayList<Double> deadCells) {
        this.deadCells = deadCells;
    }

    public ArrayList<Double> getCondensedCells() {
        return condensedCells;
    }

    public void setCondensedCells(ArrayList<Double> condensedCells) {
        this.condensedCells = condensedCells;
    }

    public ArrayList<Double> getAliveCells() {
        return aliveCells;
    }

    public void setAliveCells(ArrayList<Double> aliveCells) {
        this.aliveCells = aliveCells;
    }

    public double getTotalCells() {
        return totalCells;
    }

    public void setTotalCells(double totalCells) {
        this.totalCells = totalCells;
    }

    private void aneal(double[] parameter, double variationPound) {
        Random random = new Random();

        // Variamos positivo o negativamente
        variationPound = random.nextBoolean() ? variationPound : -variationPound;
        // Variamos el valor
        parameter[0] = parameter[0] + (parameter[0] * variationPound * random.nextDouble());
        // Aplicamos restricciones de umbrales
        if (parameter[0] > parameter[1]) parameter[0] = parameter[1];
        else if (parameter[0] < parameter[2]) parameter[0] = parameter[2];

        parameter[0] = round(parameter[0], parameter[3]);

    }

    private double round(double value, double places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(Double.valueOf(places).intValue(), RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @Override
    public SimulationState anneal(double variationPound) {
        // Exitamos calentamos un poco los parametros
        aneal(this.cellDensity, variationPound);
        aneal(this.initialInfectedCellPercentage, variationPound);
        aneal(this.viralReach, variationPound);
        aneal(this.infectionRate, variationPound);
        aneal(this.mNeptuneEffectiveness, variationPound);
        aneal(this.initialProbabilityOfDeath, variationPound);
        aneal(this.initialProbabilityOfChromatinCondensation, variationPound);
        aneal(this.markerDetectionThreashold, variationPound);
        return this;
    }

    @Override
    public Double getTemperature() {
        return temperature;
    }

    @Override
    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    @Override
    public Double cool(Double decreasing) {
        temperature -= decreasing;
        return temperature;
    }

    public double[] getCellDensity() {
        return cellDensity;
    }

    public void setCellDensity(double[] cellDensity) {
        this.cellDensity = cellDensity;
    }

    public double[] getInitialInfectedCellPercentage() {
        return initialInfectedCellPercentage;
    }

    public void setInitialInfectedCellPercentage(double[] initialInfectedCellPercentage) {
        this.initialInfectedCellPercentage = initialInfectedCellPercentage;
    }

    public double[] getViralReach() {
        return viralReach;
    }

    public void setViralReach(double[] viralReach) {
        this.viralReach = viralReach;
    }

    public double[] getInfectionRate() {
        return infectionRate;
    }

    public void setInfectionRate(double[] infectionRate) {
        this.infectionRate = infectionRate;
    }

    public double[] getmNeptuneEffectiveness() {
        return mNeptuneEffectiveness;
    }

    public void setmNeptuneEffectiveness(double[] mNeptuneEffectiveness) {
        this.mNeptuneEffectiveness = mNeptuneEffectiveness;
    }

    public double[] getInitialProbabilityOfDeath() {
        return initialProbabilityOfDeath;
    }

    public void setInitialProbabilityOfDeath(double[] initialProbabilityOfDeath) {
        this.initialProbabilityOfDeath = initialProbabilityOfDeath;
    }

    public double[] getInitialProbabilityOfChromatinCondensation() {
        return initialProbabilityOfChromatinCondensation;
    }

    public void setInitialProbabilityOfChromatinCondensation(double[] initialProbabilityOfChromatinCondensation) {
        this.initialProbabilityOfChromatinCondensation = initialProbabilityOfChromatinCondensation;
    }

    public double[] getMarkerDetectionThreashold() {
        return markerDetectionThreashold;
    }

    public void setMarkerDetectionThreashold(double[] markerDetectionThreashold) {
        this.markerDetectionThreashold = markerDetectionThreashold;
    }

    @Override
    public String toString() {
        return "ZikaInfectionState{\n" +
                "temperature=" + temperature + "\n" +
                "cellDensity=" + Arrays.toString(cellDensity) + "\n" +
                "initialInfectedCellPercentage=" + Arrays.toString(initialInfectedCellPercentage) + "\n" +
                "viralReach=" + Arrays.toString(viralReach) + "\n" +
                "infectionRate=" + Arrays.toString(infectionRate) + "\n" +
                "mNeptuneEffectiveness=" + Arrays.toString(mNeptuneEffectiveness) + "\n" +
                "initialProbabilityOfDeath=" + Arrays.toString(initialProbabilityOfDeath) + "\n" +
                "initialProbabilityOfChromatinCondensation=" + Arrays.toString(initialProbabilityOfChromatinCondensation) + "\n" +
                "markerDetectionThreashold=" + Arrays.toString(markerDetectionThreashold) + "\n" +
                "totalCells=" + totalCells + "\n" +
                "deadCells=" + deadCells + "\n" +
                "condensedCells=" + condensedCells + "\n" +
                "aliveCells=" + aliveCells + "\n" +
                '}';
    }
}
