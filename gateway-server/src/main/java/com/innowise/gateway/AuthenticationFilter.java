package com.innowise.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;


@Component
@Slf4j
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final List<String> WhiteList = List.of(
            "/auth/login",
            "/auth/register"
    );

    private final String ValidationEndpoint = "/validateToken";

    final WebClient webClient;

    public AuthenticationFilter(@Qualifier("authWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (request.getMethod() != HttpMethod.GET && !WhiteList.contains(request.getPath().value())) {
            return Mono.defer(() -> Mono.just(request.getCookies()))
                    .<MultiValueMap<String, HttpCookie>>handle((cookies, sink) -> {
                        List<HttpCookie> emptyList = List.of();
                        var tokenCookieExist = cookies.getOrDefault("token", emptyList).size() > 0;
                        var usernameCookieExist = cookies.getOrDefault("username", emptyList).size() > 0;
                        var rolesCookieExist = cookies.getOrDefault("roles", emptyList).size() > 0;

                        if (tokenCookieExist && usernameCookieExist && rolesCookieExist) {
                            sink.next(cookies);
                        } else {
                            sink.error(new AuthenticationCookiesAbsentException());
                        }
                    })
                    .doOnNext((__) -> log.info("Send validation request"))
                    .map((cookies) -> requestWithCookies(
                            webClient.get().uri(ValidationEndpoint),
                            cookies
                    ))
                    .flatMap((req) -> req.retrieve().toBodilessEntity())
                    .onErrorMap((throwable -> {
                        if (throwable instanceof WebClientResponseException err && err.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
                            return new JwtValidationException(throwable);
                        }
                        return throwable;
                    }))
                    .doOnNext(res -> log.info("Validation request status code : {}", res.getStatusCode().value()))
                    .filter(res -> res.getStatusCode().is2xxSuccessful())
                    .flatMap((__) -> chain.filter(exchange));
        }

        return chain.filter(exchange);
    }

    public WebClient.RequestHeadersSpec<?> requestWithCookies(WebClient.RequestHeadersSpec<?> webClientRequest, MultiValueMap<String, HttpCookie> cookies) {
        var mutWebClientRequest = new WebClient.RequestHeadersSpec[]{webClientRequest};

        cookies.forEach((__, keyToCookies) -> {
            for (HttpCookie cookie : keyToCookies) {
                mutWebClientRequest[0] = mutWebClientRequest[0].cookie(cookie.getName(), cookie.getValue());
            }
        });

        return mutWebClientRequest[0];
    }


//    ignore...
//    public <Key, From, To> MultiValueMap<Key, To> mapMultivaluedMapValue(
//            MultiValueMap<Key, From> multiValueMap,
//            Function<From, To> mapper,
//            Supplier<MultiValueMap<Key, To>> implMultiValueMap
//    ) {
//        Map<Key, List<To>> transformedMap = multiValueMap.keySet().stream().collect(
//                Collectors.toMap(
//                        Function.identity(),
//                        (key) -> multiValueMap.get(key).stream().map(mapper).toList()
//                )
//        );
//        MultiValueMap<Key, To> transformedMultiMap = implMultiValueMap.get();
//        transformedMultiMap.putAll(transformedMap);
//        return transformedMultiMap;
//    }

    @Override
    public int getOrder() {
        return -1;
    }
}
