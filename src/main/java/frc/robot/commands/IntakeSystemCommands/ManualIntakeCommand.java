package frc.robot.commands.IntakeSystemCommands;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.IntakeSubsystem;

public class ManualIntakeCommand extends Command{
    
    private final IntakeSubsystem intakeSubsystem;
    private double leftSpeed;
    private double rightSpeed;

    public ManualIntakeCommand(IntakeSubsystem intakeSubsystem, double leftSpeed, double rightSpeed) {
        this.intakeSubsystem = intakeSubsystem;
        this.leftSpeed = leftSpeed;
        this.rightSpeed = rightSpeed;
        addRequirements(intakeSubsystem);
    }

    @Override
    public void initialize() {}

    @Override
    public void execute() {
        intakeSubsystem.intake(leftSpeed, rightSpeed);
    }

    @Override
    public void end (boolean interrupted) {
        intakeSubsystem.intake(0.0, 0.0);
    }

    @Override
    public boolean isFinished() {
        return false;
    } 
}
