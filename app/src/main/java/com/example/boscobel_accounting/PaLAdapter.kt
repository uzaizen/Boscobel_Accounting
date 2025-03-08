package com.example.boscobel_accounting

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PaLAdapter(private var palList: List<PaL>) : RecyclerView.Adapter<PaLAdapter.RevenueViewHolder>() {

    private var onItemClickListener: ((PaL) -> Unit)? = null

    class RevenueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val accountitemCode: TextView = itemView.findViewById(R.id.account_item_code)
        val accountitemName: TextView = itemView.findViewById(R.id.account_item_name)
        val accountClassification: TextView = itemView.findViewById(R.id.account_classification)
        val palAmount: TextView = itemView.findViewById(R.id.pal_amount)
        val palComment: TextView = itemView.findViewById(R.id.pal_comment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RevenueViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pal, parent, false)
        return RevenueViewHolder(view)
    }

    override fun onBindViewHolder(holder: RevenueViewHolder, position: Int) {
        val pal = palList[position]

        // 各項目にデータを設定
        holder.accountitemCode.text = pal.accountitemcode
        holder.accountitemName.text = pal.accountitemname
        holder.accountClassification.text = pal.accountclassification
        holder.palAmount.text = "${pal.amount}"
        holder.palComment.text = pal.comment

        // アイテムクリックリスナーを設定
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(pal)
        }
    }
    fun updateData(newPaLList: List<PaL>) {
        palList = newPaLList
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: (PaL) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return palList.size
    }

}
