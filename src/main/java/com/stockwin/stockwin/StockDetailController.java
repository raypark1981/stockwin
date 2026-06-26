package com.stockwin.stockwin;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * stock-detail-view.fxml 컨트롤러.
 *
 * 종목 하나에 대한 상세 정보를 표시한다.
 * openStockDetailTab()에서 fxml 로드 후 initData()를 호출해서 종목 정보를 전달한다.
 */
public class StockDetailController {

    // =========================================================================
    // @FXML 주입: 상단 기본 정보
    // =========================================================================
    @FXML private Label lblStockCode;
    @FXML private Label lblStockName;
    @FXML private Label lblCurrentPrice;
    @FXML private Label lblChangeRate;
    @FXML private Label lblStatus;

    @FXML private Label lblPrevClose;
    @FXML private Label lblOpen;
    @FXML private Label lblHigh;
    @FXML private Label lblLow;
    @FXML private Label lblVolume;
    @FXML private Label lblTurnover;

    // =========================================================================
    // @FXML 주입: 매수 영역
    // =========================================================================
    @FXML private Label lblBuyStartPrice;
    @FXML private Label lblBuyExpectedPrice;
    @FXML private Label lblBuyExpectedVolume;
    @FXML private Label lblBuyOrderStatus;
    @FXML private Label lblBuyFilled;
    @FXML private Label lblAvgBuyPrice;

    // =========================================================================
    // @FXML 주입: 매도 영역
    // =========================================================================
    @FXML private Label lblHoldQty;
    @FXML private Label lblSellAvgBuyPrice;
    @FXML private Label lblTargetRate;
    @FXML private Label lblTargetPrice;
    @FXML private Label lblStopLossPrice;
    @FXML private Label lblCurrentRate;
    @FXML private Label lblSellOrderStatus;
    @FXML private Label lblSellFilled;
    @FXML private Label lblUnfilledQty;

    // =========================================================================
    // @FXML 주입: 호가 영역
    // =========================================================================
    @FXML private TableView<HokaRow> askTable;   // 매도 호가 테이블
    @FXML private TableView<HokaRow> bidTable;   // 매수 호가 테이블
    @FXML private Label lblHokaCurrentPrice;
    @FXML private Label lblStrength;
    @FXML private ListView<String> tradeList;    // 실시간 체결 내역

    // =========================================================================
    // 데이터 모델
    // =========================================================================

    /**
     * 호가 한 행(row)을 표현하는 record (Java 16+).
     * record는 불변 데이터 클래스를 간결하게 선언하는 문법이다.
     * 자동으로 생성자, getter, equals, hashCode, toString을 만들어준다.
     *
     * @param price  가격 문자열 (포맷 완료된 상태)
     * @param volume 잔량 문자열
     */
    private record HokaRow(String price, String volume) {}

    private final NumberFormat numFmt = NumberFormat.getNumberInstance(Locale.KOREA);

    // =========================================================================
    // 초기화
    // =========================================================================

    @FXML
    public void initialize() {
        setupHokaTables();  // 호가 테이블 컬럼 정의
    }

    /**
     * 탭이 열릴 때 외부(HelloController)에서 호출해서 종목 정보를 전달한다.
     *
     * @param stockCode 종목코드
     * @param stockName 종목명
     */
    public void initData(String stockCode, String stockName) {
        // 기본 정보 설정
        lblStockCode.setText(stockCode);
        lblStockName.setText(stockName);

        // 나머지 데이터는 실제 API 연동 전까지 샘플로 채운다
        loadSampleData(stockCode);
    }

    // =========================================================================
    // 호가 테이블 설정
    // =========================================================================

