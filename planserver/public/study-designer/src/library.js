import _ from 'lodash'

function sampleRandomRequirements(atmax_requirements, nodes) {
  let num_requirements = _.random(1, atmax_requirements)
  let random_requirements = _.sampleSize(nodes, num_requirements);
  return random_requirements.map(impl => impl.supplies)
}

export const Library = {"information": {"car/speed": {"name": "car/speed", "implemented_by": ["watchfone/speed", "openxc/speed", "obd/speed"]}, "car/odometer": {"name": "car/odometer", "implemented_by": ["watchfone/odometer", "openxc/odometer", "obd/odometer"]}, "car/fuel": {"name": "car/fuel", "implemented_by": ["watchfone/fuel", "openxc/fuel", "obd/fuel"]}, "car/rpm": {"name": "car/rpm", "implemented_by": ["watchfone/rpm", "openxc/rpm", "obd/rpm"]}, "car/steering": {"name": "car/steering", "implemented_by": ["watchfone/steering", "openxc/steering"]}, "car/gear": {"name": "car/gear", "implemented_by": ["watchfone/gear", "openxc/gear"]}, "location": {"name": "location", "implemented_by": ["phone/gps", "react-native/gps", "react-native/dummy"]}, "magnet": {"name": "magnet", "implemented_by": ["core/magnet"]}, "imu": {"name": "imu", "implemented_by": ["core/imu"]}, "aligned imu": {"name": "aligned imu", "implemented_by": ["android/aligned imu", "vsense/aligned imu", "watchfone/imu"]}}, "implementations": {"watchfone/speed": {"name": "watchfone/speed", "supplies": "car/speed", "requires": ["imu", "location"], "devices": ["android"]}, "watchfone/odometer": {"name": "watchfone/odometer", "supplies": "car/odometer", "requires": ["location"], "devices": ["android"]}, "watchfone/fuel": {"name": "watchfone/fuel", "supplies": "car/fuel", "requires": ["car/odometer"], "devices": ["android"]}, "watchfone/gear": {"name": "watchfone/gear", "supplies": "car/gear", "requires": ["car/speed"], "devices": ["android"]}, "watchfone/steering": {"name": "watchfone/steering", "supplies": "car/steering", "requires": ["car/speed", "aligned imu"], "devices": ["android"]}, "watchfone/rpm": {"name": "watchfone/rpm", "supplies": "car/rpm", "requires": ["car/gear", "car/speed"], "devices": ["android"]}, "android/aligned imu": {"name": "android/aligned imu", "supplies": "aligned imu", "requires": ["magnet", "imu"], "devices": ["android"]}, "vsense/aligned imu": {"name": "vsense/aligned imu", "supplies": "aligned imu", "requires": ["magnet", "imu"], "devices": ["android"]}, "watchfone/imu": {"name": "watchfone/imu", "supplies": "aligned imu", "requires": ["magnet", "imu"], "devices": ["android"]}, "phone/gps": {"name": "phone/gps", "supplies": "location", "requires": [], "devices": ["android", "iphone"]}, "react-native/gps": {"name": "react-native/gps", "supplies": "location", "requires": [], "devices": ["android", "iphone"]}, "react-native/dummy": {"name": "react-native/dummy", "supplies": "location", "requires": [], "devices": ["android", "iphone"]}, "core/magnet": {"name": "core/magnet", "supplies": "magnet", "requires": [], "devices": ["android", "iphone"]}, "core/imu": {"name": "core/imu", "supplies": "imu", "requires": [], "devices": ["android", "iphone"]}, "openxc/speed": {"name": "openxc/speed", "supplies": "car/speed", "requires": [], "devices": ["openxc"]}, "openxc/steering": {"name": "openxc/steering", "supplies": "car/steering", "requires": [], "devices": ["openxc"]}, "openxc/odometer": {"name": "openxc/odometer", "supplies": "car/odometer", "requires": [], "devices": ["openxc"]}, "openxc/fuel": {"name": "openxc/fuel", "supplies": "car/fuel", "requires": [], "devices": ["openxc"]}, "openxc/rpm": {"name": "openxc/rpm", "supplies": "car/rpm", "requires": [], "devices": ["openxc"]}, "openxc/gear": {"name": "openxc/gear", "supplies": "car/gear", "requires": [], "devices": ["openxc"]}, "obd/speed": {"name": "obd/speed", "supplies": "car/speed", "requires": [], "devices": ["obd"]}, "obd/odometer": {"name": "obd/odometer", "supplies": "car/odometer", "requires": [], "devices": ["obd"]}, "obd/fuel": {"name": "obd/fuel", "supplies": "car/fuel", "requires": [], "devices": ["obd"]}, "obd/rpm": {"name": "obd/rpm", "supplies": "car/rpm", "requires": [], "devices": ["obd"]}}}


export function generateDummyImplementations () {
    // each implementation discovers a set of information and requires a set of information
    // if an implementation doesn't have any requirements or sensors, that is a "leaf node" - often a raw sensor
    // only special implementations may use or require low-level data such as sensors

    const n_information = 15;
    const n_implementation = 50;
    const atmax_requirements = 10;

    // start with bottom most layer
    // each node can depend on other nodes in previous layers
    let implementations = {}
    let information = {}

    for (let i = 0; i < n_information; i++)  {
      let info = `info${i}` 
      let impl = `default_impl${i}`;
      information[info] = {
        name: info,
        implemented_by: [impl]
      }

      implementations[impl] = {
        name: impl,
        supplies: info,
        requires: [],
        devices: []
      }
    }
    
    let information_names = _.keys(information)
    
    for (let i = 0; i < n_implementation; i++) {
      let name = `impl${i}`
      let supplies = _.sample(information_names)
      implementations[name] = {
        name: name,
        supplies: supplies,
        requires: _.sampleSize(information_names, 
                    _.random(0, atmax_requirements))
      }
      information[supplies].implemented_by.push(name);
    }

    return {
      information: information,
      implementations: implementations,
    }
}
  
export function createStudyPlan(library) {  
    let information_names = _.keys(library.information);
    let required_information = _.sampleSize(information_names, 3);
    let secondary_information = []
    let leaf_nodes = []

    // for each information, pick an implementation, get their list of informations
    let plan = []

    let additional_dependencies = _.clone(required_information);

    console.log(library.implementations)

    let loop_count = 1000;  
    while (additional_dependencies.length != 0) {
      let new_additional_dependencies = [];
      for (let info of additional_dependencies) {
        let impl_name = _.sample(library.information[info].implemented_by)
        
        let dependencies = library.implementations[impl_name].requires;
        plan.push({
          info: info,
          impl: impl_name
        })
        
        let new_deps = _.filter(dependencies, dep => !_.includes(required_information, dep) && !_.includes(secondary_information, dep))
        new_additional_dependencies = _.concat(new_additional_dependencies, new_deps);
        secondary_information = _.concat(secondary_information, new_deps);

        console.log(loop_count, new_additional_dependencies)
      }
      
      new_additional_dependencies = _.uniq(new_additional_dependencies);
      additional_dependencies = _.clone(new_additional_dependencies);
      if (loop_count-- < 0) {
        alert('Looped too many times');
        break;
      }
    }

    console.log(plan, leaf_nodes)

    return plan;
}


