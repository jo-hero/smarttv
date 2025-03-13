package net.jo.bean;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;

public class UnicodeReader extends Reader {
    private static final int BOM_SIZE = 4;
    String defaultEnc;
    PushbackInputStream internalIn;
    InputStreamReader internalIn2 = null;

    public UnicodeReader(InputStream inputStream, String str) {
        this.internalIn = new PushbackInputStream(inputStream, 4);
        this.defaultEnc = str;
    }

    public String getDefaultEncoding() {
        return this.defaultEnc;
    }

    public String getEncoding() {
        if (this.internalIn2 == null) {
            return null;
        }
        return this.internalIn2.getEncoding();
    }

    /* access modifiers changed from: protected */
    public void init() throws IOException {
        int i;
        String str;
        if (this.internalIn2 == null) {
            byte[] bArr = new byte[4];
            int read = this.internalIn.read(bArr, 0, bArr.length);
            if (bArr[0] == 0 && bArr[1] == 0 && bArr[2] == -2 && bArr[3] == -1) {
                str = "UTF-32BE";
                i = read - 4;
            } else if (bArr[0] == -1 && bArr[1] == -2 && bArr[2] == 0 && bArr[3] == 0) {
                str = "UTF-32LE";
                i = read - 4;
            } else if (bArr[0] == -17 && bArr[1] == -69 && bArr[2] == -65) {
                str = "UTF-8";
                i = read - 3;
            } else if (bArr[0] == -2 && bArr[1] == -1) {
                str = "UTF-16BE";
                i = read - 2;
            } else if (bArr[0] == -1 && bArr[1] == -2) {
                str = "UTF-16LE";
                i = read - 2;
            } else {
                str = this.defaultEnc;
                i = read;
            }
            if (i > 0) {
                this.internalIn.unread(bArr, read - i, i);
            }
            if (str == null) {
                this.internalIn2 = new InputStreamReader(this.internalIn);
            } else {
                this.internalIn2 = new InputStreamReader(this.internalIn, str);
            }
        }
    }

    @Override // java.io.Closeable, java.io.Reader, java.lang.AutoCloseable
    public void close() throws IOException {
        init();
        this.internalIn2.close();
    }

    @Override // java.io.Reader
    public int read(char[] cArr, int i, int i2) throws IOException {
        init();
        return this.internalIn2.read(cArr, i, i2);
    }
}
