package ZikaModel;

import org.nlogo.api.LogoException;
import org.nlogo.core.CompilerException;
import org.nlogo.headless.HeadlessWorkspace;

public class HeadlessSimulation implements SimulationModel {

    private Integer totalRepetions;
    private Integer totalReports;

    public HeadlessSimulation(Integer totalRepetions) {
        this.totalRepetions = totalRepetions;
        this.totalReports = 6;
    }

    public HeadlessSimulation(Integer totalRepetions, Integer totalReports) {
        this.totalRepetions = totalRepetions;
        this.totalReports = totalReports;
    }

    @Override
    public SimulationState run(SimulationState state) {

        // Si ya hemos recolectado los datos solo retornamos el estado
        if (((ZikaInfectionState) state).getDeadCells().size() != 0 || ((ZikaInfectionState) state).getCondensedCells().size() != 0 || ((ZikaInfectionState) state).getAliveCells().size() != 0)
            return state;

        // NetLogo initialization
        HeadlessWorkspace headlessWorkspace = HeadlessWorkspace.newInstance();
        try {

            headlessWorkspace.open(
                    "resources/models/"
                            + "zika.nlogo");

            /**
             * Parameters Initialization
             */
            headlessWorkspace.command("setup-experiment");
            headlessWorkspace.command("set cell-density " + ((ZikaInfectionState) state).getCellDensity()[0]);                                                          // Min: 0 Max: 20 Inc: 0.01
            headlessWorkspace.command("set initial-infected-cell-percentage " + ((ZikaInfectionState) state).getInitialInfectedCellPercentage()[0]);                    // Min: 0 Max: 100.0 Inc: 0.001
            headlessWorkspace.command("set viral-reach " + ((ZikaInfectionState) state).getViralReach()[0]);                                                            // Min: 0 Max: 3 Inc: 0.1
            headlessWorkspace.command("set infection-rate " + ((ZikaInfectionState) state).getInfectionRate()[0]);                                                      // Min: 0 Max: 15 Inc: 0.1
            headlessWorkspace.command("set mNeptune-effectiveness " + ((ZikaInfectionState) state).getmNeptuneEffectiveness()[0]);                                      // Min: 0 Max: 400 Inc: 0.01
            headlessWorkspace.command("set initial-probability-of-death " + ((ZikaInfectionState) state).getInitialProbabilityOfDeath()[0]);                            // Min: 0 Max: 2 Inc: 0.001
            headlessWorkspace.command("set initial-probability-of-chromatin-condensation " + ((ZikaInfectionState) state).getInitialProbabilityOfChromatinCondensation()[0]);       // Min: 0 Max: 2.5 Inc: 0.05
            headlessWorkspace.command("set marker-detection-threashold " + ((ZikaInfectionState) state).getMarkerDetectionThreashold()[0]);                             // Min: 0 Max: 1 Inc: 0.01

            //Agregamos un reporte inicial en 0
            if (totalReports > 0) {
                ((ZikaInfectionState) state).getDeadCells().add(0.0);
                ((ZikaInfectionState) state).getCondensedCells().add(0.0);
                ((ZikaInfectionState) state).getAliveCells().add(0.0);
            }
            //Agregamos un reporte de las 24h en 0
            //((ZikaInfectionState) state).getDeadCells().add(0.0);
            //((ZikaInfectionState) state).getCondensedCells().add(0.0);
            //((ZikaInfectionState) state).getAliveCells().add(0.0);

            /*System.out.println("SIMULACIÓN");*/

            //Se reporta seis veces (incluyendo el inicial) por experimento

            //****************************************************Obtención de # TOTAL DE CELULAS
            Double totalCells = (Double) headlessWorkspace.report("count turtles");                                         // Recolectamos el dato
                /*System.out.println( "Total de celulas:" +
                        totalCells);                                                                                                 // Monitoreamos el dato*/
            ((ZikaInfectionState) state).setTotalCells(totalCells);                                                        // Lo agregamos al estado

            for (int i = 0; i < (totalReports - 1); i++){
                /**
                 * Algorithm execution
                 */
                /* Se realizan bloques de (totalRepetitions / 5) porque ya se contó el inicial,
                por ejemplo, si son 120 totales, los reportes se harían cada 24 repeticiones.
                * */
                headlessWorkspace.command("repeat "+ (totalRepetions / (totalReports - 1)) +" [ go ]"); //

                /**
                 * Data reporting
                 */
                //**************************************************Obtención de # de CÉLULAS MUERTAS
                Double deadCells = (Double) headlessWorkspace.report("count turtles with [state = \"dead\"]");                 // Recolectamos el dato
                /* System.out.println( "Células muertas:" +
                        deadCells);                                                                                                   // Monitoreamos el dato */
                ((ZikaInfectionState) state).getDeadCells().add(deadCells / totalCells);                                                           // Lo agregamos al estado

                //*********************************Obtención de # de CÉLULAS CON CROMATINA CONDENSADA
                Double condensedCells = (Double) headlessWorkspace.report("count turtles with [chromatin-condensed]");         // Recolectamos el dato
                /*System.out.println( "Células con cromatina condensada:" +
                        condensedCells);                                                                                              // Monitoreamos el dato */
                ((ZikaInfectionState) state).getCondensedCells().add(condensedCells / totalCells);                                                 // Lo agregamos al estado

                //****************************************************Obtención de # de CÉLULAS VIVAS
                Double aliveCells = (Double) headlessWorkspace.report("alive-cells");                                         // Recolectamos el dato
                /* System.out.println( "Células vivas:" +
                        aliveCells);                                                                                                 // Monitoreamos el dato */
                ((ZikaInfectionState) state).getAliveCells().add(aliveCells / totalCells);                                                        // Lo agregamos al estado

            }

            headlessWorkspace.dispose();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        return state;
    }
}
