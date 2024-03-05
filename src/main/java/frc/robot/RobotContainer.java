// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.PS4Controller;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.Constants.OperatorConstants;
import frc.robot.Constants.PieceConstants;
import frc.robot.commands.AbsoluteDrive;
import frc.robot.commands.ClimbCommand;
import frc.robot.commands.IntakeCommand;
import frc.robot.commands.IntakeMoveCommand;
import frc.robot.commands.LimelightDrive;
import frc.robot.commands.SimpleIntakeMoveCommand;
import frc.robot.commands.ShootCommand;
import frc.robot.subsystems.ClimbSubsystem;
import frc.robot.subsystems.IntakeMoveSubsystem;
import frc.robot.subsystems.IntakeSubsystem;
import frc.robot.subsystems.LimelightSubsystem;
import frc.robot.subsystems.ShootSubsystem;
import frc.robot.subsystems.SwerveSubsystem;

import java.io.File;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a "declarative" paradigm, very
 * little robot logic should actually be handled in the {@link Robot} periodic methods (other than the scheduler calls).
 * Instead, the structure of the robot (including subsystems, commands, and trigger mappings) should be declared here.
 */
public class RobotContainer {

  // The robot's subsystems and commands are defined here...
  private final SwerveSubsystem drivebase = new SwerveSubsystem(new File(Filesystem.getDeployDirectory(), "swerve"));
  private final ShootSubsystem shootSubsystem = new ShootSubsystem();
  private final IntakeSubsystem intakeSubsystem = new IntakeSubsystem();
  private final IntakeMoveSubsystem intakeMoveSubsystem = new IntakeMoveSubsystem();
  private final ClimbSubsystem climbSubsystem = new ClimbSubsystem();
  private final LimelightSubsystem limelightSubsystem = new LimelightSubsystem();

  // Replace with CommandPS4Controller or CommandJoystick if needed
  PS4Controller driveController = new PS4Controller(0);
  PS4Controller pieceController = new PS4Controller(1);
  /**
   * The container for the robot. Contains subsystems, OI devices, and commands.
   */
  public RobotContainer() {
    // Configure the trigger bindings
    configureBindings();

    AbsoluteDrive closedAbsoluteDrive = new AbsoluteDrive(drivebase,
      () -> -MathUtil.applyDeadband(driveController.getLeftY(), OperatorConstants.LEFT_Y_DEADBAND),
      () -> -MathUtil.applyDeadband(driveController.getLeftX(), OperatorConstants.LEFT_X_DEADBAND),
      () -> -MathUtil.applyDeadband(driveController.getRightX(),OperatorConstants.RIGHT_X_DEADBAND),
      () -> -MathUtil.applyDeadband(driveController.getRightY(), OperatorConstants.RIGHT_Y_DEADBAND));
    
      // Applies deadbands and inverts controls because joysticks
    // are back-right positive while robot
    // controls are front-left positive
    // left stick controls translation
    // right stick controls the desired angle NOT angular rotation
    Command driveFieldOrientedDirectAngle = drivebase.driveCommand(
        () -> -MathUtil.applyDeadband(driveController.getLeftY(), OperatorConstants.LEFT_Y_DEADBAND),
        () -> -MathUtil.applyDeadband(driveController.getLeftX(), OperatorConstants.LEFT_X_DEADBAND),
        () -> -pieceController.getRightX(),
        () -> -pieceController.getRightY());

    // Applies deadbands and inverts controls because joysticks
    // are back-right positive while robot
    // controls are front-left positive
    // left stick controls translation
    // right stick controls the angular velocity of the robot
    Command driveFieldOrientedAnglularVelocity = drivebase.driveCommand(
        () -> -MathUtil.applyDeadband(driveController.getLeftY(), OperatorConstants.LEFT_Y_DEADBAND),
        () -> -MathUtil.applyDeadband(driveController.getLeftX(), OperatorConstants.LEFT_X_DEADBAND),
        () -> -pieceController.getRightX());

    Command driveFieldOrientedDirectAngleSim = drivebase.simDriveCommand(
        () -> -MathUtil.applyDeadband(driveController.getLeftY(), OperatorConstants.LEFT_Y_DEADBAND),
        () -> -MathUtil.applyDeadband(driveController.getLeftX(), OperatorConstants.LEFT_X_DEADBAND),
        () -> -pieceController.getRightX());

    drivebase.setDefaultCommand(
        !RobotBase.isSimulation() ? driveFieldOrientedAnglularVelocity : driveFieldOrientedDirectAngleSim);
    /* 
     *  drivebase.setDefaultCommand(
        !RobotBase.isSimulation() ? driveFieldOrientedDirectAngle : driveFieldOrientedDirectAngleSim);
     */
  }

