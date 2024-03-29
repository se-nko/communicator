package com.ls.comunicator.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.ls.comunicator.R
import com.ls.comunicator.view.CardSettingsActivity
import com.ls.comunicator.core.*
import com.ls.comunicator.model.Card
import com.ls.comunicator.model.CardModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.card_list_item.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class CardAdapter(val cards : ArrayList<Card>, val context: Context, val type: CardAdapterEnum, val communicate: CardAdapter?) : RecyclerView.Adapter<CardAdapter.ViewHolder>() {

    lateinit var mTTS: TextToSpeech
    var mediaPlayer: MediaPlayer
    private var recyclerView: RecyclerView?
    private var emptyRecyclerView: TextView?


    init {
        val parentActivity = context as Activity
        recyclerView = parentActivity.findViewById(R.id.speakLineRecyclerView)
        emptyRecyclerView = parentActivity.findViewById(R.id.emptySpeakLine)
        mediaPlayer = MediaPlayer()
        mTTS = TextToSpeech(context, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR){
                //if there is no error then set language
                mTTS.language = Locale.UK
            }
            if (status == TextToSpeech.SUCCESS) {
                if (mTTS.isLanguageAvailable(Locale(Locale.getDefault().language))
                    == TextToSpeech.LANG_AVAILABLE) {
                    mTTS.language = Locale(Locale.getDefault().language)
                } else {
                    mTTS.language = Locale.US
                }
                mTTS.setPitch(1.3f)
                mTTS.setSpeechRate(0.7f)
            } else if (status == TextToSpeech.ERROR) {
                // TODO error msg
            }
        })
    }

    override fun getItemCount(): Int {
        return cards.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var viewHolder = ViewHolder(LayoutInflater.from(context).inflate(R.layout.card_list_item, parent, false))
        if(type == CardAdapterEnum.COMMUNICATIVE_LINE)
            viewHolder = ViewHolder(LayoutInflater.from(context).inflate(R.layout.card_list_communicative_item, parent, false))
        return viewHolder
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(cards.get(position))
    }

    fun add(card: Card) {
        if (cards.isEmpty()) {
            recyclerView?.visibility = View.VISIBLE
            emptyRecyclerView?.visibility = View.GONE
        }
        cards.add(card)
        notifyDataSetChanged()
        recyclerView?.layoutManager?.scrollToPosition(cards.size - 1)
    }

    fun delete() {
        if (cards.isNotEmpty()) {
            cards.removeAt(cards.lastIndex)
            notifyDataSetChanged()
        }
        if (cards.isEmpty()) {
            recyclerView?.visibility = View.GONE
            emptyRecyclerView?.visibility = View.VISIBLE
        }
    }

    fun deleteAll() {
        recyclerView?.visibility = View.GONE
        emptyRecyclerView?.visibility = View.VISIBLE
        if (cards.isNotEmpty()) {
            cards.clear()
            notifyDataSetChanged()
        }
    }

    inner class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {

        private val cardFrame = view.card_frame
        private val cardImage = view.card_image
        private val cardText = view.card_text

        fun bind(card: Card) {
            Picasso.get()
                .load(File(getFilesDir(context), "/${Consts.LISTS_FOLDER}" +
                        "/${card.page.toLowerCase(Locale.getDefault())}" +
                        "/${card.name.toLowerCase(Locale.getDefault())}/image.jpg"))
                .centerCrop()
                .placeholder(R.drawable.ic_image_black_24dp)
                .resize(400, 400)
                .into(cardImage)
            cardText.text = card.name

            if (card.image != null) {
                cardFrame.strokeColor = card.image.borderColour
                cardFrame.strokeWidth = card.image.borderSize
                cardText.setTextColor(card.image.textColour)
                cardText.textSize = card.image.textSize
                if (card.image.textPlace != null) {
                    val imageParams = cardImage.layoutParams as RelativeLayout.LayoutParams
                    val textParams = cardText.layoutParams as RelativeLayout.LayoutParams
                    when(card.image.textPlace) {
                        TextPositionEnum.UP -> {
                            textParams.removeRule(RelativeLayout.BELOW)
                            textParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                            textParams.topMargin = 10
                            textParams.bottomMargin = 0
                            imageParams.addRule(RelativeLayout.BELOW, R.id.card_text)
                            imageParams.topMargin = 0
                            imageParams.bottomMargin = 20
                        }
                        TextPositionEnum.BOTTOM -> {
                            textParams.removeRule(RelativeLayout.ALIGN_PARENT_TOP)
                            imageParams.removeRule(RelativeLayout.BELOW)
                            textParams.addRule(RelativeLayout.BELOW, R.id.card_image)
                            textParams.topMargin = 0
                            textParams.bottomMargin = 10
                            imageParams.topMargin = 20
                            imageParams.bottomMargin = 0
                        }
                        else -> {}
                    }
                }
            }
            when(type) {
                CardAdapterEnum.PAGE -> {
                    itemView.setOnLongClickListener{
                        communicate?.add(card)
                        true
                    }
                    itemView.setOnClickListener{
                        GlobalScope.launch { play(context, arrayListOf(card), mediaPlayer, mTTS) }
                    }
                }
                CardAdapterEnum.EDIT_PAGE -> {
                    val popupMenu = PopupMenu(context, itemView)
                    popupMenu.inflate(R.menu.cardmenu)
                    popupMenu.setOnMenuItemClickListener {
                        when(it.itemId) {
                            R.id.menu_edit -> {
                                val cardSettingsActivity = Intent(context, CardSettingsActivity::class.java)
                                cardSettingsActivity.putExtra("page", card.page)
                                cardSettingsActivity.putExtra("name", card.name)
                                cardSettingsActivity.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(context, cardSettingsActivity, null)
                                true
                            }
                            R.id.menu_delete -> {
                                CardModel.deletePage(context, card.page, card.name, object : CardModel.CompleteCallback {
                                    override fun onComplete(success: Boolean) {
                                        if (success) {
                                            cards.remove(card)
                                            notifyDataSetChanged()
                                        }
                                        Toast.makeText(context, if (success) "Удалено" else "Не удалось", Toast.LENGTH_SHORT).show()
                                    }
                                })
                                true
                            }
                            else -> false
                        }
                    }
                    itemView.setOnLongClickListener{
                        popupMenu.show()
                        true
                    }
                }
                else -> {}
            }
        }
    }
}


