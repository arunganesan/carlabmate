
export class Information {
    name: string;
    dtype: any;
  
    constructor(name: string, dtype: any) {
      this.name = name;
      this.dtype = dtype;
    }
  }
  
  export class Registry {
      static MapMatchedLocation = new Information('map-matched-location', 0.0)
      static Location = new Information('location', 0.0)
      static CarSpeed = new Information('car-speed', 0.0)
      static CarGear = new Information('car-gear', 0.0)
      static CarFuel = new Information('car-fuel', 0.0)
      static CarSteering = new Information('car-steering', 0.0)
      static VehiclePointingRotation = new Information('vehicle-pointing-rotation', 0.0)
      static WorldPointingRotation = new Information('world-pointing-rotation', 0.0)
      static VehicleAlignedAccel = new Information('vehicle-aligned-accel', 0.0)
      static WorldAlignedAccel = new Information('world-aligned-accel', 0.0)
      static WorldAlignedGyro = new Information('world-aligned-gyro', 0.0)
      static GravityAlignedGyro = new Information('gravity-aligned-gyro', 0.0)
      static GearModelFile = new Information('gear-model-file', 0.0)
      static CarModel = new Information('car-model', 0.0)
      static PhoneNumber = new Information('phone-number', 0.0)
      static UserText = new Information('user-text', 0.0)
      static Sighting = new Information('sighting', 0.0)
      static SightingsMap = new Information('sightings-map', 0.0)
      static GPS = new Information('gps', 0.0)
      static Accel = new Information('accel', 0.0)
      static Magnetometer = new Information('magnetometer', 0.0)
      static Gyro = new Information('gyro', 0.0)
      static Gravity = new Information('gravity', 0.0)
      static ObdFuel = new Information('obd-fuel', 0.0)
  }
  
  
  