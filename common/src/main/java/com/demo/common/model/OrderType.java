package com.demo.common.model;

/** MARKET fills immediately, LIMIT waits for price, SL triggers then becomes LIMIT, SLM triggers then becomes MARKET. */
public enum OrderType { MARKET, LIMIT, SL, SLM }
