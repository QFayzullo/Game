package uz.gita.numbergame2048

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import uz.gita.numbergame2048.databinding.HomeFragmentBinding
import uz.gita.numbergame2048.screens.GameFragment
import uz.gita.numbergame2048.screens.InfoFragment

class HomeFragment : Fragment() {

    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("game_prefs", 0)

        binding.tvNewGame.setOnClickListener {
            prefs.edit().putBoolean("has_active_game", false).apply()
            AppRepository().clearSavedGame(prefs)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, GameFragment())
                .addToBackStack(null)
                .commit()
        }
        binding.tvContinue.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, GameFragment())
                .addToBackStack(null)
                .commit()
        }






        binding.info.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, InfoFragment())
                .addToBackStack(null)
                .commit()
        }
        binding.share.setOnClickListener {
            val highScore = prefs.getInt("high_score", 0)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "2048 O'yini")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "2048 o'yinini sinab ko'ring! 🎮\n" +
                            "Mening eng yuqori natijam: $highScore\n" +
                            "Raqamlarni birlashtiring va 2048 ga yeting!"
                )
            }
            startActivity(Intent.createChooser(shareIntent, "Ulashish..."))
        }
    }
    override fun onResume() {
        super.onResume()
        val prefs = requireContext().getSharedPreferences("game_prefs", 0)
        val hasGame = prefs.getBoolean("has_active_game", false)
        binding.tvContinue.visibility = if (hasGame) View.VISIBLE else View.INVISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}