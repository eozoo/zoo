package com.cowave.zoo.framework.access.security;

import com.cowave.zoo.framework.access.Access;
import com.cowave.zoo.framework.access.AccessProperties;
import com.cowave.zoo.framework.configuration.ApplicationProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * @author shanhuiming
 */
public class BearerTokenDelegateImpl implements BearerTokenDelegate {

    private final AccessProperties accessProperties;

    private final ApplicationProperties applicationProperties;

    public BearerTokenDelegateImpl(AccessProperties accessProperties, ApplicationProperties applicationProperties) {
        this.accessProperties = accessProperties;
        this.applicationProperties = applicationProperties;
        this.validateConfiguration();
    }

    private void validateConfiguration() {
        if (!accessProperties.authEnable()) {
            return;
        }
        SignatureAlgorithm algorithm = resolveAlgorithm(accessProperties.algorithm());
        if (algorithm.isHmac()) {
            // HMAC 算法不需要公私钥
            return;
        }

        // 非对称算法：校验 access token 密钥
        String accessPubKey = accessProperties.accessPublicKey();
        String accessPriKey = accessProperties.accessPrivateKey();
        if (StringUtils.isBlank(accessPriKey) && StringUtils.isBlank(accessPubKey)) {
            return; // 均未配置，跳过校验
        }
        if (StringUtils.isNotBlank(accessPriKey)) {
            validateKey(accessPriKey, algorithm, "accessPrivateKey", true);
        }
        if (StringUtils.isNotBlank(accessPubKey)) {
            validateKey(accessPubKey, algorithm, "accessPublicKey", false);
        }

        // 非对称算法：校验 refresh token 密钥
        String refreshPubKey = accessProperties.refreshPublicKey();
        String refreshPriKey = accessProperties.refreshPrivateKey();
        if (StringUtils.isNotBlank(refreshPriKey)) {
            validateKey(refreshPriKey, algorithm, "refreshPrivateKey", true);
        }
        if (StringUtils.isNotBlank(refreshPubKey)) {
            validateKey(refreshPubKey, algorithm, "refreshPublicKey", false);
        }
    }

    private void validateKey(String keyConfig, SignatureAlgorithm expectedAlg, String keyName, boolean isPrivate) {
        String keyContent = resolveKeyContent(keyConfig);
        if (StringUtils.isBlank(keyContent)) {
            throw new IllegalArgumentException("Failed to resolve key content for " + keyName + ": " + keyConfig);
        }
        Key key;
        try {
            if (isPrivate) {
                key = parsePrivateKey(keyContent, expectedAlg);
            } else {
                key = parsePublicKey(keyContent, expectedAlg);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Invalid key for " + keyName + " with algorithm " + expectedAlg.name()
                            + ": key algorithm mismatch or invalid PEM format", e);
        }
        // 校验密钥算法族与 JWT 算法族是否匹配
        String expectedKeyAlg = toKeyFactoryAlgorithm(expectedAlg);
        String keyAlg = key.getAlgorithm();
        if (!expectedKeyAlg.equalsIgnoreCase(keyAlg)) {
            throw new IllegalArgumentException("Key algorithm mismatch for " + keyName
                    + ": expected " + expectedKeyAlg + " but key is " + keyAlg);
        }
    }

    @Override
    public String tokenStore() {
        return accessProperties.tokenStore();
    }

    @Override
    public String tokenKey() {
        return accessProperties.tokenKey();
    }

    @Override
    public AuthMode authMode() {
        return accessProperties.authMode();
    }

    @Override
    public String oauthAppId() {
        return accessProperties.oauthAppId();
    }

    @Override
    public boolean alwaysReturnHttp200() {
        return accessProperties.isAlwaysSuccess();
    }

    @Override
    public SignatureAlgorithm getAccessAlgorithm() {
        return resolveAlgorithm(accessProperties.algorithm());
    }

    @Override
    public Key getAccessSigningKey(SignatureAlgorithm algorithm) {
        return resolveSigningKey(accessProperties.accessSecret(), accessProperties.accessPrivateKey(), algorithm);
    }

    @Override
    public Key getAccessVerificationKey(SignatureAlgorithm algorithm) {
        return resolveVerificationKey(accessProperties.accessSecret(), accessProperties.accessPublicKey(), algorithm);
    }

