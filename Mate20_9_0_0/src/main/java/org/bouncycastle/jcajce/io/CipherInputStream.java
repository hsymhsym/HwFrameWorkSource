package org.bouncycastle.jcajce.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.crypto.Cipher;
import org.bouncycastle.crypto.io.InvalidCipherTextIOException;

public class CipherInputStream extends FilterInputStream {
    private byte[] buf;
    private int bufOff;
    private final Cipher cipher;
    private boolean finalized = false;
    private final byte[] inputBuffer = new byte[512];
    private int maxBuf;

    public CipherInputStream(InputStream inputStream, Cipher cipher) {
        super(inputStream);
        this.cipher = cipher;
    }

    private byte[] finaliseCipher() throws InvalidCipherTextIOException {
        try {
            this.finalized = true;
            return this.cipher.doFinal();
        } catch (Throwable e) {
            throw new InvalidCipherTextIOException("Error finalising cipher", e);
        }
    }

    private int nextChunk() throws IOException {
        if (this.finalized) {
            return -1;
        }
        this.bufOff = 0;
        this.maxBuf = 0;
        while (this.maxBuf == 0) {
            int read = this.in.read(this.inputBuffer);
            if (read == -1) {
                this.buf = finaliseCipher();
                if (this.buf == null || this.buf.length == 0) {
                    return -1;
                }
                this.maxBuf = this.buf.length;
                return this.maxBuf;
            }
            this.buf = this.cipher.update(this.inputBuffer, 0, read);
            if (this.buf != null) {
                this.maxBuf = this.buf.length;
            }
        }
        return this.maxBuf;
    }

    public int available() throws IOException {
        return this.maxBuf - this.bufOff;
    }

    public void close() throws IOException {
        try {
            this.in.close();
            this.bufOff = 0;
            this.maxBuf = 0;
        } finally {
            if (!this.finalized) {
                finaliseCipher();
            }
        }
    }

    public void mark(int i) {
    }

    public boolean markSupported() {
        return false;
    }

    public int read() throws IOException {
        if (this.bufOff >= this.maxBuf && nextChunk() < 0) {
            return -1;
        }
        byte[] bArr = this.buf;
        int i = this.bufOff;
        this.bufOff = i + 1;
        return bArr[i] & 255;
    }

    public int read(byte[] bArr, int i, int i2) throws IOException {
        if (this.bufOff >= this.maxBuf && nextChunk() < 0) {
            return -1;
        }
        i2 = Math.min(i2, available());
        System.arraycopy(this.buf, this.bufOff, bArr, i, i2);
        this.bufOff += i2;
        return i2;
    }

    public void reset() throws IOException {
    }

    public long skip(long j) throws IOException {
        if (j <= 0) {
            return 0;
        }
        int min = (int) Math.min(j, (long) available());
        this.bufOff += min;
        return (long) min;
    }
}
