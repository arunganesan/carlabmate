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




    PackageCLService() {
        initializeRouting();
    }

    void initializeRouting () {
        // 1. Routing

         /*
        [
            // Says we want to use THIS algorithm to get THIS information
            { "algorithm": "aligned-imu", "information": "world-aligned-gyro", "save": true },
            { "algorithm": "aligned-imu", "information": "world-aligned-accel", "save": true },
            { "algorithm": "aligned-imu", "information": "rotation" },
        ]
         */

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


        // 2. Start/stop carlab (manual or automatic)
        // How does CarLab get the algorithmInputWiring?
        // On that note, how does it get the list of Algorithms? Do we initialize them here?
        // We could... wait ... hm. We could bind to it and just call the "start" function with the actual objects
        // Then it could save the objects. Done.
        // Besides I think it does run on the same thread...
        // Either way it should all share the same virtual space.

        // 3. Send and receive from linkserver
        // This is a separate service/thread. It wakes up occasionally to get new downloads.
        // It also uploads any data we have.


        // Previously we did the manual trigger
        // Then a trigger service wakes up occasionally to check and then launch if needed
        // That still makes hella sense. Lets keep that. Use a button.
        // However! That does mean it's not trivial how we get data to CLService.
        //     Especially since we're not starting that frmo the Main Activity where it is wired together
        // IDEA: Why even use a main activity? That should just be for display.
        //     What if we extended CLService and HARD CODED the fucking things. (statically coded)
        //     That way, the ONLY purpose of main activity is to tell you some feedback or hacve a manual button
        //     Everything else starts elsewhere.
        //     No need for weird passing shit arouund.
        // Love it.



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