    @Override
    public String getAccessIssuer() {
        return applicationProperties.getName();
    }

    @Override
    public Integer getAccessExpireSeconds() {
        return accessProperties.accessExpire();
    }

    @Override
    public void setAccessClaims(JwtBuilder jwtBuilder, AccessUserDetails userDetails) {
        jwtBuilder.claim(CLAIM_REFRESH_ID, userDetails.getRefreshId())
                .claim(CLAIM_ACCESS_IP, Access.accessIp())
                .claim(CLAIM_ACCESS_UNIQUE, userDetails.isAccessUnique() ? 1 : 0)
                .claim(CLAIM_ACCESS_VALID, userDetails.isAccessValid() ? 1 : 0)
                .claim(CLAIM_TYPE, userDetails.getAuthType())
                .claim(CLAIM_ACCESS_ID, userDetails.getAccessId())
                .claim(CLAIM_TENANT_ID, userDetails.getTenantId())
                .claim(CLAIM_USER_ID, userDetails.getUserId())
                .claim(CLAIM_USER_CODE, userDetails.getUserCode())
                .claim(CLAIM_USER_PROPERTIES, userDetails.getUserProperties())
                .claim(CLAIM_USER_TYPE, userDetails.getUserType())
                .claim(CLAIM_USER_NAME, userDetails.getUserNick())
                .claim(CLAIM_USER_ACCOUNT, userDetails.getUsername())
                .claim(CLAIM_USER_ROLE, userDetails.getRoles())
                .claim(CLAIM_USER_PERM, userDetails.getPermissions())
                .claim(CLAIM_DEPT_ID, userDetails.getDeptId())
                .claim(CLAIM_DEPT_CODE, userDetails.getDeptCode())
                .claim(CLAIM_DEPT_NAME, userDetails.getDeptName())
                .claim(CLAIM_CLUSTER_ID, userDetails.getClusterId())
                .claim(CLAIM_CLUSTER_LEVEL, userDetails.getClusterLevel())
                .claim(CLAIM_CLUSTER_NAME, userDetails.getClusterName());
    }

    @Override
    public void setOauthAccessClaims(JwtBuilder jwtBuilder, AccessUserDetails userDetails) {
        setAccessClaims(jwtBuilder, userDetails);
        jwtBuilder.claim(CLAIM_OAUTH_ID, userDetails.getOauthId())
                .claim(CLAIM_OAUTH_NAME, userDetails.getOauthName());
    }

    @Override
    public AccessUserDetails parseAccessClaims(Claims claims) {
        AccessUserDetails userDetails = new AccessUserDetails();
        userDetails.setOauthId((String) claims.get(CLAIM_OAUTH_ID));
        // token
        userDetails.setAccessUnique(1 == (Integer) claims.get(CLAIM_ACCESS_UNIQUE));
        userDetails.setAccessValid(1 == (Integer) claims.get(CLAIM_ACCESS_VALID));
        userDetails.setAuthType((String) claims.get(CLAIM_TYPE));
        userDetails.setAccessIp((String) claims.get(CLAIM_ACCESS_IP));
        userDetails.setAccessId((String) claims.get(CLAIM_ACCESS_ID));
        userDetails.setRefreshId((String) claims.get(CLAIM_REFRESH_ID));
        userDetails.setTenantId((String) claims.get(CLAIM_TENANT_ID));
        // user
        userDetails.setUserId(claims.get(CLAIM_USER_ID));
        userDetails.setUserCode(claims.get(CLAIM_USER_CODE));
        userDetails.setUsername((String) claims.get(CLAIM_USER_ACCOUNT));
        userDetails.setUserNick((String) claims.get(CLAIM_USER_NAME));
        userDetails.setUserProperties((Map<String, Object>) claims.get(CLAIM_USER_PROPERTIES));
        // dept
        userDetails.setDeptId(claims.get(CLAIM_DEPT_ID));
        userDetails.setDeptCode(claims.get(CLAIM_DEPT_CODE));
        userDetails.setDeptName((String) claims.get(CLAIM_DEPT_NAME));
        // cluster
        userDetails.setClusterId((Integer) claims.get(CLAIM_CLUSTER_ID));
        userDetails.setClusterLevel((Integer) claims.get(CLAIM_CLUSTER_LEVEL));
        userDetails.setClusterName((String) claims.get(CLAIM_CLUSTER_NAME));
        // roles
        userDetails.setRoles((List<String>) claims.get(CLAIM_USER_ROLE));
        // permits
        userDetails.setPermissions((List<String>) claims.get(CLAIM_USER_PERM));
        return userDetails;
    }

