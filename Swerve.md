# How to make swerve
## First: make a swerve object using this code with a constructor:
```public SwerveBase(SwerveDriveKinematics kinematics, Matrix<N3, N1> stateStdDevs, Matrix<N3, N1> visionMeasurementDevs, SwerveModuleConfig... configs)```
- SwerveDriveKinematics: Convert robot speed into individual module speed
- statesStdDevs: Room for error for your modules 
- visionMeasurementDevs: The room for error in your vision measurements
- SwerveModuleConfig: Defines swerve modules


## Some of the things you can do with this swerve code are:
- drive: Takes in a variety of parameters to get where you need to go
- assign: Takes in Chassis velocity and converts robot speed to individual module speed
- stop: Stops movement of the robot
- setBrakeMode: Sets each module into brake mode
- setDriveVoltage: Sets each module to specified voltage
- getPose: Returns the position of the robot
- getTranslation: Returns translation of the robot's position
- getRawEstimatedPose: returns estimated position of the robot
- addVisionMeasurement: Logs the position of the robot at a given time for vision
- resetEncoders: sets the value of the encoders to be default value
- resetOdometry: resets the value of the odometry to default position
- getStates: gets the status of each swerve module
- getPositions: returns the positions of each swerve module
- setModuleStates: sets how much each module is turned and their velocities
- periodic: updates log of the robots position and logs estimated position
- resetAll: Resets odometry, encoders, and gyro to zero
- xLock: sets the robot to turn wheels to all face inward to center of robot
- oLock: sets the direction of all wheels for broad sides to face inward to center
- zeroLock: sets all wheels straight
- characterize: uses System identification to use PID
- getGyroRotation2d: returns the rotation of the robot in degrees
- resetGyro: resets the value of the gyro to default reset value
- getFieldVelocity: converts the velocity of each of the modules to the chassis speed and returns the value
- getRobotVelocity: uses the speeds of each of the modules to return the chassis speed
- getSpeed: returns the speed of the robot
- getModule: Uses the module number to return the module
- getPredictedPose: returns the estimated position of the robot
- getDisplacementTo: returns the value of the robot's change in position to get to a certain point
- getDistanceTo: returns the distance needed to travel to a specific point 
- getAngularDisplacementTo: returns the change in the robot's angular positon to get to a certain point or angle
- getAngleTo: bounds the change in angle between -pi and pi
- nearestPose2d: returns the nearest postion of the robot within a set of points
- nearestTranslation2d: returns the nearest translation of the robot with a set of translations
- identifyOffsetsCommand: identify the angle that the robot has been offset

  

# NOTE: This is just a template and is meant to be adjusted to individual needs