  private void configureBindings() {

    Trigger halfSpeed = new Trigger(() -> driveController.getL1Button()); // if L1 on drive, half speed
		Trigger halfTurn = new Trigger (() -> pieceController.getCircleButton()); // if Circle on piece, half turn speed	
		Trigger resetHeading = new Trigger(() -> driveController.getOptionsButton()); // if Options on drive, reset heading

		Trigger shoot = new Trigger(() -> pieceController.getR1Button()); // if R1 on piece, shoot
    Trigger intake = new Trigger(() -> pieceController.getL1Button()); // if L1 on piece, intake a piece 

    Trigger autoIntakeOut = new Trigger (() -> pieceController.getPOV() == 0); // if D-Pad up on piece, auto move intake out
    Trigger autoIntakeIn = new Trigger (() -> pieceController.getPOV() == 180); // if D-Pad down on piece, auto move intake in

    Trigger manualIntake = new Trigger(() -> pieceController.getTriangleButton()); // if Triangle on piece, turn on intake
    Trigger manualIntakeOut = new Trigger(() -> pieceController.getSquareButton()); // if Square on piece, manually move intake out
    Trigger manualIntakeIn = new Trigger(() -> pieceController.getCrossButton()); // if Cross on piece, manually move intake in

    Trigger climbUp = new Trigger(() -> driveController.getTriangleButton()); // if Triangle on drive, move climber up
    Trigger climbDown = new Trigger(() -> driveController.getCrossButton());// if Cross on drive, move climber down

    Trigger fullClimb = new Trigger(() -> driveController.getCircleButton()); // if Circle on drive, full send climber

    // simultaneously push game piece into shooter and shoot
		shoot.whileTrue(new ShootCommand(shootSubsystem, PieceConstants.leftShootPower, PieceConstants.rightShootPower));
    shoot.whileTrue(new WaitCommand(1.5). andThen(new IntakeCommand(
      intakeSubsystem, -PieceConstants.leftUpFeedPower, -PieceConstants.rightDownFeedPower)));

    // automatically move intake out and grab game pieces and then move intake in
    intake.whileTrue(new IntakeCommand(intakeSubsystem, PieceConstants.leftUpIntakePower, PieceConstants.rightDownIntakePower));
    intake.onTrue(new IntakeMoveCommand(intakeMoveSubsystem, PieceConstants.intakeOutAngle, 0.1)); // number is P for PID
    intake.onFalse(new IntakeMoveCommand(intakeMoveSubsystem, PieceConstants.intakeInAngle, 0.05)); // number is P for PID

    // automatically move intake in/out
    autoIntakeOut.onTrue(new IntakeMoveCommand(intakeMoveSubsystem, PieceConstants.intakeOutAngle, 0.1)); // number is P for PID
    autoIntakeIn.onTrue(new IntakeMoveCommand(intakeMoveSubsystem, PieceConstants.intakeInAngle, 0.05)); // number is P for PID

    // manully move intake in/out and manually grab pieces
    manualIntakeOut.whileTrue(new SimpleIntakeMoveCommand(intakeMoveSubsystem, PieceConstants.intakeMovePower));
    manualIntakeIn.whileTrue(new SimpleIntakeMoveCommand(intakeMoveSubsystem, -PieceConstants.intakeMovePower));
    manualIntake.whileTrue(new IntakeCommand(intakeSubsystem, PieceConstants.leftUpIntakePower, PieceConstants.rightDownIntakePower));

    // manually move climb mechanism up and down
    climbUp.whileTrue(new ClimbCommand(climbSubsystem, PieceConstants.climbPower));
    climbDown.whileTrue(new ClimbCommand(climbSubsystem, -PieceConstants.climbPower));

    fullClimb.whileTrue(new ClimbCommand(climbSubsystem, -PieceConstants.fullClimbPower));

///////////////////// TESTING ////////////////////
    Trigger moveTest = new Trigger(() -> driveController.getSquareButton()); 
    moveTest.onTrue(drivebase.driveToPose(
      new Pose2d(new Translation2d(4, 2), Rotation2d.fromDegrees(0))
    ));

    Trigger limelightTest = new Trigger(() -> driveController.getR1Button());
    limelightTest.onTrue(new LimelightDrive(limelightSubsystem, drivebase));

    /*driveController.a().onTrue((Commands.runOnce(drivebase::zeroGyro)));
    driveController.x().onTrue(Commands.runOnce(drivebase::addFakeVisionReading));
    driveController.b().whileTrue(
        Commands.deferredProxy(() -> drivebase.driveToPose(
                                   new Pose2d(new Translation2d(4, 4), Rotation2d.fromDegrees(0)))
                              )); */
    // driveController.x().whileTrue(Commands.runOnce(drivebase::lock, drivebase).repeatedly());
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    // An example command will be run in autonomous
    return drivebase.getAutonomousCommand("New Auto");
  }

  public void setDriveMode() {
    //drivebase.setDefaultCommand();
  }

  public void setMotorBrake(boolean brake) {
    drivebase.setMotorBrake(brake);
  }
}
