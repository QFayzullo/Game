package uz.gita.numbergame2048.data

import uz.gita.numbergame2048.R

object BackgroundUtil {
    val map = hashMapOf(
        0 to R.drawable.bg_0,
        2 to R.drawable.bg_2,
        4 to R.drawable.bg_4,
        8 to R.drawable.bg_8,
        16 to R.drawable.bg_16,
        32 to R.drawable.bg_32,
        64 to R.drawable.bg_64,
        128 to R.drawable.bg_128,
        256 to R.drawable.bg_256,
        512 to R.drawable.bg_512,
        1024 to R.drawable.bg_1024,
        2048 to R.drawable.bg_2048,
    )

    fun getColorByAmount(x: Int): Int =
        map.getOrDefault(x, R.drawable.bg_2048)
}