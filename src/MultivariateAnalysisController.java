import ZikaModel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;


public class MultivariateAnalysisController {
    public static void main(String[] argv) {

        // Inicializamos el mejor estado;
        ZikaInfectionState bestState = null;

        ObjectiveFunction objectiveFunction = (simulationState) -> {
            // Se castea el simulation state a una clase concreta de estado del Zika
            ZikaInfectionState zikaInfectionState = (ZikaInfectionState) simulationState;

            // Utilizando la versión con GUI
            // SimulationModel simulationModel = new GUISimulation(120);

            // Utilizando la versión sin GUI
            SimulationModel simulationModel = new HeadlessSimulation(120);

            // Corremos la simulación y guardamos los resultados en la variable
            zikaInfectionState = (ZikaInfectionState) simulationModel.run(zikaInfectionState);

            /*
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
        };

        // Corremos las simulaciones y recolectamos los datos
        HeadlessSimulation simulation = new HeadlessSimulation(120);

        try {
            // Creamos el archivo
            PrintWriter printer;

            for (byte i = (byte) 0b11010101; i != 127; i += 0b1) {

                printer = new PrintWriter(new FileOutputStream(
                        "multivariateAnalysisResults.txt", true));

                double[][] parameters = new double[8][];

                // Caso del primer bit
                if ((i & 0b1) != 0b0) {
                    parameters[0] = new double[]{2.0, 20.0, 0.0, 2.0};   // cellDensity
                } else {
                    parameters[0] = new double[]{18.0, 20.0, 0.0, 2.0};   // cellDensity
                }

                if ((i & 0b10) != 0b0) {
                    parameters[1] = new double[]{10.0, 100.0, 0.0, 3.0};   // initialInfectedCellPercentage
                } else {
                    parameters[1] = new double[]{90.0, 100.0, 0.0, 3.0};   // initialInfectedCellPercentage
                }

                if ((i & 0b100) != 0b0) {
                    parameters[2] = new double[]{0.3, 3.0, 0.0, 1.0};   // viralReach
                } else {
                    parameters[2] = new double[]{2.7, 3.0, 0.0, 1.0};   // viralReach
                }

                if ((i & 0b1000) != 0b0) {
                    parameters[3] = new double[]{1.5, 15.0, 0.0, 1.0};   // infectionRate
                } else {
                    parameters[3] = new double[]{13.5, 15.0, 0.0, 1.0};   // infectionRate
                }

                if ((i & 0b10000) != 0b0) {
                    parameters[4] = new double[]{40.0, 400.0, 0.0, 2.0};   // mNeptuneEffectiveness
                } else {
                    parameters[4] = new double[]{360.0, 400.0, 0.0, 2.0};   // mNeptuneEffectiveness
                }

                if ((i & 0b100000) != 0b0) {
                    parameters[5] = new double[]{0.2, 2.0, 0.0, 3.0};   // initialProbabilityOfDeath
                } else {
                    parameters[5] = new double[]{1.8, 2.0, 0.0, 3.0};   // initialProbabilityOfDeath
                }

                if ((i & 0b1000000) != 0b0) {
                    parameters[6] = new double[]{0.25, 2.5, 0.0, 2.0};   // initialProbabilityOfChromatinCondensation
                } else {
                    parameters[6] = new double[]{2.25, 2.5, 0.0, 2.0};   // initialProbabilityOfChromatinCondensation
                }

                if ((i & 0b10000000) != 0b0) {
                    parameters[7] = new double[]{0.1, 1.0, 0.0, 2.0};    // markerDetectionThreashold
                } else {
                    parameters[7] = new double[]{0.9, 1.0, 0.0, 2.0};    // markerDetectionThreashold
                }

                //ZikaInfectionState result = (ZikaInfectionState) simulation.run(new ZikaInfectionState(parameters[0], parameters[1], parameters[2], parameters[3], parameters[4], parameters[5], parameters[6], parameters[7]));
                // Jeifer parameters
                ZikaInfectionState result = (ZikaInfectionState) simulation.run(
                                        new ZikaInfectionState(new double[]{1.6, 2.5, 0.0, 2.0}, //cellDensity
                                        new double[]{0.0, 2.5, 0.0, 2.0},                   //initialInfectedCellPercetage
                                        new double[]{0.9743085, 2.5, 0.0, 2.0},             //viralReach
                                        new double[]{10.825650000000001, 2.5, 0.0, 2.0},    //infectionRate
                                        new double[]{294.63, 2.5, 0.0, 2.0},                //mNeptuneEffectivess
                                        new double[]{0.8696754, 2.5, 0.0, 2.0},             //initialProbabilityOfDeath
                                        new double[]{2.6416390274999997, 2.5, 0.0, 2.0},    //ChromatinCondesed
                                        new double[]{0.0, 2.5, 0.0, 2.0}));                 //markerDetectionThreshold

                String line = "#" + i + "\t| Parámetros:\t";

                line += "| CellDensity:\t" + result.getCellDensity()[0] + "\t";
                line += "| InitialInfectedCellPercentage:\t" + result.getInitialInfectedCellPercentage()[0] + "\t";
                line += "| ViralReach:\t" + result.getViralReach()[0] + "\t";
                line += "| InfectionRate:\t" + result.getInfectionRate()[0] + "\t";
                line += "| mNeptuneEffectiveness:\t" + result.getmNeptuneEffectiveness()[0] + "\t";
                line += "| InitialProbabilityOfDeath:\t" + result.getInitialProbabilityOfDeath()[0] + "\t";
                line += "| InitialProbabilityOfChromatinCondensation:\t" + result.getInitialProbabilityOfChromatinCondensation()[0] + "\t";
                line += "| MarkerDetectionThreashold:\t" + result.getMarkerDetectionThreashold()[0] + "\t";

                line += "| RESULTADOS:\t";
                line += "| Alive Cells:\t" + result.getAliveCells() + "\t";
                line += "| Condensed Cells:\t" + result.getCondensedCells() + "\t";
                line += "| Dead Cells:\t" + result.getDeadCells() + "\t";

                double currentEnergy = objectiveFunction.calculate(result);

                line += "| ENERGíA: \t" + currentEnergy + "\t|";

                if (null == bestState) {
                    bestState = result;
                } else if (currentEnergy < objectiveFunction.calculate(bestState)) {
                    bestState = result;
                }

                // Imprimimos
                System.out.println(line);
                printer.println(line);

                printer.close();
            }

            printer = new PrintWriter(new FileOutputStream(
                    new File("multivariateAnalysisResultsLastv2.txt"),
                    true));

            String line = "";

            line += "CellDensity:\t" + bestState.getCellDensity()[0] + "\t";
            line += "InitialInfectedCellPercentage:\t" + bestState.getInitialInfectedCellPercentage()[0] + "\t";
            line += "ViralReach:\t" + bestState.getViralReach()[0] + "\t";
            line += "InfectionRate:\t" + bestState.getInfectionRate()[0] + "\t";
            line += "mNeptuneEffectiveness:\t" + bestState.getmNeptuneEffectiveness()[0] + "\t";
            line += "InitialProbabilityOfDeath:\t" + bestState.getInitialProbabilityOfDeath()[0] + "\t";
            line += "InitialProbabilityOfChromatinCondensation:\t" + bestState.getInitialProbabilityOfChromatinCondensation()[0] + "\t";
            line += "MarkerDetectionThreashold:\t" + bestState.getMarkerDetectionThreashold()[0] + "\t";

            line += "RESULTADOS:\t";
            line += "Alive Cells:\t" + bestState.getAliveCells() + "\t";
            line += "Condensed Cells:\t" + bestState.getCondensedCells() + "\t";
            line += "Dead Cells:\t" + bestState.getDeadCells() + "\t";

            double currentEnergy = objectiveFunction.calculate(bestState);

            line += "ENERGíA: \t" + currentEnergy;

            System.out.println("\n" + line);
            printer.println("\n" + line);

            printer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
