package seifemadhamdy.dicer.util

import seifemadhamdy.dicer.R

class Dice {
    enum class Faces(val angleX: Float, val angleY: Float) {
        ONE(0f, 180f),
        TWO(-90f, -180f),
        THREE(0f, -270f),
        FOUR(0f, -90f),
        FIVE(90f, 180f),
        SIX(180f, 180f)
    }

    data class Textures(
        val faceOneTextureResId: Int = R.drawable.dice_face_one,
        val faceTwoTextureResId: Int = R.drawable.dice_face_two,
        val faceThreeTextureResId: Int = R.drawable.dice_face_three,
        val faceFourTextureResId: Int = R.drawable.dice_face_four,
        val faceFiveTextureResId: Int = R.drawable.dice_face_five,
        val faceSixTextureResId: Int = R.drawable.dice_face_six
    )

    companion object {
        var face: Faces = Faces.ONE

        fun roll(): Faces {
            return when ((1..6).random()) {
                2 -> Faces.TWO
                3 -> Faces.THREE
                4 -> Faces.FOUR
                5 -> Faces.FIVE
                6 -> Faces.SIX
                else -> Faces.ONE
            }.also {
                face = it
            }
        }
    }
}