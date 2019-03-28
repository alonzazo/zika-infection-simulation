import ZikaModel.*;
import org.nlogo.app.App;
import org.nlogo.core.LogoList;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Properties;

/**
 * *************************************************************************************** ZIKA-INFECTION-SIMULATION
 * Oscar Azofeifa B50770 - Diego Mora B54714 - Carlos Portuguez B55519
 * Paradigmas Computacionales
 * Universidad de Costa Rica
 * *****************************************************************************************************************
 */
public class MainController {
    public static void main(String[] args) {

            /**
             * Simulated annealing implementation
             */
            Properties properties = new Properties();
            properties.setProperty("Tmax","0.4929");   // Max (starting) temperature
            properties.setProperty("Tmin","0");       // Min (ending) temperature
            properties.setProperty("steps","50");    // Number of iterations
            properties.setProperty("updates","1");    // Number of updates (by default an update prints to stdout)
            properties.setProperty("learningRate","0.25"); // Learning Pound

        SimulatedAnnealer simulatedAnnealer = new SimulatedAnnealer(
                // Se descibe la función objetivo
                (SimulationState simulationState) -> {
                    // Se castea el simulation state a una clase concreta de estado del Zika
                    ZikaInfectionState zikaInfectionState = (ZikaInfectionState) simulationState;

                    // Utilizando la versión con GUI
                    // SimulationModel simulationModel = new GUISimulation(120);

                    // Utilizando la versión sin GUI
                    SimulationModel simulationModel = new HeadlessSimulation(120);

                    // Corremos la simulación y guardamos los resultados en la variable
                    zikaInfectionState = (ZikaInfectionState) simulationModel.run(zikaInfectionState);

                    /**
                     * Calculo de la energía
                     * Calculamos la diferencia del error entre esta corrida del modelo y los resultados de laboratorio
                     */
                    // Por cada reporte hecho se calcula la diferencia con los datos de laboratorio

                    double[] experimentalDeadCells = {0.0, 0.0, 0.00940963366, 0.08378955211, 0.3109314857, 0.6811579725};
                    double[] experimentalCondensedCells = {0.0, 0.0, 0.00189316276, 0.02261203413, 0.1018365033, 0.1110091982};
                    double[] experimentalAliveCells = {0.0, 0.0, 0.02196026959, 0.1349548213, 0.2170586877, 0.2078328294};

                    // Calculamos los errores cuadráticos medios
                    Double meanSquaredErrorDeadCells = Math.sqrt(Math.pow(zikaInfectionState.getDeadCells().get(0) - experimentalDeadCells[0], 2) +
                            Math.pow(zikaInfectionState.getDeadCells().get(1) - experimentalDeadCells[1], 2) +
                            Math.pow(zikaInfectionState.getDeadCells().get(2) - experimentalDeadCells[2], 2) +
                            Math.pow(zikaInfectionState.getDeadCells().get(3) - experimentalDeadCells[3], 2) +
                            Math.pow(zikaInfectionState.getDeadCells().get(4) - experimentalDeadCells[4], 2) +
                            Math.pow(zikaInfectionState.getDeadCells().get(5) - experimentalDeadCells[5], 2));

                    Double meanSquaredErrorCondensedCells = Math.sqrt(Math.pow(zikaInfectionState.getCondensedCells().get(0) - experimentalCondensedCells[0], 2) +
                            Math.pow(zikaInfectionState.getCondensedCells().get(1) - experimentalCondensedCells[1], 2) +
                            Math.pow(zikaInfectionState.getCondensedCells().get(2) - experimentalCondensedCells[2], 2) +
                            Math.pow(zikaInfectionState.getCondensedCells().get(3) - experimentalCondensedCells[3], 2) +
                            Math.pow(zikaInfectionState.getCondensedCells().get(4) - experimentalCondensedCells[4], 2) +
                            Math.pow(zikaInfectionState.getCondensedCells().get(5) - experimentalCondensedCells[5], 2));

                    Double meanSquaredErrorAliveCell = Math.sqrt(Math.pow(zikaInfectionState.getAliveCells().get(0) - experimentalAliveCells[0], 2) +
                            Math.pow(zikaInfectionState.getAliveCells().get(1) - experimentalAliveCells[1], 2) +
                            Math.pow(zikaInfectionState.getAliveCells().get(2) - experimentalAliveCells[2], 2) +
                            Math.pow(zikaInfectionState.getAliveCells().get(3) - experimentalAliveCells[3], 2) +
                            Math.pow(zikaInfectionState.getAliveCells().get(4) - experimentalAliveCells[4], 2) +
                            Math.pow(zikaInfectionState.getAliveCells().get(5) - experimentalAliveCells[5], 2));

                    // Devolvemos la media de los errores
                    return (meanSquaredErrorDeadCells + meanSquaredErrorCondensedCells + meanSquaredErrorAliveCell) / 3;
                },
                // Se describe el estado inicial de los parámetros
                new ZikaInfectionState(
                        // Attribute Val     Max     Min     Inc
                        new double[]{18, 20.0, 0.0, 2.0},   // cellDensity
                        new double[]{10, 100.0, 0.0, 3.0},   // initialInfectedCellPercentage
                        new double[]{2.7, 3.0, 0.0, 1.0},   // viralReach
                        new double[]{1.5, 15.0, 0.0, 1.0},   // infectionRate
                        new double[]{40, 400.0, 0.0, 2.0},   // mNeptuneEffectiveness
                        new double[]{1.8, 2.0, 0.0, 3.0},   // initialProbabilityOfDeath
                        new double[]{0.25, 2.5, 0.0, 2.0},   // initialProbabilityOfChromatinCondensation
                        new double[]{0.9, 1.0, 0.0, 2.0}    // markerDetectionThreshold
                ),
                properties);
        // Optimizamos el modelo
        simulatedAnnealer.optimize();
    }
}