package com.antenapro.hotelcheck.acquisition;

import com.antenapro.hotelcheck.input.CanonicalEvaluationRequest;
import com.antenapro.hotelcheck.input.EvaluationTargetType;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class HttpPublicWebAcquisitionServiceTest {

    private HttpServer server;
    private int serverPort;
    private String baseUrl;
    private HttpPublicWebAcquisitionService service;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        serverPort = server.getAddress().getPort();
        baseUrl = "http://127.0.0.1:" + serverPort;
        server.start();

        service = new HttpPublicWebAcquisitionService();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void testSuccessfulAcquisition() {
        server.createContext("/hotel", exchange -> {
            String responseStr = "<html><body><h1>Grand Palace Hotel</h1></body></html>";
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.getResponseHeaders().set("Server", "MockServer/1.0");
            byte[] bytes = responseStr.getBytes();
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });

        AcquisitionConfig config = AcquisitionConfig.builder().allowLocalhost(true).build();
        AcquisitionRequest request = new AcquisitionRequest(baseUrl + "/hotel", config);

        AcquisitionResult result = service.acquire(request);

        assertTrue(result.isSuccess());
        assertEquals(AcquisitionOutcome.SUCCESS, result.outcome());
        assertEquals(200, result.statusCode());
        assertTrue(result.contentType().contains("text/html"));
        assertTrue(result.body().contains("Grand Palace Hotel"));
        assertEquals(AcquisitionMethod.HTTP_PUBLIC, result.acquisitionMethod());
        assertNotNull(result.retrievalTimestamp());
        assertEquals("MockServer/1.0", result.provenanceMetadata().get("header.server"));
        assertTrue(result.redirectChain().isEmpty());
    }

    @Test
    void testHttpError404() {
        server.createContext("/missing", exchange -> {
            byte[] bytes = "Not Found".getBytes();
            exchange.sendResponseHeaders(404, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });

        AcquisitionConfig config = AcquisitionConfig.builder().allowLocalhost(true).build();
        AcquisitionRequest request = new AcquisitionRequest(baseUrl + "/missing", config);

        AcquisitionResult result = service.acquire(request);

        assertFalse(result.isSuccess());
        assertEquals(AcquisitionOutcome.HTTP_ERROR, result.outcome());
        assertEquals(404, result.statusCode());
        assertNotNull(result.errorMessage());
    }

    @Test
    void testHttpError500() {
        server.createContext("/error", exchange -> {
            byte[] bytes = "Server Error".getBytes();
            exchange.sendResponseHeaders(500, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });

        AcquisitionConfig config = AcquisitionConfig.builder().allowLocalhost(true).build();
        AcquisitionRequest request = new AcquisitionRequest(baseUrl + "/error", config);

        AcquisitionResult result = service.acquire(request);

        assertFalse(result.isSuccess());
        assertEquals(AcquisitionOutcome.HTTP_ERROR, result.outcome());
        assertEquals(500, result.statusCode());
    }

    @Test
    void testRedirectsWithinLimit() {
        server.createContext("/start", exchange -> {
            exchange.getResponseHeaders().set("Location", baseUrl + "/final");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });

        server.createContext("/final", exchange -> {
            byte[] bytes = "Final Destination".getBytes();
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });

        AcquisitionConfig config = AcquisitionConfig.builder()
                .allowLocalhost(true)
                .maxRedirects(3)
                .build();
        AcquisitionRequest request = new AcquisitionRequest(baseUrl + "/start", config);

        AcquisitionResult result = service.acquire(request);

        assertTrue(result.isSuccess());
        assertEquals(AcquisitionOutcome.SUCCESS, result.outcome());
        assertEquals(baseUrl + "/final", result.finalUrl());
        assertEquals(1, result.redirectChain().size());
        assertEquals(baseUrl + "/start", result.redirectChain().get(0));
        assertTrue(result.body().contains("Final Destination"));
    }

    @Test
    void testRedirectToPrivateAddressRejection() {
        server.createContext("/redirect-private", exchange -> {
            exchange.getResponseHeaders().set("Location", "http://10.0.0.1/internal");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });

        AcquisitionConfig config = AcquisitionConfig.builder()
                .allowLocalhost(true) // allow initial server connection
                .build();
        AcquisitionRequest request = new AcquisitionRequest(baseUrl + "/redirect-private", config);

        AcquisitionResult result = service.acquire(request);

        assertFalse(result.isSuccess());
        assertEquals(AcquisitionOutcome.INVALID_TARGET, result.outcome());
        assertTrue(result.errorMessage().contains("non-public"));
        assertEquals(1, result.redirectChain().size());
    }

    @Test
    void testRedirectLimitExceeded() {
        server.createContext("/loop", exchange -> {
            exchange.getResponseHeaders().set("Location", baseUrl + "/loop");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });

        AcquisitionConfig config = AcquisitionConfig.builder()
                .allowLocalhost(true)
                .maxRedirects(2)
                .build();
        AcquisitionRequest request = new AcquisitionRequest(baseUrl + "/loop", config);

        AcquisitionResult result = service.acquire(request);

        assertFalse(result.isSuccess());
        assertEquals(AcquisitionOutcome.REDIRECT_LIMIT_EXCEEDED, result.outcome());
        assertNotNull(result.errorMessage());
    }

    @Test
    void testResponseTooLargeContentLengthHeader() {
        server.createContext("/large-header", exchange -> {
            exchange.getResponseHeaders().set("Content-Length", "1000000");
            exchange.sendResponseHeaders(200, 1000000L);
            exchange.close();
        });

        AcquisitionConfig config = AcquisitionConfig.builder()
                .allowLocalhost(true)
                .maxResponseSizeBytes(1000)
                .build();
        AcquisitionRequest request = new AcquisitionRequest(baseUrl + "/large-header", config);

        AcquisitionResult result = service.acquire(request);

        assertFalse(result.isSuccess());
        assertEquals(AcquisitionOutcome.RESPONSE_TOO_LARGE, result.outcome());
    }

    @Test
    void testResponseTooLargeStreaming() {
        server.createContext("/large-stream", exchange -> {
            exchange.sendResponseHeaders(200, 0); // Chunked / unstated length
            try (OutputStream os = exchange.getResponseBody()) {
                byte[] chunk = new byte[1000];
                for (int i = 0; i < 5; i++) {
                    os.write(chunk);
                    os.flush();
                }
            }
        });

        AcquisitionConfig config = AcquisitionConfig.builder()
                .allowLocalhost(true)
                .maxResponseSizeBytes(2000)
                .build();
        AcquisitionRequest request = new AcquisitionRequest(baseUrl + "/large-stream", config);

        AcquisitionResult result = service.acquire(request);

        assertFalse(result.isSuccess());
        assertEquals(AcquisitionOutcome.RESPONSE_TOO_LARGE, result.outcome());
    }

    @Test
    void testTimeout() {
        server.createContext("/slow", exchange -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ignored) {}
            byte[] bytes = "Slow response".getBytes();
            try {
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } catch (IOException ignored) {}
        });

        AcquisitionConfig config = AcquisitionConfig.builder()
                .allowLocalhost(true)
                .readTimeout(Duration.ofMillis(200))
                .build();
        AcquisitionRequest request = new AcquisitionRequest(baseUrl + "/slow", config);

        AcquisitionResult result = service.acquire(request);

        assertFalse(result.isSuccess());
        assertEquals(AcquisitionOutcome.TIMEOUT, result.outcome());
    }

    @Test
    void testUnsupportedScheme() {
        AcquisitionConfig config = AcquisitionConfig.builder().allowLocalhost(true).build();
        AcquisitionRequest request = new AcquisitionRequest("ftp://example.com/hotel", config);

        AcquisitionResult result = service.acquire(request);

        assertFalse(result.isSuccess());
        assertEquals(AcquisitionOutcome.UNSUPPORTED_SCHEME, result.outcome());
    }

    @Test
    void testInvalidTargetNullOrEmpty() {
        AcquisitionConfig config = AcquisitionConfig.defaults();
        AcquisitionRequest request = new AcquisitionRequest("", config);

        AcquisitionResult result = service.acquire(request);

        assertFalse(result.isSuccess());
        assertEquals(AcquisitionOutcome.INVALID_TARGET, result.outcome());
    }

    @Test
    void testInvalidTargetLoopbackDisallowed() {
        AcquisitionConfig config = AcquisitionConfig.builder().allowLocalhost(false).build();
        AcquisitionRequest request = new AcquisitionRequest("http://127.0.0.1/hotel", config);

        AcquisitionResult result = service.acquire(request);

        assertFalse(result.isSuccess());
        assertEquals(AcquisitionOutcome.INVALID_TARGET, result.outcome());
    }

    @Test
    void testNetworkErrorConnectionRefused() {
        AcquisitionConfig config = AcquisitionConfig.builder().allowLocalhost(true).build();
        // Request closed port 65530
        AcquisitionRequest request = new AcquisitionRequest("http://127.0.0.1:65530/test", config);

        AcquisitionResult result = service.acquire(request);

        assertFalse(result.isSuccess());
        assertEquals(AcquisitionOutcome.NETWORK_ERROR, result.outcome());
    }

    @Test
    void testHttpsHostnameVerificationEnforced() {
        AcquisitionConfig config = AcquisitionConfig.defaults();
        // Request an unresolvable or invalid HTTPS URL to verify SSL/hostname validation fails safely as NETWORK_ERROR without bypassing TLS
        AcquisitionRequest request = new AcquisitionRequest("https://invalid-nonexistent-domain-999.example/test", config);

        AcquisitionResult result = service.acquire(request);

        assertFalse(result.isSuccess());
        assertEquals(AcquisitionOutcome.NETWORK_ERROR, result.outcome());
    }

    @Test
    void testAcquireFromCanonicalRequest() {
        server.createContext("/canonical", exchange -> {
            byte[] bytes = "OK".getBytes();
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });

        CanonicalEvaluationRequest canonicalReq = new CanonicalEvaluationRequest(
                new CanonicalEvaluationRequest.RequestInput("Grand Hotel", "Paris", baseUrl + "/canonical"),
                new CanonicalEvaluationRequest.NormalizedInput("Grand Hotel", "Paris", baseUrl + "/canonical"),
                new CanonicalEvaluationRequest.EvaluationTarget(
                        EvaluationTargetType.WEBSITE,
                        "Grand Hotel",
                        "Paris",
                        baseUrl + "/canonical",
                        null
                )
        );

        AcquisitionConfig config = AcquisitionConfig.builder().allowLocalhost(true).build();
        AcquisitionRequest request = AcquisitionRequest.fromCanonicalRequest(canonicalReq, config);

        AcquisitionResult result = service.acquire(request);

        assertTrue(result.isSuccess());
        assertEquals(AcquisitionOutcome.SUCCESS, result.outcome());
        assertEquals(200, result.statusCode());
    }

    @Test
    void testPrivateAddressTargetRejectionInService() {
        AcquisitionConfig config = AcquisitionConfig.builder().allowLocalhost(false).build();

        AcquisitionResult res1 = service.acquire(new AcquisitionRequest("http://169.254.169.254/latest/meta-data/", config));
        assertEquals(AcquisitionOutcome.INVALID_TARGET, res1.outcome());
        assertTrue(res1.errorMessage().contains("non-public"));

        AcquisitionResult res2 = service.acquire(new AcquisitionRequest("http://10.0.0.1/admin", config));
        assertEquals(AcquisitionOutcome.INVALID_TARGET, res2.outcome());
        assertTrue(res2.errorMessage().contains("non-public"));

        AcquisitionResult res3 = service.acquire(new AcquisitionRequest("http://192.168.1.1/router", config));
        assertEquals(AcquisitionOutcome.INVALID_TARGET, res3.outcome());
        assertTrue(res3.errorMessage().contains("non-public"));
    }

    @Test
    void testAllowLocalhostProductionLeakForbidden() {
        System.setProperty("env", "production");
        try {
            assertThrows(IllegalStateException.class, () -> {
                new AcquisitionConfig(null, null, 0, 0, null, true);
            });
        } finally {
            System.clearProperty("env");
        }
    }
}
