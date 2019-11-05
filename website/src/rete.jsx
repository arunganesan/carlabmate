import React from "react";
import Rete from "rete";
import ReactRenderPlugin from "rete-react-render-plugin";
import ConnectionPlugin from "rete-connection-plugin";
import AreaPlugin from "rete-area-plugin";
import { RawSensor } from "./RawSensor";

var numSocket = new Rete.Socket("Number value");


class ProduceAlignedGyro extends Rete.Component {
  constructor() {
    super("Produce Aligned Gyro");
  }

  builder(node) {
    return node
      .addInput(new Rete.Input("rotation", "rotation", numSocket))
      .addInput(new Rete.Input("gyro", "gyro", numSocket))
      .addOutput(new Rete.Output("aligned-gyro", "aligned-gyro", numSocket));
  }
}

class ProduceAlignedAccel extends Rete.Component {
  constructor() {
    super("Produce Aligned Accel");
  }

  builder(node) {
    return node
      .addInput(new Rete.Input("rotation", "rotation", numSocket))
      .addInput(new Rete.Input("accel", "accel", numSocket))
      .addOutput(new Rete.Output("aligned-accel", "aligned-accel", numSocket));
  }
}



class ProduceRotation extends Rete.Component {
  constructor() {
    super("Produce Rotation");
  }

  builder(node) {
    return node
      .addInput(new Rete.Input("magnet", "magnet", numSocket))
      .addInput(new Rete.Input("gravity", "gravity", numSocket))
      .addOutput(new Rete.Output("rotation", "rotation", numSocket));
  }
}





class ProduceWatchfoneSteering extends Rete.Component {
  constructor() {
    super("Produce Car Steering");
  }

  builder(node) {
    return node
      .addInput(new Rete.Input("aligned-gyro", "aligned-gyro", numSocket))
      .addInput(new Rete.Input("car-speed", "car-speed", numSocket))
      .addOutput(new Rete.Output("car-steering", "car-steering", numSocket));
  }
}




class ProduceWatchfoneSpeed extends Rete.Component {
  constructor() {
    super("Produce Car Speed");
  }

  builder(node) {
    return node
      .addInput(new Rete.Input("aligned-accel", "aligned-accel", numSocket))
      .addInput(new Rete.Input("gps", "gps", numSocket))
      .addOutput(new Rete.Output("car-speed", "car-speed", numSocket));
  }
}


class ProduceWatchfoneRPM extends Rete.Component {
  constructor() {
    super("Produce Car RPM");
  }

  builder(node) {
    return node
      .addInput(new Rete.Input("car-gear", "car-gear", numSocket))
      .addInput(new Rete.Input("car-speed", "car-speed", numSocket))
      .addOutput(new Rete.Output("car-rpm", "car-rpm", numSocket));
  }
}





class ProduceWatchfoneFuel extends Rete.Component {
  constructor() {
    super("Produce Car Fuel");
  }

  builder(node) {
    return node
      .addInput(new Rete.Input("car-odometer", "car-odometer", numSocket))
      .addOutput(new Rete.Output("car-fuel", "car-fuel", numSocket));
  }
}



class ProduceWatchfoneGear extends Rete.Component {
  constructor() {
    super("Produce Car Gear");
  }

  builder(node) {
    return node
      .addInput(new Rete.Input("car-speed", "car-speed", numSocket))
      .addOutput(new Rete.Output("car-gear", "car-gear", numSocket));
  }
}



class ProduceWatchfoneOdometer extends Rete.Component {
  constructor() {
    super("Produce Car Odometer");
  }

  builder(node) {
    return node
      .addInput(new Rete.Input("gps", "gps", numSocket))
      .addOutput(new Rete.Output("car-odometer", "car-odometer", numSocket));
  }
}







class RawSensors extends Rete.Component {
  constructor() {
    super("Raw Sensors");
    // this.data.component = RawSensor;
  }

  builder(node) {
    return node
      .addOutput(new Rete.Output("gyro", "gyro", numSocket))
      .addOutput(new Rete.Output("accel", "accel", numSocket))
      .addOutput(new Rete.Output("gravity", "gravity", numSocket))
      .addOutput(new Rete.Output("magnet", "magnet", numSocket))
      .addOutput(new Rete.Output("gps", "gps", numSocket))
  }
}

