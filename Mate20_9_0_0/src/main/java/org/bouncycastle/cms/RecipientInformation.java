package org.bouncycastle.cms;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.util.io.Streams;

public abstract class RecipientInformation {
    private AuthAttributesProvider additionalData;
    protected AlgorithmIdentifier keyEncAlg;
    protected AlgorithmIdentifier messageAlgorithm;
    private RecipientOperator operator;
    private byte[] resultMac;
    protected RecipientId rid;
    protected CMSSecureReadable secureReadable;

    RecipientInformation(AlgorithmIdentifier algorithmIdentifier, AlgorithmIdentifier algorithmIdentifier2, CMSSecureReadable cMSSecureReadable, AuthAttributesProvider authAttributesProvider) {
        this.keyEncAlg = algorithmIdentifier;
        this.messageAlgorithm = algorithmIdentifier2;
        this.secureReadable = cMSSecureReadable;
        this.additionalData = authAttributesProvider;
    }

    private byte[] encodeObj(ASN1Encodable aSN1Encodable) throws IOException {
        return aSN1Encodable != null ? aSN1Encodable.toASN1Primitive().getEncoded() : null;
    }

    public byte[] getContent(Recipient recipient) throws CMSException {
        try {
            return CMSUtils.streamToByteArray(getContentStream(recipient).getContentStream());
        } catch (Exception e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("unable to parse internal stream: ");
            stringBuilder.append(e.getMessage());
            throw new CMSException(stringBuilder.toString(), e);
        }
    }

    public byte[] getContentDigest() {
        return this.secureReadable instanceof CMSDigestAuthenticatedSecureReadable ? ((CMSDigestAuthenticatedSecureReadable) this.secureReadable).getDigest() : null;
    }

    public CMSTypedStream getContentStream(Recipient recipient) throws CMSException, IOException {
        this.operator = getRecipientOperator(recipient);
        return this.additionalData != null ? new CMSTypedStream(this.secureReadable.getInputStream()) : new CMSTypedStream(this.operator.getInputStream(this.secureReadable.getInputStream()));
    }

    public String getKeyEncryptionAlgOID() {
        return this.keyEncAlg.getAlgorithm().getId();
    }

    public byte[] getKeyEncryptionAlgParams() {
        try {
            return encodeObj(this.keyEncAlg.getParameters());
        } catch (Exception e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("exception getting encryption parameters ");
            stringBuilder.append(e);
            throw new RuntimeException(stringBuilder.toString());
        }
    }

    public AlgorithmIdentifier getKeyEncryptionAlgorithm() {
        return this.keyEncAlg;
    }

    public byte[] getMac() {
        if (this.resultMac == null && this.operator.isMacBased()) {
            if (this.additionalData != null) {
                try {
                    Streams.drain(this.operator.getInputStream(new ByteArrayInputStream(this.additionalData.getAuthAttributes().getEncoded(ASN1Encoding.DER))));
                } catch (IOException e) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("unable to drain input: ");
                    stringBuilder.append(e.getMessage());
                    throw new IllegalStateException(stringBuilder.toString());
                }
            }
            this.resultMac = this.operator.getMac();
        }
        return this.resultMac;
    }

    public RecipientId getRID() {
        return this.rid;
    }

    protected abstract RecipientOperator getRecipientOperator(Recipient recipient) throws CMSException, IOException;
}
