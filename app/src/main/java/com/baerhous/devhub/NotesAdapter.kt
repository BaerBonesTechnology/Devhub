package com.baerhous.devhub

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateUtils
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.baerhous.devhub.databinding.NotesAdapterBinding
import com.baerhous.devhub.model.Notes


class NotesAdapter(val context: Context, private val Notes: List<Notes>, private var clickListener: ClickListener):
    RecyclerView.Adapter<NotesAdapter.ViewHolder>() {

        private lateinit var binding: NotesAdapterBinding

    inner class ViewHolder(NotesPost: View): RecyclerView.ViewHolder(NotesPost){

        @SuppressLint("SetTextI18n")
        fun bind(Notes: Notes){
            binding.txtNote.text = Notes.note_title
            binding.txtDateLastUpdated.text = DateUtils.getRelativeTimeSpanString(Notes.note_time_Created)
            binding.txtContent.text = Notes.note_Content

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = binding.root

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val note = Notes[holder.layoutPosition]
        holder.bind(note)
        holder.itemView.setOnClickListener { clickListener.clickedItem(note, binding.txtContent) }

    }

    override fun getItemCount() = Notes.size

    interface ClickListener{
        fun clickedItem(Notes: Notes, textView: TextView)
    }
}