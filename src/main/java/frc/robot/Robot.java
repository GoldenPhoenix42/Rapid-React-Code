// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  private double uptime;
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
  
  private DifferentialDrive m_myRobot;
  private CANSparkMax m_frontLeft;
  private CANSparkMax m_frontRight;
  private CANSparkMax m_rearLeft;
  private CANSparkMax m_rearRight;


  private MotorControllerGroup m_left;
  private MotorControllerGroup m_right;

  //Ball Storage
  private CANSparkMax topStorage = new CANSparkMax(0, MotorType.kBrushless);
  private CANSparkMax bottomStorage = new CANSparkMax(0, MotorType.kBrushless);

  private MotorControllerGroup storage;

  //Arms 

  private CANSparkMax shoulder = new CANSparkMax(0, MotorType.kBrushless);
  private CANSparkMax winch1 = new CANSparkMax(0, MotorType.kBrushless);
  // private CANSparkMax winch2 = new CANSparkMax(0, MotorType.kBrushless);
  // private MotorControllerGroup winch = new MotorControllerGroup(winch1, winch2);

  private CANSparkMax grabber = new CANSparkMax(0, MotorType.kBrushless);

  //Xbox controllers
  private final XboxController m_controller1 = new XboxController(0);
  private final XboxController m_controller2 = new XboxController(1);
  //limelight
  private boolean lemonLight;

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);

    //Wheels 
    m_frontLeft = new CANSparkMax(4, MotorType.kBrushless);
    m_frontRight = new CANSparkMax(2, MotorType.kBrushless);
    m_rearLeft = new CANSparkMax(3, MotorType.kBrushless);
    m_rearRight = new CANSparkMax(5, MotorType.kBrushless);

    m_left = new MotorControllerGroup(m_frontLeft, m_rearLeft);
    m_right = new MotorControllerGroup(m_frontRight, m_rearRight);
    m_myRobot = new DifferentialDrive(m_left, m_right);

    //launcher 
    
    storage = new MotorControllerGroup(topStorage, bottomStorage);


    //Limelight
    NetworkTableInstance.getDefault().getTable("limelight").getEntry("camMode").setNumber(1);
    NetworkTableInstance.getDefault().getTable("limelight").getEntry("stream").setNumber(0);
    NetworkTableInstance.getDefault().getTable("limelight").getEntry("ledMode").setNumber(1);

    lemonLight = false;

    //Factory Default
    m_frontLeft.restoreFactoryDefaults();
    m_frontRight.restoreFactoryDefaults();
    m_rearLeft.restoreFactoryDefaults();
    m_rearRight.restoreFactoryDefaults();
    topStorage.restoreFactoryDefaults();
    bottomStorage.restoreFactoryDefaults();
    shoulder.restoreFactoryDefaults();
    winch1.restoreFactoryDefaults();
    //winch2.restoreFactoryDefaults();
    grabber.restoreFactoryDefaults();
  }

  /**
   * This function is called every robot packet, no matter the mode. Use this for items like
   * diagnostics that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
    uptime = Timer.getFPGATimestamp();
    SmartDashboard.putNumber("Uptime", uptime);
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select between different
   * autonomous modes using the dashboard. The sendable chooser code works with the Java
   * SmartDashboard. If you prefer the LabVIEW Dashboard, remove all of the chooser code and
   * uncomment the getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to the switch structure
   * below with additional strings. If using the SendableChooser make sure to add them to the
   * chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
      case kCustomAuto:
        // Put custom auto code here
        break;
      case kDefaultAuto:
      default:
        // Put default auto code here
        break;
    }
  }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {}

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {
    //Drive
    m_myRobot.tankDrive(-m_controller1.getLeftY(), m_controller1.getRightY());
    
    //Ball Storage
    if (m_controller2.getLeftBumperPressed() == true){
      storage.set(1.0);
    }
    else{
      storage.set(0);
    }

    //Arm 
    shoulder.set(m_controller2.getLeftY());
    winch1.set(m_controller2.getRightY());
    //winch.set(m_controller2.getRightY());

    //Limelight
    if (m_controller1.getYButtonPressed() == true) {
      lemonLight = !lemonLight;
    }

    if (lemonLight == true) {
      NetworkTableInstance.getDefault().getTable("limelight").getEntry("ledMode").setNumber(3);
    } else if (lemonLight == false) {
      NetworkTableInstance.getDefault().getTable("limelight").getEntry("ledMode").setNumber(1);
    }
  }

  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {
    NetworkTableInstance.getDefault().getTable("limelight").getEntry("ledMode").setNumber(1);
    m_controller1.setRumble(RumbleType.kLeftRumble, 0.0);
    m_controller1.setRumble(RumbleType.kRightRumble, 0.0);
  }

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic() {}

  /** This function is called once when test mode is enabled. */
  @Override
  public void testInit() {}

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {
    //m_myRobot.tankDrive(-m_controller1.getLeftY(), m_controller1.getRightY());
    m_myRobot.arcadeDrive(m_controller1.getLeftX(), -m_controller1.getLeftY());
  }
}
