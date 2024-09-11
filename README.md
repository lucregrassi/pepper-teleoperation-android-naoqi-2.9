# Pepper Teleoperation Android Application

This project provides an Android application that allows the Pepper robot to receive commands over a network and perform actions such as movement, speech, and volume control. The app listens for UDP commands sent from a teleoperation interface and translates them into actions for the Pepper robot.

## Features

- **Movement Commands**: Move forward, backward, rotate left, rotate right, and stop the robot.
- **Speech Commands**: Make Pepper say custom phrases.
- **Volume Control**: Adjust Pepper's volume up or down.
- **Autonomous Abilities Control**: Holds Pepper's autonomous abilities (e.g., basic awareness) during teleoperation.

## Requirements

- Pepper Robot running **Naoqi 2.9**.
- The [Pepper Teleoperation Interface](https://github.com/lucregrassi/pepper_naoqi2.9_teleoperation) running on a computer to send commands.
- Android Studio for building and deploying the app to the Pepper robot.

## Installation

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/lucregrassi/pepper-teleoperation-android.git
   cd pepper-teleoperation-android
   ```
