package com.etnc.sallevirtuelle;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView webView;
    private static final String HOME = "file:///android_asset/index.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(true);
        }
        getWindow().setStatusBarColor(Color.rgb(4,18,31));
        getWindow().setNavigationBarColor(Color.rgb(3,15,26));

        webView = new WebView(this);
        setContentView(webView);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setDefaultTextEncodingName("utf-8");
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setSupportZoom(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);

        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cm.setAcceptThirdPartyCookies(webView, true);
        }

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            private boolean isInternalWeb(Uri uri) {
                if (uri == null) return false;
                String host = uri.getHost();
                if (host == null) return false;
                host = host.toLowerCase();

                boolean defense = host.equals("defense.gouv.fr") || host.endsWith(".defense.gouv.fr");
                boolean mapsShort = host.equals("maps.app.goo.gl");
                boolean googleMaps = host.equals("maps.google.com")
                        || ((host.equals("google.com") || host.equals("www.google.com"))
                        && uri.getPath() != null && uri.getPath().startsWith("/maps"));

                return defense || mapsShort || googleMaps;
            }

            private boolean handleUrl(WebView view, String url) {
                if (url == null) return false;

                if (url.startsWith("intent://")) {
                    try {
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        String fallback = intent.getStringExtra("browser_fallback_url");
                        if (fallback != null && (fallback.startsWith("https://") || fallback.startsWith("http://"))) {
                            view.loadUrl(fallback);
                        }
                    } catch (Exception ignored) {}
                    return true;
                }

                Uri uri = Uri.parse(url);
                String scheme = uri.getScheme();

                if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
                    if (isInternalWeb(uri)) return false;
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    return true;
                }

                if ("geo".equalsIgnoreCase(scheme) || "market".equalsIgnoreCase(scheme)) {
                    return true;
                }

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
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (url == null) return;

                if (url.contains("defense.gouv.fr")) {
                    String js = "(function(){"+
                        "var s=document.createElement('style');"+
                        "s.innerHTML='header,footer,.fr-header,.fr-footer,.region-header,.region-footer,.breadcrumb,.fr-breadcrumb,[role=navigation]{display:none!important} body{padding-top:92px!important} #svtReaderBar{display:flex!important}';"+
                        "document.head.appendChild(s);"+
                        "if(!document.getElementById('svtReaderBar')){"+
                        "var b=document.createElement('div');b.id='svtReaderBar';"+
                        "b.setAttribute('style','position:fixed;top:34px;left:10px;right:10px;height:54px;z-index:2147483647;background:#061827;color:#fff;display:flex;align-items:center;padding:0 12px;gap:10px;font-family:Arial,sans-serif;border:1px solid #d7ad59;border-radius:14px;box-sizing:border-box');"+
                        "b.innerHTML='<button id=svtBack style=\"min-width:112px;height:42px;border:0;background:#123957;color:white;border-radius:11px;padding:0 14px;font-weight:800;font-size:15px\">← Retour</button><div style=\"font-weight:800;flex:1;white-space:nowrap;overflow:hidden;text-overflow:ellipsis\">Source officielle · Ministère des Armées</div>';"+
                        "document.body.appendChild(b);document.getElementById('svtBack').onclick=function(){history.back();};"+
                        "}"+
                        "})();";
                    view.evaluateJavascript(js, null);
                }

                if (url.contains("maps.app.goo.gl") || url.contains("google.com/maps") || url.contains("maps.google.com")) {
                    String js = "(function(){"+
                        "if(!document.getElementById('svtMapBack')){"+
                        "var b=document.createElement('button');b.id='svtMapBack';"+
                        "b.setAttribute('style','position:fixed;top:56px;left:12px;z-index:2147483647;min-width:136px;height:48px;border:1px solid #d7ad59;background:#061827;color:white;border-radius:14px;padding:0 14px;font:800 15px Arial,sans-serif;box-shadow:0 4px 14px rgba(0,0,0,.35)');"+
                        "b.textContent='← Salle Virtuelle';"+
                        "b.onclick=function(){history.back();};document.body.appendChild(b);"+
                        "}"+
                        "})();";
                    view.evaluateJavascript(js, null);
                }
            }
        });

        webView.loadUrl(HOME);
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
