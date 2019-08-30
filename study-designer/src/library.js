import _ from 'lodash'

function sampleRandomRequirements(atmax_requirements, nodes) {
    let num_requirements = _.random(1, atmax_requirements)
    let random_requirements = _.sampleSize(nodes, num_requirements);
    return random_requirements.map(impl => impl.supplies)
  }


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
        requires: []
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


