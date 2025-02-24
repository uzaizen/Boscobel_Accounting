package com.example.boscobel_accounting

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// CompanyData(会社情報)を定義するデータクラス
data class CompanyData(
    var documentId: String = "",
    var code: String = "",
    var name: String = "",
    var abbreviation: String = "",
    var comment: String = ""
)

object CompanyDataManager {

    private val db = FirebaseFirestore.getInstance()
    private val companydataList = mutableListOf<CompanyData>()

    // Firestoreから全てのCompanyDataを読み込み、メモリーに保持する
    suspend fun fetchAllCompanyData():Boolean {
        return try{
            val result = db.collection("CompanyData").get().await()
            companydataList.clear()
            for (document in result) {
                val companydata = document.toObject(CompanyData::class.java)
                companydataList.add(companydata)
            }
            true
        } catch (e:Exception){
            false
        }
    }

    // 取得した全てのCompanyDataデータを返す
    fun getAllCompanyData(): List<CompanyData> {
        return companydataList
    }


    fun addCompanyData(companydata:CompanyData, onComplete: (Boolean) -> Unit) {
        val newDocRef = db.collection("CompanyData").document() // 新規ドキュメント参照
        companydata.documentId = newDocRef.id // 生成されたdocumentIdを保存

        newDocRef.set(companydata)
            .addOnSuccessListener {
                companydataList.add(companydata)
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }



    // Firestore上の既存のCompanyDataを更新する
    fun updateCompanyData(documentId: String, companydata: CompanyData, onComplete: (Boolean) -> Unit) {
        db.collection("CompanyData").document(documentId)
            .set(companydata)
            .addOnSuccessListener {
                val index = companydataList.indexOfFirst { it.name == companydata.name }
                if (index >= 0) {
                    companydataList[index] = companydata
                }
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    // Firestoreから特定のCompanyDataを削除する
    fun deleteCompanyData(documentId: String, onComplete: (Boolean) -> Unit) {
        db.collection("CompanyData").document(documentId)
            .delete()
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }
}