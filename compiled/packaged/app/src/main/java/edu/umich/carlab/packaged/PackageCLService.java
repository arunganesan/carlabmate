package edu.umich.carlab.packaged;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.umich.aligned_imu.AlignedIMU;
import edu.umich.carlab.loadable.Algorithm;
import edu.umich.carlab.loadable.AlgorithmSpecs;

public class PackageCLService extends CLService {
    Map<Algorithm, Set<String>> algorithmInputWiring = new HashMap<>();
    Set<String> saveInformation = new HashSet<>();
    Set<AlgorithmInformation> strategyRequirements = new HashSet<>();

    void initializeRouting () {
        Algorithm alignedIMU = new AlignedIMU(null, this);
        strategyRequirements.add(new AlgorithmInformation(alignedIMU, "world-aligned-gyro"));
        strategyRequirements.add(new AlgorithmInformation(alignedIMU, "world-aligned-accel"));
        strategyRequirements.add(new AlgorithmInformation(alignedIMU, "rotation"));

        saveInformation.add("world-aligned-accel");
        saveInformation.add("world-aligned-gyro");

        // For all requirements
        for (AlgorithmInformation algorithmInformation : strategyRequirements) {

            // Get the function which produces that
            for (AlgorithmSpecs.AppFunction function : alignedIMU.algorithmFunctions)

                // Make sure that this algorithm gets all the INPUT to that function
                // Just make sure it's wired. Nothing fancy.
                if (function.outputInformation.equals(algorithmInformation.information)) {
                    if (!algorithmInputWiring.containsKey(algorithmInformation.algorithm))
                        algorithmInputWiring
                                .put(algorithmInformation.algorithm, new HashSet<String>());
                    algorithmInputWiring.get(algorithmInformation.algorithm)
                                        .addAll(function.inputInformation);
                    break;
                }
        }










        // We no longer need this! This IS CarLab lol
        // for (Map.Entry<Algorithm, Set<String>> wiring : algorithmInputWiring.entrySet())
        //     for (String info : wiring.getValue())
        //         carlabService.addMultiplexRoute(info, wiring.getKey());
    }


    public class AlgorithmInformation {
        public Algorithm algorithm;
        public String information;

        public AlgorithmInformation (Algorithm a, String i) {
            algorithm = a;
            information = i;
        }
    }
}
