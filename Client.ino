#include <Thread.h>
#include <TimerOne.h>
#include <Servo.h>

Thread sensorThread = Thread();
Servo myServo;
float inMin = -9.999, inMax =  9.999;
int outMin = -255, outMax = 255;
int encoderPinL = 24, encoderPinR = 13;
unsigned int counterEncoderR = 0;
unsigned int counterEncoderL = 0;
uint8_t inA = 7, inB = 2;
uint8_t INPUT1 = 5, INPUT2 = 6, INPUT3 = 3, INPUT4 = 4;
uint8_t trigPinCen = 50, echoPinCen = 52;
uint8_t trigPinLeft = 9, echoPinLeft = 8;
uint8_t trigPinRight = 12, echoPinRight = 11;
char getChar;
String getString = "";
String data[12];
int counter = 0;
int lastIndex = 0;
int yAxis = 0;
int command, pwmL, pwmR;
String cmd;
String obst = "";

void initializePins() {
  pinMode(INPUT1, OUTPUT);
  pinMode(INPUT2, OUTPUT);
  pinMode(INPUT3, OUTPUT);
  pinMode(INPUT4, OUTPUT);
  pinMode(encoderPinR, INPUT);
  pinMode(encoderPinL, INPUT);
  pinMode(trigPinLeft, OUTPUT);
  pinMode(echoPinLeft, INPUT);
  pinMode(trigPinCen, OUTPUT);
  pinMode(echoPinCen, INPUT);
}

void setup() {
  initializePins();
  Serial.begin(115200);
  Serial1.begin(9600);
  sensorThread.setInterval(100);
  sensorThread.onRun(sensorCallback);
  myServo.attach(10);
  myServo.write(90);
  /*pinMode(encoderPinR, INPUT);
  Timer1.initialize(1000000);
  attachInterrupt(0,do_count,RISING);
  Timer1.attachInterrupt(timerIsr);*/
}

void loop() {
  if (Serial.available() > 0) {
    getChar = Serial.read();
    if (getChar == '\r') {
      for (int i = 0; i < getString.length(); i++) {
        if (getString.substring(i, i+1) == "|") {
          data[counter] = getString.substring(lastIndex, i);
          lastIndex = i + 1;
          counter++;
        }
      }
      getString = "";
      counter = 0;
      lastIndex = 0;
    } else {
      getString += getChar;
    }

    cmd = data[0];
    robotWalk(cmd);

    command = data[0].toInt();
    pwmL = data[1].toInt();
    pwmR = data[2].toInt();
    if (command == 1 || command == 2 || command == 3) {
      setDirection(command, pwmL, pwmR);
    } else if (command == 4) {
      /*if(sensorThread.shouldRun()) {
        sensorThread.run();
      }*/
    }
    
  }
  /*if(sensorThread.shouldRun()) {
      sensorThread.run();
    }*/
}

void robotWalk(String cmd) {
  if (cmd == "Forward") {
    Serial.println("GO");
    digitalWrite(INPUT1, HIGH);
    digitalWrite(INPUT2, LOW);
    analogWrite(inA, 95);
    digitalWrite(INPUT3, HIGH);
    digitalWrite(INPUT4, LOW);
    analogWrite(inB, 95);  
  } else if (cmd == "Turnleft") {
    Serial.println("Left");
    digitalWrite(INPUT1, HIGH);
    digitalWrite(INPUT2, LOW);
    analogWrite(inA, 70);
    digitalWrite(INPUT3, LOW);
    digitalWrite(INPUT4, HIGH);
    analogWrite(inB, 0);
  } else if (cmd == "Turnright") {
    Serial.println("Right");
    digitalWrite(INPUT1, LOW);
    digitalWrite(INPUT2, HIGH);
    analogWrite(inA, 0);
    digitalWrite(INPUT3, HIGH);
    digitalWrite(INPUT4, LOW);
    analogWrite(inB, 70);
  } else if (cmd == "Turnback") {
    Serial.println("Right");
    digitalWrite(INPUT1, LOW);
    digitalWrite(INPUT2, HIGH);
    analogWrite(inA, 75);
    digitalWrite(INPUT3, HIGH);
    digitalWrite(INPUT4, LOW);
    analogWrite(inB, 75);  
  } else if (cmd == "Stop") {
    Stop();
  }
}

void Stop() {
  digitalWrite(INPUT1, LOW);
  digitalWrite(INPUT2, LOW);
  analogWrite(inA, 0);
  digitalWrite(INPUT3, LOW);
  digitalWrite(INPUT4, LOW);
  analogWrite(inB, 0);
}

