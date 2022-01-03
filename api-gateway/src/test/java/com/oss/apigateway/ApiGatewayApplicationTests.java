//package com.addverb.apigateway;
//
//import com.auth0.jwk.Jwk;
//import com.auth0.jwk.UrlJwkProvider;
//import com.auth0.jwt.JWT;
//import com.auth0.jwt.algorithms.Algorithm;
//import com.auth0.jwt.interfaces.DecodedJWT;
//import org.junit.Test;
//
//import java.net.URL;
//import java.security.interfaces.RSAPublicKey;
//import java.util.Date;
//import java.util.List;
//
////@SpringBootTest
//class ApiGatewayApplicationTests {
//
//	@Test
//	void contextLoads() {
//	}
//
//	@Test
//	void name() {
//		String authHeader = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICItNTlmMTdQOXYyQlk0VTltSFE2RVVCVUpTY2hDa1JuOWNqTk1EdXdaeHNNIn0.eyJleHAiOjE2MzgyNzMwMTEsImlhdCI6MTYzODI3Mjk1MSwiYXV0aF90aW1lIjoxNjM4MjcyOTUxLCJqdGkiOiJlZWNjZGM2OC02ZDU1LTQyY2MtODkwOS1lYjFiM2JlZDg4ZDQiLCJpc3MiOiJodHRwOi8vMTI3LjAuMC4xOjgwODAvYXV0aC9yZWFsbXMvbWFzdGVyIiwic3ViIjoiN2YxZmUwNGEtMWIzMS00MDM5LTlkZGMtNjk0OWQ1N2MzOGRjIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoic2VjdXJpdHktYWRtaW4tY29uc29sZSIsIm5vbmNlIjoiZDhmZjVlNzItZjcwMy00NTE3LTkxMmEtZGYxNzRkM2FhNTU1Iiwic2Vzc2lvbl9zdGF0ZSI6ImViZTUxMzc0LTA4MzItNDY3MC05YmM0LWRkZWNlYzdlNTBiMCIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOlsiaHR0cDovLzEyNy4wLjAuMTo4MDgwIl0sInNjb3BlIjoib3BlbmlkIHByb2ZpbGUgZW1haWwiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsInByZWZlcnJlZF91c2VybmFtZSI6ImFkbWluIn0.HWhXP4Ma7Tt3dRmnbgnwE5FQw71Sv9O2BJtJrh3CFo8Iy4TzFJ7cZ7jasuYRdEKTsApgdjgp8-Wvobllp_WCaT1MMZxazH9y-3Ol98aDoDG-FM-6VgFC3DPqF32ExNPScr9GMM7vkCOoZZ4RFcmaL0yT7e-hbuPbiocG0mu9kEv_g1xTCieop1HuFth-H_bc95O-Ox_twT8yvbgGB-Zm3PvXUCnqoCn3bq5Tubjzce_jcW3IcAKM39AzfO53XGGBXPYgcTMRdpqrK0AMa7Xw-OlfPx09jiGNd8qQK7ELYEGjsiTF6gji0lJO3tfLhj2GYOAruZEKvVif5QB_xKXTzA";
//		try {
//			DecodedJWT jwt = JWT.decode(authHeader.replace("Bearer", "").trim());
//
//			// check JWT is valid
//			Jwk jwk =
//					new UrlJwkProvider(new URL("http://localhost:8080/auth/realms/master/protocol/openid-connect" +
//							"/certs")).get("-59f17P9v2BY4U9mHQ6EUBUJSchCkRn9cjNMDuwZxsM");
//			Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
//
//			algorithm.verify(jwt);
//
//			// check JWT role is correct
//			List<String> roles = ((List)jwt.getClaim("realm_access").asMap().get("roles"));
//			if(!roles.contains("teacher"))
//				throw new Exception("not a teacher role");
//
//			// check JWT is still active
//			Date expiryDate = jwt.getExpiresAt();
//			if(expiryDate.before(new Date()))
//				throw new Exception("token is expired");
//
//			// all validation passed
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//}
