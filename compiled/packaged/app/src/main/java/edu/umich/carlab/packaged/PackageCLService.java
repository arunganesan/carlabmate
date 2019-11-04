package edu.umich.carlab.packaged;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.HashSet;

import edu.umich.aligned_imu.AlignedIMU;
import edu.umich.carlab.Constants;
import edu.umich.carlab.loadable.Algorithm;
import edu.umich.carlab.loadable.AlgorithmSpecs;

public class PackageCLService extends edu.umich.carlab.CLService {
    @Override
    protected void initializeRouting () {
         /*
        [
            // Says we want to use THIS algorithm to get THIS information
            { "algorithm": "aligned-imu", "information": "world-aligned-gyro", "save": true },
            { "algorithm": "aligned-imu", "information": "world-aligned-accel", "save": true },
            { "algorithm": "aligned-imu", "information": "rotation" },
        ]
         */

        Algorithm alignedIMU = new AlignedIMU(this, this);
        strategyRequirements.add(new AlgorithmInformation(alignedIMU, "world-aligned-gyro"));
        strategyRequirements.add(new AlgorithmInformation(alignedIMU, "world-aligned-accel"));
        strategyRequirements.add(new AlgorithmInformation(alignedIMU, "rotation"));

        saveInformation.add("world-aligned-accel");
        saveInformation.add("world-aligned-gyro");

        algorithmsToStart.add(AlignedIMU.class);

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



    public static void turnOffCarLab (Context context) {
        Intent intent = new Intent(context, PackageCLService.class);
        intent.setAction(Constants.MASTER_SWITCH_OFF);
        context.startService(intent);
    }

    public static void turnOnCarLab (Context context) {
        // This means we havent' connected in a while.
        // And this re-establishment isn't due to a temporary break
        // And we just connected to the actual OBD device

        Intent intent = new Intent(context, PackageCLService.class);
        intent.setAction(Constants.MASTER_SWITCH_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }




}
