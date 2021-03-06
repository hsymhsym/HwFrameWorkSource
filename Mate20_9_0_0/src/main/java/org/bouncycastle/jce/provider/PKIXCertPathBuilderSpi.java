package org.bouncycastle.jce.provider;

import java.security.InvalidAlgorithmParameterException;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathBuilderResult;
import java.security.cert.CertPathBuilderSpi;
import java.security.cert.CertPathParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.jcajce.PKIXCertStore;
import org.bouncycastle.jcajce.PKIXCertStoreSelector;
import org.bouncycastle.jcajce.PKIXExtendedBuilderParameters;
import org.bouncycastle.jcajce.PKIXExtendedBuilderParameters.Builder;
import org.bouncycastle.jcajce.PKIXExtendedParameters;
import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;
import org.bouncycastle.jce.exception.ExtCertPathBuilderException;
import org.bouncycastle.x509.ExtendedPKIXBuilderParameters;
import org.bouncycastle.x509.ExtendedPKIXParameters;

public class PKIXCertPathBuilderSpi extends CertPathBuilderSpi {
    private Exception certPathException;

    protected CertPathBuilderResult build(X509Certificate x509Certificate, PKIXExtendedBuilderParameters pKIXExtendedBuilderParameters, List list) {
        CertPathBuilderResult certPathBuilderResult = null;
        if (list.contains(x509Certificate) || pKIXExtendedBuilderParameters.getExcludedCerts().contains(x509Certificate)) {
            return null;
        }
        if (pKIXExtendedBuilderParameters.getMaxPathLength() != -1 && list.size() - 1 > pKIXExtendedBuilderParameters.getMaxPathLength()) {
            return null;
        }
        list.add(x509Certificate);
        try {
            CertificateFactory certificateFactory = new CertificateFactory();
            PKIXCertPathValidatorSpi pKIXCertPathValidatorSpi = new PKIXCertPathValidatorSpi();
            try {
                if (CertPathValidatorUtilities.isIssuerTrustAnchor(x509Certificate, pKIXExtendedBuilderParameters.getBaseParameters().getTrustAnchors(), pKIXExtendedBuilderParameters.getBaseParameters().getSigProvider())) {
                    CertPath engineGenerateCertPath = certificateFactory.engineGenerateCertPath(list);
                    PKIXCertPathValidatorResult pKIXCertPathValidatorResult = (PKIXCertPathValidatorResult) pKIXCertPathValidatorSpi.engineValidate(engineGenerateCertPath, pKIXExtendedBuilderParameters);
                    return new PKIXCertPathBuilderResult(engineGenerateCertPath, pKIXCertPathValidatorResult.getTrustAnchor(), pKIXCertPathValidatorResult.getPolicyTree(), pKIXCertPathValidatorResult.getPublicKey());
                }
                List arrayList = new ArrayList();
                arrayList.addAll(pKIXExtendedBuilderParameters.getBaseParameters().getCertificateStores());
                arrayList.addAll(CertPathValidatorUtilities.getAdditionalStoresFromAltNames(x509Certificate.getExtensionValue(Extension.issuerAlternativeName.getId()), pKIXExtendedBuilderParameters.getBaseParameters().getNamedCertificateStoreMap()));
                Collection hashSet = new HashSet();
                hashSet.addAll(CertPathValidatorUtilities.findIssuerCerts(x509Certificate, pKIXExtendedBuilderParameters.getBaseParameters().getCertStores(), arrayList));
                if (hashSet.isEmpty()) {
                    throw new AnnotatedException("No issuer certificate for certificate in certification path found.");
                }
                Iterator it = hashSet.iterator();
                while (it.hasNext() && certPathBuilderResult == null) {
                    certPathBuilderResult = build((X509Certificate) it.next(), pKIXExtendedBuilderParameters, list);
                }
                if (certPathBuilderResult == null) {
                    list.remove(x509Certificate);
                }
                return certPathBuilderResult;
            } catch (Throwable e) {
                throw new AnnotatedException("No additional X.509 stores can be added from certificate locations.", e);
            } catch (Throwable e2) {
                throw new AnnotatedException("Cannot find issuer certificate for certificate in certification path.", e2);
            } catch (Throwable e22) {
                throw new AnnotatedException("Certification path could not be constructed from certificate list.", e22);
            } catch (Throwable e222) {
                throw new AnnotatedException("Certification path could not be validated.", e222);
            } catch (Exception e3) {
                this.certPathException = e3;
            }
        } catch (Exception e4) {
            throw new RuntimeException("Exception creating support classes.");
        }
    }

