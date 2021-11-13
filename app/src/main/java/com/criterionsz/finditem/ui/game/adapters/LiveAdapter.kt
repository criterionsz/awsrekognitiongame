package com.criterionsz.finditem.ui.game.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.criterionsz.finditem.databinding.ItemLiveBinding

class LiveAdapter(private val lives: MutableList<Int>) :
    RecyclerView.Adapter<LiveAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemLiveBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(image: Int) {
            binding.live.setImageResource(image)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemLiveBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(lives[position])
    }

    fun submit(newList: List<Int>) {
        lives.clear()
        lives.addAll(newList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return lives.size
    }

}

