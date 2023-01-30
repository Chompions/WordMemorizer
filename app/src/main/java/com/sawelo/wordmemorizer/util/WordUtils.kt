package com.sawelo.wordmemorizer.util

import android.content.Context
import android.widget.Toast
import com.sawelo.wordmemorizer.data.data_class.Category
import com.sawelo.wordmemorizer.data.data_class.Word
import com.sawelo.wordmemorizer.data.data_class.WordWithCategories

object WordUtils {
    fun Context.showToast(text: String?) {
        Toast
            .makeText(this, text, Toast.LENGTH_SHORT)
            .show()
    }

    fun Category.isAll(): Boolean = this.categoryName == defaultCategoryList.first()

    val defaultCategoryList = listOf("All", "Important", "Names")
    val defaultWordList = listOf(
        WordWithCategories(
            Word(
                wordText = "お早うございます",
                furiganaText = "おはようございます",
                definitionText = "good morning",
                createdTimeMillis = System.currentTimeMillis(),
            ),
            listOf(Category(
                2, "Important"
            ))
        )
    )

    val hiraganaTable = listOf(
        'あ', 'い', 'う', 'え', 'お',
        'か', 'き', 'く', 'け', 'こ',
        'さ', 'し', 'す', 'せ', 'そ',
        'た', 'ち', 'つ', 'て', 'と',
        'な', 'に', 'ぬ', 'ね', 'の',
        'は', 'ひ', 'ふ', 'へ', 'ほ',
        'ま', 'み', 'む', 'め', 'も',
        'や', 'ゆ', 'よ',
        'ら', 'り', 'る', 'れ', 'ろ',
        'わ', 'を', 'ん',
        'が', 'ぎ', 'ぐ', 'げ', 'ご',
        'ざ', 'じ', 'ず', 'ぜ', 'ぞ',
        'だ', 'ぢ', 'づ', 'で', 'ど',
        'ば', 'び', 'ぶ', 'べ', 'ぼ',
        'ぱ', 'ぴ', 'ぷ', 'ぺ', 'ぽ',
        'ゃ', 'ゅ', 'ょ',
    )

    val katakanaTable = listOf(
        'ア', 'イ', 'ウ', 'エ', 'オ',
        'カ', 'キ', 'ク', 'ケ', 'コ',
        'サ', 'シ', 'ス', 'セ', 'ソ',
        'タ', 'チ', 'ツ', 'テ', 'ト',
        'ナ', 'ニ', 'ヌ', 'ネ', 'ノ',
        'ハ', 'ヒ', 'フ', 'ヘ', 'ホ',
        'マ', 'ミ', 'ム', 'メ', 'モ',
        'ヤ', 'ユ', 'ヨ',
        'ラ', 'リ', 'ル', 'レ', 'ロ',
        'ワ', 'ヲ', 'ン',
        'ガ', 'ギ', 'グ', 'ゲ', 'ゴ',
        'ザ', 'ジ', 'ズ', 'ゼ', 'ゾ',
        'ダ', 'ヂ', 'ヅ', 'デ', 'ド',
        'バ', 'ビ', 'ブ', 'ベ', 'ボ',
        'パ', 'ピ', 'プ', 'ペ', 'ポ',
        'ャ', 'ュ', 'ョ',
    )
}