boolean checkObst() {
  long FrontSensor = SonarSensor(trigPinCen, echoPinCen);
  if (FrontSensor >= 9.0 && FrontSensor <= 13.0) {
    return true;
  } else {
    return false;
  }
}

void setDirection(int command, int PWMl, int PWMr) {
  switch(command) {
    case 1:
      digitalWrite(INPUT1, HIGH);
      digitalWrite(INPUT2, LOW);
      analogWrite(inA, PWMr);
      digitalWrite(INPUT3, HIGH);
      digitalWrite(INPUT4, LOW);
      analogWrite(inB, PWMl);
      break;
    case 2:
      digitalWrite(INPUT1, LOW);
      digitalWrite(INPUT2, HIGH);
      analogWrite(inA, PWMr);
      digitalWrite(INPUT3, LOW);
      digitalWrite(INPUT4, HIGH);
      analogWrite(inB, PWMl);
      break;
    case 3:
      digitalWrite(INPUT1, LOW);
      digitalWrite(INPUT2, LOW);
      analogWrite(inA, 0);
      digitalWrite(INPUT3, LOW);
      digitalWrite(INPUT4, LOW);
      analogWrite(inB, 0);
      break;
    default:
      break;
  }
}

void setServoPosition(float yAxis) {
  float g = 9.81;
  int y = (yAxis/g) * 255;
  y = map(y, 255, -255, 0, 180);
  myServo.write(y);
}

void beaconCallback(){
  String data[20];
  int counter = 0;
  int lastIndex = 0;
  Serial1.write("AT+DISI?");
  if (Serial1.available() > 0) {
    String str = Serial1.readString();
      for (int i = 0; i < str.length(); i++) {
        if (str.substring(i, i+1) == ":") {
          data[counter] = str.substring(lastIndex, i);
          lastIndex = i + 1;
          counter++;
        }
        if (i == str.length() - 1) {
          data[counter] = str.substring(lastIndex, i+1);
        }
      }
      counter = 0;
      lastIndex = 0;
      String rssibeacon2 = data[5].substring(2, 4);
      String rssibeacon1 = data[10].substring(2, 4);
      String rssibeacon3 = data[15].substring(2, 4);
      Serial.println(data[4] + "|" + rssibeacon2 + "|" + data[9] + "|" + rssibeacon1 + "|" + data[14] + "|" + rssibeacon3);
  }
}
void sensorCallback(){
  int flag = 1, flagS = 1;
  long FrontSensor = SonarSensor(trigPinCen, echoPinCen);
  long LeftSensor = SonarSensor(trigPinLeft, echoPinLeft);
  long RightSensor = SonarSensor(trigPinRight, echoPinRight);
  if (FrontSensor > 0.0 && FrontSensor <= 9.0) {
    Serial.print("Front|");
    Serial.println(FrontSensor);
  } else if (LeftSensor > 0.0 && LeftSensor <= 9.0) {    
    Serial.print("Left|");
    Serial.println(LeftSensor);
  } else if (RightSensor > 0.0 && RightSensor <= 9.0) {
    Serial.print("Right|");
    Serial.println(RightSensor);
  } else if (FrontSensor >= 11.0 && FrontSensor <= 15.0) {
    Serial.println("Stop|");
    Stop();
  } else if (FrontSensor >= 15.0 || LeftSensor >= 15.0) {
    Serial.println("Stopsound|0|");
    delay(1000);
  }
  /*if (LeftSensor > 0.0 && LeftSensor <= 4.0 && flag == 1) {    
    Serial.print("Left|");
    Serial.println(LeftSensor);
    flag = 0;
    flagS = 1;
  } else if (LeftSensor >= 6.0 && LeftSensor <= 10.0 && flag == 0) {
    Serial.print("Left|");
    Serial.println(LeftSensor);
    flag = 1;
    flagS = 1;
  } else if (LeftSensor > 10.0 && LeftSensor <= 14.0 && flagS == 1) {
    Serial.print("Left|");
    Serial.println(LeftSensor);
    flagS = 0;
  }*/
}

long SonarSensor(int trigPin,int echoPin) {
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);
  long duration = pulseIn(echoPin, HIGH);
  long distance = (duration/2) / 29.1;
  return distance;
}

/*void do_count() {
  counter++;
}

void timerIsr() {
  Timer1.detachInterrupt();
  Serial.print("Motor Speed: ");
  int rotation = (counter/20)*60;
  Serial.print(rotation, DEC);
  Serial.println(" RPM");
  counter=0;
  Timer1.attachInterrupt(timerIsr);
}*/

