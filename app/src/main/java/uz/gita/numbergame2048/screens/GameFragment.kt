package uz.gita.numbergame2048.screens

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import uz.gita.numbergame2048.AppRepository
import uz.gita.numbergame2048.HomeFragment
import uz.gita.numbergame2048.MyTouchListener
import uz.gita.numbergame2048.R
import uz.gita.numbergame2048.data.BackgroundUtil
import uz.gita.numbergame2048.data.SideEnum
import uz.gita.numbergame2048.databinding.GameFragmentBinding

class GameFragment : Fragment() {

    private var _binding: GameFragmentBinding? = null
    private val binding get() = _binding!!

    private val repository = AppRepository()
    private val cells = ArrayList<TextView>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = GameFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("game_prefs", 0)

        if (repository.hasSavedGame(prefs)) {
            repository.loadFromPrefs(prefs)
        }

        loadCells()
        showMatrix(prefs)

        val touchListener = MyTouchListener(requireContext())
        touchListener.setMoveSideListener { side ->

            val moved = when (side) {
                SideEnum.LEFT  -> repository.moveToLeft()
                SideEnum.RIGHT -> repository.moveToRight()
                SideEnum.UP    -> repository.moveToUp()
                SideEnum.DOWN  -> repository.moveToDown()
            }

            if (moved) {
                repository.addNewElement()
                repository.saveToPrefs(prefs)

                prefs.edit().putBoolean("has_active_game", true).apply()

                showMatrix(prefs)

                val highScore = prefs.getInt("high_score", 0)
                if (repository.score > highScore) {
                    prefs.edit().putInt("high_score", repository.score).apply()
                }

                if (repository.isWin()) {
                    showWinDialog(prefs)
                    return@setMoveSideListener
                }

                if (repository.isGameOver()) {
                    repository.clearSavedGame(prefs)
                    prefs.edit().putBoolean("has_active_game", false).apply()
                    showGameOverDialog(prefs)
                }
            }
        }
        binding.container.setOnTouchListener(touchListener)

        binding.restart.setOnClickListener {
            repository.clearSavedGame(prefs)
            prefs.edit().putBoolean("has_active_game", false).apply()
            repository.resetGame()
            showMatrix(prefs)
        }

        binding.home.setOnClickListener {
            repository.saveToPrefs(prefs)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

        binding.backStep.setOnClickListener {
            if (repository.canUndo()) {
                repository.undo()
                repository.saveToPrefs(prefs)
                showMatrix(prefs)
            }
        }
    }

    private fun loadCells() {
        val lines = listOf(
            binding.linear1,
            binding.linear2,
            binding.linear3,
            binding.linear4
        )
        for (line in lines)
            for (i in 0 until 4)
                cells.add(line.getChildAt(i) as TextView)
    }

    private fun showMatrix(prefs: SharedPreferences) {
        val highScore = prefs.getInt("high_score", 0)

        for (i in 0 until 4) {
            for (j in 0 until 4) {
                val value = repository.matrix[i][j]
                val cell = cells[i * 4 + j]

                val oldText = cell.text.toString()
                val oldValue = if (oldText.isEmpty()) 0 else oldText.toInt()

                cell.text = if (value == 0) "" else value.toString()
                cell.setBackgroundResource(BackgroundUtil.getColorByAmount(value))
                if (value > oldValue && oldValue != 0) {
                    playMergeAnimation(cell)
                }
            }
        }

        binding.scoreCount.text = repository.score.toString()
        binding.highScoreCount.text = maxOf(repository.score, highScore).toString()
    }

    private fun showGameOverDialog(prefs: SharedPreferences) {
        AlertDialog.Builder(requireContext())
            .setTitle("O'yin tugadi! 😢")
            .setMessage("Sizning natijangiz: ${repository.score}")
            .setPositiveButton("Qayta o'ynash") { _, _ ->
                repository.resetGame()
                prefs.edit().putBoolean("has_active_game", false).apply()
                showMatrix(prefs)
            }
            .setNegativeButton("Bosh sahifa") { _, _ ->
                prefs.edit().putBoolean("has_active_game", false).apply()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment())
                    .commit()
            }
            .setCancelable(false)
            .show()
    }

    private fun showWinDialog(prefs: SharedPreferences) {
        AlertDialog.Builder(requireContext())
            .setTitle("🎉 Tabriklaymiz!")
            .setMessage("Siz 2048 ga yetdingiz!\nNatijangiz: ${repository.score}")
            .setPositiveButton("Davom etish") { _, _ ->
            }
            .setNegativeButton("Qayta boshlash") { _, _ ->
                repository.clearSavedGame(prefs)
                repository.resetGame()
                prefs.edit().putBoolean("has_active_game", false).apply()
                showMatrix(prefs)
            }
            .setCancelable(false)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun playMergeAnimation(view: View) {
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.18f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.18f)

        ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY).apply {
            duration = 120
            repeatCount = 1
            repeatMode = ValueAnimator.REVERSE
            start()
        }
    }
}