export async function createAlignedIMU(container) {
  var components = [
    new ProduceAlignedAccel(),
    new ProduceAlignedGyro(),
    new ProduceRotation(),
    new RawSensors()
  ];

  var editor = new Rete.NodeEditor("demo@0.1.0", container);
  editor.use(ConnectionPlugin);
  editor.use(ReactRenderPlugin);

  var engine = new Rete.Engine("demo@0.1.0");

  components.map(c => {
    editor.register(c);
    engine.register(c);
  });

  let prodAccel = await components[0].createNode();
  let prodGyro = await components[1].createNode();
  let prodRotation = await components[2].createNode();
  let rawSensors = await components[3].createNode();

  prodAccel.position = [700, 0];
  prodGyro.position = [700, 300];
  prodRotation.position = [350, 150];
  rawSensors.position = [0, 150];

  editor.addNode(prodAccel);
  editor.addNode(prodGyro);
  editor.addNode(prodRotation);
  editor.addNode(rawSensors);

  editor.connect(rawSensors.outputs.get("gravity"), prodRotation.inputs.get("gravity"));
  editor.connect(rawSensors.outputs.get("magnet"), prodRotation.inputs.get("magnet"));
  editor.connect(rawSensors.outputs.get("accel"), prodAccel.inputs.get("accel"));
  editor.connect(rawSensors.outputs.get("gyro"), prodGyro.inputs.get("gyro"));
  editor.connect(prodRotation.outputs.get("rotation"), prodGyro.inputs.get("rotation"));
  editor.connect(prodRotation.outputs.get("rotation"), prodAccel.inputs.get("rotation"));

  editor.on(
    "process nodecreated noderemoved connectioncreated connectionremoved",
    async () => {
      console.log("process");
      await engine.abort();
      await engine.process(editor.toJSON());
    }
  );

  editor.view.resize();
  editor.trigger("process");
  AreaPlugin.zoomAt(editor, editor.nodes);
}










export async function createWatchfone(container) {
  var components = [
    new ProduceAlignedAccel(),
    new ProduceAlignedGyro(),
    new ProduceRotation(),
    new RawSensors(),


    new ProduceWatchfoneFuel(),
    new ProduceWatchfoneSpeed(),
    new ProduceWatchfoneSteering(),
    new ProduceWatchfoneGear(),
    new ProduceWatchfoneOdometer(),
    new ProduceWatchfoneRPM(),
  ];

  var editor = new Rete.NodeEditor("demo@0.1.0", container);
  editor.use(ConnectionPlugin);
  editor.use(ReactRenderPlugin);

  var engine = new Rete.Engine("demo@0.1.0");

  components.map(c => {
    editor.register(c);
    engine.register(c);
  });

  let prodAccel = await components[0].createNode();
  let prodGyro = await components[1].createNode();
  let prodRotation = await components[2].createNode();
  let rawSensors = await components[3].createNode();
  
  let wfFuel = await components[4].createNode();
  let wfSpeed = await components[5].createNode();
  let wfSteering = await components[6].createNode();
  let wfGear = await components[7].createNode();
  let wfOdometer = await components[8].createNode();
  let wfRPM = await components[9].createNode();

  prodAccel.position = [700, 0];
  prodGyro.position = [700, 450];
  prodRotation.position = [350, 150];
  rawSensors.position = [0, 150];

  wfOdometer.position = [700, 700];
  wfFuel.position = [1050, 700];
  wfSpeed.position = [1050, 150];
  wfGear.position = [1400, 0];
  wfRPM.position = [1750, 0];
  wfSteering.position = [1750, 300];
  

  editor.addNode(prodAccel);
  editor.addNode(prodGyro);
  editor.addNode(prodRotation);
  editor.addNode(rawSensors);
  
  editor.addNode(wfFuel);
  editor.addNode(wfSpeed);
  editor.addNode(wfSteering);
  editor.addNode(wfGear);
  editor.addNode(wfOdometer);
  editor.addNode(wfRPM);

  editor.connect(rawSensors.outputs.get("gravity"), prodRotation.inputs.get("gravity"));
  editor.connect(rawSensors.outputs.get("magnet"), prodRotation.inputs.get("magnet"));
  editor.connect(rawSensors.outputs.get("accel"), prodAccel.inputs.get("accel"));
  editor.connect(rawSensors.outputs.get("gyro"), prodGyro.inputs.get("gyro"));
  editor.connect(prodRotation.outputs.get("rotation"), prodGyro.inputs.get("rotation"));
  editor.connect(prodRotation.outputs.get("rotation"), prodAccel.inputs.get("rotation"));


  editor.connect(wfOdometer.outputs.get("car-odometer"), wfFuel.inputs.get("car-odometer"));
  editor.connect(rawSensors.outputs.get("gps"), wfOdometer.inputs.get("gps"));
  editor.connect(rawSensors.outputs.get("gps"), wfSpeed.inputs.get("gps"));
  editor.connect(prodAccel.outputs.get("aligned-accel"), wfSpeed.inputs.get("aligned-accel"));
  editor.connect(prodGyro.outputs.get("aligned-gyro"), wfSteering.inputs.get("aligned-gyro"));
  editor.connect(wfSpeed.outputs.get("car-speed"), wfSteering.inputs.get("car-speed"));
  editor.connect(wfSpeed.outputs.get("car-speed"), wfGear.inputs.get("car-speed"));

  editor.connect(wfSpeed.outputs.get("car-speed"), wfRPM.inputs.get("car-speed"));
  editor.connect(wfGear.outputs.get("car-gear"), wfRPM.inputs.get("car-gear"));
  

  editor.on(
    "process nodecreated noderemoved connectioncreated connectionremoved",
    async () => {
      console.log("process");
      await engine.abort();
      await engine.process(editor.toJSON());
    }
  );

  editor.view.resize();
  editor.trigger("process");
  AreaPlugin.zoomAt(editor, editor.nodes);
}
