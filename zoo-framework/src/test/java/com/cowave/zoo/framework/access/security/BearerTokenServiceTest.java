/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.zoo.framework.access.security;

import com.cowave.zoo.framework.access.AccessProperties;
import com.cowave.zoo.framework.access.filter.AccessIdGenerator;
import com.cowave.zoo.framework.configuration.ApplicationProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


/**
 * JWT签名/验签
 *
 * @author shanhuiming
 */
public class BearerTokenServiceTest {

    private ApplicationProperties applicationProperties;
    private AccessIdGenerator accessIdGenerator;
    private AccessProperties.AuthConfig authConfig;
    private AccessProperties accessProperties;

    @BeforeEach
    void setUp() {
        applicationProperties = mock(ApplicationProperties.class);
        accessIdGenerator = mock(AccessIdGenerator.class);
        when(applicationProperties.getName()).thenReturn("test-app");
        when(accessIdGenerator.newAccessId()).thenReturn("test-access-id");

        authConfig = new AccessProperties.AuthConfig();
        authConfig.setEnable(true);

        accessProperties = new AccessProperties();
        accessProperties.setAuth(authConfig);
    }

    private BearerTokenServiceImpl newTokenService() {
        BearerTokenDelegate bearerTokenDelegate = new BearerTokenDelegateImpl(accessProperties, applicationProperties);
        return new BearerTokenServiceImpl(null, new ObjectMapper(), accessIdGenerator, bearerTokenDelegate);
    }

    private AccessUserDetails newUserDetails() {
        AccessUserDetails details = new AccessUserDetails();
        details.setAuthType("password");
        details.setAccessId("test-access-id");
        details.setRefreshId("test-refresh-id");
        details.setTenantId("test-tenant");
        details.setUserId(1L);
        details.setUserCode("U001");
        details.setUsername("testuser");
        details.setUserNick("Test User");
        details.setRoles(java.util.List.of("user"));
        details.setPermissions(java.util.List.of("read", "write"));
        details.setDeptId(1L);
        details.setDeptCode("D001");
        details.setDeptName("Test Dept");
        details.setClusterId(1);
        details.setClusterLevel(1);
        details.setClusterName("Test Cluster");
        return details;
    }

    // ======================== HMAC 签名验签 ========================

    /**
     * HS256
     */
    @Test
    void hmacHS256_signAndVerify() {
        authConfig.setAlgorithm("HS256");
        authConfig.setAccessSecret("my-hs256-secret");
        AccessUserDetails userDetails = newUserDetails();

        BearerTokenServiceImpl tokenService = newTokenService();
        tokenService.assignAccessToken(userDetails);

        String token = userDetails.getAccessToken();
        assertTrue(tokenService.validAccessToken(token));
    }

    /**
     * HS384
     */
    @Test
    void hmacHS384_signAndVerify() {
        authConfig.setAlgorithm("HS384");
        authConfig.setAccessSecret("my-hs384-secret");
        AccessUserDetails userDetails = newUserDetails();

        BearerTokenServiceImpl tokenService = newTokenService();
        tokenService.assignAccessToken(userDetails);

        String token = userDetails.getAccessToken();
        assertTrue(tokenService.validAccessToken(token));
    }

    /**
     * HS512
     */
    @Test
    void hmacHS512_signAndVerify() {
        authConfig.setAlgorithm("HS512");
        authConfig.setAccessSecret("my-hs512-secret");
        AccessUserDetails userDetails = newUserDetails();

        BearerTokenServiceImpl tokenService = newTokenService();
        tokenService.assignAccessToken(userDetails);

        String token = userDetails.getAccessToken();
        assertTrue(tokenService.validAccessToken(token));
    }

    /**
     * 默认HS512
     */
    @Test
    void hmac_defaultAlgorithmIsHS512() {
        authConfig.setAccessSecret("default-secret");
        AccessUserDetails userDetails = newUserDetails();

        BearerTokenServiceImpl service = newTokenService();
        service.assignAccessToken(userDetails);

        String token = userDetails.getAccessToken();
        assertTrue(service.validAccessToken(token));
    }

    // ======================== RSA 签名验签 ========================

