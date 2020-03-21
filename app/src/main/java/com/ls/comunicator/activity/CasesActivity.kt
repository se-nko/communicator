package com.ls.comunicator.activity

import android.media.*
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.ls.comunicator.R
import com.ls.comunicator.core.Card
import com.ls.comunicator.core.CaseEnum
import com.ls.comunicator.core.Consts
import com.ls.comunicator.core.SingletonCard
import java.lang.Exception
import java.util.*


class CasesActivity : AppCompatActivity() {

    lateinit var card: Card
    private val watcher: TextChange = TextChange()
    lateinit var mediaRecorder: MediaRecorder
    lateinit var mediaPlayer: MediaPlayer
    lateinit var nEditText: TextInputEditText
    lateinit var gEditText: TextInputEditText
    lateinit var dEditText: TextInputEditText
    lateinit var aEditText: TextInputEditText
    lateinit var iEditText: TextInputEditText
    lateinit var pEditText: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cases)

        nEditText = findViewById(R.id.nominative_text)
        nEditText.addTextChangedListener(watcher)
        gEditText = findViewById(R.id.genitive_text)
        gEditText.addTextChangedListener(watcher)
        dEditText=  findViewById(R.id.dative_text)
        dEditText.addTextChangedListener(watcher)
        aEditText = findViewById(R.id.accusative_text)
        aEditText.addTextChangedListener(watcher)
        iEditText = findViewById(R.id.instrumental_text)
        iEditText.addTextChangedListener(watcher)
        pEditText = findViewById(R.id.prepositional_text)
        pEditText.addTextChangedListener(watcher)

        card = SingletonCard.card

        if (card.cases == null) {
            card.cases = EnumMap(CaseEnum::class.java)
            card.cases[CaseEnum.NOMINATIVE] = ""
            card.cases[CaseEnum.GENITVIE] = ""
            card.cases[CaseEnum.DATIVE] = ""
            card.cases[CaseEnum.ACCUSATIVE] = ""
            card.cases[CaseEnum.INSTRUMENTAL] = ""
            card.cases[CaseEnum.PREPOSITIONAL] = ""
        }

        findViewById<MaterialButton>(R.id.back_button)
            .setOnClickListener {
                onBackPressed()
            }
    }

    private fun createMediaRecorder() {
        mediaRecorder = MediaRecorder()
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)


    }

    fun onVoiceBtnClick(button: View) {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Запись")
        val view = layoutInflater.inflate(R.layout.dialog_voice, null)
        val startBtn = view.findViewById<MaterialButton>(R.id.start_play)
        val stopBtn = view.findViewById<MaterialButton>(R.id.stop_play)

        startBtn.setOnClickListener {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO) ,
                    123)
            createMediaRecorder()
            startBtn.isEnabled = false
            stopBtn.isEnabled = true
            mediaRecorder.setOutputFile(getPath(button))
           try {
               mediaRecorder.prepare()
               mediaRecorder.start()
           } catch (e: Exception) {
                e.printStackTrace()
           }
        }
        stopBtn.setOnClickListener {
            startBtn.isEnabled = true
            stopBtn.isEnabled = false
            mediaRecorder.stop()
            mediaRecorder.release()
        }
        builder.setView(view)
        builder.setPositiveButton("Ok") { dialogInterface, i ->
        }
        builder.show()
    }

    fun onFileBtnClick(view: View) {
        when (view.id) {
            R.id.nominative_file_button -> {}
            R.id.genitive_file_button -> {}
            R.id.dative_file_button -> {}
            R.id.accusative_file_button -> {}
            R.id.instrumental_file_button -> {}
            R.id.prepositional_file_button -> {}
        }
    }

    fun getPath(view: View): String {
        var case: CaseEnum = CaseEnum.NOMINATIVE
        val page = card.page.toLowerCase(Locale.getDefault())
        val name = card.name.toLowerCase(Locale.getDefault())

        when (view.id) {
            R.id.nominative_voice_button -> {case = CaseEnum.NOMINATIVE}
            R.id.genitive_voice_button -> {case = CaseEnum.GENITVIE}
            R.id.dative_voice_button -> {case = CaseEnum.DATIVE}
            R.id.accusative_voice_button -> {case = CaseEnum.ACCUSATIVE}
            R.id.instrumental_voice_button -> {case = CaseEnum.INSTRUMENTAL}
            R.id.prepositional_voice_button -> {case = CaseEnum.PREPOSITIONAL}
        }
        when (view.id) {
            R.id.nominative_play_button -> {case = CaseEnum.NOMINATIVE}
            R.id.genitive_play_button -> {case = CaseEnum.GENITVIE}
            R.id.dative_play_button -> {case = CaseEnum.DATIVE}
            R.id.accusative_play_button -> {case = CaseEnum.ACCUSATIVE}
            R.id.instrumental_play_button -> {case = CaseEnum.INSTRUMENTAL}
            R.id.prepositional_play_button -> {case = CaseEnum.PREPOSITIONAL}
        }
        return Environment.getExternalStorageDirectory().absolutePath +
                "/${Consts.LISTS_FOLDER}/${page}/${name}/sound/${case.text}.3gp"
    }

    inner class TextChange : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            when (p0) {
                nEditText.editableText -> { card.cases[CaseEnum.NOMINATIVE] = p0.toString() }
                gEditText.editableText -> { card.cases[CaseEnum.GENITVIE] = p0.toString() }
                dEditText.editableText -> { card.cases[CaseEnum.DATIVE] = p0.toString() }
                aEditText.editableText -> { card.cases[CaseEnum.ACCUSATIVE] = p0.toString() }
                iEditText.editableText -> { card.cases[CaseEnum.INSTRUMENTAL] = p0.toString() }
                pEditText.editableText -> { card.cases[CaseEnum.PREPOSITIONAL] = p0.toString() }
            }
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    }

    fun onPlayBtnClick(view: View) {
        mediaPlayer = MediaPlayer()
        try {
            mediaPlayer.setDataSource(getPath(view))
            mediaPlayer.prepare()
        } catch (e : Exception) {
            e.printStackTrace()
        }
        mediaPlayer.start()
    }


    override fun onDestroy() {
        super.onDestroy()
//        mediaRecorder.release()
//        mediaPlayer.release()
    }
}
