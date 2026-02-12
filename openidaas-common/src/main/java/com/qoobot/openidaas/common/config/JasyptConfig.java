package com.qoobot.openidaas.common.config;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jasypt加密配置
 *
 * @author QooBot
 */
@Configuration
@EnableEncryptableProperties
public class JasyptConfig {

    @Value("${jasypt.encryptor.password:}")
    private String encryptorPassword;

    @Value("${jasypt.encryptor.algorithm:PBEWithHMACSHA512AndAES_256}")
    private String algorithm;

    @Value("${jasypt.encryptor.key-obtention-iterations:1000}")
    private Integer iterations;

    @Value("${jasypt.encryptor.pool-size:4}")
    private Integer poolSize;

    @Value("${jasypt.encryptor.salt-generator-classname:org.jasypt.salt.RandomSaltGenerator}")
    private String saltGeneratorClassname;

    @Value("${jasypt.encryptor.iv-generator-classname:org.jasypt.iv.RandomIvGenerator}")
    private String ivGeneratorClassname;

    @Bean
    public PooledPBEStringEncryptor jasyptStringEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();

        // 从环境变量或配置中获取加密密码
        String password = encryptorPassword;
        if (password == null || password.isEmpty()) {
            password = System.getenv("JASYPT_ENCRYPTOR_PASSWORD");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalStateException("Jasypt encryptor password must be set via configuration or environment variable JASYPT_ENCRYPTOR_PASSWORD");
        }

        config.setPassword(password);
        config.setAlgorithm(algorithm);
        config.setKeyObtentionIterations(iterations);
        config.setPoolSize(poolSize);
        config.setSaltGeneratorClassName(saltGeneratorClassname);
        config.setIvGeneratorClassName(ivGeneratorClassname);
        config.setStringOutputType("hexadecimal");

        encryptor.setConfig(config);
        return encryptor;
    }
}
