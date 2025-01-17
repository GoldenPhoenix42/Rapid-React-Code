// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.revrobotics.CANSparkMax;
import com.revrobotics.SparkMaxRelativeEncoder;
//import com.revrobotics.RelativeEncoder;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DriverStation;
//import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.Compressor;
//import static edu.wpi.first.wpilibj.DoubleSolenoid.Value.*;
import edu.wpi.first.wpilibj.PowerDistribution;
import edu.wpi.first.wpilibj.PowerDistribution.ModuleType;




import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;



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

  private Timer auto;
  
  private DifferentialDrive m_myRobot;
  private CANSparkMax m_frontLeft;
  private CANSparkMax m_frontRight;
  private CANSparkMax m_rearLeft;
  private CANSparkMax m_rearRight;

  private VictorSPX m_backShooter;
  private VictorSPX m_frontShooter;

  private VictorSPX m_elevator1;
  private VictorSPX m_elevator2;
  private VictorSPX m_cargoSlurper;

  private TalonSRX m_shoulder;
  private TalonSRX m_winch;

  private MotorControllerGroup m_left;
  private MotorControllerGroup m_right;

  private final XboxController m_controller = new XboxController(0);
  private final XboxController m_controller2 = new XboxController(1);

  // Pneumatics
  private final DoubleSolenoid m_solenoid1 = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, 0, 1);
  private final Compressor compressor = new Compressor(0, PneumaticsModuleType.CTREPCM);

  boolean enabled = compressor.enabled();
  boolean pressureSwitch = compressor.getPressureSwitchValue();

  double speed;
  double speed2;

  boolean triggerHappy;
  boolean isTankDrive;
  boolean sucking;

  private PowerDistribution m_pdp;
  double voltage;

  boolean doingYourMom;

  String ally; 
  boolean isBlue;

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    ally = DriverStation.getAlliance().toString();

    if(ally.equals("Blue")){
      isBlue = true;
    }
    else{
      isBlue = false;
    }

    auto = new Timer();
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);

    //drive motors
    m_frontLeft = new CANSparkMax(4, MotorType.kBrushless);
    m_frontRight = new CANSparkMax(2, MotorType.kBrushless);
    m_rearLeft = new CANSparkMax(3, MotorType.kBrushless);
    m_rearRight = new CANSparkMax(5, MotorType.kBrushless);
    
    //shooter motors
    m_backShooter = new VictorSPX(6);
    m_frontShooter = new VictorSPX(7);
    m_backShooter.setInverted(true);
    m_frontShooter.setInverted(true);

    //elevator and cargo slurper motors
    m_elevator1 = new VictorSPX(8);
    m_elevator2 = new VictorSPX(9);
    m_cargoSlurper = new VictorSPX(10);

    //climb motors
    m_shoulder = new TalonSRX(11);
    m_winch = new TalonSRX(12);

    m_frontLeft.restoreFactoryDefaults();
    m_frontRight.restoreFactoryDefaults();
    m_rearLeft.restoreFactoryDefaults();
    m_rearRight.restoreFactoryDefaults();

    m_left = new MotorControllerGroup(m_frontLeft, m_rearLeft);
    m_right = new MotorControllerGroup(m_frontRight, m_rearRight);
    m_myRobot = new DifferentialDrive(m_left, m_right);

    NetworkTableInstance.getDefault().getTable("limelight").getEntry("camMode").setNumber(1);
    NetworkTableInstance.getDefault().getTable("limelight").getEntry("stream").setNumber(0);
    NetworkTableInstance.getDefault().getTable("limelight").getEntry("ledMode").setNumber(1);

    m_frontLeft.getEncoder(SparkMaxRelativeEncoder.Type.kHallSensor, 42).getVelocity();

    triggerHappy = false;
    isTankDrive = true;
    sucking = false;

    m_pdp = new PowerDistribution(0, ModuleType.kCTRE);

    doingYourMom = false;

    System.out.println(ally);
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
    voltage = m_pdp.getVoltage();
    uptime = Timer.getFPGATimestamp();
    SmartDashboard.putNumber("Uptime", uptime);
    SmartDashboard.putNumber("Front Left Motor RPM", m_frontLeft.getEncoder(SparkMaxRelativeEncoder.Type.kHallSensor, 42).getVelocity());
    SmartDashboard.putNumber("Front Right Motor RPM", m_frontRight.getEncoder(SparkMaxRelativeEncoder.Type.kHallSensor, 42).getVelocity());
    SmartDashboard.putNumber("Rear Left Motor RPM", m_rearLeft.getEncoder(SparkMaxRelativeEncoder.Type.kHallSensor, 42).getVelocity());
    SmartDashboard.putNumber("Rear Right Motor RPM", m_rearRight.getEncoder(SparkMaxRelativeEncoder.Type.kHallSensor, 42).getVelocity());
    SmartDashboard.putBoolean("Is Tank Drive (LJ)", isTankDrive);
    SmartDashboard.putBoolean("Is Trigger Happy? (RB)", triggerHappy);
    SmartDashboard.putNumber("Total Voltage", voltage);
    SmartDashboard.putBoolean("Is sucking? (LB)", sucking);
    SmartDashboard.putBoolean("Is doing your mom?", doingYourMom);
    SmartDashboard.putBoolean("Is blue?", isBlue);

    if (triggerHappy){
      SmartDashboard.putNumber("Shooter motor speed percentage", speed2*100);
    }
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
        auto.start();
        m_cargoSlurper.set(ControlMode.PercentOutput, 100);
        m_frontShooter.set(ControlMode.PercentOutput, 100);
        m_backShooter.set(ControlMode.PercentOutput, 100);
        m_elevator1.set(ControlMode.PercentOutput, 100);
        m_elevator2.set(ControlMode.PercentOutput, 100);

        break;
      case kDefaultAuto:
      default:
        // Put default auto code here
        auto.start();

        while(auto.get()<=3){
          // m_cargoSlurper.set(ControlMode.PercentOutput, 1);
          m_frontShooter.set(ControlMode.PercentOutput, .5);
          m_backShooter.set(ControlMode.PercentOutput, .5);
          m_elevator1.set(ControlMode.PercentOutput, 1);
          m_elevator2.set(ControlMode.PercentOutput, 1);
        }

        m_frontShooter.set(ControlMode.PercentOutput, 0);
        m_backShooter.set(ControlMode.PercentOutput, 0);
        m_elevator1.set(ControlMode.PercentOutput, 0);
        m_elevator2.set(ControlMode.PercentOutput, 0);
        /* 
        while(3<auto.get()&&auto.get()<=4){
          m_left.set(.5);
          m_right.set(.5);
        }
        */
        while(4<auto.get()&&auto.get()<=6){
          //m_cargoSlurper.set(ControlMode.PercentOutput, 1);
          //m_elevator1.set(ControlMode.PercentOutput, 1);
          //m_elevator2.set(ControlMode.PercentOutput, 1);
          m_left.set(-0.5);
          m_right.set(0.5);
        } 

        /**
        while(6<auto.get()&&auto.get()<=7){
          m_left.set(0.5);
          m_right.set(0.5);
        }
        while(6<auto.get() && auto.get() <= 8){
          m_left.set(1.0);
          m_right.set(-1.0);
        }
        while(auto.get()>8){
          //m_cargoSlurper.set(ControlMode.PercentOutput, 1);
          m_frontShooter.set(ControlMode.PercentOutput, 0.5);
          m_backShooter.set(ControlMode.PercentOutput, 0.5);
          m_elevator1.set(ControlMode.PercentOutput, 1);
          m_elevator2.set(ControlMode.PercentOutput, 1);
        } 
        */
        m_left.set(0);
        m_right.set(0);

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
    if (m_controller.getLeftBumperPressed()){
      isTankDrive = !isTankDrive;
    }

    if(isTankDrive){
      m_myRobot.tankDrive(-m_controller.getLeftY(), m_controller.getRightY());
    }
    else{
      m_myRobot.arcadeDrive(m_controller.getRightY(), m_controller.getRightX());
    }

    
    //intake
    
    if (m_controller.getAButton()) {
      m_cargoSlurper.set(ControlMode.PercentOutput, 1);
    }
    else{
      m_cargoSlurper.set(ControlMode.PercentOutput, 0);
    }

    if(m_controller2.getLeftBumperPressed()){
      sucking = !sucking;
    }

    if(!sucking){
      //launcher
      if (m_controller2.getXButton()) {
        m_frontShooter.set(ControlMode.PercentOutput, 0.5);
        m_backShooter.set(ControlMode.PercentOutput, 0.5);
      }    
      else if (m_controller2.getBButton()){
        m_frontShooter.set(ControlMode.PercentOutput, 0.25);
        m_backShooter.set(ControlMode.PercentOutput,  0.25);
      }
      else{
        m_frontShooter.set(ControlMode.PercentOutput, 0);
        m_backShooter.set(ControlMode.PercentOutput, 0);
      }

      speed = m_controller2.getLeftTriggerAxis();
      m_elevator1.set(ControlMode.PercentOutput, speed);
      m_elevator2.set(ControlMode.PercentOutput, speed);

      if (m_controller2.getRightBumperPressed()){
        triggerHappy = !triggerHappy;
      }

      if (triggerHappy){
        speed2 = m_controller2.getRightTriggerAxis();
        m_frontShooter.set(ControlMode.PercentOutput, speed2);
        m_backShooter.set(ControlMode.PercentOutput, speed2);
      }
    }
    else{
      //launcher
      if (m_controller2.getXButton()) {
        m_frontShooter.set(ControlMode.PercentOutput, -0.25);
        m_backShooter.set(ControlMode.PercentOutput, -0.25);
      }
      else if (m_controller2.getBButton()){
        m_frontShooter.set(ControlMode.PercentOutput, -0.1);
        m_backShooter.set(ControlMode.PercentOutput,  -0.1);
      }
      else{
        m_frontShooter.set(ControlMode.PercentOutput, 0);
        m_backShooter.set(ControlMode.PercentOutput, 0);
      }

      speed = m_controller2.getLeftTriggerAxis();
      m_elevator1.set(ControlMode.PercentOutput, -speed*0.5);
      m_elevator2.set(ControlMode.PercentOutput, -speed*0.5);

      if (m_controller2.getRightBumperPressed()){
        triggerHappy = !triggerHappy;
      }

      if (triggerHappy){
        speed2 = m_controller2.getRightTriggerAxis();
        m_frontShooter.set(ControlMode.PercentOutput, -speed2*0.5);
        m_backShooter.set(ControlMode.PercentOutput, -speed2*0.5);
      }
    }
    //elevator/storage
    /*
    if (m_controller2.getAButton()){
      m_elevator1.set(ControlMode.PercentOutput, 1);
      m_elevator2.set(ControlMode.PercentOutput, 1);
    }
    else{
      m_elevator1.set(ControlMode.PercentOutput, 0);
      m_elevator2.set(ControlMode.PercentOutput, 0);
    }*/
    //Pnumatics
    /*
    if (m_controller2.getYButton())
    {
      m_solenoid1.set(DoubleSolenoid.Value.kForward);
    }
    else
    {
      m_solenoid1.set(DoubleSolenoid.Value.kReverse);
    } 
    */

    if((m_controller.getStartButtonPressed() || m_controller2.getStartButtonPressed())&&sucking){
      doingYourMom = true;
    }

  }

  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {
    NetworkTableInstance.getDefault().getTable("limelight").getEntry("ledMode").setNumber(1);
    m_controller.setRumble(RumbleType.kLeftRumble, 0.0);
    m_controller.setRumble(RumbleType.kRightRumble, 0.0);
    doingYourMom = false;
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
    //m_myRobot.tankDrive(-m_controller.getLeftY(), m_controller.getRightY());
    m_myRobot.arcadeDrive(m_controller.getLeftX(), -m_controller.getLeftY());
  }
}