    @Override
    public SignatureAlgorithm getRefreshAlgorithm() {
        return resolveAlgorithm(accessProperties.algorithm());
    }

    @Override
    public Key getRefreshSigningKey(SignatureAlgorithm algorithm) {
        return resolveSigningKey(accessProperties.refreshSecret(), accessProperties.refreshPrivateKey(), algorithm);
    }

    @Override
    public Key getRefreshVerificationKey(SignatureAlgorithm algorithm) {
        return resolveVerificationKey(accessProperties.refreshSecret(), accessProperties.refreshPublicKey(), algorithm);
    }

    @Override
    public String getRefreshIssuer() {
        return applicationProperties.getName();
    }

    @Override
    public Integer getRefreshExpireSeconds() {
        return accessProperties.refreshExpire();
    }

    @Override
    public void setRefreshClaims(JwtBuilder jwtBuilder, AccessUserDetails userDetails) {
        jwtBuilder.claim(CLAIM_ACCESS_UNIQUE, userDetails.isAccessUnique() ? 1 : 0)
                .claim(CLAIM_ACCESS_VALID, userDetails.isAccessValid() ? 1 : 0)
                .claim(CLAIM_TYPE, userDetails.getAuthType())
                .claim(CLAIM_REFRESH_ID, userDetails.getRefreshId())
                .claim(CLAIM_USER_ACCOUNT, userDetails.getUsername())
                .claim(CLAIM_TENANT_ID, userDetails.getTenantId());
    }

    @Override
    public void setOauthRefreshClaims(JwtBuilder jwtBuilder, AccessUserDetails userDetails) {
        setRefreshClaims(jwtBuilder, userDetails);
        jwtBuilder.claim(CLAIM_OAUTH_ID, userDetails.getOauthId())
                .claim(CLAIM_OAUTH_NAME, userDetails.getOauthName());
    }

    @Override
    public AccessUserDetails parseRefreshClaims(Claims claims) {
        AccessUserDetails tokenDetails = new AccessUserDetails();
        tokenDetails.setTenantId((String) claims.get(CLAIM_TENANT_ID));
        tokenDetails.setAuthType((String) claims.get(CLAIM_TYPE));
        tokenDetails.setUsername((String) claims.get(CLAIM_USER_ACCOUNT));
        tokenDetails.setRefreshId((String) claims.get(CLAIM_REFRESH_ID));
        tokenDetails.setAccessUnique(1 == (Integer) claims.get(CLAIM_ACCESS_UNIQUE));
        tokenDetails.setAccessValid(1 == (Integer) claims.get(CLAIM_ACCESS_VALID));
        return tokenDetails;
    }

    @Override
    public AccessUserDetails parseOauthRefreshClaims(Claims claims) {
        AccessUserDetails tokenDetails = new AccessUserDetails();
        tokenDetails.setTenantId((String) claims.get(CLAIM_TENANT_ID));
        tokenDetails.setAuthType((String) claims.get(CLAIM_TYPE));
        tokenDetails.setUsername((String) claims.get(CLAIM_USER_ACCOUNT));
        tokenDetails.setRefreshId((String) claims.get(CLAIM_REFRESH_ID));
        tokenDetails.setAccessUnique(1 == (Integer) claims.get(CLAIM_ACCESS_UNIQUE));
        tokenDetails.setOauthId((String) claims.get(CLAIM_OAUTH_ID));
        return tokenDetails;
    }

