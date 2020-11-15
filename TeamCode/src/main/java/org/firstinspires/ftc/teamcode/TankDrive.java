//Tank Drive


package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;


@TeleOp(name = "TankDrive", group = "Taus")

public class TankDrive extends LinearOpMode {

    public AutonomousMethods method = new AutonomousMethods();
    boolean isAPressed = false;
    boolean shooterOn = false;
    boolean isBPressed = false;
    boolean clawClosed = false;
    boolean isRunning = false;

    boolean isXPressed = false;
    boolean stopperDown = true;

    @Override
    public void runOpMode() {

        method.robot.initializeHardware(hardwareMap);
        method.runWithouthEncoders();
        method.robot.frontLeftMotor.setDirection(DcMotor.Direction.FORWARD);
        method.robot.frontRightMotor.setDirection(DcMotor.Direction.REVERSE);
        method.robot.backLeftMotor.setDirection(DcMotor.Direction.FORWARD);
        method.robot.backRightMotor.setDirection(DcMotor.Direction.REVERSE);

        telemetry.addLine("Waiting for Start Button");
        telemetry.update();

        waitForStart();

        telemetry.addData("Mode", "running");
        telemetry.update();

        method.controlLaunchServo(1);




        while (opModeIsActive()) {

            drive1();
            shooter();
            intake();
            claw();
            stopper();
            resetAngle();
            telemetry.addLine("");
            //telemetry.addData("Time ::", method.robot.period.seconds());

            telemetry.update();
            telemetry.clear();
        }


        method.setAllMotorsTo(0);
    }


    public void drive() {
        method.robot.frontLeftMotor.setPower(-gamepad1.left_stick_y);
        method.robot.backLeftMotor.setPower(-gamepad1.left_stick_y);
        method.robot.frontRightMotor.setPower(-gamepad1.right_stick_y);
        method.robot.backRightMotor.setPower(-gamepad1.right_stick_y);

    }

    public void drive1(){
        double scaleFactor = 1;
        double rotationValue = gamepad1.right_stick_x;
        double stickX = gamepad1.left_stick_x;
        double stickY = gamepad1.left_stick_y;

        double gyroAngle = method.getHeading() * Math.PI / 180; //Converts gyroAngle into radians
        if (gyroAngle > Math.PI/2) {
            gyroAngle = -gyroAngle + (5*Math.PI / 2);
        }
        else{
            gyroAngle = -(gyroAngle-90);
        }

        //Robot Centric
        //gyroAngle = Math.PI / 2;

        //Linear directions in case you want to do straight lines.

        if (gamepad1.dpad_right) {
            stickX = .3;
        } else if (gamepad1.dpad_left) {
            stickX = -.3;
        }
        if (gamepad1.dpad_up) {
            stickY = .3;
        } else if (gamepad1.dpad_down) {
            stickY = -.3;
        }


        //MOVEMENT for rotation
        //inverse tangent of gamepad stick y/ gamepad stick x = angle of joystick
        double joystickAngle = Math.atan2(stickY, stickX);
        double theta =  gyroAngle - joystickAngle;

        //theta + pi/4 because wheels apply power at 45 degree angle
        double calculationAngle = theta + (Math.PI / 4);

        //magnatude of movement using pythagorean theorem
        double magnitude = Math.sqrt(Math.pow(stickX, 2) + Math.pow(stickY, 2));


        double xComponent = magnitude * (Math.cos(calculationAngle));
        double yComponent = magnitude * (Math.sin(calculationAngle));

        //creates scaleFactor to make sure movement+turning doesn't exceed power 1
        if (yComponent - rotationValue > 1) {
            scaleFactor = Math.abs(yComponent - rotationValue);
        }
        if (yComponent + rotationValue > 1 && yComponent + rotationValue > scaleFactor) {
            scaleFactor = Math.abs(yComponent + rotationValue);
        }



        method.robot.frontLeftMotor.setPower((yComponent + rotationValue) / scaleFactor);
        method.robot.backLeftMotor.setPower((xComponent + rotationValue) / scaleFactor);
        method.robot.backRightMotor.setPower((yComponent - rotationValue) / scaleFactor);
        method.robot.frontRightMotor.setPower((xComponent - rotationValue) / scaleFactor);


    }

    public void shooter(){

        if(gamepad1.a && !isAPressed){
            isAPressed = true;
            if (!shooterOn) {
              method.setShooterPower(1);
              shooterOn = true;
            }
            else{
             method.setShooterPower(.2);
             shooterOn = false;
            }
        }
        if(!gamepad1.a){
            isAPressed = false;
        }

    }

    public void stopper(){

        if(gamepad1.x && !isXPressed){
            isXPressed = true;
            if (!stopperDown) {
                method.controlLaunchServo(.85);
                stopperDown = true;
            }
            else{
                method.controlLaunchServo(0);
                stopperDown = false;
            }
        }
        if(!gamepad1.x){
            isXPressed = false;
        }

    }

    public void intake(){

        if(gamepad1.right_bumper){
           method.setIntakePower(1);
        }
        else if (gamepad1.left_bumper){
            method.setIntakePower(-1);
        }
        else{
          method.setIntakePower(0);
        }

    }

    public void claw(){

        if((gamepad1.b && !isBPressed)){
            isBPressed = true;
            if (!clawClosed) {
                method.controlClawServo(.25);//closing claw
                isRunning = true;
                method.runtime.reset();

            }
            else{
                method.controlArmServo(1);//moving arm down
                isRunning = true;
                method.runtime.reset();
            }
        }
        if(!gamepad1.b){
            isBPressed = false;
        }
        if (isRunning){
            telemetry.addData("sec", method.runtime.seconds());
            telemetry.update();
            if (!clawClosed){
                if (method.runtime.seconds() > .5) {
                    method.controlArmServo(0);//move arm up
                    clawClosed = true;
                    isRunning = false;
                }

            }
            else{
                if (method.runtime.seconds() > .5) {
                    method.controlClawServo(.7);//opening claw
                    clawClosed = false;
                    isRunning = false;
                }
            }
        }

    }

    public void resetAngle() {
        if (gamepad1.y) {
            method.resetAngle = method.getHeading() + method.resetAngle;
        }
    }
}








