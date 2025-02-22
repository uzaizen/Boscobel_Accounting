package com.example.boscobel_accounting
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class AccountItemFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AccountingItemAdapter
    private lateinit var add_button : Button
    private lateinit var edit_code : EditText
    private lateinit var edit_name :EditText
    private lateinit var edit_classification: EditText
    private lateinit var edit_comment : EditText
    private var selectedAccountingItem: AccountItem? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account_item, container, false)

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val list:MutableList<AccountItem> = mutableListOf()

        adapter = AccountingItemAdapter(list).apply {
            setOnItemClickListener { accountingitem ->
                onAccountingItemSelected(accountingitem) // 選択された商品を編集フィールドに反映
            }
        }

        recyclerView.adapter = adapter
        fetchAccountingItem() // Firestoreからデータを取得して表示

        setHasOptionsMenu(true)
        add_button = view.findViewById<Button>(R.id.button_add) // レイアウトにあるボタンID
        edit_code = view.findViewById<EditText>(R.id.edit_code)
        edit_name = view.findViewById<EditText>(R.id.edit_name)
        edit_classification = view.findViewById<EditText>(R.id.edit_classification)
        edit_comment = view.findViewById<EditText>(R.id.edit_comment)

        add_button.setOnClickListener {
            confirmDelete() // 確認して削除
        }
        return view

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_input ->
            { Toast.makeText(requireContext(), "Input selected in frag", Toast.LENGTH_SHORT).show()
                add_button.text = "入力"
                toolbar.title="勘定科目入力"
                setAddButtonForInput()
            }
            R.id.action_modify ->
            { Toast.makeText(requireContext(), "Modify selected in frag", Toast.LENGTH_SHORT).show()
                add_button.text = "修正"
                toolbar.title="勘定科目修正"
                setAddButtonForModify()
            }
            R.id.action_delete ->
            { Toast.makeText(requireContext(), "Delete selected in frag", Toast.LENGTH_SHORT).show()
                add_button.text = "削除"
                toolbar.title="勘定科目削除"
                setAddButtonForDelete()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setAddButtonForInput() {
        add_button.setOnClickListener {
            val newAccountItem = AccountItem(
                code = edit_code.text.toString(),
                name = edit_name.text.toString(),
                classification = edit_classification.text.toString(),
                comment = edit_comment.text.toString()
            )
            AccountItemManager.addAccountItem(newAccountItem) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Successfully added an accounting item", Toast.LENGTH_SHORT).show()
                    fetchAccountingItem() // データを再取得
                    inputfieldclear() // 入力フィールドをクリア
                } else {
                    Toast.makeText(requireContext(), "Failed to add an accounting item", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setAddButtonForModify() {
        add_button.setOnClickListener {
            val updatedAccountingItem = selectedAccountingItem?.copy(
                code = edit_code.text.toString(),
                name = edit_name.text.toString(),
                classification = edit_classification.text.toString(),
                comment = edit_comment.text.toString()
            )
            if (updatedAccountingItem != null && selectedAccountingItem != null) {
                val documentId = selectedAccountingItem!!.documentId
                if (documentId.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "Document ID is missing for the selected channel", Toast.LENGTH_SHORT).show()
//                    return
                }

                AccountItemManager.updateAccountItem(documentId, updatedAccountingItem) { success ->
                    if (success) {
                        Toast.makeText(requireContext(), "Successfully updated the channel", Toast.LENGTH_SHORT).show()
                        fetchAccountingItem()
                        inputfieldclear()
                        selectedAccountingItem = null
                    } else {
                        Toast.makeText(requireContext(), "Failed to update the channel", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "No channel selected for modification", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setAddButtonForDelete() {
        add_button.setOnClickListener {
            confirmDelete()
        }
    }

    private fun fetchAccountingItem() {
        lifecycleScope.launch {
            val success = AccountItemManager.fetchAllAccountItem()
            if (success) {
                val accountingitem = AccountItemManager.getAllAccountItem()
                val sortedList = accountingitem.sortedBy { it.code }
                adapter.updateData(sortedList)  // RecyclerViewのデータを更新
                // 次の処理
            } else {
                Toast.makeText(requireContext(), "Failed to fetch accounting item from Firestore", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun confirmDelete() {

        if (selectedAccountingItem!= null) {

            AlertDialog.Builder(requireContext())
                .setTitle("削除確認")
                .setMessage("選択した勘定科目を削除してもよろしいですか？")
                .setPositiveButton("削除") { _, _ ->
                    deleteAccountingItem()
                }
                .setNegativeButton("キャンセル", null)
                .show()
        } else {
            Toast.makeText(requireContext(), "削除する勘定科目が選択されていません", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteAccountingItem() {
        val documentId = selectedAccountingItem?.documentId
        Log.d("DeleteDebug", "Deleting document with ID: $documentId") // デバッグログ

        if (documentId != null) {
            AccountItemManager.deleteAccountItem(documentId) { success ->
                if (success) {
                    Log.d("DeleteDebug", "Successfully deleted document with ID: $documentId") // 成功ログ
                    Toast.makeText(requireContext(), "Successfully deleted the channel", Toast.LENGTH_SHORT).show()
                    fetchAccountingItem()
                    inputfieldclear()
                    selectedAccountingItem = null
                } else {
                    Log.e("DeleteDebug", "Failed to delete document with ID: $documentId") // エラーログ
                    Toast.makeText(requireContext(), "Failed to delete the accounting item", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "削除する勘定科目が選択されていません", Toast.LENGTH_SHORT).show()
        }
    }



    private fun onAccountingItemSelected(accountItem: AccountItem) { // New feature: Populate fields with selected channel data
        selectedAccountingItem = accountItem
        edit_code.setText(accountItem.code)
        edit_name.setText(accountItem.name)
        edit_classification.setText(accountItem.classification)
        edit_comment.setText(accountItem.comment)
    }

    private fun inputfieldclear(){
        edit_code.setText("")
        edit_name.setText("")
        edit_classification.setText("")
        edit_comment.setText("")
    }

    /*
    private fun getdatafromdisp(){
        inputfieldclear()
        add_button.setOnClickListener {
            val newChannel = Channel(
                code = edit_code.text.toString(),
                name = edit_name.text.toString(),
                comment = edit_comment.text.toString()
            )

            ChannelManager.addChannel(newChannel) { success ->
                if (success) {
                    Toast.makeText(
                        requireContext(),
                        "Successfully add a channel",
                        Toast.LENGTH_SHORT
                    ).show()
                    fetchChannel()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to add a channel",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }
    }

    private fun modifydata() {
        add_button.setOnClickListener {
            if (selectedChannel == null) {
                Toast.makeText(requireContext(), "No channel selected for modification", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val documentId = selectedChannel!!.documentId
            if (documentId.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Document ID is missing for the selected channel", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedChannel = selectedChannel?.copy(
                code = edit_code.text.toString(),
                name = edit_name.text.toString(),
                comment = edit_comment.text.toString()
            )

            ChannelManager.updateChannel(documentId, updatedChannel!!) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Successfully updated the channel", Toast.LENGTH_SHORT).show()
                    fetchChannel()
                    inputfieldclear()
                    selectedChannel = null
                } else {
                    Toast.makeText(requireContext(), "Failed to update the channel", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

     */
}


