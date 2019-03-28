package ZikaModel;

import org.nlogo.app.App;

public class GUISimulation implements SimulationModel {

    private Integer totalRepetions;

    public GUISimulation(Integer totalRepetions) {
        this.totalRepetions = totalRepetions;
    }

    @Override
    public SimulationState run(SimulationState state) {
        // NetLogo initialization
        App.main(new String[]{});
        try {
            /**
             * Note the use of EventQueue.invokeAndWait to ensure that a method is called from the right thread.
             * This is because most of the methods on the App class may only be called some certain threads. Most
             * of the methods may only be called from the AWT event queue thread; but a few methods, such as main()
             * and commmand(), may only be called from threads other than the AWT event queue thread (such as, in
             * this example, the main thread).
             *
             * Reference: https://github.com/NetLogo/NetLogo/wiki/Controlling-API
             */
            java.awt.EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    try {
                        App.app().open(
                                "resources/models/"
                                        + "zika.nlogo");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            /**
             * Default instructions
             */
                                /* App.app().command("set density 62");
                                App.app().command("random-seed 0");
                                App.app().command("setup");
                                App.app().command("repeat 50 [ go ]");
                                System.out.println(
                                App.app().report("burned-trees")); */

            /**
             * Parameters Initialization
             */
            App.app().command("setup-experiment");
            App.app().command("set cell-density " + ((ZikaInfectionState) state).getCellDensity()[0]);                                                          // Min: 0 Max: 20 Inc: 0.01
            App.app().command("set initial-infected-cell-percentage " + ((ZikaInfectionState) state).getInitialInfectedCellPercentage()[0]);                    // Min: 0 Max: 100.0 Inc: 0.001
            App.app().command("set viral-reach " + ((ZikaInfectionState) state).getViralReach()[0]);                                                            // Min: 0 Max: 3 Inc: 0.1
            App.app().command("set infection-rate " + ((ZikaInfectionState) state).getInfectionRate()[0]);                                                      // Min: 0 Max: 15 Inc: 0.1
            App.app().command("set mNeptune-effectiveness " + ((ZikaInfectionState) state).getmNeptuneEffectiveness()[0]);                                      // Min: 0 Max: 400 Inc: 0.01
            App.app().command("set initial-probability-of-death " + ((ZikaInfectionState) state).getInitialProbabilityOfDeath()[0]);                            // Min: 0 Max: 2 Inc: 0.001
            App.app().command("set initial-probability-of-chromatin-condensation " + ((ZikaInfectionState) state).getInitialProbabilityOfChromatinCondensation()[0]);       // Min: 0 Max: 2.5 Inc: 0.05
            App.app().command("set marker-detection-threashold " + ((ZikaInfectionState) state).getMarkerDetectionThreashold()[0]);                             // Min: 0 Max: 1 Inc: 0.01

            //Agregamos un reporte inicial en 0
            ((ZikaInfectionState) state).getDeadCells().add(0.0);
            ((ZikaInfectionState) state).getCondensedCells().add(0.0);
            ((ZikaInfectionState) state).getAliveCells().add(0.0);

            System.out.println("SIMULACIÓN");

            // Se reporta seis veces (incluyendo el inicial) por experimento
            for (int i = 0; i < 5; i++) {
                /**
                 * Algorithm execution
                 */
                /* Se realizan bloques de (totalRepetitions / 5) porque ya se contó el inicial,
                por ejemplo, si son 120 totales, los reportes se harían cada 24 repeticiones.
                * */
                App.app().command("repeat " + (totalRepetions / 5) + " [ go ]"); //

                /**
                 * Data reporting
                 */
                //************************************************** Obtención de # de CÉLULAS MUERTAS
                Double deadCells = (Double) App.app().report("count turtles with [state = \"dead\"]");                 // Recolectamos el dato
                System.out.println("Células muertas:" +
                        deadCells);                                                                                                   // Monitoreamos el dato
                ((ZikaInfectionState) state).getDeadCells().add(deadCells);                                                           // Lo agregamos al estado

                //********************************* Obtención de # de CÉLULAS CON CROMATINA CONDENSADA
                Double condensedCells = (Double) App.app().report("count turtles with [chromatin-condensed]");         // Recolectamos el dato
                System.out.println("Células con cromatina condensada:" +
                        condensedCells);                                                                                              // Monitoreamos el dato
                ((ZikaInfectionState) state).getCondensedCells().add(condensedCells);                                                 // Lo agregamos al estado

                //**************************************************** Obtención de # de CÉLULAS VIVAS
                Double aliveCells = (Double) App.app().report("alive-cells");                                         // Recolectamos el dato
                System.out.println("Células vivas:" +
                        aliveCells);                                                                                                 // Monitoreamos el dato
                ((ZikaInfectionState) state).getAliveCells().add(aliveCells);                                                        // Lo agregamos al estado
            }

            App.app().quit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return state;
    }
}