    /**
     * askTable (매도 호가), bidTable (매수 호가) 컬럼을 정의한다.
     *
     * 매도 호가 구조: [잔량] [매도호가] → 높은 가격이 위에 표시
     * 매수 호가 구조: [매수호가] [잔량]  → 높은 가격이 위에 표시
     */
    private void setupHokaTables() {

        // ── 매도 호가 테이블 ──────────────────────────────
        TableColumn<HokaRow, String> askVolumeCol = new TableColumn<>("잔량");
        // ReadOnlyStringWrapper: 단순 문자열을 Property로 감싸는 편의 클래스
        // TableView는 Property 타입을 요구하기 때문에 사용
        askVolumeCol.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().volume()));
        askVolumeCol.setPrefWidth(80);
        askVolumeCol.setStyle("-fx-alignment: CENTER-RIGHT; -fx-text-fill: #0000CC;");

        TableColumn<HokaRow, String> askPriceCol = new TableColumn<>("매도호가");
        askPriceCol.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().price()));
        askPriceCol.setPrefWidth(100);
        // 매도 호가는 파란색 (하락 방향)
        askPriceCol.setStyle("-fx-alignment: CENTER-RIGHT; -fx-text-fill: #0000CC; -fx-font-weight: bold;");

        askTable.getColumns().addAll(askVolumeCol, askPriceCol);
        // 테이블 자체 컬럼 리사이즈: 컬럼들이 전체 너비를 채우도록
        askTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // ── 매수 호가 테이블 ──────────────────────────────
        TableColumn<HokaRow, String> bidPriceCol = new TableColumn<>("매수호가");
        bidPriceCol.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().price()));
        bidPriceCol.setPrefWidth(100);
        // 매수 호가는 빨간색 (상승 방향)
        bidPriceCol.setStyle("-fx-alignment: CENTER-RIGHT; -fx-text-fill: #CC0000; -fx-font-weight: bold;");

        TableColumn<HokaRow, String> bidVolumeCol = new TableColumn<>("잔량");
        bidVolumeCol.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().volume()));
        bidVolumeCol.setPrefWidth(80);
        bidVolumeCol.setStyle("-fx-alignment: CENTER-RIGHT; -fx-text-fill: #CC0000;");

        bidTable.getColumns().addAll(bidPriceCol, bidVolumeCol);
        bidTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // =========================================================================
    // 상태값 업데이트 메서드 (실시간 데이터 수신 시 호출 예정)
    // =========================================================================

    /**
     * 종목 상태 레이블 텍스트와 배경색을 함께 업데이트한다.
     *
     * 상태에 따라 직관적인 색상으로 표시:
     *   감시중/주문중  → 주황/노랑 계열
     *   체결(성공)     → 초록 계열
     *   종료           → 회색
     */
    public void updateStatus(String status) {
        lblStatus.setText(status);

        String bgColor = switch (status) {
            case "감시중"      -> "#1565C0"; // 파랑
            case "매수주문완료" -> "#E65100"; // 주황
            case "매수체결"    -> "#2E7D32"; // 진한 초록
            case "매도감시중"  -> "#6A1B9A"; // 보라
            case "매도주문완료" -> "#4A148C"; // 진한 보라
            case "매도체결"    -> "#00695C"; // 청록
            case "전략종료"    -> "#424242"; // 진한 회색
            default            -> "#555555"; // 기본 (후보 등)
        };

        lblStatus.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                "-fx-text-fill: #FFFFFF;" +
                "-fx-padding: 3 12 3 12;" +
                "-fx-background-radius: 4;" +
                "-fx-font-weight: bold;"
        );
    }

    /**
     * 현재가와 등락률을 업데이트한다.
     * 한국 주식 관례: 상승=빨강, 하락=파랑
     *
     * @param price      현재가
     * @param changeRate 등락률 (예: +29.89, -1.23)
     */
    public void updatePrice(long price, double changeRate) {
        String priceStr = numFmt.format(price);
        String rateStr  = String.format("%+.2f%%", changeRate);

        lblCurrentPrice.setText(priceStr);
        lblChangeRate.setText(rateStr);
        lblHokaCurrentPrice.setText(priceStr);

        // 색상: 상승=빨강(#FF4444), 하락=파랑(#4444FF), 보합=회색
        String color;
        if (changeRate > 0)      color = "#FF4444";
        else if (changeRate < 0) color = "#4444FF";
        else                     color = "#AAAAAA";

        lblCurrentPrice.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 22px; -fx-font-weight: bold;");
        lblChangeRate.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 15px; -fx-font-weight: bold;");

        // 현재 수익률 색상도 같은 기준으로 처리
        if (changeRate > 0)
            lblCurrentRate.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #CC0000;");
        else if (changeRate < 0)
            lblCurrentRate.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0000CC;");
    }

    // =========================================================================
    // 샘플 데이터 (API 연동 전 테스트용)
    // =========================================================================

    private void loadSampleData(String stockCode) {

        // ── 상단 기본 정보 ──
        lblCurrentPrice.setText("73,400");
        lblChangeRate.setText("+29.89%");
        lblPrevClose.setText("56,500");
        lblOpen.setText("58,000");
        lblHigh.setText("74,100");
        lblLow.setText("57,800");
        lblVolume.setText("45,234,567");
        lblTurnover.setText("3,312억");
        updateStatus("감시중");

        // ── 매수 영역 ──
        lblBuyStartPrice.setText("57,000");
        lblBuyExpectedPrice.setText("57,200");
        lblBuyExpectedVolume.setText("12,345");
        lblBuyOrderStatus.setText("대기");
        lblBuyFilled.setText("미체결");
        lblAvgBuyPrice.setText("-");

        // ── 매도 영역 ──
        lblHoldQty.setText("0 주");
        lblSellAvgBuyPrice.setText("-");
        lblTargetRate.setText("+5.00%");
        lblTargetPrice.setText("60,060");
        lblStopLossPrice.setText("54,150");
        lblCurrentRate.setText("-");
        lblSellOrderStatus.setText("-");
        lblSellFilled.setText("-");
        lblUnfilledQty.setText("-");

        // ── 호가 영역: 매도 10호가 (높은 가격부터) ──
        ObservableList<HokaRow> askData = FXCollections.observableArrayList(
                new HokaRow("58,600", "2,100"),
                new HokaRow("58,500", "1,350"),
                new HokaRow("58,400", "3,720"),
                new HokaRow("58,300", "890"),
                new HokaRow("58,200", "4,560"),
                new HokaRow("58,100", "2,230"),
                new HokaRow("58,000", "6,780"),
                new HokaRow("57,900", "1,100"),
                new HokaRow("57,800", "3,450"),
                new HokaRow("57,700", "2,870")
        );
        askTable.setItems(askData);

        // ── 호가 영역: 매수 10호가 (높은 가격부터) ──
        ObservableList<HokaRow> bidData = FXCollections.observableArrayList(
                new HokaRow("57,600", "5,230"),
                new HokaRow("57,500", "3,410"),
                new HokaRow("57,400", "7,890"),
                new HokaRow("57,300", "2,100"),
                new HokaRow("57,200", "4,560"),
                new HokaRow("57,100", "1,230"),
                new HokaRow("57,000", "8,900"),
                new HokaRow("56,900", "3,670"),
                new HokaRow("56,800", "2,450"),
                new HokaRow("56,700", "6,120")
        );
        bidTable.setItems(bidData);

        // ── 체결강도 ──
        lblHokaCurrentPrice.setText("57,650");
        lblStrength.setText("123.5%");

        // ── 실시간 체결 내역 (최신이 위에 오도록 추가) ──
        ObservableList<String> trades = FXCollections.observableArrayList(
                "09:32:14  57,650  ▲  500주",
                "09:32:11  57,600  ▲  1,200주",
                "09:32:08  57,550  ▼  300주",
                "09:32:05  57,600  ▲  800주",
                "09:32:01  57,500  ▼  200주"
        );
        tradeList.setItems(trades);
    }
}