    public CertPathBuilderResult engineBuild(CertPathParameters certPathParameters) throws CertPathBuilderException, InvalidAlgorithmParameterException {
        PKIXExtendedBuilderParameters build;
        if (certPathParameters instanceof PKIXBuilderParameters) {
            Builder builder;
            PKIXBuilderParameters pKIXBuilderParameters = (PKIXBuilderParameters) certPathParameters;
            PKIXExtendedParameters.Builder builder2 = new PKIXExtendedParameters.Builder((PKIXParameters) pKIXBuilderParameters);
            if (certPathParameters instanceof ExtendedPKIXParameters) {
                ExtendedPKIXBuilderParameters extendedPKIXBuilderParameters = (ExtendedPKIXBuilderParameters) certPathParameters;
                for (PKIXCertStore addCertificateStore : extendedPKIXBuilderParameters.getAdditionalStores()) {
                    builder2.addCertificateStore(addCertificateStore);
                }
                Builder builder3 = new Builder(builder2.build());
                builder3.addExcludedCerts(extendedPKIXBuilderParameters.getExcludedCerts());
                builder3.setMaxPathLength(extendedPKIXBuilderParameters.getMaxPathLength());
                builder = builder3;
            } else {
                builder = new Builder(pKIXBuilderParameters);
            }
            build = builder.build();
        } else if (certPathParameters instanceof PKIXExtendedBuilderParameters) {
            build = (PKIXExtendedBuilderParameters) certPathParameters;
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Parameters must be an instance of ");
            stringBuilder.append(PKIXBuilderParameters.class.getName());
            stringBuilder.append(" or ");
            stringBuilder.append(PKIXExtendedBuilderParameters.class.getName());
            stringBuilder.append(".");
            throw new InvalidAlgorithmParameterException(stringBuilder.toString());
        }
        List arrayList = new ArrayList();
        PKIXCertStoreSelector targetConstraints = build.getBaseParameters().getTargetConstraints();
        try {
            Collection findCertificates = CertPathValidatorUtilities.findCertificates(targetConstraints, build.getBaseParameters().getCertificateStores());
            findCertificates.addAll(CertPathValidatorUtilities.findCertificates(targetConstraints, build.getBaseParameters().getCertStores()));
            if (findCertificates.isEmpty()) {
                throw new CertPathBuilderException("No certificate found matching targetContraints.");
            }
            CertPathBuilderResult certPathBuilderResult = null;
            Iterator it = findCertificates.iterator();
            while (it.hasNext() && certPathBuilderResult == null) {
                certPathBuilderResult = build((X509Certificate) it.next(), build, arrayList);
            }
            if (certPathBuilderResult != null || this.certPathException == null) {
                if (certPathBuilderResult != null || this.certPathException != null) {
                    return certPathBuilderResult;
                }
                throw new CertPathBuilderException("Unable to find certificate chain.");
            } else if (this.certPathException instanceof AnnotatedException) {
                throw new CertPathBuilderException(this.certPathException.getMessage(), this.certPathException.getCause());
            } else {
                throw new CertPathBuilderException("Possible certificate chain could not be validated.", this.certPathException);
            }
        } catch (Throwable e) {
            throw new ExtCertPathBuilderException("Error finding target certificate.", e);
        }
    }
}
