package org.swasth;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.swasth.service.EncDeCode;
import org.swasth.service.VerifyQRCode;
import org.swasth.utils.JWSUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HcxQRCodeGenerator {
    private static int width;
    private static int height;
    private static String privatekey;

    static {
        try {
            loadConfig();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadConfig() throws Exception {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = HcxQRCodeGenerator.class.getResourceAsStream("/application.yml")) {
            Map<String, Object> config = yaml.load(inputStream);
            Map<String, Object> qrCodeConfig = (Map<String, Object>) config.get("qr_code");
            width = parseWidthHeight((String) qrCodeConfig.get("width"));
            height = parseWidthHeight((String) qrCodeConfig.get("height"));
            privatekey = resolvePlaceholder((String) qrCodeConfig.get("private_key"));
        }
    }

    private static int parseWidthHeight(String value) {
        if (value.startsWith("${") && value.endsWith("}")) {
            Pattern pattern = Pattern.compile("\\$\\{(.+?):(\\d+)}");
            Matcher matcher = pattern.matcher(value);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(2));
            }
        }
        return Integer.parseInt(value);
    }

    private static String resolvePlaceholder(String value) {
        if (value.startsWith("${") && value.endsWith("}")) {
            int colonIndex = value.indexOf(':');
            if (colonIndex != -1) {
                return value.substring(colonIndex + 1, value.length() - 1);
            }
        }
        return value;
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            String json = args[0];
            Gson gson = new Gson();
            Map<String, Object> map = gson.fromJson(json, HashMap.class);
            System.out.println("Map received from command line argument:");
            String certificate = IOUtils.toString(new URI(privatekey), StandardCharsets.UTF_8);
            generateQrToken((Map<String, Object>) map.get("payload"), certificate);
        } else {
            System.out.println("No input to process");
        }
    }

    private static String getPrivateKey(String privateKey) {
        privateKey = privateKey
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        return privateKey;
    }

    private static String generateQrToken(Map<String, Object> requestBody, String privateKey) throws Exception {
        Map<String, Object> headers = new HashMap<>();
        String jwsToken = JWSUtils.generate(headers, requestBody, HcxQRCodeGenerator.getPrivateKey(privateKey));
        String participantCode = null;
        if (requestBody.containsKey("participantCode")) {
            participantCode = (String) requestBody.get("participantCode");
        }
        String payload = createVerifiableCredential(requestBody, jwsToken);
        generateQRCode(EncDeCode.encodePayload(payload), participantCode);
        return payload;
    }
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static String createVerifiableCredential(Map<String, Object> payload, String proofValue) throws Exception {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
        cfg.setClassForTemplateLoading(HcxQRCodeGenerator.class, "/templates");
        Template template = cfg.getTemplate("verifiable-credential.ftl");
        LocalDateTime issuanceDate = LocalDateTime.now();
        LocalDateTime expirationDate = LocalDateTime.now().plusYears(1);
        Map<String, Object> data = new HashMap<>();
        data.put("issuanceDate", formatter.format(issuanceDate));
        data.put("expirationDate", formatter.format(expirationDate));
        data.put("subjectId", UUID.randomUUID());
        data.put("payload", new Gson().toJson(payload));
        data.put("proofCreated", LocalDateTime.now());
        data.put("proofValue", proofValue);
        StringWriter out = new StringWriter();
        template.process(data, out);
        System.out.println(out);
        return out.toString();
    }

    private static void generateQRCode(String content, String participantCode) throws Exception {
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height);
        String currentDir = System.getProperty("user.dir");
        Path path = FileSystems.getDefault().getPath(currentDir + "/" + participantCode + "_qr_code_" + System.currentTimeMillis() + ".png");
        MatrixToImageWriter.writeToPath(matrix, "PNG", path);
        System.out.println("QR code image generated and saved to: " + path.toAbsolutePath());
    }
}
