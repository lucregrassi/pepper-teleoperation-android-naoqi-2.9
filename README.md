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

2.	Open the Project in Android Studio:
    * Import the project into Android Studio to build and run it on an Android device.

4. Connect to Pepper Robot:
   * Ensure the Android device and Pepper robot are connected to the same network.
   * Deploy the application on your Android device or a Pepper tablet

## Usage

Once the application is running on an Android device or Pepper tablet, it listens for UDP commands on port 54321. The following actions can be triggered based on the received command.

Movement commands:
   * MOVE_FORWARD: Moves Pepper forward.
   * MOVE_BACKWARD: Moves Pepper backward.
   * ROTATE_LEFT: Rotates Pepper to the left.
   * ROTATE_RIGHT: Rotates Pepper to the right.
   * STOP: Stops Pepper’s movement.

Speech commands:
   * Any custom string sent over UDP will make Pepper speak the received text using the ALTextToSpeech service.

Volume control:
   * VOLUME_UP: Increases Pepper’s volume.
   * VOLUME_DOWN: Decreases Pepper’s volume.

The app can also control Pepper’s autonomous abilities during teleoperation:
   * Basic Awareness is enabled to allow the robot to stay aware of its surroundings while being controlled.

## UI Controls

The app also provides buttons in the UI to manually control Pepper’s movement:
   * Move forward, backward, rotate left, rotate right, and stop the robot.
   * Volume control is also available via buttons.

## License

This project is licensed under the GNU  License. See the [LICENSE](https://github.com/lucregrassi/pepper-teleoperation-android-naoqi-2.9/blob/main/LICENSE) file for details.

## Contact
   * GitHub: [lucregrassi](https://github.com/lucregrassi)
   * Email: lucrezia.grassi@edu.unige.it

