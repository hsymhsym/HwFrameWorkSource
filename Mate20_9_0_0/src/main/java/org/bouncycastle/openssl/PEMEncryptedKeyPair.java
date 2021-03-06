package org.bouncycastle.openssl;

import java.io.IOException;

public class PEMEncryptedKeyPair {
    private final String dekAlgName;
    private final byte[] iv;
    private final byte[] keyBytes;
    private final PEMKeyPairParser parser;

    PEMEncryptedKeyPair(String str, byte[] bArr, byte[] bArr2, PEMKeyPairParser pEMKeyPairParser) {
        this.dekAlgName = str;
        this.iv = bArr;
        this.keyBytes = bArr2;
        this.parser = pEMKeyPairParser;
    }

    public PEMKeyPair decryptKeyPair(PEMDecryptorProvider pEMDecryptorProvider) throws IOException {
        StringBuilder stringBuilder;
        try {
            return this.parser.parse(pEMDecryptorProvider.get(this.dekAlgName).decrypt(this.keyBytes, this.iv));
        } catch (IOException e) {
            throw e;
        } catch (Exception e2) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("cannot create extraction operator: ");
            stringBuilder.append(e2.getMessage());
            throw new PEMException(stringBuilder.toString(), e2);
        } catch (Exception e22) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("exception processing key pair: ");
            stringBuilder.append(e22.getMessage());
            throw new PEMException(stringBuilder.toString(), e22);
        }
    }
}