    private static KeyPair generateRsaKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private static String toPrivateKeyPem(PrivateKey privateKey) {
        String base64 = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        return "-----BEGIN PRIVATE KEY-----\n" + base64 + "\n-----END PRIVATE KEY-----";
    }

    private static String toPublicKeyPem(PublicKey publicKey) {
        String base64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        return "-----BEGIN PUBLIC KEY-----\n" + base64 + "\n-----END PUBLIC KEY-----";
    }

    /**
     * RS256
     */
    @Test
    void rsaRS256_signAndVerify() throws Exception {
        KeyPair keyPair = generateRsaKeyPair();
        String privateKeyPem = toPrivateKeyPem(keyPair.getPrivate());
        String publicKeyPem = toPublicKeyPem(keyPair.getPublic());
        authConfig.setAlgorithm("RS256");
        authConfig.setAccessPrivateKey(privateKeyPem);
        authConfig.setAccessPublicKey(publicKeyPem);
        AccessUserDetails userDetails = newUserDetails();

        BearerTokenServiceImpl tokenService = newTokenService();
        tokenService.assignAccessToken(userDetails);

        String token = userDetails.getAccessToken();
        assertTrue(tokenService.validAccessToken(token));
    }

    /**
     * RS384
     */
    @Test
    void rsaRS384_signAndVerify() throws Exception {
        KeyPair keyPair = generateRsaKeyPair();
        String privateKeyPem = toPrivateKeyPem(keyPair.getPrivate());
        String publicKeyPem = toPublicKeyPem(keyPair.getPublic());
        authConfig.setAlgorithm("RS384");
        authConfig.setAccessPrivateKey(privateKeyPem);
        authConfig.setAccessPublicKey(publicKeyPem);
        AccessUserDetails userDetails = newUserDetails();

        BearerTokenServiceImpl tokenService = newTokenService();
        tokenService.assignAccessToken(userDetails);

        String token = userDetails.getAccessToken();
        assertTrue(tokenService.validAccessToken(token));
    }

    /**
     * RS512
     */
    @Test
    void rsaRS512_signAndVerify() throws Exception {
        KeyPair keyPair = generateRsaKeyPair();
        String privateKeyPem = toPrivateKeyPem(keyPair.getPrivate());
        String publicKeyPem = toPublicKeyPem(keyPair.getPublic());
        authConfig.setAlgorithm("RS512");
        authConfig.setAccessPrivateKey(privateKeyPem);
        authConfig.setAccessPublicKey(publicKeyPem);
        AccessUserDetails userDetails = newUserDetails();

        BearerTokenServiceImpl tokenService = newTokenService();
        tokenService.assignAccessToken(userDetails);

        String token = userDetails.getAccessToken();
        assertTrue(tokenService.validAccessToken(token));
    }

    // ======================== EC 签名验签 ========================

