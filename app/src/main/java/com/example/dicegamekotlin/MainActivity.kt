package com.example.dicegamekotlin

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.dicegamekotlin.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import java.util.function.Consumer

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val diceImages: MutableList<ImageView> = ArrayList()
    private lateinit var diceResIds: IntArray

    companion object {
        const val MAX_CYCLE = 70
        const val MIN_CYCLE = 50
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initDiceImages()

        // init models dices
        diceResIds = intArrayOf(
            R.drawable.de1,
            R.drawable.de2,
            R.drawable.de3,
            R.drawable.de4,
            R.drawable.de5,
            R.drawable.de6
        )

        // Restore state
        if (savedInstanceState != null) {
            diceImages.forEach(Consumer { dice: ImageView ->
                val key = buildDiceStateKey(dice)
                Log.d("MainActivity", "onCreatekey=$key")
                val resId = savedInstanceState.getInt(key)
                if (resId != 0) {
                    Log.d("MainActivity", "onCreatevalue=$resId")
                    dice.setImageResource(resId)
                    dice.tag = resId
                }
            })
        }
        Log.d("MainActivity", "onCreate")
    }

    private fun initDiceImages() {
        val diceGroupLayout = findViewById<LinearLayout>(R.id.diceGroupLayout)
        val numberDice = diceGroupLayout.childCount
        for (i in 0 until numberDice) {
            diceImages.add(diceGroupLayout.getChildAt(i) as ImageView)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        diceImages.forEach(Consumer { dice: ImageView ->
            val resId = dice.tag as Int
            val key = buildDiceStateKey(dice)
            outState.putInt(key, resId)
            Log.d(
                "MainActivity",
                "onSaveInstanceState[key=$key, value=$resId]"
            )
        })
    }

    private fun buildDiceStateKey(dice: ImageView): String {
        return dice.id.toString()
    }

    override fun onStart() {
        super.onStart()
    }

    fun doRotate(view: View?) {

        // BEGIN
        CoroutineScope(Dispatchers.Main).launch {
            refreshRotateButtonToRotating()
            Log.d("App", "End Begin")
        }
        // ROTATING
        val numberDice = diceImages.size
        Log.d("App", "Begin ${numberDice}")
        var jobs = mutableListOf<Job>()
        var values = IntArray(numberDice)
        CoroutineScope(Dispatchers.Main).launch {
//        runBlocking {
//          withContext (Dispatchers.Main){
            diceImages.map {
                launch { values[diceImages.indexOf(it)] = doRotate(it) }
            }.joinAll()
            Log.d("App", "Sum ${values.sum()}")

//            }
//        }

            Log.d("App", "End ${numberDice}")
            // END
//            CoroutineScope(Dispatchers.Main).launch {
                Log.d("App", "Begin Terminate")
                refreshRotateButtonToRotate()
//            }
        }

    }

    private suspend fun doRotate(dice: ImageView) : Int {
        val numberCycle = ThreadLocalRandom.current().nextInt(MAX_CYCLE - MIN_CYCLE + 1) + MIN_CYCLE
//        val numberCycle = MAX_CYCLE
        val random = Random(System.currentTimeMillis())
        var i = 1
        var resultResId : Int = 0
        while (i < numberCycle) {
            resultResId = random.nextInt(6)
            delay((10 * i).toLong())
            refreshDice(dice, resultResId)
            i++
        }

        Log.d("App", "${dice.id} finished")
        return resultResId;
    }

    private fun refreshRotateButtonToRotating() {
        binding.rotateButton.setText(R.string.rotating)
        binding.rotateButton.isEnabled = false
    }

    private fun refreshRotateButtonToRotate() {
        binding.rotateButton.setText(R.string.rotate)
        binding.rotateButton.isEnabled = true
    }

    private fun refreshDice(dice: ImageView, resultResId: Int) {
        dice.setImageResource(diceResIds[resultResId])
        dice.tag = diceResIds[resultResId]
    }

}