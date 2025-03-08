package com.example.boscobel_accounting

import android.app.DatePickerDialog
import android.app.AlertDialog
import android.icu.text.NumberFormat
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.*


class PaLFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PaLAdapter
    private lateinit var enter_button : Button
    private lateinit var edit_accountitem_code : EditText
    private lateinit var edit_accountitem_name :EditText
    private lateinit var edit_accountitem_classification : EditText
    private lateinit var edit_accountitem_amount : EditText
    private lateinit var edit_comment : EditText
    private var selectedPaL: PaL? = null

    private lateinit var tvSelectedDate: TextView
    private var selectedDate: Date = Date() // 初期値として現在の日付
    private lateinit var selectedCompany : CompanyData

    private var companymaster:List<CompanyData> = emptyList()
    private var companyNames:List<String> = emptyList()

    private var accountitemmaster:List<AccountItem> = emptyList()
    private var accountitemNames : List<String> = emptyList()
    private var accountitemCode : List<String> = emptyList()
    private var accountitemClassification : List<String> = emptyList()
    private lateinit var selectedAccountItem : AccountItem
    private lateinit var selectedAccountItemName: String
    private lateinit var selectedAccountItemCode: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pal, container, false)

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val list:MutableList<PaL> = mutableListOf()

        adapter = PaLAdapter(list).apply {
            setOnItemClickListener { pal ->
                onPaLSelected(pal) // 選択された商品を編集フィールドに反映
            }
        }

        recyclerView.adapter = adapter

        setHasOptionsMenu(true)


        enter_button = view.findViewById<Button>(R.id.button_enter)
        edit_accountitem_code = view.findViewById<EditText>(R.id.edit_accountitem_code)
        edit_accountitem_name = view.findViewById<EditText>(R.id.edit_accounititem_name)
        edit_accountitem_classification = view.findViewById<EditText>(R.id.edit_accountitem_classification)
        edit_accountitem_amount = view.findViewById<EditText>(R.id.edit_accountitem_amount)
        edit_comment = view.findViewById<EditText>(R.id.edit_comment)

        enter_button.setOnClickListener {
            confirmDelete() // 確認して削除
        }
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // レイアウト内のビューを取得
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate)
        val btnSelectDate = view.findViewById<Button>(R.id.btnSelectDate)
        val btnSaveData = view.findViewById<Button>(R.id.btnSaveData)

        // 日付選択ボタン
        btnSelectDate.setOnClickListener {
            showDatePicker()
        }

        // Firestoreから該当日付＆チャネル・データ読み出しボタン
        btnSaveData.setOnClickListener {
            readSelectedPaLFromFirestore()
        }

        fetchCompany()
        fetchAccountItem()

        val showDialogButton = view.findViewById<Button>(R.id.showDialogButton)
        val tvSelectedcompany = view.findViewById<TextView>(R.id.tvSelectedCompany)

        // ボタンのクリックイベントでDialogを表示
        showDialogButton.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("会社選択してください")
            builder.setItems(companyNames.toTypedArray()) { _, which ->
                // 選択されたチャネル名
                val selectedName = companyNames[which]

                // 選択されたnameに対応するChannelを取得
                selectedCompany = companymaster.first { it.name == selectedName }

                tvSelectedcompany.text = "選択した会社: ${selectedCompany.code}:${selectedCompany.name}"

                // 結果を表示
                Toast.makeText(
                    context,
                    "選択された会社: ${selectedCompany.name}\nコード: ${selectedCompany.code}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            builder.create().show()
        }


        edit_accountitem_code.setOnClickListener{
            fetchAccountItem()
            val builder = AlertDialog.Builder(context)
            builder.setTitle("勘定科目を選択してください")
            builder.setItems(accountitemNames.toTypedArray()) { _, which ->
                // 選択された商品名
                val selectedName = accountitemNames[which]

                // 選択されたnameに対応するAccountItemを取得
                selectedAccountItem = accountitemmaster.first { it.name == selectedName }

                edit_accountitem_code.setText(selectedAccountItem.code)
                edit_accountitem_name.setText(selectedAccountItem.name)
                edit_accountitem_classification.setText(selectedAccountItem.classification)

                // 結果を表示
                Toast.makeText(
                    context,
                    "選択された科目: ${selectedAccountItem.name}\nコード: ${selectedAccountItem.code}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            builder.create().show()
        }

    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val calendarSelected = Calendar.getInstance()
            calendarSelected.set(selectedYear, selectedMonth, selectedDay)
            selectedDate = calendarSelected.time

            // 日付をフォーマットしてTextViewに表示
            val dateFormat = SimpleDateFormat("yyyy/MM", Locale.getDefault())
            tvSelectedDate.text = "選択した年月: ${dateFormat.format(selectedDate)}"
        }, year, month,day).show()
    }


    private fun readSelectedPaLFromFirestore() {
        fetchPaL()

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_input ->
            { Toast.makeText(requireContext(), "Input selected in frag", Toast.LENGTH_SHORT).show()
                enter_button.text = "入力"
                toolbar.title="損益データ入力"
                setAddButtonForInput()
            }
            R.id.action_modify ->
            { Toast.makeText(requireContext(), "Modify selected in frag", Toast.LENGTH_SHORT).show()
                enter_button.text = "修正"
                toolbar.title="損益データ修正"
                setAddButtonForModify()
            }
            R.id.action_delete ->
            { Toast.makeText(requireContext(), "Delete selected in frag", Toast.LENGTH_SHORT).show()
                enter_button.text = "削除"
                toolbar.title="損益データ削除"
                setAddButtonForDelete()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun fetchCompany() {
        lifecycleScope.launch {
            val success = CompanyDataManager.fetchAllCompanyData()
            if (success) {
                val company = CompanyDataManager.getAllCompanyData()
                val sortedList = company.sortedBy { it.code }
                companymaster = sortedList
                companyNames = companymaster.map { it.name }
            } else {
                Toast.makeText(requireContext(), "Failed to fetch company from Firestore", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchAccountItem() {
        lifecycleScope.launch {
            val success = AccountItemManager.fetchAllAccountItem()
            if (success) {
                val accountitem = AccountItemManager.getAllAccountItem()
                val sortedList = accountitem.sortedBy { it.code }
                accountitemmaster = sortedList
                accountitemNames = accountitemmaster.map { it.name }
                accountitemCode = accountitemmaster.map{it.code}
                accountitemClassification = accountitemmaster.map{it.classification}
            } else {
                Toast.makeText(requireContext(), "Failed to fetch account item from Firestore", Toast.LENGTH_SHORT).show()
            }
        }
    }

    

    private fun setAddButtonForInput() {
        val dateFormat = SimpleDateFormat("yyyy/MM", Locale.getDefault())
        enter_button.setOnClickListener {
            val newPaL = PaL(
                companycode = selectedCompany.code,
                companyname = selectedCompany.name,
                date = dateFormat.format(selectedDate),
                accountitemcode = selectedAccountItem.code,  //????
                accountitemname = selectedAccountItem.name,
                accountclassification = selectedAccountItem.classification,
                amount = edit_accountitem_amount.text?.toString()?.toIntOrNull() ?: 0,
                comment = edit_comment.text.toString()
            )
            PaLManager.addPaL(newPaL) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Successfully added a PandL data", Toast.LENGTH_SHORT).show()
                    fetchPaL() // データを再取得
                    inputfieldclear() // 入力フィールドをクリア

                } else {
                    Toast.makeText(requireContext(), "Failed to add a PandL data", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setAddButtonForModify() {
        val dateFormat = SimpleDateFormat("yyyy/MM", Locale.getDefault())
        enter_button.setOnClickListener {
            val updatedPaL = selectedPaL?.copy(
                companycode = selectedCompany.code,
                companyname = selectedCompany.name,
                date = dateFormat.format(selectedDate),
                accountitemcode = edit_accountitem_code.text.toString(),
                accountitemname = edit_accountitem_name.text.toString(),
                accountclassification = edit_accountitem_classification.text.toString(),
                amount = edit_accountitem_amount.text?.toString()?.toIntOrNull() ?: 0,
                comment = edit_comment.text.toString()
            )
            if (updatedPaL != null && selectedPaL != null) {
                val documentId = selectedPaL!!.documentId
                if (documentId.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "Document ID is missing for the selected PandL data", Toast.LENGTH_SHORT).show()
//                    return
                }

                PaLManager.updatePaL(documentId, updatedPaL) { success ->
                    if (success) {
                        Toast.makeText(requireContext(), "Successfully updated the PandL data", Toast.LENGTH_SHORT).show()
                        fetchPaL()
                        inputfieldclear()
                        selectedPaL = null
                    } else {
                        Toast.makeText(requireContext(), "Failed to update the PandL", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "No PandL selected for modification", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setAddButtonForDelete() {
        enter_button.setOnClickListener {
            confirmDelete()
        }
    }

    private fun fetchPaL() {
        val dateFormat = SimpleDateFormat("yyyy/MM", Locale.getDefault())

        PaLManager.fetchSelectedPaL(dateFormat.format(selectedDate), selectedCompany.code) { success, error ->
            if (success) {
                val pal = PaLManager.getAllPaL()
                val sortedList = pal.sortedBy{it.accountitemcode}
                adapter.updateData(sortedList)  // RecyclerViewのデータを更新
            } else {
                // エラーハンドリング
                Toast.makeText(requireContext(),"Failed to fetch PandL data from Firestore", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmDelete() {

        if (selectedPaL != null) {

            AlertDialog.Builder(requireContext())
                .setTitle("削除確認")
                .setMessage("選択した損益データを削除してもよろしいですか？")
                .setPositiveButton("削除") { _, _ ->
                    deletePaL()
                }
                .setNegativeButton("キャンセル", null)
                .show()
        } else {
            Toast.makeText(requireContext(), "削除する損益データが選択されていません", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deletePaL() {
        val documentId = selectedPaL?.documentId

        if (documentId != null) {
            PaLManager.deletePaL(documentId) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Successfully deleted the PandL data", Toast.LENGTH_SHORT).show()
                    fetchPaL()
                    inputfieldclear()
                    selectedPaL = null
                } else {
                    Toast.makeText(requireContext(), "Failed to delete the PandL data", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "削除する損益データが選択されていません", Toast.LENGTH_SHORT).show()
        }
    }



    private fun onPaLSelected(pal: PaL) { // New feature: Populate fields with selected channel data
        selectedPaL = pal
        edit_accountitem_code.setText(pal.accountitemcode)
        edit_accountitem_name.setText(pal.accountitemname)
        edit_accountitem_classification.setText(pal.accountclassification)
        edit_accountitem_amount.setText(pal.amount.toString())
        edit_comment.setText(pal.comment)
    }

    private fun inputfieldclear(){
        edit_accountitem_code.setText("")
        edit_accountitem_name.setText("")
        edit_accountitem_classification.setText("")
        edit_accountitem_amount.setText("")
        edit_comment.setText("")
    }
}