    private static KeyPair generateEcKeyPair(int keySize) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(keySize);
        return generator.generateKeyPair();
    }

    /**
     * ES256
     */
    @Test
    void ecES256_signAndVerify() throws Exception {
        KeyPair keyPair = generateEcKeyPair(256);
        String privateKeyPem = toPrivateKeyPem(keyPair.getPrivate());
        String publicKeyPem = toPublicKeyPem(keyPair.getPublic());
        authConfig.setAlgorithm("ES256");
        authConfig.setAccessPrivateKey(privateKeyPem);
        authConfig.setAccessPublicKey(publicKeyPem);
        AccessUserDetails userDetails = newUserDetails();

        BearerTokenServiceImpl tokenService = newTokenService();
        tokenService.assignAccessToken(userDetails);

        String token = userDetails.getAccessToken();
        assertTrue(tokenService.validAccessToken(token));
    }

    /**
     * ES384
     */
    @Test
    void ecES384_signAndVerify() throws Exception {
        KeyPair keyPair = generateEcKeyPair(384);
        String privateKeyPem = toPrivateKeyPem(keyPair.getPrivate());
        String publicKeyPem = toPublicKeyPem(keyPair.getPublic());
        authConfig.setAlgorithm("ES384");
        authConfig.setAccessPrivateKey(privateKeyPem);
        authConfig.setAccessPublicKey(publicKeyPem);
        AccessUserDetails userDetails = newUserDetails();

        BearerTokenServiceImpl tokenService = newTokenService();
        tokenService.assignAccessToken(userDetails);

        String token = userDetails.getAccessToken();
        assertTrue(tokenService.validAccessToken(token));
    }

    /**
     * ES512
     */
    @Test
    void ecES512_signAndVerify() throws Exception {
        KeyPair keyPair = generateEcKeyPair(521);
        String privateKeyPem = toPrivateKeyPem(keyPair.getPrivate());
        String publicKeyPem = toPublicKeyPem(keyPair.getPublic());
        authConfig.setAlgorithm("ES512");
        authConfig.setAccessPrivateKey(privateKeyPem);
        authConfig.setAccessPublicKey(publicKeyPem);
        AccessUserDetails userDetails = newUserDetails();

        BearerTokenServiceImpl tokenService = newTokenService();
        tokenService.assignAccessToken(userDetails);

        String token = userDetails.getAccessToken();
        assertTrue(tokenService.validAccessToken(token));
    }

    // ======================== 算法密钥匹配校验 ========================

    @Test
    void invalidAlgorithmName_throwsException() {
        authConfig.setAlgorithm("INVALID_ALG");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, this::newTokenService);
        assertTrue(ex.getMessage().contains("INVALID_ALG") || ex.getMessage().contains("Unsupported"));
    }

    @Test
    void rsaAlgorithm_withEcKey_throwsException() throws Exception {
        KeyPair ecKeyPair = generateEcKeyPair(256);
        String ecPrivatePem = toPrivateKeyPem(ecKeyPair.getPrivate());
        String ecPublicPem = toPublicKeyPem(ecKeyPair.getPublic());

        authConfig.setAlgorithm("RS256"); // RSA 算法
        authConfig.setAccessPrivateKey(ecPrivatePem); // 但配置的是 EC 密钥
        authConfig.setAccessPublicKey(ecPublicPem);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, this::newTokenService);
        assertTrue(ex.getMessage().contains("mismatch") || ex.getMessage().contains("Invalid key"));
    }

    @Test
    void ecAlgorithm_withRsaKey_throwsException() throws Exception {
        KeyPair rsaKeyPair = generateRsaKeyPair();
        String rsaPrivatePem = toPrivateKeyPem(rsaKeyPair.getPrivate());
        String rsaPublicPem = toPublicKeyPem(rsaKeyPair.getPublic());

        authConfig.setAlgorithm("ES256"); // EC 算法
        authConfig.setAccessPrivateKey(rsaPrivatePem); // 但配置的是 RSA 密钥
        authConfig.setAccessPublicKey(rsaPublicPem);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, this::newTokenService);
        assertTrue(ex.getMessage().contains("mismatch") || ex.getMessage().contains("Invalid key"));
    }

    @Test
    void rsaAlgorithm_withoutPrivateKey_throwsAtSignTime() throws Exception {
        KeyPair keyPair = generateRsaKeyPair();
        String publicKeyPem = toPublicKeyPem(keyPair.getPublic());

        authConfig.setAlgorithm("RS256");
        // 只配置公钥，不配置私钥
        authConfig.setAccessPublicKey(publicKeyPem);
        // 构造函数不报错（公钥已配置，用于验签场景）

        BearerTokenServiceImpl service = newTokenService();
        AccessUserDetails userDetails = newUserDetails();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.assignAccessToken(userDetails));
        assertTrue(ex.getMessage().contains("Private key"));
    }

    @Test
    void hmAlgorithm_doesNotRequireKeyPair() {
        authConfig.setAlgorithm("HS512");
        authConfig.setAccessSecret("my-secret");
        // HMAC 不需要公私钥配置，构造函数不抛异常
        BearerTokenServiceImpl service = newTokenService();
        assertNotNull(service);
    }

    // ======================== 文件路径加载 ========================

    @Test
    void fileProtocol_loadsKeyFromFile(@TempDir Path tempDir) throws Exception {
        KeyPair keyPair = generateRsaKeyPair();
        String privateKeyPem = toPrivateKeyPem(keyPair.getPrivate());
        String publicKeyPem = toPublicKeyPem(keyPair.getPublic());

        Path privateKeyFile = tempDir.resolve("private.pem");
        Path publicKeyFile = tempDir.resolve("public.pem");
        Files.writeString(privateKeyFile, privateKeyPem);
        Files.writeString(publicKeyFile, publicKeyPem);

        authConfig.setAlgorithm("RS256");
        authConfig.setAccessPrivateKey("file:" + privateKeyFile.toAbsolutePath());
        authConfig.setAccessPublicKey("file:" + publicKeyFile.toAbsolutePath());
        AccessUserDetails userDetails = newUserDetails();

        BearerTokenServiceImpl tokenService = newTokenService();
        tokenService.assignAccessToken(userDetails);

        String token = userDetails.getAccessToken();
        assertTrue(tokenService.validAccessToken(token));
    }

    @Test
    void classpathProtocol_loadsKeyFromClasspath() throws Exception {
        KeyPair keyPair = generateRsaKeyPair();
        String privateKeyPem = toPrivateKeyPem(keyPair.getPrivate());
        String publicKeyPem = toPublicKeyPem(keyPair.getPublic());

        Path classpathDir = Path.of("target/test-classes/keys");
        Files.createDirectories(classpathDir);
        Files.writeString(classpathDir.resolve("rsa-private.pem"), privateKeyPem);
        Files.writeString(classpathDir.resolve("rsa-public.pem"), publicKeyPem);

        authConfig.setAlgorithm("RS256");
        authConfig.setAccessPrivateKey("classpath:keys/rsa-private.pem");
        authConfig.setAccessPublicKey("classpath:keys/rsa-public.pem");
        AccessUserDetails userDetails = newUserDetails();

        BearerTokenServiceImpl tokenService = newTokenService();
        tokenService.assignAccessToken(userDetails);

        String token = userDetails.getAccessToken();
        assertTrue(tokenService.validAccessToken(token));
    }

    // ======================== 无效 token 拒绝 ========================

    @Test
    void tamperedToken_isRejected() throws Exception {
        KeyPair keyPair = generateRsaKeyPair();
        authConfig.setAlgorithm("RS256");
        authConfig.setAccessPrivateKey(toPrivateKeyPem(keyPair.getPrivate()));
        authConfig.setAccessPublicKey(toPublicKeyPem(keyPair.getPublic()));
        AccessUserDetails userDetails = newUserDetails();

        BearerTokenServiceImpl tokenService = newTokenService();
        tokenService.assignAccessToken(userDetails);

        String token = userDetails.getAccessToken();
        // 篡改 payload 中间的一个字符，让签名对不上
        String[] parts = token.split("\\.");
        String tamperedPayload = parts[1].substring(0, parts[1].length() / 2)
                + "X" + parts[1].substring(parts[1].length() / 2 + 1);
        String tamperedToken = parts[0] + "." + tamperedPayload + "." + parts[2];
        assertFalse(tokenService.validAccessToken(tamperedToken));
    }

    // ======================== refresh token 支持 ========================

    @Test
    void rsaRefreshToken_signAndVerify() throws Exception {
        KeyPair accessKeyPair = generateRsaKeyPair();
        KeyPair refreshKeyPair = generateRsaKeyPair();
        authConfig.setAlgorithm("RS256");
        authConfig.setAccessPrivateKey(toPrivateKeyPem(accessKeyPair.getPrivate()));
        authConfig.setAccessPublicKey(toPublicKeyPem(accessKeyPair.getPublic()));
        authConfig.setRefreshPrivateKey(toPrivateKeyPem(refreshKeyPair.getPrivate()));
        authConfig.setRefreshPublicKey(toPublicKeyPem(refreshKeyPair.getPublic()));
        AccessUserDetails userDetails = newUserDetails();

        BearerTokenServiceImpl tokenService = newTokenService();
        tokenService.assignAccessRefreshToken(userDetails);
        String accessToken = userDetails.getAccessToken();
        String refreshToken = userDetails.getRefreshToken();
        assertNotNull(accessToken);
        assertNotNull(refreshToken);
        assertTrue(tokenService.validAccessToken(accessToken));
    }
}
