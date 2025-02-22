package com.example.boscobel_accounting


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class AccountingItemAdapter(private var accountingitemList: List<AccountItem>) : RecyclerView.Adapter<AccountingItemAdapter.AccountingItemViewHolder>() {

    private var onItemClickListener: ((AccountItem) -> Unit)? = null

    class AccountingItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val codeTextView: TextView = itemView.findViewById(R.id.text_code)
        val nameTextView: TextView = itemView.findViewById(R.id.text_name)
        val classificationTextView: TextView = itemView.findViewById(R.id.text_classification)
        val commentTextView: TextView = itemView.findViewById(R.id.text_comment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountingItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_accountingitem, parent, false)
        return AccountingItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: AccountingItemViewHolder, position: Int) {
        val accountingitem = accountingitemList[position]
        holder.codeTextView.text = accountingitem.code
        holder.nameTextView.text = accountingitem.name
        holder.classificationTextView.text = accountingitem.classification
        holder.commentTextView.text = accountingitem.comment

        // アイテムクリックリスナーを設定
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(accountingitem)
        }

    }

    fun updateData(newAccountingItemList: List<AccountItem>) {
        accountingitemList = newAccountingItemList
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: (AccountItem) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return accountingitemList.size
    }

}



