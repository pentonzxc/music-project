package com.innowise;

import com.innowise.localstorage.DefaultLocalStorage;
import com.innowise.localstorage.LocalStorage;

import java.util.Arrays;

class Tests {
    public static void main(String[] args) throws InterruptedException {
        LocalStorage localStorage = new DefaultLocalStorage();


        byte[] bytes = "ku".getBytes();

        String metadata = "value:2";

        localStorage.put(
                        "hello",
                        bytes,
                        metadata,
                        "txt",
                        "json",
                        true
                )
                .doOnNext((sts) -> System.out.println(Arrays.toString(sts)))
                .flatMap((sts) -> localStorage.<byte[], String>get(sts[0].split("[.]")[0], "txt", "json"))
                .doOnNext(tuple -> {
                    assert new String(tuple.getT1()).equals(new String(bytes));
                    assert tuple.getT2().equals(metadata);
                })
                .block();
    }
}
