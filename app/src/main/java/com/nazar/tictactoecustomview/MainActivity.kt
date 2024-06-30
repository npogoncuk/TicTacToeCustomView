package com.nazar.tictactoecustomview

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nazar.tictactoecustomview.databinding.ActivityMainBinding
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var isPlayerXTurn = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater).also { setContentView(it.root) }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.randomButton.setOnClickListener {
            binding.ticTacToeView.ticTacToeField = TicTacToeField(Random.nextInt(3, 10), Random.nextInt(3, 10)).also { field ->
                for (i in 0 until field.rows) {
                    for (j in 0 until field.columns) {
                        field[i, j] = when(Random.nextInt(3)) {
                            0 -> Cell.PLAYER_X
                            1 -> Cell.PLAYER_O
                            else -> Cell.EMPTY
                        }
                    }
                }
            }
        }

        binding.ticTacToeView.cellActionListener = { row, column, field ->
            val cell = field[row, column]
            if (cell == Cell.EMPTY) {
                field[row, column] = if (isPlayerXTurn) Cell.PLAYER_X else Cell.PLAYER_O
                isPlayerXTurn = !isPlayerXTurn
            }
        }
    }
}