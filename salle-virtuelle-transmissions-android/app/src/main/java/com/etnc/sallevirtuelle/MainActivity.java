package com.etnc.sallevirtuelle;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView webView;
    private boolean museumMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(4,18,31));
        getWindow().setNavigationBarColor(Color.rgb(3,15,26));

        webView = new WebView(this);
        setContentView(webView);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setDefaultTextEncodingName("utf-8");
        s.setSupportZoom(true);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setJavaScriptCanOpenWindowsAutomatically(false);
        s.setSupportMultipleWindows(false);

        CookieManager.getInstance().setAcceptCookie(true);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            private boolean isDefense(Uri uri) {
                String host = uri != null ? uri.getHost() : null;
                if (host == null) return false;
                host = host.toLowerCase();
                return host.equals("defense.gouv.fr") || host.endsWith(".defense.gouv.fr");
            }

            private boolean isMuseumStart(Uri uri) {
                String host = uri != null ? uri.getHost() : null;
                if (host == null) return false;
                host = host.toLowerCase();
                return host.equals("maps.app.goo.gl");
            }

            private boolean handleUrl(WebView view, String url) {
                if (url == null) return false;

                if (url.startsWith("intent://")) {
                    if (museumMode) {
                        try {
                            Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                            String fallback = intent.getStringExtra("browser_fallback_url");
                            if (fallback != null && (fallback.startsWith("https://") || fallback.startsWith("http://"))) {
                                view.loadUrl(fallback);
                            }
                        } catch (Exception ignored) {}
                        return true;
                    }
                    return true;
                }

                Uri uri = Uri.parse(url);
                String scheme = uri.getScheme();

                if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
                    if (isMuseumStart(uri)) {
                        museumMode = true;
                        return false;
                    }
                    if (museumMode) {
                        return false;
                    }
                    if (isDefense(uri)) {
                        return false;
                    }
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    return true;
                }

                // Prevent Google Maps from launching a separate native app while in museum mode.
                if (museumMode) return true;
                return false;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleUrl(view, request.getUrl().toString());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrl(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                WebSettings settings = view.getSettings();
                if (museumMode) {
                    settings.setBuiltInZoomControls(true);
                    settings.setDisplayZoomControls(true);
                    settings.setSupportZoom(true);
                } else if (url != null && url.startsWith("file:///android_asset/")) {
                    settings.setBuiltInZoomControls(false);
                    settings.setDisplayZoomControls(false);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (url != null && url.startsWith("file:///android_asset/")) {
                    museumMode = false;
                    view.getSettings().setBuiltInZoomControls(false);
                    view.getSettings().setDisplayZoomControls(false);
                }

                if (url != null && url.contains("defense.gouv.fr")) {
                    String js = "(function(){"+
                        "var s=document.createElement('style');"+
                        "s.innerHTML='header,footer,.fr-header,.fr-footer,.region-header,.region-footer,.breadcrumb,.fr-breadcrumb,[role=navigation]{display:none!important} body{padding-top:92px!important} #svtReaderBar{display:flex!important}';"+
                        "document.head.appendChild(s);"+
                        "if(!document.getElementById('svtReaderBar')){"+
                        "var b=document.createElement('div');b.id='svtReaderBar';"+
                        "b.setAttribute('style','position:fixed;top:34px;left:10px;right:10px;height:54px;z-index:2147483647;background:#061827;color:#fff;display:flex;align-items:center;padding:0 12px;gap:10px;font-family:Arial,sans-serif;border:1px solid #d7ad59;border-radius:14px');"+
                        "b.innerHTML='<button id=svtBack style=\"border:0;background:#123957;color:white;border-radius:10px;padding:9px 12px;font-weight:700\">← Retour</button><div style=\"font-weight:800;flex:1\">Source officielle · Ministère des Armées</div>';"+
                        "document.body.appendChild(b);document.getElementById('svtBack').onclick=function(){history.back();};"+
                        "}"+
                        "})();";
                    view.evaluateJavascript(js, null);
                }

                if (museumMode && url != null && !url.startsWith("file:///android_asset/")) {
                    String js = "(function(){"+
                        "if(!document.getElementById('svtMuseumBack')){"+
                        "var b=document.createElement('button');b.id='svtMuseumBack';"+
                        "b.setAttribute('style','position:fixed;top:52px;left:12px;z-index:2147483647;height:46px;border:1px solid #d7ad59;background:#061827;color:#fff;border-radius:14px;padding:0 14px;font:800 15px Arial,sans-serif;box-shadow:0 5px 16px rgba(0,0,0,.35)');"+
                        "b.textContent='← Visite';"+
                        "b.onclick=function(){history.back();};document.body.appendChild(b);"+
                        "}"+
                        "})();";
                    view.evaluateJavascript(js, null);
                }
            }
        });

        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
