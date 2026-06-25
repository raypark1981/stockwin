package com.stockwin.stockwin;

import javafx.beans.property.*;

/**
 * 전일 상친 목록 테이블의 한 행(row) 데이터 모델.
 *
 * JavaFX Property 타입을 사용하면 TableView가 값 변경을 자동 감지해서
 * UI를 갱신한다. 이것이 일반 필드(int, String)와의 차이점이다.
 *
 * 각 필드마다 세 가지를 제공한다:
 *   1. xxxProperty()  → TableView 바인딩용 (PropertyValueFactory에서 사용)
 *   2. getXxx()       → 단순 값 읽기용 (이벤트 처리 등)
 *   3. setXxx()       → 값 변경용 (필요 시 추가)
 */
public class StockItem {

    // StringProperty: 문자열 타입 Property
    private final StringProperty stockCode;           // 종목코드 (예: "005930")
    private final StringProperty stockName;           // 종목명   (예: "삼성전자")

    // LongProperty: 정수(long) 타입 Property - 가격/거래량처럼 큰 수에 사용
    private final LongProperty prevClosePrice;        // 전일 종가
    private final LongProperty prevVolume;            // 전일 거래량
    private final LongProperty marketCap;             // 시가총액 (억원 단위)
    private final LongProperty floatingShares;        // 유통주식수

    // DoubleProperty: 실수(double) 타입 Property - 퍼센트/배수값에 사용
    private final DoubleProperty prevChangeRate;      // 전일 등락률 (%)
    private final DoubleProperty per;                 // PER
    private final DoubleProperty volumeIncreaseRate;  // 거래량 증가율 (%)

    private final StringProperty sector;              // 테마/섹터
    private final StringProperty investmentWarning;   // 투자주의/경고 여부 (예: "-", "투자주의")

    // 생성자: 모든 필드를 받아 Property로 감싼다
    public StockItem(String stockCode, String stockName,
                     long prevClosePrice, double prevChangeRate,
                     long prevVolume, long marketCap,
                     double per, long floatingShares,
                     double volumeIncreaseRate, String sector,
                     String investmentWarning) {

        this.stockCode          = new SimpleStringProperty(stockCode);
        this.stockName          = new SimpleStringProperty(stockName);
        this.prevClosePrice     = new SimpleLongProperty(prevClosePrice);
        this.prevChangeRate     = new SimpleDoubleProperty(prevChangeRate);
        this.prevVolume         = new SimpleLongProperty(prevVolume);
        this.marketCap          = new SimpleLongProperty(marketCap);
        this.per                = new SimpleDoubleProperty(per);
        this.floatingShares     = new SimpleLongProperty(floatingShares);
        this.volumeIncreaseRate = new SimpleDoubleProperty(volumeIncreaseRate);
        this.sector             = new SimpleStringProperty(sector);
        this.investmentWarning  = new SimpleStringProperty(investmentWarning);
    }

    // -------------------------------------------------------------------------
    // Property getter: TableColumn.setCellValueFactory()에서 호출됨
    // 메서드명 규칙: 반드시 "필드명 + Property" 형식이어야 PropertyValueFactory가 찾는다
    // -------------------------------------------------------------------------
    public StringProperty stockCodeProperty()          { return stockCode; }
    public StringProperty stockNameProperty()          { return stockName; }
    public LongProperty prevClosePriceProperty()       { return prevClosePrice; }
    public DoubleProperty prevChangeRateProperty()     { return prevChangeRate; }
    public LongProperty prevVolumeProperty()           { return prevVolume; }
    public LongProperty marketCapProperty()            { return marketCap; }
    public DoubleProperty perProperty()                { return per; }
    public LongProperty floatingSharesProperty()       { return floatingShares; }
    public DoubleProperty volumeIncreaseRateProperty() { return volumeIncreaseRate; }
    public StringProperty sectorProperty()             { return sector; }
    public StringProperty investmentWarningProperty()  { return investmentWarning; }

    // -------------------------------------------------------------------------
    // 일반 getter: 이벤트 처리나 조건 비교 등 단순 값 읽기용
    // -------------------------------------------------------------------------
    public String getStockCode()          { return stockCode.get(); }
    public String getStockName()          { return stockName.get(); }
    public long   getPrevClosePrice()     { return prevClosePrice.get(); }
    public double getPrevChangeRate()     { return prevChangeRate.get(); }
    public long   getPrevVolume()         { return prevVolume.get(); }
    public long   getMarketCap()          { return marketCap.get(); }
    public double getPer()                { return per.get(); }
    public long   getFloatingShares()     { return floatingShares.get(); }
    public double getVolumeIncreaseRate() { return volumeIncreaseRate.get(); }
    public String getSector()             { return sector.get(); }
    public String getInvestmentWarning()  { return investmentWarning.get(); }
}
