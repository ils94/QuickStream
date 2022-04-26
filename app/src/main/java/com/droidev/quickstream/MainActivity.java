package com.droidev.quickstream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefreshStream;
    private WebView webViewStream, webViewChat;
    private ImageView imageView;
    private LinearLayout viewLayout;

    private String currentStreamURL = "", currentChatURL = "", channelName = "";
    private boolean streamLoaded = false, firstStreamLoad = false;

    private ArrayList<String> history;

    private TinyDB tinyDB;

    private Menu mOptionmenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar bar = getSupportActionBar();
        assert bar != null;
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ff0006")));

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        viewLayout = findViewById(R.id.viewLayout);

        imageView = findViewById(R.id.imageView);

        swipeRefreshStream = findViewById(R.id.swipeRefreshStream);

        webViewStream = findViewById(R.id.webViewStream);
        webViewStream.setBackgroundColor(Color.TRANSPARENT);

        webViewChat = findViewById(R.id.webViewChat);
        webViewChat.setBackgroundColor(Color.TRANSPARENT);

        history = new ArrayList<>();

        tinyDB = new TinyDB(this);

        webViewStream.setOnLongClickListener(view -> {

            View decorView = getWindow().getDecorView();

            if (Objects.requireNonNull(getSupportActionBar()).isShowing()) {

                getSupportActionBar().hide();

                int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                decorView.setSystemUiVisibility(uiOptions);

            } else {

                getSupportActionBar().show();

                int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
                decorView.setSystemUiVisibility(uiOptions);
            }

            return false;
        });

        swipeRefreshStream.setOnRefreshListener(() -> webViewStream.reload());

        if (savedInstanceState == null) {
            webViewStream.loadUrl(currentStreamURL);
            webViewChat.loadUrl(currentChatURL);
        }

        Toast.makeText(this, "Long press the Stream Video to enter fullscreen.", Toast.LENGTH_LONG).show();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);

        mOptionmenu = menu;

        mOptionmenu.findItem(R.id.openBrowser).setEnabled(false);
        mOptionmenu.findItem(R.id.streamerChannel).setEnabled(false);

        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.search:

                searchStreamer();

                break;

            case R.id.showChat:

                showChat();

                break;

            case R.id.clearHistory:

                clearHistory();

                break;

            case R.id.openBrowser:

                openBrowser(currentStreamURL);

                break;

            case R.id.streamerChannel:

                openBrowser("https://www.twitch.tv/" + channelName);

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void searchStreamer() {

        AutoCompleteTextView autoCompleteTextView = new AutoCompleteTextView(this);
        autoCompleteTextView.setHint("Insert channel name here");
        autoCompleteTextView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        autoCompleteTextView.setInputType(InputType.TYPE_CLASS_TEXT);
        autoCompleteTextView.setMaxLines(1);

        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.addView(autoCompleteTextView);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Search for Streamer")
                .setMessage("Insert the channel name of the Streamer below and click Play!")
                .setPositiveButton("Play!", null)
                .setNegativeButton("Cancel", null)
                .setView(lay)
                .show();

        autoCompleteTextView.setOnTouchListener((view, motionEvent) -> {

            autoCompleteTextView.showDropDown();

            return false;
        });

        history = tinyDB.getListString("history");

        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, history);
        autoCompleteTextView.setAdapter(adapter);

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

        positiveButton.setOnClickListener(v -> {

            channelName = autoCompleteTextView.getText().toString().replace(" ", "").toLowerCase();

            currentStreamURL = "https://player.twitch.tv/?channel=" + channelName + "&parent=twitch.tv";

            currentChatURL = "https://www.twitch.tv/popout/" + channelName + "/chat?no-mobile-redirect=true";

            if (!channelName.equals("")) {

                imageView.setVisibility(View.GONE);

                swipeRefreshStream.setVisibility(View.VISIBLE);

                webViewStream.setVisibility(View.VISIBLE);

                playVideo(currentStreamURL);

                dialog.dismiss();
            } else {

                Toast.makeText(MainActivity.this, "Error. The field cannot be empty.", Toast.LENGTH_SHORT).show();
            }
        });

        autoCompleteTextView.setOnEditorActionListener((textView, i, keyEvent) -> {

            if (i == EditorInfo.IME_ACTION_SEARCH) {

                channelName = autoCompleteTextView.getText().toString().replace(" ", "").toLowerCase();

                currentStreamURL = "https://player.twitch.tv/?channel=" + channelName + "&parent=twitch.tv";

                currentChatURL = "https://www.twitch.tv/popout/" + channelName + "/chat?no-mobile-redirect=true";

                if (!channelName.equals("")) {

                    imageView.setVisibility(View.GONE);

                    swipeRefreshStream.setVisibility(View.VISIBLE);

                    webViewStream.setVisibility(View.VISIBLE);

                    playVideo(currentStreamURL);

                    dialog.dismiss();
                } else {

                    Toast.makeText(MainActivity.this, "Error. The field cannot be empty.", Toast.LENGTH_SHORT).show();
                }

                return true;
            }
            return false;
        });

    }

    private void clearHistory() {

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Clear History")
                .setMessage("This will clear all your searching history. Do you wish to continue?")
                .setPositiveButton("Yes", null)
                .setNegativeButton("No", null)
                .show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

        positiveButton.setOnClickListener(v -> {

            tinyDB.remove("history");

            Toast.makeText(MainActivity.this, "Search History has been cleaned.", Toast.LENGTH_SHORT).show();

            dialog.dismiss();
        });
    }

    private void openBrowser(String url) {

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void playVideo(String urlStream) {

        webViewStream.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if (url.equals(currentStreamURL)) {
                    view.loadUrl(url);
                }

                return true;
            }

            public void onPageFinished(WebView view, String url) {

                if (!swipeRefreshStream.isRefreshing()) {

                    streamLoaded = true;

                    firstStreamLoad = true;

                    loadChat();
                }

                swipeRefreshStream.setRefreshing(false);
            }
        });

        WebSettings webSettingsStream = webViewStream.getSettings();
        webSettingsStream.setJavaScriptEnabled(true);
        webSettingsStream.setJavaScriptCanOpenWindowsAutomatically(false);
        webSettingsStream.setUseWideViewPort(true);
        webSettingsStream.setLoadWithOverviewMode(true);

        webViewStream.loadUrl(urlStream);

        loadChat();

        if (!history.contains(channelName)) {

            tinyDB.remove("history");
            history.add(channelName);
            tinyDB.putListString("history", history);
        }

        setTitle("twitch.tv/" + channelName);

        mOptionmenu.findItem(R.id.openBrowser).setEnabled(true);
        mOptionmenu.findItem(R.id.streamerChannel).setEnabled(true);
        mOptionmenu.findItem(R.id.streamerChannel).setTitle("Go to twitch.tv/" + channelName);

        int orientation = this.getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {

            layoutConfig(LinearLayout.HORIZONTAL, 30, 70);
        } else {

            layoutConfig(LinearLayout.VERTICAL, 50, 50);
        }
    }

    private void showChat() {

        if (firstStreamLoad) {

            if (webViewChat.getVisibility() == View.GONE && webViewStream.getVisibility() == View.VISIBLE) {

                streamLoaded = true;

                webViewChat.setVisibility(View.VISIBLE);

                loadChat();

            } else {

                webViewChat.setVisibility(View.GONE);

                webViewChat.loadUrl("javascript:document.open();document.close();");
            }
        } else {

            Toast.makeText(this, "You must first open a Stream, and wait for it to finish loading.", Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadChat() {

        if (webViewChat.getVisibility() == View.VISIBLE && streamLoaded) {

            webViewChat.setWebViewClient(new WebViewClient() {
                public boolean shouldOverrideUrlLoading(WebView view, String url) {

                    if (url.equals(currentStreamURL)) {
                        view.loadUrl(url);
                    }

                    return true;
                }

                @Override
                public void onPageFinished(WebView view, String url) {

                    webViewChat.stopLoading();
                }
            });

            WebSettings webSettingsChat = webViewChat.getSettings();
            webSettingsChat.setJavaScriptEnabled(true);
            webSettingsChat.setJavaScriptCanOpenWindowsAutomatically(false);
            webSettingsChat.setUseWideViewPort(true);
            webSettingsChat.setLoadWithOverviewMode(true);

            webViewChat.loadUrl(currentChatURL);

            streamLoaded = false;
        }
    }

    private void layoutConfig(int orientation, float stream, float chat) {

        viewLayout.setOrientation(orientation);

        swipeRefreshStream.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, stream));
        webViewChat.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, chat));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        webViewStream.saveState(outState);
        webViewChat.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webViewStream.restoreState(savedInstanceState);
        webViewChat.saveState(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

            layoutConfig(LinearLayout.HORIZONTAL, 30f, 70f);

        } else {

            layoutConfig(LinearLayout.VERTICAL, 50f, 50f);
        }
    }
}