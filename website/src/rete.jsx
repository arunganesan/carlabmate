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
  }
}

export async function createEditor(container) {
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
