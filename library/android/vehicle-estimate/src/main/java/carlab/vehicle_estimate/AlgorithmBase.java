package carlab.vehicle_estimate;

import android.content.Context;
import android.renderscript.Float2;
import android.renderscript.Float3;

import java.util.HashMap;
import java.util.Map;

import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.Registry;

public abstract class AlgorithmBase extends edu.umich.carlab.loadable.Algorithm {
    Map<Registry.Information, Object> latestValues = new HashMap<>();


    public static Function estimateSpeed = new Function(
            "estimateSpeed",
            Algorithm.class,
            Registry.CarSpeed,
            Registry.VehicleAlignedAccel, Registry.GPS
    );



    public static Function estimateGear = new Function(
            "estimateGear",
            Algorithm.class,
            Registry.CarGear,
            Registry.CarSpeed, Registry.GearModelFile
    );



    public static Function estimateSteering = new Function(
            "estimateSteering",
            Algorithm.class,
            Registry.CarSteering,
            Registry.CarSpeed, Registry.GravityAlignedGyro
    );


    public AlgorithmBase (CLDataProvider cl, Context context) {
        super(cl, context);
        name = "estimateSteering";
    }

    @Override
    public void newData (DataMarshal.DataObject dObject) {
        super.newData(dObject);

        Registry.Information information = dObject.information;
        if (dObject.dataType != DataMarshal.MessageType.DATA) return;
        if (dObject.value == null) return;

        latestValues.put(information, dObject.value);



        if (estimateSpeed.matchesRequired(information) &&
            estimateSpeed.haveReceivedAllRequiredData(latestValues.keySet())) {
            outputData(
                    Registry.CarSpeed,
                    estimateSpeed(
                            (Float3) latestValues.get(Registry.VehicleAlignedAccel),
                            (Float3) latestValues.get(Registry.GPS)));
        }



        if (estimateGear.matchesRequired(information) &&
            estimateGear.haveReceivedAllRequiredData(latestValues.keySet())) {
            outputData(
                    Registry.CarGear,
                    estimateGear(
                            (Float) latestValues.get(Registry.CarSpeed)));
        }



        if (estimateSteering.matchesRequired(information) &&
            estimateSteering.haveReceivedAllRequiredData(latestValues.keySet())) {
            outputData(
                    Registry.CarSteering,
                    estimateSteering(
                            (Float) latestValues.get(Registry.CarSpeed),
                            (Float) latestValues.get(Registry.GravityAlignedGyro)));
        }

    }

    public abstract Float estimateSpeed (Float3 VehicleAlignedAccel, Float3 GPS);

    public abstract Integer estimateGear (Float CarSpeed);

    public abstract Float estimateSteering (Float CarSpeed, Float GravityAlignedGyro);
}





