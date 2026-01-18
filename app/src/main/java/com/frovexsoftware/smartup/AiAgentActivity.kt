package com.frovexsoftware.smartup

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class AiAgentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_agent)

        findViewById<android.view.View>(R.id.btnAiAgentBack).setOnClickListener {
            finish()
        }
    }
}
