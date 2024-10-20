package com.ricelab.pepperteleoperation

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.`object`.actuation.*
import com.aldebaran.qi.sdk.`object`.geometry.Transform
import com.aldebaran.qi.sdk.`object`.holder.AutonomousAbilitiesType
import com.aldebaran.qi.sdk.`object`.holder.Holder
import com.aldebaran.qi.sdk.builder.GoToBuilder
import com.aldebaran.qi.sdk.builder.HolderBuilder
import com.aldebaran.qi.sdk.builder.AnimationBuilder
import com.aldebaran.qi.sdk.builder.AnimateBuilder
import com.aldebaran.qi.sdk.builder.SayBuilder
import com.aldebaran.qi.sdk.builder.TransformBuilder
import kotlinx.coroutines.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import android.media.AudioManager

class MainActivity : AppCompatActivity(), RobotLifecycleCallbacks {

    private lateinit var qiContext: QiContext // Property to store the QiContext
    private var goTo: GoTo? = null
    private var goToFuture: Future<Void>? = null // Add a variable to store the GoTo future
    private var holder: Holder? = null
    private val commandPort = 54321 // The port to listen for commands
    private var job: Job? = null // Job for the UDP listener coroutine
    private lateinit var audioManager: AudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()  // If you're using the native action bar
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)
        QiSDK.register(this, this)

        // Get buttons from layout
        val moveForwardButton: Button = findViewById(R.id.move_forward_button)
        val moveBackwardButton: Button = findViewById(R.id.move_backward_button)
        val rotateLeftButton: Button = findViewById(R.id.rotate_left_button)
        val rotateRightButton: Button = findViewById(R.id.rotate_right_button)
        val stopButton: Button = findViewById(R.id.stop_button)

        // Set button click listeners
        moveForwardButton.setOnClickListener { moveRobot(10.0, 0.0, 0.0) }
        moveBackwardButton.setOnClickListener { moveRobot(-10.0, 0.0, 3.14) }
        rotateLeftButton.setOnClickListener { moveRobot(0.0, 0.0, -1.57) }
        rotateRightButton.setOnClickListener { moveRobot(0.0, 0.0, 1.57) }
        stopButton.setOnClickListener { stopRobot() }
        // Initialize the AudioManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    private fun handleCommand(command: String) {
        when (command) {
            "MOVE_FORWARD" -> moveRobot(10.0, 0.0, 0.0)
            "MOVE_BACKWARD" -> moveRobot(-10.0, 0.0, 3.14)
            "ROTATE_LEFT" -> moveRobot(0.0, 0.0, 1.57)
            "ROTATE_RIGHT" -> moveRobot(0.0, 0.0, -1.57)
            "STOP" -> stopRobot()
            "VOLUME_UP" -> changeVolume(AudioManager.ADJUST_RAISE)
            "VOLUME_DOWN" -> changeVolume(AudioManager.ADJUST_LOWER)
            "HUG" -> performAnimation(R.raw.hug)
            "GREET" -> performAnimation(R.raw.hello)
            "HANDSHAKE" -> performAnimation(R.raw.handshake)
            else -> {
                // If it's not a movement command, assume it's a phrase for the robot to say
                sayText(command)
            }
        }
    }

    private fun performAnimation(animationRes: Int) {
        if (!::qiContext.isInitialized) {
            Log.e("MainActivity", "QiContext is not initialized.")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create an animation object from the resource.
                val myAnimation: Animation = AnimationBuilder.with(qiContext)
                    .withResources(animationRes)
                    .build()

                // Build the Animate action.
                val animate: Animate = AnimateBuilder.with(qiContext)
                    .withAnimation(myAnimation)
                    .build()

                // Run the animation asynchronously.
                animate.async().run().thenConsume { future ->
                    if (future.isSuccess) {
                        Log.i("MainActivity", "Animation completed successfully.")
                    } else {
                        Log.e("MainActivity", "Animation failed: ${future.errorMessage}")
                    }
                }

            } catch (e: Exception) {
                Log.e("MainActivity", "Error during animation: ${e.message}", e)
            }
        }
    }

    private fun changeVolume(direction: Int) {
        // Adjust the volume for the STREAM_MUSIC (used for media playback)
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, 0)
    }

    private fun moveRobot(x: Double, y: Double, theta: Double) {
        if (!::qiContext.isInitialized) {
            Log.e("MainActivity", "QiContext is not initialized.")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val actuation: Actuation = qiContext.actuation
                val robotFrame: Frame = actuation.robotFrame()
                val transform: Transform = TransformBuilder.create().from2DTransform(x, y, theta)
                val mapping: Mapping = qiContext.mapping

                val targetFrame: FreeFrame = mapping.makeFreeFrame()
                targetFrame.update(robotFrame, transform, System.currentTimeMillis())

                goTo = GoToBuilder.with(qiContext)
                    .withFrame(targetFrame.frame())
                    .withFinalOrientationPolicy(OrientationPolicy.ALIGN_X)
                    .withMaxSpeed(0.3F)
                    .withPathPlanningPolicy(PathPlanningPolicy.STRAIGHT_LINES_ONLY)
                    .build()

                goTo?.addOnStartedListener { Log.i("MainActivity", "GoTo action started.") }
                goTo?.removeAllOnStartedListeners()

                goToFuture = goTo?.async()?.run() // Save the future of the GoTo action
                goToFuture?.thenConsume { future ->
                    if (future.isSuccess) {
                        Log.i("MainActivity", "GoTo action finished with success.")
                    } else if (future.hasError()) {
                        Log.e("MainActivity", "GoTo action finished with error.", future.error)
                    }
                }

            } catch (e: Exception) {
                Log.e("MainActivity", "Error during robot movement: ${e.message}", e)
            }
        }
    }

    private fun stopRobot() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                goToFuture?.cancel() // Cancel the current GoTo action
                goToFuture = null // Reset the future
                Log.i("MainActivity", "Movement stopped.")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error during stopping robot: ${e.message}", e)
            }
        }
    }

    // Function to make Pepper speak
    private fun sayText(text: String) {
        if (!::qiContext.isInitialized) {
            Log.e("MainActivity", "QiContext is not initialized.")
            return
        }

        // Build the Say action
        val say = SayBuilder.with(qiContext)
            .withText(text)
            // .withBodyLanguageOption(BodyLanguageOption.DISABLED)
            .build()

        // Run the Say action asynchronously
        say.async().run().thenConsume {
            if (it.isSuccess) {
                Log.i("MainActivity", "Pepper said: $text")
            } else {
                Log.e("MainActivity", "Error saying text: ${it.errorMessage}")
            }
        }
    }

    override fun onRobotFocusGained(qiContext: QiContext) {
        this.qiContext = qiContext // Store the QiContext when it is gained
        holdAbilities()
        startUdpListener()
    }

    private fun startUdpListener() {
        // Cancel any existing job before starting a new one
        job?.cancel()

        job = CoroutineScope(Dispatchers.IO).launch {
            val socket = DatagramSocket(commandPort)
            val buffer = ByteArray(1024)
            try {
                while (isActive) { // Check if the coroutine is still active
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket.receive(packet)
                    val command = String(packet.data, 0, packet.length).trim()
                    handleCommand(command)
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "UDP listener error: ${e.message}", e)
            } finally {
                socket.close()
            }
        }
    }

    private fun holdAbilities() {
        // Build the holder for the abilities
        holder = HolderBuilder.with(qiContext)
            .withAutonomousAbilities(
                // AutonomousAbilitiesType.BACKGROUND_MOVEMENT,
                AutonomousAbilitiesType.BASIC_AWARENESS,
                // AutonomousAbilitiesType.AUTONOMOUS_BLINKING
            )
            .build()

        // Hold the abilities asynchronously
        holder?.async()?.hold()?.andThenConsume {
            Log.i("MainActivity", "Abilities held successfully.")
        } ?: Log.e("MainActivity", "Failed to hold abilities.")
    }

    override fun onRobotFocusLost() {
        // Handle loss of robot focus if needed
    }

    override fun onRobotFocusRefused(reason: String?) {
        // Handle focus refusal if needed
    }
}