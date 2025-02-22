package com.example.boscobel_accounting

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// AccountItem(勘定科目)を定義するデータクラス
data class AccountItem(
    var documentId: String = "",
    var code: String = "",
    var name: String = "",
    var classification : String = "",
    var comment: String = ""
)

object AccountItemManager {

    private val db = FirebaseFirestore.getInstance()
    private val accountitemList = mutableListOf<AccountItem>()

    // Firestoreから全てのAccountItemを読み込み、メモリーに保持する
    suspend fun fetchAllAccountItem():Boolean {
        return try{
            val result = db.collection("AccountItem").get().await()
            accountitemList.clear()
            for (document in result) {
                val accountitem = document.toObject(AccountItem::class.java)
                accountitemList.add(accountitem)
            }
            true
        } catch (e:Exception){
            false
        }
    }

    // 取得した全てのAccountItemデータを返す
    fun getAllAccountItem(): List<AccountItem> {
        return accountitemList
    }


    fun addAccountItem(accountingitem:AccountItem, onComplete: (Boolean) -> Unit) {
        val newDocRef = db.collection("AccountItem").document() // 新規ドキュメント参照
        accountingitem.documentId = newDocRef.id // 生成されたdocumentIdを保存

        newDocRef.set(accountingitem)
            .addOnSuccessListener {
                accountitemList.add(accountingitem)
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }



    // Firestore上の既存のAccountItemを更新する
    fun updateAccountItem(documentId: String, accountitem: AccountItem, onComplete: (Boolean) -> Unit) {
        db.collection("AccountItem").document(documentId)
            .set(accountitem)
            .addOnSuccessListener {
                val index = accountitemList.indexOfFirst { it.name == accountitem.name }
                if (index >= 0) {
                    accountitemList[index] = accountitem
                }
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    // Firestoreから特定のAccountItemを削除する
    fun deleteAccountItem(documentId: String, onComplete: (Boolean) -> Unit) {
        db.collection("AccountItem").document(documentId)
            .delete()
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }
}