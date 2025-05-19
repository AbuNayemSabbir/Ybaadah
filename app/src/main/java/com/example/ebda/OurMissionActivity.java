package com.example.ebda;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class OurMissionActivity extends AppCompatActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_our_mission);

        // Initialize WebView
        WebView webView = findViewById(R.id.webViewMission);
        webView.setWebViewClient(new WebViewClient());
        
        // Enable JavaScript (needed for Google Docs)
        webView.getSettings().setJavaScriptEnabled(true);
        
        // Load Google Drive document
        // Replace with your actual Google Doc ID or use a local HTML file
        String googleDocUrl = "https://docs.google.com/document/d/e/2PACX-1vQXcUay71IEvfFzUhJ-VZQjnMFQDoAiX_QGm1U8kWiOKEjTL0o9LRV1CZg9jYI-lA/pub?embedded=true";
        webView.loadUrl(googleDocUrl);
        
        // Alternative: Load local HTML content
        /*
        String missionHtml = "<html><body style='text-align:center; padding:20px;'>" +
                "<h1>Our Mission</h1>" +
                "<p>Our mission is to help Muslims maintain proper prayer times by automatically silencing their phones during prayer times.</p>" +
                "<p>This app allows you to set specific times for each prayer, and your phone will automatically switch to silent mode during those times.</p>" +
                "<p>We believe this small service can help Muslims focus on their prayers without distractions.</p>" +
                "</body></html>";
        webView.loadData(missionHtml, "text/html", "UTF-8");
        */
    }
}