package common.core.swerve;

import java.util.List;

import common.hardware.motorcontroller.NAR_Motor;
import common.hardware.motorcontroller.NAR_Motor.Control;
import common.utility.sysid.CmdSysId;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public abstract class SwerveBase extends SubsystemBase {

    protected boolean chassisVelocityCorrection = true;
    public boolean fieldRelative = true;
    protected double dtConstant = 0.009;
    public double throttle = 1;

    protected final SwerveDriveKinematics kinematics;
    protected SwerveDrivePoseEstimator odometry;
    protected final SwerveModule[] modules;
    private Pose2d estimatedPose;

    public double maxSpeed;

    public SwerveBase(SwerveDriveKinematics kinematics, Matrix<N3, N1> stateStdDevs, Matrix<N3, N1> visionMeasurementDevs, SwerveModuleConfig... configs) {
        this.kinematics = kinematics;
        this.maxSpeed = configs[0].maxSpeed;
        estimatedPose = new Pose2d();

        modules = new SwerveModule[] {
            new SwerveModule(configs[0]),
            new SwerveModule(configs[1]),
            new SwerveModule(configs[2]),
            new SwerveModule(configs[3])
        };
        Timer.delay(1.5);

        resetEncoders();

        odometry = new SwerveDrivePoseEstimator(kinematics, new Rotation2d(), getPositions(),
                                                estimatedPose, stateStdDevs, visionMeasurementDevs);
    }

    public void drive(Translation2d translationVel, Rotation2d rotationVel) {
        drive(new ChassisSpeeds(translationVel.getX(), translationVel.getY(), rotationVel.getRadians()));
    }

    public void drive(Translation2d translationVel, double rotationVel) {
        drive(new ChassisSpeeds(translationVel.getX(), translationVel.getY(), rotationVel));
    }

    // forces into robot relative speeds
    public void drive(double xVel, double yVel, double omega) {
        drive(ChassisSpeeds.fromRobotRelativeSpeeds(xVel, yVel, omega, getGyroRotation2d()));
    }

    /**
     * Provides user editable insulation between requests and assignments to the swerve modules
     * @param velocity requested 
     */
    public void drive(ChassisSpeeds velocity) {
        assign(velocity);
    }

    /**
     * Assigns the requested velocity to the swerve modules
     * @param velocity requested velocity
     */
    public void assign(ChassisSpeeds velocity) {
        if(fieldRelative) velocity = ChassisSpeeds.fromFieldRelativeSpeeds(velocity, getGyroRotation2d()); // convert to field relative if applicable
        if(chassisVelocityCorrection) velocity = ChassisSpeeds.discretize(velocity, dtConstant);
        setModuleStates(kinematics.toSwerveModuleStates(velocity.times(throttle)));
    }

    public void stop() {
        for (SwerveModule module : modules) {
            module.stop();
        }
    }

    public void setBrakeMode(boolean isBrake) {
        for (final SwerveModule module : modules) {
            module.setBrakeMode(isBrake);
        }
    }

    public void setDriveVoltage(double volts) {
        for (final SwerveModule module : modules) {
            module.getDriveMotor().setVolts(volts);
        }
    }

    public Pose2d getPose() {
        return new Pose2d(estimatedPose.getTranslation(), getGyroRotation2d());
    }

    public Translation2d getTranslation() {
        return estimatedPose.getTranslation();
    }

    public Pose2d getRawEstimatedPose() {
        return estimatedPose;
    }

    public void addVisionMeasurement(Pose2d pose, double timeStamp) {
        odometry.addVisionMeasurement(pose, timeStamp);
    }

    public void resetEncoders() {
        for (SwerveModule module : modules) {
            module.resetToAbsolute();
        }
    }

    public void resetOdometry(Pose2d pose) {
        resetGyro(pose.getRotation().getDegrees());
        odometry.resetPosition(getGyroRotation2d(), getPositions(), pose);
    }

    public SwerveModuleState[] getStates() {
        SwerveModuleState[] states = new SwerveModuleState[4];
        for (SwerveModule module : modules) {
            states[module.moduleNumber] = module.getState();
        }
        return states;
    }

    public SwerveModulePosition[] getPositions() {
        SwerveModulePosition[] positions = new SwerveModulePosition[4];
        for (SwerveModule module : modules) {
            positions[module.moduleNumber] = module.getModulePosition();
        }
        return positions;
    }
    
    public void toggleFieldRelative() {
        fieldRelative = !fieldRelative;
    }

    public void setModuleStates(SwerveModuleState[] desiredStates) {
        SwerveDriveKinematics.desaturateWheelSpeeds(desiredStates, maxSpeed);
        
        for (SwerveModule module : modules){
            module.setDesiredState(desiredStates[module.moduleNumber]);
        }
    }

    @Override
    public void periodic() {
        odometry.update(getGyroRotation2d(), getPositions());
        estimatedPose = odometry.getEstimatedPosition();
    }

    public void resetAll() {
        resetOdometry(new Pose2d(0,0, new Rotation2d(0)));
        resetEncoders();
        resetGyro(0);
    }
    
    public void xLock() {
        modules[0].getAngleMotor().set(45, Control.Position);
        modules[1].getAngleMotor().set(-45, Control.Position);
        modules[2].getAngleMotor().set(135, Control.Position);
        modules[3].getAngleMotor().set(-135, Control.Position);
    }

    public void oLock() {
        modules[0].getAngleMotor().set(135, Control.Position);
        modules[1].getAngleMotor().set(45, Control.Position);
        modules[2].getAngleMotor().set(-135, Control.Position);
        modules[3].getAngleMotor().set(-45, Control.Position);
    }

    public void zeroLock() {
        modules[0].getAngleMotor().set(0, Control.Position);
        modules[1].getAngleMotor().set(0, Control.Position);
        modules[2].getAngleMotor().set(0, Control.Position);
        modules[3].getAngleMotor().set(0, Control.Position);
    }

    public Command characterize(double startDelay, double rampRate) {
        NAR_Motor driveMotor = modules[0].getDriveMotor();
        return new CmdSysId(
            getName(), 
            (volts)-> setDriveVoltage(volts), 
            ()-> driveMotor.getVelocity(), 
            ()-> driveMotor.getPosition(), 
            startDelay, 
            rampRate, 
            10, 
            true, 
            this
        );
    }

    public abstract double getYaw();

    public abstract double getPitch();

    public abstract double getRoll();

    public Rotation2d getGyroRotation2d() {
        return Rotation2d.fromDegrees(getYaw());
    }

    public abstract void resetGyro(double reset);

    /**
     * Gets the current field-relative velocity (x, y and omega) of the robot
     *
     * @return A ChassisSpeeds object of the current field-relative velocity
     */
    public ChassisSpeeds getFieldVelocity() {
        return ChassisSpeeds.fromRobotRelativeSpeeds(
            kinematics.toChassisSpeeds(getStates()), getGyroRotation2d());
    }

    /**
     * Gets the current robot-relative velocity (x, y and omega) of the robot
     *
     * @return A ChassisSpeeds object of the current robot-relative velocity
     */
    public ChassisSpeeds getRobotVelocity() {
        return kinematics.toChassisSpeeds(getStates());
    }

    public double getSpeed() {
        final ChassisSpeeds velocity = getRobotVelocity();
        return Math.hypot(velocity.vxMetersPerSecond, velocity.vyMetersPerSecond);
    }

    public SwerveModule[] getModules() {
        return modules;
    }

    public SwerveModule getModule(int moduleNumber) {
        if(moduleNumber < 0 || moduleNumber > 3) throw new IllegalArgumentException("Module number must be between 0 and 3");
        return modules[moduleNumber];
    }

    public Pose2d getPredictedPose(ChassisSpeeds velocity, double dt) {
        final Translation2d x = getPose().getTranslation();
        final Rotation2d theta = getPose().getRotation();
        final Translation2d dx = new Translation2d(velocity.vxMetersPerSecond * dt, velocity.vyMetersPerSecond * dt);
        final Rotation2d dtheta = Rotation2d.fromRadians(velocity.omegaRadiansPerSecond * dt);
        return new Pose2d(x.plus(dx), theta.plus(dtheta));
    }

    public Translation2d getDisplacementTo(Translation2d point) {
        return getPose().getTranslation().minus(point);
    }

    public double getDistanceTo(Translation2d point) {
        return Math.abs(getPose().getTranslation().getDistance(point));
    }

    public Rotation2d getAngularDisplacementTo(Translation2d point) {
        return getPose().getRotation().minus(getDisplacementTo(point).getAngle());
    }

    public Rotation2d getAngularDisplacementTo(Rotation2d angle) {
        return getGyroRotation2d().minus(angle);
    }

    public double getAngleTo(Rotation2d angle) {
        return MathUtil.angleModulus(getAngularDisplacementTo(angle).getRadians());
    }

    public Pose2d nearestPose2d(List<Pose2d> poses) {
        return getPose().nearest(poses);
    }

    public Translation2d nearestTranslation2d(List<Translation2d> translations) {
        return getPose().getTranslation().nearest(translations);
    }

    public Command identifyOffsetsCommand() {
        return runOnce(()-> {
            for(SwerveModule module : getModules()) {
                double rawAngle = module.getRawAbsoluteAngle().getDegrees();
                System.out.println("public static final double MOD" + module.moduleNumber + "_CANCODER_OFFSET = " + rawAngle + ";");
            }
        });
    }

    public Command characterize(double startDelay, double rampRate, double targetPosition) {
        NAR_Motor driveMotor = modules[0].getDriveMotor();
        final double startPos = driveMotor.getPosition();
        return new CmdSysId(
            getName(), 
            (volts)-> setDriveVoltage(volts), 
            ()-> driveMotor.getVelocity(), 
            ()-> driveMotor.getPosition(), 
            startDelay, 
            rampRate, 
            startPos + targetPosition, 
            true, 
            this
        );
    }

    public Command characterizeTranslation(double startDelay, double rampRate, double targetPosition) {
        return characterize(startDelay, rampRate, targetPosition).beforeStarting(()-> zeroLock());
    }

    public Command characterizeRotation(double startDelay, double rampRate, double targetPosition) {
        return characterize(startDelay, rampRate, targetPosition).beforeStarting(()-> oLock());
    }

}