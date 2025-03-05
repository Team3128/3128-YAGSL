package common.core.subsystems;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

import common.hardware.motorcontroller.NAR_Motor.Neutral;
import edu.wpi.first.wpilibj2.command.Command;
import static edu.wpi.first.wpilibj2.command.Commands.*;
import edu.wpi.first.wpilibj2.command.Subsystem;

public interface NAR_Subsystem extends Subsystem{
    
    abstract void reset();
    abstract Command resetCommand();

    abstract void run(double power);
    abstract Command runCommand(double power);

    public void runVolts(double volts);
    public Command runVoltsCommand(double volts);

    public void stop();
    public Command stopCommand();

    public void initShuffleboard();

    public void setNeutralMode(Neutral mode);

    public double getVolts();
}
