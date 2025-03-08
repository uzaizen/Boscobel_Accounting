package com.example.boscobel_accounting


import android.icu.text.SimpleDateFormat
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

data class PaL(
    var documentId: String = "",
    var companycode:String = "",
    var companyname:String = "",
    var date: String = getCurrentYearMonth(), // デフォルト値を現在の年月に設定
    /* データの代入方法
    val newYearMonth = "2025-02" // 変数を作って代入
　　　val pal = PaL(date = newYearMonth)
     */
    var accountitemcode: String ="",
    var accountitemname: String ="",
    var accountclassification: String = "",
    var amount: Int = 0,
    var comment: String = ""
)

fun getCurrentYearMonth(): String {
    val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    return sdf.format(Date()) // 現在の日付をフォーマット
}

// Firestoreとのやり取りを管理するオブジェクト
object PaLManager {

    private val db = FirebaseFirestore.getInstance()
    private val palList = mutableListOf<PaL>()

    // Firestoreから全てのPaLを読み込み、メモリーに保持する
    fun fetchAllPaL(onComplete: (Boolean) -> Unit) {
        db.collection("PaL")  // Firestore上のコレクション名を指定
            .get()
            .addOnSuccessListener { result ->
                palList.clear()
                for (document in result) {
                    val pal = document.toObject(PaL::class.java)
                    palList.add(pal)
                }
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }
    /* ここから修正

     */
    // Firestoreから特定年月のPaLを読み込み、メモリーに保持する
    fun fetchSelectedPaL(
        targetDate: String,
        targetCompanyCode: String,
        onComplete: (Boolean, Exception?) -> Unit)
    {

        db.collection("PaL")  // Firestore上のコレクション名を指定
            .whereEqualTo("date", targetDate)
            .whereEqualTo("companycode", targetCompanyCode)
            .get()
            .addOnSuccessListener { result ->
                palList.clear()
                for (document in result) {
                    val pal = document.toObject(PaL::class.java)
                    palList.add(pal)
                }
                onComplete(true, null)
            }
            .addOnFailureListener {exception ->
                onComplete(false,exception )
            }
    }


    // 取得した全てのProductデータを返す
    fun getAllPaL(): List<PaL> {
        return palList
    }

    fun addPaL(pal: PaL, onComplete: (Boolean) -> Unit) {
        val newDocRef = db.collection("PaL").document() // 新規ドキュメント参照
        pal.documentId = newDocRef.id // 生成されたdocumentIdを保存

        newDocRef.set(pal)
            .addOnSuccessListener {
                palList.add(pal)
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }



    // Firestore上の既存のPaLを更新する
    fun updatePaL(documentId: String, pal: PaL, onComplete: (Boolean) -> Unit) {
        db.collection("PaL").document(documentId)
            .set(pal)
            .addOnSuccessListener {
                val index = palList.indexOfFirst { it.date == pal.date && it.accountitemcode == pal.accountitemcode}
                if (index >= 0) {
                    palList[index] = pal
                }
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    // Firestoreから特定のPaLを削除する
    fun deletePaL(documentId: String, onComplete: (Boolean) -> Unit) {
        db.collection("PaL").document(documentId)
            .delete()
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }
}