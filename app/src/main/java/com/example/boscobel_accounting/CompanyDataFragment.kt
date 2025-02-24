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

class CompanyDataFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CompanyDataAdapter
    private lateinit var add_button : Button
    private lateinit var edit_code : EditText
    private lateinit var edit_name :EditText
    private lateinit var edit_abbreviation : EditText
    private lateinit var edit_comment : EditText
    private var selectedCompanyData: CompanyData? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_company_data, container, false)

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val list:MutableList<CompanyData> = mutableListOf()

        adapter = CompanyDataAdapter(list).apply {
            setOnItemClickListener { companydata ->
                onCompanyDataSelected(companydata) // 選択された商品を編集フィールドに反映
            }
        }

        recyclerView.adapter = adapter
        fetchCompanyData() // Firestoreからデータを取得して表示

        setHasOptionsMenu(true)
        add_button = view.findViewById<Button>(R.id.button_add) // レイアウトにあるボタンID
        edit_code = view.findViewById<EditText>(R.id.edit_code)
        edit_name = view.findViewById<EditText>(R.id.edit_name)
        edit_abbreviation = view.findViewById<EditText>(R.id.edit_abbreviation)
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
                toolbar.title="会社情報入力"
                setAddButtonForInput()
            }
            R.id.action_modify ->
            { Toast.makeText(requireContext(), "Modify selected in frag", Toast.LENGTH_SHORT).show()
                add_button.text = "修正"
                toolbar.title="会社情報修正"
                setAddButtonForModify()
            }
            R.id.action_delete ->
            { Toast.makeText(requireContext(), "Delete selected in frag", Toast.LENGTH_SHORT).show()
                add_button.text = "削除"
                toolbar.title="会社情報削除"
                setAddButtonForDelete()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setAddButtonForInput() {
        add_button.setOnClickListener {
            val newCompanyData = CompanyData(
                code = edit_code.text.toString(),
                name = edit_name.text.toString(),
                abbreviation = edit_abbreviation.text.toString(),
                comment = edit_comment.text.toString()
            )
            CompanyDataManager.addCompanyData(newCompanyData) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Successfully added a company data", Toast.LENGTH_SHORT).show()
                    fetchCompanyData() // データを再取得
                    inputfieldclear() // 入力フィールドをクリア
                } else {
                    Toast.makeText(requireContext(), "Failed to add a company data", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setAddButtonForModify() {
        add_button.setOnClickListener {
            val updatedCompanyData = selectedCompanyData?.copy(
                code = edit_code.text.toString(),
                name = edit_name.text.toString(),
                abbreviation = edit_abbreviation.text.toString(),
                comment = edit_comment.text.toString()
            )
            if (updatedCompanyData != null && selectedCompanyData != null) {
                val documentId = selectedCompanyData!!.documentId
                if (documentId.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "Document ID is missing for the company data", Toast.LENGTH_SHORT).show()
//                    return
                }

                CompanyDataManager.updateCompanyData(documentId, updatedCompanyData) { success ->
                    if (success) {
                        Toast.makeText(requireContext(), "Successfully updated the company data", Toast.LENGTH_SHORT).show()
                        fetchCompanyData()
                        inputfieldclear()
                        selectedCompanyData = null
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

    private fun fetchCompanyData() {
        lifecycleScope.launch {
            val success = CompanyDataManager.fetchAllCompanyData()
            if (success) {
                val companydata = CompanyDataManager.getAllCompanyData()
                val sortedList = companydata.sortedBy { it.code }
                adapter.updateData(sortedList)  // RecyclerViewのデータを更新
                // 次の処理
            } else {
                Toast.makeText(requireContext(), "Failed to fetch company data from Firestore", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun confirmDelete() {

        if (selectedCompanyData!= null) {

            AlertDialog.Builder(requireContext())
                .setTitle("削除確認")
                .setMessage("選択した会社情報を削除してもよろしいですか？")
                .setPositiveButton("削除") { _, _ ->
                    deleteCompanyData()
                }
                .setNegativeButton("キャンセル", null)
                .show()
        } else {
            Toast.makeText(requireContext(), "削除する会社情報が選択されていません", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteCompanyData() {
        val documentId = selectedCompanyData?.documentId
        Log.d("DeleteDebug", "Deleting document with ID: $documentId") // デバッグログ

        if (documentId != null) {
            CompanyDataManager.deleteCompanyData(documentId) { success ->
                if (success) {
                    Log.d("DeleteDebug", "Successfully deleted document with ID: $documentId") // 成功ログ
                    Toast.makeText(requireContext(), "Successfully deleted the company data", Toast.LENGTH_SHORT).show()
                    fetchCompanyData()
                    inputfieldclear()
                    selectedCompanyData = null
                } else {
                    Log.e("DeleteDebug", "Failed to delete document with ID: $documentId") // エラーログ
                    Toast.makeText(requireContext(), "Failed to delete the company data", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "削除する会社情報が選択されていません", Toast.LENGTH_SHORT).show()
        }
    }



    private fun onCompanyDataSelected(companyData:CompanyData) { // New feature: Populate fields with selected channel data
        selectedCompanyData = companyData
        edit_code.setText(companyData.code)
        edit_name.setText(companyData.name)
        edit_abbreviation.setText(companyData.abbreviation)
        edit_comment.setText(companyData.comment)
    }

    private fun inputfieldclear(){
        edit_code.setText("")
        edit_name.setText("")
        edit_abbreviation.setText("")
        edit_comment.setText("")
    }

}


