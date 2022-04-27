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

    private String currentStreamURL = "", currentChatURL = "", channelName = "", mainWindow = "";

    private ArrayList<String> history;

    private TinyDB tinyDB;

    private Menu optionMenu;

    private void showInfos(){

        setTitle("twitch.tv/" + channelName);

        optionMenu.findItem(R.id.openBrowser).setEnabled(true);
        optionMenu.findItem(R.id.streamerChannel).setEnabled(true);
        optionMenu.findItem(R.id.streamerChannel).setTitle("Go to twitch.tv/" + channelName);
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

    private void fullScreen() {

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
    }

    private void createURLs(AutoCompleteTextView autoCompleteTextView) {

        channelName = autoCompleteTextView.getText().toString().replace(" ", "").toLowerCase();

        currentStreamURL = "https://player.twitch.tv/?channel=" + channelName + "&parent=twitch.tv";

        currentChatURL = "https://www.twitch.tv/popout/" + channelName + "/chat?no-mobile-redirect=true";
    }

    private void layoutConfig(int orientation, float stream, float chat) {

        viewLayout.setOrientation(orientation);

        swipeRefreshStream.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, stream));
        webViewChat.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, chat));
    }

    private void alternate() {

        if (mainWindow.equals("Stream")) {

            createChatInterface();
        } else if (mainWindow.equals("Chat")) {

            createStreamInterface();
        }
    }

    private void createStreamInterface() {

        webViewChat.loadUrl("about:blank");

        imageView.setVisibility(View.GONE);

        webViewChat.setVisibility(View.GONE);

        optionMenu.findItem(R.id.showChat).setVisible(true);
        optionMenu.findItem(R.id.refresh).setVisible(false);
        optionMenu.findItem(R.id.showStream).setVisible(false);
        optionMenu.findItem(R.id.alternate).setEnabled(true);
        optionMenu.findItem(R.id.alternate).setTitle("Chat as Main Window");

        swipeRefreshStream.setVisibility(View.VISIBLE);

        webViewStream.setVisibility(View.VISIBLE);

        loadStream();

        mainWindow = "Stream";
    }

    private void createChatInterface() {

        webViewStream.loadUrl("about:blank");

        imageView.setVisibility(View.GONE);

        swipeRefreshStream.setVisibility(View.GONE);

        webViewStream.setVisibility(View.GONE);

        optionMenu.findItem(R.id.showChat).setVisible(false);
        optionMenu.findItem(R.id.refresh).setVisible(true);
        optionMenu.findItem(R.id.showStream).setVisible(true);
        optionMenu.findItem(R.id.alternate).setEnabled(true);
        optionMenu.findItem(R.id.alternate).setTitle("Stream as Main Window");

        webViewChat.setVisibility(View.VISIBLE);

        loadChat();

        mainWindow = "Chat";
    }

    private void showChat() {

        if (webViewChat.getVisibility() == View.GONE && webViewStream.getVisibility() == View.VISIBLE) {

            webViewChat.setVisibility(View.VISIBLE);

            loadStreamChat();

        } else {

            webViewChat.loadUrl("about:blank");

            webViewChat.setVisibility(View.GONE);
        }

    }

    private void showStream() {

        if (webViewStream.getVisibility() == View.GONE && webViewChat.getVisibility() == View.VISIBLE) {

            swipeRefreshStream.setVisibility(View.VISIBLE);

            webViewStream.setVisibility(View.VISIBLE);

            loadChatStream();

        } else {

            webViewStream.loadUrl("about:blank");

            swipeRefreshStream.setVisibility(View.GONE);

            webViewStream.setVisibility(View.GONE);
        }

    }

    private void loadChat() {

        createWebViewChat();

        if (!history.contains(channelName)) {

            tinyDB.remove("history");
            history.add(channelName);
            tinyDB.putListString("history", history);
        }

        int orientation = this.getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {

            layoutConfig(LinearLayout.HORIZONTAL, 30, 70);
        } else {

            layoutConfig(LinearLayout.VERTICAL, 50, 50);
        }
    }

    private void loadStream() {

        createWebViewStream();

        if (!history.contains(channelName)) {

            tinyDB.remove("history");
            history.add(channelName);
            tinyDB.putListString("history", history);
        }

        int orientation = this.getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {

            layoutConfig(LinearLayout.HORIZONTAL, 30, 70);
        } else {

            layoutConfig(LinearLayout.VERTICAL, 50, 50);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void createWebViewStream() {

        webViewStream.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if (url.equals(currentStreamURL)) {
                    view.loadUrl(url);
                }

                return true;
            }

            public void onPageFinished(WebView view, String url) {

                swipeRefreshStream.setRefreshing(false);

                webViewStream.stopLoading();

                Toast.makeText(MainActivity.this, "Long press the video stream window to enter fullscreen.", Toast.LENGTH_LONG).show();
            }
        });

        WebSettings webSettingsStream = webViewStream.getSettings();
        webSettingsStream.setJavaScriptEnabled(true);
        webSettingsStream.setJavaScriptCanOpenWindowsAutomatically(false);
        webSettingsStream.setUseWideViewPort(true);
        webSettingsStream.setLoadWithOverviewMode(true);

        webViewStream.loadUrl(currentStreamURL);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void createWebViewChat() {

        webViewChat.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if (url.equals(currentChatURL)) {
                    view.loadUrl(url);
                }

                return true;
            }

            public void onPageFinished(WebView view, String url) {

                webViewChat.stopLoading();
            }
        });

        WebSettings webSettingsStream = webViewChat.getSettings();
        webSettingsStream.setJavaScriptEnabled(true);
        webSettingsStream.setJavaScriptCanOpenWindowsAutomatically(false);
        webSettingsStream.setUseWideViewPort(true);
        webSettingsStream.setLoadWithOverviewMode(true);

        webViewChat.loadUrl(currentChatURL);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadStreamChat() {

        if (webViewChat.getVisibility() == View.VISIBLE) {

            createWebViewChat();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadChatStream() {

        if (webViewChat.getVisibility() == View.VISIBLE) {

            createWebViewStream();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void searchStreamer() {

        AutoCompleteTextView autoCompleteTextView = new AutoCompleteTextView(this);
        autoCompleteTextView.setHint("Insert Channel name here");
        autoCompleteTextView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        autoCompleteTextView.setInputType(InputType.TYPE_CLASS_TEXT);
        autoCompleteTextView.setMaxLines(1);

        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.addView(autoCompleteTextView);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Search Channel")
                .setMessage("Insert the Channel name of the Streamer below and choose either the Stream or Chat as the main window.")
                .setPositiveButton("Stream", null)
                .setNegativeButton("Chat", null)
                .setNeutralButton("Cancel", null)
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
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        positiveButton.setOnClickListener(v -> {

            createURLs(autoCompleteTextView);

            if (!autoCompleteTextView.getText().toString().equals("")) {

                createStreamInterface();

                showInfos();

                dialog.dismiss();
            } else {

                Toast.makeText(MainActivity.this, "Error. The field cannot be empty.", Toast.LENGTH_SHORT).show();
            }
        });

        negativeButton.setOnClickListener(view -> {

            createURLs(autoCompleteTextView);

            if (!autoCompleteTextView.getText().toString().equals("")) {

                createChatInterface();

                showInfos();

                dialog.dismiss();
            } else {

                Toast.makeText(MainActivity.this, "Error. The field cannot be empty.", Toast.LENGTH_SHORT).show();
            }
        });
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

            case R.id.showStream:

                showStream();

                break;

            case R.id.refresh:

                loadChat();

                break;

            case R.id.alternate:

                alternate();

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

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);

        optionMenu = menu;

        optionMenu.findItem(R.id.openBrowser).setEnabled(false);
        optionMenu.findItem(R.id.streamerChannel).setEnabled(false);

        return super.onCreateOptionsMenu(menu);
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

        if (mainWindow.equals("Stream")) {

            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

                layoutConfig(LinearLayout.HORIZONTAL, 30f, 70f);

            } else {

                layoutConfig(LinearLayout.VERTICAL, 50f, 50f);
            }
        } else if (mainWindow.equals("Chat")) {

            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

                layoutConfig(LinearLayout.HORIZONTAL, 50f, 50f);

            } else {

                layoutConfig(LinearLayout.VERTICAL, 50f, 50f);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
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

            fullScreen();

            return false;
        });

        swipeRefreshStream.setOnRefreshListener(this::loadStream);

        if (savedInstanceState == null) {
            webViewStream.loadUrl(currentStreamURL);
            webViewChat.loadUrl(currentChatURL);
        }
    }
}