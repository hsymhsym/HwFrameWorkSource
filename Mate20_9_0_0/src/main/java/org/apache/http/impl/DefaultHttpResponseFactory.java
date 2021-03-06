package org.apache.http.impl;

import java.util.Locale;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.ProtocolVersion;
import org.apache.http.ReasonPhraseCatalog;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.HttpContext;

@Deprecated
public class DefaultHttpResponseFactory implements HttpResponseFactory {
    protected final ReasonPhraseCatalog reasonCatalog;

    public DefaultHttpResponseFactory(ReasonPhraseCatalog catalog) {
        if (catalog != null) {
            this.reasonCatalog = catalog;
            return;
        }
        throw new IllegalArgumentException("Reason phrase catalog must not be null.");
    }

    public DefaultHttpResponseFactory() {
        this(EnglishReasonPhraseCatalog.INSTANCE);
    }

    public HttpResponse newHttpResponse(ProtocolVersion ver, int status, HttpContext context) {
        if (ver != null) {
            Locale loc = determineLocale(context);
            return new BasicHttpResponse(new BasicStatusLine(ver, status, this.reasonCatalog.getReason(status, loc)), this.reasonCatalog, loc);
        }
        throw new IllegalArgumentException("HTTP version may not be null");
    }

    public HttpResponse newHttpResponse(StatusLine statusline, HttpContext context) {
        if (statusline != null) {
            return new BasicHttpResponse(statusline, this.reasonCatalog, determineLocale(context));
        }
        throw new IllegalArgumentException("Status line may not be null");
    }

    protected Locale determineLocale(HttpContext context) {
        return Locale.getDefault();
    }
}
