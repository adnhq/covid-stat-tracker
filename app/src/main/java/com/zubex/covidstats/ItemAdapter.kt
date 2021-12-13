package com.zubex.covidstats

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemAdapter(var dataset: List<CountryStats>) :
    RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {
    class ItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val titleView: TextView = itemView.findViewById(R.id.reportTitle)
        val statView: TextView = itemView.findViewById(R.id.reportStat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.titleView.text = dataset[position].title
        holder.statView.text = dataset[position].currentStats

    }

    override fun getItemCount(): Int {
        return dataset.size
    }
}