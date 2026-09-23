package com.etnc.sallevirtuelle;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView webView;

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
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            private boolean isInternalOfficial(Uri uri) {
                String host = uri != null ? uri.getHost() : null;
                if (host == null) return false;
                host = host.toLowerCase();
                return host.equals("defense.gouv.fr") || host.endsWith(".defense.gouv.fr");
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme();
                if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
                    if (isInternalOfficial(uri)) return false;
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    return true;
                }
                return false;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
                    Uri uri = Uri.parse(url);
                    if (isInternalOfficial(uri)) return false;
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (url != null && url.contains("defense.gouv.fr")) {
                    String js = "(function(){"+
                        "var s=document.createElement('style');"+
                        "s.innerHTML='header,footer,.fr-header,.fr-footer,.region-header,.region-footer,.breadcrumb,.fr-breadcrumb,[role=navigation]{display:none!important} body{padding-top:58px!important} #svtReaderBar{display:flex!important}';"+
                        "document.head.appendChild(s);"+
                        "if(!document.getElementById('svtReaderBar')){"+
                        "var b=document.createElement('div');b.id='svtReaderBar';"+
                        "b.setAttribute('style','position:fixed;top:0;left:0;right:0;height:58px;z-index:2147483647;background:#061827;color:#fff;display:flex;align-items:center;padding:0 14px;gap:12px;font-family:Arial,sans-serif;border-bottom:2px solid #d7ad59');"+
                        "b.innerHTML='<button id=svtBack style=\"border:0;background:#123957;color:white;border-radius:10px;padding:9px 12px;font-weight:700\">← Retour</button><div style=\"font-weight:800;flex:1\">Source officielle · Ministère des Armées</div>';"+
                        "document.body.appendChild(b);document.getElementById('svtBack').onclick=function(){history.back();};"+
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
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
}
