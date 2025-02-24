package com.example.boscobel_accounting

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class CompanyDataAdapter(private var companydataList: List<CompanyData>) : RecyclerView.Adapter<CompanyDataAdapter.CompanyDataViewHolder>() {

    private var onItemClickListener: ((CompanyData) -> Unit)? = null

    class CompanyDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val codeTextView: TextView = itemView.findViewById(R.id.text_code)
        val nameTextView: TextView = itemView.findViewById(R.id.text_name)
        val abbreviationTextView: TextView = itemView.findViewById(R.id.text_abbreviation)
        val commentTextView: TextView = itemView.findViewById(R.id.text_comment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompanyDataViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_companydata, parent, false)
        return CompanyDataViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompanyDataViewHolder, position: Int) {
        val companydata = companydataList[position]
        holder.codeTextView.text = companydata.code
        holder.nameTextView.text = companydata.name
        holder.abbreviationTextView.text = companydata.abbreviation
        holder.commentTextView.text = companydata.comment

        // アイテムクリックリスナーを設定
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(companydata)
        }

    }

    fun updateData(newCompanyDataList: List<CompanyData>) {
        companydataList = newCompanyDataList
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: (CompanyData) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return companydataList.size
    }

}