    // 获取密钥配置
    private String resolveKeyContent(String keyConfig) {
        if (StringUtils.isBlank(keyConfig)) {
            return null;
        }
        // 配置的file路径
        if (keyConfig.startsWith("file:")) {
            try {
                return Files.readString(Paths.get(keyConfig.substring(5)));
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to read key file: " + keyConfig, e);
            }
        }
        // 配置的classpath路径
        if (keyConfig.startsWith("classpath:")) {
            String classpath = keyConfig.substring(10);
            // 确保 classpath 路径不以 / 开头
            if (classpath.startsWith("/")) {
                classpath = classpath.substring(1);
            }

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) {
                classLoader = getClass().getClassLoader();
            }
            try (InputStream is = classLoader.getResourceAsStream(classpath)) {
                if (is == null) {
                    throw new IllegalArgumentException("Key file not found in classpath: " + keyConfig);
                }
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to read key from classpath: " + keyConfig, e);
            }
        }
        // 直接配置的PEM字符串，做一个基本校验
        if (!keyConfig.contains("-----BEGIN")) {
            throw new IllegalArgumentException(
                    "Key config does not look like a PEM key or a valid path prefix. "
                    + "Use 'file:' for filesystem paths, 'classpath:' for classpath resources, "
                    + "or provide the PEM key content directly. Input: " + keyConfig.substring(0, Math.min(80, keyConfig.length())) + "...");
        }
        return keyConfig;
    }

    private SignatureAlgorithm resolveAlgorithm(String algorithm) {
        if (StringUtils.isBlank(algorithm)) {
            return SignatureAlgorithm.HS512;
        }
        try {
            return SignatureAlgorithm.forName(algorithm);
        } catch (SignatureException e) {
            throw new IllegalArgumentException("Unsupported JWT algorithm: " + algorithm, e);
        }
    }

    // 签名的私钥
    private Key resolveSigningKey(String secret, String privateKeyConfig, SignatureAlgorithm algorithm) {
        // HMAC
        if (algorithm.isHmac()) {
            byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            return new SecretKeySpec(keyBytes, algorithm.getJcaName());
        }
        // RSA or EC
        String privateKeyContent = resolveKeyContent(privateKeyConfig);
        if (StringUtils.isBlank(privateKeyContent)) {
            throw new IllegalStateException("Private key must be configured for algorithm: " + algorithm.name());
        }
        return parsePrivateKey(privateKeyContent, algorithm);
    }

    // 验证签名的公钥
    private Key resolveVerificationKey(String secret, String publicKeyConfig, SignatureAlgorithm algorithm) {
        // HMAC
        if (algorithm.isHmac()) {
            byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            return new SecretKeySpec(keyBytes, algorithm.getJcaName());
        }
        // RSA or EC
        String publicKeyContent = resolveKeyContent(publicKeyConfig);
        if (StringUtils.isBlank(publicKeyContent)) {
            throw new IllegalStateException("Public key must be configured for algorithm: " + algorithm.name());
        }
        return parsePublicKey(publicKeyContent, algorithm);
    }

    // 构造私钥
    private PrivateKey parsePrivateKey(String pemContent, SignatureAlgorithm algorithm) {
        // 检查是否为PKCS#1格式，不支持，给出明确的错误提示
        if (pemContent.contains("-----BEGIN RSA PRIVATE KEY-----")
                || pemContent.contains("-----BEGIN EC PRIVATE KEY-----")) {
            throw new IllegalArgumentException(
                    "PKCS#1 format private key is not supported. "
                    + "Please convert it to PKCS#8 format using: "
                    + "openssl pkcs8 -topk8 -inform PEM -outform PEM -in <key.pem> -out <key_pkcs8.pem> -nocrypt");
        }
        String base64 = pemContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(toKeyFactoryAlgorithm(algorithm));
            return keyFactory.generatePrivate(spec);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse private key for " + algorithm.name(), e);
        }
    }

    // 构造公钥
    private PublicKey parsePublicKey(String pemContent, SignatureAlgorithm algorithm) {
        // 检查是否为PKCS#1格式，不支持，给出明确的错误提示
        if (pemContent.contains("-----BEGIN RSA PUBLIC KEY-----")) {
            throw new IllegalArgumentException(
                    "PKCS#1 format public key is not supported. "
                    + "Please convert it to X.509 (SubjectPublicKeyInfo) format using: "
                    + "openssl rsa -pubin -in <key.pem> -outform PEM -out <key_spki.pem>");
        }
        String base64 = pemContent
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance(toKeyFactoryAlgorithm(algorithm));
            return kf.generatePublic(spec);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse public key for " + algorithm.name(), e);
        }
    }

    private static String toKeyFactoryAlgorithm(SignatureAlgorithm algorithm) {
        if (algorithm.isRsa()) {
            return "RSA";
        }
        if (algorithm.isEllipticCurve()) {
            return "EC";
        }
        throw new IllegalArgumentException("Unsupported key algorithm: " + algorithm.name());
    }
}
