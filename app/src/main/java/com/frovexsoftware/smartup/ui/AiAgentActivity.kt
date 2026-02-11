package com.frovexsoftware.smartup.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.frovexsoftware.smartup.R

class AiAgentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_agent)

        findViewById<android.view.View>(R.id.btnAiAgentBack).setOnClickListener {
            finish()
        }
    }
}
